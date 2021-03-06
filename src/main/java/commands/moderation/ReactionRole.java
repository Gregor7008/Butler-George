package commands.moderation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ReactionRole implements Command{
	
	private final EventWaiter waiter = Bot.INSTANCE.getWaiter();
	private SlashCommandEvent oevent;
	private TextChannel finalchannel, channel;
	private Guild guild;
	private User user;
	private String msgid;
	private int progress = 0;
	private List<Message> messages = new ArrayList<Message>();;

	@Override
	public void perform(SlashCommandEvent event) {
		oevent = event;
		guild = event.getGuild();
		user = event.getUser();
		channel = event.getTextChannel();
		msgid = event.getOption("message").getAsString();
		finalchannel = guild.getTextChannelById(event.getOption("channel").getAsGuildChannel().getId());
		if (!event.getMember().hasPermission(Permission.MANAGE_ROLES)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:nopermission")).queue();
			return;
		}
		if (finalchannel.equals(null)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:nochannel")).queue();
			return;
		}
		if (finalchannel.retrieveMessageById(msgid).complete().equals(null)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:nomessage")).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineAddRoles")).queue();
			this.defineAddRoles();
			return;
		}
		if (event.getSubcommandName().equals("delete")) {
			Configloader.INSTANCE.removeReactionRoleConfig(guild, finalchannel, msgid);
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:delsuccess")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			finalchannel.retrieveMessageById(msgid).complete().clearReactions().queue();
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:defineRemoveEmoji")).queue();
			waiter.waitForEvent(GuildMessageReactionAddEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getUser().getIdLong() == user.getIdLong();},
					e -> {event.getHook().editOriginalEmbeds(AnswerEngine.ae.buildMessage(
							AnswerEngine.ae.getTitle(guild, user, "/commands/moderation/reactionrole:remsuccess"),
							AnswerEngine.ae.getDescription(guild, user, "/commands/moderation/reactionrole:remsuccess").replace("{emoji}", e.getReactionEmote().getEmoji()))).queue(
									r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
						  String[] actions = Configloader.INSTANCE.getReactionroleConfig(guild, finalchannel, msgid).split(";");
						  Configloader.INSTANCE.setReactionroleConfig(guild, finalchannel, msgid, "");
						  for (int i = 0; i < actions.length; i++) {
							if (!actions[i].contains(e.getReactionEmote().getAsCodepoints())) {
								Configloader.INSTANCE.addReactionroleConfig(guild, finalchannel, msgid, actions[i]);
							}
						  }
						  finalchannel.retrieveMessageById(msgid).complete().removeReaction(e.getReactionEmote().getAsCodepoints()).queue();},
					1, TimeUnit.MINUTES,
					() -> {event.getHook().deleteOriginal().queue();
						   channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			return;
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("reactionrole", "0")
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
		Configloader.INSTANCE.setReactionroleConfig(guild, finalchannel, msgid, "");
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messages.add(e.getMessage());
								  List<Role> roles = e.getMessage().getMentionedRoles();
								  this.defineAddEmojis(roles);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineAddEmojis(List<Role> roles) {
		Role role = roles.get(progress);
		Message msg = channel.sendMessageEmbeds(AnswerEngine.ae.buildMessage(
				AnswerEngine.ae.getTitle(guild, user, "/commands/moderation/reactionrole:defineAddEmojis"),
				AnswerEngine.ae.getDescription(guild, user, "/commands/moderation/reactionrole:defineAddEmojis").replace("{role}", role.getAsMention()))).complete();
		messages.add(msg);
		waiter.waitForEvent(GuildMessageReactionAddEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  if(e.getUser().getIdLong() != user.getIdLong()) {return false;}
				  	  return e.getMessageId().equals(msg.getId());},
				e -> {Configloader.INSTANCE.addReactionroleConfig(guild, finalchannel, msgid, e.getReactionEmote().getAsCodepoints() + "_" + role.getId());
					  msg.editMessageEmbeds(AnswerEngine.ae.buildMessage(
								AnswerEngine.ae.getTitle(guild, user, "/commands/moderation/reactionrole:defineAddEmojis"),
								AnswerEngine.ae.getDescription(guild, user, "/commands/moderation/reactionrole:defineAddEmojis").replace("{role}", role.getAsMention())
								+ "\n->" + e.getReactionEmote().getEmoji())).queue();
					  progress++;
					  if (progress < roles.size()) {
						  this.defineAddEmojis(roles);
					  } else {
						  this.addReactions();
					  }},
				1, TimeUnit.MINUTES,
				() -> {this.cleanup();
					   channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});		
	}
	
	private void addReactions() {
		Message msg  = channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:adding")).complete();
		messages.add(msg);
		String[] actions = Configloader.INSTANCE.getReactionroleConfig(guild, finalchannel, msgid).split(";");
		for (int i = 0; i < actions.length; i++) {
			String[] temp1 = actions[i].split("_");
			finalchannel.retrieveMessageById(msgid).complete().addReaction(temp1[0]).queue();
		}
		try {Thread.sleep(2000);} catch (InterruptedException e) {}
		msg.editMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/reactionrole:success")).queue();
		try {Thread.sleep(5000);} catch (InterruptedException e) {}
		this.cleanup();
	}
	
	private void cleanup() {
		channel.deleteMessages(messages).queue();
		oevent.getHook().deleteOriginal().queue();
	}
}