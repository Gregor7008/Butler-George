package base;

import functions.chat;
import functions.embed;
import functions.rolecheck;
import functions.rolesorting;
import functions.test;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
	
	String object;
	Member member;
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getMessage().getAuthor().isBot()) {return;}
		if (event.getMessage().getContentRaw().startsWith(Bot.prefix)) {
			String[] raw = event.getMessage().getContentRaw().split("\\s+", 2);
			String[] command = raw[0].split("(?<=" + Bot.prefix + ")");
			try {object = raw[1];
			} catch (Exception e) {object = "none";}
			try {member = event.getMessage().getMentionedMembers().get(0);
			} catch (Exception e) {member = event.getMember();}
			switch(command[1]) {
				case "role-check":
					new rolecheck(event, member);
					break;
				case "role-sort":
					new rolesorting(event, member);
					break;
				case "embed":
					new embed(event, object, member);
					break;
				case "test":
					new test(event);
					break;
				case "stop":
					event.getMessage().delete().queue();
					Bot.endMe();
					break;
				default:
					new Answer("Unknown Command",":exclamation: | This command doesn't seem to exist! \n ---> Error code: 001", event.getChannel(), true);
					event.getMessage().delete().queue();
			}
		} else { 
				new chat(event);
		}
	}
}
