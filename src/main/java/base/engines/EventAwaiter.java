package base.engines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private List<Long> ids = new ArrayList<>();
	
	public EventAwaiter() {
		INSTANCE = this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GenericEvent> long appendTask(AwaitTask<T> task) {
		long id = 0L;
		while (ids.contains(id)) {
			id = ThreadLocalRandom.current().nextLong(100000, 999999);
		}
		switch (task.awaitedEvent) {
		case BUTTON_INTERACTION_EVENT:
			awaitingButtonInteraction.put(id, (AwaitTask<ButtonInteractionEvent>) task);
			break;
		case MESSAGE_REACTION_ADD_EVENT:
			awaitingReactionAdding.put(id, (AwaitTask<MessageReactionAddEvent>) task);
			break;
		case MESSAGE_REACTION_REMOVE_EVENT:
			awaitingReactionRemoval.put(id, (AwaitTask<MessageReactionRemoveEvent>) task);
			break;
		case MESSAGE_RECEIVED_EVENT:
			awaitingMessageReceival.put(id, (AwaitTask<MessageReceivedEvent>) task);
			break;
		case MODAL_INTERACTION_EVENT:
			awaitingModalInteraction.put(id, (AwaitTask<ModalInteractionEvent>) task);
			break;
		case SELECT_MENU_INTERACTION_EVENT:
			awaitingSelectMenuInteraction.put(id, (AwaitTask<SelectMenuInteractionEvent>) task);
			break;
		default:
			throw new IllegalArgumentException("Invalid awaited event " + task.awaitedEvent.name() + "!");
		}
		ids.add(id);
		return id;
	}
	
	public void removeTask(long id) {
		awaitingButtonInteraction.remove(id);
		awaitingReactionAdding.remove(id);
		awaitingReactionRemoval.remove(id);
		awaitingMessageReceival.remove(id);
		awaitingModalInteraction.remove(id);
		awaitingSelectMenuInteraction.remove(id);
		ids.remove(id);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		for (AwaitTask<MessageReceivedEvent> task : awaitingMessageReceival.values()) {
			if (!event.getAuthor().isBot()
					&& event.getGuild().getId().equals(task.getGuild().getId())
					&& event.getAuthor().getId().equals(task.getUser().getId())
					&& event.getChannel().getId().equals(task.getChannel().getId())
					&& ids.contains(task.getId())) {
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
					&& event.getMessageId().equals(task.getMessage().getId())
					&& ids.contains(task.getId())) {
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
					&& event.getMessageId().equals(task.getMessage().getId())
					&& ids.contains(task.getId())) {
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
					&& event.getMessageId().equals(task.getMessage().getId())
					&& ids.contains(task.getId())) {
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
					&& event.getMessageId().equals(task.getMessage().getId())
					&& ids.contains(task.getId())) {
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
					&& event.getChannel().getId().equals(task.getChannel().getId())
					&& ids.contains(task.getId())) {
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