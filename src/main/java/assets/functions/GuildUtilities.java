package assets.functions;

import base.Bot.ShutdownReason;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class GuildUtilities extends ListenerAdapter {
	
	public void onStartup() {}
	public void onShutdown(ShutdownReason reason) {}
	public long getGuildId() {return 0L;}
	
}