package components.context;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface MessageContextEventHandler {

	public void execute(MessageContextInteractionEvent event);
	public CommandData initialize();
	
}