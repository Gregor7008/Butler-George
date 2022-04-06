package components.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import base.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class AnswerEngine {
	
	public static AnswerEngine ae;
	public String footer = "Made with ❤️ by Gregor7008";
	
	public AnswerEngine() {
		ae = this;
	}
	
	public MessageEmbed fetchMessage(Guild guild, User user, String input)  {
		return this.buildMessage(this.getTitle(guild, user, input), this.getDescription(guild, user, input));
	}
	
	public MessageEmbed buildMessage(String title, String description) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(title);
		eb.setColor(56575);
		eb.setDescription(description);
		eb.setFooter(footer);
		MessageEmbed embed = eb.build();
		return embed;
	}
	
	public String getTitle(Guild guild, User user, String input) {
		String[] temp1 = this.getRaw(guild, user, input).split(";\\s+");
		return temp1[0];
	}

	public String getDescription(Guild guild, User user, String input) {
		String[] temp1 = this.getRaw(guild, user, input).split(";\\s+");
		return temp1[1];
	}
	
	public String getRaw(Guild guild, User user, String input) {
		String lang = Configloader.INSTANCE.getUserConfig(guild, user, "language");
		lang = "en"; //<= Disables the multi-language function, as translations aren't dont yet!
		String[] temp1 = input.split(":");
		String path = temp1[0];
		String key = temp1[1];
		File propertiesFile = new File(Bot.environment + "/languages/" + lang + "/" + path + ".properties");
		Properties properties = new Properties();
		
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile))) {
			properties.load(bis);
		} catch (Exception e) {}
		String temp2 = properties.getProperty(key);
		return temp2;
	}
}