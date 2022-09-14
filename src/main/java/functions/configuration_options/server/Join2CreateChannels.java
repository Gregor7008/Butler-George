package functions.configuration_options.server;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import assets.base.AwaitTask;
import assets.functions.ConfigurationEvent;
import assets.functions.ConfigurationEventHandler;
import assets.functions.ConfigurationOptionData;
import assets.functions.ConfigurationSubOptionData;
import engines.base.LanguageEngine;
import engines.configs.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Join2CreateChannels implements ConfigurationEventHandler {

	@Override
	public void execute(ConfigurationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		JSONObject join2createchannels = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("join2createchannels");
		if (event.getSubOperation().equals("remove")) {
			join2createchannels.clear();
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
			return;
		}
		if (event.getSubOperation().equals("list")) {
			this.listJoin2Creates(event);
			return;
		}
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defchannels")).queue();
		AwaitTask.forMessageReceival(guild, user, event.getChannel(),
				e -> {if (!e.getMessage().getMentions().getChannels().isEmpty()) {
						  List<GuildChannel> channels = e.getMessage().getMentions().getChannels();
			  			  boolean noInvalidChannelFound = true;
			  			  for (int i = 0; i < channels.size(); i++) {
			  				  noInvalidChannelFound = (guild.getVoiceChannelById(channels.get(i).getIdLong()) != null);
			  			  }
			  			  return noInvalidChannelFound;
			  		  } else {return false;}},
				e -> {List<GuildChannel> channels = e.getMessage().getMentions().getChannels().stream().distinct().toList();
					  if (event.getSubOperation().equals("add")) {
						 this.configNewChannels(event, channels, 0);
						 return;
					  }
					  if (event.getSubOperation().equals("delete")) {
						 channels.forEach(c -> join2createchannels.remove(c.getId()));
						 event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess")).queue();
						 return;
					  }
				}, null).append();
	}

	@Override
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("Join2CreateChannels")
													.setInfo("Configure Join2Create channels for your server")
													.setSubOptions(new ConfigurationSubOptionData[] {
															new ConfigurationSubOptionData("add", "Activate one or more channels as Join2Create channels"),
				  											new ConfigurationSubOptionData("delete", "Deactivate one channel from the active ones"),
				  											new ConfigurationSubOptionData("remove", "Deactivate all channels"),
				  											new ConfigurationSubOptionData("list", "List all active channels")
													})
													.setRequiredPermissions(Permission.MANAGE_SERVER, Permission.MANAGE_CHANNEL);
		return configurationOptionData;
	}

	@Override
	public List<Permission> getRequiredPermissions() {
		return List.of(Permission.MANAGE_CHANNEL, Permission.VOICE_MOVE_OTHERS);
	}
	
	private void listJoin2Creates(ConfigurationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		EmbedBuilder eb = new EmbedBuilder(LanguageEngine.fetchMessage(guild, user, this, "list"));
		JSONObject j2cs = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("join2createchannels");
		if (j2cs.isEmpty()) {
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nonedefined")).queue();
		} else {
			List<String> keys = new ArrayList<>();
			keys.addAll(j2cs.keySet());
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				JSONObject channelConfig = j2cs.getJSONObject(key);
				String description = "- Name: " + channelConfig.getString("name") 
								 + "\n- Limit: " + String.valueOf(channelConfig.getInt("limit"))
								 + "\n- Configurable: " + String.valueOf(channelConfig.getBoolean("configurable"));
				eb.addField(String.valueOf(i+1) + "). " + guild.getGuildChannelById(Long.valueOf(key)).getName() + " (" + key + "):", description, false);
			}
			event.getMessage().editMessageEmbeds(eb.build()).queue();
		}
	}
	
	private void configNewChannels(ConfigurationEvent event, List<GuildChannel> channels, int progress) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		GuildChannel channel = channels.get(progress);
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defname").replaceDescription("{channel}", "**" + channel.getName() + "**")).queue();
		AwaitTask.forMessageReceival(guild, user, event.getChannel(),
				n -> {String name = n.getMessage().getContentDisplay();
					  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "deflimit").replaceDescription("{channel}", "**" + channel.getName() + "**")).queue();
					  AwaitTask.forMessageReceival(guild, user, event.getChannel(),
							  l -> {try {Integer.parseInt(l.getMessage().getContentRaw());
								  	     return true;
							  	    } catch (NumberFormatException ex) {
								  		return false;
							  	    }},
							  l -> {
								  int limit = Integer.parseInt(l.getMessage().getContentRaw());
								  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defconfigurable").replaceDescription("{channel}", "**" + channel.getName() + "**")).setActionRow(
										  Button.secondary("true", Emoji.fromUnicode("\u2705")),
										  Button.secondary("false", Emoji.fromUnicode("\u274C"))).queue();
								  AwaitTask.forButtonInteraction(guild, user, event.getMessage(),
										  e -> {
											  ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("join2createchannels")
											  .put(channel.getId(), new JSONObject().put("name", name)
													  .put("limit", limit)
													  .put("configurable", Boolean.valueOf(e.getButton().getId())));
											  if (channels.size() == 1) {
												  e.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defsuccesso")).setComponents().queue();
											  } else {
												  if (progress + 1 < channels.size()) {
													  e.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "preparingnext")).setComponents().queue(r -> {
														  this.configNewChannels(event.setMessage(e.getMessage()), channels, progress + 1);
													  });
												  } else {
													  e.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defsuccessm")).setComponents().queue();
												  }
											  }
										  }).append();
							  }, null).append();
				}).append();
	}
}