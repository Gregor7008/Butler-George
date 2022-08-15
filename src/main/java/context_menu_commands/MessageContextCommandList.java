package context_menu_commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import context_menu_commands.assets.MessageContextEventHandler;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class MessageContextCommandList {
	
	private static final HashMap<String, MessageContextEventHandler> handlers = new HashMap<>();
	private static final HashMap<String, CommandData> data = new HashMap<>();
	
	public static void create() {}
	
	public static void add(MessageContextEventHandler handler) {
		CommandData commandData = handler.initialize();
		String name = commandData.getName();
		handlers.put(name, handler);
		data.put(name, commandData);
	}
	
	public static void remove(String name) {
		handlers.remove(name);
		data.remove(name);
	}
	
	public static HashMap<String, MessageContextEventHandler> getHandlers() {
		return handlers;
	}
	
	public static MessageContextEventHandler getHandler(String name) {
		return handlers.get(name);
	}
	
	public static List<CommandData> getCommandData() {
		List<CommandData> commandDataList =  new ArrayList<>();
		commandDataList.addAll(data.values());
		return commandDataList;
	}
	
	public static CommandData getCommandData(String name) {
		return data.get(name);
	}
}