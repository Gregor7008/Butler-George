package configuration_options.assets;

public class ConfigurationOptionData {
	
	private ConfigurationEventHandler configurationEventHandler = null;
	private String name = null;
	private String info = null;
	private ConfigurationSubOptionData[] subOperations = null;
	
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
	
	public ConfigurationOptionData setSubOperations(ConfigurationSubOptionData[] subOperations) {
		this.subOperations = subOperations;
		return this;
	}
	
	public ConfigurationOptionData setSubOperation(ConfigurationSubOptionData subOperation) {
		this.subOperations = new ConfigurationSubOptionData[] {subOperation};
		return this;
	}
	
	public ConfigurationEventHandler getOperationEventHandler() {
		return this.configurationEventHandler;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getInfo() {
		return this.info;	
	}
	
	public ConfigurationSubOptionData[] getSubOperations() {
		return this.subOperations;
	}
 }
