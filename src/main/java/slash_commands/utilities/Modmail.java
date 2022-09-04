package slash_commands.utilities;

import base.engines.LanguageEngine;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import slash_commands.assets.SlashCommandEventHandler;

public class Modmail implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (event.isFromGuild()) {
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

	private void openOnGuild(SlashCommandInteractionEvent event) {
		//Reply with modal and ask for title and description, open ticket on valid input
	}

	private void selectOnGuild(SlashCommandInteractionEvent event) {
		//List tickets from guild and add select menu, select selected ticket
	}

	private void closeOnGuild(SlashCommandInteractionEvent event) {
		//If option valid, close - else list tickets from guild and add select menu, close selected ticket
	}

	private void openOnPrivate(SlashCommandInteractionEvent event) {
		//Reply with select menu, choose guild, then as in "openOnguild"
	}

	private void selectOnPrivate(SlashCommandInteractionEvent event) {
		//List all tickets by guild and add select menu, select selected ticket
	}

	private void closeOnPrivate(SlashCommandInteractionEvent event) {
		//If option valid, close - else list tickets by guild and add select menu, close selected ticket
	}

	private void list(SlashCommandInteractionEvent event) {
		//List all tickets by guild and add select menu, reply with button row for selection or closing the ticket
	}
}