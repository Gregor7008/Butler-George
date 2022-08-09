package slash_commands.utilities;

import java.util.List;

import base.engines.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import slash_commands.assets.SlashCommandEventHandler;

public class Modmail implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (event.getGuild() == null) {
			switch (event.getSubcommandName()) {
			case "open":
				this.openOnGuild(event);
				break;
			case "list":
				this.list(event);
				break;
			case "select":
				this.selectOnGuild(event);
				break;
			case "close":
				this.closeOnGuild(event);
				break;
			default:
				event.replyEmbeds(LanguageEngine.fetchMessage(null, null, null, "fatal")).queue();
			}
		} else {
			switch (event.getSubcommandName()) {
			case "open":
				this.openOnPrivate(event);
				break;
			case "list":
				this.list(event);
				break;
			case "select":
				this.selectOnPrivate(event);
				break;
			case "close":
				this.closeOnPrivate(event);
				break;
			default:
				event.replyEmbeds(LanguageEngine.fetchMessage(event.getGuild(), event.getUser(), null, "fatal")).queue();
			}
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("modmail", "0")
				.addSubcommands(new SubcommandData("open", "Creates a new modmail ticket"),
								new SubcommandData("list", "Sends you a list of all open modmail tickets"),
								new SubcommandData("select", "Select a modmail ticket"),
								new SubcommandData("close", "Close a modmail ticket"));
		return command;
	}

	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
	}

	private void openOnGuild(SlashCommandInteractionEvent event) {

	}


	private void selectOnGuild(SlashCommandInteractionEvent event) {

	}

	private void closeOnGuild(SlashCommandInteractionEvent event) {

	}

	private void openOnPrivate(SlashCommandInteractionEvent event) {

	}

	private void selectOnPrivate(SlashCommandInteractionEvent event) {

	}

	private void closeOnPrivate(SlashCommandInteractionEvent event) {

	}

	private void list(SlashCommandInteractionEvent event) {

	}
}