package main;

import java.util.concurrent.TimeUnit;

import functions.embed;
import functions.rolecheck;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
	
	String[] raw, command;
	String object;
	int onOther;
	Member executeOn;
	Role mentionedRole;
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		raw = event.getMessage().getContentRaw().split("\\s+", 2);
		if (raw[0].startsWith(Startup.prefix)) {
			String input = raw[0];
			try {
				object = raw[1];
			} catch (Exception e) {
				object = "none";
			}
			try {
				mentionedRole = event.getMessage().getMentionedRoles().get(0);
			} catch (Exception e) {}
			try {
				executeOn = event.getMessage().getMentionedMembers().get(0);
				onOther = 1;
			} catch (Exception e) {
				onOther = 0;
			}
			command = input.split("(?<=" + Startup.prefix + ")");
			event.getMessage().delete().queue();
			switch(onOther) {
				case(1):
					switch(command[1]) {
					case "role-check":
						new rolecheck(event, mentionedRole, executeOn);
						break;
					case "embed":
						new embed(event, object, executeOn);
						break;
					default:
						event.getChannel().sendMessage("Unknown Command").queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
					}
					break;
				case(0):
					switch(command[1]) {
					case "role-check":
						new rolecheck(event, mentionedRole);
						break;
					case "embed":
						new embed(event, object);
						break;
					case "stop":
						Startup.endMe();
						break;
					default:
						event.getChannel().sendMessage("Unknown Command").queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
					}
					break;
			}
		}
	}
}
