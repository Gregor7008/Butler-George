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
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Join2Create implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if(!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/join2create:nopermission")).queue();
			return;
		}
		Configloader.INSTANCE.setGuildConfig(guild, "join2create", event.getOption("channel").getAsGuildChannel().getId());
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/join2create:success")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("join2create", "Set the join2create channel of the server!").addOptions(new OptionData(OptionType.CHANNEL, "channel", "Enter the voicechannel that should be used as the join2create channel!").setRequired(true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/join2create:help");
	}
}