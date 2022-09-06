package functions.configuration_options.server;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import assets.base.AwaitTask;
import assets.functions.ConfigurationEvent;
import assets.functions.ConfigurationEventHandler;
import assets.functions.ConfigurationOptionData;
import assets.functions.ConfigurationSubOptionData;
import engines.base.LanguageEngine;
import engines.base.Toolbox;
import engines.configs.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class AutoMessages implements ConfigurationEventHandler {

	@Override
	public void execute(ConfigurationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "seltype")).setActionRow(
				Button.secondary("welcome", Emoji.fromUnicode("\uD83C\uDF89")),
				Button.secondary("goodbye", Emoji.fromUnicode("\uD83D\uDC4B")),
				Button.secondary("level", Emoji.fromUnicode("\uD83C\uDD99")),
				Button.secondary("boost", Emoji.fromUnicode("\uD83D\uDC8E"))).queue();
		
		AwaitTask.forButtonInteraction(guild, user, event.getMessage(),
				b -> {
					String type = b.getComponentId();
					JSONArray selectedmsg = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray(type + "msg");
					boolean defined = false;
					try {
						defined = !selectedmsg.getString(1).equals("");
					} catch (JSONException e) {}
					if (event.getSubOperation().equals("set")) {
						//Implement menu
						SelectMenu.Builder menuBuilder = SelectMenu.create("channel")
								.setPlaceholder("Select channel")
								.setRequiredRange(1, 1);
						List<TextChannel> availableChannels = guild.getTextChannels().stream().filter(
								c -> guild.getSelfMember().hasPermission(c, Permission.MESSAGE_SEND)).toList();
						for (TextChannel channel : availableChannels) {
							String topic = channel.getTopic();
							if (topic != null) {
								menuBuilder.addOption(channel.getName(), channel.getId(), topic);
							} else {
								menuBuilder.addOption(channel.getName(), channel.getId());
							}
						}
						SelectMenu menu = menuBuilder.build();
						MessageEmbed firstReplyEmbed = LanguageEngine.fetchMessage(guild, user, this, "selchannel").replaceDescription("{type}", type);
						b.editMessageEmbeds(firstReplyEmbed).setComponents(ActionRow.of(menu)).queue();
						AwaitTask.forSelectMenuInteraction(guild, user, event.getMessage(),
								s -> {
									long selChannelId = Long.valueOf(s.getSelectedOptions().get(0).getValue());
									TextInput titleInput = TextInput.create("title", "Title", TextInputStyle.SHORT)
											.setPlaceholder("Input title")
											.setRequired(true)
											.build();
									TextInput messageInput = TextInput.create("message", "Message", TextInputStyle.PARAGRAPH)
											.setPlaceholder("Input message")
											.setRequired(false)
											.build();
									TextInput varDisplay = TextInput.create("variables", "You may use following variables:", TextInputStyle.PARAGRAPH)
											.setValue(LanguageEngine.getRaw(guild, user, this, "variables"))
											.setRequired(false)
											.build();
									Modal.Builder modalBuilder = Modal.create("configMessage", type.substring(0, 1).toUpperCase() + type.substring(1) + " message configuration");
									modalBuilder.addActionRows(ActionRow.of(titleInput), ActionRow.of(messageInput), ActionRow.of(varDisplay));
									s.replyModal(modalBuilder.build()).queue();
									s.getMessage().editMessageEmbeds(firstReplyEmbed).setComponents().queue();
									AwaitTask.forModalInteraction(guild, user, s.getMessage(),
											e -> {
												selectedmsg.put(0, selChannelId);
												selectedmsg.put(1, e.getValue("title").getAsString());
												selectedmsg.put(2, e.getValue("message").getAsString());
												selectedmsg.put(3, true);
												e.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, type + "success")).setComponents().queue();
											}).append();
								}).append();
						return;
					}
					if (!defined) {
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nonedefined").replaceDescription("{type}", type)).setComponents().queue();
						return;
					}
					if (event.getSubOperation().equals("on")) {
						if (!selectedmsg.getBoolean(3)) {
							selectedmsg.put(3, true);
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "onsuccess").replaceDescription("{type}", type)).setComponents().queue();
							return;
						} else {
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "onfail").replaceDescription("{type}", type)).setComponents().queue();
							return;
						}
					}
					if (event.getSubOperation().equals("off")) {
						if (selectedmsg.getBoolean(3)) {
							selectedmsg.put(3, false);
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offsuccess").replaceDescription("{type}", type)).setComponents().queue();
							return;
						} else {
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offfail").replaceDescription("{type}", type)).setComponents().queue();
							return;
						}
					}
					if (event.getSubOperation().equals("test")) {
						String title = Toolbox.processAutoMessage(selectedmsg.getString(1), guild, user, false);
						String message = Toolbox.processAutoMessage(selectedmsg.getString(2), guild, user, true);
						guild.getTextChannelById(selectedmsg.getLong(0)).sendMessageEmbeds(LanguageEngine.buildMessage(title, message, null)).queue();
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "testsuccess").replaceDescription("{type}", type)).setComponents().queue();
					}
				}).append();
	}

	@Override
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("AutoMessages")
													.setInfo("Configure messages sent on a user leaving/joining")
													.setSubOptions(new ConfigurationSubOptionData[] {
															new ConfigurationSubOptionData("set", "Set a goodbye message for leaving members"),
															new ConfigurationSubOptionData("on", "Activate (and set if not already done) the goodbye message"),
															new ConfigurationSubOptionData("off", "Deactivate the goodbye message"),
															new ConfigurationSubOptionData("test", "Test the currently set goodbye message")
													})
													.setRequiredPermissions(Permission.MANAGE_SERVER);
		return configurationOptionData;
	}
}