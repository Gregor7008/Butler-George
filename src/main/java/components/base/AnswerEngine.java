package components.base;

import java.io.IOException;
import java.util.Properties;

import components.base.assets.CustomMessageEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class AnswerEngine {
	
	public static AnswerEngine build;
	public String footer = "Made with ❤️ by Gregor7008";
	
	public AnswerEngine() {
		build = this;
	}
	
	public CustomMessageEmbed fetchMessage(Guild guild, User user, String input)  {
		return this.buildMessage(this.getTitle(guild, user, input), this.getDescription(guild, user, input));
	}
	
	public MessageEmbed createMessage(String title, String description) {
		return this.buildMessage(title, description).convert();
	}
	
	private CustomMessageEmbed buildMessage(String title, String description) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(title);
		eb.setColor(56575);
		eb.setDescription(description);
		eb.setFooter(footer);
		MessageEmbed embed = eb.build();
		return new CustomMessageEmbed(embed);
	}
	
	private String getTitle(Guild guild, User user, String input) {
		String[] temp1 = this.getRaw(guild, user, input).split("; ");
		return temp1[0];
	}

	private String getDescription(Guild guild, User user, String input) {
		String[] temp1 = this.getRaw(guild, user, input).split("; ");
		return temp1[1];
	}
	
	public String getRaw(Guild guild, User user, String input) {
		String lang = "en";
		if (user != null && guild != null) {
			//lang = ConfigLoader.run.getUserConfig(guild, user).getString("language"); <= Deactivated as translations are not ready
		}
		String[] temp1 = input.split(":");
		String path = temp1[0];
		String key = temp1[1];
		Properties properties = new Properties();
		if (!path.startsWith("/")) {
			ConsoleEngine.out.info(this, "Path error for path \"" + path + "\"");
			path = "/" + path;
		}
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("languages/" + lang + path + ".properties"));
		} catch (NullPointerException | IOException e) {
			ConsoleEngine.out.error(this, "Couldn't find language files!");
			return "Error!; :x: | Couldn't find language files!\nContact support immediately!";
		}
		String temp2 = properties.getProperty(key);
		return temp2;
	}
}