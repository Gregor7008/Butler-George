package base;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

import org.json.JSONObject;

import assets.logging.Logger;
import engines.base.EventAwaiter;
import engines.base.LanguageEngine;
import engines.configs.ConfigLoader;
import engines.configs.ConfigVerifier;
import engines.functions.ModController;
import engines.logging.ConsoleEngine;
import functions.configuration_options.ServerConfigurationOptionsList;
import functions.context_menu_commands.MessageContextCommandList;
import functions.context_menu_commands.UserContextCommandList;
import functions.guild_utilities.GuildUtilitiesList;
import functions.slash_commands.SlashCommandList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

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
		this.performPreStartupOperations(serverIP, port, databaseName, username, password);
		JDABuilder builder = JDABuilder.createDefault(token);
		builder.addEventListeners(new EventProcessor(), new EventAwaiter());
		Object[] serverUtils = GuildUtilitiesList.getEngines().values().toArray();
		builder.addEventListeners(serverUtils);
		builder.setRawEventsEnabled(true);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
    	jda = builder.build().awaitReady();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);	    
	    jda.getPresence().setActivity(Activity.playing(VERSION));
	    this.performPostStartupOperations();
	}
	
	private void performPreStartupOperations(String serverIP, String port, String databaseName, String username, String password) {
//		Essentials
		INSTANCE = this;
		new ConfigLoader(serverIP, port, databaseName, username, password);
//	    Lists
	    ServerConfigurationOptionsList.create();
	    MessageContextCommandList.create();
	    UserContextCommandList.create();
	    GuildUtilitiesList.create();
	    SlashCommandList.create();
//	    Debug logging
	    LOG.debug("Pre-Startup operations completed");
	}
	
	private void performPostStartupOperations() {
//		Essentials
		Runtime.getRuntime().addShutdownHook(this.getShutdownThread());
		Thread.setDefaultUncaughtExceptionHandler(ConsoleEngine.getInstance());
//	    Engines
		new ConfigVerifier();
	    new ModController();
//	    Startup operations
    	checkConfigs();
    	GuildUtilitiesList.getEngines().forEach((id, handler) -> handler.onStartup());
//    	GUI
    	GUI.INSTANCE.setBotRunning(true);
    	GUI.INSTANCE.updateStatistics();
    	GUI.INSTANCE.startRuntimeMeasuring();
//    	Debug logging
    	LOG.debug("Post-Startup operations completed");
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
				offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "offline"));
			}
    		offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "information"));
    		if (additionalMessage != null && !additionalMessage.equals("")) {
    			offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "addonpresent"));
    			offlineMessageBuilder.append(additionalMessage);
    		}
    		long msgid = guild.getTextChannelById(chid).sendMessageEmbeds(LanguageEngine.buildMessageFromRaw(offlineMessageBuilder.toString(), null)).complete().getIdLong();
    		ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("offlinemsg").put(0, msgid).put(1, chid);
    	}
//		Stop period operations
		timer.cancel();
//		Shutdown operations
		GuildUtilitiesList.getEngines().forEach((id, handler) -> handler.onShutdown(reason));
//		Shutdown bot
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		GUI.INSTANCE.setBotRunning(false);
		GUI.INSTANCE.stopRuntimeMeasuring();
		if (!errorOccured) {
			ConfigLoader.INSTANCE.manager.pushCache();
		}
		shutdown = true;
//		Debug logging
		LOG.debug("Bot offline");
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