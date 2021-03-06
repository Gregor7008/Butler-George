package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ForbiddenWords implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:nopermission")).queue();
			return;
		}
		switch (event.getSubcommandName()) {
		case "add":
			String rawadd = event.getOption("words").getAsString();
			String[] splitadd = rawadd.split(";\\s");
			for (int i = 0; i < splitadd.length; i++) {
				Configloader.INSTANCE.addGuildConfig(guild, "forbidden", splitadd[i]);
			}
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:addsuccess")).queue();
			break;
		case "remove":
			String rawremove = event.getOption("words").getAsString();
			String[] splitremove = rawremove.split(";\\s");
			for (int i = 0; i < splitremove.length; i++) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "forbidden", splitremove[i]);
			}
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:removesuccess")).queue();
			break;
		case "set":
			String rawset = event.getOption("words").getAsString();
			String finalset = rawset.replaceAll(";\\s", ";");
			Configloader.INSTANCE.setGuildConfig(guild, "forbidden", finalset);
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:setsuccess")).queue();
			break;
		case "clear":
			Configloader.INSTANCE.setGuildConfig(guild, "forbidden", "");
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/forbiddenwords:clearsuccess")).queue();
			break;
		case "list":
			String rawlist = Configloader.INSTANCE.getGuildConfig(guild, "forbidden");
			String finallist = rawlist.replaceAll(";", ", ");
			String title = AnswerEngine.ae.getTitle(guild, user, "/commands/moderation/forbiddenwords:list");
			String description = AnswerEngine.ae.getDescription(guild, user, "/commands/moderation/forbiddenwords:list");
			event.replyEmbeds(AnswerEngine.ae.buildMessage(title, description + finallist)).queue();
			break;
		default:
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:fatal")).queue() ;
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("forbiddenwords", "0")
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