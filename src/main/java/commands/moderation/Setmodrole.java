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

public class Setmodrole implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(), "/commands/moderation/setmodrole:nopermission")).queue();
			return;
		}
		Configloader.INSTANCE.setGuildConfig(event.getGuild(), "modrole", event.getOption("role").getAsRole().getId());
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(), "/commands/moderation/setmodrole:success")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("setmodrole", "Define the moderation role").addOption(OptionType.ROLE, "role", "Mention the role");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/setmodrole:help");
	}

}
