package base.engines;

import java.util.ArrayList;
import java.util.List;

import base.assets.AwaitTask;
import base.engines.logging.ConsoleEngine;
import base.engines.logging.Logger;
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
	
	private static Logger LOG = ConsoleEngine.getLogger(EventAwaiter.class);
	
	private List<AwaitTask<MessageReceivedEvent>> awaitingMessageReceival = new ArrayList<>();
	private List<AwaitTask<MessageReactionAddEvent>> awaitingReactionAdding = new ArrayList<>();
	private List<AwaitTask<MessageReactionRemoveEvent>> awaitingReactionRemoval = new ArrayList<>();
	private List<AwaitTask<ButtonInteractionEvent>> awaitingButtonInteraction = new ArrayList<>();
	private List<AwaitTask<SelectMenuInteractionEvent>> awaitingSelectMenuInteraction = new ArrayList<>();
	private List<AwaitTask<ModalInteractionEvent>> awaitingModalInteraction = new ArrayList<>();
	
	public EventAwaiter() {
		INSTANCE = this;
	}
	
	@Override
	public String toString() {
		StringBuilder sB = new StringBuilder();
		String prefix = null;
		if (awaitingMessageReceival.size() > 0) {
			sB.append("Awaiting message receival (" + awaitingMessageReceival.size() + "):");
			awaitingMessageReceival.forEach(task -> {
				sB.append("\n" + task.getGuild().getName() + ", " + task.getUser().getName());
			});
			prefix = "\n";
		}
		if (awaitingReactionAdding.size() > 0) {
			sB.append(prefix + "Awaiting reaction adding (" + awaitingReactionAdding.size() + "):");
			awaitingReactionAdding.forEach(task -> {
				sB.append("\n" + task.getGuild().getName() + ", " + task.getUser().getName());
			});
			prefix = "\n";
		}
		if (awaitingReactionRemoval.size() > 0) {
			sB.append(prefix + "Awaiting reaction removal (" + awaitingReactionRemoval.size() + "):");
			awaitingReactionRemoval.forEach(task -> {
				sB.append("\n" + task.getGuild().getName() + ", " + task.getUser().getName());
			});
			prefix = "\n";
		}
		if (awaitingButtonInteraction.size() > 0) {
			sB.append(prefix + "Awaiting button interaction (" + awaitingButtonInteraction.size() + "):");
			awaitingButtonInteraction.forEach(task -> {
				sB.append("\n" + task.getGuild().getName() + ", " + task.getUser().getName());
			});
			prefix = "\n";
		}
		if (awaitingSelectMenuInteraction.size() > 0) {
			sB.append(prefix + "Awaiting select menu interaction (" + awaitingSelectMenuInteraction.size() + "):");
			awaitingSelectMenuInteraction.forEach(task -> {
				sB.append("\n" + task.getGuild().getName() + ", " + task.getUser().getName());
			});
			prefix = "\n";
		}
		if (awaitingModalInteraction.size() > 0) {
			sB.append(prefix + "Awaiting modal interaction (" + awaitingModalInteraction.size() + "):");
			awaitingModalInteraction.forEach(task -> {
				sB.append("\n" + task.getGuild().getName() + ", " + task.getUser().getName());
			});
			prefix = "\n";
		}
		if (prefix == null) {
			sB.append("The EventAwaiter instance doesn't wait for any events at the moment!");
		}
		return sB.toString();
	}
	
	public void clear() {
		for (int i = 0; i < awaitingButtonInteraction.size(); i++) {
			awaitingButtonInteraction.get(0).cancel();
		}
		for (int i = 0; i < awaitingReactionAdding.size(); i++) {
			awaitingReactionAdding.get(0).cancel();
		}
		for (int i = 0; i < awaitingReactionRemoval.size(); i++) {
			awaitingReactionRemoval.get(0).cancel();
		}
		for (int i = 0; i < awaitingMessageReceival.size(); i++) {
			awaitingMessageReceival.get(0).cancel();
		}
		for (int i = 0; i < awaitingModalInteraction.size(); i++) {
			awaitingModalInteraction.get(0).cancel();
		}
		for (int i = 0; i < awaitingSelectMenuInteraction.size(); i++) {
			awaitingSelectMenuInteraction.get(0).cancel();
		}
		LOG.info("EventAwaiter successfully cleared!");
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GenericEvent> void appendTask(AwaitTask<T> task) {
		switch (task.awaitedEvent) {
		case BUTTON_INTERACTION_EVENT:
			awaitingButtonInteraction.add((AwaitTask<ButtonInteractionEvent>) task);
			break;
		case MESSAGE_REACTION_ADD_EVENT:
			awaitingReactionAdding.add((AwaitTask<MessageReactionAddEvent>) task);
			break;
		case MESSAGE_REACTION_REMOVE_EVENT:
			awaitingReactionRemoval.add((AwaitTask<MessageReactionRemoveEvent>) task);
			break;
		case MESSAGE_RECEIVED_EVENT:
			awaitingMessageReceival.add((AwaitTask<MessageReceivedEvent>) task);
			break;
		case MODAL_INTERACTION_EVENT:
			awaitingModalInteraction.add((AwaitTask<ModalInteractionEvent>) task);
			break;
		case SELECT_MENU_INTERACTION_EVENT:
			awaitingSelectMenuInteraction.add((AwaitTask<SelectMenuInteractionEvent>) task);
			break;
		default:
			throw new IllegalArgumentException("Invalid awaited event " + task.awaitedEvent.name() + "!");
		}
	}
	
	public <T extends GenericEvent> void removeTask(AwaitTask<T> task) {
		switch (task.awaitedEvent) {
		case BUTTON_INTERACTION_EVENT:
			awaitingButtonInteraction.remove(task);
			break;
		case MESSAGE_REACTION_ADD_EVENT:
			awaitingReactionAdding.remove(task);
			break;
		case MESSAGE_REACTION_REMOVE_EVENT:
			awaitingReactionRemoval.remove(task);
			break;
		case MESSAGE_RECEIVED_EVENT:
			awaitingMessageReceival.remove(task);
			break;
		case MODAL_INTERACTION_EVENT:
			awaitingModalInteraction.remove(task);
			break;
		case SELECT_MENU_INTERACTION_EVENT:
			awaitingSelectMenuInteraction.remove(task);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		List<AwaitTask<MessageReceivedEvent>> listCopy = List.copyOf(awaitingMessageReceival);
		for (int i = 0; i < listCopy.size(); i++) {
			AwaitTask<MessageReceivedEvent> task = listCopy.get(i);
			if (!event.getAuthor().isBot()
					&& event.getGuild().getId().equals(task.getGuild().getId())
					&& event.getAuthor().getId().equals(task.getUser().getId())
					&& event.getChannel().getId().equals(task.getChannel().getId())) {
				task.complete(event);
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		List<AwaitTask<MessageReactionAddEvent>> listCopy = List.copyOf(awaitingReactionAdding);
		for (int i = 0; i < listCopy.size(); i++) {
			AwaitTask<MessageReactionAddEvent> task = listCopy.get(i);
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
		List<AwaitTask<MessageReactionRemoveEvent>> listCopy = List.copyOf(awaitingReactionRemoval);
		for (int i = 0; i < listCopy.size(); i++) {
			AwaitTask<MessageReactionRemoveEvent> task = listCopy.get(i);
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
		List<AwaitTask<ButtonInteractionEvent>> listCopy = List.copyOf(awaitingButtonInteraction);
		for (int i = 0; i < listCopy.size(); i++) {
			AwaitTask<ButtonInteractionEvent> task = listCopy.get(i);
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
		List<AwaitTask<SelectMenuInteractionEvent>> listCopy = List.copyOf(awaitingSelectMenuInteraction);
		for (int i = 0; i < listCopy.size(); i++) {
			AwaitTask<SelectMenuInteractionEvent> task = listCopy.get(i);
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
		List<AwaitTask<ModalInteractionEvent>> listCopy = List.copyOf(awaitingModalInteraction);
		for (int i = 0; i < listCopy.size(); i++) {
			AwaitTask<ModalInteractionEvent> task = listCopy.get(i);
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