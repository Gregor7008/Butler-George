package components.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface Command {
	
	public void perform(SlashCommandInteractionEvent event);
	public CommandData initialize();
	public Permission getMinimumPermission();

}