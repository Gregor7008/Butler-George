package context;

import java.util.concurrent.ConcurrentHashMap;

import components.context.MessageContextEventHandler;
import components.context.UserContextEventHandler;
import context.moderation.Mute;
import context.moderation.TempBan;
import context.moderation.TempMute;
import context.moderation.Unmute;

public class ContextList {
	
	public ConcurrentHashMap<String, MessageContextEventHandler> messageContextEventHandlers = new ConcurrentHashMap<String, MessageContextEventHandler>();
	public ConcurrentHashMap<String, UserContextEventHandler> userContextEventHandlers = new ConcurrentHashMap<String, UserContextEventHandler>();
	
	public ContextList() {
		//Moderation
		userContextEventHandlers.put("mute", new Mute());
		userContextEventHandlers.put("tempban", new TempBan());
		userContextEventHandlers.put("tempmute", new TempMute());
		userContextEventHandlers.put("unmute", new Unmute());
	}
}