package base.engines;

import java.util.HashMap;

import base.assets.AwaitTask;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventAwaiter extends ListenerAdapter {

	public static EventAwaiter INSTANCE;
	
	private HashMap<Long, AwaitTask<MessageReceivedEvent>> awaitingMessageReceival = new HashMap<>();
	private HashMap<Long, AwaitTask<MessageReactionAddEvent>> awaitingReactionAdded = new HashMap<>();
	private HashMap<Long, AwaitTask<MessageReactionRemoveEvent>> awaitingReactionRemoval = new HashMap<>();
	private HashMap<Long, AwaitTask<ButtonInteractionEvent>> awaitingButtonInteraction = new HashMap<>();
	private HashMap<Long, AwaitTask<SelectMenuInteractionEvent>> awaitingSelectMenuInteraction = new HashMap<>();
	private HashMap<Long, AwaitTask<ModalInteractionEvent>> awaitingModalInteraction = new HashMap<>();
	
	public EventAwaiter() {
		INSTANCE = this;
	}
	
	public HashMap<Long, ?> getMapOfType(String type) {
		switch (type.getClass().getName()) {
		case "ButtonInteractionEvent":
			return this.awaitingButtonInteraction;
		case "MessageReceivedEvent":
			return this.awaitingMessageReceival;
		case "ModalInteractionEvent":
			return this.awaitingModalInteraction;
		case "MessageReactionAddEvent":
			return this.awaitingReactionAdded;
		case "MessageReactionRemoveEvent":
			return this.awaitingReactionRemoval;
		case "SelectMenuInteractionEvent":
			return this.awaitingSelectMenuInteraction;
		default:
			throw new IllegalArgumentException("Invalid task type!");
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		for (AwaitTask<MessageReceivedEvent> task : awaitingMessageReceival.values()) {
			if (event.getGuild().getId().equals(task.getGuild().getId())
					&& event.getAuthor().getId().equals(task.getUser().getId())
					&& event.getChannel().getId().equals(task.getChannel().getId())) {
				this.finalizeChecks(event, task);
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		for (AwaitTask<MessageReactionAddEvent> task : awaitingReactionAdded.values()) {
			if (event.getGuild().getId().equals(task.getGuild().getId())
					&& event.getUser().getId().equals(task.getUser().getId())
					&& event.getChannel().getId().equals(task.getChannel().getId())
					&& event.getMessageId().equals(task.getMessage().getId())) {
				this.finalizeChecks(event, task);
			}
		}
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		for (AwaitTask<MessageReactionRemoveEvent> task : awaitingReactionRemoval.values()) {
			if (!event.getGuild().getId().equals(task.getGuild().getId())
					&& event.getUser().getId().equals(task.getUser().getId())
					&& event.getChannel().getId().equals(task.getChannel().getId())
					&& event.getMessageId().equals(task.getMessage().getId())) {
				this.finalizeChecks(event, task);
			}
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		for (AwaitTask<ButtonInteractionEvent> task : awaitingButtonInteraction.values()) {
			if (event.getGuild().getId().equals(task.getGuild().getId())
					&& event.getUser().getId().equals(task.getUser().getId())
					&& event.getChannel().getId().equals(task.getChannel().getId())
					&& event.getMessageId().equals(task.getMessage().getId())) {
				if (task.getComponentIds() != null) {
					if (task.getComponentIds().contains(event.getComponentId())) {
						this.finalizeChecks(event, task);
					}
				} else {
					this.finalizeChecks(event, task);
				}
			}
		}
	}

	@Override
	public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
		for (AwaitTask<SelectMenuInteractionEvent> task : awaitingSelectMenuInteraction.values()) {
			if (event.getGuild().getId().equals(task.getGuild().getId())
					&& event.getUser().getId().equals(task.getUser().getId())
					&& event.getChannel().getId().equals(task.getChannel().getId())
					&& event.getMessageId().equals(task.getMessage().getId())) {
				if (task.getComponentIds() != null) {
					if (task.getComponentIds().contains(event.getComponentId())) {
						this.finalizeChecks(event, task);
					}
				} else {
					this.finalizeChecks(event, task);
				}
			}
		}
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		for (AwaitTask<ModalInteractionEvent> task : awaitingModalInteraction.values()) {
			if (event.getGuild().getId().equals(task.getGuild().getId())
					&& event.getUser().getId().equals(task.getUser().getId())
					&& event.getChannel().getId().equals(task.getChannel().getId())) {
				if (task.getComponentIds() != null) {
					if (task.getComponentIds().contains(event.getModalId())) {
						this.finalizeChecks(event, task);
					}
				} else {
					this.finalizeChecks(event, task);
				}
			}
		}
	}
	
	private <T extends GenericEvent> void finalizeChecks(T event, AwaitTask<T> task) {
		if (task.getAdditionalPredicate() != null) {
			if (task.getAdditionalPredicate().test(event)) {
				return;
			} else {
				task.setInvalidInputReceived(true);
				return;
			}
		} else {
			task.getTimeoutTask().cancel();
			task.acceptSuccessConsumer(event);
		}
	}
}