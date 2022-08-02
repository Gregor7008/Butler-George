package configuration_options.server;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import base.assets.AwaitTask;
import base.engines.ConfigLoader;
import base.engines.LanguageEngine;
import base.engines.Toolbox;
import configuration_options.assets.ConfigurationEvent;
import configuration_options.assets.ConfigurationEventHandler;
import configuration_options.assets.ConfigurationOptionData;
import configuration_options.assets.ConfigurationSubOptionData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class AutoMessages implements ConfigurationEventHandler {

	@Override
	public void execute(ConfigurationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "unsupportedJDA")).queue();
//		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "seltype")).setActionRow(
//				Button.secondary("welcome", Emoji.fromUnicode("\uD83C\uDF89")),
//				Button.secondary("goodbye", Emoji.fromUnicode("\uD83D\uDC4B")),
//				Button.secondary("level", Emoji.fromUnicode("\uD83C\uDD99"))).queue();
		
//		TODO Implement auto message configuration when JDA updates and supports selection menus in modals!
		AwaitTask.forButtonInteraction(guild, user, event.getMessage(),
				b -> {
					String type = b.getComponentId();
					JSONArray selectedmsg = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray(type + "msg");
					boolean defined = false;
					try {
						defined = !selectedmsg.getString(1).equals("");
					} catch (JSONException e) {}
					if (event.getSubOperation().equals("set")) {
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
						TextInput titleInput = TextInput.create("title", "Title", TextInputStyle.SHORT)
								.setPlaceholder("Input title")
								.setRequired(true)
								.build();
						TextInput messageInput = TextInput.create("message", "Message", TextInputStyle.PARAGRAPH)
								.setPlaceholder("Input message")
								.build();
						Modal.Builder modalBuilder = Modal.create("configMessage", type.substring(0, 1).toUpperCase() + type.substring(1) + " message configuration");
						modalBuilder.addActionRows(ActionRow.of(menu), ActionRow.of(titleInput), ActionRow.of(messageInput));
						b.replyModal(modalBuilder.build()).queue();
						AwaitTask.forModalInteraction(guild, user, event.getChannel(),
								e -> {
									selectedmsg.put(0, 0L);			//		<- Get selected value from SelectMenu, convert to long and execute
									selectedmsg.put(1, e.getValue("title").getAsString());
									selectedmsg.put(2, e.getValue("message").getAsString());
									selectedmsg.put(3, true);
									event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, type + "success")).queue();
								}).append();
						return;
					}
					if (!defined) {
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nonedefined").replaceDescription("{type}", type)).queue();
						return;
					}
					if (event.getSubOperation().equals("on")) {
						if (!selectedmsg.getBoolean(3)) {
							selectedmsg.put(3, true);
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "onsuccess").replaceDescription("{type}", type)).queue();
							return;
						} else {
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "onfail").replaceDescription("{type}", type)).queue();
							return;
						}
					}
					if (event.getSubOperation().equals("off")) {
						if (selectedmsg.getBoolean(3)) {
							selectedmsg.put(3, false);
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offsuccess").replaceDescription("{type}", type)).queue();
							return;
						} else {
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offfail").replaceDescription("{type}", type)).queue();
							return;
						}
					}
					if (event.getSubOperation().equals("test")) {
						String title = Toolbox.processAutoMessage(selectedmsg.getString(1), guild, user);
						String message = Toolbox.processAutoMessage(selectedmsg.getString(2), guild, user);
						guild.getTextChannelById(selectedmsg.getLong(0)).sendMessageEmbeds(LanguageEngine.buildMessage(title, message, null)).queue();
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "testsuccess").replaceDescription("{type}", type)).queue();
					}
				}); //.append(); <- Append again, when JDA supports this operation
	}

	@Override
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("AutoMessages")
													.setInfo("Configure messages sent on a user leaving/joining")
													.setSubOperations(new ConfigurationSubOptionData[] {
															new ConfigurationSubOptionData("set", "Set a goodbye message for leaving members"),
															new ConfigurationSubOptionData("on", "Activate (and set if not already done) the goodbye message"),
															new ConfigurationSubOptionData("off", "Deactivate the goodbye message"),
															new ConfigurationSubOptionData("test", "Test the currently set goodbye message")
													});
		return configurationOptionData;
	}
}