package operations;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class ReactionRole implements OperationEventHandler {
	
	private OperationEvent event;
	private Guild guild;
	private User user;
	private Message message;
	private String msgid, chid;
	int progress = 0;

	@Override
	public void execute(OperationEvent event) {
		this.event = event;
		this.guild = event.getGuild();
		this.user = event.getUser();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defchannel")).queue();
		ResponseDetector.waitForMessage(guild, user, event.getChannel(),
				e -> {return !e.getMessage().getMentions().getChannels().isEmpty();},
				e -> {this.chid = e.getMessage().getMentions().getChannels().get(0).getId();
					  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defmessage").convert()).queue();
					  ResponseDetector.waitForMessage(guild, user, event.getChannel(),
							  m -> {return guild.getTextChannelById(chid).retrieveMessageById(e.getMessage().getContentRaw()).complete() != null;},
							  m -> {this.msgid = e.getMessage().getContentRaw();
							  	    if (event.getSubOperation().equals("add")) {
							  	    	event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defineAddRoles").convert()).complete();
							  	    	this.defineAddRoles();
							  	    	return;
							  	    }
							  	    if (event.getSubOperation().equals("remove")) {
							  	    	ConfigLoader.getReactionChannelConfig(guild, chid).remove(msgid);
							  	    	event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
							  	    	guild.getTextChannelById(chid).retrieveMessageById(msgid).complete().clearReactions().queue();
							  	    	return;
							  	    }
							  	    if (event.getSubOperation().equals("delete")) {
							  	    	event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defineDelEmoji").convert()).queue();
							  	    	ResponseDetector.waitForReaction(guild, user, message,
							  	    			r -> {event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")
							  	    					.replaceDescription("{emoji}", r.getReaction().getEmoji().getFormatted()).convert()).queue(a -> a.delete().queueAfter(3, TimeUnit.SECONDS));
							  	    				  JSONObject actions = ConfigLoader.getReactionMessageConfig(guild, chid, msgid);
							  	    				  actions.remove(r.getReaction().getEmoji().getAsReactionCode());
							  	    				  guild.getTextChannelById(chid).retrieveMessageById(msgid).complete().clearReactions(r.getReaction().getEmoji()).queue();});
							  	    	return;
							  	    }
							  });
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("ReactionRole")
													.setInfo("Configure a reaction to give or remove a role")
													.setSubOperations(new SubOperationData[] {
														new SubOperationData("add", "Add a new reactionrole to a message"),
														new SubOperationData("delete", "Deactivate and delete a reactionrole from a message"),
														new SubOperationData("remove", "Remove all reactionroles from a message"),
														new SubOperationData("list", "List all active reactionroles")
													});
		return operationData;
	}

	private void defineAddRoles() {
		ConfigLoader.createReactionMessageConfig(guild, chid, msgid);
		ResponseDetector.waitForMessage(guild, user, message.getChannel(),
							e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
							e -> {List<Role> roles = e.getMessage().getMentions().getRoles();
								  this.defineAddEmojis(roles);});
	}
	
	private void defineAddEmojis(List<Role> roles) {
		Role role = roles.get(progress);
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defineAddEmojis").replaceDescription("{role}", role.getAsMention()).convert()).queue();
		ResponseDetector.waitForReaction(guild, user, message,
				e -> {ConfigLoader.getReactionMessageConfig(guild, chid, msgid).put(e.getReaction().getEmoji().getAsReactionCode(), role.getId());
					  progress++;
					  if (progress < roles.size()) {
						  event.getMessage().clearReactions().queue();
						  this.defineAddEmojis(roles);
					  } else {
						  this.addReactions();
					  }});		
	}
	
	private void addReactions() {
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "adding").convert()).complete();
		Set<String> actions = ConfigLoader.getReactionMessageConfig(guild, chid, msgid).keySet();
		actions.forEach(e -> guild.getTextChannelById(chid).retrieveMessageById(msgid).complete().addReaction(Emoji.fromUnicode(e)).queue());
		try {Thread.sleep(1000);} catch (InterruptedException e) {}
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").convert()).queue();
	}
}