package slash_commands.assets;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommandEventHandler {
	
	public void execute(SlashCommandInteractionEvent event);
	public CommandData initialize();
	public boolean checkBotPermissions(SlashCommandInteractionEvent event);
	public boolean isAvailableTo(Member member);

}