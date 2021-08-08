package components;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class AnswerEngine {
	
	private static AnswerEngine INSTANCE;
	
	public static AnswerEngine getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AnswerEngine();
		}
		return INSTANCE;
	}
	
	public MessageAction fetchMessage (String input, GuildMessageReceivedEvent event)  {
		String[] temp1 = input.split(":");
		String path = temp1[0];
		String key = temp1[1];
		File propertiesFile = new File("./src/main/resources" + path + ".properties");
		Properties properties = new Properties();
		EmbedBuilder eb = new EmbedBuilder();
	 
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile))) {
			properties.load(bis);
		} catch (Exception e) {}
		String temp2 = properties.getProperty(key);
	
		temp2.replace("{servername}", event.getGuild().getName());
		temp2.replace("{membername}", event.getMember().getAsMention());
		temp2.replace("{membercount}", Integer.toString(event.getGuild().getMemberCount()));
		try {
			temp2.replace("{mentionedrole}", event.getMessage().getMentionedRoles().get(0).getAsMention());
		} catch (IndexOutOfBoundsException e) {}
	
		String[] temp3 = temp2.split(";\\s+");
		eb.setTitle(temp3[0]);
		eb.setColor(56575);
		eb.setDescription(temp3[1]);
		eb.setAuthor("NoLimits", null,"https://i.ibb.co/CWJ8nVn/No-Limits-mit-Stern-V1.png");
		eb.setFooter("Official NoLimits Bot! - discord.gg/qHA2vUs");
		MessageEmbed embed = eb.build();
		if (Boolean.parseBoolean(temp3[2])==true) {event.getMessage().delete().queue();}
		return event.getChannel().sendMessageEmbeds(embed);
	}
	
	public MessageAction buildMessage (String title, String description, TextChannel channel) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(title);
		eb.setColor(56575);
		eb.setDescription(description);
		eb.setAuthor("NoLimits", null,"https://i.ibb.co/CWJ8nVn/No-Limits-mit-Stern-V1.png");
		eb.setFooter("Official NoLimits Bot! - discord.gg/qHA2vUs");
		MessageEmbed embed = eb.build();
		return channel.sendMessageEmbeds(embed);
	}
}
