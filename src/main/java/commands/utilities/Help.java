package commands.utilities;

import commands.Command;
import commands.CommandList;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Help implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		CommandList commandList = new CommandList();
		Command cmd;
		String help = null;
		if ((cmd = commandList.utilitycmds.get(event.getOption("command").getAsString())) != null) {
			help = cmd.getHelp(event.getGuild(), event.getUser());
			String[] helpsplit = help.split(";\\s+");
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage(helpsplit[0], helpsplit[1])).queue();
			return;
		}
		if ((cmd = commandList.moderationcmds.get(event.getOption("command").getAsString())) != null 
				&& event.getMember().getRoles().contains(event.getGuild().getRoleById(Configloader.INSTANCE.getGuildConfig(event.getGuild(), "modrole")))) {
			help = cmd.getHelp(event.getGuild(), event.getUser());
			String[] helpsplit = help.split(";\\s+");
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage(helpsplit[0], helpsplit[1])).queue();
			return;
		}
		if ((cmd = commandList.musiccmds.get(event.getOption("command").getAsString())) != null) {
			help = cmd.getHelp(event.getGuild(), event.getUser());
			String[] helpsplit = help.split(";\\s+");
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage(helpsplit[0], helpsplit[1])).queue();
			return;
		}
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(), "/commands/utilities/help:error")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("help", "Get help for a specific command").addOptions(new OptionData(OptionType.STRING, "command", "Hand over the command you need help with!").setRequired(true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/help:help");
	}

}
