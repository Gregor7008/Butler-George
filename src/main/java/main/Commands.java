package main;

import functions.embed;
import functions.rolecheck;
import functions.rolesorting;
import functions.test;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
	
	String[] raw, command;
	String object;
	Member member;
	Role role;
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getMessage().getContentRaw().startsWith(Startup.prefix) && !event.getMessage().getAuthor().isBot()) {
		raw = event.getMessage().getContentRaw().split("\\s+", 2);
			String input = raw[0];
			try {
				object = raw[1];
			} catch (Exception e) {
				object = "none";
			}
			try {
				role = event.getMessage().getMentionedRoles().get(0);
			} catch (Exception e) {}
			try {
				member = event.getMessage().getMentionedMembers().get(0);
			} catch (Exception e) {
				member = event.getMember();
			}
			command = input.split("(?<=" + Startup.prefix + ")");
			switch(command[1]) {
				case "role-check":
					new rolecheck(event, role, member);
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
					Startup.endMe();
					break;
				default:
					new Answer("Unknown Command",":exclamation: | This command doesn't seem to exist! \n Error code: 001", event, false);
			}
		}
	}
}
