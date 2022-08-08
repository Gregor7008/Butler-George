package base;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

import org.json.JSONObject;

import base.engines.EventAwaiter;
import base.engines.LanguageEngine;
import base.engines.configs.ConfigLoader;
import base.engines.configs.ConfigVerifier;
import base.engines.logging.ConsoleEngine;
import base.engines.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import slash_commands.engines.ModController;
import slash_commands.engines.ServerUtilities;

public class Bot {
	
	public static Bot INSTANCE;
	public static String VERSION = "V2.0-dev";
	public static String NAME = "Butler George";
	public static String ID = "853887837823959041";
	public static String HOME = "708381749826289666";
	
	private static Logger LOG = ConsoleEngine.getLogger(Bot.class);
	
	public JDA jda;
	
	private Timer timer = new Timer();
	private Thread shutdownThread = null;
	private boolean errorOccured = false;
	private boolean shutdown = false;
	
	public Bot(String token, String serverIP, String port, String databaseName, String username, String password) throws LoginException, InterruptedException, IOException {
		INSTANCE = this;
		new ConfigLoader(serverIP, port, databaseName, username, password);
		JDABuilder builder = JDABuilder.createDefault(token);
		builder.addEventListeners(new EventProcessor(), new EventAwaiter());
		builder.setRawEventsEnabled(true);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
    	jda = builder.build().awaitReady();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);	    
	    jda.getPresence().setActivity(Activity.playing(VERSION));
	    this.startup();
	}
	
	public void startup() {
//		Essentials
		Runtime.getRuntime().addShutdownHook(this.getShutdownThread());
		Thread.setDefaultUncaughtExceptionHandler(ConsoleEngine.getInstance());
//	    Engines
		new ConfigVerifier();
	    new ModController();
//	    Startup operations
    	checkConfigs();
    	ServerUtilities.controlChannels(true);
//    	GUI
    	GUI.INSTANCE.setBotRunning(true);
    	GUI.INSTANCE.updateStatistics();
    	GUI.INSTANCE.startRuntimeMeasuring();
	}
	
	public void shutdown(ShutdownReason reason, @Nullable String additionalMessage) {
		List<Guild> guilds = jda.getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
    		Guild guild = guilds.get(i);
//    		Delete channels created with Join2Create channels
    		JSONObject createdchannels = ConfigLoader.INSTANCE.getFirstGuildLayerConfig(guild, "createdchannels");
    		if (!createdchannels.isEmpty()) {
    			createdchannels.keySet().forEach(e -> createdchannels.getJSONObject(e).keySet().forEach(a ->  guild.getVoiceChannelById(a).delete().queue()));
    			createdchannels.clear();
    		}
//    		Send offline message
    		long chid = ConfigLoader.INSTANCE.getGuildConfig(guild).getLong("communityinbox");
    		if (chid == 0L) {
        		chid = guild.getTextChannels().stream().filter(c -> {return guild.getSelfMember().hasPermission(c, Permission.MESSAGE_SEND);}).toList().get(0).getIdLong();
        	}
    		StringBuilder offlineMessageBuilder = new StringBuilder();
    		switch (reason) {
			case FATAL_ERROR:
				offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "fatal"));
				break;
			case MAINTENANCE:
				offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "maintenance"));
				break;
			case OFFLINE:
				offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "offline"));
				break;
			case RESTART:
				offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "restart"));
				break;
			default:
				throw new IllegalArgumentException("Invalid shutdown reason - Contact support immediately!");
			}
    		offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "information"));
    		if (additionalMessage != null && !additionalMessage.equals("")) {
    			offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "addonpresent"));
    			offlineMessageBuilder.append(additionalMessage);
    		}
    		long msgid = guild.getTextChannelById(chid).sendMessageEmbeds(LanguageEngine.buildMessageFromRaw(offlineMessageBuilder.toString(), null)).complete().getIdLong();
    		ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("offlinemsg").put(0, msgid).put(1, chid);
    	}
//		Stop period operations and shutdown bot
		timer.cancel();
		ServerUtilities.controlChannels(false);
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		GUI.INSTANCE.setBotRunning(false);
		GUI.INSTANCE.stopRuntimeMeasuring();
		if (!errorOccured) {
			ConfigLoader.INSTANCE.manager.pushCache();
		}
		LOG.info("Bot offline");
		shutdown = true;
	}
	
	public Timer getTimer() {
		return timer;
	}
	
	public Thread getShutdownThread() {
		if (shutdownThread == null) {
			shutdownThread = new Thread(() -> {
				if (!shutdown) {
					shutdown(ShutdownReason.RESTART, null);
				}
			});
		}
		return shutdownThread;
	}
	
	public void onErrorOccurrence() {
		errorOccured = true;
		GUI.INSTANCE.increaseErrorCounter();
		GUI.INSTANCE.updateBotBoolean();
	}
	
	public boolean hasErrorOccurred() {
		return errorOccured;
	}
	
	public boolean isShutdown() {
		return shutdown;
	}
	
	public void kill() {
		this.timer.cancel();
		INSTANCE = null;
		this.shutdown = true;
	}
	
	private void checkConfigs() {
		for (int i = 0; i < jda.getGuilds().size(); i++) {
    		Guild guild = jda.getGuilds().get(i);
    		ConfigVerifier.RUN.guildCheck(guild);
    		ConfigVerifier.RUN.usersCheck(guild);
		}
	}
	
	public static enum ShutdownReason {
		
		OFFLINE,
		MAINTENANCE,
		RESTART,
		FATAL_ERROR;
		
	}
}