package components.base;

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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public abstract class ConfigManager {

	private static MongoClient client = null;
	private static MongoDatabase database = null;
	private static MongoCollection<Document> userconfigs = null;
	private static MongoCollection<Document> guildconfigs = null;
	private static ConcurrentHashMap<Long, JSONObject> userConfigCache = new ConcurrentHashMap<Long, JSONObject>();
	private static ConcurrentHashMap<Long, JSONObject> guildConfigCache = new ConcurrentHashMap<Long, JSONObject>();
	public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd.MM.yyyy | O");
	
	//Manage cache	
	public static boolean pushCache() {
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
			ConsoleEngine.out.error(ConfigManager.class, "Push failed!");
			return false;
		}
	}
	
	public static boolean setDatabaseConnection(String clientIP, String databaseName) {
		if (clientIP.equals("Enter database IP") || databaseName.equals("Enter database name")) {
			return false;
		}
		try {
			client = MongoClients.create(clientIP);
			client.listDatabases();
			database = client.getDatabase(databaseName);
		} catch (Exception e) {
			client = null;
			database = null;
			userconfigs = null;
			guildconfigs = null;
			return false;
		}
		try {
			userconfigs = database.getCollection("user");
		} catch (IllegalArgumentException e) {
			 database.createCollection("user");
			 userconfigs = database.getCollection("user");
		}
		try {
			guildconfigs = database.getCollection("guild");
		} catch (IllegalArgumentException e) {
			 database.createCollection("guild");
			 guildconfigs = database.getCollection("guild");
		}
		return true;
	}
	
	//Get Cache
	public static ConcurrentHashMap<Long, JSONObject> getUserCache() {
		return userConfigCache;
	}
	
	public static ConcurrentHashMap<Long, JSONObject> getGuildCache() {
		return guildConfigCache;
	}
	
	public static MongoCollection<Document> getUserCollection() {
		return userconfigs;
	}
	
	public static MongoCollection<Document> getGuildCollection() {
		return guildconfigs;
	}
	
	//Get JSONObjects
	@NonNull
	public static JSONObject getUserConfig(User user) {
		JSONObject config = null;
		config = userConfigCache.get(user.getIdLong());
		if (config == null) {
			Document doc = userconfigs.find(new Document("id", user.getIdLong())).first();
			if (doc != null) {
				userConfigCache.put(user.getIdLong(), new JSONObject(doc.toJson()));
				config = userConfigCache.get(user.getIdLong());
			}
		}
		if (config == null) {
			config = ConfigManager.createUserConfig(user);
		}
		return config;
	}
	
	@NonNull
	public static JSONObject getMemberConfig(Guild guild, User user) {
		JSONObject config = null;
		try {
			config = ConfigManager.getUserConfig(user).getJSONObject(guild.getId());
		} catch (JSONException e) {
			config = ConfigManager.createMemberConfig(guild, user);
		}
		return config;
	}
	
	@NonNull
	public static JSONObject getGuildConfig(Guild guild) {
		JSONObject config = null;
		config = guildConfigCache.get(guild.getIdLong());
		if (config == null) {
			Document doc = guildconfigs.find(new Document("id", guild.getIdLong())).first();
			if (doc != null) {
				guildConfigCache.put(guild.getIdLong(), new JSONObject(doc.toJson()));
				config = guildConfigCache.get(guild.getIdLong());
			}
		}
		if (config == null) {
			config = ConfigManager.createGuildConfig(guild);
		}
		return config;
	}
	
	//Create JSONObjects
	private static JSONObject createUserConfig(User user) {
		JSONObject newConfig = new JSONObject();
		//Simple values
		newConfig.put("id",							user.getIdLong());
		
		userConfigCache.put(user.getIdLong(), newConfig);
		return newConfig;
	}
	
	private static JSONObject createMemberConfig(Guild guild, User user) {
		JSONObject userConfig = ConfigManager.getUserConfig(user);
		JSONObject newConfig = new JSONObject();
		//Simple values
		newConfig.put("guildid",					guild.getIdLong());
		newConfig.put("customchannelcategory",		0L);
		newConfig.put("experience",					0);
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
	
	private static JSONObject createGuildConfig(Guild guild) {
		JSONObject newConfig = new JSONObject();
		//Simple values
		newConfig.put("id",							guild.getIdLong());
		newConfig.put("adminroles",					new JSONArray());
		newConfig.put("userautoroles",				new JSONArray());
		newConfig.put("botautoroles",				new JSONArray());
		newConfig.put("communityinbox",				0L);
		newConfig.put("customchannelpolicingroles",	new JSONArray());
		newConfig.put("customchannelcategories",	new JSONObject());
		newConfig.put("createdchannels",			new JSONObject());
		newConfig.put("goodbyemsg",					new JSONArray());
		newConfig.put("join2createchannels",		new JSONObject());
		newConfig.put("levelrewards",				new JSONObject());
		newConfig.put("moderationroles",			new JSONArray());
		newConfig.put("moderationinbox",			0L);
		newConfig.put("offlinemsg",					new JSONArray());
		newConfig.put("penalties",					new JSONObject());
		newConfig.put("suggestioninbox",			0L);
		newConfig.put("supportroles",				new JSONArray());
		newConfig.put("supporttalk",				0L);
		newConfig.put("ticketchannels",				new JSONArray());
		newConfig.put("ticketcount",				1);
		newConfig.put("welcomemsg",					new JSONArray());
		//Deep-nested values (2 layers or more)
		newConfig.put("modmails",					new JSONObject());
		newConfig.put("polls",						new JSONObject());
		newConfig.put("reactionroles",				new JSONObject());
		
		guildConfigCache.put(guild.getIdLong(), newConfig);
		return newConfig;
	}
	
	public static JSONObject createPollConfig(Guild guild, String channelID, String messageID) {
		if (ConfigLoader.getPollConfig(guild, channelID, messageID) != null) {
			return ConfigLoader.getPollConfig(guild, channelID, messageID);
		}
		JSONObject guildConfig = ConfigManager.getGuildConfig(guild);
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
	
	public static JSONObject createReactionroleConfig(Guild guild, String channelID, String messageID) {
		if (ConfigLoader.getReactionMessageConfig(guild, channelID, messageID) != null) {
			return ConfigLoader.getReactionMessageConfig(guild, channelID, messageID);
		}
		JSONObject guildConfig = ConfigManager.getGuildConfig(guild);
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