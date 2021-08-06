package components;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Answer {
	
	public Answer(String input, GuildMessageReceivedEvent e) {
		this.fetchMessage(input, e);
		//Input should look like this: new Answer("/folderxy:filexy", event);
	}
	
	public Answer(String input, String input2, TextChannel channel) {
		this.buildMessage(input, input2, false, channel);
	}
	
	public void fetchMessage (String input, GuildMessageReceivedEvent event) {
	
	String[] temp1 = input.split(":");
	String path = temp1[0];
	String key = temp1[1];
	File propertiesFile = new File("./src/main/resources" + path + ".properties");
	Properties properties = new Properties();
	 
	try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile))) {
	  properties.load(bis);
	} catch (Exception ex) {}
	 
	String temp2 = properties.getProperty(key);
	try {
	temp2.replace("{servername}", event.getGuild().getName());
	temp2.replace("{membername}", event.getAuthor().getAsMention());
	temp2.replace("{membercount}", Integer.toString(event.getGuild().getMemberCount()));
	temp2.replace("{mentionedrole}", event.getMessage().getMentionedRoles().get(0).getAsMention());
	temp2.replace("{currentsong", null);
	} catch (Exception e) {}
	String[] temp3 = temp2.split(";\\s+");
	this.buildMessage(temp3[0], temp3[1], Boolean.parseBoolean(temp3[2]), event.getChannel());
	}
	
	public void buildMessage (String title, String message, boolean delete, TextChannel channel) {
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
}
