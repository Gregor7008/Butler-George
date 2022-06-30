package components.utilities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base.Bot;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;

public class Toolbox {

	public static void clearValueOfJSONObject(JSONObject jObject, String key) {
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
			jObject.put(key, 0);
			return;
		} catch (JSONException e) {}
		try {
			jObject.getLong(key);
			jObject.put(key, 0L);
			return;
		} catch (JSONException e) {}
	}
	
	public static boolean removeValueFromArray(JSONArray current, Object value) {
		for (int i = 0; i < current.length(); i++) {
			if (current.get(i).equals(value)) {
				current.remove(i);
				i = current.length();
				return true;
			}
		}
		return false;
	}
	
	public static User checkCategory(Category category, Guild guild) {
		try {
			return Bot.run.jda.getUserById(ConfigLoader.getFirstGuildLayerConfig(guild, "customchannelcategories").getLong(category.getId()));
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static void disableActionRows(Message message) {
		List<ActionRow> actionRows = message.getActionRows();
		List<ActionRow> newActionRows = new ArrayList<ActionRow>();
		actionRows.forEach(a -> newActionRows.add(a.asDisabled()));
		message.editMessageComponents(newActionRows).queue();
	}
}