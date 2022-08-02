package base.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import base.Bot;
import base.engines.EventAwaiter;
import base.engines.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

public class AwaitTask<T extends GenericEvent> {
	
	public AwaitedEvent awaitedEvent;
	
	private Guild guild;
	private User user;
	private Message message;
	private MessageChannel channel;
	
	private Predicate<T> additionalPredicate;
	private Consumer<T> eventConsumer;
	private Runnable timeoutRunnable;
	private List<String> componentIds;
	
	private boolean invalidInputReceived = false;
	private boolean executed = false;
	private boolean cancelled = false;
	private TimerTask timeoutTask = null;
	private long selfId = 0L;
	
	public static AwaitTask<MessageReceivedEvent> forMessageReceival(Guild guild, User user, MessageChannel channel, Consumer<MessageReceivedEvent> onSuccess) {
		return AwaitTask.forMessageReceival(guild, user, channel, null, onSuccess, null);
	}
	
	public static AwaitTask<MessageReceivedEvent> forMessageReceival(Guild guild, User user, MessageChannel channel,
			@Nullable Predicate<MessageReceivedEvent> additionalPredicate, Consumer<MessageReceivedEvent> onSuccess, @Nullable Runnable onTimeout) {
		return new AwaitTask<MessageReceivedEvent>(AwaitedEvent.MESSAGE_RECEIVED_EVENT, guild, user, channel, additionalPredicate, onSuccess, onTimeout);
	}
	
	public static AwaitTask<MessageReactionAddEvent> forReactionAdding(Guild guild, User user, Message message, Consumer<MessageReactionAddEvent> onSuccess) {
		return AwaitTask.forReactionAdding(guild, user, message, null, onSuccess, null);
	}

	public static AwaitTask<MessageReactionAddEvent> forReactionAdding(Guild guild, User user, Message message,
			@Nullable Predicate<MessageReactionAddEvent> additionalPredicate, Consumer<MessageReactionAddEvent> onSuccess, @Nullable Runnable onTimeout) {
		return new AwaitTask<MessageReactionAddEvent>(AwaitedEvent.MESSAGE_REACTION_ADD_EVENT, guild, user, message, additionalPredicate, onSuccess, onTimeout);
		
	}
	
	public static AwaitTask<MessageReactionRemoveEvent> forReactionRemoval(Guild guild, User user, Message message, Consumer<MessageReactionRemoveEvent> onSuccess) {
		return AwaitTask.forReactionRemoval(guild, user, message, null, onSuccess, null);
	}
	
	public static AwaitTask<MessageReactionRemoveEvent> forReactionRemoval(Guild guild, User user, Message message,
			@Nullable Predicate<MessageReactionRemoveEvent> additionalPredicate, Consumer<MessageReactionRemoveEvent> onSuccess, @Nullable Runnable onTimeout) {
		return new AwaitTask<MessageReactionRemoveEvent>(AwaitedEvent.MESSAGE_REACTION_REMOVE_EVENT, guild, user, message, additionalPredicate, onSuccess, onTimeout);
		
	}
	
	public static AwaitTask<ButtonInteractionEvent> forButtonInteraction(Guild guild, User user, Message message, Consumer<ButtonInteractionEvent> onSuccess) {
		return AwaitTask.forButtonInteraction(guild, user, message, null, onSuccess, null);
	}
	
	public static AwaitTask<ButtonInteractionEvent> forButtonInteraction(Guild guild, User user, Message message,
			@Nullable Predicate<ButtonInteractionEvent> additionalPredicate, Consumer<ButtonInteractionEvent> onSuccess, @Nullable Runnable onTimeout) {
		return new AwaitTask<ButtonInteractionEvent>(AwaitedEvent.BUTTON_INTERACTION_EVENT, guild, user, message, additionalPredicate, onSuccess, onTimeout);
		
	}
	
	public static AwaitTask<SelectMenuInteractionEvent> forSelectMenuInteraction(Guild guild, User user, Message message, Consumer<SelectMenuInteractionEvent> onSuccess) {
		return AwaitTask.forSelectMenuInteraction(guild, user, message, null, onSuccess, null);
	}
	
	public static AwaitTask<SelectMenuInteractionEvent> forSelectMenuInteraction(Guild guild, User user, Message message,
			@Nullable Predicate<SelectMenuInteractionEvent> additionalPredicate, Consumer<SelectMenuInteractionEvent> onSuccess, @Nullable Runnable onTimeout) {
		return new AwaitTask<SelectMenuInteractionEvent>(AwaitedEvent.SELECT_MENU_INTERACTION_EVENT, guild, user, message, additionalPredicate, onSuccess, onTimeout);
		
	}
	
	public static AwaitTask<ModalInteractionEvent> forModalInteraction(Guild guild, User user, MessageChannel channel, Consumer<ModalInteractionEvent> onSuccess) {
		return AwaitTask.forModalInteraction(guild, user, channel, null, onSuccess, null);
	}
	
	public static AwaitTask<ModalInteractionEvent> forModalInteraction(Guild guild, User user, MessageChannel channel,
			@Nullable Predicate<ModalInteractionEvent> additionalPredicate, Consumer<ModalInteractionEvent> onSuccess, @Nullable Runnable onTimeout) {
		return new AwaitTask<ModalInteractionEvent>(AwaitedEvent.MODAL_INTERACTION_EVENT, guild, user, channel, additionalPredicate, onSuccess, onTimeout);
		
	}
	
	protected AwaitTask(AwaitedEvent awaitedEvent, Guild guild, User user, Message message,
			@Nullable Predicate<T> additionalPredicate, Consumer<T> onSuccess, @Nullable Runnable onTimeout) {
		this.awaitedEvent = awaitedEvent;
		this.guild = guild;
		this.user = user;
		this.message = message;
		this.channel = message.getChannel();
		this.additionalPredicate = additionalPredicate;
		this.eventConsumer = onSuccess;
		this.timeoutRunnable = onTimeout;
	}
	
	protected AwaitTask(AwaitedEvent awaitedEvent, Guild guild, User user, MessageChannel channel,
			@Nullable Predicate<T> additionalPredicate, Consumer<T> onSuccess, @Nullable Runnable onTimeout) {
		this.awaitedEvent = awaitedEvent;
		this.guild = guild;
		this.user = user;
		this.message = null;
		this.channel = channel;
		this.additionalPredicate = additionalPredicate;
		this.eventConsumer = onSuccess;
		this.timeoutRunnable = onTimeout;
	}
	
	public AwaitTask<T> addValidComponents(String ... componentIds) {
		if (componentIds.length > 0 && this.componentIds == null) {
			this.componentIds = new ArrayList<>();
		} else {
			this.componentIds = null;
		}
		for (String id : componentIds) {
			this.componentIds.add(id);
		}
		return this;
	}
	
	public void append() {
		AwaitTask<T> self = this;
		EventAwaiter.INSTANCE.appendTask(self);
		this.timeoutTask = new TimerTask() {
			@Override
			public void run() {
				EventAwaiter.INSTANCE.removeTask(selfId);
				cancelled = true;
				if (timeoutRunnable != null) {
					timeoutRunnable.run();
				} else {
					String keyword = "timeout";
					if (invalidInputReceived) {
						keyword = "invalid";
					}
					if (message != null) {
						message.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, keyword).convert()).setActionRows().queue();
					} else {
						channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, keyword).convert()).setActionRows().queue();
					}
				}
			}
		};
		Bot.INSTANCE.getTimer().schedule(timeoutTask, TimeUnit.MINUTES.toMillis(1));
	}
	
	public void cancel() {
		EventAwaiter.INSTANCE.removeTask(this.selfId);
		this.timeoutTask.cancel();
		this.cancelled = true;
	}
	
	public void complete(T event) {
		if (this.additionalPredicate != null) {
			if (!this.additionalPredicate.test(event)) {
				this.invalidInputReceived = true;
				return;
			}
		}
		if (!executed && !cancelled) {
			EventAwaiter.INSTANCE.removeTask(this.selfId);
			this.timeoutTask.cancel();
			this.executed = true;
			this.eventConsumer.accept(event);
		}
	}	
	
	public Guild getGuild() {
		return this.guild;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public Message getMessage() {
		return this.message;
	}
	
	public MessageChannel getChannel() {
		return this.channel;
	}
	
	public List<String> getComponentIds() {
		return componentIds;
	}
	
	public long getId() {
		return this.selfId;
	}
	
	public static enum AwaitedEvent {
		MESSAGE_RECEIVED_EVENT,
		MESSAGE_REACTION_ADD_EVENT,
		MESSAGE_REACTION_REMOVE_EVENT,
		BUTTON_INTERACTION_EVENT,
		SELECT_MENU_INTERACTION_EVENT,
		MODAL_INTERACTION_EVENT;
	}
}