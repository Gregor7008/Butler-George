package functions.context_menu_commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import assets.functions.UserContextEventHandler;
import functions.context_menu_commands.user_context.Mute;
import functions.context_menu_commands.user_context.TempBan;
import functions.context_menu_commands.user_context.TempMute;
import functions.context_menu_commands.user_context.Unmute;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class UserContextCommandList {
	
	private static final HashMap<String, UserContextEventHandler> handlers = new HashMap<>();
	private static final HashMap<String, CommandData> data = new HashMap<>();
	
	public static void create() {
		add(new Mute());
		add(new TempBan());
		add(new TempMute());
		add(new Unmute());
	}
	
	public static void add(UserContextEventHandler handler) {
		CommandData commandData = handler.initialize();
		String name = commandData.getName();
		handlers.put(name, handler);
		data.put(name, commandData);
	}
	
	public static void remove(String name) {
		handlers.remove(name);
		data.remove(name);
	}
	
	public static HashMap<String, UserContextEventHandler> getHandlers() {
		return handlers;
	}
	
	public static UserContextEventHandler getHandler(String name) {
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