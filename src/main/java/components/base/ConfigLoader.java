package components.base;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import components.base.assets.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ConfigLoader {
	
	public static ConfigLoader run;
	public static ConfigManager manager = new ConfigManager();
	
	public ConfigLoader() {
		run = this;
	}
	
	//User configs
	public JSONObject getUserConfig(Guild guild, User user) {
		return manager.getMemberConfig(guild, user);
	}
	
	public JSONObject getFirstUserLayerConfig(Guild guild, User user, String key) {
		return this.getUserConfig(guild, user).getJSONObject(key);
	}
	
	public JSONObject getSecondUserLayerConfig(Guild guild, User user, String key, String subKey) {
		return this.getFirstUserLayerConfig(guild, user, key).getJSONObject(subKey);
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
	public JSONObject createReactionroleConfig(Guild guild, String channelID, String messageID) {
		return manager.createReactionroleConfig(guild, channelID, messageID);
	}
	
	public JSONObject getReactionroleConfig(Guild guild, String channelID, String messageID) {
		JSONObject config = null;
		try {
			config = this.getThirdGuildLayerConfig(guild, "reactionroles", channelID, messageID);
		} catch (JSONException e) {}
		return config;
	}
	
	//Modmail configs
	public JSONObject getModmailConfig(Guild guild) {
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
	
	public void removeValueFromArray(JSONArray current, Object value) {
		int index = -1;
		for (int i = 0; i < current.length(); i++) {
			if (current.get(i).equals(value)) {
				index = i;
			}
		}
		if (index >= 0) {
			current.remove(index);
		}
	}
 }