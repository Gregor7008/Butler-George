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

public class SupportTalk implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (event.getSubcommandName().equals("set")) {
			ConfigLoader.run.getGuildConfig(guild).put("suggestionchannel", event.getOption("channel").getAsGuildChannel().getIdLong());
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/supporttalk:setsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("clear")) {
			ConfigLoader.run.getGuildConfig(guild).put("suggestionchannel", Long.valueOf(0));
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/supporttalk:clearsuccess").convert()).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("supporttalk", "0")
				.addSubcommands(new SubcommandData("set", "Sets the support talk of this server")
						.addOption(OptionType.CHANNEL, "channel", "Mention a voice channel", true))
				.addSubcommands(new SubcommandData("clear", "Unassigns the channel for voice support"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.build.getRaw(guild, user, "/commands/moderation/supporttalk:help");
	}
}