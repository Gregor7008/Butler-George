package components.utilities;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import components.base.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class ResponseDetector {

	public static EventWaiter eventWaiter = new EventWaiter();
	
	public static void waitForMessage(Guild guild, User user, MessageChannel channel, Predicate<MessageReceivedEvent> additionalCondition,
	   	      					      Consumer<MessageReceivedEvent> onSuccess) {
		ResponseDetector.waitForMessage(guild, user, channel, additionalCondition, onSuccess,
				() -> {channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	public static void waitForMessage(Guild guild, User user, MessageChannel channel, Predicate<MessageReceivedEvent> additionalCondition,
							   	      Consumer<MessageReceivedEvent> onSuccess, Runnable onTimeout) {
		eventWaiter.waitForEvent(MessageReceivedEvent.class,
				e -> {if (!e.getGuild().getId().equals(guild.getId())) {
						  return false;
					  }
					  if (!e.getChannel().getId().equals(channel.getId())) {
					  	  return false;
					  }
					  if (!e.getAuthor().getId().equals(user.getId())) {
						  return false;
					  }
					  if (additionalCondition != null) {
						  return additionalCondition.test(e);
					  } else {
						  return true;
					  }
				},
				e -> {onSuccess.accept(e);
				},
				1, TimeUnit.MINUTES,
				() -> {onTimeout.run();});
	}
	
	public static void waitForMessage(Guild guild, User user, MessageChannel channel,
			      					  Consumer<MessageReceivedEvent> onSuccess) {
		ResponseDetector.waitForMessage(guild, user, channel, onSuccess,
				() -> {channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	public static void waitForMessage(Guild guild, User user, MessageChannel channel,
	   	      						  Consumer<MessageReceivedEvent> onSuccess, Runnable onTimeout) {
		eventWaiter.waitForEvent(MessageReceivedEvent.class,
				e -> {if (!e.getGuild().getId().equals(guild.getId())) {
					      return false;
					  }
					  if (!e.getChannel().getId().equals(channel.getId())) {
						  return false;
					  }
					  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {onSuccess.accept(e);
				},
				1, TimeUnit.MINUTES,
				() -> {onTimeout.run();});
	}
	
	public static void waitForReaction(Guild guild, User user, Message message,
			  						  Consumer<MessageReactionAddEvent> onSuccess) {
		ResponseDetector.waitForReaction(guild, user, message, onSuccess,
				() -> {message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	public static void waitForReaction(Guild guild, User user, Message message,
			   				    	   Consumer<MessageReactionAddEvent> onSuccess, Runnable onTimeout) {
		eventWaiter.waitForEvent(MessageReactionAddEvent.class,
				e -> {if (!e.getGuild().getId().equals(guild.getId())) {
					  	  return false;
				  	  }
					  if (!e.getChannel().getId().equals(message.getChannel().getId())) {
					   	  return false;
					  }
					  if (!e.getMessageId().equals(message.getId())) {
						  return false;
					  }
					  return e.getUser().getIdLong() == user.getIdLong();
				},
				e -> {onSuccess.accept(e);
				},
				1, TimeUnit.MINUTES,
				() -> {onTimeout.run();});
	}
	
	public static void waitForButtonClick(Guild guild, User user, Message message, @Nullable String buttonID,
			  					       Consumer<ButtonInteractionEvent> onSuccess) {
		ResponseDetector.waitForButtonClick(guild, user, message, buttonID, onSuccess,
				() -> {message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	public static void waitForButtonClick(Guild guild, User user, Message message, @Nullable String buttonID,
			   					   		  Consumer<ButtonInteractionEvent> onSuccess, Runnable onTimeout) {
		eventWaiter.waitForEvent(ButtonInteractionEvent.class,
				e -> {if (!e.getGuild().getId().equals(guild.getId())) {
					  	  return false;
				  	  }
					  if (!e.getChannel().getId().equals(message.getChannel().getId())) {
						  return false;
					  }
					  if (!e.getMessage().getId().equals(message.getId())) {
						  return false;
					  }
					  if (buttonID != null && !e.getButton().getId().equals(buttonID)) {
						  return false;
					  }
					  return e.getUser().getIdLong() == user.getIdLong();
				},
				e -> {onSuccess.accept(e);
					  Toolbox.disableActionRows(message);
				},
				1, TimeUnit.MINUTES,
				() -> {onTimeout.run();
					   Toolbox.disableActionRows(message);});
	}

	public static void waitForMenuSelection(Guild guild, User user, Message message, @Nullable String menuID,
		       						      Consumer<SelectMenuInteractionEvent> onSuccess) {
		ResponseDetector.waitForMenuSelection(guild, user, message, menuID, onSuccess,
				() -> {message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	public static void waitForMenuSelection(Guild guild, User user, Message message, @Nullable String menuID,
											Consumer<SelectMenuInteractionEvent> onSuccess, Runnable onTimeout) {
		eventWaiter.waitForEvent(SelectMenuInteractionEvent.class,
				e -> {if (!e.getGuild().getId().equals(guild.getId())) {
					  	  return false;
				  	  }
					  if (!e.getChannel().getId().equals(message.getChannel().getId())) {
						  return false;
					  }
					  if (!e.getMessageId().equals(message.getId())) {
						  return false;
					  }
					  if (menuID != null && !e.getSelectMenu().getId().equals(menuID)) {
						  return false;
					  }
					  return e.getUser().getIdLong() == user.getIdLong();
				},
				e -> {onSuccess.accept(e);
					  Toolbox.disableActionRows(message);
				},
				1, TimeUnit.MINUTES,
				() -> {onTimeout.run();
					   Toolbox.disableActionRows(message);});
	}
}