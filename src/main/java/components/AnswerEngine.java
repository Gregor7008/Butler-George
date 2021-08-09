package components;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class AnswerEngine {
	
	private static AnswerEngine INSTANCE;
	
	public static AnswerEngine getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AnswerEngine();
		}
		return INSTANCE;
	}
	
	public MessageEmbed fetchMessage (String input, Guild guild, Member member)  {
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
	
		temp2.replace("{servername}", guild.getName());
		temp2.replace("{membername}", member.getAsMention());
		temp2.replace("{membercount}", Integer.toString(guild.getMemberCount()));
		
		String[] temp3 = temp2.split(";\\s+");
		eb.setTitle(temp3[0]);
		eb.setColor(56575);
		eb.setDescription(temp3[1]);
		eb.setFooter("Official-NoLimits Bot! - discord.gg/qHA2vUs");
		MessageEmbed embed = eb.build();
		return embed;
	}
	
	public MessageEmbed buildMessage (String title, String description) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(title);
		eb.setColor(56575);
		eb.setDescription(description);
		eb.setFooter("Official NoLimits-Bot! - discord.gg/qHA2vUs");
		MessageEmbed embed = eb.build();
		return embed;
	}
}
