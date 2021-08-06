package base;

import commands.CommandList;
import commands.Commands;
import components.Answer;
import components.Automatic;
import components.Test;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Processor extends ListenerAdapter {
	
	String arguments;
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getMessage().getAuthor().isBot()) {return;}
		if (event.getMessage().getContentRaw().startsWith(Bot.INSTANCE.getConfig("prefix"))) {			
			String[] raw = event.getMessage().getContentRaw().split("\\s+", 2);
			String[] command = raw[0].split("(?<=" + Bot.INSTANCE.getConfig("prefix") + ")");
			try {arguments = raw[1];
			} catch (Exception e) {arguments = null;}
			
			CommandList commandList = new CommandList();
			Commands mdc;
			if((mdc = commandList.CommandList.get(command[1])) != null) {
				mdc.perform(event, arguments);
				return;
			}
			
			switch(command[1]) {
				case("stop"):
					event.getMessage().delete().queue();
					Bot.INSTANCE.shutdown();
					break;
				case("test"):
					new Test(event);
					break;
				default:
					event.getMessage().delete().queue();
					new Answer("/base/messageprocessor:unknown", event);
			}
		} else { 
				new Automatic(event);
		}
	}
}
