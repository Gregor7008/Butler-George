package commands.moderation;

import commands.Command;
import components.AnswerEngine;
import components.Developers;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Rolecheck implements Command {
	
	@Override
	public void perform(SlashCommandEvent event) {
		final Member member = event.getOption("member").getAsMember();
		if (!event.getMember().hasPermission(Permission.MANAGE_ROLES) && !Developers.getInstance().developers.contains(event.getMember().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:nopermission")).queue();
			return;
		}
		final Role mentionedRole = event.getOption("role").getAsRole();
		if (member == null) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:nomember")).queue();
			return;
		}
		if (mentionedRole == null) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:incomplete")).queue();
			return;
		}
		if (hasRole(member, mentionedRole)==true) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:found")).queue();
		} else {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:notfound")).queue();
		}
	}

	@Override
	public CommandData initialize(Guild guild) {
		CommandData command = new CommandData("rolecheck", "Check if a member has a specific role!")
											  .addOptions(new OptionData(OptionType.USER, "member", "The member that should be checked").setRequired(true))
											  .addOptions(new OptionData(OptionType.ROLE, "role", "The role that the member should be checked for").setRequired(true));
								
		return command;
	}

	@Override
	public String getHelp() {
		return "Provide the bot with a member, a role and the command and it will check if the member has the provided role!";
	}
	
	private boolean hasRole(Member member, Role role) {
		return member.getRoles().contains(role);
	}
}