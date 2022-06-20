package actions.administration;

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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class ReactionRole implements ActionRequest {
	
	private final EventWaiter waiter = Bot.run.getWaiter();
	private Action event;
	private Guild guild;
	private User user;
	private Message message;
	private String msgid, chid;
	int progress = 0;

	@Override
	public void execute(Action event) {
		this.event = event;
		this.guild = event.getGuild();
		this.user = event.getUser();
		this.msgid = event.getSubAction().getOptionAsString(1);
		this.chid = event.getSubAction().getOptionAsChannel(0).getId();
		if (guild.getTextChannelById(chid).retrieveMessageById(msgid).complete() == null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:nomessage")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("add")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineAddRoles")).complete();
			this.defineAddRoles();
			return;
		}
		if (event.getSubAction().getName().equals("delete")) {
			ConfigLoader.getReactionChannelConfig(guild, chid).remove(msgid);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:delsuccess")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
			guild.getTextChannelById(chid).retrieveMessageById(msgid).complete().clearReactions().queue();
			return;
		}
		if (event.getSubAction().getName().equals("remove")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineRemoveEmoji")).queue();
			waiter.waitForEvent(MessageReactionAddEvent.class,
					e -> {if(!e.getChannel().getId().equals(message.getChannel().getId())) {return false;} 
					  	  return e.getUser().getIdLong() == user.getIdLong();},
					e -> {event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:remsuccess")
							   .replaceDescription("{emoji}", e.getReactionEmote().getEmoji())).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
						  JSONObject actions = ConfigLoader.getReactionMessageConfig(guild, chid, msgid);
						  actions.remove(e.getReactionEmote().getAsCodepoints());
						  guild.getTextChannelById(chid).retrieveMessageById(msgid).complete().removeReaction(e.getReactionEmote().getAsCodepoints()).queue();},
					1, TimeUnit.MINUTES,
					() -> {event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
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
		ConfigLoader.createReactionMessageConfig(guild, chid, msgid);
		waiter.waitForEvent(MessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(message.getChannel().getId())) {return false;}
								  if(e.getMessage().getMentions().getRoles().isEmpty()) {return false;}
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {List<Role> roles = e.getMessage().getMentions().getRoles();
								  this.defineAddEmojis(roles);},
							1, TimeUnit.MINUTES,
							() -> {event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineAddEmojis(List<Role> roles) {
		Role role = roles.get(progress);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineAddEmojis").replaceDescription("{role}", role.getAsMention())).queue();
		waiter.waitForEvent(MessageReactionAddEvent.class,
				e -> {if(!e.getChannel().getId().equals(message.getChannel().getId())) {return false;} 
				  	  if(e.getUser().getIdLong() != user.getIdLong()) {return false;}
				  	  return e.getMessageId().equals(message.getId());},
				e -> {ConfigLoader.getReactionMessageConfig(guild, chid, msgid).put(e.getReactionEmote().getAsCodepoints(), role.getId());
					  progress++;
					  if (progress < roles.size()) {
						  this.defineAddEmojis(roles);
					  } else {
						  this.addReactions();
					  }},
				1, TimeUnit.MINUTES,
				() -> {event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});		
	}
	
	private void addReactions() {
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:adding").convert()).complete();
		Set<String> actions = ConfigLoader.getReactionMessageConfig(guild, chid, msgid).keySet();
		actions.forEach(e -> guild.getTextChannelById(chid).retrieveMessageById(msgid).complete().addReaction(e).queue());
		try {Thread.sleep(1000);} catch (InterruptedException e) {}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/reactionrole:success").convert()).queue();
	}
}