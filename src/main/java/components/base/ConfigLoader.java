package components.base;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base.Bot;
import components.base.assets.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ConfigLoader {
	
	public static ConfigLoader run;
	public static ConfigManager manager;
	
	public ConfigLoader() {
		run = this;
		manager = new ConfigManager();
	}
	
	//User configs
	public JSONObject getUserConfig(User user) {
		return manager.getUserConfig(user);
	}
	
	public JSONObject getMemberConfig(Guild guild, User user) {
		return manager.getMemberConfig(guild, user);
	}
	
	public JSONObject getFirstMemberLayerConfig(Guild guild, User user, String key) {
		return this.getMemberConfig(guild, user).getJSONObject(key);
	}
	
	public JSONObject getSecondMemberLayerConfig(Guild guild, User user, String key, String subKey) {
		return this.getFirstMemberLayerConfig(guild, user, key).getJSONObject(subKey);
	}
	
	//Guild configs
	public JSONObject getGuildConfig(Guild guild) {
		return manager.getGuildConfig(guild);
	}
	
	public JSONObject getFirstGuildLayerConfig(Guild guild, String firstKey) {
		return this.getGuildConfig(guild).getJSONObject(firstKey);
	}
	
	public JSONObject getSecondGuildLayerConfig(Guild guild, String key, String subKey) {
		return this.getFirstGuildLayerConfig(guild, key).getJSONObject(subKey);
	}
	
	public JSONObject getThirdGuildLayerConfig(Guild guild, String key, String subKey, String subSubKey) {
		return this.getSecondGuildLayerConfig(guild, key, subKey).getJSONObject(subSubKey);
	}
	
	//Poll configs
	public JSONObject createPollConfig(Guild guild, String channelID, String messageID) {
		return manager.createPollConfig(guild, channelID, messageID);
	}
	
	public JSONObject getPollConfig(Guild guild, String channelID, String messageID) {
		JSONObject config = null;
		try {
			config = this.getThirdGuildLayerConfig(guild, "polls", channelID, messageID);
		} catch (JSONException e) {}
		return config;
	}
	
	public JSONObject getPollAnswers(Guild guild, String channelID, String messageID) {
		return this.getPollConfig(guild, channelID, messageID).getJSONObject("answers");
	}
	
	//Reactionrole configs
	public JSONObject createReactionMessageConfig(Guild guild, String channelID, String messageID) {
		return manager.createReactionroleConfig(guild, channelID, messageID);
	}
	
	public JSONObject getReactionMessageConfig(Guild guild, String channelID, String messageID) {
		JSONObject config = null;
		try {
			config = this.getThirdGuildLayerConfig(guild, "reactionroles", channelID, messageID);
		} catch (JSONException e) {}
		return config;
	}
	
	public JSONObject getReactionChannelConfig(Guild guild, String channelID) {
		JSONObject config = null;
		try {
			config = this.getSecondGuildLayerConfig(guild, "reactionroles", channelID);
		} catch (JSONException e) {}
		return config;
	}
	
	//Modmail configs
	public Long getModMailOfUser(String userID) {
		try {
			return this.getModMailConfig(Bot.run.jda.getGuildById(Bot.homeID)).getLong(userID);
		} catch (JSONException e) {
			return null;
		}
	}
	
	public Long getModMailOfChannel(String channelID) {
		JSONObject modmailConfig = this.getModMailConfig(Bot.run.jda.getGuildById(Bot.homeID));
		String[] keys = (String[]) modmailConfig.keySet().toArray();
		Long returnValue = null;
		for (int i = 0; i < keys.length; i++) {
			if (modmailConfig.getLong(keys[i]) == Long.valueOf(channelID)) {
				returnValue = Long.valueOf(keys[i]);
				i = keys.length;
			}
		}
		return returnValue;
	}
	
	public void setModMailConfig(String channelID, String userID) {
		this.getModMailConfig(Bot.run.jda.getGuildById(Bot.homeID)).put(userID, channelID);
	}
	
	private JSONObject getModMailConfig(Guild guild) {
		return this.getFirstGuildLayerConfig(guild, "modmails");
	}
	
	//Tool methods
	public void clearValue(JSONObject jObject, String key) {
		try {
			jObject.getString(key);
			jObject.put(key, "");
			return;
		} catch (JSONException e) {}
		try {
			jObject.getJSONArray(key);
			jObject.put(key, new JSONArray());
			return;
		} catch (JSONException e) {}
		try {
			jObject.getInt(key);
			jObject.put(key, Integer.valueOf(0));
			return;
		} catch (JSONException e) {}
		try {
			jObject.getLong(key);
			jObject.put(key, Long.valueOf(0));
			return;
		} catch (JSONException e) {}
	}
	
	public boolean removeValueFromArray(JSONArray current, Object value) {
		for (int i = 0; i < current.length(); i++) {
			if (current.get(i).equals(value)) {
				current.remove(i);
				i = current.length();
				return true;
			}
		}
		return false;
	}
 }