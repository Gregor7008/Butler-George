package components.base.assets;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.lang.NonNull;

import components.base.ConfigLoader;
import components.base.ConsoleEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ConfigManager {

	private static MongoClient client = MongoClients.create("mongodb://192.168.178.104:17389");
	private static MongoDatabase database = client.getDatabase("butler-george");
	private static MongoCollection<Document> userconfigs = database.getCollection("user");
	private static MongoCollection<Document> guildconfigs = database.getCollection("guild");
	private final ConcurrentHashMap<Long, JSONObject> userConfigCache = new ConcurrentHashMap<Long, JSONObject>();
	private final ConcurrentHashMap<Long, JSONObject> guildConfigCache = new ConcurrentHashMap<Long, JSONObject>();
	public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd.MM.yyyy | O");
	
	//Manage cache	
	public boolean pushCache() {
		try {
			userConfigCache.forEach((id, obj) -> {
				Document searchresult = userconfigs.find(new Document("id", Long.valueOf(id))).first();
				if (searchresult != null) {
					userconfigs.replaceOne(searchresult, Document.parse(obj.toString()));
				} else {
					userconfigs.insertOne(Document.parse(obj.toString()));
				}
			});
			userConfigCache.clear();
			guildConfigCache.forEach((id, obj) -> {
				Document searchresult = guildconfigs.find(new Document("id", Long.valueOf(id))).first();
				if (searchresult != null) {
					guildconfigs.replaceOne(searchresult, Document.parse(obj.toString()));
				} else {
					guildconfigs.insertOne(Document.parse(obj.toString()));
				}
			});
			guildConfigCache.clear();
			return true;
		} catch (Exception e) {
			ConsoleEngine.out.error(this, "Push failed!");
			return false;
		}
	}
	
	//Get Cache
	public ConcurrentHashMap<Long, JSONObject> getUserCache() {
		return userConfigCache;
	}
	
	public ConcurrentHashMap<Long, JSONObject> getGuildCache() {
		return guildConfigCache;
	}
	
	public MongoCollection<Document> getUserCollection() {
		return userconfigs;
	}
	
	public MongoCollection<Document> getGuildCollection() {
		return guildconfigs;
	}
	
	//Get JSONObjects
	@NonNull
	public JSONObject getUserConfig(User user) {
		JSONObject config = null;
		config = userConfigCache.get(user.getIdLong());
		if (config == null) {
			Document doc = userconfigs.find(new Document("id", user.getIdLong())).first();
			userConfigCache.put(user.getIdLong(), new JSONObject(doc.toJson()));
			config = userConfigCache.get(user.getIdLong());
		}
		if (config == null) {
			config = this.createUserConfig(user);
		}
		return config;
	}
	
	@NonNull
	public JSONObject getMemberConfig(Guild guild, User user) {
		JSONObject config = null;
		try {
			config = this.getUserConfig(user).getJSONObject(guild.getId());
		} catch (JSONException e) {
			config = this.createMemberConfig(guild, user);
		}
		return config;
	}
	
	@NonNull
	public JSONObject getGuildConfig(Guild guild) {
		JSONObject config = null;
		config = guildConfigCache.get(guild.getIdLong());
		if (config == null) {
			Document doc = guildconfigs.find(new Document("id", guild.getIdLong())).first();
			guildConfigCache.put(guild.getIdLong(), new JSONObject(doc.toJson()));
			config = guildConfigCache.get(guild.getIdLong());
		}
		if (config == null) {
			config = this.createGuildConfig(guild);
		}
		return config;
	}
	
	//Create JSONObjects
	private JSONObject createUserConfig(User user) {
		JSONObject newConfig = new JSONObject();
		//Simple values
		newConfig.put("id",							user.getIdLong());
		
		userConfigCache.put(user.getIdLong(), newConfig);
		return newConfig;
	}
	
	private JSONObject createMemberConfig(Guild guild, User user) {
		JSONObject userConfig = this.getUserConfig(user);
		JSONObject newConfig = new JSONObject();
		//Simple values
		newConfig.put("guildid",					guild.getIdLong());
		newConfig.put("customchannelcategory",		0L);
		newConfig.put("experience",					0L);
		newConfig.put("language",					"en");
		newConfig.put("lastmail", 					OffsetDateTime.now().minusDays(1).format(dateTimeFormatter));
		newConfig.put("lastsuggestion", 			OffsetDateTime.now().minusDays(1).format(dateTimeFormatter));
		newConfig.put("lastxpgotten", 				OffsetDateTime.now().minusDays(1).format(dateTimeFormatter));
		newConfig.put("level",						0);
		newConfig.put("levelbackground",			0);
		newConfig.put("levelspamcount",				0);
		newConfig.put("muted",						false);
		newConfig.put("penaltycount",				0);
		newConfig.put("tempbanneduntil", 			"");
		newConfig.put("tempbanned",					false);
		newConfig.put("tempmuted",					false);
		newConfig.put("warnings",					new JSONArray());
		
		userConfig.put(guild.getId(), newConfig);
		return newConfig;
	}
	
	private JSONObject createGuildConfig(Guild guild) {
		JSONObject newConfig = new JSONObject();
		//Simple values
		newConfig.put("id",							guild.getIdLong());
		newConfig.put("autoroles",					new JSONArray());
		newConfig.put("botautoroles",				new JSONArray());
		newConfig.put("customchannelcategories",	new JSONObject());
		newConfig.put("customchannelaccessroles",	new JSONArray());
		newConfig.put("customchannelroles",			new JSONArray());
		newConfig.put("forbiddenwords",				new JSONArray());
		newConfig.put("goodbyemsg",					"");
		newConfig.put("createdchannels",			new JSONObject());
		newConfig.put("join2createchannels",		new JSONObject());
		newConfig.put("levelmsgchannel",			0L);
		newConfig.put("levelrewards",				new JSONObject());
		newConfig.put("offlinemsg",					0L);
		newConfig.put("penalties",					new JSONObject());
		newConfig.put("reportchannel",				0L);
		newConfig.put("suggestionchannel",			0L);
		newConfig.put("supportcategory",			0L);
		newConfig.put("supportchat",				0L);
		newConfig.put("supporttalk",				0L);
		newConfig.put("ticketchannels",				new JSONArray());
		newConfig.put("ticketcount",				1);
		newConfig.put("welcomemsg",					"");
		//Deep-nested values (2 layers or more)
		newConfig.put("modmails",					new JSONObject());
		newConfig.put("polls",						new JSONObject());
		newConfig.put("reactionroles",				new JSONObject());
		
		guildConfigCache.put(guild.getIdLong(), newConfig);
		return newConfig;
	}
	
	public JSONObject createPollConfig(Guild guild, String channelID, String messageID) {
		if (ConfigLoader.run.getPollConfig(guild, channelID, messageID) != null) {
			return ConfigLoader.run.getPollConfig(guild, channelID, messageID);
		}
		JSONObject guildConfig = this.getGuildConfig(guild);
		try {
			guildConfig.getJSONObject("polls").getJSONObject(channelID);
		} catch (JSONException e) {
			guildConfig.getJSONObject("polls").put(channelID, new JSONObject());
		}
		JSONObject newConfig = new JSONObject();
		//Simple values
		newConfig.put("anonymous",					false);
		newConfig.put("answercount",				0);
		newConfig.put("answers",					new JSONObject());
		newConfig.put("channel",					Long.valueOf(channelID));
		newConfig.put("creationdate",				OffsetDateTime.now().format(dateTimeFormatter));
		newConfig.put("daysopen",					0);
		newConfig.put("description",				"");
		newConfig.put("footer",						"");
		newConfig.put("message",					Long.valueOf(messageID));
		newConfig.put("owner",						0L);
		newConfig.put("possibleanswers",			new JSONArray());
		newConfig.put("thumbnailurl", 				"");
		newConfig.put("title",						"");
		newConfig.put("guild",						guild.getIdLong());
		
		guildConfig.getJSONObject("polls").getJSONObject(channelID).put(messageID, newConfig);
		return newConfig;
	}
	
	public JSONObject createReactionroleConfig(Guild guild, String channelID, String messageID) {
		if (ConfigLoader.run.getReactionMessageConfig(guild, channelID, messageID) != null) {
			return ConfigLoader.run.getReactionMessageConfig(guild, channelID, messageID);
		}
		JSONObject guildConfig = this.getGuildConfig(guild);
		try {
			guildConfig.getJSONObject("reactionroles").getJSONObject(channelID);
		} catch (JSONException e) {
			guildConfig.getJSONObject("reactionroles").put(channelID, new JSONObject());
		}
		JSONObject newConfig = new JSONObject();
		guildConfig.getJSONObject("reactionroles").getJSONObject(channelID).put(messageID, newConfig);
		return newConfig;
	}
}