package configuration_options.assets;

public interface ConfigurationEventHandler {
	
	public void execute(ConfigurationEvent event);
	public ConfigurationOptionData initialize();

}