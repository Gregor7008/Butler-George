package commands.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import components.ResponseDetector;
import components.Toolbox;
import components.base.LanguageEngine;
import components.commands.CommandEventHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;

public class Embed implements CommandEventHandler {

	private Member member;
	private User user;
	private Guild guild;
	private TextChannel channel, target;
	private List<MessageEmbed> embedCache = new ArrayList<>();
	
	//TODO /embed rework for stability, reliability and more customizability (For a way to manage images, look at Levelbackground.java#>66)!
	@Override
	public void execute(SlashCommandInteractionEvent event) {
		this.member = event.getMember();
		this.user = event.getUser();
		this.guild = event.getGuild();
		this.channel = event.getTextChannel();
		if (event.getOption("channel") != null) {
			this.target = event.getOption("channel").getAsTextChannel();
			if (this.target == null) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, null, "invalid").convert()).queue();
				return;
			}
		} else {
			this.target = this.channel;
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setup").convert()).queue();
		this.startEmbedConfiguration(event.getHook().retrieveOriginal().complete(), new EmbedBuilder());
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("embed", "Creates a custom embedded message!")
									  .addOption(OptionType.CHANNEL, "channel", "The channel in which the embed should be send", false);
		command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_EMBED_LINKS))
		   	   .setGuildOnly(true);
		return command;
	}

	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
	}
	
	private void startEmbedConfiguration(Message message, EmbedBuilder eb) {
		eb = new EmbedBuilder();
		eb.setColor(LanguageEngine.color);
		message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "author").convert())
			 .setActionRow(Button.secondary("true", Emoji.fromUnicode("\u2705")),
					  	   Button.secondary("false", Emoji.fromUnicode("\u274C"))).queue();
		ResponseDetector.waitForButtonClick(guild, user, message, null,
				e -> {
					if (Boolean.parseBoolean(e.getButton().getId())) {
						eb.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
					}
					SelectMenu.Builder menu = SelectMenu.create("selvalue")
							.setPlaceholder("Select a value to configure/edit")
							.setRequiredRange(1, 1)
							.addOption("Title", "title", "The title of your embed, displayed on top of everything the embed contains")
							.addOption("Description", "description", "The description of your embed, displayed below the title")
							.addOption("Footer", "footer", "The footer of your embed, displayed right at the bottom of your embed")
							.addOption("Thumbnail", "thumbnail", "The thumbnail of your embed, displayed in the top right corner")
							.addOption("Image", "image", "The image of your embed, displayed between description and any additional fields")
							.addOption("Field", "field", "Add a new field with title and description");
					e.deferEdit().queue();
					this.continueEmbedConfiguration(e.getMessage(), menu, true, eb);
				});
	}
	
	private void continueEmbedConfiguration(Message message, SelectMenu.Builder menu, boolean firstCall, EmbedBuilder eb) {	
		List<ActionRow> actionRows = new ArrayList<>();
		actionRows.add(ActionRow.of(menu.build()));
		String messageAddon = "";
		if (!firstCall) {
			actionRows.add(ActionRow.of(Button.secondary("again", Emoji.fromUnicode("\uD83D\uDD04")),
										Button.secondary("send", Emoji.fromUnicode("\uD83D\uDCE8")),
			 						    Button.secondary("cancel", Emoji.fromUnicode("\u274C"))));
			messageAddon = LanguageEngine.getRaw(guild, user, this, "addon");
		}
		message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "selvalue").replaceDescription("{addon}", messageAddon).convert()).setActionRows(actionRows).queue();
		ResponseDetector.waitForMenuSelection(guild, user, message, menu.getId(),
				sm -> {
					switch (sm.getSelectedOptions().get(0).getValue()) {
					case "title":
						TextInput.Builder titleInput = TextInput.create("title", "Title", TextInputStyle.SHORT)
														.setPlaceholder("Type your title here")
														.setMinLength(1)
														.setMaxLength(MessageEmbed.TITLE_MAX_LENGTH)
														.setRequired(true);
						if (!eb.isEmpty()) {
							titleInput.setValue(eb.build().getTitle());
						}
						TextInput titleURLInput = TextInput.create("titleURL", "URL", TextInputStyle.SHORT)
														.setPlaceholder("For an embedded link in your title")
														.setMinLength(20)
														.setMaxLength(MessageEmbed.URL_MAX_LENGTH)
														.setRequired(false)
														.build();
						Modal modal = Modal.create("titleConfig", "Title configuration")
										   .addActionRows(ActionRow.of(titleInput.build()), ActionRow.of(titleURLInput))
										   .build();
						sm.replyModal(modal).queue(); //<- Unknown Interaction Exception here (Cause: "Has already been replied to"), when configuring second Embed
						ResponseDetector.waitForModalInput(guild, user, sm.getMessage(), modal,
								m -> {
									m.deferEdit().queue();
									String url = m.getValue("titleURL").getAsString();;
									if (url.equals("") || !Toolbox.checkURL(url)) {
										url = null;
									}
									eb.setTitle(m.getValue("title").getAsString(), url);
									Toolbox.deleteActionRows(sm.getMessage(), () -> {
										sm.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "titlesuccess")
												.replaceDescription("{title}", eb.build().getTitle()).convert()).queue();
										try {Thread.sleep(1000);} catch (InterruptedException ex) {}
										this.continueEmbedConfiguration(sm.getMessage(), menu, false);
									});
								});
						break;
					default:
						sm.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "fatal").convert()).queue();
					}
				});
		if (!firstCall) {
			List<String> buttonIDs = new ArrayList<>();
			buttonIDs.add("again");
			buttonIDs.add("send");
			buttonIDs.add("cancel");
			ResponseDetector.waitForButtonClick(guild, user, message, buttonIDs,
					bc -> {
						MessageEditCallbackAction mecAction = null;
						if (bc.getButton().getId().equals("send")) {
							embedCache.add(eb.build());
							target.sendMessageEmbeds(embedCache).queue();
							embedCache.clear();
							mecAction = bc.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "sendsuccess").convert()).setActionRows();
						} else if (bc.getButton().getId().equals("cancel")) {
							mecAction = bc.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "cancelsuccess").convert()).setActionRows();
						} else if (bc.getButton().getId().equals("again")) {
							embedCache.add(eb.build());
							bc.deferEdit().queue();
							this.startEmbedConfiguration(bc.getMessage());
						}
						if (mecAction != null) {
							if (target.getIdLong() == channel.getIdLong()) {
								mecAction.queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
							} else {
								mecAction.queue();
							}
						}			
					}, () -> {});
		}
	}
}