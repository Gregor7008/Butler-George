package commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface Command {
	
	public void perform(SlashCommandEvent event);
	public CommandData initialize();
	public String getHelp(Guild guild, User user);

}
