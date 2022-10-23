package engines.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoQueryException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.lang.NonNull;

import assets.logging.Logger;
import base.Bot;
import base.GUI;
import engines.logging.ConsoleEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ConfigManager {

	public static DateTimeFormatter CONFIG_TIME_SAVE_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss - dd.MM.yyyy | O");
	
	private static Logger LOG = ConsoleEngine.getLogger(ConfigManager.class);
	
	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<Document> userConfigs;
	private MongoCollection<Document> guildConfigs;
	private ConcurrentHashMap<Long, JSONObject> userConfigCache = new ConcurrentHashMap<Long, JSONObject>();
	private ConcurrentHashMap<Long, JSONObject> guildConfigCache = new ConcurrentHashMap<Long, JSONObject>();
	
	public ConfigManager(String serverIP, String serverPort, String databaseName, String username, String password) {
		if (serverIP.equals(GUI.INSTANCE.databaseIP.getName())) {
			throw new IllegalArgumentException("No database IP provided!");
		}
		if (databaseName.equals(GUI.INSTANCE.databaseName.getName())) {
			throw new IllegalArgumentException("No database name provided!");
		}
		StringBuilder uriBuilder = new StringBuilder();
		uriBuilder.append("mongodb://");
		boolean customUser = false;
		if (!username.equals(GUI.INSTANCE.username.getName())) {
			uriBuilder.append(this.encodeToURL(username) + ":" + this.encodeToURL(password) + "@");
			customUser = true;
		}
		uriBuilder.append(serverIP.replace("mongodb://", ""));
		if (serverPort.equals(GUI.INSTANCE.databasePort.getName())) {
			uriBuilder.append(":27017");
		} else {
			uriBuilder.append(":" + serverPort);
		}
		if (customUser) {
			uriBuilder.append("/?authMechanism=SCRAM-SHA-256");
		}
		MongoClientSettings setting = MongoClientSettings.builder()
				.applyToSocketSettings(builder -> {
					builder.connectTimeout(3, TimeUnit.SECONDS);
					builder.readTimeout(500, TimeUnit.MILLISECONDS);
				})
				.applyToClusterSettings( builder -> builder.serverSelectionTimeout(3, TimeUnit.SECONDS))
				.applyConnectionString(new ConnectionString(uriBuilder.toString()))
				.build();
		try {
			client = MongoClients.create(setting);
			database = client.getDatabase(databaseName);
			try {
				userConfigs = database.getCollection("user");
			} catch (IllegalArgumentException e) {
				database.createCollection("user");
				userConfigs = database.getCollection("user");
			}
			try {
				guildConfigs = database.getCollection("guild");
			} catch (IllegalArgumentException e) {
				database.createCollection("guild");
				guildConfigs = database.getCollection("guild");
			}
			try {
				userConfigs.find(new Document("id", 475974084937646080L)).first();
			} catch (MongoSecurityException | MongoQueryException e) {
				client.close();
				throw new IllegalArgumentException("Authentication data invalid!");
			}
		} catch (MongoTimeoutException e) {
			throw new IllegalArgumentException("Connection to server failed, please check online status of server!");
		}
		Bot.INSTANCE.getTimer().schedule(new TimerTask() {
			private int executions = 0;
			@Override
			public void run() {
				if (executions > 1) {
					pushCache();
				}
				executions++;
			}
		}, TimeUnit.MINUTES.toMillis(5));
	}
	
	private String encodeToURL(String input) {
		try {
			return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public void log() {
		LOG.info("---------| User-Cache |---------");
		userConfigCache.forEach((id, obj) -> {
			LOG.info("-> " + Bot.INSTANCE.jda.retrieveUserById(id).complete().getName());
		});
		if (userConfigCache.isEmpty()) {
			LOG.info("EMPTY");
		}
		LOG.info("---------| Guild-Cache |--------");
		guildConfigCache.forEach((id, obj) -> {
			LOG.info("-> " + Bot.INSTANCE.jda.getGuildById(id).getName());
		});
		if (guildConfigCache.isEmpty()) {
			LOG.info("EMPTY");
		}
	}
	
	//Manage cache	
	public boolean pushCache() {
	    if (GUI.INSTANCE.getErrorBoolean()) {
	        return false;
	    }
	    try {
			userConfigCache.forEach((id, obj) -> {
				Document searchresult = userConfigs.find(new Document("id", Long.valueOf(id))).first();
				if (searchresult != null) {
					userConfigs.replaceOne(searchresult, Document.parse(obj.toString()));
				} else {
					userConfigs.insertOne(Document.parse(obj.toString()));
				}
			});
			userConfigCache.clear();
			guildConfigCache.forEach((id, obj) -> {
				Document searchresult = guildConfigs.find(new Document("id", Long.valueOf(id))).first();
				if (searchresult != null) {
					guildConfigs.replaceOne(searchresult, Document.parse(obj.toString()));
				} else {
					guildConfigs.insertOne(Document.parse(obj.toString()));
				}
			});
			guildConfigCache.clear();
			GUI.INSTANCE.increasePushCounter();
			return true;
		} catch (Exception e) {
			LOG.error("Push failed!");
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
	
	//Get JSONObjects
	@NonNull
	public JSONObject getUserConfig(User user) {
		JSONObject config = null;
		config = userConfigCache.get(user.getIdLong());
		if (config == null) {
			Document doc = userConfigs.find(new Document("id", user.getIdLong())).first();
			if (doc != null) {
				userConfigCache.put(user.getIdLong(), new JSONObject(doc.toJson()));
				config = userConfigCache.get(user.getIdLong());
			}
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
			ConfigVerifier.RUN.userCheck(guild, user);
		}
		return config;
	}
	
	@NonNull
	public JSONObject getGuildConfig(Guild guild) {
		JSONObject config = null;
		config = guildConfigCache.get(guild.getIdLong());
		if (config == null) {
			Document doc = guildConfigs.find(new Document("id", guild.getIdLong())).first();
			if (doc != null) {
				guildConfigCache.put(guild.getIdLong(), new JSONObject(doc.toJson()));
				config = guildConfigCache.get(guild.getIdLong());
				ConfigVerifier.RUN.guildCheck(guild);
			}
		}
		if (config == null) {
			config = this.createGuildConfig(guild);
		}
		return config;
	}
	
	private JSONObject createUserConfig(User user) {
		JSONObject newConfig = new JSONObject();
		//Simple values
		newConfig.put("id",							user.getIdLong());
		newConfig.put("selected_ticket",			new JSONArray());
		
		userConfigCache.put(user.getIdLong(), newConfig);
		return newConfig;
	}
	
	private JSONObject createMemberConfig(Guild guild, User user) {
		JSONObject userConfig = this.getUserConfig(user);
		JSONObject newConfig = new JSONObject();
		//Simple values
		newConfig.put("guildid",					guild.getIdLong());
		newConfig.put("customchannelcategory",		0L);
		newConfig.put("experience",					0);
		newConfig.put("language",					"en");
		newConfig.put("lastmail", 					OffsetDateTime.now().minusDays(1).format(CONFIG_TIME_SAVE_FORMAT));
		newConfig.put("lastsuggestion", 			OffsetDateTime.now().minusDays(1).format(CONFIG_TIME_SAVE_FORMAT));
		newConfig.put("lastxpgotten", 				OffsetDateTime.now().minusDays(1).format(CONFIG_TIME_SAVE_FORMAT));
		newConfig.put("level",						0);
		newConfig.put("levelbackground",			0);
		newConfig.put("levelspamcount",				0);
		newConfig.put("modmails",					new JSONObject());
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
		newConfig.put("adminroles",					new JSONArray());
		newConfig.put("boostmsg",					new JSONArray());
		newConfig.put("userautoroles",				new JSONArray());
		newConfig.put("botautoroles",				new JSONArray());
		newConfig.put("communityinbox",				0L);
		newConfig.put("customchannelpolicingroles",	new JSONArray());
		newConfig.put("customchannelcategories",	new JSONObject());
		newConfig.put("createdchannels",			new JSONObject());
		newConfig.put("goodbyemsg",					new JSONArray());
		newConfig.put("join2createchannels",		new JSONObject());
		newConfig.put("levelrewards",				new JSONObject());
		newConfig.put("levelmsg",					"");
		newConfig.put("moderationroles",			new JSONArray());
		newConfig.put("moderationinbox",			0L);
		newConfig.put("modmailcategory",			0L);
		newConfig.put("modmails",					new JSONObject());
		newConfig.put("offlinemsg",					new JSONArray());
		newConfig.put("penalties",					new JSONObject());
		newConfig.put("suggestioninbox",			0L);
		newConfig.put("supportroles",				new JSONArray());
		newConfig.put("supporttalk",				0L);
		newConfig.put("welcomemsg",					new JSONArray());
		//Deep-nested values (2 layers or more)
		newConfig.put("polls",						new JSONObject());
		newConfig.put("reactionroles",				new JSONObject());
		
		guildConfigCache.put(guild.getIdLong(), newConfig);
		return newConfig;
	}
	
	public JSONObject createPollConfig(Guild guild, String channelID, String messageID) {
		if (ConfigLoader.INSTANCE.getPollConfig(guild, channelID, messageID) != null) {
			return ConfigLoader.INSTANCE.getPollConfig(guild, channelID, messageID);
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
		newConfig.put("creationdate",				OffsetDateTime.now().format(CONFIG_TIME_SAVE_FORMAT));
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
		if (ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channelID, messageID) != null) {
			return ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channelID, messageID);
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