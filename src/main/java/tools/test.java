package tools;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class test {

	public test(GuildMessageReceivedEvent event) {
		int e = event.getGuild().getRolesByName("GTAV", true).get(0).getPosition();
		System.out.println(e);
	}	 
}
