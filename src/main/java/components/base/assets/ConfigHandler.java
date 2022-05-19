package components.base.assets;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ConfigHandler {

	private static MongoClient client = MongoClients.create("mongodb://192.168.178.104:17389");
	private static MongoDatabase database = client.getDatabase("butler-george");
	private static MongoCollection<Document> userconfigs = database.getCollection("user");
	private static MongoCollection<Document> guildconfigs = database.getCollection("guild");
	private final ConcurrentHashMap<Long, JSONObject> userConfigCache = new ConcurrentHashMap<Long, JSONObject>();
	private final ConcurrentHashMap<Long, JSONObject> guildConfigCache = new ConcurrentHashMap<Long, JSONObject>();
	public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd.MM.yyyy | O");
	
	public ConfigHandler() {
		this.pullCache();
	}
	
	public void pullCache() {
		userConfigCache.clear();
		guildConfigCache.clear();
		userconfigs.find().forEach(doc -> userConfigCache.put(doc.getLong("id"), new JSONObject(doc.toJson())));
		guildconfigs.find().forEach(doc -> guildConfigCache.put(doc.getLong("id"), new JSONObject(doc.toJson())));
	}
	
	public boolean pushCache() {
		try {
			userConfigCache.forEach((id, obj) -> {
				Document searchresult = userconfigs.find(new Document("id", Long.valueOf(id))).first();
				if (searchresult != null) {
					userconfigs.updateOne(searchresult, Document.parse(obj.toString()));
				} else {
					userconfigs.insertOne(Document.parse(obj.toString()));
				}
			});
			guildConfigCache.forEach((id, obj) -> {
				Document searchresult = guildconfigs.find(new Document("id", Long.valueOf(id))).first();
				if (searchresult != null) {
					userconfigs.updateOne(searchresult, Document.parse(obj.toString()));
				} else {
					userconfigs.insertOne(Document.parse(obj.toString()));
				}
			});
			return true;
		} catch (Exception e) {return false;}
	}
	
	public JSONObject getUserConfig(User user) {
		JSONObject config = userConfigCache.get(user.getIdLong());
		if (config == null) {
			config = this.createUserConfig(user);
		}
		return config;
	}
	
	public JSONObject getGuildConfig(Guild guild) {
		JSONObject config = guildConfigCache.get(guild.getIdLong());
		if (config == null) {
			config = this.createGuildConfig(guild);
		}
		return config;
	}
	
	private JSONObject createUserConfig(User user) {
		JSONObject newConfig = new JSONObject();
		//Simple values
		newConfig.put("id",							user.getIdLong());
		newConfig.put("customchannelcategory",		Long.valueOf(0));
		newConfig.put("experience",					Integer.valueOf(0));
		newConfig.put("language",					"en");
		newConfig.put("lastmail", 					OffsetDateTime.now().minusDays(1).format(dateTimeFormatter));
		newConfig.put("lastsuggestion", 			OffsetDateTime.now().minusDays(1).format(dateTimeFormatter));
		newConfig.put("lastxpgotten", 				OffsetDateTime.now().minusDays(1).format(dateTimeFormatter));
		newConfig.put("level",						Integer.valueOf(0));
		newConfig.put("levelbackground",			Integer.valueOf(0));
		newConfig.put("levelspamcount",				Integer.valueOf(0));
		newConfig.put("muted",						false);
		newConfig.put("penaltycount",				Integer.valueOf(0));
		newConfig.put("tempbanneduntil", 			"");
		newConfig.put("tempbanned",					false);
		newConfig.put("tempmuted",					false);
		newConfig.put("warnings",					new JSONArray());
		
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
		newConfig.put("forbiddenwords",				"");
		newConfig.put("goodbyemsg",					"");
		newConfig.put("createdchannels",			new JSONObject());
		newConfig.put("join2createchannels",		new JSONObject());
		newConfig.put("levelmsgchannel",			Long.valueOf(0));
		newConfig.put("levelrewars",				new JSONObject());
		newConfig.put("offlinemsg",					new JSONArray());
		newConfig.put("penalties",					new JSONObject());
		newConfig.put("reportchannel",				Long.valueOf(0));
		newConfig.put("suggestionchannel",			Long.valueOf(0));
		newConfig.put("supportcategory",			Long.valueOf(0));
		newConfig.put("supportchat",				Long.valueOf(0));
		newConfig.put("ticketcount",				Integer.valueOf(0));
		newConfig.put("welcomemsg",					"");
		//Deep-Nested values
		newConfig.put("modmails",					new JSONObject());
		newConfig.put("polls",						new JSONObject());
		newConfig.put("reactionroles",				new JSONObject());
		
		return newConfig;
	}
	
	public JSONObject
}