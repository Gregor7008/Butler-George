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

public class ModRole implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/modrole:nopermission").convert()).queue();
			return;
		}
		Configloader.INSTANCE.setGuildConfig(guild, "modrole", event.getOption("role").getAsRole().getId());
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/modrole:success").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("modrole", "0")
				.addSubcommands(new SubcommandData("set", "Sets the role for moderators on this server")
						.addOption(OptionType.ROLE, "role", "Mention the role", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/modrole:help");
	}
}