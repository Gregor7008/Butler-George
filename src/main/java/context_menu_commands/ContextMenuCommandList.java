package context_menu_commands;

import java.util.concurrent.ConcurrentHashMap;

import context_menu_commands.assets.MessageContextEventHandler;
import context_menu_commands.assets.UserContextEventHandler;
import context_menu_commands.moderation.Mute;
import context_menu_commands.moderation.TempBan;
import context_menu_commands.moderation.TempMute;
import context_menu_commands.moderation.Unmute;

public class ContextMenuCommandList {
	
	public ConcurrentHashMap<String, MessageContextEventHandler> messageContextEventHandlers = new ConcurrentHashMap<String, MessageContextEventHandler>();
	public ConcurrentHashMap<String, UserContextEventHandler> userContextEventHandlers = new ConcurrentHashMap<String, UserContextEventHandler>();
	
	public ContextMenuCommandList() {
		//Moderation
		userContextEventHandlers.put("mute", new Mute());
		userContextEventHandlers.put("tempban", new TempBan());
		userContextEventHandlers.put("tempmute", new TempMute());
		userContextEventHandlers.put("unmute", new Unmute());
	}
}