package functions;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tools.answer;

public class rolesorting {
	
	Role grouprole;
	List<Role> subroles;
	Member member;

	public rolesorting(GuildMessageReceivedEvent event) {
		definegroup(event);
	}
	
	private void definegroup(GuildMessageReceivedEvent event) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/rolesorting:definegroup", event);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == event.getAuthor().getIdLong();},
							e -> {grouprole = e.getMessage().getMentionedRoles().get(0);
								  this.definesub(event);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(2, event);
								   new answer("/commands/rolesorting:timeout", event);});
	}
	
	private void definesub(GuildMessageReceivedEvent event) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/rolesorting:definesub", event);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == event.getAuthor().getIdLong();},
							e -> {subroles = e.getMessage().getMentionedRoles();
								  this.definemember(event);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(4, event);
								   new answer("/commands/rolesorting:timeout", event);});
	}

	private void definemember(GuildMessageReceivedEvent event) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/rolesorting:definemember", event);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == event.getAuthor().getIdLong();},
							e -> {member = e.getMessage().getMentionedMembers().get(0);
								  this.rolesorter(event);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(6, event);
								   new answer("/commands/rolesorting:timeout", event);});
	}
	
	private void rolesorter(GuildMessageReceivedEvent event) {
		int size = member.getRoles().size();
		int match = 0;
		for (int i = 1; i < size; i++) {
			if (subroles.contains(member.getRoles().get(i))) {
				event.getGuild().addRoleToMember(member, grouprole).queue();
				match++;
			}
		}
		if (match == 0 && member.getRoles().contains(grouprole)) {
			event.getGuild().removeRoleFromMember(member, grouprole).queue();
		}
		List<Message> messages = event.getChannel().getHistory().retrievePast(7).complete();
		event.getChannel().deleteMessages(messages).queue();
		new answer("/commands/rolesorting:success", event);
	}
	
	private void cleanup(int i, GuildMessageReceivedEvent event) {
		List<Message> messages = event.getChannel().getHistory().retrievePast(i).complete();
		event.getChannel().deleteMessages(messages).queue();
	}
}
