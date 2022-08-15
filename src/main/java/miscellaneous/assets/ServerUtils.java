package miscellaneous.assets;

import base.Bot.ShutdownReason;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class ServerUtils extends ListenerAdapter {
	
	public void onStartup() {}
	public void onShutdown(ShutdownReason reason) {}
	public long getGuildId() {return 0L;}
	
}