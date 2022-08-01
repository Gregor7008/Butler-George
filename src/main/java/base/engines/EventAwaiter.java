package base.engines;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

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
	private HashMap<Long, AwaitTask<MessageReactionAddEvent>> awaitingReactionAdding = new HashMap<>();
	private HashMap<Long, AwaitTask<MessageReactionRemoveEvent>> awaitingReactionRemoval = new HashMap<>();
	private HashMap<Long, AwaitTask<ButtonInteractionEvent>> awaitingButtonInteraction = new HashMap<>();
	private HashMap<Long, AwaitTask<SelectMenuInteractionEvent>> awaitingSelectMenuInteraction = new HashMap<>();
	private HashMap<Long, AwaitTask<ModalInteractionEvent>> awaitingModalInteraction = new HashMap<>();
	
	public EventAwaiter() {
		INSTANCE = this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GenericEvent> long appendTask(AwaitTask<T> task) {
		long id = 0L;
		switch (task.awaitedEvent) {
		case BUTTON_INTERACTION_EVENT:
			while (awaitingButtonInteraction.containsKey(id) || id == 0L) {
				id = this.newId();
			}
			awaitingButtonInteraction.put(id, (AwaitTask<ButtonInteractionEvent>) task);
			break;
		case MESSAGE_REACTION_ADD_EVENT:
			while (awaitingReactionAdding.containsKey(id) || id == 0L) {
				id = this.newId();
			}
			awaitingReactionAdding.put(id, (AwaitTask<MessageReactionAddEvent>) task);
			break;
		case MESSAGE_REACTION_REMOVE_EVENT:
			while (awaitingReactionRemoval.containsKey(id) || id == 0L) {
				id = this.newId();
			}
			awaitingReactionRemoval.put(id, (AwaitTask<MessageReactionRemoveEvent>) task);
			break;
		case MESSAGE_RECEIVED_EVENT:
			while (awaitingMessageReceival.containsKey(id) || id == 0L) {
				id = this.newId();
			}
			awaitingMessageReceival.put(id, (AwaitTask<MessageReceivedEvent>) task);
			break;
		case MODAL_INTERACTION_EVENT:
			while (awaitingModalInteraction.containsKey(id) || id == 0L) {
				id = this.newId();
			}
			awaitingModalInteraction.put(id, (AwaitTask<ModalInteractionEvent>) task);
			break;
		case SELECT_MENU_INTERACTION_EVENT:
			while (awaitingSelectMenuInteraction.containsKey(id) || id == 0L) {
				id = this.newId();
			}
			awaitingSelectMenuInteraction.put(id, (AwaitTask<SelectMenuInteractionEvent>) task);
			break;
		default:
			throw new IllegalArgumentException("Invalid awaited event " + task.awaitedEvent.name() + "!");
		}
		return id;
	}
	
	public <T extends GenericEvent> void removeTask(AwaitTask<T> task) {
		long id = task.getId();
		switch (task.awaitedEvent) {
		case BUTTON_INTERACTION_EVENT:
			awaitingButtonInteraction.remove(id);
			break;
		case MESSAGE_REACTION_ADD_EVENT:
			awaitingReactionAdding.remove(id);
			break;
		case MESSAGE_REACTION_REMOVE_EVENT:
			awaitingReactionRemoval.remove(id);
			break;
		case MESSAGE_RECEIVED_EVENT:
			awaitingMessageReceival.remove(id);
			break;
		case MODAL_INTERACTION_EVENT:
			awaitingModalInteraction.remove(id);
			break;
		case SELECT_MENU_INTERACTION_EVENT:
			awaitingSelectMenuInteraction.remove(id);
			break;
		default:
			throw new IllegalArgumentException("Invalid awaited event " + task.awaitedEvent.name() + "!");
		}
	}
	
	private long newId() {
		return ThreadLocalRandom.current().nextLong(100000, 999999);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		for (AwaitTask<MessageReceivedEvent> task : awaitingMessageReceival.values()) {
			if (event.getGuild().getId().equals(task.getGuild().getId())
					&& event.getAuthor().getId().equals(task.getUser().getId())
					&& event.getChannel().getId().equals(task.getChannel().getId())) {
				task.complete(event);
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		for (AwaitTask<MessageReactionAddEvent> task : awaitingReactionAdding.values()) {
			if (event.getGuild().getId().equals(task.getGuild().getId())
					&& event.getUser().getId().equals(task.getUser().getId())
					&& event.getChannel().getId().equals(task.getChannel().getId())
					&& event.getMessageId().equals(task.getMessage().getId())) {
				task.complete(event);
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
				task.complete(event);
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
						task.complete(event);
					}
				} else {
					task.complete(event);
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
						task.complete(event);
					}
				} else {
					task.complete(event);
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
						task.complete(event);
					}
				} else {
					task.complete(event);
				}
			}
		}
	}
}