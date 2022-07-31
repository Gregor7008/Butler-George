package base.assets;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import base.Bot;
import base.engines.EventAwaiter;
import base.engines.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;

public class AwaitTask<T extends GenericEvent> {
	
	private Guild guild;
	private User user;
	private Message message = null;
	private MessageChannel channel;
	private Predicate<T> additionalPredicate = null;
	private Consumer<T> onSuccess = null;
	private Runnable onTimeout = null;
	private List<String> componentIds = null;
	private boolean invalidInputReceived = false;
	private long timeoutDelay = 1;
	private TimeUnit timeoutDelayUnit = TimeUnit.MINUTES;
	private TimerTask timeoutTask = null;
	private long selfId = ThreadLocalRandom.current().nextLong(100000, 999999);
	private HashMap<Long, AwaitTask<T>> selfMap = null;
	
	
	public AwaitTask(Guild guild, User user, Message message,
			Predicate<T> additionalPredicate, Consumer<T> onSuccess, long timeoutDelay, TimeUnit timeoutDelayUnit, Runnable onTimeout) {
		this.guild = guild;
		this.user = user;
		this.message = message;
		this.channel = message.getChannel();
		this.additionalPredicate = additionalPredicate;
		this.onSuccess = onSuccess;
		this.timeoutDelay = timeoutDelay;
		this.timeoutDelayUnit = timeoutDelayUnit;
		this.onTimeout = onTimeout;
	}
	
	public AwaitTask(Guild guild, User user, Message message,
			Predicate<T> additionalPredicate, Consumer<T> onSuccess) {
		this.guild = guild;
		this.user = user;
		this.message = message;
		this.channel = message.getChannel();
		this.additionalPredicate = additionalPredicate;
		this.onSuccess = onSuccess;
	}

	public AwaitTask(Guild guild, User user, Message message,
			Consumer<T> onSuccess) {
		this.guild = guild;
		this.user = user;
		this.message = message;
		this.channel = message.getChannel();
		this.onSuccess = onSuccess;
	}
	
	public AwaitTask(Guild guild, User user, MessageChannel channel,
			Predicate<T> additionalPredicate, Consumer<T> onSuccess, long timeoutDelay, TimeUnit timeoutDelayUnit, Runnable onTimeout) {
		this.guild = guild;
		this.user = user;
		this.channel = channel;
		this.additionalPredicate = additionalPredicate;
		this.onSuccess = onSuccess;
		this.timeoutDelay = timeoutDelay;
		this.timeoutDelayUnit = timeoutDelayUnit;
		this.onTimeout = onTimeout;
	}
	
	public AwaitTask(Guild guild, User user, MessageChannel channel,
			Predicate<T> additionalPredicate, Consumer<T> onSuccess) {
		this.guild = guild;
		this.user = user;
		this.channel = channel;
		this.additionalPredicate = additionalPredicate;
		this.onSuccess = onSuccess;
	}

	public AwaitTask(Guild guild, User user, MessageChannel channel,
			Consumer<T> onSuccess) {
		this.guild = guild;
		this.user = user;
		this.channel = channel;
		this.onSuccess = onSuccess;
	}

	@SuppressWarnings("unchecked")
	public void append() {
		this.selfMap = (HashMap<Long, AwaitTask<T>>) EventAwaiter.INSTANCE.getMapOfType(this.getType());
		while (selfMap.containsKey(selfId)) {
			selfId = ThreadLocalRandom.current().nextLong(100000, 999999);
		}
		selfMap.put(selfId, this);
		timeoutTask = new TimerTask() {
			@Override
			public void run() {
				selfMap.remove(selfId);
				if (onTimeout != null) {
					onTimeout.run();
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
		Bot.INSTANCE.centralTimer.schedule(timeoutTask, timeoutDelayUnit.toMillis(timeoutDelay));
	}
	
	public AwaitTask<T> setInvalidInputReceived(boolean value) {
		this.invalidInputReceived = value;
		return this;
	}
	
	public AwaitTask<T> addComponentId(String ... ids) {
		if (ids.length > 0 && this.componentIds == null) {
			this.componentIds = new ArrayList<>();
		}
		for (String id : ids) {
			this.componentIds.add(id);
		}
		return this;
	}
	
	public void acceptSuccessConsumer(T event) {
		selfMap.remove(selfId);
		onSuccess.accept(event);
	}
	
	public String getType() {
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        String[] raw = genericSuperclass.getActualTypeArguments()[0].getTypeName().split("\\.");
        return raw[raw.length-1];
    }
	
	@NotNull
	public Guild getGuild() {
		return this.guild;
	}
	
	@NotNull
	public User getUser() {
		return this.user;
	}
	
	public Message getMessage() {
		return this.message;
	}
	
	@NotNull
	public MessageChannel getChannel() {
		return this.channel;
	}
	
	public Predicate<T> getAdditionalPredicate() {
		return this.additionalPredicate;
	}
	
	public List<String> getComponentIds() {
		return this.componentIds;
	}
	
	@NotNull
	public boolean getInvalidInputReceived() {
		return this.invalidInputReceived;
	}
	
	public TimerTask getTimeoutTask() {
		return this.timeoutTask;
	}
	
	@NotNull
	public long getSelfId() {
		return this.selfId;
	}
}