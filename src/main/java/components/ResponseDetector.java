package components;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import base.Bot;
import components.base.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.Modal;

public abstract class ResponseDetector {
	
	public static void waitForMessage(Guild guild, User user, MessageChannel channel,
			  						  Consumer<MessageReceivedEvent> onSuccess) {
		ResponseDetector.waitForMessage(guild, user, channel, null, onSuccess,
				(invalidMessageGiven) -> {channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	public static void waitForMessage(Guild guild, User user, Message message,
			  						  Consumer<MessageReceivedEvent> onSuccess) {
		ResponseDetector.waitForMessage(guild, user, message.getChannel(), null, onSuccess,
				(invalidMessageGiven) -> {message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	public static void waitForMessage(Guild guild, User user, MessageChannel channel,
			  						  Consumer<MessageReceivedEvent> onSuccess, Runnable onTimeout) {
		ResponseDetector.waitForMessage(guild, user, channel, null, onSuccess, b -> onTimeout.run());
	}
	
	public static void waitForMessage(Guild guild, User user, MessageChannel channel, Predicate<MessageReceivedEvent> additionalCondition,
	   	      					      Consumer<MessageReceivedEvent> onSuccess) {
		ResponseDetector.waitForMessage(guild, user, channel, additionalCondition, onSuccess,
				(invalidMessageGiven) -> {if (invalidMessageGiven) {
						   invalidMessageGiven = false;
						   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "invalid").convert()).queue();
					   } else {
						   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();
					   }
				});
	}
	
	public static void waitForMessage(Guild guild, User user, Message message, Predicate<MessageReceivedEvent> additionalCondition,
									  Consumer<MessageReceivedEvent> onSuccess) {
		ResponseDetector.waitForMessage(guild, user, message.getChannel(), additionalCondition, onSuccess,
				(invalidMessageGiven) -> {if (invalidMessageGiven) {
					   	   invalidMessageGiven = false;
					   	   message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "invalid").convert()).queue();
				   	   } else {
				   		   message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();
				   	   }
				});
	}
	
	public static void waitForMessage(Guild guild, User user, MessageChannel channel, Predicate<MessageReceivedEvent> additionalCondition,
							   	      Consumer<MessageReceivedEvent> onSuccess, Consumer<Boolean> onTimeout) {
		AtomicReference<Boolean> invalidMessageGiven = new AtomicReference<>(false);
		Bot.INSTANCE.eventWaiter.waitForEvent(MessageReceivedEvent.class,
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
 						  if (additionalCondition.test(e)) {
 							  return true;
 						  } else {
 							  invalidMessageGiven.set(true);
 							  return false;
 						  }
					  } else {
						  return true;
					  }
				},
				onSuccess,
				1, TimeUnit.MINUTES,
				() -> {onTimeout.accept(invalidMessageGiven.get());});
	}
	
	public static void waitForReaction(Guild guild, User user, Message message,
			  						  Consumer<MessageReactionAddEvent> onSuccess) {
		ResponseDetector.waitForReaction(guild, user, message, onSuccess,
				() -> {message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();
					   message.clearReactions().queue();});
	}
	
	public static void waitForReaction(Guild guild, User user, Message message,
			   				    	   Consumer<MessageReactionAddEvent> onSuccess, Runnable onTimeout) {
		Bot.INSTANCE.eventWaiter.waitForEvent(MessageReactionAddEvent.class,
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
				onSuccess,
				1, TimeUnit.MINUTES,
				onTimeout);
	}
	
	public static void waitForButtonClick(Guild guild, User user, Message message, @Nullable List<String> buttonIDs,
			  					       Consumer<ButtonInteractionEvent> onSuccess) {
		ResponseDetector.waitForButtonClick(guild, user, message, buttonIDs, onSuccess,
				() -> {message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	public static void waitForButtonClick(Guild guild, User user, Message message, @Nullable  List<String> buttonIDs,
			   					   		  Consumer<ButtonInteractionEvent> onSuccess, Runnable onTimeout) {
		Bot.INSTANCE.eventWaiter.waitForEvent(ButtonInteractionEvent.class,
				e -> {if (!e.getGuild().getId().equals(guild.getId())) {
					  	  return false;
				  	  }
					  if (!e.getChannel().getId().equals(message.getChannel().getId())) {
						  return false;
					  }
					  if (!e.getMessage().getId().equals(message.getId())) {
						  return false;
					  }
					  if (buttonIDs != null) {
						  if (!buttonIDs.contains(e.getButton().getId())) {
							  return false;
						  }
					  }
					  return e.getUser().getIdLong() == user.getIdLong();
				},
				onSuccess,
				1, TimeUnit.MINUTES,
				onTimeout);
	}

	public static void waitForMenuSelection(Guild guild, User user, Message message, @Nullable String menuID,
		       						      Consumer<SelectMenuInteractionEvent> onSuccess) {
		ResponseDetector.waitForMenuSelection(guild, user, message, menuID, onSuccess,
				() -> {message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	public static void waitForMenuSelection(Guild guild, User user, Message message, @Nullable String menuID,
											Consumer<SelectMenuInteractionEvent> onSuccess, Runnable onTimeout) {
		Bot.INSTANCE.eventWaiter.waitForEvent(SelectMenuInteractionEvent.class,
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
				onSuccess,
				1, TimeUnit.MINUTES,
				onTimeout);
	}
	
	public static void waitForModalInput(Guild guild, User user, Message message, Modal modal,
										 Consumer<ModalInteractionEvent> onSuccess) {
		ResponseDetector.waitForModalInput(guild, user, message.getChannel(), modal, onSuccess,
				() -> {message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	public static void waitForModalInput(Guild guild, User user, MessageChannel channel, Modal modal,
										 Consumer<ModalInteractionEvent> onSuccess, Runnable onTimeout) {
	Bot.INSTANCE.eventWaiter.waitForEvent(ModalInteractionEvent.class,
			e -> {if (!e.getGuild().getId().equals(guild.getId())) {
			  	  	  return false;
		  	  	  }
			  	  if (!e.getMessageChannel().getId().equals(channel.getId())) {
			  		  return false;
			  	  }
			  	  if (!e.getModalId().equals(modal.getId())) {
			  		  return false;
			  	  }
			  	  return e.getUser().getIdLong() == user.getIdLong();
			},
			onSuccess,
			1, TimeUnit.MINUTES,
			onTimeout);
	}
}