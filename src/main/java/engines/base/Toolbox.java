package engines.base;

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

import base.Bot;
import engines.configs.ConfigLoader;
import engines.functions.GuildMusicManager;
import engines.functions.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

public abstract class Toolbox {

	public static void forwardMessage(MessageChannel target, Message source) {
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
		MessageCreateAction messageAction = target.sendMessage(source.getContentRaw());
		for (int i = 0; i < files.size(); i++) {
			messageAction.addFiles(FileUpload.fromData(files.get(i), names.get(i)));
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
			return Bot.INSTANCE.jda.getUserById(ConfigLoader.INSTANCE.getGuildConfig(guild, "customchannelcategories").getLong(category.getId()));
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static String processAutoMessage(String input, Guild guild, User user, boolean mentions) {
		String output =  input.replace("{server}", guild.getName())
				.replace("{membercount}", Integer.toString(guild.getMemberCount()))
				.replace("{date}", OffsetDateTime.now().format(LanguageEngine.DEFAULT_TIME_FORMAT))
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
	
	public static void stopMusicAndLeaveOn(Guild guild) {
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		musicManager.scheduler.player.stopTrack();
		musicManager.scheduler.queue.clear();
		VoiceChannel vc = (VoiceChannel) guild.getSelfMember().getVoiceState().getChannel();
		guild.getAudioManager().closeAudioConnection();
		if (vc.getUserLimit() != 0) {
			vc.getManager().setUserLimit(vc.getUserLimit() - 1).queue();
		}
	}
}