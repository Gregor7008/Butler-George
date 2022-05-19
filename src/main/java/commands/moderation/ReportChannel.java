
package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ReportChannel implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		ConfigLoader.run.setGuildConfig(guild, "reportchannel", event.getOption("channel").getAsGuildChannel().getId());
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reportchannel:success").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("reportchannel", "0")
				.addSubcommands(new SubcommandData("set", "Sets a report channel for this server")
						.addOption(OptionType.CHANNEL, "channel", "The channel that should be used", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/reportchannel:help");
	}
}