package assets.functions;

import assets.logging.Logger;
import base.Bot.ShutdownReason;
import engines.logging.ConsoleEngine;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class GuildUtilities extends ListenerAdapter {
	

    protected static Logger LOG = ConsoleEngine.getLogger(GuildUtilities.class);
    
	public void onStartup() {}
	public void onShutdown(ShutdownReason reason) {}
	public long getGuildId() {return 0L;}
	
}