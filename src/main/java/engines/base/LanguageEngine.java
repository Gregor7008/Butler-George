package engines.base;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.annotation.Nullable;

import assets.base.CustomMessageEmbed;
import assets.logging.Logger;
import engines.configs.ConfigManager;
import engines.logging.ConsoleEngine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public abstract class LanguageEngine {
	
	public static String footer = "Made with ❤️ by Gregor7008";
	public static DateTimeFormatter ODT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
	public static int color = 56575;
	
	private static Logger LOG = ConsoleEngine.getLogger(ConfigManager.class);
	
	public static CustomMessageEmbed fetchMessage(Guild guild, User user, Object requester, String key)  {
		String raw = LanguageEngine.getRaw(guild, user, requester, key);
	    return LanguageEngine.buildMessageFromRaw(raw, LanguageEngine.getFooter(raw));
	}
	
	public static CustomMessageEmbed buildMessageFromRaw(String raw, @Nullable String opFooter) {
		String[] rawSplit = raw.split("; ");
		return LanguageEngine.buildMessage(rawSplit[0], rawSplit[1], LanguageEngine.getFooter(raw));
	}
	
	public static CustomMessageEmbed buildMessage(String title, String description, @Nullable String opFooter) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(title);
		eb.setColor(color);
		eb.setDescription(description);
		if (opFooter == null) {
			eb.setFooter(footer);
		} else {
			eb.setFooter(opFooter);
		}
		return new CustomMessageEmbed(eb.build());
	}
	
	public static String getTitle(Guild guild, User user, Object requester, String key) {
		return LanguageEngine.getRaw(guild, user, requester, key).split("; ")[0];
	}

	public static String getDescription(Guild guild, User user, Object requester, String key) {
		return LanguageEngine.getRaw(guild, user, requester, key).split("; ")[1];
	}
	
	public static String getFooter(Guild guild, User user, Object requester, String key) {
		return LanguageEngine.getFooter(LanguageEngine.getRaw(guild, user, requester, key));
	}
	
	public static String getFooter(String raw) {
		String[] rawSplit = raw.split("; ");
		try {
			return rawSplit[2];
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public static String getRaw(Guild guild, User user, Object requester, String key) {
		String lang = "en";
		if (user != null && guild != null) {
			//lang = ConfigLoader.INSTANCE.getUserConfig(guild, user).getString("language"); <= Deactivated as translations are not ready
		}
		String path = "general";
		if (requester != null) {
			String requesterName = "";
			if (requester instanceof Class) {
				Class<?> castedRequester = (Class<?>) requester;
				requesterName = castedRequester.getName();
			} else {
				requesterName = requester.getClass().getName();
			}
			path = requesterName.replace('.', '/').replace("$1", "").toLowerCase();
		}
		String fullpath = "languages/" + lang + "/" + path + ".properties";
		Properties properties = new Properties();
		try {
			properties.load(LanguageEngine.class.getClassLoader().getResourceAsStream(fullpath));
			String temp2 = properties.getProperty(key);
			if (temp2 == null) {
				throw new NullPointerException();
			} else {
				return temp2;
			}
		} catch (NullPointerException | IOException e) {
			LOG.error("Couldn't find language file for " + fullpath + ":" + key);
			return "Error!; :x: | Couldn't find language files!\nContact support immediately!";
		}
	}
}