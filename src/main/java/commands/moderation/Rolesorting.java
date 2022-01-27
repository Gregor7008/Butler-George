package commands.moderation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Rolesorting implements Command{
	
	private SlashCommandEvent oevent;
	private Role grouprole;
	private List<Role> subroles;
	private List<Member> members;
	private EventWaiter waiter = Bot.INSTANCE.getWaiter();
	private Guild guild;
	private Member member;
	private User user;
	private TextChannel channel;
	private List<Message> messages = new ArrayList<>();
	
	@Override
	public void perform(SlashCommandEvent event) {
		oevent = event;
		guild = event.getGuild();
		user = event.getUser();
		member = event.getMember();
		if (!member.hasPermission(Permission.MANAGE_ROLES)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/rolesorting:nopermission")).queue();
			return;
		}
		channel = event.getTextChannel();
		this.definegroup();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("rolesort", "Use this command to sort groups by grouping roles!");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/rolesorting:help");
	}

	private void definegroup() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/rolesorting:definegroup")).complete());
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == member.getUser().getIdLong();},
							e -> {messages.add(e.getMessage());
								  grouprole = e.getMessage().getMentionedRoles().get(0);
								  this.definesub();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void definesub() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/rolesorting:definesub")).complete());
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == member.getUser().getIdLong();},
							e -> {messages.add(e.getMessage());
								  subroles = e.getMessage().getMentionedRoles();
								  this.definemember();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void definemember() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/rolesorting:definemember")).complete());
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == member.getUser().getIdLong();},
							e -> {messages.add(e.getMessage());
								  members = e.getMessage().getMentionedMembers();
								  this.rolesorter();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void rolesorter() {
		for (int e = 0; e<members.size(); e++) {
			this.sorter(guild, members.get(e), subroles, grouprole);
		}
		this.cleanup();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/rolesorting:success")).queue(response -> response.delete().queueAfter(10, TimeUnit.SECONDS));
	}
	
	public void sorter(Guild iguild, Member mb, List<Role> sr, Role gr) {
			int match = 0;
			for (int i = 0; i < mb.getRoles().size(); i++) {
				if (sr.contains(mb.getRoles().get(i))) {
					match++;
				}
			}
			if (match > 0 && !mb.getRoles().contains(gr)) {
				iguild.addRoleToMember(mb, gr).queue();
				System.out.println("Role " + gr.getName() + " added to " + mb.getEffectiveName());
			}
			if (match == 0 && mb.getRoles().contains(gr)) {
				iguild.removeRoleFromMember(mb, gr).queue();
				System.out.println("Role " + gr.getName() + " removed from " + mb.getEffectiveName());
			}
	}
	
	private void cleanup() {
		oevent.getHook().deleteOriginal().queue();
		channel.deleteMessages(messages).queue();
	}
}