package assets.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.base.exceptions.EntityNotFoundException;
import assets.base.exceptions.EntityNotFoundException.ReferenceType;
import assets.data.single.AutoMessageData;
import assets.data.single.GiveawayData;
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

	private final long guild;
	private List<Long> admin_roles = new ArrayList<>();
	private List<Long> custom_channel_policing_roles = new ArrayList<>();
	private List<Long> moderation_roles = new ArrayList<>();
	private List<Long> support_roles = new ArrayList<>();
	private List<Long> bot_auto_roles = new ArrayList<>();
	private List<Long> user_auto_roles = new ArrayList<>();
	private AutoMessageData boost_message, goodbye_message, level_up_message, welcome_message;
	private long community_inbox_channel, moderation_inbox_channel, suggestion_inbox_channel, support_talk;
	private Message offline_message;
	private ConcurrentHashMap<Long, Join2CreateChannelData> join2create_channels = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long, Long> custom_categories = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, Long> level_rewards = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, PenaltyData> penalties = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long, ModMailData> modmails = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long, ConcurrentHashMap<Long, PollData>> polls = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long, ConcurrentHashMap<Long, ReactionRoleData>> reaction_roles = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long, ConcurrentHashMap<Long, GiveawayData>> giveaways = new ConcurrentHashMap<>();

	public GuildData(JSONObject data) throws EntityNotFoundException {
		this.guild = data.getLong(Key.GUILD_ID);
		Guild guild_object = Bot.getAPI().getGuildById(guild);
		if (guild_object == null) {
		    throw new EntityNotFoundException(ReferenceType.GUILD).setReferenceId(guild);
		} else {
		    this.instanciateFromJSON(data);
		}
	}
	
	public GuildData(Guild guild) {
	    this.guild = guild.getIdLong();
	}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        Guild guild_object = Bot.getAPI().getGuildById(guild);
        
        this.admin_roles = DataTools.getIdsFromArrayKeys(data, Key.STATIC_ROLES, Key.ADMIN_ROLES);
        this.custom_channel_policing_roles = DataTools.getIdsFromArrayKeys(data, Key.STATIC_ROLES, Key.CUSTOM_CHANNEL_POLICING_ROLES);
        this.moderation_roles = DataTools.getIdsFromArrayKeys(data, Key.STATIC_ROLES, Key.MODERATION_ROLES);
        this.support_roles = DataTools.getIdsFromArrayKeys(data, Key.STATIC_ROLES, Key.SUPPORT_ROLES);
        this.bot_auto_roles = DataTools.getIdsFromArrayKeys(data, Key.AUTO_ROLES, Key.BOT_AUTO_ROLES);
        this.user_auto_roles = DataTools.getIdsFromArrayKeys(data, Key.AUTO_ROLES, Key.USER_AUTO_ROLES);

        JSONObject auto_messages = data.getJSONObject(Key.AUTO_MESSAGES);
        JSONObject boost_message_data = auto_messages.getJSONObject(Key.BOOST_MESSAGE);
        if (!boost_message_data.isEmpty()) {
            this.boost_message = new AutoMessageData(guild_object, boost_message_data);
        }
        JSONObject goodbye_message_data = auto_messages.getJSONObject(Key.GOODBYE_MESSAGE);
        if (!goodbye_message_data.isEmpty()) {
            this.goodbye_message = new AutoMessageData(guild_object, goodbye_message_data);
        }
        JSONObject level_up_message_data = auto_messages.getJSONObject(Key.LEVEL_UP_MESSAGE);
        if (!level_up_message_data.isEmpty()) {
            this.level_up_message = new AutoMessageData(guild_object, level_up_message_data);
        }
        JSONObject welcome_message_data = auto_messages.getJSONObject(Key.WELCOME_MESSAGE);
        if (!welcome_message_data.isEmpty()) {
            this.welcome_message = new AutoMessageData(guild_object, welcome_message_data);
        }

        JSONObject static_channels = data.getJSONObject(Key.STATIC_CHANNELS);
        this.community_inbox_channel = static_channels.getLong(Key.COMMUNITY_INBOX_CHANNEL);
        this.moderation_inbox_channel = static_channels.getLong(Key.MODERATION_INBOX_CHANNEL);
        this.suggestion_inbox_channel = static_channels.getLong(Key.SUGGESTION_INBOX_CHANNEL);
        this.support_talk = static_channels.getLong(Key.SUPPORT_TALK);

        JSONArray offline_message_data = data.getJSONObject(Key.STATIC_MESSAGES).getJSONArray(Key.OFFLINE_MESSAGE);
        if (!offline_message_data.isEmpty()) {
            TextChannel channel = guild_object.getTextChannelById(offline_message_data.getLong(1));
            if (channel != null) {
                this.offline_message = channel.retrieveMessageById(offline_message_data.getLong(0)).complete();
            }
        }

        JSONObject j2c_channels_data = data.getJSONObject(Key.AUTO_CHANNELS).getJSONObject(Key.JOIN2CREATE_CHANNELS);
        j2c_channels_data.keySet().forEach(channelId -> {
            VoiceChannel channel = guild_object.getVoiceChannelById(channelId);
            if (channel != null) {
                join2create_channels.put(Long.valueOf(channelId), new Join2CreateChannelData(channel, j2c_channels_data.getJSONObject(channelId)));
            }
        });

        JSONObject custom_categories_data = data.getJSONObject(Key.AUTO_CHANNELS).getJSONObject(Key.CUSTOM_CATEGORIES);
        custom_categories_data.keySet().forEach(categoryId -> {
            Category category = guild_object.getCategoryById(categoryId);
            Member member = guild_object.retrieveMemberById(custom_categories_data.getLong(categoryId)).complete();
            if (category != null && member != null) {
                custom_categories.put(category.getIdLong(), member.getIdLong());
            }
        });

        JSONObject lvl_reward_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.LEVEL_REWARDS);
        lvl_reward_data.keySet().forEach(level_count -> {
            Role role = guild_object.getRoleById(lvl_reward_data.getLong(level_count));
            if (role != null) {
                level_rewards.put(Integer.valueOf(level_count), role.getIdLong());
            }
        });

        JSONObject penalties_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.PENALTIES);
        penalties_data.keySet().forEach(warning_count -> penalties.put(Integer.valueOf(warning_count), new PenaltyData(guild_object, penalties_data.getJSONObject(warning_count))));

        JSONObject modmails_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.MODMAILS);
        modmails_data.keySet().forEach(channelId -> {
            TextChannel channel = guild_object.getTextChannelById(channelId);
            if (channel != null) {
                modmails.put(channel.getIdLong(), new ModMailData(guild_object, channel, modmails_data.getJSONObject(channelId)));
            }
        });

        JSONObject polls_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.POLLS);
        polls_data.keySet().forEach(channelId -> {
            TextChannel channel = guild_object.getTextChannelById(channelId);
            if (channel != null) {
                ConcurrentHashMap<Long, PollData> pollSubMap = new ConcurrentHashMap<>();
                JSONObject polls_sub_data = polls_data.getJSONObject(channelId);
                polls_sub_data.keySet().forEach(messageId -> {
                    Message message = channel.retrieveMessageById(messageId).complete();
                    if (message != null) {
                        pollSubMap.put(message.getIdLong(), new PollData(message, polls_sub_data.getJSONObject(messageId)));
                    }
                });
                if (!pollSubMap.isEmpty()) {
                    polls.put(channel.getIdLong(), pollSubMap);
                }
            }
        });

        JSONObject reaction_roles_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.REACTION_ROLES);
        reaction_roles_data.keySet().forEach(channelId -> {
            TextChannel channel = guild_object.getTextChannelById(channelId);
            if (channel != null) {
                ConcurrentHashMap<Long, ReactionRoleData> reactionRoleSubMap = new ConcurrentHashMap<>();
                JSONObject reaction_role_sub_data = reaction_roles_data.getJSONObject(channelId);
                reaction_role_sub_data.keySet().forEach(messageId -> {
                    Message message = channel.retrieveMessageById(messageId).complete();
                    if (message != null) {
                        reactionRoleSubMap.put(message.getIdLong(), new ReactionRoleData(reaction_role_sub_data.getJSONObject(messageId)));
                    }
                });
                if (!reactionRoleSubMap.isEmpty()) {
                    reaction_roles.put(channel.getIdLong(), reactionRoleSubMap);
                }
            }
        });
        
        JSONObject giveaways_data = data.getJSONObject(Key.OTHER).getJSONObject(Key.GIVEAWAYS);
        giveaways_data.keySet().forEach(channelId -> {
            TextChannel channel = guild_object.getTextChannelById(channelId);
            if (channel != null) {
                ConcurrentHashMap<Long, GiveawayData> giveawaySubMap = new ConcurrentHashMap<>();
                JSONObject giveaway_sub_data = giveaways_data.getJSONObject(channelId);
                giveaway_sub_data.keySet().forEach(messageId -> {
                    Message message = channel.retrieveMessageById(messageId).complete();
                    if (message != null) {
                        giveawaySubMap.put(message.getIdLong(), new GiveawayData(message, giveaway_sub_data.getJSONObject(messageId)));
                    }
                });
                if (!giveawaySubMap.isEmpty()) {
                    giveaways.put(channel.getIdLong(), giveawaySubMap);
                }
            }
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
	    
	    static_roles.put(Key.ADMIN_ROLES, new JSONArray(admin_roles));
	    static_roles.put(Key.CUSTOM_CHANNEL_POLICING_ROLES, new JSONArray(custom_channel_policing_roles));
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
        
        JSONObject custom_channel_categories_object = new JSONObject();
        custom_categories.forEach((category, owner) -> custom_channel_categories_object.put(String.valueOf(category), String.valueOf(owner)));
        auto_channels.put(Key.CUSTOM_CATEGORIES, custom_channel_categories_object);
        
        JSONObject level_rewards_object = new JSONObject();
        level_rewards.forEach((level_count, reward_role) -> level_rewards_object.put(String.valueOf(level_count), String.valueOf(reward_role)));
        other.put(Key.LEVEL_REWARDS, level_rewards_object);
	    
	    JSONObject join2create_channels_object = new JSONObject();
	    join2create_channels.forEach((channel, data) -> join2create_channels_object.put(String.valueOf(channel), data.compileToJSON()));
	    auto_channels.put(Key.JOIN2CREATE_CHANNELS, join2create_channels_object);
	    
	    JSONObject penalties_object = new JSONObject();
	    penalties.forEach((warning_count, data) -> penalties_object.put(String.valueOf(warning_count), data.compileToJSON()));
	    other.put(Key.PENALTIES, penalties_object);
	    
	    JSONObject modmails_object = new JSONObject();
	    modmails.forEach((channel, data) -> modmails_object.put(String.valueOf(channel), data.compileToJSON()));
	    other.put(Key.MODMAILS, modmails_object);
	    
	    JSONObject polls_object = new JSONObject();
	    polls.forEach((channel, message_map) -> {
	        JSONObject message_map_object = new JSONObject();
	        message_map.forEach((message, data) -> message_map_object.put(String.valueOf(message), data.compileToJSON()));
	        if (!message_map_object.isEmpty()) {
	            polls_object.put(String.valueOf(channel), message_map_object);
	        }
	    });
	    other.put(Key.POLLS, polls_object);
	    
	    JSONObject reaction_roles_object = new JSONObject();
        reaction_roles.forEach((channel, message_map) -> {
            JSONObject message_map_object = new JSONObject();
            message_map.forEach((message, data) -> message_map_object.put(String.valueOf(message), data.compileToJSON()));
            if (!message_map_object.isEmpty()) {
                reaction_roles_object.put(String.valueOf(channel), message_map_object);
            }
        });
        other.put(Key.REACTION_ROLES, reaction_roles_object);
        
        JSONObject giveaways_object = new JSONObject();
        giveaways.forEach((channel, message_map) -> {
            JSONObject message_map_object = new JSONObject();
            message_map.forEach((message, data) -> message_map_object.put(String.valueOf(message), data.compileToJSON()));
            if (!message_map_object.isEmpty()) {
                giveaways_object.put(String.valueOf(channel), message_map_object);
            }
        });
        other.put(Key.GIVEAWAYS, giveaways_object);

        compiledData.put(Key.GUILD_ID, guild);
        compiledData.put(Key.GUILD_NAME, Bot.getAPI().getGuildById(guild).getName());
        compiledData.put(Key.STATIC_ROLES, static_roles);
        compiledData.put(Key.AUTO_ROLES, auto_roles);
        compiledData.put(Key.STATIC_MESSAGES, static_messages);
        compiledData.put(Key.AUTO_MESSAGES, auto_messages);
        compiledData.put(Key.STATIC_CHANNELS, static_channels);
        compiledData.put(Key.AUTO_CHANNELS, auto_channels);
        compiledData.put(Key.OTHER, other);
	    
	    return compiledData;
	}

    @Override
    public boolean verify(ReferenceType type) {
        // TODO Auto-generated method stub
        return false;
    }
    
    public Guild getGuild() {
        return Bot.getAPI().getGuildById(guild);
    }
    
//  Static Roles
	public List<Role> getAdminRoles() {
		return DataTools.getRolesFromIds(this.getGuild(), this.admin_roles);
	}

	public GuildData setAdminRoles(List<Role> roles) {
	    DataTools.setList(this.admin_roles, List.of(DataTools.convertRoleListToIds(roles)));
		return this;
	}
    
    public GuildData addAdminRoles(Role... roles) {
        DataTools.addToList(this.admin_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }
    
    public GuildData removeAdminRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.admin_roles, indices);
        return this;
    }
    
    public GuildData removeAdminRolesByRole(Role... roles) {
        DataTools.removeValuesFromList(this.admin_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }
    
    public List<Role> getCustomChannelPolicingRoles() {
        return DataTools.getRolesFromIds(this.getGuild(), this.custom_channel_policing_roles);
    }

    public GuildData setCustomChannelPolicingRoles(List<Role> roles) {
        DataTools.setList(this.custom_channel_policing_roles, List.of(DataTools.convertRoleListToIds(roles)));
        return this;
    }
    
    public GuildData addCustomChannelPolicingRoles(Role... roles) {
        DataTools.addToList(this.custom_channel_policing_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }
    
    public GuildData removeCustomChannelPolicingRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.custom_channel_policing_roles, indices);
        return this;
    }
    
    public GuildData removeCustomChannelPolicingRolesByRole(Role... roles) {
        DataTools.removeValuesFromList(this.custom_channel_policing_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }

	public List<Role> getModerationRoles() {
        return DataTools.getRolesFromIds(this.getGuild(), this.moderation_roles);
	}

	public GuildData setModerationRoles(List<Role> roles) {
        DataTools.setList(this.moderation_roles, List.of(DataTools.convertRoleListToIds(roles)));
        return this;
	}
    
    public GuildData addModerationRoles(Role... roles) {
        DataTools.addToList(this.moderation_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }
    
    public GuildData removeModerationRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.moderation_roles, indices);
        return this;
    }
    
    public GuildData removeModerationRolesByRole(Role... roles) {
        DataTools.addToList(this.moderation_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }

	public List<Role> getSupportRoles() {
        return DataTools.getRolesFromIds(this.getGuild(), this.support_roles);
	}

	public GuildData setSupportRoles(List<Role> roles) {
        DataTools.setList(this.support_roles, List.of(DataTools.convertRoleListToIds(roles)));
        return this;
	}
	
    public GuildData addSupportRoles(Role... roles) {
        DataTools.addToList(this.support_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }
    
    public GuildData removeSupportRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.support_roles, indices);
        return this;
    }
    
    public GuildData removeSupportRolesByRole(Role... roles) {
        DataTools.removeValuesFromList(this.support_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }
    
//  Auto Roles
	public List<Role> getBotAutoRoles() {
        return DataTools.getRolesFromIds(this.getGuild(), this.bot_auto_roles);
	}

	public GuildData setBotAutoRoles(List<Role> roles) {
        DataTools.setList(this.bot_auto_roles, List.of(DataTools.convertRoleListToIds(roles)));
        return this;
	}
	
	public GuildData addBotAutoRoles(Role... roles) {
        DataTools.addToList(this.bot_auto_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }
    
    public GuildData removeBotAutoRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.bot_auto_roles, indices);
        return this;
    }
    
    public GuildData removeBotAutoRolesByRole(Role... roles) {
        DataTools.removeValuesFromList(this.bot_auto_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }

	public List<Role> getUserAutoRoles() {
        return DataTools.getRolesFromIds(this.getGuild(), this.user_auto_roles);
	}

	public GuildData setUserAutoRoles(List<Role> roles) {
        DataTools.setList(this.user_auto_roles, List.of(DataTools.convertRoleListToIds(roles)));
        return this;
	}
	public GuildData addUserAutoRoles(Role... roles) {
        DataTools.addToList(this.user_auto_roles, DataTools.convertRoleArrayToIds(roles));
        return this;
    }
    
    public GuildData removeUserAutoRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.user_auto_roles, indices);
        return this;
    }
    
    public GuildData removeUserAutoRolesByRole(Role... roles) {
        DataTools.removeValuesFromList(this.user_auto_roles, DataTools.convertRoleArrayToIds(roles));
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
		return this.getGuild().getTextChannelById(this.community_inbox_channel);
	}

	public GuildData setCommunityInboxChannel(TextChannel community_inbox_channel) {
		this.community_inbox_channel = community_inbox_channel.getIdLong();
        return this;
	}

	public TextChannel getModerationInboxChannel() {
		return this.getGuild().getTextChannelById(this.moderation_inbox_channel);
	}

	public GuildData setModerationInboxChannel(TextChannel moderation_inbox_channel) {
		this.moderation_inbox_channel = moderation_inbox_channel.getIdLong();
        return this;
	}

	public TextChannel getSuggestionInboxChannel() {
		return this.getGuild().getTextChannelById(this.suggestion_inbox_channel);
	}

	public GuildData setSuggestionInboxChannel(TextChannel suggestion_inbox_channel) {
		this.suggestion_inbox_channel = suggestion_inbox_channel.getIdLong();
        return this;
	}

	public VoiceChannel getSupportTalk() {
		return this.getGuild().getVoiceChannelById(this.support_talk);
	}

	public GuildData setSupportTalk(VoiceChannel support_talk) {
		this.support_talk = support_talk.getIdLong();
        return this;
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
	
	public GuildData setJoin2CreateChannelIds(ConcurrentHashMap<Long, Join2CreateChannelData> join2create_channels) {
	    DataTools.setMap(this.join2create_channels, join2create_channels);
	    return this;
	}
	
	public GuildData setJoin2CreateChannels(ConcurrentHashMap<VoiceChannel, Join2CreateChannelData> join2create_channels) {
	    ConcurrentHashMap<Long, Join2CreateChannelData> converted_map = new ConcurrentHashMap<>();
	    for (Map.Entry<VoiceChannel, Join2CreateChannelData> entry : join2create_channels.entrySet()) {
	        converted_map.put(entry.getKey().getIdLong(), entry.getValue());
	    }
	    DataTools.setMap(this.join2create_channels, converted_map);
        return this;
	}
	
	public GuildData addJoin2CreateChannels(Join2CreateChannelData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.join2create_channels.put(datas[i].getVoiceChannelId(), datas[i]);
	    }
        return this;
	}
	
	public GuildData removeJoin2CreateChannels(long... channel_ids) {
	    for (int i = 0; i < channel_ids.length; i++) {
            this.join2create_channels.remove(channel_ids[i]);
        }
        return this;
	}
	
	public GuildData removeJoin2CreateChannels(VoiceChannel... channels) {
	    for (int i = 0; i < channels.length; i++) {
	        this.join2create_channels.remove(channels[i].getIdLong());
	    }
        return this;
	}
	
	public GuildData removeJoin2CreateChannelsByData(Join2CreateChannelData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.join2create_channels.remove(datas[i].getVoiceChannelId());
	    }
        return this;
	}
	
	public ConcurrentHashMap<Long, Long> getCustomCategoryIds() {
	    return this.custom_categories;
	}

	public ConcurrentHashMap<Category, Member> getCustomCategories() {
        ConcurrentHashMap<Category, Member> return_value = new ConcurrentHashMap<>();
        Map<Long, Member> converted_members = this.getGuild().retrieveMembersByIds(false, this.custom_categories.values()).get().stream().collect(Collectors.toMap(Member::getIdLong, member -> member));
        for (Map.Entry<Long, Long> entry : this.custom_categories.entrySet()) {
            return_value.put(this.getGuild().getCategoryById(entry.getKey()), converted_members.get(entry.getValue()));
        }
		return return_value;
	}
	
	public long getCustomCategoryOwnerId(long category_id) {
	    return this.custom_categories.get(category_id);
	}
	
	public long getCustomCategoryOwnerId(Category category) {
	    return this.custom_categories.get(category.getIdLong());
	}
	
	public Member getCustomCategoryOwner(long category_id) {
	    return this.getGuild().retrieveMemberById(this.custom_categories.get(category_id)).complete();
	}
	
	public Member getCustomCategoryOwner(Category category) {
	    return this.getGuild().retrieveMemberById(this.custom_categories.get(category.getIdLong())).complete();
	}
	
	public GuildData setCustomCategoryIds(ConcurrentHashMap<Long, Long> custom_categories) {
	    DataTools.setMap(this.custom_categories, custom_categories);
	    return this;
	}

	public GuildData setCustomCategories(ConcurrentHashMap<Category, Member> custom_categories) {
        ConcurrentHashMap<Long, Long> converted_map = new ConcurrentHashMap<>();
        for (Map.Entry<Category, Member> entry : custom_categories.entrySet()) {
            converted_map.put(entry.getKey().getIdLong(), entry.getValue().getIdLong());
        }
        DataTools.setMap(this.custom_categories, converted_map);
        return this;
	}
	
	public GuildData addCustomCategoryOwner(Long category_id, Long member_id) {
	    this.custom_categories.put(category_id, member_id);
	    return this;
	}
	
	public GuildData addCustomCategoryOwner(Category category, Member owner) {
	    this.custom_categories.put(category.getIdLong(), owner.getIdLong());
        return this;
	}
	
	public GuildData removeCustomCategories(long... category_ids) {
	    for (int i = 0; i < category_ids.length; i++) {
            this.custom_categories.remove(category_ids[i]);
        }
	    return this;
	}
	
	public GuildData removeCustomCategories(Category... categories) {
	    for (int i = 0; i < categories.length; i++) {
	        this.custom_categories.remove(categories[i].getIdLong());
	    }
        return this;
	}
	
	public GuildData removeCustomCategoriesByOwnerIds(Long... owner_ids) {
	    DataTools.removeValuesFromMap(this.custom_categories, owner_ids);
	    return this;
	}
	
	public GuildData removeCustomCategoriesByOwner(Member... owners) {
	    DataTools.removeValuesFromMap(this.custom_categories, Arrays.stream(owners).map(Member::getIdLong).toArray(Long[]::new));
        return this;
	}
	
//  Other
	public ConcurrentHashMap<Integer, Role> getLevelRewards() {
	    ConcurrentHashMap<Integer, Role> return_value = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, Long> entry : this.level_rewards.entrySet()) {
            return_value.put(entry.getKey(), this.getGuild().getRoleById(entry.getValue()));
        }
        return return_value;
	}
	
	public long getLevelRewardId(int level_count) {
	    return this.level_rewards.get(level_count);
	}
	
	public Role getLevelReward(int level_count) {
	    return this.getGuild().getRoleById(this.level_rewards.get(level_count));
	}

	public GuildData setLevelRewards(ConcurrentHashMap<Integer, Role> level_rewards) {
	    ConcurrentHashMap<Integer, Long> converted_map = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, Role> entry : level_rewards.entrySet()) {
            converted_map.put(entry.getKey(), entry.getValue().getIdLong());
        }
        DataTools.setMap(this.level_rewards, converted_map);
        return this;
	}
	
	public GuildData addLevelRewardId(int level_count, long reward_id) {
	    this.level_rewards.put(level_count, reward_id);
        return this;
	}
	
	public GuildData addLevelReward(int level_count, Role reward) {
	    this.level_rewards.put(level_count, reward.getIdLong());
        return this;
	}
	
	public GuildData removeLevelRewards(int... level_counts) {
	    for (int i = 0; i < level_counts.length; i++) {
	        this.level_rewards.remove(level_counts[i]);
	    }
        return this;
	}
	
	public GuildData removeLevelRewardsByRewardIds(Long... rewards_ids) {
	    DataTools.removeValuesFromMap(this.level_rewards, rewards_ids);
	    return this;
	}
	
	public GuildData removeLevelRewardsByReward(Role... rewards) {
	    DataTools.removeValuesFromMap(this.level_rewards, Arrays.stream(rewards).map(Role::getIdLong).toArray(Long[]::new));
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
	        this.penalties.put(datas[i].getWarningLimit(), datas[i]);
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
            this.penalties.remove(datas[i].getWarningLimit());
        }
        return this;
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

	public GuildData setModmails(ConcurrentHashMap<TextChannel, ModMailData> modmails) {
	    ConcurrentHashMap<Long, ModMailData> converted_map = new ConcurrentHashMap<>();
        for (Map.Entry<TextChannel, ModMailData> entry : modmails.entrySet()) {
            converted_map.put(entry.getKey().getIdLong(), entry.getValue());
        }
        DataTools.setMap(this.modmails, converted_map);
        return this;
	}
	
	public GuildData addModmails(ModMailData... datas) {
	    for (int i = 0; i < datas.length; i++) {
	        this.modmails.put(datas[i].getGuildChannelId(), datas[i]);
	    }
        return this;
	}
	
	public GuildData removeModmails(long... channel_ids) {
	    for (int i = 0; i < channel_ids.length; i++) {
            this.modmails.remove(channel_ids[i]);
        }
        return this;
	}
	
	public GuildData removeModmails(TextChannel... channels) {
	    for (int i = 0; i < channels.length; i++) {
	        this.modmails.remove(channels[i].getIdLong());
	    }
        return this;
	}
	
	public GuildData removeModmailsByData(ModMailData... datas) {
	    for (int i = 0; i < datas.length; i++) {
            this.modmails.remove(datas[i].getGuildChannelId());
        }
        return this;
	}
	
	public ConcurrentHashMap<Long, ConcurrentHashMap<Long, PollData>> getPollIds() {
	    return this.polls;
	}

	public ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, PollData>> getPolls() {
		ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, PollData>> return_value = new ConcurrentHashMap<>();
		this.polls.forEach((channel_id, map) -> {
		    ConcurrentHashMap<Message, PollData> sub_map = new ConcurrentHashMap<>();
		    TextChannel channel = this.getGuild().getTextChannelById(channel_id);
		    if (channel != null) {
		        map.forEach((message_id, data) -> {
		            Message message = channel.retrieveMessageById(message_id).complete();
		            if (message != null) {
		                sub_map.put(message, data);
		            }
		        });
		        return_value.put(channel, sub_map);
		    }
		});
		return return_value;
	}
	
	public ConcurrentHashMap<Message, PollData> getPollsByChannel(long channel_id) {
	    return this.getPollsByChannel(this.getGuild().getTextChannelById(channel_id));
	}
	
	public ConcurrentHashMap<Message, PollData> getPollsByChannel(TextChannel channel) {
	    ConcurrentHashMap<Message, PollData> return_value = new ConcurrentHashMap<>();
	    ConcurrentHashMap<Long, PollData> sub_map = this.polls.get(channel.getIdLong());
	    if (channel != null && sub_map != null && !sub_map.isEmpty()) {
	        sub_map.forEach((message_id, data) -> {
	            Message message = channel.retrieveMessageById(message_id).complete();
	            if (message != null) {
	                return_value.put(message, data);
	            }
	        });
	    }
	    return return_value;
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
	
	public ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, GiveawayData>> getGiveaways() {
        return this.giveaways;
    }
    
    public ConcurrentHashMap<Message, GiveawayData> getGiveawaysByChannel(TextChannel channel) {
        return this.giveaways.get(channel);
    }
    
    public ReactionRoleData getGiveaway(TextChannel channel, Message message) {
        return this.getReactionRolesByChannel(channel).get(message);
    }

    public GuildData setGiveaways(ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, GiveawayData>> giveaways) {
        DataTools.setMap(this.giveaways, giveaways);
        return this;
    }
    
    public GuildData setGiveawaysByChannel(TextChannel channel, ConcurrentHashMap<Message, GiveawayData> giveaways) {
        DataTools.setMap(this.getGiveawaysByChannel(channel), giveaways);
        return this;
    }
    
    public GuildData addGiveaways(GiveawayData... datas) {
        for (int i = 0; i < datas.length; i++) {
            this.getGiveawaysByChannel(datas[i].getChannel()).put(datas[i].getMessage(), datas[i]);
        }
        return this;
    }
    
    public GuildData removeGiveaway(TextChannel channel, Message message) {
        this.getGiveawaysByChannel(channel).remove(message);
        return this;
    }
    
    public GuildData removeGiveawayByData(ReactionRoleData... datas) {
        for (int i = 0; i < datas.length; i++) {
            this.getGiveawaysByChannel(datas[i].getChannel()).remove(datas[i].getMessage());
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
            public static final String CUSTOM_CATEGORIES = "custom_categories";
        public static final String OTHER = "other";
            public static final String LEVEL_REWARDS = "level_rewards";
            public static final String PENALTIES = "penalties";
            public static final String MODMAILS = "modmails";
            public static final String POLLS = "polls";
            public static final String REACTION_ROLES = "reaction_roles";
            public static final String GIVEAWAYS = "giveaways";
    }
}