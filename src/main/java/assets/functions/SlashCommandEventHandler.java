package assets.functions;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommandEventHandler {
	
	public void execute(SlashCommandInteractionEvent event);
	public CommandData initialize();
}