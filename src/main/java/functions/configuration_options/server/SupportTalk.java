package functions.configuration_options.server;

import java.util.List;

import assets.base.AwaitTask;
import assets.functions.ConfigurationEvent;
import assets.functions.ConfigurationEventHandler;
import assets.functions.ConfigurationOptionData;
import assets.functions.ConfigurationSubOptionData;
import engines.base.LanguageEngine;
import engines.configs.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;

public class SupportTalk implements ConfigurationEventHandler {

	@Override
	public void execute(ConfigurationEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (event.getSubOperation().equals("set")) {
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defchannel")).queue();
			AwaitTask.forMessageReceival(guild, user, event.getChannel(),
					e -> {if (!e.getMessage().getMentions().getChannels().isEmpty()) {
						 	 return e.getMessage().getMentions().getChannels().get(0).getType().isAudio();
					} else {return false;}}, 
					e -> {
						GuildChannel channel = e.getMessage().getMentions().getChannels().get(0);
						ConfigLoader.INSTANCE.getGuildConfig(guild).put("supporttalk", channel.getIdLong());
						event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess").replaceDescription("{channel}", channel.getAsMention())).queue();
						return;
					}, null).append();
		}
		if (event.getSubOperation().equals("clear")) {
			ConfigLoader.INSTANCE.getGuildConfig(guild).put("supporttalk", 0L);
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess")).queue();
		}
	}

	@Override
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("SupportTalk")
															 .setInfo("Configure a voice channel for voice support")
															 .setSubOptions(new ConfigurationSubOptionData[] {
																	 new ConfigurationSubOptionData("set", "Set a voice channel as the support talk"),
																	 new ConfigurationSubOptionData("clear", "Undefine the support talk")
															 })
															 .setRequiredPermissions(Permission.MANAGE_SERVER, Permission.MANAGE_CHANNEL);
		return configurationOptionData;
	}

	@Override
	public List<Permission> getRequiredPermissions() {
		return List.of(Permission.VOICE_MOVE_OTHERS);
	}
}