package base;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface Commands {
	
	public void perform(GuildMessageReceivedEvent event, String arguments);

}
