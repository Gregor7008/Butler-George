package components.base;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class LanguageEngine {
	
	public static String footer = "Made with ❤️ by Gregor7008";
	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
	
	public static CustomMessageEmbed fetchMessage(Guild guild, User user, String path)  {
		return LanguageEngine.buildMessage(LanguageEngine.getTitle(guild, user, path), LanguageEngine.getDescription(guild, user, path));
	}
	
	public static MessageEmbed createMessage(String title, String description) {
		return LanguageEngine.buildMessage(title, description).convert();
	}
	
	private static CustomMessageEmbed buildMessage(String title, String description) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(title);
		eb.setColor(56575);
		eb.setDescription(description);
		eb.setFooter(footer);
		MessageEmbed embed = eb.build();
		return new CustomMessageEmbed(embed);
	}
	
	private static String getTitle(Guild guild, User user, String input) {
		String[] temp1 = LanguageEngine.getRaw(guild, user, input).split("; ");
		return temp1[0];
	}

	private static String getDescription(Guild guild, User user, String input) {
		String[] temp1 = LanguageEngine.getRaw(guild, user, input).split("; ");
		return temp1[1];
	}
	
	public static String getRaw(Guild guild, User user, String input) {
		String lang = "en";
		if (user != null && guild != null) {
			//lang = ConfigLoader.getUserConfig(guild, user).getString("language"); <= Deactivated as translations are not ready
		}
		String[] temp1 = input.split(":");
		String path = temp1[0];
		String key = temp1[1];
		Properties properties = new Properties();
		if (!path.startsWith("/")) {
			ConsoleEngine.out.info(LanguageEngine.class, "Path error for path \"" + path + "\"");
			path = "/" + path;
		}
		try {
			properties.load(LanguageEngine.class.getClassLoader().getResourceAsStream("languages/" + lang + path + ".properties"));
		} catch (NullPointerException | IOException e) {
			ConsoleEngine.out.error(LanguageEngine.class, "Couldn't find language files!");
			return "Error!; :x: | Couldn't find language files!\nContact support immediately!";
		}
		String temp2 = properties.getProperty(key);
		return temp2;
	}
}