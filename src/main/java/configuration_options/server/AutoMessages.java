package configuration_options.server;

import java.time.OffsetDateTime;

import org.json.JSONArray;
import org.json.JSONException;

import base.assets.AwaitTask;
import base.engines.ConfigLoader;
import base.engines.LanguageEngine;
import configuration_options.assets.ConfigurationEvent;
import configuration_options.assets.ConfigurationEventHandler;
import configuration_options.assets.ConfigurationOptionData;
import configuration_options.assets.ConfigurationSubOptionData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class AutoMessages implements ConfigurationEventHandler {

	@Override
	public void execute(ConfigurationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "seltype")).setActionRow(
				Button.secondary("welcome", Emoji.fromUnicode("\uD83C\uDF89")),
				Button.secondary("goodbye", Emoji.fromUnicode("\uD83D\uDC4B"))).queue();
		AwaitTask.forButtonInteraction(guild, user, event.getMessage(),
				b -> {
					String type = b.getComponentId();
					JSONArray goodbyemsg = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray(type + "msg");
					boolean defined = false;
					try {
						defined = !goodbyemsg.getString(0).equals("");
					} catch (JSONException e) {}
					if (event.getSubOperation().equals("set")) {
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setrequest").replaceDescription("{type}", type).convert()).queue();
						AwaitTask.forMessageReceival(guild, user, event.getChannel(),
								e -> {if (!e.getMessage().getMentions().getChannels().isEmpty()) {
							  			  return guild.getTextChannelById(e.getMessage().getMentions().getChannels().get(0).getIdLong()) != null;
							  		  } else {return false;}},
								e -> {goodbyemsg.put(1, e.getMessage().getMentions().getChannels().get(0).getIdLong());
									  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "def" + type + "msg").convert()).queue();
									  AwaitTask.forMessageReceival(guild, user, event.getChannel(),
											  a -> {goodbyemsg.put(0, a.getMessage().getContentRaw()).put(2, true);
											  	    event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, type + "success").convert()).queue();
											  }).append();
								}, null).append();
						return;
					}
					if (!defined) {
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nonedefined").replaceDescription("{type}", type).convert()).queue();
						return;
					}
					if (event.getSubOperation().equals("on")) {
						if (!goodbyemsg.getBoolean(2)) {
							goodbyemsg.put(2, true);
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "onsuccess").replaceDescription("{type}", type).convert()).queue();
							return;
						} else {
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "onfail").replaceDescription("{type}", type).convert()).queue();
							return;
						}
					}
					if (event.getSubOperation().equals("off")) {
						if (goodbyemsg.getBoolean(2)) {
							goodbyemsg.put(2, false);
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offsuccess").replaceDescription("{type}", type).convert()).queue();
							return;
						} else {
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offfail").replaceDescription("{type}", type).convert()).queue();
							return;
						}
					}
					if (event.getSubOperation().equals("test")) {
						String msg = goodbyemsg.getString(0)
						   .replace("{server}", guild.getName())
						   .replace("{user}", event.getUser().getName())
						   .replace("{membercount}", Integer.toString(guild.getMemberCount()))
						   .replace("{date}", OffsetDateTime.now().format(LanguageEngine.formatter));
						guild.getTextChannelById(goodbyemsg.getLong(1)).sendMessage(msg).queue();
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "testsuccess").replaceDescription("{type}", type).convert()).queue();
					}
				}).append();
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