package assets.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.base.exceptions.EntityNotFoundException;
import assets.base.exceptions.EntityNotFoundException.ReferenceType;
import assets.data.single.AutoMessageData;
import assets.data.single.GiveawayData;
import assets.data.single.Join2CreateChannelData;
import assets.data.single.LevelRewardData;
import assets.data.single.ModMailData;
import assets.data.single.PenaltyData;
import assets.data.single.PollData;
import assets.data.single.ReactionRoleData;
import base.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class GuildData {

    private final long guild_id;
    private List<Long> admin_roles = new ArrayList<>();
    private List<Long> moderation_roles = new ArrayList<>();
    private List<Long> support_roles = new ArrayList<>();
    private List<Long> bot_auto_roles = new ArrayList<>();
    private List<Long> user_auto_roles = new ArrayList<>();
    private AutoMessageData boost_message, goodbye_message, level_up_message, welcome_message;
    private long community_inbox_channel, moderation_inbox_channel, suggestion_inbox_channel, support_talk;
    private Message offline_message;
    private ConcurrentHashMap<Long, Join2CreateChannelData> join2create_channels = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, LevelRewardData> level_rewards = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, PenaltyData> penalties = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, ModMailData> modmails = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, ConcurrentHashMap<Long, PollData>> polls = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, ConcurrentHashMap<Long, ReactionRoleData>> reaction_roles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, ConcurrentHashMap<Long, GiveawayData>> giveaways = new ConcurrentHashMap<>();

    public GuildData(Guild guild) throws EntityNotFoundException {
        if (guild == null) {
            throw new EntityNotFoundException(ReferenceType.GUILD, "Couldn't find guild, aborting config creation");
        } else {
            this.guild_id = guild.getIdLong();
        }
    }

    public GuildData(JSONObject data) {
        this.guild_id = data.getLong(Key.GUILD_ID);
        Guild guild = this.getGuild();
        JSONArray offline_message_data = data.getJSONObject(Key.STATIC_MESSAGES).getJSONArray(Key.OFFLINE_MESSAGE);
        if (!offline_message_data.isEmpty()) {
            TextChannel channel = guild.getTextChannelById(offline_message_data.getLong(1));
            if (channel != null) {
                this.offline_message = channel.retrieveMessageById(offline_message_data.getLong(0)).complete();
            }
        }

//	      0-Layer Nesting
        this.admin_roles = DataTools.getIdsFromArrayKeys(data, Key.STATIC_ROLES, Key.ADMIN_ROLES);
        this.moderation_roles = DataTools.getIdsFromArrayKeys(data, Key.STATIC_ROLES, Key.MODERATION_ROLES);
        this.support_roles = DataTools.getIdsFromArrayKeys(data, Key.STATIC_ROLES, Key.SUPPORT_ROLES);
        this.bot_auto_roles = DataTools.getIdsFromArrayKeys(data, Key.AUTO_ROLES, Key.BOT_AUTO_ROLES);
        this.user_auto_roles = DataTools.getIdsFromArrayKeys(data, Key.AUTO_ROLES, Key.USER_AUTO_ROLES);

        JSONObject static_channels = data.getJSONObject(Key.STATIC_CHANNELS);
        this.community_inbox_channel = static_channels.getLong(Key.COMMUNITY_INBOX_CHANNEL);
        this.moderation_inbox_channel = static_channels.getLong(Key.MODERATION_INBOX_CHANNEL);
        this.suggestion_inbox_channel = static_channels.getLong(Key.SUGGESTION_INBOX_CHANNEL);
        this.support_talk = static_channels.getLong(Key.SUPPORT_TALK);

//	      1-Layer Nesting
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

//	      2-Layer Nesting
        JSONObject j2c_channels_data = data.getJSONObject(Key.AUTO_CHANNELS).getJSONObject(Key.JOIN2CREATE_CHANNELS);
        j2c_channels_data.keySet().forEach(channelId -> {
            VoiceChannel channel = guild.getVoiceChannelById(channelId);
            if (channel != null) {
                join2create_channels.put(Long.valueOf(channelId),
                        new Join2CreateChannelData(channel, j2c_channels_data.getJSONObject(channelId)));
            }
        });

        JSONObject other = data.getJSONObject(Key.OTHER);
        JSONObject lvl_reward_data = other.getJSONObject(Key.LEVEL_REWARDS);
        lvl_reward_data.keySet().forEach(level_count -> {
            JSONObject reward_data = lvl_reward_data.getJSONObject(level_count);
            if (!reward_data.isEmpty()) {
                level_rewards.put(Integer.valueOf(level_count), new LevelRewardData(guild, reward_data));
            }
        });

        JSONObject penalties_data = other.getJSONObject(Key.PENALTIES);
        penalties_data.keySet().forEach(warning_count -> {
            JSONObject penalty_data = penalties_data.getJSONObject(warning_count);
            if (!penalty_data.isEmpty())
                penalties.put(Integer.valueOf(warning_count), new PenaltyData(guild, penalty_data));
        });

        JSONObject modmails_data = other.getJSONObject(Key.MODMAILS);
        modmails_data.keySet().forEach(channelId -> {
            TextChannel channel = guild.getTextChannelById(channelId);
            JSONObject modmail_data = modmails_data.getJSONObject(channelId);
            if (channel != null && !modmail_data.isEmpty()) {
                modmails.put(channel.getIdLong(), new ModMailData(guild, channel, modmail_data));
            }
        });

//	      3-Layer Nesting
        JSONObject polls_data = other.getJSONObject(Key.POLLS);
        polls_data.keySet().forEach(channelId -> {
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel != null) {
                ConcurrentHashMap<Long, PollData> pollSubMap = new ConcurrentHashMap<>();
                JSONObject polls_sub_data = polls_data.getJSONObject(channelId);
                polls_sub_data.keySet().forEach(messageId -> {
                    JSONObject poll_data = polls_sub_data.getJSONObject(messageId);
                    Message message = channel.retrieveMessageById(messageId).complete();
                    if (message != null && !poll_data.isEmpty()) {
                        pollSubMap.put(Long.valueOf(messageId), new PollData(channel, message, poll_data));
                    }
                });
                if (!pollSubMap.isEmpty()) {
                    polls.put(channel.getIdLong(), pollSubMap);
                }
            }
        });

        JSONObject reaction_roles_data = other.getJSONObject(Key.REACTION_ROLES);
        reaction_roles_data.keySet().forEach(channelId -> {
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel != null) {
                ConcurrentHashMap<Long, ReactionRoleData> reactionRoleSubMap = new ConcurrentHashMap<>();
                JSONObject reaction_role_sub_data = reaction_roles_data.getJSONObject(channelId);
                reaction_role_sub_data.keySet().forEach(messageId -> {
                    JSONObject reaction_role_data = reaction_role_sub_data.getJSONObject(messageId);
                    Message message = channel.retrieveMessageById(messageId).complete();
                    if (message != null && !reaction_role_data.isEmpty()) {
                        reactionRoleSubMap.put(Long.valueOf(messageId),
                                new ReactionRoleData(channel, message, reaction_role_data));
                    }
                });
                if (!reactionRoleSubMap.isEmpty()) {
                    reaction_roles.put(channel.getIdLong(), reactionRoleSubMap);
                }
            }
        });

        JSONObject giveaways_data = other.getJSONObject(Key.GIVEAWAYS);
        giveaways_data.keySet().forEach(channelId -> {
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel != null) {
                ConcurrentHashMap<Long, GiveawayData> giveawaySubMap = new ConcurrentHashMap<>();
                JSONObject giveaway_sub_data = giveaways_data.getJSONObject(channelId);
                giveaway_sub_data.keySet().forEach(messageId -> {
                    JSONObject giveaway_data = giveaway_sub_data.getJSONObject(messageId);
                    Message message = channel.retrieveMessageById(messageId).complete();
                    if (message != null && !giveaway_data.isEmpty()) {
                        giveawaySubMap.put(Long.valueOf(messageId), new GiveawayData(channel, message, giveaway_data));
                    }
                });
                if (!giveawaySubMap.isEmpty()) {
                    giveaways.put(channel.getIdLong(), giveawaySubMap);
                }
            }
        });
    }

    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();

        JSONObject static_roles = new JSONObject();
        JSONObject auto_roles = new JSONObject();
        JSONObject static_messages = new JSONObject();
        JSONObject auto_messages = new JSONObject();
        JSONObject static_channels = new JSONObject();
        JSONObject auto_channels = new JSONObject();
        JSONObject other = new JSONObject();

        static_roles.put(Key.ADMIN_ROLES, new JSONArray(admin_roles));
        static_roles.put(Key.MODERATION_ROLES, new JSONArray(moderation_roles));
        static_roles.put(Key.SUPPORT_ROLES, new JSONArray());

        auto_roles.put(Key.BOT_AUTO_ROLES, new JSONArray(bot_auto_roles));
        auto_roles.put(Key.USER_AUTO_ROLES, new JSONArray(user_auto_roles));

        if (offline_message != null) {
            static_messages.put(Key.OFFLINE_MESSAGE,
                    new JSONArray(List.of(offline_message.getIdLong(), offline_message.getChannel().getIdLong())));
        } else {
            static_messages.put(Key.OFFLINE_MESSAGE, new JSONArray());
        }

        if (boost_message != null) {
            auto_messages.put(Key.BOOST_MESSAGE, boost_message.compileToJSON());
        } else {
            auto_messages.put(Key.BOOST_MESSAGE, new JSONObject());
        }
        if (goodbye_message != null) {
            auto_messages.put(Key.GOODBYE_MESSAGE, goodbye_message.compileToJSON());
        } else {
            auto_messages.put(Key.GOODBYE_MESSAGE, new JSONObject());
        }
        if (level_up_message != null) {
            auto_messages.put(Key.LEVEL_UP_MESSAGE, level_up_message.compileToJSON());
        } else {
            auto_messages.put(Key.LEVEL_UP_MESSAGE, new JSONObject());
        }
        if (welcome_message != null) {
            auto_messages.put(Key.WELCOME_MESSAGE, welcome_message.compileToJSON());
        } else {
            auto_messages.put(Key.WELCOME_MESSAGE, new JSONObject());
        }

        static_channels.put(Key.COMMUNITY_INBOX_CHANNEL, community_inbox_channel);
        static_channels.put(Key.MODERATION_INBOX_CHANNEL, moderation_inbox_channel);
        static_channels.put(Key.SUGGESTION_INBOX_CHANNEL, suggestion_inbox_channel);
        static_channels.put(Key.SUPPORT_TALK, support_talk);

        JSONObject level_rewards_object = new JSONObject();
        level_rewards.forEach((level_count, reward_role) -> level_rewards_object.put(String.valueOf(level_count),
                String.valueOf(reward_role)));
        other.put(Key.LEVEL_REWARDS, level_rewards_object);

        JSONObject join2create_channels_object = new JSONObject();
        join2create_channels.forEach(
                (channel, data) -> join2create_channels_object.put(String.valueOf(channel), data.compileToJSON()));
        auto_channels.put(Key.JOIN2CREATE_CHANNELS, join2create_channels_object);

        JSONObject penalties_object = new JSONObject();
        penalties.forEach(
                (warning_count, data) -> penalties_object.put(String.valueOf(warning_count), data.compileToJSON()));
        other.put(Key.PENALTIES, penalties_object);

        JSONObject modmails_object = new JSONObject();
        modmails.forEach((channel, data) -> modmails_object.put(String.valueOf(channel), data.compileToJSON()));
        other.put(Key.MODMAILS, modmails_object);

        JSONObject polls_object = new JSONObject();
        polls.forEach((channel, message_map) -> {
            JSONObject message_map_object = new JSONObject();
            message_map
                    .forEach((message, data) -> message_map_object.put(String.valueOf(message), data.compileToJSON()));
            if (!message_map_object.isEmpty()) {
                polls_object.put(String.valueOf(channel), message_map_object);
            }
        });
        other.put(Key.POLLS, polls_object);

        JSONObject reaction_roles_object = new JSONObject();
        reaction_roles.forEach((channel, message_map) -> {
            JSONObject message_map_object = new JSONObject();
            message_map
                    .forEach((message, data) -> message_map_object.put(String.valueOf(message), data.compileToJSON()));
            if (!message_map_object.isEmpty()) {
                reaction_roles_object.put(String.valueOf(channel), message_map_object);
            }
        });
        other.put(Key.REACTION_ROLES, reaction_roles_object);

        JSONObject giveaways_object = new JSONObject();
        giveaways.forEach((channel, message_map) -> {
            JSONObject message_map_object = new JSONObject();
            message_map
                    .forEach((message, data) -> message_map_object.put(String.valueOf(message), data.compileToJSON()));
            if (!message_map_object.isEmpty()) {
                giveaways_object.put(String.valueOf(channel), message_map_object);
            }
        });
        other.put(Key.GIVEAWAYS, giveaways_object);

        compiledData.put(Key.GUILD_ID, guild_id);
        compiledData.put(Key.GUILD_NAME, Bot.getAPI().getGuildById(guild_id).getName());
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
        return Bot.getAPI().getGuildById(guild_id);
    }

//  Static Roles
    public List<Long> getAdminRoleIds() {
        return this.getAdminRoles().stream().map(Role::getIdLong).toList();
    }

    public List<Role> getAdminRoles() {
        return DataTools.getRolesFromIds(this.getGuild(), this.admin_roles);
    }

    public void setAdminRoles(List<Role> roles) {
        DataTools.setList(this.admin_roles, List.of(DataTools.convertRoleListToIds(roles)));
    }

    public void addAdminRoles(Role... roles) {
        DataTools.addToList(this.admin_roles, DataTools.convertRoleArrayToIds(roles));
    }

    public void removeAdminRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.admin_roles, indices);
    }

    public void removeAdminRolesByRole(Role... roles) {
        DataTools.removeValuesFromList(this.admin_roles, DataTools.convertRoleArrayToIds(roles));
    }

    public List<Long> getModerationRoleIds() {
        return this.getModerationRoles().stream().map(Role::getIdLong).toList();
    }

    public List<Role> getModerationRoles() {
        return DataTools.getRolesFromIds(this.getGuild(), this.moderation_roles);
    }

    public void setModerationRoles(List<Role> roles) {
        DataTools.setList(this.moderation_roles, List.of(DataTools.convertRoleListToIds(roles)));
    }

    public void addModerationRoles(Role... roles) {
        DataTools.addToList(this.moderation_roles, DataTools.convertRoleArrayToIds(roles));
    }

    public void removeModerationRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.moderation_roles, indices);
    }

    public void removeModerationRolesByRole(Role... roles) {
        DataTools.addToList(this.moderation_roles, DataTools.convertRoleArrayToIds(roles));
    }

    public List<Long> getSupportRoleIds() {
        return this.getSupportRoles().stream().map(Role::getIdLong).toList();
    }

    public List<Role> getSupportRoles() {
        return DataTools.getRolesFromIds(this.getGuild(), this.support_roles);
    }

    public void setSupportRoles(List<Role> roles) {
        DataTools.setList(this.support_roles, List.of(DataTools.convertRoleListToIds(roles)));
    }

    public void addSupportRoles(Role... roles) {
        DataTools.addToList(this.support_roles, DataTools.convertRoleArrayToIds(roles));
    }

    public void removeSupportRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.support_roles, indices);
    }

    public void removeSupportRolesByRole(Role... roles) {
        DataTools.removeValuesFromList(this.support_roles, DataTools.convertRoleArrayToIds(roles));
    }

//  Auto Roles
    public List<Long> getBotAutoRoleIds() {
        return this.getBotAutoRoles().stream().map(Role::getIdLong).toList();
    }

    public List<Role> getBotAutoRoles() {
        return DataTools.getRolesFromIds(this.getGuild(), this.bot_auto_roles);
    }

    public void setBotAutoRoles(List<Role> roles) {
        DataTools.setList(this.bot_auto_roles, List.of(DataTools.convertRoleListToIds(roles)));
    }

    public void addBotAutoRoles(Role... roles) {
        DataTools.addToList(this.bot_auto_roles, DataTools.convertRoleArrayToIds(roles));
    }

    public void removeBotAutoRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.bot_auto_roles, indices);
    }

    public void removeBotAutoRolesByRole(Role... roles) {
        DataTools.removeValuesFromList(this.bot_auto_roles, DataTools.convertRoleArrayToIds(roles));
    }

    public List<Long> getUserAutoRoleIds() {
        return this.getUserAutoRoles().stream().map(Role::getIdLong).toList();
    }

    public List<Role> getUserAutoRoles() {
        return DataTools.getRolesFromIds(this.getGuild(), this.user_auto_roles);
    }

    public void setUserAutoRoles(List<Role> roles) {
        DataTools.setList(this.user_auto_roles, List.of(DataTools.convertRoleListToIds(roles)));
    }

    public void addUserAutoRoles(Role... roles) {
        DataTools.addToList(this.user_auto_roles, DataTools.convertRoleArrayToIds(roles));
    }

    public void removeUserAutoRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.user_auto_roles, indices);
    }

    public void removeUserAutoRolesByRole(Role... roles) {
        DataTools.removeValuesFromList(this.user_auto_roles, DataTools.convertRoleArrayToIds(roles));
    }

//  Static Messages
    public Message getOfflineMessage() {
        return this.offline_message;
    }

    public void setOfflineMessage(Message offline_message) {
        this.offline_message = offline_message;
    }

//  Auto Messages
    public AutoMessageData getBoostMessage() {
        return this.boost_message;
    }

    public void setBoostMessage(AutoMessageData boost_message) {
        this.boost_message = boost_message;
    }

    public AutoMessageData getGoodbyeMessage() {
        return this.goodbye_message;
    }

    public void setGoodbyeMessage(AutoMessageData goodbye_message) {
        this.goodbye_message = goodbye_message;
    }

    public AutoMessageData getLevelUpMessage() {
        return this.level_up_message;
    }

    public void setLevelUpMssage(AutoMessageData level_up_message) {
        this.level_up_message = level_up_message;
    }

    public AutoMessageData getWelcomeMessage() {
        return this.welcome_message;
    }

    public void setWelcomeMessage(AutoMessageData welcome_message) {
        this.welcome_message = welcome_message;
    }

//  Static Channels
    public long getCommunityInboxChannelId() {
        return this.getCommunityInboxChannel().getIdLong();
    }

    public TextChannel getCommunityInboxChannel() {
        return this.getGuild().getTextChannelById(this.community_inbox_channel);
    }

    public void setCommunityInboxChannel(TextChannel community_inbox_channel) {
        if (community_inbox_channel != null) {
            this.community_inbox_channel = community_inbox_channel.getIdLong();
        } else {
            this.community_inbox_channel = 0L;
        }
    }

    public long getModerationInboxChannelId() {
        return this.getModerationInboxChannel().getIdLong();
    }

    public TextChannel getModerationInboxChannel() {
        return this.getGuild().getTextChannelById(this.moderation_inbox_channel);
    }

    public void setModerationInboxChannel(TextChannel moderation_inbox_channel) {
        if (moderation_inbox_channel != null) {
            this.moderation_inbox_channel = moderation_inbox_channel.getIdLong();
        } else {
            this.moderation_inbox_channel = 0L;
        }
    }

    public long getSuggestionInboxChannelId() {
        return this.getSuggestionInboxChannel().getIdLong();
    }

    public TextChannel getSuggestionInboxChannel() {
        return this.getGuild().getTextChannelById(this.suggestion_inbox_channel);
    }

    public void setSuggestionInboxChannel(TextChannel suggestion_inbox_channel) {
        if (suggestion_inbox_channel != null) {
            this.suggestion_inbox_channel = suggestion_inbox_channel.getIdLong();
        } else {
            this.suggestion_inbox_channel = 0L;
        }
    }

    public VoiceChannel getSupportTalk() {
        return this.getGuild().getVoiceChannelById(this.support_talk);
    }

    public long getSupportTalkId() {
        return this.getSupportTalk().getIdLong();
    }

    public void setSupportTalk(VoiceChannel support_talk) {
        if (support_talk != null) {
            this.support_talk = support_talk.getIdLong();
        } else {
            this.support_talk = 0L;
        }
    }

//	Auto Channels
    public ConcurrentHashMap<Long, Join2CreateChannelData> getJoin2CreateChannelIdDatas() {
        return this.join2create_channels;
    }

    public ConcurrentHashMap<VoiceChannel, Join2CreateChannelData> getJoin2CreateChannelDatas() {
        ConcurrentHashMap<VoiceChannel, Join2CreateChannelData> return_value = new ConcurrentHashMap<>();
        for (Map.Entry<Long, Join2CreateChannelData> entry : this.join2create_channels.entrySet()) {
            return_value.put(this.getGuild().getVoiceChannelById(entry.getKey()), entry.getValue());
        }
        return return_value;
    }

    public Join2CreateChannelData getJoin2CreateChannelData(long id) {
        return this.join2create_channels.get(id);
    }

    public Join2CreateChannelData getJoin2CreateChannelData(VoiceChannel channel) {
        return this.join2create_channels.get(channel.getIdLong());
    }

    public void setJoin2CreateChannelIds(ConcurrentHashMap<Long, Join2CreateChannelData> join2create_channels) {
        DataTools.setMap(this.join2create_channels, join2create_channels);
    }

    public void setJoin2CreateChannels(ConcurrentHashMap<VoiceChannel, Join2CreateChannelData> join2create_channels) {
        ConcurrentHashMap<Long, Join2CreateChannelData> converted_map = new ConcurrentHashMap<>();
        if (this.join2create_channels != null) {
            for (Map.Entry<VoiceChannel, Join2CreateChannelData> entry : join2create_channels.entrySet()) {
                converted_map.put(entry.getKey().getIdLong(), entry.getValue());
            }
        }
        this.join2create_channels = converted_map;
    }

    public void addJoin2CreateChannels(Join2CreateChannelData... datas) {
        for (Join2CreateChannelData data : datas) {
            this.join2create_channels.put(data.getVoiceChannelId(), data);
        }
    }

    public void removeJoin2CreateChannels(long... channel_ids) {
        for (long id : channel_ids) {
            this.join2create_channels.remove(id);
        }
    }

    public void removeJoin2CreateChannels(VoiceChannel... channels) {
        for (VoiceChannel channel : channels) {
            this.join2create_channels.remove(channel.getIdLong());
        }
    }

    public void removeJoin2CreateChannelsByData(Join2CreateChannelData... datas) {
        for (Join2CreateChannelData data : datas) {
            this.join2create_channels.remove(data.getVoiceChannelId());
        }
    }

//  Other
    public ConcurrentHashMap<Integer, LevelRewardData> getLevelRewards() {
        return this.level_rewards;
    }

    public LevelRewardData getLevelRewardData(int level_count) {
        return this.level_rewards.get(level_count);
    }

    public void setLevelRewards(ConcurrentHashMap<Integer, LevelRewardData> level_rewards) {
        DataTools.setMap(this.level_rewards, level_rewards);
    }

    public void addLevelRewards(LevelRewardData... datas) {
        for (LevelRewardData data : datas) {
            this.level_rewards.put(data.getLevelCount(), data);
        }
    }

    public void removeLevelRewards(int... level_counts) {
        for (int level_count : level_counts) {
            this.level_rewards.remove(level_count);
        }
    }

    public void removeLevelRewardsByData(LevelRewardData... datas) {
        for (LevelRewardData data : datas) {
            this.level_rewards.remove(data.getLevelCount());
        }
    }

    public ConcurrentHashMap<Integer, PenaltyData> getPenalties() {
        return this.penalties;
    }

    public PenaltyData getPenalty(int warning_count) {
        return this.penalties.get(warning_count);
    }

    public void setPenalties(ConcurrentHashMap<Integer, PenaltyData> penalties) {
        DataTools.setMap(this.penalties, penalties);
    }

    public void addPenalties(PenaltyData... datas) {
        for (PenaltyData data : datas) {
            this.penalties.put(data.getWarningLimit(), data);
        }
    }

    public void removePenalties(int... warning_counts) {
        for (int warning_count : warning_counts) {
            this.penalties.remove(warning_count);
        }
    }

    public void removePenaltiesByData(PenaltyData... datas) {
        for (PenaltyData data : datas) {
            this.penalties.remove(data.getWarningLimit());
        }
    }

    public ConcurrentHashMap<Long, ModMailData> getModmailIds() {
        return this.modmails;
    }

    public ConcurrentHashMap<TextChannel, ModMailData> getModmails() {
        ConcurrentHashMap<TextChannel, ModMailData> return_value = new ConcurrentHashMap<>();
        for (Map.Entry<Long, ModMailData> entry : this.modmails.entrySet()) {
            return_value.put(this.getGuild().getTextChannelById(entry.getKey()), entry.getValue());
        }
        return return_value;
    }

    public ModMailData getModMail(long channel_id) {
        return this.modmails.get(channel_id);
    }

    public ModMailData getModMail(TextChannel channel) {
        return this.modmails.get(channel.getIdLong());
    }

    public void setModmails(ConcurrentHashMap<TextChannel, ModMailData> modmails) {
        ConcurrentHashMap<Long, ModMailData> converted_map = new ConcurrentHashMap<>();
        if (modmails != null) {
            for (Map.Entry<TextChannel, ModMailData> entry : modmails.entrySet()) {
                converted_map.put(entry.getKey().getIdLong(), entry.getValue());
            }
        }
        this.modmails = converted_map;
    }

    public void addModmails(ModMailData... datas) {
        for (ModMailData data : datas) {
            this.modmails.put(data.getGuildChannelId(), data);
        }
    }

    public void removeModmails(long... channel_ids) {
        for (long id : channel_ids) {
            this.modmails.remove(id);
        }
    }

    public void removeModmails(TextChannel... channels) {
        for (TextChannel channel : channels) {
            this.modmails.remove(channel.getIdLong());
        }
    }

    public void removeModmailsByData(ModMailData... datas) {
        for (ModMailData data : datas) {
            this.modmails.remove(data.getGuildChannelId());
        }
    }

    public ConcurrentHashMap<Long, ConcurrentHashMap<Long, PollData>> getPollIds() {
        return this.polls;
    }

    public ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, PollData>> getPolls() {
        return DataTools.convertMesConMapToObj(this.getGuild(), this.polls);
    }

    public ConcurrentHashMap<Message, PollData> getPollsByChannel(long channel_id) {
        return this.getPollsByChannel(this.getGuild().getTextChannelById(channel_id));
    }

    public ConcurrentHashMap<Message, PollData> getPollsByChannel(TextChannel channel) {
        return DataTools.convertMesConMapOfChannelToObj(this.getGuild(), channel, this.polls);
    }

    public ConcurrentHashMap<Long, PollData> getPollIdsByChannel(long channel_id) {
        return this.getPollIdsByChannel(this.getGuild().getTextChannelById(channel_id));
    }

    public ConcurrentHashMap<Long, PollData> getPollIdsByChannel(TextChannel channel) {
        ConcurrentHashMap<Long, PollData> return_value = new ConcurrentHashMap<>();
        if (channel != null) {
            ConcurrentHashMap<Long, PollData> stored_map = this.polls.get(channel.getIdLong());
            if (stored_map != null) {
                return_value = stored_map;
            }
        }
        return return_value;
    }

    public PollData getPoll(TextChannel channel, Message message) {
        if (channel != null && message != null) {
            ConcurrentHashMap<Long, PollData> returned_map = this.polls.get(channel.getIdLong());
            if (returned_map != null) {
                return returned_map.get(message.getIdLong());
            }
        }
        return null;
    }

    public void setPolls(ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, PollData>> polls) {
        this.polls = DataTools.convertMesConMapToIds(polls);
    }

    public void setPollsByChannel(TextChannel channel, ConcurrentHashMap<Message, PollData> polls) {
        ConcurrentHashMap<Long, PollData> converted_map = DataTools.convertMesConMapOfChannelToIds(channel, polls);
        if (!converted_map.isEmpty()) {
            this.polls.put(channel.getIdLong(), converted_map);
        }
    }

    public void addPolls(PollData... datas) {
        DataTools.addDataToMesConMap(this.polls, datas);
    }

    public void removePoll(TextChannel channel, Message message) {
        if (channel != null && message != null) {
            ConcurrentHashMap<Long, PollData> stored_map = this.polls.get(channel.getIdLong());
            if (stored_map != null) {
                stored_map.remove(message.getIdLong());
            }
        }
    }

    public void removePollsByData(PollData... datas) {
        for (int i = 0; i < datas.length; i++) {
            if (datas[i].getChannelId() != 0L && datas[i].getMessageId() != 0L) {
                ConcurrentHashMap<Long, PollData> stored_map = this.polls.get(datas[i].getChannelId());
                if (stored_map != null) {
                    stored_map.remove(datas[i].getMessageId());
                }
            }
        }
    }

    public ConcurrentHashMap<Long, ConcurrentHashMap<Long, ReactionRoleData>> getReactionRoleIds() {
        return this.reaction_roles;
    }

    public ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, ReactionRoleData>> getReactionRoles() {
        return DataTools.convertMesConMapToObj(this.getGuild(), this.reaction_roles);
    }

    public ConcurrentHashMap<Message, ReactionRoleData> getReactionRolesByChannel(long channel_id) {
        return this.getReactionRolesByChannel(this.getGuild().getTextChannelById(channel_id));
    }

    public ConcurrentHashMap<Message, ReactionRoleData> getReactionRolesByChannel(TextChannel channel) {
        return DataTools.convertMesConMapOfChannelToObj(this.getGuild(), channel, this.reaction_roles);
    }

    public ConcurrentHashMap<Long, ReactionRoleData> getReactionRoleIdsByChannel(long channel_id) {
        return this.getReactionRoleIdsByChannel(this.getGuild().getTextChannelById(channel_id));
    }

    public ConcurrentHashMap<Long, ReactionRoleData> getReactionRoleIdsByChannel(TextChannel channel) {
        ConcurrentHashMap<Long, ReactionRoleData> return_value = new ConcurrentHashMap<>();
        if (channel != null) {
            ConcurrentHashMap<Long, ReactionRoleData> stored_map = this.reaction_roles.get(channel.getIdLong());
            if (stored_map != null) {
                return_value = stored_map;
            }
        }
        return return_value;
    }

    public ReactionRoleData getReactionRole(TextChannel channel, Message message) {
        if (channel != null && message != null) {
            ConcurrentHashMap<Long, ReactionRoleData> returned_map = this.reaction_roles.get(channel.getIdLong());
            if (returned_map != null) {
                return returned_map.get(message.getIdLong());
            }
        }
        return null;
    }

    public void setReactionRoles(
            ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, ReactionRoleData>> reaction_roles) {
        this.reaction_roles = DataTools.convertMesConMapToIds(reaction_roles);
    }

    public void setReactionRolesByChannel(TextChannel channel,
            ConcurrentHashMap<Message, ReactionRoleData> reaction_roles) {
        ConcurrentHashMap<Long, ReactionRoleData> converted_map = DataTools.convertMesConMapOfChannelToIds(channel,
                reaction_roles);
        if (!converted_map.isEmpty()) {
            this.reaction_roles.put(channel.getIdLong(), converted_map);
        }
    }

    public void addReactionRoles(ReactionRoleData... datas) {
        DataTools.addDataToMesConMap(this.reaction_roles, datas);
    }

    public void removeReactionRole(TextChannel channel, Message message) {
        if (channel != null && message != null) {
            ConcurrentHashMap<Long, ReactionRoleData> stored_map = this.reaction_roles.get(channel.getIdLong());
            if (stored_map != null) {
                stored_map.remove(message.getIdLong());
            }
        }
    }

    public void removeReactionRoleByData(ReactionRoleData... datas) {
        for (int i = 0; i < datas.length; i++) {
            if (datas[i].getChannelId() != 0L && datas[i].getMessageId() != 0L) {
                ConcurrentHashMap<Long, ReactionRoleData> stored_map = this.reaction_roles.get(datas[i].getChannelId());
                if (stored_map != null) {
                    stored_map.remove(datas[i].getMessageId());
                }
            }
        }
    }

    public ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, GiveawayData>> getGiveaways() {
        return DataTools.convertMesConMapToObj(this.getGuild(), this.giveaways);
    }

    public ConcurrentHashMap<Message, GiveawayData> getGiveawaysByChannel(TextChannel channel) {
        return DataTools.convertMesConMapOfChannelToObj(this.getGuild(), channel, this.giveaways);
    }

    public GiveawayData getGiveaway(TextChannel channel, Message message) {
        if (channel != null && message != null) {
            ConcurrentHashMap<Long, GiveawayData> returned_map = this.giveaways.get(channel.getIdLong());
            if (returned_map != null) {
                return returned_map.get(message.getIdLong());
            }
        }
        return null;
    }

    public void setGiveaways(ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, GiveawayData>> giveaways) {
        this.giveaways = DataTools.convertMesConMapToIds(giveaways);
    }

    public void setGiveawaysByChannel(TextChannel channel, ConcurrentHashMap<Message, GiveawayData> giveaways) {
        ConcurrentHashMap<Long, GiveawayData> converted_map = DataTools.convertMesConMapOfChannelToIds(channel,
                giveaways);
        if (!converted_map.isEmpty()) {
            this.giveaways.put(channel.getIdLong(), converted_map);
        }
    }

    public void addGiveaways(GiveawayData... datas) {
        DataTools.addDataToMesConMap(this.giveaways, datas);
    }

    public void removeGiveaway(TextChannel channel, Message message) {
        if (channel != null && message != null) {
            ConcurrentHashMap<Long, GiveawayData> stored_map = this.giveaways.get(channel.getIdLong());
            if (stored_map != null) {
                stored_map.remove(message.getIdLong());
            }
        }
    }

    public void removeGiveawayByData(ReactionRoleData... datas) {
        for (int i = 0; i < datas.length; i++) {
            if (datas[i].getChannelId() != 0L && datas[i].getMessageId() != 0L) {
                ConcurrentHashMap<Long, GiveawayData> stored_map = this.giveaways.get(datas[i].getChannelId());
                if (stored_map != null) {
                    stored_map.remove(datas[i].getMessageId());
                }
            }
        }
    }

    private static abstract class Key {
        public static final String GUILD_ID = "id";
        public static final String GUILD_NAME = "name";
        public static final String STATIC_ROLES = "static_roles";
        public static final String ADMIN_ROLES = "admin_roles";
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
        public static final String OTHER = "other";
        public static final String LEVEL_REWARDS = "level_rewards";
        public static final String PENALTIES = "penalties";
        public static final String MODMAILS = "modmails";
        public static final String POLLS = "polls";
        public static final String REACTION_ROLES = "reaction_roles";
        public static final String GIVEAWAYS = "giveaways";
    }

    public ConcurrentHashMap<Long, Join2CreateChannelData> getJoin2CreateChannelDataIds() {
        return null;
    }

    public ReactionRoleData getReactionRole(long channelId, long messageId) {
        return null;
    }
}