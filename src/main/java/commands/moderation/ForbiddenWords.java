package commands.moderation;

import org.json.JSONArray;

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
		String rawWords = event.getOption("words").getAsString();
		String[] words = rawWords.split(";\\s");
		JSONArray forbiddenwords = ConfigLoader.run.getGuildConfig(guild).getJSONArray("forbiddenwords");
		switch (event.getSubcommandName()) {
		case "add":
			for (int i = 0; i < words.length; i++) {
				forbiddenwords.put(words[i]);
			}
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:addsuccess").convert()).queue();
			break;
		case "remove":
			for (int i = 0; i < words.length; i++) {
				ConfigLoader.run.removeValueFromArray(forbiddenwords, words[i]);
			}
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:removesuccess").convert()).queue();
			break;
		case "set":
			forbiddenwords.clear();
			for (int i = 0; i < words.length; i++) {
				forbiddenwords.put(words[i]);
			}
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:setsuccess").convert()).queue();
			break;
		case "clear":
			forbiddenwords.clear();
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:clearsuccess").convert()).queue();
			break;
		case "list":
			if (forbiddenwords.isEmpty()) {
				event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:none").convert()).queue();
				break;
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < words.length; i++) {
				sb.append(words[i]);
				if (i + 1 != words.length) {
					sb.append(", ");
				}
			}
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:list").replaceDescription("{list}", sb.toString()).convert()).queue();
			break;
		default:
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "general:fatal").convert()).queue() ;
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
		return AnswerEngine.run.getRaw(guild, user, "/commands/moderation/forbiddenwords:help");
	}
}