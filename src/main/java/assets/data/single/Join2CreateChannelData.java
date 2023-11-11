package assets.data.single;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assets.data.DataTools;
import base.GUI;
import engines.base.Check;
import engines.data.ConfigLoader;
import engines.functions.TrackScheduler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

public class Join2CreateChannelData {

    private static final ConcurrentHashMap<Guild, List<Join2CreateChannelData>> JOIN2CREATE_CHANNEL_LIST = new ConcurrentHashMap<>();
    
    private final long guild_id, channel_id;
    private String name_format = "{member}'s channel";
    private int limit_preset = -1;
    private boolean configurable = false;
    private ConcurrentHashMap<Long, Long> children = new ConcurrentHashMap<>();		//Map(ChannelId, OwnerId)
    
    public static Join2CreateChannelData getParentOf(VoiceChannel children) {
        List<Join2CreateChannelData> join2create_channel_guild_data = JOIN2CREATE_CHANNEL_LIST.get(children.getGuild());
        for (int i = 0; i < join2create_channel_guild_data.size(); i++) {
            Join2CreateChannelData current = join2create_channel_guild_data.get(i);
            if (current.isParentOf(children)) {
                return current;
            }
        }
        return null;
    }
    
    public static void manageJoin(Guild guild, Member member, AudioChannel audioChannel) {
		Join2CreateChannelData join2createChannel = ConfigLoader.get().getGuildData(guild).getJoin2CreateChannelData(audioChannel.getIdLong());
		if (join2createChannel != null) {
			audioChannel.getPermissionContainer().upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VOICE_SPEAK).queue();
			Collection<Permission> defaultPerms = new ArrayList<Permission>();
			defaultPerms.add(Permission.VIEW_CHANNEL);
			defaultPerms.add(Permission.VOICE_SPEAK);
			Collection<Permission> perms = new ArrayList<Permission>();
			if (join2createChannel.isConfigurable()) {
				perms.add(Permission.MANAGE_CHANNEL);
				perms.add(Permission.MANAGE_PERMISSIONS);
				perms.add(Permission.CREATE_INSTANT_INVITE);
				perms.add(Permission.VOICE_MUTE_OTHERS);
			}
			String name = join2createChannel.getNameFormat()
			    .replace("{member}", member.getEffectiveName())
			    .replace("{number}", String.valueOf(join2createChannel.getChildrenIds().size() + 1));
			Category category = audioChannel.getParentCategory();
			VoiceChannel newChannel = null;
			if (category != null) {
				newChannel = guild.createVoiceChannel(name, category).complete();
			} else {
				newChannel = guild.createVoiceChannel(name).complete();
			}
			newChannel.upsertPermissionOverride(guild.getPublicRole()).setAllowed(defaultPerms).complete();
			newChannel.upsertPermissionOverride(member).setAllowed(perms).complete();
			if (join2createChannel.getLimitPreset() > 0) {
				newChannel.getManager().setUserLimit(join2createChannel.getLimitPreset()).queue();
			}
			guild.moveVoiceMember(member, newChannel).queue();
			join2createChannel.addChild(newChannel, member);
//			Update GUI information
			GUI.INSTANCE.increaseJ2CCounter();
		}
	}
	
    public static void manageLeave(Guild guild, Member member, AudioChannel audioChannel) {
		int conmemb = audioChannel.getMembers().size();
		JSONObject createdchannels = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("createdchannels");
		List<String> parentChannels = new ArrayList<>();
		parentChannels.addAll(createdchannels.keySet());
		for (int i = 0; i < parentChannels.size(); i++) {
			try {
				JSONObject parentChannelData = createdchannels.getJSONObject(parentChannels.get(i));
				JSONArray channelData = parentChannelData.getJSONArray(audioChannel.getId());
				JSONObject parentChannelConfig = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("join2createchannels").getJSONObject(parentChannels.get(i));
				long ownerID = channelData.getLong(0);
				if (conmemb == 0) {
					audioChannel.delete().queue();
					parentChannelData.remove(audioChannel.getId());
					int index = channelData.getInt(1);
					//Update index numbers
					List<String> subChannels = new ArrayList<>();
					subChannels.addAll(parentChannelData.keySet());
					for (int a = 0; a < subChannels.size(); a++) {
						JSONArray subChannelData = parentChannelData.getJSONArray(subChannels.get(a));
						VoiceChannel target = guild.getVoiceChannelById(subChannels.get(a));
						int currentIndex = subChannelData.getInt(1);
						Member owner = guild.retrieveMemberById(subChannelData.getLong(0)).complete();
						String namePattern = parentChannelConfig.getString("name");
						String name = namePattern.replace("{member}",owner.getEffectiveName())
								.replace("{number}", String.valueOf(currentIndex));
						String newName = namePattern .replace("{member}", owner.getEffectiveName())
								.replace("{number}", String.valueOf(currentIndex - 1));
						if (subChannelData.getInt(1) > index
								&& namePattern.contains("{number}")
								&& target.getName().equals(name)) {
							target.getManager().setName(newName).queue();
							subChannelData.put(1, currentIndex - 1);
						}
					}
					//Update GUI information
                  GUI.INSTANCE.decreaseJ2CCounter();
				} else {
					if (ownerID == member.getIdLong()) {
						Collection<Permission> perms = new ArrayList<Permission>();
						if (parentChannelConfig.getBoolean("configurable")) {
							perms.add(Permission.MANAGE_CHANNEL);
							perms.add(Permission.MANAGE_PERMISSIONS);
							perms.add(Permission.CREATE_INSTANT_INVITE);
							perms.add(Permission.VOICE_MUTE_OTHERS);
						}
						Member newowner =  audioChannel.getMembers().get(0);
                      channelData.put(0, newowner.getIdLong());
						String name = audioChannel.getName().replace(member.getEffectiveName(), newowner.getEffectiveName());
						audioChannel.getManager().setName(name).queue(sc -> {}, er -> {});
						audioChannel.getPermissionContainer().upsertPermissionOverride(newowner).setAllowed(perms).queue(sc -> {}, er -> {});
						audioChannel.getPermissionContainer().getPermissionOverride(member).delete().queue(sc -> {}, er -> {});
						audioChannel.getPermissionContainer().getManager().putPermissionOverride(newowner, perms, null).removePermissionOverride(guild.getMember(user)).setName(name).queue(sc -> {}, er -> {});
					}
				}
				i = parentChannels.size();
			} catch (JSONException ex) {}
		}
	}
    
	public Join2CreateChannelData(VoiceChannel channel, JSONObject data) {
	    this.channel_id = channel.getIdLong();
	    this.guild_id = channel.getGuild().getIdLong();
	    this.instanciateFromJSON(data);
	    JOIN2CREATE_CHANNEL_LIST.get(channel.getGuild()).add(this);
	}
	
	public Join2CreateChannelData(VoiceChannel channel) {
	    this.channel_id = channel.getIdLong();
        this.guild_id = channel.getGuild().getIdLong();
        JOIN2CREATE_CHANNEL_LIST.get(channel.getGuild()).add(this);
	}

    public Join2CreateChannelData instanciateFromJSON(JSONObject data) {
        this.name_format = data.getString(Key.NAME_FORMAT);
        this.limit_preset = data.getInt(Key.LIMIT_PRESET);
        this.configurable = data.getBoolean(Key.CONFIGURABLE);
        
        JSONObject chilren_object = data.getJSONObject(Key.CHILDREN);
        List<Long> channelIds = DataTools.convertJSONArrayListToLongList(chilren_array);
        DataTools.validateRoleIdList(guild_id, channelIds);
        this.children = validateRoleIdList;
        
        return this;
    }

    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();

        if (Check.isValidChannel(channel)) {
            compiledData.put(Key.NAME_FORMAT, this.name_format);
            compiledData.put(Key.LIMIT_PRESET, this.limit_preset);
            compiledData.put(Key.CONFIGURABLE, configurable);

            JSONArray children_data = new JSONArray();
            children.forEach(channel -> {
                if (Check.isValidChannel(channel)) {
                    children_data.put(channel.getIdLong());
                }
            });
            compiledData.put(Key.CHILDREN, children_data);
        }

        return compiledData;
    }
    
    public List<Long> getChildrenIds() {
        return Collections.list(this.children.keys());
    }
    
    public List<VoiceChannel> getChildren() {
    	List<VoiceChannel> return_value = new ArrayList<>();
    	List<Long> childrenIds = this.getChildrenIds();
    	for (long childrenId : childrenIds) {
    		return_value.add()
    	}
    	return return_value;
    }
    
    public Join2CreateChannelData setChildren(List<VoiceChannel> children) {
        DataTools.setList(this.children, children);
        return this;
    }
    
    public Join2CreateChannelData addChildren(VoiceChannel... childrens) {
        DataTools.addToList(this.children, childrens);
        return this;
    }
    
    public Join2CreateChannelData removeChildren(int... indicies) {
        DataTools.removeIndiciesFromList(this.children, indicies);
        return this;
    }
    
    public Join2CreateChannelData removeChildrenByChannel(VoiceChannel... childrens) {
        DataTools.removeValuesFromList(this.children, childrens);
        return this;
    }
    
    public boolean isParentOf(VoiceChannel children) {
        return this.children.contains(children);
    }
    
    public Guild getGuild() {
        return this.guild;
    }

    public VoiceChannel getVoiceChannel() {
        return this.channel;
    }
    
    public long getVoiceChannelId() {
        return this.channel.getIdLong();
    }
    
    public String getNameFormat() {
        return this.name_format;
    }
    
    public Join2CreateChannelData setNameFormat(String name_format) {
        this.name_format = name_format;
        return this;
    }
    
    public int getLimitPreset() {
        return this.limit_preset;
    }
    
    public Join2CreateChannelData setLimitPreset(Integer limit_preset) {
        if (limit_preset == null || limit_preset <= 0) {
            this.limit_preset = -1;
        } else {
            this.limit_preset = limit_preset;
        }
        return this;
    }
    
    public boolean isConfigurable() {
        return this.configurable;
    }
    
    public Join2CreateChannelData setConfigurable(boolean configurable) {
        this.configurable = configurable;
        return this;
    }
    
    private static abstract class Key {
        public static final String NAME_FORMAT = "name_format";
        public static final String LIMIT_PRESET = "limit_preset";
        public static final String CONFIGURABLE = "configurable";
        public static final String CHILDREN = "children";
    }
}