package commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface TextCommand {

	public void perform(GuildMessageReceivedEvent event, String argument);
	
}
