package tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class Answer {

	public Answer(String title, String message, TextChannel channel, boolean delete) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(title);
		eb.setColor(56575);
		eb.setDescription(message);
		eb.setAuthor("NoLimits", null,"https://i.ibb.co/CWJ8nVn/No-Limits-mit-Stern-V1.png");
		eb.setFooter("Official NoLimits Bot! - discord.gg/qHA2vUs");
		MessageEmbed embed = eb.build();
		if (delete == true) {
			channel.sendMessageEmbeds(embed).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
		} else {
			channel.sendMessageEmbeds(embed).queue();
		}
	}
	
	public String fetchMessage (String input) {
	
	String[] temp1 = input.split(":");
	String path = temp1[0];
	String key = temp1[1];
	File propertiesFile = new File(path + ".properties");
	Properties properties = new Properties();
	 
	try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile))) {
	  properties.load(bis);
	} catch (Exception ex) {}
	 
	return properties.getProperty(key);
	}
}
