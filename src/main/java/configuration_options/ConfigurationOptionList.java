package configuration_options;

import java.util.concurrent.ConcurrentHashMap;

import configuration_options.assets.ConfigurationEventHandler;
import configuration_options.server.AutoMessages;
import configuration_options.server.AutoRoles;
import configuration_options.server.InboxChannels;
import configuration_options.server.Join2CreateChannels;
import configuration_options.server.LevelRewards;
import configuration_options.server.Penalties;
import configuration_options.server.ReactionRoles;
import configuration_options.server.StaticRoles;
import configuration_options.server.SupportTalk;

public class ConfigurationOptionList {
	
	public ConcurrentHashMap<String, ConfigurationEventHandler> serverOperations = new ConcurrentHashMap<>();
	
	public ConfigurationOptionList() {
		//Administration
		this.serverOperations.put("AutoRoles", new AutoRoles());
		this.serverOperations.put("StaticRoles", new StaticRoles());
		this.serverOperations.put("AutoMessages", new AutoMessages());
		this.serverOperations.put("Join2CreateChannels", new Join2CreateChannels());
		this.serverOperations.put("InboxChannels", new InboxChannels());
		this.serverOperations.put("LevelRewards", new LevelRewards());
		this.serverOperations.put("Penalties", new Penalties());
		this.serverOperations.put("ReactionRoles", new ReactionRoles());
		this.serverOperations.put("SupportTalk", new SupportTalk());
	}
}
