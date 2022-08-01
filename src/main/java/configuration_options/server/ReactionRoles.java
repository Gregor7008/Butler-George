package configuration_options.server;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import base.assets.AwaitTask;
import base.engines.ConfigLoader;
import base.engines.LanguageEngine;
import configuration_options.assets.ConfigurationEvent;
import configuration_options.assets.ConfigurationEventHandler;
import configuration_options.assets.ConfigurationOptionData;
import configuration_options.assets.ConfigurationSubOptionData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class ReactionRoles implements ConfigurationEventHandler {
	
	private ConfigurationEvent event;
	private Guild guild;
	private User user;
	private Message message;
	private String msgid, chid;
	int progress = 0;

	@Override
	public void execute(ConfigurationEvent event) {
		this.event = event;
		this.guild = event.getGuild();
		this.user = event.getUser();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defchannel")).queue();
		AwaitTask.forMessageReceival(guild, user, event.getChannel(),
				e -> {return !e.getMessage().getMentions().getChannels().isEmpty();},
				e -> {this.chid = e.getMessage().getMentions().getChannels().get(0).getId();
					  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defmessage").convert()).queue();
					  AwaitTask.forMessageReceival(guild, user, event.getChannel(),
							  m -> {return guild.getTextChannelById(chid).retrieveMessageById(e.getMessage().getContentRaw()).complete() != null;},
							  m -> {this.msgid = e.getMessage().getContentRaw();
							  	    if (event.getSubOperation().equals("add")) {
							  	    	event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defineAddRoles").convert()).complete();
							  	    	this.defineAddRoles();
							  	    	return;
							  	    }
							  	    if (event.getSubOperation().equals("remove")) {
							  	    	ConfigLoader.INSTANCE.getReactionChannelConfig(guild, chid).remove(msgid);
							  	    	event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
							  	    	guild.getTextChannelById(chid).retrieveMessageById(msgid).complete().clearReactions().queue();
							  	    	return;
							  	    }
							  	    if (event.getSubOperation().equals("delete")) {
							  	    	event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defineDelEmoji").convert()).queue();
							  	    	AwaitTask.forReactionAdding(guild, user, message,
							  	    			r -> {event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")
							  	    					.replaceDescription("{emoji}", r.getReaction().getEmoji().getFormatted()).convert()).queue(a -> a.delete().queueAfter(3, TimeUnit.SECONDS));
							  	    				  JSONObject actions = ConfigLoader.INSTANCE.getReactionMessageConfig(guild, chid, msgid);
							  	    				  actions.remove(r.getReaction().getEmoji().getAsReactionCode());
							  	    				  guild.getTextChannelById(chid).retrieveMessageById(msgid).complete().clearReactions(r.getReaction().getEmoji()).queue();
							  	    				  }).append();
							  	    	return;
							  	    }
							  }, null).append();
				}, null).append();
	}

	@Override
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("ReactionRoles")
													.setInfo("Configure reactions to give or remove a role")
													.setSubOperations(new ConfigurationSubOptionData[] {
														new ConfigurationSubOptionData("add", "Add new reactionroles to a message"),
														new ConfigurationSubOptionData("delete", "Deactivate and delete a reactionrole from a message"),
														new ConfigurationSubOptionData("remove", "Remove all reactionroles from a message"),
														new ConfigurationSubOptionData("list", "List all active reactionroles")
													});
		return configurationOptionData;
	}

	private void defineAddRoles() {
		ConfigLoader.INSTANCE.createReactionMessageConfig(guild, chid, msgid);
		AwaitTask.forMessageReceival(guild, user, message.getChannel(),
							e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
							e -> {List<Role> roles = e.getMessage().getMentions().getRoles();
								  this.defineAddEmojis(roles);}, null).append();
	}
	
	private void defineAddEmojis(List<Role> roles) {
		Role role = roles.get(progress);
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defineAddEmojis").replaceDescription("{role}", role.getAsMention()).convert()).queue();
		AwaitTask.forReactionAdding(guild, user, message,
				e -> {ConfigLoader.INSTANCE.getReactionMessageConfig(guild, chid, msgid).put(e.getReaction().getEmoji().getAsReactionCode(), role.getId());
					  progress++;
					  if (progress < roles.size()) {
						  event.getMessage().clearReactions().queue();
						  this.defineAddEmojis(roles);
					  } else {
						  this.addReactions();
					  }}).append();		
	}
	
	private void addReactions() {
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "adding").convert()).complete();
		Set<String> actions = ConfigLoader.INSTANCE.getReactionMessageConfig(guild, chid, msgid).keySet();
		actions.forEach(e -> guild.getTextChannelById(chid).retrieveMessageById(msgid).complete().addReaction(Emoji.fromUnicode(e)).queue());
		try {Thread.sleep(1000);} catch (InterruptedException e) {}
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").convert()).queue();
	}
}