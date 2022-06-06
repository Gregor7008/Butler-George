package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import components.moderation.ModEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Mute implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getOption("member").getAsUser();
		ConfigLoader.run.getMemberConfig(guild, user).put("muted", true);
		event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/mute:success").replaceDescription("{user}", user.getName()).convert()).queue();
		ModEngine.run.modCheck(guild);
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("mute", "Mute a user permanently")
				.addOptions(new OptionData(OptionType.USER, "member", "The member you want to mute", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.build.getRaw(guild, user, "/commands/moderation/mute:help");
	}
}