
package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ReportChannel implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reportchannel:nopermission")).queue();
			return;
		}
		Configloader.INSTANCE.setGuildConfig(guild, "reportchannel", event.getOption("channel").getAsGuildChannel().getId());
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reportchannel:success")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("reportchannel", "0")
				.addSubcommands(new SubcommandData("set", "Sets a report channel for this server")
						.addOption(OptionType.CHANNEL, "channel", "The channel that should be used", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/reportchannel:help");
	}
}