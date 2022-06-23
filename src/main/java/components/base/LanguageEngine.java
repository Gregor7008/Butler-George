package components.base;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import components.utilities.CustomMessageEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class LanguageEngine {
	
	public static String footer = "Made with ❤️ by Gregor7008";
	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
	
	public static CustomMessageEmbed fetchMessage(Guild guild, User user, Object requester, String key)  {
		return LanguageEngine.buildMessage(LanguageEngine.getTitle(guild, user, requester, key), LanguageEngine.getDescription(guild, user, requester, key));
	}
	
	public static CustomMessageEmbed buildMessage(String title, String description) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(title);
		eb.setColor(56575);
		eb.setDescription(description);
		eb.setFooter(footer);
		MessageEmbed embed = eb.build();
		return new CustomMessageEmbed(embed);
	}
	
	public static String getTitle(Guild guild, User user, Object requester, String key) {
		String[] temp1 = LanguageEngine.getRaw(guild, user, requester, key).split("; ");
		return temp1[0];
	}

	public static String getDescription(Guild guild, User user, Object requester, String key) {
		String[] temp1 = LanguageEngine.getRaw(guild, user, requester, key).split("; ");
		return temp1[1];
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
		} catch (NullPointerException | IOException e) {
			ConsoleEngine.out.error(LanguageEngine.class, "Couldn't find language file for " + fullpath + ":" + key);
			return "Error!; :x: | Couldn't find language files!\nContact support immediately!";
		}
		String temp2 = properties.getProperty(key);
		return temp2;
	}
}