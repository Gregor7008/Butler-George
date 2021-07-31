package base;

import functions.chat;
import functions.embed;
import functions.rolecheck;
import functions.rolesorting;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import tools.answer;
import tools.test;

public class Commands extends ListenerAdapter {
	
	String object;
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getMessage().getAuthor().isBot()) {return;}
		if (event.getMessage().getContentRaw().startsWith(Bot.prefix)) {
			String[] raw = event.getMessage().getContentRaw().split("\\s+", 2);
			String[] command = raw[0].split("(?<=" + Bot.prefix + ")");
			try {object = raw[1];
			} catch (Exception e) {object = "none";}
			switch(command[1]) {
				case "role-check":
					new rolecheck(event);
					break;
				case "role-sort":
					new rolesorting(event);
					break;
				case "embed":
					new embed(event);
					break;
				case "test":
					new test(event);
					break;
				case "stop":
					event.getMessage().delete().queue();
					Bot.shutdown();
					break;
				default:
					new answer("/commands/commands:unknown", event);
					event.getMessage().delete().queue();
			}
		} else { 
				new chat(event);
		}
	}
}
