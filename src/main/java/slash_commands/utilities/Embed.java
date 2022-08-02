package slash_commands.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import base.assets.AwaitTask;
import base.engines.LanguageEngine;
import base.engines.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
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
import slash_commands.assets.CommandEventHandler;

public class Embed implements CommandEventHandler {

	private Member member;
	private User user;
	private Guild guild;
	private MessageChannel channel, target;
	private List<MessageEmbed> embedCache = new ArrayList<>();
	
//	TODO /embed rework for stability, reliability and more customizability (For a way to manage images, look at Levelbackground.java#>66)!
	@Override
	public void execute(SlashCommandInteractionEvent event) {
		this.member = event.getMember();
		this.user = event.getUser();
		this.guild = event.getGuild();
		this.channel = event.getMessageChannel();
		if (event.getOption("channel") != null) {
			this.target = event.getOption("channel").getAsChannel().asGuildMessageChannel();
			if (this.target == null) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, null, "invalid").convert()).queue();
				return;
			}
		} else {
			this.target = this.channel;
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setup").convert()).queue();
		this.startEmbedConfiguration(event.getHook().retrieveOriginal().complete());
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
	
	private void startEmbedConfiguration(Message message) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(LanguageEngine.color);
		message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "author").convert())
			 .setActionRow(Button.secondary("true", Emoji.fromUnicode("\u2705")),
					  	   Button.secondary("false", Emoji.fromUnicode("\u274C"))).queue();
		AwaitTask.forButtonInteraction(guild, user, message,
				e -> {
					if (Boolean.parseBoolean(e.getButton().getId())) {
						eb.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
					}
					SelectMenu menu = SelectMenu.create("selvalue")
							.setPlaceholder("Select a value to configure/edit")
							.setRequiredRange(1, 1)
							.addOption("Title", "title", "The title of your embed, displayed on top of everything the embed contains")
							.addOption("Description", "description", "The description of your embed, displayed below the title")
							.addOption("Footer", "footer", "The footer of your embed, displayed right at the bottom of your embed")
							.addOption("Thumbnail", "thumbnail", "The thumbnail of your embed, displayed in the top right corner")
							.addOption("Image", "image", "The image of your embed, displayed between description and any additional fields")
							.addOption("Field", "field", "Configure additional fields of your embed")
							.addOption("Attachements", "attachements", "Add attachements to the message containing the embed")
							.build();
					e.deferEdit().queue();
					this.continueEmbedConfiguration(e.getMessage(), menu, true, eb);
				}).append();
	}
	
	private void continueEmbedConfiguration(Message message, SelectMenu menu, boolean firstCall, EmbedBuilder eb) {	
		List<ActionRow> actionRows = new ArrayList<>();
		actionRows.add(ActionRow.of(menu));
		String messageAddon = "";
		if (!firstCall) {
			actionRows.add(ActionRow.of(Button.secondary("again", Emoji.fromUnicode("\uD83D\uDD04")),
										Button.secondary("send", Emoji.fromUnicode("\uD83D\uDCE8")),
			 						    Button.secondary("cancel", Emoji.fromUnicode("\u274C"))));
			messageAddon = LanguageEngine.getRaw(guild, user, this, "addon");
		}
		message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "selvalue").replaceDescription("{addon}", messageAddon).convert()).setActionRows(actionRows).queue();
		AtomicReference<AwaitTask<ButtonInteractionEvent>> buttonInteractionTask = new AtomicReference<>(null);
		AwaitTask<SelectMenuInteractionEvent> selectionMenuTask = AwaitTask.forSelectMenuInteraction(guild, user, message,
				s -> {
					if (buttonInteractionTask.get() != null) {
						buttonInteractionTask.get().cancel();
					}
					switch (s.getSelectedOptions().get(0).getValue()) {
					case "title":
						this.configureTitle(menu, eb, s);
						break;
					case "description":
						this.configureDescription(menu, eb, s);
						break;
					case "footer":
						this.configureFooter(menu, eb, s);
						break;
					case "thumbnail":
						this.configureThumbnail(menu, eb, s);
						break;
					case "image":
						this.configureImage(menu, eb, s);
						break;
					case "field":
						this.configureFields(menu, eb, s);
						break;
					case "attachements":
						this.configureAttachements(menu, eb, s);
						break;
					default:
						s.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "fatal").convert()).queue();
					}
				}).addValidComponents(menu.getId()).append();
		if (!firstCall) {
			buttonInteractionTask.set(AwaitTask.forButtonInteraction(guild, user, message, null,
					b -> {
						selectionMenuTask.cancel();
						MessageEditCallbackAction mecAction = null;
						if (b.getButton().getId().equals("send")) {
							embedCache.add(eb.build());
							target.sendMessageEmbeds(embedCache).queue();
							embedCache.clear();
							mecAction = b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "sendsuccess").convert()).setActionRows();
						} else if (b.getButton().getId().equals("cancel")) {
							mecAction = b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "cancelsuccess").convert()).setActionRows();
						} else if (b.getButton().getId().equals("again")) {
							embedCache.add(eb.build());
							b.deferEdit().queue();
							this.startEmbedConfiguration(b.getMessage());
						}
						if (mecAction != null) {
							if (target.getIdLong() == channel.getIdLong()) {
								mecAction.queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
							} else {
								mecAction.queue();
							}
						}
					},
					() -> {}).append());
		}
	}
	
	private void configureTitle(SelectMenu menu, EmbedBuilder eb, SelectMenuInteractionEvent event) {
		TextInput.Builder titleInput = TextInput.create("title", "Title", TextInputStyle.SHORT)
				.setPlaceholder("Input title")
				.setMinLength(1)
				.setMaxLength(MessageEmbed.TITLE_MAX_LENGTH)
				.setRequired(true);
		String current = eb.build().getTitle();
		if (current != null) {
			titleInput.setValue(current);
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
		event.replyModal(modal).queue();
		AwaitTask.forModalInteraction(guild, user, event.getChannel(),
				m -> {
					m.deferEdit().queue();
					String url = m.getValue("titleURL").getAsString();;
					if (url.equals("") || !Toolbox.checkURL(url)) {
						url = null;
					}
					String input = m.getValue("title").getAsString();
					eb.setTitle(input, url);
					event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "titlesuccess")
							.replaceDescription("{title}", input).convert()).setActionRows().queue();
					try {Thread.sleep(1000);} catch (InterruptedException ex) {}
					this.continueEmbedConfiguration(event.getMessage(), menu, false, eb);
				}).addValidComponents(modal.getId()).append();
	}
	
	private void configureDescription(SelectMenu menu, EmbedBuilder eb, SelectMenuInteractionEvent event) {
		TextInput.Builder descriptionInput = TextInput.create("description", "Description", TextInputStyle.SHORT)
				.setPlaceholder("Input description")
				.setMinLength(1)
				.setMaxLength(MessageEmbed.DESCRIPTION_MAX_LENGTH)
				.setRequired(true);
		String current = eb.build().getDescription();
		if (current != null) {
			descriptionInput.setValue(current);
		}
		Modal modal = Modal.create("descriptionConfig", "Description configuration")
				.addActionRows(ActionRow.of(descriptionInput.build()))
				.build();
		event.replyModal(modal).queue();
		AwaitTask.forModalInteraction(guild, user, event.getChannel(),
				m -> {
					String input = m.getValue("description").getAsString();
					eb.setDescription(input);
					event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "descriptionsuccess")
							.replaceDescription("{description}", input).convert()).setActionRows().queue();
					try {Thread.sleep(1000);} catch (InterruptedException ex) {}
					this.continueEmbedConfiguration(event.getMessage(), menu, false, eb);
				}).addValidComponents(modal.getId()).append();
	}
	
	private void configureFooter(SelectMenu menu, EmbedBuilder eb, SelectMenuInteractionEvent event) {
		TextInput.Builder footerInput = TextInput.create("footer", "Footer", TextInputStyle.SHORT)
				.setPlaceholder("Input footer")
				.setMinLength(1)
				.setMaxLength(MessageEmbed.TEXT_MAX_LENGTH)
				.setRequired(true);
		Footer current = eb.build().getFooter();
		if (current != null) {
			footerInput.setValue(current.getText());
		}
		Modal modal = Modal.create("descriptionConfig", "Description configuration")
				.addActionRows(ActionRow.of(footerInput.build()))
				.build();
		event.replyModal(modal).queue();
		AwaitTask.forModalInteraction(guild, user, event.getChannel(),
				m -> {
					String input = m.getValue("description").getAsString();
					eb.setDescription(input);
					event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "descriptionsuccess")
							.replaceDescription("{description}", input).convert()).setActionRows().queue();
					try {Thread.sleep(1000);} catch (InterruptedException ex) {}
					this.continueEmbedConfiguration(event.getMessage(), menu, false, eb);
				}).addValidComponents(modal.getId()).append();
	}
	
	private void configureThumbnail(SelectMenu menu, EmbedBuilder eb, SelectMenuInteractionEvent event) {
		
	}
	
	private void configureImage(SelectMenu menu, EmbedBuilder eb, SelectMenuInteractionEvent event) {
		
	}
	
	private void configureFields(SelectMenu menu, EmbedBuilder eb, SelectMenuInteractionEvent event) {
		
	}
	
	private void configureAttachements(SelectMenu menu, EmbedBuilder eb, SelectMenuInteractionEvent event) {
		
	}
}