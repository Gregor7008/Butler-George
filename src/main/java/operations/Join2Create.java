package operations;

import java.util.List;

import org.json.JSONObject;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Join2Create implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		JSONObject join2createchannels = ConfigLoader.getGuildConfig(guild).getJSONObject("join2createchannels");
		if (event.getSubOperation().equals("remove")) {
			join2createchannels.clear();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
			return;
		}
		if (event.getSubOperation().equals("list")) {
			this.listJoin2Creates(event);
			return;
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defchannels")).queue();
		ResponseDetector.waitForMessage(guild, user, event.getChannel(),
				e -> {return e.getMessage().getMentions().getChannels().isEmpty();},
				e -> {List<GuildChannel> channels = e.getMessage().getMentions().getChannels();
					  if (event.getSubOperation().equals("add")) {
						 this.configNewChannels(event, channels);
						 return;
					  }
					  if (event.getSubOperation().equals("delete")) {
						 channels.forEach(c -> join2createchannels.remove(c.getId()));
						 event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess")).queue();
						 return;
					  }
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Join2Create")
													.setInfo("Configure Join2Create channels for your server")
													.setSubOperations(new SubOperationData[] {
															new SubOperationData("add", "Activate one or more channels as Join2Create channels"),
				  											new SubOperationData("delete", "Deactivate one channel from the active ones"),
				  											new SubOperationData("remove", "Deactivate all channels"),
				  											new SubOperationData("list", "List all active channels")
													});
		return operationData;
	}
	
	private void listJoin2Creates(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		EmbedBuilder eb = new EmbedBuilder(LanguageEngine.fetchMessage(guild, user, this, "list").convert());
		JSONObject j2cs = ConfigLoader.getGuildConfig(guild).getJSONObject("join2createchannels");
		j2cs.keySet().forEach(e -> {
			JSONObject channelConfig = j2cs.getJSONObject(e);
			String description = "Name:" + channelConfig.getString("name") 
							 + "\nLimit:" + String.valueOf(channelConfig.getInt("limit"))
							 + "\nConfigurable:" + String.valueOf(channelConfig.getBoolean("configurable"));
			eb.addField(guild.getGuildChannelById(e) + " (" + e + "):", description, false);
		});
		event.replyEmbeds(eb.build());
	}
	
	private void configNewChannels(OperationEvent event, List<GuildChannel> channels) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		for (int i = 0; i < channels.size(); i++) {
			GuildChannel channel = channels.get(i);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defname").replaceDescription("{channel}", channel.getName() + " (" + channel.getId() + ")")).queue();
			ResponseDetector.waitForMessage(guild, user, event.getChannel(),
					n -> {String name = n.getMessage().getContentDisplay();
						  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "deflimit").replaceDescription("{channel}", channel.getName() + " (" + channel.getId() + ")")).queue();
						  ResponseDetector.waitForMessage(guild, user, event.getChannel(),
								  l -> {try {Integer.parseInt(l.getMessage().getContentRaw());
									  	     return true;
								  	    } catch (NumberFormatException ex) {
									  		return false;
								  	    }},
								  l -> {
									  int limit = Integer.parseInt(l.getMessage().getContentRaw());
									  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defconfigurable").replaceDescription("{channel}", channel.getName() + " (" + channel.getId() + ")")).setActionRow(
											  Button.primary("true", Emoji.fromFormatted(":white_check_mark")),
											  Button.primary("false", Emoji.fromFormatted(":x:"))).queue();
									  ResponseDetector.waitForButtonClick(guild, user, event.getMessage(), null,
											  e -> {
												  ConfigLoader.getGuildConfig(guild).getJSONObject("join2createchannels")
												  					.put(channel.getId(), new JSONObject().put("name", name)
														  												  .put("limit", limit)
														  												  .put("configurable", Boolean.valueOf(e.getButton().getId())));
											  });
								  });
					});
		}
	}
}