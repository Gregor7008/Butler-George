package components.base;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class Configloader {
	
	public static Configloader INSTANCE;
	
	public Configloader() {
		INSTANCE = this;
	}
//	Get values
	public String getUserConfig(Guild guild, User user, String key) {
		return this.findorCreateUserConfig(user, guild).getString(key);
	}
	
	public JSONArray getUserConfigArray(Guild guild, User user, String key) {
		return this.findorCreateUserConfig(user, guild).getJSONArray(key);
	}
	
	public int getUserConfigInt(Guild guild, User user, String key) {
		return this.findorCreateUserConfig(user, guild).getInt(key);
	}
	
	public String getGuildConfig(Guild guild, String key) {
		return this.findorCreateGuildConfig(guild).getString(key);
	}
	
	public JSONArray getGuildConfigArray(Guild guild, String key) {
		return this.findorCreateGuildConfig(guild).getJSONArray(key);
	}
	
	public int getGuildConfigInt(Guild guild, String key) {
		return this.findorCreateGuildConfig(guild).getInt(key);
	}
	
	
//	Change file in any way
	public void setUserConfig(Guild guild, User user, String key, String value) {
		this.findorCreateUserConfig(user, guild).put(key, value);
	}
	
	public void addUserConfig(Guild guild, User user, String key, String value) {
		this.findorCreateUserConfig(user, guild).getJSONArray(key).put(value);
		if (this.getUserConfig(guild, user, key).equals("")) {
			this.setUserConfig(guild, user, key, value);
		} else {
			this.setUserConfig(guild, user, key, this.getUserConfig(guild, user, key) + ";" + value);
		}
	}
	
	public void removeUserConfig(Guild guild, User user, String key, String value) {
		this.removeValueFromArray(this.findorCreateUserConfig(user, guild).getJSONArray(key), value);
		if (this.getUserConfig(guild, user, key).equals(value)) {
			this.setUserConfig(guild, user, key, "");
		} else {
			this.setUserConfig(guild, user, key, this.getUserConfig(guild, user, key).replace(";" + value, ""));
		}
	}
	
	public void setGuildConfig(Guild guild, String key, String value) {
		this.findorCreateGuildConfig(guild).put(key, value);
	}
	
	public void addGuildConfig(Guild guild, String key, String value) {
		this.findorCreateGuildConfig(guild).getJSONArray(key).put(value);
	}
	
	public void removeGuildConfig(Guild guild, String key, String value) {
		this.removeValueFromArray(this.findorCreateGuildConfig(guild).getJSONArray(key), value);
	}
	
	public void setReactionroleConfig(Guild guild, String channelID, String msgid, String key, String value) {
		this.findReactionroleConfig(guild, channelID, msgid).put(key, value);
	}
	
	public void deleteReactionRoleConfig(Guild guild, String channelID, String msgid) {
		this.findorCreateReactionroleConfigs(guild, channelID).remove(msgid);
	}
	
	public void setPollConfig(Guild guild, String channelID, String msgid, String key, String value) {
		this.findPollConfig(guild, channelID, msgid).put(key, value);
	}
	
	public void deletePollConfig(Guild guild, String channelID, String msgID) {
		this.findPollConfigs(guild, channelID).remove(msgID);
	}
	//Find or Create methods
	public JSONObject findorCreateUserConfig(User user, Guild guild) {
		Document doc = userconfigs.find(new Document("id", user.getId())).first();
		JSONObject usercf;
		if (doc == null) {
			usercf = new JSONObject(new JSONTokener(this.getClass().getClassLoader().getResourceAsStream("templates/user-template.json")));
			usercf.put("id", user.getId());
		} else {
			usercf = new JSONObject(doc.toJson());
		}
		JSONObject membercf;
		try {
			membercf = usercf.getJSONObject("guilds").getJSONObject(guild.getId());
		} catch (JSONException e) {
			usercf.getJSONObject("guilds").put(guild.getId(), new JSONObject(new JSONTokener(this.getClass().getClassLoader().getResourceAsStream("templates/member-template.json"))));
			membercf = usercf.getJSONObject("guilds").getJSONObject(guild.getId());
		}
		this.pushConfig(usercf);
		return membercf;
	}
	
	public JSONObject findorCreateGuildConfig(Guild guild) {
		Document doc = guildconfigs.find(new Document("id", guild.getId())).first();
		if (doc == null) {
			JSONObject newDoc = new JSONObject(new JSONTokener(this.getClass().getClassLoader().getResourceAsStream("templates/guild-template.json")));
			newDoc.put("id", guild.getId());
			this.pushConfig(newDoc);
			return newDoc;
		} else {
			return new JSONObject(doc.toJson());
		}
	}
	
	public JSONObject findPollConfig(Guild guild, String channelID, String msgID) {
		JSONObject pollcfs = this.findPollConfigs(guild, channelID);
		JSONObject channel, msg;
		channel = this.findorCreateChannelConfig(pollcfs, channelID);
		try {
			msg = channel.getJSONObject(msgID);
		} catch (JSONException e) {
			msg = null;
		}
		return msg;
	}
	
	public JSONObject createPollConfig(Guild guild, String channelID, String msgID) {
		JSONObject pollcfs = this.findPollConfigs(guild, channelID);
		JSONObject channel = this.findorCreateChannelConfig(pollcfs, channelID);
		channel.put(msgID, new JSONObject(new JSONTokener(this.getClass().getClassLoader().getResourceAsStream("templates/poll-template.json"))));
		return channel.getJSONObject(msgID);
	}
	
	public JSONObject findPollConfigs(Guild guild, String channelID) {
		return this.findorCreateGuildConfig(guild).getJSONObject("polls");
	}
	
	public JSONObject findReactionroleConfig(Guild guild, String channelID, String msgID) {
		JSONObject rrcfs = this.findPollConfigs(guild, channelID);
		JSONObject channel, msg;
		channel = this.findorCreateChannelConfig(rrcfs, channelID);
		try {
			msg = channel.getJSONObject(msgID);
		} catch (JSONException e) {
			msg = null;
		}
		return msg;
	}
	
	public JSONObject createReactionroleConfig(Guild guild, String channelID, String msgID) {
		JSONObject rrcfs = this.findPollConfigs(guild, channelID);
		JSONObject channel = this.findorCreateChannelConfig(rrcfs, channelID);
		channel.put(msgID, new JSONObject());
		return channel.getJSONObject(msgID);
	}
	
	public JSONObject findorCreateReactionroleConfigs(Guild guild, String channelID) {
		return this.findorCreateGuildConfig(guild).getJSONObject("reactionroles");
	}
	
	public JSONObject findorCreateChannelConfig(JSONObject jObject, String channelID) {
		JSONObject channel;
		try {
			channel = jObject.getJSONObject(channelID);
		} catch (JSONException e) {
			jObject.put(channelID, new JSONObject());
			channel = jObject.getJSONObject(channelID);
		}
		return channel;
	}
	//Tool methods	
	private void pushConfig(JSONObject jObject) {
		MongoCollection<Document> selected = null;
		if (jObject.getString("type").equals("guild")) {
			selected = guildconfigs;
		} else if (jObject.getString("type").equals("user")) {
			selected = userconfigs;
		}
		Document result = selected.find(new Document("id", jObject.get("id"))).first();
		if (result != null) {
			selected.updateOne(result, Document.parse(jObject.toString()));
		} else {
			selected.insertOne(Document.parse(jObject.toString()));
		}
	}
	@SuppressWarnings("unused")
	private void removeValueFromArray(JSONArray current, String value) {
		int index = -1;
		for (int i = 0; i < current.length(); i++) {
			if (current.getString(i).equals(value)) {
				index = i;
			}
		}
		if (index >= 0) {
			current.remove(index);
		}
	}
 }