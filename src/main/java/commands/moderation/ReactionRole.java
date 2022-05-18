package commands.moderation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ReactionRole implements Command{
	
	private final EventWaiter waiter = Bot.INSTANCE.getWaiter();
	private SlashCommandInteractionEvent oevent;
	private TextChannel finalchannel, channel;
	private Guild guild;
	private User user;
	private String msgid;
	private int progress = 0;
	private List<Message> messages = new ArrayList<Message>();;

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		oevent = event;
		guild = event.getGuild();
		user = event.getUser();
		channel = event.getTextChannel();
		msgid = event.getOption("message").getAsString();
		finalchannel = guild.getTextChannelById(event.getOption("channel").getAsGuildChannel().getId());
		if (finalchannel == null) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:nochannel").convert()).queue();
			return;
		}
		try {
			if (finalchannel.retrieveMessageById(msgid).complete() == null) {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:nomessage").convert()).queue();
				return;
			}
		} catch (IllegalArgumentException e) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:nomessage").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineAddRoles").convert()).queue();
			this.defineAddRoles();
			return;
		}
		if (event.getSubcommandName().equals("delete")) {
			Configloader.INSTANCE.deleteReactionRoleConfig(guild, finalchannel.getId(), msgid);
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:delsuccess").convert()).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			finalchannel.retrieveMessageById(msgid).complete().clearReactions().queue();
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineRemoveEmoji").convert()).queue();
			waiter.waitForEvent(MessageReactionAddEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getUser().getIdLong() == user.getIdLong();},
					e -> {event.getHook().editOriginalEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:remsuccess").replaceDescription("{emoji}", e.getReactionEmote().getEmoji()).convert()).queue(
									r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
						  JSONObject actions = Configloader.INSTANCE.findReactionroleConfig(guild, finalchannel.getId(), msgid);
						  actions.remove(e.getReactionEmote().getAsCodepoints());
						  finalchannel.retrieveMessageById(msgid).complete().removeReaction(e.getReactionEmote().getAsCodepoints()).queue();},
					1, TimeUnit.MINUTES,
					() -> {event.getHook().deleteOriginal().queue();
						   channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			return;
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("reactionrole", "0")
									.addSubcommands(new SubcommandData("add", "Adds a reaction to a message that acts as a button to get a specified role")
											.addOption(OptionType.CHANNEL, "channel", "The channel of your message", true)
											.addOption(OptionType.STRING, "message", "The message-id for your message", true))
									.addSubcommands(new SubcommandData("delete", "Deletes all reactions of a reactionrole message")
											.addOption(OptionType.CHANNEL, "channel", "The channel of your message", true)
											.addOption(OptionType.STRING, "message", "The message-id for your message", true))
									.addSubcommands(new SubcommandData("remove", "Removes one reaction of a reactionrole message")
											.addOption(OptionType.CHANNEL, "channel", "The channel of your message", true)
											.addOption(OptionType.STRING, "message", "The message-id for your message", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/reactionrole:help");
	}

	private void defineAddRoles() {
		Configloader.INSTANCE.createReactionroleConfig(guild, finalchannel.getId(), msgid);
		waiter.waitForEvent(MessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;}
								  if(e.getMessage().getMentionedRoles().isEmpty()) {return false;}
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messages.add(e.getMessage());
								  List<Role> roles = e.getMessage().getMentionedRoles();
								  this.defineAddEmojis(roles);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineAddEmojis(List<Role> roles) {
		Role role = roles.get(progress);
		Message msg = channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineAddEmojis").replaceDescription("{role}", role.getAsMention()).convert()).complete();
		messages.add(msg);
		waiter.waitForEvent(MessageReactionAddEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  if(e.getUser().getIdLong() != user.getIdLong()) {return false;}
				  	  return e.getMessageId().equals(msg.getId());},
				e -> {Configloader.INSTANCE.findReactionroleConfig(guild, finalchannel.getId(), msgid).put(e.getReactionEmote().getAsCodepoints(), role.getId());
					  msg.editMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineAddEmojis")
							  .replaceDescription("{role}", role.getAsMention() + "\n->" + e.getReactionEmote().getEmoji()).convert()).queue();
					  progress++;
					  if (progress < roles.size()) {
						  this.defineAddEmojis(roles);
					  } else {
						  this.addReactions();
					  }},
				1, TimeUnit.MINUTES,
				() -> {this.cleanup();
					   channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});		
	}
	
	private void addReactions() {
		Message msg  = channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:adding").convert()).complete();
		messages.add(msg);
		Set<String> actions = Configloader.INSTANCE.findReactionroleConfig(guild, finalchannel.getId(), msgid).keySet();
		actions.forEach(e -> finalchannel.retrieveMessageById(msgid).complete().addReaction(e).queue());
		try {Thread.sleep(2000);} catch (InterruptedException e) {}
		msg.editMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:success").convert()).queue();
		try {Thread.sleep(5000);} catch (InterruptedException e) {}
		this.cleanup();
	}
	
	private void cleanup() {
		channel.deleteMessages(messages).queue();
		oevent.getHook().deleteOriginal().queue();
	}
}