package components.base;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import components.base.assets.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ConfigLoader {
	
	public static ConfigLoader cfl;
	public static ConfigManager cfm = new ConfigManager();
	
	public ConfigLoader() {
		cfl = this;
	}
	
	//Get User values
	public String getUserString(Guild guild, User user, String key) {
		return this.getUserConfig(user, guild).getString(key);
	}
	
	public JSONArray getUserArray(Guild guild, User user, String key) {
		return this.getUserConfig(user, guild).getJSONArray(key);
	}
	
	public int getUserInt(Guild guild, User user, String key) {
		return this.getUserConfig(user, guild).getInt(key);
	}
	
	public long getUserLong(Guild guild, User user, String key) {
		return this.getUserConfig(user, guild).getLong(key);
	}
	
	//Get Guild values
	public String getGuildString(Guild guild, String key) {
		return this.getGuildConfig(guild).getString(key);
	}
	
	public JSONArray getGuildArray(Guild guild, String key) {
		return this.getGuildConfig(guild).getJSONArray(key);
	}
	
	public int getGuildInt(Guild guild, String key) {
		return this.getGuildConfig(guild).getInt(key);
	}
	
	public long getGuildLong(Guild guild, String key) {
		return this.getGuildConfig(guild).getLong(key);
	}
	
	//Get Poll values
		//TODO
	
	//Get Reactionrole values
		//TODO
	
	//Get Modmail values
		//TODO
	
	//Get basis objects
	private JSONObject getUserConfig(User user, Guild guild) {
		return cfm.getMemberConfig(guild, user);
	}
	
	private JSONObject getGuildConfig(Guild guild) {
		return cfm.getGuildConfig(guild);
	}
	
	//Get deep-nested objects
	private JSONObject getFirstLayer(Guild guild, String firstKey) {
		return this.getGuildConfig(guild).getJSONObject(firstKey);
	}
	
	private JSONObject getSecondLayer(Guild guild, String firstKey, String secondKey) {
		return this.getFirstLayer(guild, firstKey).getJSONObject(secondKey);
	}
	
	//Get deep-nested values
	private Object getFirstLayerValue(Guild guild, String firstKey, String key) {
		return this.getFirstLayer(guild, firstKey).get(key);
	}
	
	private Object getSecondLayerValue(Guild guild, String firstKey, String secondKey, String key) {
		return this.getSecondLayer(guild, firstKey, secondKey).get(key);
	}

	//Tool methods	
	private void setValue(JSONObject jObject, String key, Object value) {
		jObject.put(key, value);
	}
	
	private void clearValue(JSONObject jObject, String key) {
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
	
	private void addValueToArray(JSONArray jArray, Object value) {
		jArray.put(value);
	}
	
	private void removeValueFromArray(JSONArray current, Object value) {
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