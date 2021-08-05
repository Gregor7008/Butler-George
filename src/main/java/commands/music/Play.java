package commands.music;

import base.Commands;
import components.Answer;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Play implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String arguments) {
		if(arguments == null) {
			new Answer("commands/music/play:wrongusage", event);
		} else {
			
		}
		
	}

}
