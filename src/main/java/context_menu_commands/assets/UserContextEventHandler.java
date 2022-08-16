package context_menu_commands.assets;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface UserContextEventHandler {

	public void execute(UserContextInteractionEvent event);
	public CommandData initialize();
	public boolean checkBotPermissions(UserContextInteractionEvent event);
	public boolean isAvailableTo(Member member);
	
}