package context_menu_commands.assets;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface MessageContextEventHandler {

	public void execute(MessageContextInteractionEvent event);
	public CommandData initialize();
	public boolean checkBotPermissions(MessageContextInteractionEvent event);
	public boolean isAvailableTo(Member member);
	
}