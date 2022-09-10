package functions.slash_commands.support;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.base.AwaitTask;
import assets.functions.SlashCommandEventHandler;
import base.Bot;
import engines.base.LanguageEngine;
import engines.configs.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class Modmail implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (event.isFromGuild()) {
			event.getUser().openPrivateChannel().complete().sendMessageEmbeds(LanguageEngine.fetchMessage(event.getGuild(), event.getUser(), this, "testing")).queue(
					message -> {
						message.delete().queueAfter(1, TimeUnit.SECONDS);
						switch (event.getSubcommandName()) {
						case "open":
							this.open(event, event.getUser(), event.getGuild());
							break;
						case "list":
							this.listOnGuild(event);
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
					},
					error -> {
						event.replyEmbeds(LanguageEngine.fetchMessage(event.getGuild(), event.getUser(), this, "testerror")).queue();
						return;
					});
		} else {
			switch (event.getSubcommandName()) {
			case "open":
				this.openOnPrivate(event);
				break;
			case "list":
				this.listOnPrivate(event);
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
								new SubcommandData("select", "Select a modmail ticket").addOption(OptionType.INTEGER, "ticketid", "The ID of the ticket you want to select", false),
								new SubcommandData("close", "Close a modmail ticket").addOption(OptionType.INTEGER, "ticketid", "The ID of the ticket you want to close", false));
		return command;
	}

	private void openOnPrivate(SlashCommandInteractionEvent event) {
		final User user = event.getUser();
		SelectMenu.Builder menu = SelectMenu.create("selguild")
				.setRequiredRange(1, 1)
				.setPlaceholder("Select server");
		List<Guild> availableGuilds = event.getJDA().getGuilds().stream().filter(g -> {
			try {
				g.retrieveBan(user).complete();
				return true;
			} catch (ErrorResponseException e) {}
			return g.isMember(user);
		}).toList();
		availableGuilds.forEach(g -> menu.addOption(g.getName(), g.getId(), g.getDescription()));
		Message response = event.replyEmbeds(LanguageEngine.fetchMessage(null, event.getUser(), this, "selguild"))
				.setActionRow(menu.build())
				.complete().retrieveOriginal().complete();
		AwaitTask.forSelectMenuInteraction(null, user, response,
				sm -> {
					this.open(sm, user, event.getJDA().getGuildById(sm.getSelectedOptions().get(0).getValue()));
				}).addValidComponents(menu.getId()).append();
	}

	private void selectOnPrivate(SlashCommandInteractionEvent event) {
		final User user = event.getUser();
		this.ticketSelection(event, user, null,
				(newEvent, selection) -> {
					final Guild finalGuild = newEvent.getJDA().getGuildById(selection[0]);
					ConfigLoader.INSTANCE.getUserConfig(user).put("selected_ticket", new JSONArray().put(finalGuild.getIdLong()).put(Long.valueOf(selection[1])));
					MessageEmbed embed = LanguageEngine.fetchMessage(finalGuild, user, this, "selectSuccess")
							.replaceDescription("{title}", ConfigLoader.INSTANCE
									.getMemberConfig(finalGuild, user)
									.getJSONObject("modmails")
									.getJSONArray(selection[1])
									.getString(1))
							.replaceDescription("{guild}", finalGuild.getName());
					if (newEvent instanceof SlashCommandInteractionEvent) {
						event.replyEmbeds(embed).setComponents().queue();
					} else if (newEvent instanceof SelectMenuInteractionEvent){
						SelectMenuInteractionEvent castedEvent = (SelectMenuInteractionEvent) newEvent;
						castedEvent.editMessageEmbeds(embed).setComponents().queue();
					}
				});
	}

	private void selectOnGuild(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		this.ticketSelection(event, user, guild,
				(newEvent, selection) -> {
					final Guild finalGuild = newEvent.getJDA().getGuildById(selection[0]);
					ConfigLoader.INSTANCE.getUserConfig(user).put("selected_ticket", new JSONArray().put(guild.getIdLong()).put(Long.valueOf(selection[1])));
					MessageEmbed embed = LanguageEngine.fetchMessage(finalGuild, user, this, "selectSuccess")
							.replaceDescription("{title}", ConfigLoader.INSTANCE
									.getMemberConfig(finalGuild, user)
									.getJSONObject("modmails")
									.getJSONArray(selection[1])
									.getString(1))
							.replaceDescription("{guild}", finalGuild.getName());
					if (newEvent instanceof SlashCommandInteractionEvent) {
						event.replyEmbeds(embed).setComponents().queue();
					} else if (newEvent instanceof SelectMenuInteractionEvent){
						SelectMenuInteractionEvent castedEvent = (SelectMenuInteractionEvent) newEvent;
						castedEvent.editMessageEmbeds(embed).setComponents().queue();
					}
				});
	}
	
	private void listOnGuild(SlashCommandInteractionEvent event) {
		event.replyEmbeds(LanguageEngine.fetchMessage(event.getGuild(), event.getUser(), this, "list")
				.replaceDescription("{tickets}", this.list(event.getUser(), event.getGuild(), null))).queue();
	}

	private void listOnPrivate(SlashCommandInteractionEvent event) {
		event.replyEmbeds(LanguageEngine.fetchMessage(null, event.getUser(), this, "list")
				.replaceDescription("{tickets}", this.list(event.getUser(), null, null))).queue();
	}

	private void closeOnPrivate(SlashCommandInteractionEvent event) {
		final User user = event.getUser();
		this.ticketSelection(event, user, null,
				(newEvent, selection) -> {
					final Guild finalGuild = newEvent.getJDA().getGuildById(selection[0]);
					this.close(newEvent, finalGuild, user, selection[1]);
				});
	}

	private void closeOnGuild(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		this.ticketSelection(event, user, guild,
				(newEvent, selection) -> {
					final Guild finalGuild = newEvent.getJDA().getGuildById(selection[0]);
					this.close(newEvent, finalGuild, user, selection[1]);
				});
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
					TextChannel ticketChannel = modmailCategory.createTextChannel(user.getName() + "-" + String.valueOf(id)).complete();
					ticketChannel.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();

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
							.replaceDescription("{mention}", guild.getPublicRole().getAsMention())
							.replaceDescription("{addon}", addon))
							.setActionRow(Button.secondary(String.valueOf(ticketChannel.getIdLong() + guild.getIdLong()) + "_close", Emoji.fromUnicode("\uD83D\uDD12"))).complete();
					ticketChannel.sendMessage("**" + mi.getValue("title").getAsString() + "**\n\n" + mi.getValue("message").getAsString()).queue();
					guildModmail.put(ticketChannel.getId(), new JSONArray().put(user.getIdLong()).put(id).put(initialMessage.getIdLong()));
					userModmail.put(String.valueOf(id), new JSONArray().put(ticketChannel.getIdLong()).put(mi.getValue("title").getAsString()));
					ConfigLoader.INSTANCE.getUserConfig(user).put("selected_ticket", new JSONArray().put(guild.getIdLong()).put(id));
					if (event instanceof SelectMenuInteractionEvent) {
						mi.editMessageEmbeds(
								LanguageEngine.fetchMessage(guild, user, this, "openSuccess")
											  .replaceDescription("{guild}", guild.getName())
											  .replaceDescription("{title}", mi.getValue("title").getAsString()),
								LanguageEngine.fetchMessage(guild, user, this, "selectSuccess")
											  .replaceDescription("{guild}", guild.getName())
								  		      .replaceDescription("{title}", mi.getValue("title").getAsString())).setComponents().queue();
					} else if (event instanceof SlashCommandInteractionEvent) {
						mi.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "openSuccess")
								.replaceDescription("{guild}", guild.getName())
								.replaceDescription("{title}", mi.getValue("title").getAsString())).setComponents().queue();
						user.openPrivateChannel().complete().sendMessageEmbeds(
								LanguageEngine.fetchMessage(guild, user, this, "selectSuccess")
											   .replaceDescription("{guild}", guild.getName())
								  			   .replaceDescription("{title}", mi.getValue("title").getAsString())).queue();
					}
				}).addValidComponents(modal.getId()).append();
	}
	
	private void ticketSelection(SlashCommandInteractionEvent event, User user, @Nullable Guild guild, BiConsumer<GenericInteractionCreateEvent, String[]> onSelection) {
		JSONObject userConfig = ConfigLoader.INSTANCE.getUserConfig(user);
		SelectMenu.Builder menu = SelectMenu.create("selticket")
				.setPlaceholder("Select ticket")
				.setRequiredRange(1, 1);
		String list = this.list(user, guild, menu);
		if (list.equals("")) {
			if (guild != null) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "noticketsguild")).queue();
			} else {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "noticketsprivate")).queue();
			}
			return;
		}
		if (event.getOption("ticketid") != null) {
			List<String> validkeys = new ArrayList<>();
			if (guild == null) {
				validkeys = userConfig.keySet().stream().filter(k -> {
					try {
						Guild guildFromKey = event.getJDA().getGuildById(k);
						if (guildFromKey != null) {
							JSONObject modmailsOpt = ConfigLoader.INSTANCE.getMemberConfig(guildFromKey, user);
							return modmailsOpt.keySet().contains(event.getOption("ticketid").getAsString());
						}
					} catch (NumberFormatException e) {}
					return false;
				}).toList();
			} else {
				validkeys.add(guild.getId());
			}
			if (!validkeys.isEmpty()) {
				String[] selectedValue = new String[] {validkeys.get(0), event.getOption("ticketid").getAsString()};
				onSelection.accept(event, selectedValue);
				return;
			}
		}
		Message response = event.replyEmbeds(LanguageEngine.fetchMessage(null, user, this, "selticket")
				.replaceDescription("{tickets}", list))
				.setActionRow(menu.build())
				.complete().retrieveOriginal().complete();
		AwaitTask.forSelectMenuInteraction(null, user, response,
				sm -> {
					String[] selectedValue = sm.getSelectedOptions().get(0).getValue().split(";");
					onSelection.accept(sm, selectedValue);
				}).addValidComponents(menu.getId()).append();
	}
	
	private String list(User user, @Nullable Guild guild, @Nullable SelectMenu.Builder menu) {
		JSONObject userConfig = ConfigLoader.INSTANCE.getUserConfig(user);
		List<String> validkeys = userConfig.keySet().stream().filter(k -> {
			try {
				Guild guildFromKey = Bot.INSTANCE.jda.getGuildById(k);
				return guildFromKey != null;
			} catch (NumberFormatException e) {
				return false;
			}
		}).toList();
		if (guild != null) {
			validkeys.clear();
			validkeys.add(guild.getId());
		}
		StringBuilder sB = new StringBuilder();
		for (int g = 0; g < validkeys.size(); g++) {
			String key = validkeys.get(g);
			JSONObject modmailObject = userConfig.getJSONObject(key).getJSONObject("modmails");
			if (!modmailObject.isEmpty()) {
				if (guild != null) {
					sB.append("**" + Bot.INSTANCE.jda.getGuildById(key).getName() + "**\n");
				} else {
					sB.append("\n");
				}
				List<String[]> tickets = new ArrayList<>();
				modmailObject.keySet().forEach(ticket -> tickets.add(new String[] {ticket, modmailObject.getJSONArray(ticket).getString(1)}));
				for (int i = 0; i < tickets.size(); i++) {
					String[] ticket = tickets.get(i);
					if (menu != null) {
						menu.addOption(ticket[0], key + ";" + ticket[0], ticket[1]);
					}
					sB.append("`#" + ticket[0] + "` " + ticket[1] + "\n");
				}
			}
		}
		return sB.toString();
	}
	
	public void close(GenericInteractionCreateEvent event, Guild ticketGuild, User ticketUser, String ticketID) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		JSONArray selectedTicket = ConfigLoader.INSTANCE.getUserConfig(ticketUser).getJSONArray("selected_ticket");
		JSONArray userTicketData = ConfigLoader.INSTANCE.getMemberConfig(ticketGuild, ticketUser).getJSONObject("modmails").getJSONArray(ticketID);
		String ticketTitle = userTicketData.getString(1);
		TextChannel ticketChannel = ticketGuild.getTextChannelById(userTicketData.getLong(0));
		String buttonCriteria = String.valueOf(ticketChannel.getIdLong() + ticketGuild.getIdLong());
		if (selectedTicket.getLong(0) == Long.valueOf(ticketID) && selectedTicket.getLong(1) == Long.valueOf(ticketID)) {
			selectedTicket.clear();
		};
		if (event.isFromGuild()) {
			ticketUser.openPrivateChannel().complete().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "awaitConfirm")
					.replaceDescription("{guild}", ticketGuild.getName())
					.replaceDescription("{title}", ticketTitle)).queue();
		}
		if (event instanceof ButtonInteractionEvent) {
			ButtonInteractionEvent castedEvent = (ButtonInteractionEvent) event;
			castedEvent.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "closeSuccessAdmin")
					.replaceDescription("{guild}", ticketGuild.getName())
					.replaceDescription("{title}", ticketTitle)).queue();
			Bot.INSTANCE.getTimer().schedule(new TimerTask() {
				@Override
				public void run() {
					castedEvent.getChannel().delete().queue();
				}
			}, TimeUnit.SECONDS.toMillis(5));
			return;
		} else if (event instanceof SlashCommandInteractionEvent) {
			SlashCommandInteractionEvent castedEvent = (SlashCommandInteractionEvent) event;
			castedEvent.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "awaitConfirm")
					.replaceDescription("{guild}", ticketGuild.getName())
					.replaceDescription("{title}", ticketTitle)).queue();

		} else if (event instanceof SelectMenuInteractionEvent) {
			SelectMenuInteractionEvent castedEvent = (SelectMenuInteractionEvent) event;
			castedEvent.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "awaitConfirm")
					.replaceDescription("{guild}", ticketGuild.getName())
					.replaceDescription("{title}", ticketTitle)).queue();
		}
		ticketChannel.sendMessageEmbeds(LanguageEngine.fetchMessage(null, null, this, "closeConfirmation")
				.replaceDescription("{user}", ticketUser.getName()))
		.setActionRow(Button.secondary(buttonCriteria + "_confirmclose", Emoji.fromUnicode("\u2705")),
				Button.secondary(String.valueOf(buttonCriteria + "_denyclose"), Emoji.fromUnicode("\u274C"))).queue();
		//TODO Implement saving of user feedback message id for later use
	}
	
	public void confirmclose(GenericInteractionCreateEvent event, Guild guild, User user, String ticketID) {
		//TODO Implement confirm close function
	}
}