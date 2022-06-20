package components.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface Command {
	
	public void perform(SlashCommandInteractionEvent event);
	public CommandData initialize();
	public boolean canBeAccessedBy(Member member);

}