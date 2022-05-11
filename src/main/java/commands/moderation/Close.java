package commands.moderation;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Close implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getTextChannel().getName().contains("-support")) {
			event.getTextChannel().delete().queue();
			return;
		}
		if (Configloader.INSTANCE.getMailConfig1(event.getTextChannel().getId()) != null) {
			String cid = event.getTextChannel().getId();
			event.getTextChannel().delete().queue();
			User cuser = Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getMailConfig1(cid));
			Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getMailConfig1(cid)).openPrivateChannel().complete().sendMessageEmbeds(
					AnswerEngine.ae.fetchMessage(guild, cuser, "/commands/moderation/close:closed").replaceDescription("{reason}", event.getOption("reason").getAsString()).convert()).queue();
			Configloader.INSTANCE.removeMailConfig(cid);
			try {
				if (event.getOption("warning").getAsBoolean()) {
					Configloader.INSTANCE.addUserConfig(guild, cuser, "warnings", "Modmail abuse");
					Bot.INSTANCE.penaltyCheck(guild);
				}
			} catch (NullPointerException e) {}
		} else {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/close:nochannel").convert()).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("close", "Closes the support channel")
				.addOption(OptionType.STRING, "reason", "The reason why the ticket was closed", true)
				.addOption(OptionType.BOOLEAN, "warning", "Whether the member should be warned");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/close:help");
	}
}