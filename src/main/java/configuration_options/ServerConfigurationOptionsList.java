package configuration_options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import configuration_options.assets.ConfigurationEventHandler;
import configuration_options.assets.ConfigurationOptionData;
import configuration_options.server.AutoRoles;
import configuration_options.server.InboxChannels;
import configuration_options.server.Join2CreateChannels;
import configuration_options.server.LevelRewards;
import configuration_options.server.Penalties;
import configuration_options.server.ReactionRoles;
import configuration_options.server.StaticRoles;
import configuration_options.server.SupportTalk;

public abstract class ServerConfigurationOptionsList {
	
	private static final HashMap<String, ConfigurationEventHandler> handlers = new HashMap<>();
	private static final HashMap<String, ConfigurationOptionData> data = new HashMap<>();
	
	public static void create() {
		add(new AutoRoles());
		add(new InboxChannels());
		add(new Join2CreateChannels());
		add(new LevelRewards());
		add(new Penalties());
		add(new ReactionRoles());
		add(new StaticRoles());
		add(new SupportTalk());
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