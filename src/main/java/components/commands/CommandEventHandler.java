package components.commands;

import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface CommandEventHandler {
	
	public void execute(SlashCommandInteractionEvent event);
	public CommandData initialize();
	public List<Role> additionalWhitelistedRoles(Guild guild);

}