package slash_commands.utilities;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONArray;
import org.json.JSONObject;

import base.Bot;
import base.assets.AwaitTask;
import base.engines.LanguageEngine;
import base.engines.configs.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import slash_commands.assets.SlashCommandEventHandler;

public class Modmail implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (event.isFromGuild()) {
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
		} else {
			switch (event.getSubcommandName()) {
			case "open":
				this.open(event, event.getUser(), event.getGuild());
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

	private void selectOnGuild(SlashCommandInteractionEvent event) {
		//List tickets from guild and add select menu, select selected ticket
	}

	private void closeOnGuild(SlashCommandInteractionEvent event) {
		//If option valid, close - else list tickets from guild and add select menu, close selected ticket
	}

	private void openOnPrivate(SlashCommandInteractionEvent event) {
		final User user = event.getUser();
		SelectMenu.Builder menu = SelectMenu.create("selguild")
				.setRequiredRange(1, 1)
				.setPlaceholder("Select server");
		List<Guild> availableGuilds = Bot.INSTANCE.jda.getGuilds().stream().filter(g -> {
			try {
				g.retrieveBan(user).complete();
				return true;
			} catch (ErrorResponseException e) {}
			return g.isMember(user);
		}).toList();
		availableGuilds.forEach(g -> menu.addOption(g.getName(), g.getId(), g.getDescription()));
		Message response = event.replyEmbeds(LanguageEngine.fetchMessage(null, event.getUser(), this, "selguild")).setActionRow(menu.build()).complete().retrieveOriginal().complete();
		AwaitTask.forSelectMenuInteraction(null, user, response,
				sm -> {
					this.open(sm, user, Bot.INSTANCE.jda.getGuildById(sm.getSelectedOptions().get(0).getValue()));
				});
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
	
	private <T extends IModalCallback> void open(T event, User user, Guild guild) {
		TextInput titleInput = TextInput.create("title", "Title", TextInputStyle.SHORT)
				.setRequiredRange(3, 50)
				.setPlaceholder("Input title")
				.build();
		TextInput messageInput = TextInput.create("message", "Message", TextInputStyle.PARAGRAPH)
				.setPlaceholder("Input initial message")
				.build();
		Modal modal = Modal.create("modmailModal", "Open Modmail").addActionRows(ActionRow.of(titleInput), ActionRow.of(messageInput)).build();
		event.replyModal(modal).queue();
		AwaitTask.forModalInteraction(guild, user, event.getMessageChannel(),
				mi -> {
					JSONObject guildModmail = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("modmails");
					JSONObject userModmail = ConfigLoader.INSTANCE.getMemberConfig(guild, user).getJSONObject("modmails");
					long id = ThreadLocalRandom.current().nextLong(1000,10000);
					while (guildModmail.keySet().contains(String.valueOf(id)) || userModmail.keySet().contains(String.valueOf(id))) {
						id = ThreadLocalRandom.current().nextLong(1000,10000);
					}
					Category modmailCategory;
					if (ConfigLoader.INSTANCE.getGuildConfig(guild).getLong("modmailcategory") == 0) {
						modmailCategory = guild.createCategory("----------📝 ModMail ------------").complete();
						ConfigLoader.INSTANCE.getGuildConfig(guild).put("modmailcategory", modmailCategory.getIdLong());
					} else {
						modmailCategory = guild.getCategoryById(ConfigLoader.INSTANCE.getGuildConfig(guild).getLong("modmailcategory"));
					}
					TextChannel ticketChannel = modmailCategory.createTextChannel(user.getName() + "#" + String.valueOf(id)).complete();
					ticketChannel.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
					mi.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "openSuccess")
							.replaceDescription("{guild}", guild.getName())
							.replaceDescription("{mention}", guild.getPublicRole().getAsMention())
							.replaceDescription("{title}", mi.getValue("title").getAsString())).queue();
					String addon = "";
					if (!ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("supportroles").isEmpty()) {
						ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("supportroles").forEach(sr -> {
							ticketChannel.upsertPermissionOverride(guild.getRoleById((long) sr)).grant(Permission.VIEW_CHANNEL).queue();
						});
					} else {
						addon = LanguageEngine.getRaw(guild, user, this, "setsupportrole");
					}
					Message initialMessage = ticketChannel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "newticket")
							.replaceDescription("{user}", user.getName())
							.replaceDescription("{addon}", addon)).complete();
					guildModmail.put(ticketChannel.getId(), new JSONArray().put(user.getIdLong()).put(id).put(initialMessage.getIdLong()));
					userModmail.put(String.valueOf(id), new JSONArray().put(guild.getIdLong()).put(ticketChannel.getIdLong()));
					ticketChannel.sendMessage("**" + mi.getValue("title").getAsString() + "**\n\n" + mi.getValue("message").getAsString()).queue();
				}).addValidComponents(modal.getId()).append();
	}
}