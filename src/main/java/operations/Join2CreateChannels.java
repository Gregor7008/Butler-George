package operations;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import components.ResponseDetector;
import components.Toolbox;
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

public class Join2CreateChannels implements OperationEventHandler {

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
						 event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess").convert()).queue();
						 return;
					  }
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Join2CreateChannels")
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
		if (j2cs.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nonedefined")).queue();
		} else {
			List<String> keys = new ArrayList<>();
			keys.addAll(j2cs.keySet());
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				JSONObject channelConfig = j2cs.getJSONObject(key);
				String description = "- Name: " + channelConfig.getString("name") 
								 + "\n- Limit: " + String.valueOf(channelConfig.getInt("limit"))
								 + "\n- Configurable: " + String.valueOf(channelConfig.getBoolean("configurable"));
				eb.addField(String.valueOf(i+1) + ". " + guild.getGuildChannelById(Long.valueOf(key)).getName() + " (" + key + "):", description, false);
			}
			event.replyEmbeds(eb.build()).queue();
		}
	}
	
	private void configNewChannels(OperationEvent event, List<GuildChannel> channels, int progress) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		GuildChannel channel = channels.get(progress);
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defname").replaceDescription("{channel}", "**" + channel.getName() + "**").convert()).queue();
		ResponseDetector.waitForMessage(guild, user, event.getChannel(),
				n -> {String name = n.getMessage().getContentDisplay();
					  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "deflimit").replaceDescription("{channel}", "**" + channel.getName() + "**").convert()).queue();
					  ResponseDetector.waitForMessage(guild, user, event.getChannel(),
							  l -> {try {Integer.parseInt(l.getMessage().getContentRaw());
								  	     return true;
							  	    } catch (NumberFormatException ex) {
								  		return false;
							  	    }},
							  l -> {
								  int limit = Integer.parseInt(l.getMessage().getContentRaw());
								  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defconfigurable").replaceDescription("{channel}", "**" + channel.getName() + "**").convert()).setActionRow(
										  Button.secondary("true", Emoji.fromUnicode("\u2705")),
										  Button.secondary("false", Emoji.fromUnicode("\u274C"))).queue();
								  ResponseDetector.waitForButtonClick(guild, user, event.getMessage(), null,
										  e -> {
											  Toolbox.deleteActionRows(e.getMessage(), () -> {
												  ConfigLoader.getGuildConfig(guild).getJSONObject("join2createchannels")
								  					.put(channel.getId(), new JSONObject().put("name", name)
										  												  .put("limit", limit)
										  												  .put("configurable", Boolean.valueOf(e.getButton().getId())));
												  if (channels.size() == 1) {
													  e.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defsuccesso").convert()).queue();
												  } else {
													  if (progress + 1 < channels.size()) {
														  e.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "preparingnext").convert()).queue(r -> {
															  this.configNewChannels(event.setMessage(e.getMessage()), channels, progress + 1);
														  });
													  } else {
														  e.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defsuccessm").convert()).queue();
													  }
												  }
											  });
										  });
							  });
				});
	}
}