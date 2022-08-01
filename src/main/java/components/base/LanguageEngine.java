package components.base;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class LanguageEngine {
	
	public static String footer = "Made with ❤️ by Gregor7008";
	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
	public static int color = 56575;
	
	public static CustomMessageEmbed fetchMessage(Guild guild, User user, Object requester, String key)  {
		String[] raw = LanguageEngine.getRaw(guild, user, requester, key).split("; ");
		return LanguageEngine.buildMessage(raw[0], raw[1], LanguageEngine.getFooter(raw));
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
		MessageEmbed embed = eb.build();
		return new CustomMessageEmbed(embed);
	}
	
	public static String getTitle(Guild guild, User user, Object requester, String key) {
		String[] raw = LanguageEngine.getRaw(guild, user, requester, key).split("; ");
		return raw[0];
	}

	public static String getDescription(Guild guild, User user, Object requester, String key) {
		String[] raw = LanguageEngine.getRaw(guild, user, requester, key).split("; ");
		return raw[1];
	}
	
	public static String getFooter(Guild guild, User user, Object requester, String key) {
		String[] raw = LanguageEngine.getRaw(guild, user, requester, key).split("; ");
		return LanguageEngine.getFooter(raw);
	}
	
	public static String getFooter(String[] raw) {
		try {
			return raw[2];
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public static String getRaw(Object requester, String key) {
		return LanguageEngine.getRaw(null, null, requester, key);
	}
	
	public static String getRaw(Guild guild, User user, Object requester, String key) {
		String lang = "en";
		if (user != null && guild != null) {
			//lang = ConfigLoader.getUserConfig(guild, user).getString("language"); <= Deactivated as translations are not ready
		}
		String path = "general";
		if (requester != null) {
			path = requester.getClass().getName().replace('.', '/').toLowerCase();
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
			ConsoleEngine.out.error(LanguageEngine.class, "Couldn't find language file for " + fullpath + ":" + key);
			return "Error!; :x: | Couldn't find language files!\nContact support immediately!";
		}
	}
}