package main;

import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Answer {

	public Answer(String title, String message, GuildMessageReceivedEvent event, boolean delete) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(title);
		eb.setColor(56575);
		eb.setDescription(message);
		eb.setAuthor("NoLimits", null,"https://i.ibb.co/CWJ8nVn/No-Limits-mit-Stern-V1.png");
		eb.setFooter("Official NoLimits Bot! - discord.gg/qHA2vUs");
		if (delete == true) {
			event.getChannel().sendMessage(eb.build()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
		} else {
			event.getChannel().sendMessage(eb.build()).queue();
		}
	}
}
