package base.engines;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base.Bot;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public abstract class Toolbox {

	public static void resendMessage(MessageChannel target, Message source) {
		List<Attachment> attachements = source.getAttachments();
		List<File> files = new ArrayList<>();
		List<String> names = new ArrayList<>();
		for (int i = 0; i < attachements.size(); i++) {
			File file = null;
			try {file = File.createTempFile(attachements.get(i).getFileName(), null);
			} catch (IOException e) {}
			Boolean deleted = true;
			if (file.exists()) {
				deleted = file.delete();
			}
			if (deleted) {
				try {
					attachements.get(i).getProxy().downloadToFile(file).get();
				} catch (InterruptedException | ExecutionException e) {}
				names.add(attachements.get(i).getFileName());
				files.add(file);
			}
		}
		MessageAction messageAction = target.sendMessage(source);
		for (int i = 0; i < files.size(); i++) {
			File file = files.get(i);
			messageAction.addFile(file, names.get(i));
		}
		messageAction.queue(e -> files.forEach(f -> f.delete()));
	}
	
	public static boolean checkURL(String subject) {
		try {
			new URL(subject);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}
	
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
			return Bot.INSTANCE.jda.getUserById(ConfigLoader.INSTANCE.getFirstGuildLayerConfig(guild, "customchannelcategories").getLong(category.getId()));
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static String processAutoMessage(String input, Guild guild, User user, boolean mentions) {
		String output =  input.replace("{server}", guild.getName())
				.replace("{membercount}", Integer.toString(guild.getMemberCount()))
				.replace("{date}", OffsetDateTime.now().format(LanguageEngine.formatter))
				.replace("{boosts}", String.valueOf(guild.getBoostCount()))
				.replace("{level}", String.valueOf(ConfigLoader.INSTANCE.getMemberConfig(guild, user).getInt("level")));
		if (mentions) {
			return output.replace("{user}", user.getAsMention());
		} else {
			return output.replace("{user}", user.getName());
		}
	}
	
	public static void scheduleOperation(Runnable operation, long delay) {
		Bot.INSTANCE.getTimer().schedule(new TimerTask() {
			@Override
			public void run() {
				operation.run();
			}
		}, delay);
	}
}