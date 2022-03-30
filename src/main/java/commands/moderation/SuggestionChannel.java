package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
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
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/suggestionchannel:nopermission").convert()).queue();
			return;
		}
		Configloader.INSTANCE.setGuildConfig(guild, "suggest", event.getOption("channel").getAsGuildChannel().getId());
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/suggestionchannel:successset").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("suggestionchannel", "0")
				.addSubcommands(new SubcommandData("set", "Sets a suggestion channel for this server")
						.addOption(OptionType.CHANNEL, "channel", "Mention the channel that should be used", true));	
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/suggestionchannel:help");
	}
}