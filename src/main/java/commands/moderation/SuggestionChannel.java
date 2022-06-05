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

public class SuggestionChannel implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubcommandName().equals("set")) {
			ConfigLoader.run.getGuildConfig(guild).put("suggestionchannel", event.getOption("channel").getAsGuildChannel().getIdLong());
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/moderation/suggestionchannel:setsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("clear")) {
			ConfigLoader.run.getGuildConfig(guild).put("suggestionchannel", Long.valueOf(0));
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/moderation/suggestionchannel:clearsuccess").convert()).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("suggestionchannel", "0")
				.addSubcommands(new SubcommandData("set", "Sets a suggestion channel for this server")
						.addOption(OptionType.CHANNEL, "channel", "Mention the channel that should be used", true))
				.addSubcommands(new SubcommandData("clear", "Unassigns the channel for suggestions"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.run.getRaw(guild, user, "/commands/moderation/suggestionchannel:help");
	}
}