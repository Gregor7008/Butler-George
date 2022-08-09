package context_menu_commands.moderation;

import base.engines.LanguageEngine;
import base.engines.configs.ConfigLoader;
import context_menu_commands.assets.UserContextEventHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import slash_commands.engines.ModController;

public class Mute implements UserContextEventHandler {

	@Override
	public void execute(UserContextInteractionEvent event) {
		final Guild guild = event.getGuild();
		ConfigLoader.INSTANCE.getMemberConfig(guild, event.getTarget()).put("muted", true);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, event.getUser(), this, "success")
						.replaceDescription("{user}", event.getTarget().getAsMention())).setEphemeral(true).queue();
		ModController.RUN.userModCheck(guild, event.getTarget());
	}

	@Override
	public CommandData initialize() {
		CommandData context = Commands.context(Command.Type.USER, "Mute").setGuildOnly(true);
		return context;
	}
}