package assets.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.data.single.AutoMessageData;
import assets.data.single.Join2CreateChannelData;
import assets.data.single.ModMailData;
import assets.data.single.PenaltyData;
import assets.data.single.PollData;
import assets.data.single.ReactionRoleData;
import base.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class GuildData implements DataContainer {

	private final Guild guild;
	private List<Role> admin_roles = new LinkedList<>();
	private List<Role> custom_channel_policing_roles = new LinkedList<>();
	private List<Role> moderation_roles = new LinkedList<>();
	private List<Role> support_roles = new LinkedList<>();
	private List<Role> bot_auto_roles = new LinkedList<>();
	private List<Role> user_auto_roles = new LinkedList<>();
	private AutoMessageData boost_message, goodbye_message, level_up_message, welcome_message;
	private TextChannel community_inbox_channel, moderation_inbox_channel, suggestion_inbox_channel;
	private VoiceChannel support_talk;
	private Message offline_message;
	private ConcurrentHashMap<VoiceChannel, Join2CreateChannelData> join2create_channels = new ConcurrentHashMap<>();
	private ConcurrentHashMap<VoiceChannel, VoiceChannel> join2create_channel_links = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Category, Member> custom_categories = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, Role> level_rewards = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, PenaltyData> penalties = new ConcurrentHashMap<>();
	private ConcurrentHashMap<TextChannel, ModMailData> modmails = new ConcurrentHashMap<>();
	private ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, PollData>> polls = new ConcurrentHashMap<>();
	private ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, ReactionRoleData>> reaction_roles = new ConcurrentHashMap<>();

	public GuildData(JSONObject data) {
		this.guild = Bot.INSTANCE.jda.getGuildById(data.getLong(Key.GUILD_ID));
		this.instanciateFromJSON(data);
	}
	
	public GuildData(Guild guild) {
	    this.guild = guild;
	}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        this.admin_roles = DataTools.getRolesFromArrayKeys(guild, data, Key.STATIC_ROLES, Key.ADMIN_ROLES);
        this.custom_channel_policing_roles = DataTools.getRolesFromArrayKeys(guild, data, Key.STATIC_ROLES, Key.CUSTOM_CHANNEL_POLICING_ROLES);
        this.moderation_roles = DataTools.getRolesFromArrayKeys(guild, data, Key.STATIC_ROLES, Key.MODERATION_ROLES);
        this.support_roles = DataTools.getRolesFromArrayKeys(guild, data, Key.STATIC_ROLES, Key.SUPPORT_ROLES);
        this.bot_auto_roles = DataTools.getRolesFromArrayKeys(guild, data, Key.AUTO_ROLES, Key.BOT_AUTO_ROLES);
        this.user_auto_roles = DataTools.getRolesFromArrayKeys(guild, data, Key.AUTO_ROLES, Key.USER_AUTO_ROLES);

        JSONObject auto_messages = data.getJSONObject(Key.AUTO_MESSAGES);
        JSONObject boost_message_data = auto_messages.getJSONObject(Key.BOOST_MESSAGE);
        if (!boost_message_data.isEmpty()) {
            this.boost_message = new AutoMessageData(guild, boost_message_data);
        }
        JSONObject goodbye_message_data = auto_messages.getJSONObject(Key.GOODBYE_MESSAGE);
        if (!goodbye_message_data.isEmpty()) {
            this.goodbye_message = new AutoMessageData(guild, goodbye_message_data);
        }
        JSONObject level_up_message_data = auto_messages.getJSONObject(Key.LEVEL_UP_MESSAGE);
        if (!level_up_message_data.isEmpty()) {
            this.level_up_message = new AutoMessageData(guild, level_up_message_data);
        }
        JSONObject welcome_message_data = auto_messages.getJSONObject(Key.WELCOME_MESSAGE);
        if (!welcome_message_data.isEmpty()) {
            this.welcome_message = new AutoMessageData(guild, welcome_message_data);
        }

        JSONObject static_channels = data.getJSONObject(Key.STATIC_CHANNELS);
        this.community_inbox_channel = guild.getTextChannelById(static_channels.getLong(Key.COMMUNITY_INBOX_CHANNEL));
        this.moderation_inbox_channel = guild.getTextChannelById(static_channels.getLong(Key.MODERATION_INBOX_CHANNEL));
        this.suggestion_inbox_channel = guild.getTextChannelById(static_channels.getLong(Key.SUGGESTION_INBOX_CHANNEL));

        this.support_talk = guild.getVoiceChannelById(static_channels.getLong(Key.SUPPORT_TALK));

        JSONArray offline_message_data = data.getJSONObject(Key.STATIC_MESSAGES).getJSONArray(Key.OFFLINE_MESSAGE);
        this.offline_message = guild.getTextChannelById(offline_message_data.getLong(1)).retrieveMessageById(offline_message_data.getLong(0)).complete();

        JSONObject j2c_channels_data = data.getJSONObject(Key.AUTO_CHANNELS).getJSONObject(Key.JOIN2CREATE_CHANNELS);
        j2c_channels_data.keySet().forEach(channelId -> join2create_channels.put(guild.getVoiceChannelById(channelId), new Join2CreateChannelData(guild, j2c_channels_data.getJSONObject(channelId))));
        join2create_channels.values().removeAll(Collections.singleton(null));
        
        JSONObject j2c_channels_link_data = data.getJSONObject(Key.AUTO_CHANNELS).getJSONObject(Key.JOIN2CREATE_CHANNEL_LINKS);
        j2c_channels_link_data.keySet().forEach(channelId -> join2create_channel_links.put(guild.getVoiceChannelById(channelId), guild.getVoiceChannelById(j2c_channels_data.getLong(channelId))));
        join2create_channel_links.values().removeAll(Collections.singleton(null));

        JSONObject custom_categories_data = data.getJSONObject(Key.AUTO_CHANNELS).getJSONObject(Key.CUSTOM_CATEGORIES);
        custom_categories_data.keySet().forEach(categoryId -> custom_categories.put(guild.getCategoryById(categoryId), guild.getMemberById(custom_categories_data.getLong(categoryId))));
        custom_categories.values().removeAll(Collections.singleton(null));
        custom_categories.keySet().removeAll(Collections.singleton(null));

        JSONObject lvl_reward_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.LEVEL_REWARDS);
        lvl_reward_data.keySet().forEach(level_count -> level_rewards.put(Integer.valueOf(level_count), guild.getRoleById(lvl_reward_data.getLong(level_count))));
        level_rewards.values().removeAll(Collections.singleton(null));

        JSONObject penalties_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.PENALTIES);
        penalties_data.keySet().forEach(warning_count -> penalties.put(Integer.valueOf(warning_count), new PenaltyData(penalties_data.getJSONObject(warning_count))));

        JSONObject modmails_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.MODMAILS);
        modmails_data.keySet().forEach(channelId -> modmails.put(guild.getTextChannelById(channelId), new ModMailData(modmails_data.getJSONObject(channelId))));

        JSONObject polls_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.POLLS);
        polls_data.keySet().forEach(channelId -> {
            TextChannel channel = guild.getTextChannelById(channelId);
            ConcurrentHashMap<Message, PollData> pollSubMap = new ConcurrentHashMap<>();
            JSONObject polls_sub_data = polls_data.getJSONObject(channelId);
            polls_sub_data.keySet().forEach(messageId -> {
                pollSubMap.put(channel.retrieveMessageById(messageId).complete(), new PollData(polls_sub_data.getJSONObject(messageId)));
            });
            polls.put(channel, pollSubMap);
        });

        JSONObject reaction_roles_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.REACTION_ROLES);
        reaction_roles_data.keySet().forEach(channelId -> {
            TextChannel channel = guild.getTextChannelById(channelId);
            ConcurrentHashMap<Message, ReactionRoleData> reactionRoleSubMap = new ConcurrentHashMap<>();
            JSONObject reaction_role_sub_data = reaction_roles_data.getJSONObject(channelId);
            reaction_role_sub_data.keySet().forEach(messageId -> {
                reactionRoleSubMap.put(channel.retrieveMessageById(messageId).complete(), new ReactionRoleData(reaction_role_sub_data.getJSONObject(messageId)));
            });
            reaction_roles.put(channel, reactionRoleSubMap);
        });
        return this;
    }
	
	@Override
	public JSONObject compileToJSON() {
	    JSONObject compiledData = new JSONObject();
	    
	    JSONObject static_roles = new JSONObject();
	    JSONObject auto_roles = new JSONObject();
	    JSONObject static_messages = new JSONObject();
	    JSONObject auto_messages = new JSONObject();
	    JSONObject static_channels = new JSONObject();
	    JSONObject auto_channels = new JSONObject();
	    JSONObject other = new JSONObject();
	    
	    static_roles.put(Key.ADMIN_ROLES, 
	            new JSONArray(admin_roles.stream().map(role -> {return role.getIdLong();}).toList()));
	    static_roles.put(Key.CUSTOM_CHANNEL_POLICING_ROLES, 
                new JSONArray(custom_channel_policing_roles.stream().map(role -> {return role.getIdLong();}).toList()));
	    static_roles.put(Key.MODERATION_ROLES, 
                new JSONArray(moderation_roles.stream().map(role -> {return role.getIdLong();}).toList()));
	    static_roles.put(Key.SUPPORT_ROLES, 
                new JSONArray(support_roles.stream().map(role -> {return role.getIdLong();}).toList()));
	    
	    auto_roles.put(Key.BOT_AUTO_ROLES, 
                new JSONArray(bot_auto_roles.stream().map(role -> {return role.getIdLong();}).toList()));
	    auto_roles.put(Key.USER_AUTO_ROLES, 
                new JSONArray(user_auto_roles.stream().map(role -> {return role.getIdLong();}).toList()));
	    
	    if (offline_message != null) {
	        static_messages.put(Key.OFFLINE_MESSAGE,
	                new JSONArray(List.of(offline_message.getIdLong(), offline_message.getChannel().getIdLong())));
	    } else {
	        static_messages.put(Key.OFFLINE_MESSAGE, new JSONArray());
	    }
	    
	    if (boost_message != null) {
	        auto_messages.put(Key.BOOST_MESSAGE,
	                boost_message.compileToJSON());
	    } else {
	        auto_messages.put(Key.BOOST_MESSAGE, new JSONObject());
	    }
	    if (goodbye_message != null) {
	        auto_messages.put(Key.GOODBYE_MESSAGE,
	                goodbye_message.compileToJSON());
        } else {
            auto_messages.put(Key.GOODBYE_MESSAGE, new JSONObject());
        }
	    if (level_up_message != null) {
	        auto_messages.put(Key.LEVEL_UP_MESSAGE,
	                level_up_message.compileToJSON());
        } else {
            auto_messages.put(Key.LEVEL_UP_MESSAGE, new JSONObject());
        }
	    if (welcome_message != null) {
	        auto_messages.put(Key.WELCOME_MESSAGE,
	                welcome_message.compileToJSON());
        } else {
            auto_messages.put(Key.WELCOME_MESSAGE, new JSONObject());
        }
	    
	    if (community_inbox_channel != null) {
	        static_channels.put(Key.COMMUNITY_INBOX_CHANNEL,
	                community_inbox_channel.getIdLong());
	    } else {
	        static_channels.put(Key.COMMUNITY_INBOX_CHANNEL, 0L);
	    }
	    if (moderation_inbox_channel != null) {
	        static_channels.put(Key.MODERATION_INBOX_CHANNEL,
	                moderation_inbox_channel.getIdLong());
        } else {
            static_channels.put(Key.MODERATION_INBOX_CHANNEL, 0L);
        }
	    if (suggestion_inbox_channel != null) {
	        static_channels.put(Key.SUGGESTION_INBOX_CHANNEL,
	                suggestion_inbox_channel.getIdLong());
        } else {
            static_channels.put(Key.SUGGESTION_INBOX_CHANNEL, 0L);
        }
	    if (support_talk != null) {
	        static_channels.put(Key.SUPPORT_TALK,
	                support_talk.getIdLong());
        } else {
            static_channels.put(Key.SUPPORT_TALK, 0L);
        }
	    
	    JSONObject join2create_channels_object = new JSONObject();
	    join2create_channels.forEach((channel, data) -> join2create_channels_object.put(channel.getId(), data.compileToJSON()));
	    auto_channels.put(Key.JOIN2CREATE_CHANNELS, join2create_channels_object);
	    
	    JSONObject join2create_channel_links_object = new JSONObject();
	    join2create_channel_links.forEach((channel, parent) -> join2create_channels_object.put(channel.getId(), parent.getIdLong()));
	    auto_channels.put(Key.JOIN2CREATE_CHANNEL_LINKS, join2create_channel_links_object);
	    
	    JSONObject custom_channel_categories_object = new JSONObject();
	    custom_categories.forEach((category, owner) -> custom_channel_categories_object.put(category.getId(), owner.getIdLong()));
	    auto_channels.put(Key.CUSTOM_CATEGORIES, custom_channel_categories_object);
	    
	    JSONObject level_rewards_object = new JSONObject();
	    level_rewards.forEach((level_count, reward_role) -> level_rewards_object.put(String.valueOf(level_count), reward_role.getIdLong()));
	    other.put(Key.LEVEL_REWARDS, level_rewards_object);
	    
	    JSONObject penalties_object = new JSONObject();
	    penalties.forEach((warning_count, data) -> penalties_object.put(String.valueOf(warning_count), data.compileToJSON()));
	    other.put(Key.PENALTIES, penalties_object);
	    
	    JSONObject modmails_object = new JSONObject();
	    modmails.forEach((channel, data) -> modmails_object.put(channel.getId(), data.compileToJSON()));
	    other.put(Key.MODMAILS, modmails_object);
	    
	    JSONObject polls_object = new JSONObject();
	    polls.forEach((channel, message_map) -> {
	        JSONObject message_map_object = new JSONObject();
	        message_map.forEach((message, data) -> message_map_object.put(message.getId(), data.compileToJSON()));
	        if (!message_map_object.isEmpty()) {
	            polls_object.put(channel.getId(), message_map_object);
	        }
	    });
	    
	    JSONObject reaction_roles_object = new JSONObject();
        reaction_roles.forEach((channel, message_map) -> {
            JSONObject message_map_object = new JSONObject();
            message_map.forEach((message, data) -> message_map_object.put(message.getId(), data.compileToJSON()));
            if (!message_map_object.isEmpty()) {
                reaction_roles_object.put(channel.getId(), message_map_object);
            }
        });

        compiledData.put(Key.GUILD_ID, guild.getIdLong());
        compiledData.put(Key.GUILD_NAME, guild.getName());
        compiledData.put(Key.STATIC_ROLES, static_roles);
        compiledData.put(Key.AUTO_ROLES, auto_roles);
        compiledData.put(Key.STATIC_MESSAGES, static_messages);
        compiledData.put(Key.AUTO_MESSAGES, auto_messages);
        compiledData.put(Key.STATIC_CHANNELS, static_channels);
        compiledData.put(Key.AUTO_CHANNELS, auto_channels);
        compiledData.put(Key.OTHER, other);
	    
	    return compiledData;
	}
    
    public Guild getGuild() {
        return this.guild;
    }
//  Static Roles
	public List<Role> getAdminRoles() {
		return this.admin_roles;
	}

	public GuildData setAdminRoles(List<Role> roles) {
	    DataTools.setList(this.admin_roles, roles);
		return this;
	}
    
    public GuildData addAdminRoles(Role... roles) {
        this.admin_roles.addAll(List.of(roles));
        return this;
    }
    
    public GuildData removeAdminRoles(int... indices) {
        DataTools.removeFromList(this.admin_roles, indices);
        return this;
    }
    
    public GuildData removeAdminRolesByRole(Role... roles) {
        this.admin_roles.removeAll(List.of(roles));
        return this;
    }
    
    public List<Role> getCustomChannelPolicingRoles() {
        return this.custom_channel_policing_roles;
    }

    public GuildData setCustomChannelPolicingRoles(List<Role> roles) {
        DataTools.setList(this.custom_channel_policing_roles, roles);
        return this;
    }
    
    public GuildData addCustomChannelPolicingRoles(Role... roles) {
        this.custom_channel_policing_roles.addAll(List.of(roles));
        return this;
    }
    
    public GuildData removeCustomChannelPolicingRoles(int... indices) {
        DataTools.removeFromList(this.custom_channel_policing_roles, indices);
        return this;
    }
    
    public GuildData removeCustomChannelPolicingRolesByRole(Role... roles) {
        this.custom_channel_policing_roles.removeAll(List.of(roles));
        return this;
    }

	public List<Role> getModerationRoles() {
		return this.moderation_roles;
	}

	public GuildData setModerationRoles(List<Role> roles) {
	    DataTools.setList(this.moderation_roles, roles);
        return this;
	}
    
    public GuildData addModerationRoles(Role... roles) {
        this.moderation_roles.addAll(List.of(roles));
        return this;
    }
    
    public GuildData removeModerationRoles(int... indices) {
        DataTools.removeFromList(this.moderation_roles, indices);
        return this;
    }
    
    public GuildData removeModerationRolesByRole(Role... roles) {
        this.moderation_roles.removeAll(List.of(roles));
        return this;
    }

	public List<Role> getSupportRoles() {
		return this.support_roles;
	}

	public GuildData setSupportRoles(List<Role> roles) {
	    DataTools.setList(this.support_roles, roles);
        return this;
	}
	
    public GuildData addSupportRoles(Role... roles) {
        this.support_roles.addAll(List.of(roles));
        return this;
    }
    
    public GuildData removeSupportRoles(int... indices) {
        DataTools.removeFromList(this.support_roles, indices);
        return this;
    }
    
    public GuildData removeSupportRolesByRole(Role... roles) {
        this.support_roles.removeAll(List.of(roles));
        return this;
    }
    
//  Auto Roles
	public List<Role> getBotAutoRoles() {
		return this.bot_auto_roles;
	}

	public GuildData setBotAutoRoles(List<Role> roles) {
	    DataTools.setList(this.bot_auto_roles, roles);
        return this;
	}
	
	public GuildData addBotAutoRoles(Role... roles) {
	    this.bot_auto_roles.addAll(List.of(roles));
        return this;
    }
    
    public GuildData removeBotAutoRoles(Role... roles) {
        this.bot_auto_roles.removeAll(List.of(roles));
        return this;
    }
    
    public GuildData removeBotAutoRoles(int... indices) {
        DataTools.removeFromList(this.bot_auto_roles, indices);
        return this;
    }

	public List<Role> getUserAutoRoles() {
		return this.user_auto_roles;
	}

	public GuildData setUserAutoRoles(List<Role> roles) {
	    DataTools.setList(this.user_auto_roles, roles);
        return this;
	}
	public GuildData addUserAutoRoles(Role... roles) {
        this.user_auto_roles.addAll(List.of(roles));
        return this;
    }
    
    public GuildData removeUserAutoRoles(Role... roles) {
        this.user_auto_roles.removeAll(List.of(roles));
        return this;
    }
    
    public GuildData removeUserAutoRoles(int... indices) {
        DataTools.removeFromList(this.user_auto_roles, indices);
        return this;
    }
//  Static Messages
    public Message getOfflineMessage() {
        return this.offline_message;
    }

    public GuildData setOfflineMessage(Message offline_message) {
        this.offline_message = offline_message;
        return this;
    }
//  Auto Messages
	public AutoMessageData getBoostMessage() {
		return this.boost_message;
	}

	public GuildData setBoostMessage(AutoMessageData boost_message) {
		this.boost_message = boost_message;
        return this;
	}

	public AutoMessageData getGoodbyeMessage() {
		return this.goodbye_message;
	}

	public GuildData setGoodbyeMessage(AutoMessageData goodbye_message) {
		this.goodbye_message = goodbye_message;
        return this;
	}

	public AutoMessageData getLevelUpMessage() {
		return this.level_up_message;
	}

	public GuildData setLevelUpMssage(AutoMessageData level_up_message) {
		this.level_up_message = level_up_message;
        return this;
	}

	public AutoMessageData getWelcomeMessage() {
		return this.welcome_message;
	}

	public GuildData setWelcomeMessage(AutoMessageData welcome_message) {
		this.welcome_message = welcome_message;
        return this;
	}
//  Static Channels
	public TextChannel getCommunityInboxChannel() {
		return this.community_inbox_channel;
	}

	public GuildData setCommunityInboxChannel(TextChannel community_inbox_channel) {
		this.community_inbox_channel = community_inbox_channel;
        return this;
	}

	public TextChannel getModerationInboxChannel() {
		return this.moderation_inbox_channel;
	}

	public GuildData setModerationInboxChannel(TextChannel moderation_inbox_channel) {
		this.moderation_inbox_channel = moderation_inbox_channel;
        return this;
	}

	public TextChannel getSuggestionInboxChannel() {
		return this.suggestion_inbox_channel;
	}

	public GuildData setSuggestionInboxChannel(TextChannel suggestion_inbox_channel) {
		this.suggestion_inbox_channel = suggestion_inbox_channel;
        return this;
	}

	public VoiceChannel getSupportTalk() {
		return this.support_talk;
	}

	public GuildData setSupportTalk(VoiceChannel support_talk) {
		this.support_talk = support_talk;
        return this;
	}
//	Auto Channels
	public ConcurrentHashMap<VoiceChannel, Join2CreateChannelData> getJoin2CreateChannelDatas() {
		return this.join2create_channels;
	}
	
	public Join2CreateChannelData getJoin2CreateChannelData(VoiceChannel channel) {
	    return this.join2create_channels.get(channel);
	}
	
	public GuildData setJoin2CreateChannels(ConcurrentHashMap<VoiceChannel, Join2CreateChannelData> join2create_channels) {
	    DataTools.setMap(this.join2create_channels, join2create_channels);
        return this;
	}
	
	public GuildData addJoin2CreateChannels(Join2CreateChannelData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.join2create_channels.put(datas[i].getVoiceChannel(), datas[i]);
	    }
        return this;
	}
	public GuildData removeJoin2CreateChannels(VoiceChannel... channels) {
	    for (int i = 0; i < channels.length; i++) {
	        this.join2create_channels.remove(channels[i]);
	    }
        return this;
	}
	
	public GuildData removeJoin2CreateChannelsByData(Join2CreateChannelData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.join2create_channels.remove(datas[i].getVoiceChannel());
	    }
        return this;
	}
	
	public ConcurrentHashMap<VoiceChannel, VoiceChannel> getJoin2CreateChannelLinks() {
		return this.join2create_channel_links;
	}
	
	public VoiceChannel getParentJoin2CreateChannel(VoiceChannel channel) {
	    return this.join2create_channel_links.get(channel);
	}
	
	public Join2CreateChannelData getParentJoin2CreateChannelData(VoiceChannel channel) {
	    return this.getJoin2CreateChannelData(this.getParentJoin2CreateChannel(channel));
	}
	
	public GuildData setJoin2CreateChannelLinks(ConcurrentHashMap<VoiceChannel, VoiceChannel> join2create_channel_links) {
	    DataTools.setMap(this.join2create_channel_links, join2create_channel_links);
        return this;
	}
	
	public GuildData addParentJoin2CreateChannel(VoiceChannel channel, VoiceChannel parent) {
	    this.join2create_channel_links.put(channel, parent);
        return this;
	}
	
	public GuildData addParentJoin2CreateChannels(ConcurrentHashMap<VoiceChannel, VoiceChannel> join2create_channel_links) {
	    this.join2create_channel_links.putAll(join2create_channel_links);
        return this;
	}
	
	public GuildData removeJoin2CreateChannelLinks(VoiceChannel... channels) {
	    for (int i = 0; i < channels.length; i++) {
            this.join2create_channel_links.remove(channels[i]);
        }
        return this;
	}
    
    public GuildData removeJoin2CreateChannelLinksByParents(VoiceChannel... parents) {
        DataTools.removeFromMap(this.join2create_channel_links, parents);
        return this;
    }

	public ConcurrentHashMap<Category, Member> getCustomCategories() {
		return this.custom_categories;
	}
	
	public Member getCustomCategoryOwner(Category category) {
	    return this.custom_categories.get(category);
	}

	public GuildData setCustomCategories(ConcurrentHashMap<Category, Member> custom_channel_categories) {
	    DataTools.setMap(this.custom_categories, custom_channel_categories);
        return this;
	}
	
	public GuildData addCustomCategoryOwner(Category category, Member owner) {
	    this.custom_categories.put(category, owner);
        return this;
	}
	
	public GuildData addCustomCategoryOwners(ConcurrentHashMap<Category, Member> custom_channel_categories) {
	    this.custom_categories.putAll(custom_channel_categories);
        return this;
	}
	
	public GuildData removeCustomCategories(Category... categories) {
	    for (int i = 0; i < categories.length; i++) {
	        this.custom_categories.remove(categories[i]);
	    }
        return this;
	}
	
	public GuildData removeCustomCategoriesByOwner(Member... owners) {
	    DataTools.removeFromMap(this.custom_categories, owners);
        return this;
	}
//  Other
	public ConcurrentHashMap<Integer, Role> getLevelRewards() {
		return this.level_rewards;
	}
	
	public Role getLevelReward(int level_count) {
	    return this.level_rewards.get(level_count);
	}

	public GuildData setLevelRewards(ConcurrentHashMap<Integer, Role> level_rewards) {
	    DataTools.setMap(this.level_rewards, level_rewards);
        return this;
	}
	
	public GuildData addLevelReward(int level_count, Role reward) {
	    this.level_rewards.put(level_count, reward);
        return this;
	}
	
	public GuildData addLevelRewards(ConcurrentHashMap<Integer, Role> level_rewards) {
	    this.level_rewards.putAll(level_rewards);
        return this;
	}
	
	public GuildData removeLevelRewards(int... level_counts) {
	    for (int i = 0; i < level_counts.length; i++) {
	        this.level_rewards.remove(level_counts[i]);
	    }
        return this;
	}
	
	public GuildData removeLevelRewardsByReward(Role... rewards) {
	    DataTools.removeFromMap(this.level_rewards, rewards);
        return this;
	}

	public ConcurrentHashMap<Integer, PenaltyData> getPenalties() {
		return this.penalties;
	}
	
	public PenaltyData getPenalty(int warning_count) {
	    return this.penalties.get(warning_count);
	}

	public GuildData setPenalties(ConcurrentHashMap<Integer, PenaltyData> penalties) {
	    DataTools.setMap(this.penalties, penalties);
        return this;
	}
	
	public GuildData addPenalties(PenaltyData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.penalties.put(datas[i].getWarningCount(), datas[i]);
	    }
        return this;
	}
	
	public GuildData removePenalties(int... warning_counts) {
	    for (int i = 0; i < warning_counts.length; i++) {
	        this.penalties.remove(warning_counts[i]);
	    }
        return this;
	}
	
	public GuildData removePenaltiesByData(PenaltyData... datas) {
	    for (int i = 0; i < datas.length; i++) {
            this.penalties.remove(datas[i].getWarningCount());
        }
        return this;
	}

	public ConcurrentHashMap<TextChannel, ModMailData> getModmails() {
		return this.modmails;
	}
	
	public ModMailData getModMail(TextChannel channel) {
	    return this.modmails.get(channel);
	}

	public GuildData setModmails(ConcurrentHashMap<TextChannel, ModMailData> modmails) {
	    DataTools.setMap(this.modmails, modmails);
        return this;
	}
	
	public GuildData addModmails(ModMailData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.modmails.put(datas[i].getGuildChannel(), datas[i]);
	    }
        return this;
	}
	
	public GuildData removeModmails(TextChannel... channels) {
	    for (int i = 0; i < channels.length; i++) {
	        this.modmails.remove(channels[i]);
	    }
        return this;
	}
	
	public GuildData removeModmailsByData(ModMailData... datas) {
	    for (int i = 0; i < datas.length; i++) {
            this.modmails.remove(datas[i].getGuildChannel());
        }
        return this;
	}

	public ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, PollData>> getPolls() {
		return this.polls;
	}
	
	public ConcurrentHashMap<Message, PollData> getPollsByChannel(TextChannel channel) {
	    return this.polls.get(channel);
	}
	
	public PollData getPoll(TextChannel channel, Message message) {
	    return this.getPollsByChannel(channel).get(message);
	}

	public GuildData setPolls(ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, PollData>> polls) {
	    DataTools.setMap(this.polls, polls);
        return this;
	}
	
	public GuildData setPollsByChannel(TextChannel channel,  ConcurrentHashMap<Message, PollData> polls) {
	    DataTools.setMap(this.getPollsByChannel(channel), polls);
        return this;
	}
	
	public GuildData addPolls(PollData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.getPollsByChannel(datas[i].getChannel()).put(datas[i].getMessage(), datas[i]);
	    }
        return this;
	}
	
	public GuildData removePoll(TextChannel channel, Message message) {
	    this.getPollsByChannel(channel).remove(message);
	    return this;
	}
	
	public GuildData removePollsByData(PollData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.getPollsByChannel(datas[i].getChannel()).remove(datas[i].getMessage());
	    }
	    return this;
	}

	public ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, ReactionRoleData>> getReactionRoles() {
		return this.reaction_roles;
	}
	
	public ConcurrentHashMap<Message, ReactionRoleData> getReactionRolesByChannel(TextChannel channel) {
	    return this.reaction_roles.get(channel);
	}
	
	public ReactionRoleData getReactionRole(TextChannel channel, Message message) {
	    return this.getReactionRolesByChannel(channel).get(message);
	}

	public GuildData setReactionRoles(ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, ReactionRoleData>> reaction_roles) {
		DataTools.setMap(this.reaction_roles, reaction_roles);
        return this;
	}
	
	public GuildData setReactionRolesByChannel(TextChannel channel, ConcurrentHashMap<Message, ReactionRoleData> reaction_roles) {
	    DataTools.setMap(this.getReactionRolesByChannel(channel), reaction_roles);
	    return this;
	}
	
	public GuildData addReactionRoles(ReactionRoleData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.getReactionRolesByChannel(datas[i].getChannel()).put(datas[i].getMessage(), datas[i]);
	    }
	    return this;
	}
	
	public GuildData removeReactionRole(TextChannel channel, Message message) {
	    this.getReactionRolesByChannel(channel).remove(message);
	    return this;
	}
	
	public GuildData removeReactionRoleByData(ReactionRoleData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.getReactionRolesByChannel(datas[i].getChannel()).remove(datas[i].getMessage());
	    }
	    return this;
	}
    
    private static abstract class Key {
        public static final String GUILD_ID = "id";
        public static final String GUILD_NAME = "name";
        public static final String STATIC_ROLES = "static_roles";
            public static final String ADMIN_ROLES = "admin_roles";
            public static final String CUSTOM_CHANNEL_POLICING_ROLES = "custom_channel_policing_roles";
            public static final String MODERATION_ROLES = "moderation_roles";
            public static final String SUPPORT_ROLES = "support_roles";
        public static final String AUTO_ROLES = "auto_roles";
            public static final String BOT_AUTO_ROLES = "bot_auto_roles";
            public static final String USER_AUTO_ROLES = "user_auto_roles";
        public static final String STATIC_MESSAGES = "static_messages";
            public static final String OFFLINE_MESSAGE = "offline_message";
        public static final String AUTO_MESSAGES = "auto_messages";
            public static final String BOOST_MESSAGE = "boost_message";
            public static final String GOODBYE_MESSAGE = "goodbye_message";
            public static final String LEVEL_UP_MESSAGE = "level_up_message";
            public static final String WELCOME_MESSAGE = "welcome_message";
        public static final String STATIC_CHANNELS = "static_channels";
            public static final String COMMUNITY_INBOX_CHANNEL = "community_inbox_channel";
            public static final String MODERATION_INBOX_CHANNEL = "moderation_inbox_channel";
            public static final String SUGGESTION_INBOX_CHANNEL = "suggestion_inbox_channel";
            public static final String SUPPORT_TALK = "support_talk";
        public static final String AUTO_CHANNELS = "auto_channels";
            public static final String JOIN2CREATE_CHANNELS = "join2create_channels";
            public static final String JOIN2CREATE_CHANNEL_LINKS = "join2create_channel_links";
            public static final String CUSTOM_CATEGORIES = "custom_categories";
        public static final String OTHER = "other";
            public static final String LEVEL_REWARDS = "level_rewards";
            public static final String PENALTIES = "penalties";
            public static final String MODMAILS = "modmails";
            public static final String POLLS = "polls";
            public static final String REACTION_ROLES = "reaction_roles";
    }
}