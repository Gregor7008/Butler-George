package base;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import javax.security.auth.login.LoginException;

import org.json.JSONObject;

import base.engines.ConfigLoader;
import base.engines.ConfigVerifier;
import base.engines.ConsoleEngine;
import base.engines.EventAwaiter;
import base.engines.LanguageEngine;
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
	public JDA jda;
	public 	Timer centralTimer = new Timer();
	public boolean noErrorOccured = true;
	
	public Bot(String token, String databaseIP, String databaseName) throws LoginException, InterruptedException, IOException {
		INSTANCE = this;
		new ConfigLoader(databaseIP, databaseName);
		JDABuilder builder = JDABuilder.createDefault(token);
		builder.addEventListeners(new EventProcessor(), new EventAwaiter());
		builder.setRawEventsEnabled(true);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
    	jda = builder.build().awaitReady();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);	    
	    jda.getPresence().setActivity(Activity.playing(VERSION));
	    this.startup(databaseIP, databaseName);
	}
	
	public void startup(String databaseIP, String databaseName) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (INSTANCE != null && this.jda != null) {
				this.shutdown(true);
			}
		}));
		GUI.INSTANCE.setBotRunning(true);
		Thread.setDefaultUncaughtExceptionHandler(ConsoleEngine.INSTANCE);
	    new ConfigVerifier();
	    new ModController();
	    ServerUtilities.controlChannels(true);
    	this.checkConfigs();
    	GUI.INSTANCE.updateStatistics();
    	GUI.INSTANCE.startRuntimeMeasuring();
	}
	
	public void shutdown(boolean handleManagedChannels) {
		ServerUtilities.controlChannels(false);
		List<Guild> guilds = jda.getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
    		Guild guild = guilds.get(i);
    		if (handleManagedChannels) {
    			JSONObject createdchannels = ConfigLoader.INSTANCE.getFirstGuildLayerConfig(guild, "createdchannels");
    			if (!createdchannels.isEmpty()) {
    				createdchannels.keySet().forEach(e -> createdchannels.getJSONObject(e).keySet().forEach(a ->  guild.getVoiceChannelById(a).delete().queue()));
    				createdchannels.clear();
    			}
    			long chid = guild.getTextChannels().stream().filter(c -> {return guild.getSelfMember().hasPermission(c, Permission.MESSAGE_SEND);}).toList().get(0).getIdLong();
    			if (ConfigLoader.INSTANCE.getGuildConfig(guild).getLong("communityinbox") != 0) {
    				chid = ConfigLoader.INSTANCE.getGuildConfig(guild).getLong("communityinbox");
        		}
    			long msgid = guild.getTextChannelById(chid).sendMessageEmbeds(LanguageEngine.fetchMessage(guild, null, this, "offline").convert()).complete().getIdLong();
    			ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("offlinemsg").put(0, msgid).put(1, chid);
    		}
    	}
		this.centralTimer.cancel();
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		GUI.INSTANCE.setBotRunning(false);
		GUI.INSTANCE.stopRuntimeMeasuring();
		if (noErrorOccured) {
			ConfigLoader.INSTANCE.manager.pushCache();
		}
		ConsoleEngine.INSTANCE.info(this, "Bot offline");
		INSTANCE = null;
		jda = null;
	}
	
	private void checkConfigs() {
		for (int i = 0; i < jda.getGuilds().size(); i++) {
    		Guild guild = jda.getGuilds().get(i);
    		ConfigVerifier.RUN.guildCheck(guild);
    		ConfigVerifier.RUN.usersCheck(guild);
		}
	}
}