package textcommands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Rolesorting{
	
	private Role grouprole;
	private List<Role> subroles;
	private List<Member> members;
	private Bot bot;
	private Guild guild;
	private Member member;
	private TextChannel channel;

	public Rolesorting(Guild iguild, Member imember, TextChannel ichannel) {
		guild = iguild;
		member = imember;
		channel = ichannel;
		this.definegroup();
	}
	
	public void sort(Guild guild, Member mb, List<Role> sr, Role gr) {
		this.rolesorter(guild, mb, sr, gr);
	}

	private void definegroup() {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/rolesorting:definegroup", guild, member, channel).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == member.getUser().getIdLong();},
							e -> {grouprole = e.getMessage().getMentionedRoles().get(0);
								  this.definesub();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(2);
								   AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:timeout", guild, member, channel).queue();});
	}
	
	private void definesub() {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:definesub", guild, member, channel).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == member.getUser().getIdLong();},
							e -> {subroles = e.getMessage().getMentionedRoles();
								  this.definemember();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(4);
								   AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:timeout", guild, member, channel).queue();});
	}

	private void definemember() {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:definemember", guild, member, channel).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == member.getUser().getIdLong();},
							e -> {members = e.getMessage().getMentionedMembers();
								  this.rolesorter();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(6);
								   AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:timeout", guild, member, channel).queue();});
	}
	
	private void rolesorter() {
		for (int e = 0; e<=members.size()-1; e++) {
			Member member = members.get(e);
			int size = member.getRoles().size();
			int match = 0;
			for (int i = 1; i < size; i++) {
				if (subroles.contains(member.getRoles().get(i))) {
					guild.addRoleToMember(member, grouprole).queue();
					match++;
				}
			}
			if (match == 0 && member.getRoles().contains(grouprole)) {
				guild.removeRoleFromMember(member, grouprole).queue();
			}
		}
		this.cleanup(7);
		AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:success", guild, member, channel).queue();
	}
	
	public void rolesorter(Guild iguild, Member mb, List<Role> sr, Role gr) {
			int size = mb.getRoles().size();
			int match = 0;
			for (int i = 1; i < size; i++) {
				if (sr.contains(mb.getRoles().get(i))) {
					iguild.addRoleToMember(mb, gr).queue();
					match++;
				}
			}
			if (match == 0 && mb.getRoles().contains(gr)) {
				iguild.removeRoleFromMember(mb, gr).queue();
			}
	}
	
	private void cleanup(int i) {
		List<Message> messages = channel.getHistory().retrievePast(i).complete();
		channel.deleteMessages(messages).queue();
	}
}
