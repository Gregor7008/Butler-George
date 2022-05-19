package commands.utilities;

import java.util.concurrent.TimeUnit;

import commands.Command;
import commands.CommandList;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Help implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final String input = event.getOption("command").getAsString().toLowerCase();
		CommandList commandList = new CommandList();
		Command cmd;
		String help = null;
		if (input.equals("help")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "eastereggs:2").convert()).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		if ((cmd = commandList.utilitycmds.get(input)) != null) {
			help = cmd.getHelp(guild, user);
			String[] helpsplit = help.split(";\\s+");
			event.replyEmbeds(AnswerEngine.ae.createMessage(helpsplit[0].replace("{cmd}", "`/" + input + "`"), ":bulb: | " + helpsplit[1])).queue();
			return;
		}
		if ((cmd = commandList.moderationcmds.get(input)) != null 
				&& event.getMember().getRoles().contains(guild.getRoleById(ConfigLoader.run.getGuildConfig(guild, "modrole")))) {
			help = cmd.getHelp(guild, user);
			String[] helpsplit = help.split(";\\s+");
			event.replyEmbeds(AnswerEngine.ae.createMessage(helpsplit[0].replace("{cmd}", "`/" + input + "`"), ":bulb: | " + helpsplit[1])).queue();
			return;
		}
		if ((cmd = commandList.musiccmds.get(input)) != null) {
			help = cmd.getHelp(guild, user);
			String[] helpsplit = help.split(";\\s+");
			event.replyEmbeds(AnswerEngine.ae.createMessage(helpsplit[0].replace("{cmd}", "`/" + input + "`"), ":bulb: | " + helpsplit[1])).queue();
			return;
		}
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/help:unknown").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("help", "Get help for a specific command")
				.addOptions(new OptionData(OptionType.STRING, "command", "Name the command you need help with", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return null;
	}
}