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

public class Setlevelchannel implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/setlevelchannel:nopermission")).queue();
			return;
		}
		Configloader.INSTANCE.setGuildConfig(guild, "levelmsgch", event.getOption("channel").getAsGuildChannel().getId());
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/setlevelchannel:success")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("setlevelchannel", "Defines a channel for level-up messages!").addOption(OptionType.CHANNEL, "channel", "Mention a text channel!", true);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/setlevelchannel:help");
	}
}