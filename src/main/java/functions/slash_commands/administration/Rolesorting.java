package functions.slash_commands.administration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import assets.base.AwaitTask;
import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.base.Toolbox;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Rolesorting implements SlashCommandEventHandler {
	
	@Override
	public void execute(SlashCommandInteractionEvent event) {
		this.definegroup(event.getGuild(), event.getUser(), event.getChannel().asTextChannel(), event);
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("rolesort", "Adds and removes roles by other roles (If member has a role, give him another role)");
		command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
		   	   .setGuildOnly(true);
		return command;
	}

	private void definegroup(Guild guild, User user, TextChannel channel, SlashCommandInteractionEvent event) {
	    Message message = event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "definegroup")).complete().retrieveOriginal().complete();
	    AwaitTask.forMessageReceival(guild, user, channel, null,
	            e -> {
	                Role grouprole = e.getMessage().getMentions().getRoles().get(0);
	                this.definesub(guild, user, channel, grouprole, message);
	            }, null).append();
	}

	private void definesub(Guild guild, User user, TextChannel channel, Role grouprole, Message message) {
	    Message newMessage = message.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "definesub")).complete();
	    AwaitTask.forMessageReceival(guild, user, channel, null,
	            e -> {
	                List<Role> subroles = e.getMessage().getMentions().getRoles();
	                this.definemember(guild, user, channel, grouprole, subroles, newMessage);
	            }, null).append();
	}

	private void definemember(Guild guild, User user, TextChannel channel, Role grouprole, List<Role> subroles, Message message) {
	    message.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "definemember")).queue();
	    AwaitTask.forMessageReceival(guild, user, channel, null,
	            e -> {
	                Mentions mentions = e.getMessage().getMentions();
	                List<Member> members = mentions.getMembers();
	                if (!mentions.getRoles().isEmpty()) {
	                    if (mentions.getRoles().contains(guild.getPublicRole())) {
	                        members.addAll(guild.loadMembers().get());
	                    } else {
	                        mentions.getRoles().forEach(role -> members.addAll(guild.findMembersWithRoles(role).get()));
	                    }
	                }
	                this.rolesorter(guild, grouprole, subroles, members);
	                message.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "success")).queue();
	            }, null).append();
	}
	
	private void rolesorter(Guild guild, Role grouprole, List<Role> subroles, List<Member> members) {
	    List<Member> listWithoutDuplicates = new ArrayList<>();
	    Set<Member> set = new HashSet<>(members);
	    listWithoutDuplicates.addAll(set);
		for (int e = 0; e < listWithoutDuplicates.size(); e++) {
			Toolbox.sortRoles(guild, listWithoutDuplicates.get(e), subroles, grouprole);
		}
	}
}