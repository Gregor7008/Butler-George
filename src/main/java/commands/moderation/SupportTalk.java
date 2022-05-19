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
		ConfigLoader.run.setGuildConfig(guild, "supporttalk", event.getOption("channel").getAsGuildChannel().getId());
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/supporttalk:success").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("supporttalk", "0")
				.addSubcommands(new SubcommandData("set", "Sets the support talk of this server")
						.addOption(OptionType.CHANNEL, "channel", "Mention a voice channel", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/supporttalk:help");
	}
}