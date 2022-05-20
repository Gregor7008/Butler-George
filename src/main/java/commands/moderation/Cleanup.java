package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Cleanup implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		//TODO Running through all users, checking whether they left, if so: Delete Guild Object inside Users' config-file
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/cleanup:success").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("cleanup", "Deletes all progress of left members");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/cleanup:help");
	}
}