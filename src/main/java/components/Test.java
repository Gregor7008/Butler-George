package components;

import base.Configloader;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Test {

	public Test(GuildMessageReceivedEvent event, String argument) {
		System.out.println(Configloader.INSTANCE.getConfigs(event.getGuild(), "autoroles"));
	}	 
}
