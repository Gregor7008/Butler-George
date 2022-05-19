package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ForbiddenWords implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		switch (event.getSubcommandName()) {
		case "add":
			String rawadd = event.getOption("words").getAsString();
			String[] splitadd = rawadd.split(";\\s");
			int e = 0;
			if (ConfigLoader.cfl.getGuildConfig(guild, "forbidden").equals("\\//\\//\\//")) {
				ConfigLoader.cfl.setGuildConfig(guild, "forbidden", splitadd[0]);
				e++;
			}
			for (int i = e; i < splitadd.length; i++) {
				ConfigLoader.cfl.addGuildConfig(guild, "forbidden", splitadd[i]);
			}
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:addsuccess").convert()).queue();
			break;
		case "remove":
			String rawremove = event.getOption("words").getAsString();
			String[] splitremove = rawremove.split(";\\s");
			for (int i = 0; i < splitremove.length; i++) {
				ConfigLoader.cfl.removeGuildConfig(guild, "forbidden", splitremove[i]);
			}
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:removesuccess").convert()).queue();
			break;
		case "set":
			String rawset = event.getOption("words").getAsString();
			String finalset = rawset.replaceAll(";\\s", ";");
			ConfigLoader.cfl.setGuildConfig(guild, "forbidden", finalset);
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:setsuccess").convert()).queue();
			break;
		case "clear":
			ConfigLoader.cfl.setGuildConfig(guild, "forbidden", "");
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:clearsuccess").convert()).queue();
			break;
		case "list":
			if (ConfigLoader.cfl.getGuildConfig(guild, "forbidden").equals("\\//\\//\\//")) {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:none").convert()).queue();
				break;
			}
			String rawlist = ConfigLoader.cfl.getGuildConfig(guild, "forbidden");
			String finallist = rawlist.replaceAll(";", ", ");
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:list").replaceDescription("{list}", finallist).convert()).queue();
			break;
		default:
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:fatal").convert()).queue() ;
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("forbiddenwords", "0")
									.addSubcommands(new SubcommandData("add", "Adds words to the list of forbidden words")
										.addOptions(new OptionData(OptionType.STRING, "words", "The words that should be added", true)))
									.addSubcommands(new SubcommandData("remove", "Removes words from the list of forbidden words")
										.addOptions(new OptionData(OptionType.STRING, "words", "The words that should be removed", true)))
									.addSubcommands(new SubcommandData("set", "Sets the list of forbidden words")
										.addOptions(new OptionData(OptionType.STRING, "words", "The words that should be set", true)))
									.addSubcommands(new SubcommandData("clear", "Clears the list of forbidden words"))
									.addSubcommands(new SubcommandData("list", "Displays the current list of forbidden words"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/forbiddenwords:help");
	}
}