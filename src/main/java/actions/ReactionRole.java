package actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.actions.SubActionData;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class ReactionRole implements ActionRequest {
	
	private final EventWaiter waiter = Bot.run.getWaiter();
	private TextChannel finalchannel, channel;
	private Guild guild;
	private User user;
	private String msgid;
	private int progress = 0;
	private List<Message> messages = new ArrayList<Message>();;

	@Override
	public void execute(Action event) {
		guild = event.getGuild();
		user = event.getUser();
		channel = event.getTextChannel();
		msgid = event.getSubAction().getOptionAsString(1);
		finalchannel = guild.getTextChannelById(event.getSubAction().getOptionAsChannel(0).getId());
		if (finalchannel == null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:nochannel"));
			return;
		}
		if (finalchannel.retrieveMessageById(msgid).complete() == null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:nomessage"));
			return;
		}
		if (event.getSubAction().getName().equals("add")) {
			messages.add(event.replyEmbedsRA(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineAddRoles")).complete());
			this.defineAddRoles();
			return;
		}
		if (event.getSubAction().getName().equals("delete")) {
			ConfigLoader.getReactionChannelConfig(guild, finalchannel.getId()).remove(msgid);
			event.replyEmbedsRA(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:delsuccess")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
			finalchannel.retrieveMessageById(msgid).complete().clearReactions().queue();
			return;
		}
		if (event.getSubAction().getName().equals("remove")) {
			Message reply = event.replyEmbedsRA(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineRemoveEmoji")).complete();
			waiter.waitForEvent(MessageReactionAddEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getUser().getIdLong() == user.getIdLong();},
					e -> {reply.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:remsuccess").replaceDescription("{emoji}", e.getReactionEmote().getEmoji()).convert()).queue(
									r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
						  JSONObject actions = ConfigLoader.getReactionMessageConfig(guild, finalchannel.getId(), msgid);
						  actions.remove(e.getReactionEmote().getAsCodepoints());
						  finalchannel.retrieveMessageById(msgid).complete().removeReaction(e.getReactionEmote().getAsCodepoints()).queue();},
					1, TimeUnit.MINUTES,
					() -> {reply.delete().queue();
						   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			return;
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("ReactionRole")
													.setInfo("Configure a reaction to give or remove a role")
													.setMinimumPermission(Permission.MANAGE_ROLES)
													.setCategory(ActionData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
														new SubActionData("add", new OptionType[] {OptionType.CHANNEL, OptionType.STRING}),
														new SubActionData("delete", new OptionType[] {OptionType.CHANNEL, OptionType.STRING}),
														new SubActionData("remove", new OptionType[] {OptionType.CHANNEL, OptionType.STRING})
													});
		return actionData;
	}

	private void defineAddRoles() {
		ConfigLoader.createReactionMessageConfig(guild, finalchannel.getId(), msgid);
		waiter.waitForEvent(MessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;}
								  if(e.getMessage().getMentions().getRoles().isEmpty()) {return false;}
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messages.add(e.getMessage());
								  List<Role> roles = e.getMessage().getMentions().getRoles();
								  this.defineAddEmojis(roles);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineAddEmojis(List<Role> roles) {
		Role role = roles.get(progress);
		Message msg = channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineAddEmojis").replaceDescription("{role}", role.getAsMention()).convert()).complete();
		messages.add(msg);
		waiter.waitForEvent(MessageReactionAddEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  if(e.getUser().getIdLong() != user.getIdLong()) {return false;}
				  	  return e.getMessageId().equals(msg.getId());},
				e -> {ConfigLoader.getReactionMessageConfig(guild, finalchannel.getId(), msgid).put(e.getReactionEmote().getAsCodepoints(), role.getId());
					  msg.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineAddEmojis")
							  .replaceDescription("{role}", role.getAsMention() + "\n->" + e.getReactionEmote().getEmoji()).convert()).queue();
					  progress++;
					  if (progress < roles.size()) {
						  this.defineAddEmojis(roles);
					  } else {
						  this.addReactions();
					  }},
				1, TimeUnit.MINUTES,
				() -> {this.cleanup();
					   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});		
	}
	
	private void addReactions() {
		Message msg  = channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:adding").convert()).complete();
		messages.add(msg);
		Set<String> actions = ConfigLoader.getReactionMessageConfig(guild, finalchannel.getId(), msgid).keySet();
		actions.forEach(e -> finalchannel.retrieveMessageById(msgid).complete().addReaction(e).queue());
		try {Thread.sleep(2000);} catch (InterruptedException e) {}
		msg.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:success").convert()).queue();
		try {Thread.sleep(5000);} catch (InterruptedException e) {}
		this.cleanup();
	}
	
	private void cleanup() {
		channel.deleteMessages(messages).queue();
	}
}