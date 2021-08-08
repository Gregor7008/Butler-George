package commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Commands;
import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Rolesorting implements Commands{
	
	Role grouprole;
	List<Role> subroles;
	List<Member> members;
	Bot bot;

	@Override
	public void perform(GuildMessageReceivedEvent event, String arguments) {
		definegroup(event);
	}
	
	public void sort(GuildMessageReceivedEvent event, Member mb, List<Role> sr, Role gr) {
		this.rolesorter(event, mb, sr, gr);
	}

	private void definegroup(GuildMessageReceivedEvent event) {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/rolesorting:definegroup", event).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == event.getAuthor().getIdLong();},
							e -> {grouprole = e.getMessage().getMentionedRoles().get(0);
								  this.definesub(event);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(2, event);
								   AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:timeout", event).queue();});
	}
	
	private void definesub(GuildMessageReceivedEvent event) {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:definesub", event).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == event.getAuthor().getIdLong();},
							e -> {subroles = e.getMessage().getMentionedRoles();
								  this.definemember(event);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(4, event);
								   AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:timeout", event).queue();});
	}

	private void definemember(GuildMessageReceivedEvent event) {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:definemember", event).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == event.getAuthor().getIdLong();},
							e -> {members = e.getMessage().getMentionedMembers();
								  this.rolesorter(event);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(6, event);
								   AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:timeout", event).queue();});
	}
	
	private void rolesorter(GuildMessageReceivedEvent event) {
		for (int e = 0; e<=members.size()-1; e++) {
			Member member = members.get(e);
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
		}
		this.cleanup(7, event);
		AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:success", event).queue();
	}
	
	public void rolesorter(GuildMessageReceivedEvent event, Member mb, List<Role> sr, Role gr) {
			int size = mb.getRoles().size();
			int match = 0;
			for (int i = 1; i < size; i++) {
				if (sr.contains(mb.getRoles().get(i))) {
					event.getGuild().addRoleToMember(mb, gr).queue();
					match++;
				}
			}
			if (match == 0 && mb.getRoles().contains(gr)) {
				event.getGuild().removeRoleFromMember(mb, gr).queue();
			}
	}
	
	private void cleanup(int i, GuildMessageReceivedEvent event) {
		List<Message> messages = event.getChannel().getHistory().retrievePast(i).complete();
		event.getChannel().deleteMessages(messages).queue();
	}
}
