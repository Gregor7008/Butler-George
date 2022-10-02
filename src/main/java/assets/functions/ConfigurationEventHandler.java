package assets.functions;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.Permission;

public interface ConfigurationEventHandler {
	
	public void execute(ConfigurationEvent event);
	public ConfigurationOptionData initialize();
	public default List<Permission> getRequiredPermissions() {
		return new ArrayList<Permission>();
	}
}