package actions;

import components.base.LanguageEngine;
import components.commands.Command;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class SupportChannel implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubcommandName().equals("set")) {
			ConfigLoader.getGuildConfig(guild).put("suggestionchannel", event.getOption("channel").getAsGuildChannel().getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/supportchannel:setsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("clear")) {
			ConfigLoader.getGuildConfig(guild).put("suggestionchannel", 0L);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/supportchannel:clearsuccess").convert()).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("supportchannel", "0")
				.addSubcommands(new SubcommandData("set", "Sets the support channel of this server")
						.addOption(OptionType.CHANNEL, "channel", "Mention a text channel", true))
				.addSubcommands(new SubcommandData("clear", "Unassigns the channel for ticket creation"));
		return command;
	}
}