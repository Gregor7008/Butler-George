package components.utilities;

import commands.Command;
import components.Developerlist;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Test implements Command{

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
		if (!Developerlist.getInstance().developers.contains(event.getMember().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("No Permission!", ":warning: | You have no permission to use this command!\n You must be a developer of this bot to use this command!"));
			return;
		}
	}
}
