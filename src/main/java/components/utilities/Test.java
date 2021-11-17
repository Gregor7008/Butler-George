package components.utilities;

import java.util.ArrayList;
import java.util.List;

import commands.Command;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Test implements Command{

	public List<String> developers = new ArrayList<String>();
	
	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("test", "Testing the \"perform\" method in \"Test.java\"");
		return command;
	}
	
	@Override
	public String getHelp(Guild guild, User user) {
		return "This command is only for developers!";
	}
	
	@Override
	public void perform(SlashCommandEvent event) {
		developers.add("475974084937646080");
		developers.add("806631059667025940");
		developers.add("407547342628323338");
		
		if (!developers.contains(event.getMember().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("No Permission!", ":warning: | You have no permission to use this command!\n You must be a developer of this bot to use this command!"));
			return;
		}
	}
}
