package configuration_options.server;

import base.assets.AwaitTask;
import base.engines.LanguageEngine;
import base.engines.configs.ConfigLoader;
import configuration_options.assets.ConfigurationEvent;
import configuration_options.assets.ConfigurationEventHandler;
import configuration_options.assets.ConfigurationOptionData;
import configuration_options.assets.ConfigurationSubOptionData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class InboxChannels implements ConfigurationEventHandler {

	@Override
	public void execute(ConfigurationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "seltype"))
						  .setActionRow(Button.secondary("community", Emoji.fromUnicode("\uD83C\uDF89")),
								  	    Button.secondary("suggestion", Emoji.fromUnicode("\uD83D\uDCA1")),
								  	    Button.secondary("moderation", Emoji.fromUnicode("\uD83D\uDC6E"))).queue();
		AwaitTask.forButtonInteraction(guild, user, event.getMessage(),
				b -> {
					final String selection = b.getComponentId();
					if (event.getSubOperation().equals("set")) {
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defchannel").replaceDescription("{selection}", selection)).setActionRows().queue();
						AwaitTask.forMessageReceival(guild, user, event.getChannel(),
								e -> {if (!e.getMessage().getMentions().getChannels().isEmpty()) {
									return guild.getTextChannelById(e.getMessage().getMentions().getChannels().get(0).getIdLong()) != null;
								} else {return false;}
								},
								e -> {
									long id = e.getMessage().getMentions().getChannels().get(0).getIdLong();
									ConfigLoader.INSTANCE.getGuildConfig(guild).put(selection + "inbox", id);
									b.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess")
											.replaceDescription("{selection}", selection)
											.replaceDescription("{channel}", guild.getTextChannelById(id).getAsMention())).queue();
									e.getMessage().delete().queue();
									return;
								},null).append();
					}
					if (event.getSubOperation().equals("clear")) {
						ConfigLoader.INSTANCE.getGuildConfig(guild).put("communityinbox", 0L);
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess").replaceDescription("{selection}", selection)).setActionRows().queue();
					}
				}).append();
	}

	@Override
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("InboxChannels")
													.setInfo("Configure inbox channels for different receivers/purposes")
													.setSubOperations(new ConfigurationSubOptionData[] {
															new ConfigurationSubOptionData("set", "Set an inbox channel for a selected use case"),
															new ConfigurationSubOptionData("clear", "Undefine an inbox channel")
													});
		return configurationOptionData;
	}
}