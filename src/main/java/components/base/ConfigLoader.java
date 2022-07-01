package components.base;

import org.json.JSONException;
import org.json.JSONObject;

import base.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class ConfigLoader {
	
	//User configs
	public static JSONObject getUserConfig(User user) {
		return ConfigManager.getUserConfig(user);
	}
	
	public static JSONObject getMemberConfig(Guild guild, User user) {
		if (user.isBot()) {
			return null;
		} else {
			return ConfigManager.getMemberConfig(guild, user);
		}
	}
	
	public static JSONObject getFirstMemberLayerConfig(Guild guild, User user, String key) {
		return ConfigLoader.getMemberConfig(guild, user).getJSONObject(key);
	}
	
	public static JSONObject getSecondMemberLayerConfig(Guild guild, User user, String key, String subKey) {
		return ConfigLoader.getFirstMemberLayerConfig(guild, user, key).getJSONObject(subKey);
	}
	
	//Guild configs
	public static JSONObject getGuildConfig(Guild guild) {
		return ConfigManager.getGuildConfig(guild);
	}
	
	public static JSONObject getFirstGuildLayerConfig(Guild guild, String firstKey) {
		return ConfigLoader.getGuildConfig(guild).getJSONObject(firstKey);
	}
	
	public static JSONObject getSecondGuildLayerConfig(Guild guild, String key, String subKey) {
		return ConfigLoader.getFirstGuildLayerConfig(guild, key).getJSONObject(subKey);
	}
	
	public static JSONObject getThirdGuildLayerConfig(Guild guild, String key, String subKey, String subSubKey) {
		return ConfigLoader.getSecondGuildLayerConfig(guild, key, subKey).getJSONObject(subSubKey);
	}
	
	//Poll configs
	public static JSONObject createPollConfig(Guild guild, String channelID, String messageID) {
		return ConfigManager.createPollConfig(guild, channelID, messageID);
	}
	
	public static JSONObject getPollConfig(Guild guild, String channelID, String messageID) {
		JSONObject config = null;
		try {
			config = ConfigLoader.getThirdGuildLayerConfig(guild, "polls", channelID, messageID);
		} catch (JSONException e) {}
		return config;
	}
	
	public static JSONObject getPollAnswers(Guild guild, String channelID, String messageID) {
		return ConfigLoader.getPollConfig(guild, channelID, messageID).getJSONObject("answers");
	}
	
	//Reactionrole configs
	public static JSONObject createReactionMessageConfig(Guild guild, String channelID, String messageID) {
		return ConfigManager.createReactionroleConfig(guild, channelID, messageID);
	}
	
	public static JSONObject getReactionMessageConfig(Guild guild, String channelID, String messageID) {
		JSONObject config = null;
		try {
			config = ConfigLoader.getThirdGuildLayerConfig(guild, "reactionroles", channelID, messageID);
		} catch (JSONException e) {}
		return config;
	}
	
	public static JSONObject getReactionChannelConfig(Guild guild, String channelID) {
		JSONObject config = null;
		try {
			config = ConfigLoader.getSecondGuildLayerConfig(guild, "reactionroles", channelID);
		} catch (JSONException e) {}
		return config;
	}
	
	//Modmail configs
	public static TextChannel getModMailOfUser(String userID) {
		try {
			return Bot.run.jda.getGuildById(Bot.homeID).getTextChannelById(ConfigLoader.getModMailConfig(Bot.run.jda.getGuildById(Bot.homeID)).getLong(userID));
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static User getModMailOfChannel(String channelID) {
		JSONObject modmailConfig = ConfigLoader.getModMailConfig(Bot.run.jda.getGuildById(Bot.homeID));
		String[] keys = (String[]) modmailConfig.keySet().toArray();
		User returnValue = null;
		for (int i = 0; i < keys.length; i++) {
			if (modmailConfig.getLong(keys[i]) == Long.valueOf(channelID)) {
				returnValue = Bot.run.jda.getUserById(Long.valueOf(keys[i]));
				i = keys.length;
			}
		}
		return returnValue;
	}
	
	public static void setModMailConfig(String channelID, String userID) {
		ConfigLoader.getModMailConfig(Bot.run.jda.getGuildById(Bot.homeID)).put(userID, channelID);
	}
	
	private static JSONObject getModMailConfig(Guild guild) {
		return ConfigLoader.getFirstGuildLayerConfig(guild, "modmails");
	}
 }