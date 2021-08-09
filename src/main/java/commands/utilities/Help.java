package commands.utilities;

import commands.Command;
import commands.CommandList;
import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Help implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		CommandList commandList = new CommandList();
		Command cmd;
		String help = "An Error occured";
		if ((cmd = commandList.commands.get(event.getOption("command").toString())) != null) {
			help = cmd.getHelp();
		}
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Help for:" + event.getOption("command").toString() +"-command", help));
	}

	@Override
	public CommandData initialize(Guild guild) {
		CommandData command = new CommandData("help", "Get help for a specific command").addOptions(new OptionData(OptionType.STRING, "command", "Hand over the command you need help with!").setRequired(true));
		return command;
	}

	@Override
	public String getHelp() {
		return ":face_with_symbols_over_mouth: | WHY WOULD YOU DO THIS?!";
	}

}
