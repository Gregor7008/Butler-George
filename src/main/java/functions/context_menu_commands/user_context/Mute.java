package functions.context_menu_commands.user_context;

import assets.functions.UserContextEventHandler;
import engines.base.LanguageEngine;
import engines.data.ConfigLoader;
import engines.functions.ModController;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Mute implements UserContextEventHandler {

	@Override
	public void execute(UserContextInteractionEvent event) {
		final Guild guild = event.getGuild();
		ConfigLoader.INSTANCE.getMemberConfig(guild, event.getTarget()).put("muted", true);
		event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, event.getUser(), this, "success")
						.replaceDescription("{user}", event.getTarget().getAsMention())).setEphemeral(true).queue();
		ModController.RUN.userModCheck(guild, event.getTarget());
	}

	@Override
	public CommandData initialize() {
		CommandData context = Commands.context(Command.Type.USER, "Mute");
		context.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)).setGuildOnly(true);
		return context;
	}
}