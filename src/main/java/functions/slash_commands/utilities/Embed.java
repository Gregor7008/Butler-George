package functions.slash_commands.utilities;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import assets.base.AwaitTask;
import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.base.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;

public class Embed implements SlashCommandEventHandler {

	private Member member;
	private User user;
	private Guild guild;
	private GuildMessageChannel channel, target;
	private List<MessageEmbed> embedCache = new ArrayList<>();
	private HashMap<String, InputStream> attachments = new HashMap<>();
	
	@Override
	public void execute(SlashCommandInteractionEvent event) {
		this.member = event.getMember();
		this.user = event.getUser();
		this.guild = event.getGuild();
		this.channel = event.getGuildChannel().asStandardGuildMessageChannel();
		if (event.getOption("channel") != null) {
			this.target = event.getOption("channel").getAsChannel().asGuildMessageChannel();
		} else {
			this.target = this.channel;
		}
		if (!member.hasPermission(target, Permission.MESSAGE_SEND)) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "invalid")).queue();
			return;
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setup")).queue();
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
	
	private void startEmbedConfiguration(Message message) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(LanguageEngine.EMBED_DEFAULT_COLOR);
		message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "author"))
			 .setActionRow(Button.secondary("true", Emoji.fromUnicode("\u2705")),
					  	   Button.secondary("false", Emoji.fromUnicode("\u274C"))).queue();
		AwaitTask.forButtonInteraction(guild, user, message,
				e -> {
					if (Boolean.parseBoolean(e.getButton().getId())) {
						eb.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
					}
					StringSelectMenu menu = StringSelectMenu.create("selvalue")
							.setPlaceholder("Select a value to configure/edit")
							.setRequiredRange(1, 1)
							.addOption("Title", "title", "The title of your embed, displayed on top of everything the embed contains")
							.addOption("Description", "description", "The description of your embed, displayed below the title")
							.addOption("Footer", "footer", "The footer of your embed, displayed right at the bottom of your embed")
							.addOption("Thumbnail", "thumbnail", "The thumbnail of your embed, displayed in the top right corner")
							.addOption("Image", "image", "The image of your embed, displayed between description and any additional fields")
							.addOption("Field", "field", "Configure additional fields of your embed")
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
		message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "selvalue").replaceDescription("{addon}", messageAddon)).setComponents(actionRows).queue();
		AtomicReference<AwaitTask<ButtonInteractionEvent>> buttonInteractionTask = new AtomicReference<>(null);
		AwaitTask<StringSelectInteractionEvent> selectionMenuTask = AwaitTask.forSelectMenuInteraction(guild, user, message,
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
					default:
						s.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "fatal")).queue();
					}
				}).addValidComponents(menu.getId()).append();
		if (!firstCall) {
			buttonInteractionTask.set(AwaitTask.forButtonInteraction(guild, user, message, null,
					b -> {
						selectionMenuTask.cancel();
						MessageEditCallbackAction mecAction = null;
						if (b.getButton().getId().equals("send")) {
							embedCache.add(eb.build());
							MessageCreateAction sendAction = target.sendMessageEmbeds(embedCache);
							attachments.forEach((fileName, inputStream) -> {
								sendAction.addFiles(FileUpload.fromData(inputStream, fileName));
							});
							sendAction.queue();
							embedCache.clear();
							mecAction = b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "sendsuccess")).setComponents();
						} else if (b.getButton().getId().equals("cancel")) {
							mecAction = b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "cancelsuccess")).setComponents();
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
					}, () -> {})
				.append());
		}
	}
	
	private void configureTitle(SelectMenu menu, EmbedBuilder eb, StringSelectInteractionEvent event) {
		TextInput.Builder titleInput = TextInput.create("title", "Title", TextInputStyle.SHORT)
				.setPlaceholder("Input title")
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
				.addComponents(ActionRow.of(titleInput.build()), ActionRow.of(titleURLInput))
				.build();
		event.replyModal(modal).queue();
		AwaitTask.forModalInteraction(guild, user, event.getMessage(), null,
				m -> {
					m.deferEdit().queue();
					String url = m.getValue("titleURL").getAsString();;
					if (url.equals("") || !Toolbox.checkURL(url)) {
						url = null;
					}
					String input = m.getValue("title").getAsString();
					eb.setTitle(input, url);
					this.defaultConsumer(menu, eb, event, "title", input);
				}, () -> {
					this.defaultTimeout(menu, eb, event);
				}).addValidComponents(modal.getId()).append();
	}
	
	private void configureDescription(SelectMenu menu, EmbedBuilder eb, StringSelectInteractionEvent event) {
		TextInput.Builder descriptionInput = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
				.setPlaceholder("Input description")
				.setMinLength(1)
				.setRequired(true);
		String current = eb.build().getDescription();
		if (current != null) {
			descriptionInput.setValue(current);
		}
		Modal modal = Modal.create("descriptionConfig", "Description configuration")
				.addComponents(ActionRow.of(descriptionInput.build()))
				.build();
		event.replyModal(modal).queue();
		AwaitTask.forModalInteraction(guild, user, event.getMessage(), null,
				m -> {
					m.deferEdit().queue();
					String input = m.getValue("description").getAsString();
					eb.setDescription(input);
					this.defaultConsumer(menu, eb, event, "description", input);
				}, () -> {
					this.defaultTimeout(menu, eb, event);
				}).addValidComponents(modal.getId()).append();
	}
	
	private void configureFooter(SelectMenu menu, EmbedBuilder eb, StringSelectInteractionEvent event) {
		TextInput.Builder footerInput = TextInput.create("footer", "Footer", TextInputStyle.SHORT)
				.setPlaceholder("Input footer")
				.setMinLength(1)
				.setMaxLength(MessageEmbed.TEXT_MAX_LENGTH)
				.setRequired(true);
		Footer current = eb.build().getFooter();
		if (current != null) {
			footerInput.setValue(current.getText());
		}
		Modal modal = Modal.create("footerConfig", "Footer configuration")
				.addComponents(ActionRow.of(footerInput.build()))
				.build();
		event.replyModal(modal).queue();
		AwaitTask.forModalInteraction(guild, user, event.getMessage(), null,
				m -> {
					m.deferEdit().queue();
					String input = m.getValue("footer").getAsString();
					eb.setFooter(input);
					this.defaultConsumer(menu, eb, event, "footer", input);
				}, () -> {
					this.defaultTimeout(menu, eb, event);
				}).addValidComponents(modal.getId()).append();
	}
	
	private void configureThumbnail(SelectMenu menu, EmbedBuilder eb, StringSelectInteractionEvent event) {
		event.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "configthumbnail")).setComponents().queue();
		AwaitTask.forMessageReceival(guild, user, channel,
				m -> {
					return !m.getMessage().getAttachments().stream().filter(a -> a.isImage()).toList().isEmpty();
				},
				m -> {
					Attachment thumbnail = m.getMessage().getAttachments().stream().filter(a -> a.isImage()).toList().get(0);
					String embedIndex = String.valueOf(embedCache.size());
					String extension = thumbnail.getFileExtension();
					if (extension == null) {
						extension = "png";
					}
					try {
						attachments.put("thumbnail" + embedIndex + ")." + extension, thumbnail.getProxy().download().get());
						eb.setThumbnail("attachment://thumbnail" + embedIndex + ")." + extension);
						this.defaultConsumer(menu, eb, event, "thumbnail", "");
					} catch (InterruptedException | ExecutionException e) {
						this.errorConsumer(menu, eb, event);
					}
				}, () -> {
					this.defaultTimeout(menu, eb, event);
				}).append();
	}
	
	private void configureImage(SelectMenu menu, EmbedBuilder eb, StringSelectInteractionEvent event) {
		event.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "configthumbnail")).setComponents().queue();
		AwaitTask.forMessageReceival(guild, user, channel,
				m -> {
					return !m.getMessage().getAttachments().stream().filter(a -> a.isImage()).toList().isEmpty();
				},
				m -> {
					Attachment image = m.getMessage().getAttachments().stream().filter(a -> a.isImage()).toList().get(0);
					String embedIndex = String.valueOf(embedCache.size());
					String extension = image.getFileExtension();
					if (extension == null) {
						extension = "png";
					}
					try {
						attachments.put("image" + embedIndex + ")." + extension, image.getProxy().download().get());
						eb.setImage("attachment://image" + embedIndex + ")." + extension);
						this.defaultConsumer(menu, eb, event, "thumbnail", "");
					} catch (InterruptedException | ExecutionException e) {
						this.errorConsumer(menu, eb, event);
					}
				}, () -> {
					this.defaultTimeout(menu, eb, event);
				}).append();
	}
	
	private void configureFields(SelectMenu menu, EmbedBuilder eb, StringSelectInteractionEvent event) {
		List<Field> fields = eb.build().getFields();
		if (!fields.isEmpty()) {
		    StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("fieldselection")
					.setRequiredRange(1, 1)
					.setPlaceholder("Select field to configure")
					.addOption("New field", "new");
			for (int i = 0; i < fields.size(); i++) {
				menuBuilder.addOption(fields.get(i).getName(), String.valueOf(i));
			}
			event.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "selfield")).setActionRow(menuBuilder.build()).queue();
			AwaitTask.forSelectMenuInteraction(guild, user, event.getMessage(), null,
					sm -> {
						String value = sm.getSelectedOptions().get(0).getValue();
						if (!value.equals("new")) {
							this.continueFieldConfiguration(menu, eb, sm, Integer.parseInt(value), fields);
						} else {
							this.continueFieldConfiguration(menu, eb, sm, -1, fields);
						}
					}, () -> {
						this.defaultTimeout(menu, eb, event);
					}).addValidComponents(menuBuilder.getId()).append();
		} else {
			this.continueFieldConfiguration(menu, eb, event, -1, fields);
		}
	}
	
	private void continueFieldConfiguration(SelectMenu menu, EmbedBuilder eb, StringSelectInteractionEvent event, int targetIndex, List<Field> fields) {
		TextInput.Builder titleInput = TextInput.create("title", "Title", TextInputStyle.SHORT)
				.setMaxLength(MessageEmbed.TITLE_MAX_LENGTH)
				.setRequired(true)
				.setPlaceholder("Input title");
		TextInput.Builder descriptionInput = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
				.setMaxLength(MessageEmbed.VALUE_MAX_LENGTH)
				.setRequired(false)
				.setPlaceholder("Input description");
		if (targetIndex >= 0) {
			titleInput.setValue(fields.get(targetIndex).getName());
			descriptionInput.setValue(fields.get(targetIndex).getValue());
		}
		Modal modal = Modal.create("fieldconfig", "Field configuration")
				.addComponents(ActionRow.of(titleInput.build()), ActionRow.of(descriptionInput.build()))
				.build();
		event.replyModal(modal).queue();
		AwaitTask.forModalInteraction(guild, user, event.getMessage(), null,
				mi -> {
					mi.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "selinline"))
							.setActionRow(Button.secondary("true", Emoji.fromUnicode("\u2705")),
										  Button.secondary("false", Emoji.fromUnicode("\u274C"))).queue();
					AwaitTask.forButtonInteraction(guild, user, event.getMessage(), null,
							bi -> {
								String title = mi.getValue("title").getAsString();
								String description = mi.getValue("description").getAsString();
								boolean inline = Boolean.parseBoolean(bi.getComponentId());
								if (targetIndex < 0) {
									eb.addField(title, description, inline);
								} else {
									eb.clearFields();
									fields.set(targetIndex, new Field(title, description, inline));
									for(Field field : fields) {
										eb.addField(field);
									}
								}
								this.defaultConsumer(menu, eb, event, "field", "");
							}, () -> {
								this.defaultTimeout(menu, eb, event);
							}).addValidComponents("true", "false").append();
				}, () -> {
					this.defaultTimeout(menu, eb, event);
				}).addValidComponents(modal.getId()).append();
	}
	
	private void defaultConsumer(SelectMenu menu, EmbedBuilder eb, StringSelectInteractionEvent event, String key, String input) {
		Message newMessage = event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, key + "success")
				.replaceDescription("{" + key + "}", input)).setComponents().complete();
		Toolbox.scheduleOperation(() -> continueEmbedConfiguration(newMessage, menu, false, eb), 2000);
	}
	
	private void errorConsumer(SelectMenu menu, EmbedBuilder eb, StringSelectInteractionEvent event) {
		Message newMessage = event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "error")).setComponents().complete();
		Toolbox.scheduleOperation(() -> continueEmbedConfiguration(newMessage, menu, false, eb), 2000);
	}
	
	private void defaultTimeout(SelectMenu menu, EmbedBuilder eb, StringSelectInteractionEvent event) {
		Message newMessage = event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "timeout")).setComponents().complete();
		Toolbox.scheduleOperation(() -> continueEmbedConfiguration(newMessage, menu, false, eb), 2000);
	}
}