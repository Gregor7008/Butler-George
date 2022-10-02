package functions.configuration_options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import assets.functions.ConfigurationEventHandler;
import assets.functions.ConfigurationOptionData;

public abstract class UserConfigurationOptionsList {
	
	private static final HashMap<String, ConfigurationEventHandler> handlers = new HashMap<>();
	private static final HashMap<String, ConfigurationOptionData> data = new HashMap<>();
	
	public static void create() {
		
	}
	
	public static void add(ConfigurationEventHandler handler) {
		ConfigurationOptionData configurationOptionData = handler.initialize();
		String name = configurationOptionData.getName();
		handlers.put(name, handler);
		data.put(name, configurationOptionData);
	}
	
	public static void remove(String name) {
		handlers.remove(name);
		data.remove(name);
	}
	
	public static HashMap<String, ConfigurationEventHandler> getHandlers() {
		return handlers;
	}
	
	public static ConfigurationEventHandler getHandler(String name) {
		return handlers.get(name);
	}
	
	public static List<ConfigurationOptionData> getConfigurationOptionData() {
		List<ConfigurationOptionData> commandDataList =  new ArrayList<>();
		commandDataList.addAll(data.values());
		return commandDataList;
	}
	
	public static ConfigurationOptionData getConfigurationOptionData(String name) {
		return data.get(name);
	}
}