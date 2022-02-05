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

public class SetCustomChannelRole implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/setcustomchannelrole:nopermission")).queue();
			return;
		}
		Configloader.INSTANCE.setGuildConfig(guild, "ccrole", event.getOption("role").getAsRole().getId());
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/setcustomchannelrole:success")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("setcustomchannelrole", "Sets the role that should be able to create custom user channels").addOption(OptionType.ROLE, "role", "The wanted role", true);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/setcustomchannelrole:help");
	}
}