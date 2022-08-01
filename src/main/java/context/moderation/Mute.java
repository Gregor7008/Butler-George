package context.moderation;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.ModController;
import components.context.UserContextEventHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Mute implements UserContextEventHandler {

	@Override
	public void execute(UserContextInteractionEvent event) {
		final Guild guild = event.getGuild();
		ConfigLoader.getMemberConfig(guild, event.getTarget()).put("muted", true);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, event.getUser(), this, "success")
						.replaceDescription("{user}", event.getTarget().getAsMention()).convert()).queue();
		ModController.run.userModCheck(guild, event.getTarget());
	}

	@Override
	public CommandData initialize() {
		CommandData context = Commands.context(Command.Type.USER, "Mute");
		return context;
	}
}