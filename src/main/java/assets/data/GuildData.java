package assets.data;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.data.single.AutoMessageData;
import assets.data.single.Join2CreateChannelData;
import assets.data.single.PenaltyData;
import assets.data.single.PollData;
import assets.data.single.ReactionRoleData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class GuildData {

	private final JSONObject data;
	private final Guild guild;
	private List<Role> admin_roles, moderation_roles, support_roles, bot_auto_roles, user_auto_roles;
	private AutoMessageData boost_message, goodbye_message, level_up_message, welcome_message;
	private TextChannel community_inbox_channel, moderation_inbox_channel, suggestion_inbox_channel;
	private VoiceChannel support_talk;
	private ConcurrentHashMap<VoiceChannel, Join2CreateChannelData> join2create_channels;
	private ConcurrentHashMap<VoiceChannel, VoiceChannel> join2create_channel_links;
	private ConcurrentHashMap<Category, Member> custom_channel_categories;
	private Message offline_message;
	private ConcurrentHashMap<Integer, Role> level_rewards;
	private ConcurrentHashMap<Integer, PenaltyData> penalties;
	private ConcurrentHashMap<TextChannel, ModMailGuildData> modmails;
	private ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, PollData>> polls;
	private ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, ReactionRoleData>> reaction_roles;

	public GuildData(Guild guild, JSONObject rawData) {
		this.data = rawData;
		this.guild = guild;

		admin_roles = this.getRolesFromArrayKeys("static_roles", "admin_roles");
		moderation_roles = this.getRolesFromArrayKeys("static_roles", "moderation_roles");
		support_roles = this.getRolesFromArrayKeys("static_roles", "support_roles");
		bot_auto_roles = this.getRolesFromArrayKeys("auto_roles", "bot_auto_roles");
		user_auto_roles = this.getRolesFromArrayKeys("auto_roles", "user_auto_roles");

		boost_message = new AutoMessageData(guild, data.getJSONObject("auto_messages").getJSONObject("boost_message"));
		goodbye_message = new AutoMessageData(guild, data.getJSONObject("auto_messages").getJSONObject("goodbye_message"));
		level_up_message = new AutoMessageData(guild, data.getJSONObject("auto_messages").getJSONObject("level_up_message"));
		welcome_message = new AutoMessageData(guild, data.getJSONObject("auto_messages").getJSONObject("welcome_message"));

		community_inbox_channel = guild.getTextChannelById(data.getJSONObject("static_channels").getLong("community_inbox_channel"));
		moderation_inbox_channel = guild.getTextChannelById(data.getJSONObject("static_channels").getLong("moderation_inbox_channel"));
		suggestion_inbox_channel = guild.getTextChannelById(data.getJSONObject("static_channels").getLong("suggestion_inbox_channel"));

		support_talk = guild.getVoiceChannelById(data.getJSONObject("static_channels").getLong("support_talk"));

		JSONArray offline_message_data = data.getJSONArray("offline_message");
		offline_message = guild.getTextChannelById(offline_message_data.getLong(1)).retrieveMessageById(offline_message_data.getLong(0)).complete();

		this.join2create_channels = new ConcurrentHashMap<>();
		JSONObject j2c_channels_data = data.getJSONObject("join2create_channels");
		j2c_channels_data.keySet().forEach(channelId -> join2create_channels.put(guild.getVoiceChannelById(channelId), new Join2CreateChannelData(guild, j2c_channels_data.getJSONObject(channelId))));

		this.join2create_channel_links = new ConcurrentHashMap<>();
		JSONObject j2c_channels_link_data = data.getJSONObject("join2create_channel_links");
		j2c_channels_link_data.keySet().forEach(channelId -> join2create_channel_links.put(guild.getVoiceChannelById(channelId), guild.getVoiceChannelById(j2c_channels_data.getLong(channelId))));

		this.custom_channel_categories = new ConcurrentHashMap<>();
		JSONObject cc_categories_data = data.getJSONObject("custom_channel_categories");
		cc_categories_data.keySet().forEach(categoryId -> custom_channel_categories.put(guild.getCategoryById(categoryId), guild.getMemberById(cc_categories_data.getLong(categoryId))));

		this.level_rewards = new ConcurrentHashMap<>();
		JSONObject lvl_reward_data = data.getJSONObject("level_rewards");
		lvl_reward_data.keySet().forEach(level_count -> level_rewards.put(Integer.valueOf(level_count), guild.getRoleById(lvl_reward_data.getLong(level_count))));

		this.penalties = new ConcurrentHashMap<>();
		JSONObject penalties_data = data.getJSONObject("penalties");
		penalties_data.keySet().forEach(warning_count -> penalties.put(Integer.valueOf(warning_count), new PenaltyData(guild, penalties_data.getJSONObject(warning_count))));

		this.modmails = new ConcurrentHashMap<>();
		JSONObject modmails_data = data.getJSONObject("modmails");
		modmails_data.keySet().forEach(channelId -> modmails.put(guild.getTextChannelById(channelId), new ModMailGuildData(guild, modmails_data.getJSONObject(channelId))));

		this.polls = new ConcurrentHashMap<>();
		JSONObject polls_data = data.getJSONObject("polls");
		polls_data.keySet().forEach(channelId -> {
			TextChannel channel = guild.getTextChannelById(channelId);
			ConcurrentHashMap<Message, PollData> pollSubMap = new ConcurrentHashMap<>();
			JSONObject polls_sub_data = polls_data.getJSONObject(channelId);
			polls_sub_data.keySet().forEach(messageId -> {
				pollSubMap.put(channel.retrieveMessageById(messageId).complete(), new PollData(guild, polls_sub_data.getJSONObject(messageId)));
			});
			polls.put(channel, pollSubMap);
		});

		this.reaction_roles = new ConcurrentHashMap<>();
		JSONObject reaction_roles_data = data.getJSONObject("reaction_roles");
		reaction_roles_data.keySet().forEach(channelId -> {
			TextChannel channel = guild.getTextChannelById(channelId);
			ConcurrentHashMap<Message, ReactionRoleData> reactionRoleSubMap = new ConcurrentHashMap<>();
			JSONObject reaction_role_sub_data = reaction_roles_data.getJSONObject(channelId);
			reaction_role_sub_data.keySet().forEach(messageId -> {
				reactionRoleSubMap.put(channel.retrieveMessageById(messageId).complete(), new ReactionRoleData(guild, reaction_role_sub_data.getJSONObject(messageId)));
			});
			reaction_roles.put(channel, reactionRoleSubMap);
		});
	}

	private List<Role> getRolesFromArrayKeys(String primary, String secondary) {
		JSONArray values = data.getJSONObject(primary).getJSONArray(secondary);
		List<Role> roles = new LinkedList<>();
		for (int i = 0; i < values.length(); i++) {
			roles.add(guild.getRoleById(values.getLong(i)));
		}
		return roles;
	}

	public List<Role> getAdminRoles() {
		return admin_roles;
	}

	public void setAdminRoles(List<Role> admin_roles) {
		this.admin_roles = admin_roles;
	}

	public List<Role> getModerationRoles() {
		return moderation_roles;
	}

	public void setModerationRoles(List<Role> moderation_roles) {
		this.moderation_roles = moderation_roles;
	}

	public List<Role> getSupportRoles() {
		return support_roles;
	}

	public void setSupportRoles(List<Role> support_roles) {
		this.support_roles = support_roles;
	}

	public List<Role> getBotAutoRoles() {
		return bot_auto_roles;
	}

	public void setBotAutoRoles(List<Role> bot_auto_roles) {
		this.bot_auto_roles = bot_auto_roles;
	}

	public List<Role> getUserAutoRoles() {
		return user_auto_roles;
	}

	public void setUserAutoRoles(List<Role> user_auto_roles) {
		this.user_auto_roles = user_auto_roles;
	}

	public AutoMessageData getBoostMessage() {
		return boost_message;
	}

	public void setBoostMessage(AutoMessageData boost_message) {
		this.boost_message = boost_message;
	}

	public AutoMessageData getGoodbyeMessage() {
		return goodbye_message;
	}

	public void setGoodbyeMessage(AutoMessageData goodbye_message) {
		this.goodbye_message = goodbye_message;
	}

	public AutoMessageData getLevelUpMessage() {
		return level_up_message;
	}

	public void setLevelUpMssage(AutoMessageData level_up_message) {
		this.level_up_message = level_up_message;
	}

	public AutoMessageData getWelcomeMessage() {
		return welcome_message;
	}

	public void setWelcomeMessage(AutoMessageData welcome_message) {
		this.welcome_message = welcome_message;
	}

	public TextChannel getCommunityInboxChannel() {
		return community_inbox_channel;
	}

	public void setCommunityInboxChannel(TextChannel community_inbox_channel) {
		this.community_inbox_channel = community_inbox_channel;
	}

	public TextChannel getModerationInboxChannel() {
		return moderation_inbox_channel;
	}

	public void setModerationInboxChannel(TextChannel moderation_inbox_channel) {
		this.moderation_inbox_channel = moderation_inbox_channel;
	}

	public TextChannel getSuggestionInboxChannel() {
		return suggestion_inbox_channel;
	}

	public void setSuggestionInboxChannel(TextChannel suggestion_inbox_channel) {
		this.suggestion_inbox_channel = suggestion_inbox_channel;
	}

	public VoiceChannel getSupportTalk() {
		return support_talk;
	}

	public void setSupportTalk(VoiceChannel support_talk) {
		this.support_talk = support_talk;
	}

	public Message getOfflineMessage() {
		return offline_message;
	}

	public void setOfflineMessage(Message offline_message) {
		this.offline_message = offline_message;
	}
	
	public ConcurrentHashMap<VoiceChannel, Join2CreateChannelData> getJoin2CreateChannels() {
		return this.join2create_channels;
	}
	
	public void setJoin2CreateChannels(ConcurrentHashMap<VoiceChannel, Join2CreateChannelData> join2create_channels) {
		this.join2create_channels = join2create_channels;
	}
	
	public ConcurrentHashMap<VoiceChannel, VoiceChannel> getJoin2CreateChannelLinks() {
		return this.join2create_channel_links;
	}
	
	public void setJoin2CreateChannelLinks(ConcurrentHashMap<VoiceChannel, VoiceChannel> join2create_channel_links) {
		this.join2create_channel_links = join2create_channel_links;
	}

	public ConcurrentHashMap<Category, Member> getCustomChannelCategories() {
		return custom_channel_categories;
	}

	public void setCustomChannelCategories(ConcurrentHashMap<Category, Member> custom_channel_categories) {
		this.custom_channel_categories = custom_channel_categories;
	}

	public ConcurrentHashMap<Integer, Role> getLevelRewards() {
		return level_rewards;
	}

	public void setLevelRewards(ConcurrentHashMap<Integer, Role> level_rewards) {
		this.level_rewards = level_rewards;
	}

	public ConcurrentHashMap<Integer, PenaltyData> getPenalties() {
		return penalties;
	}

	public void setPenalties(ConcurrentHashMap<Integer, PenaltyData> penalties) {
		this.penalties = penalties;
	}

	public ConcurrentHashMap<TextChannel, ModMailGuildData> getModmails() {
		return modmails;
	}

	public void setModmails(ConcurrentHashMap<TextChannel, ModMailGuildData> modmails) {
		this.modmails = modmails;
	}

	public ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, PollData>> getPolls() {
		return polls;
	}

	public void setPolls(ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, PollData>> polls) {
		this.polls = polls;
	}

	public ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, ReactionRoleData>> getReactionRoles() {
		return reaction_roles;
	}

	public void setReactionRoles(ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, ReactionRoleData>> reaction_roles) {
		this.reaction_roles = reaction_roles;
	}
}