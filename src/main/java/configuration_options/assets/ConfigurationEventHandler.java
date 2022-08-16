package configuration_options.assets;

import net.dv8tion.jda.api.entities.Member;

public interface ConfigurationEventHandler {
	
	public void execute(ConfigurationEvent event);
	public ConfigurationOptionData initialize();
	public boolean checkBotPermissions(ConfigurationEvent event);
	public boolean isAvailableTo(Member member);

}