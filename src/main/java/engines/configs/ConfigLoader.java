package engines.configs;

import org.json.JSONException;
import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ConfigLoader {
	
	public static ConfigLoader INSTANCE;
	public ConfigManager manager;
	
	public ConfigLoader(String serverIP, String port, String databaseName, String username, String password) {
		manager = new ConfigManager(serverIP, port, databaseName, username, password);
		INSTANCE = this;
	}
	
	//User configs
	public JSONObject getUserConfig(User user) {
		return manager.getUserConfig(user);
	}
	
	public JSONObject getMemberConfig(Guild guild, User user) {
		if (user.isBot()) {
			return null;
		} else {
			return manager.getMemberConfig(guild, user);
		}
	}
	
	public JSONObject getMemberConfig(Guild guild, User user, String key) {
		return this.getMemberConfig(guild, user).getJSONObject(key);
	}
	
	public JSONObject getMemberConfig(Guild guild, User user, String key, String subKey) {
		return this.getMemberConfig(guild, user, key).getJSONObject(subKey);
	}
	
	//Guild configs
	public JSONObject getGuildConfig(Guild guild) {
		return manager.getGuildConfig(guild);
	}
	
	public JSONObject getGuildConfig(Guild guild, String firstKey) {
		return this.getGuildConfig(guild).getJSONObject(firstKey);
	}
	
	public JSONObject getGuildConfig(Guild guild, String key, String subKey) {
		return this.getGuildConfig(guild, key).getJSONObject(subKey);
	}
	
	public JSONObject getThirdGuildLayerConfig(Guild guild, String key, String subKey, String subSubKey) {
		return this.getGuildConfig(guild, key, subKey).getJSONObject(subSubKey);
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
			config = this.getGuildConfig(guild, "reactionroles", channelID);
		} catch (JSONException e) {}
		return config;
	}
 }