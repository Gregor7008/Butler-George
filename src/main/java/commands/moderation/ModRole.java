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

public class ModRole implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/modrole:nopermission")).queue();
			return;
		}
		Configloader.INSTANCE.setGuildConfig(guild, "modrole", event.getOption("role").getAsRole().getId());
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/modrole:success")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("modrole", "0")
				.addSubcommands(new SubcommandData("set", "Sets the role for moderators on this server")
						.addOption(OptionType.ROLE, "role", "Mention the role", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/modrole:help");
	}
}