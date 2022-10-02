package assets.functions;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface UserContextEventHandler {

	public void execute(UserContextInteractionEvent event);
	public CommandData initialize();
}