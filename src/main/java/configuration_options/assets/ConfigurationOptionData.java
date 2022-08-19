package configuration_options.assets;

import net.dv8tion.jda.api.Permission;

public class ConfigurationOptionData {
	
	private ConfigurationEventHandler configurationEventHandler = null;
	private String name = null;
	private String info = null;
	private ConfigurationSubOptionData[] subOptions = null;
	private Permission[] requiredPermissions = new Permission[]{};
	
	public ConfigurationOptionData(ConfigurationEventHandler configurationEventHandler) {
		this.configurationEventHandler = configurationEventHandler;
	}

	public ConfigurationOptionData setName(String name) {
		this.name = name;
		return this;
	}
	
	public ConfigurationOptionData setInfo(String info) {
		this.info = info;
		return this;
	}
	
	public ConfigurationOptionData setSubOptions(ConfigurationSubOptionData[] subOptions) {
		this.subOptions = subOptions;
		return this;
	}
	
	public ConfigurationOptionData setSubOpion(ConfigurationSubOptionData subOption) {
		this.subOptions = new ConfigurationSubOptionData[] {subOption};
		return this;
	}
	
	public ConfigurationOptionData setRequiredPermissions(Permission... permissions) {
		this.requiredPermissions = permissions;
		return this;
	}
	
	public ConfigurationEventHandler getConfigurationEventHandler() {
		return this.configurationEventHandler;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getInfo() {
		return this.info;	
	}
	
	public ConfigurationSubOptionData[] getSubOptions() {
		return this.subOptions;
	}
	
	public Permission[] getRequiredPermissions() {
		return this.requiredPermissions;
	}
 }
