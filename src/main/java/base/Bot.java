package base;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import org.json.JSONObject;

import components.base.ConfigLoader;
import components.base.ConfigManager;
import components.base.ConsoleEngine;
import components.base.LanguageEngine;
import components.commands.moderation.ModEngine;
import components.utilities.ConfigVerifier;
import components.utilities.ResponseDetector;
import components.utilities.ServerUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Bot {
	
	public static Bot run;
	public static String version = "V2.0-dev";
	public static String name = "Butler George";
	public static String id = "853887837823959041";
	public static String homeID = "708381749826289666";
	public JDA jda;
	private Timer timer = new Timer();
	public int timerCount = 0;
	public boolean noErrorOccured = false;
	
	public Bot(String token) throws LoginException, InterruptedException, IOException {
		run = this;
	    //Create Bot
		JDABuilder builder = JDABuilder.createDefault(token);
		builder.addEventListeners(ResponseDetector.eventWaiter);
		builder.addEventListeners(new Processor());
		builder.setRawEventsEnabled(true);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
    	jda = builder.build().awaitReady();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);	    
	    jda.getPresence().setActivity(Activity.playing(version));
	    GUI.get.setBotRunning(true);
	    //Startup engines
	    Thread.setDefaultUncaughtExceptionHandler(ConsoleEngine.out);
	    new ConfigVerifier();
	    new ModEngine();
	    ServerUtilities.controlChannels(true);
    	this.checkConfigs();
    	this.startTimer();
    	GUI.get.updateStatistics();
    	GUI.get.startRuntimeMeasuring();
	}
	
	public void shutdown(boolean handleManagedChannels) {
		ServerUtilities.controlChannels(false);
		List<Guild> guilds = jda.getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
    		Guild guild = guilds.get(i);
    		if (handleManagedChannels) {
    			JSONObject createdchannels = ConfigLoader.getFirstGuildLayerConfig(guild, "createdchannels");
    			if (!createdchannels.isEmpty()) {
    				createdchannels.keySet().forEach(e -> guild.getVoiceChannelById(e).delete().queue());
    			}
    			if (ConfigLoader.getGuildConfig(guild).getLong("communityinbox") != 0) {
    				long chid = ConfigLoader.getGuildConfig(guild).getLong("communityinbox");
    				long msgid = guild.getTextChannelById(chid).sendMessageEmbeds(LanguageEngine.fetchMessage(guild, null, this, "offline").convert()).complete().getIdLong();
        			ConfigLoader.getGuildConfig(guild).getJSONArray("offlinemsg").put(msgid).put(chid);
        		}
    		}
    	}
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		GUI.get.setBotRunning(false);
		GUI.get.stopRuntimeMeasuring();
		if (noErrorOccured) {
			ConfigManager.pushCache();
		}
		ConsoleEngine.out.info(this, "Bot offline");
		run = null;
		jda = null;
	}
	
	private void checkConfigs() {
		for (int i = 0; i < jda.getGuilds().size(); i++) {
    		Guild guild = jda.getGuilds().get(i);
    		ConfigVerifier.run.guildCheck(guild);
    		ConfigVerifier.run.usersCheck(guild);
		}
	}
	
	private void startTimer() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				List<Guild> guilds = jda.getGuilds();
				for (int i = 0; i < guilds.size(); i++) {
					Guild guild = guilds.get(i);
					ModEngine.run.guildPenaltyCheck(guild);
					ModEngine.run.guildModCheck(guild);
				}
				if (timerCount > 0 && noErrorOccured) {
					ConfigManager.pushCache();
				}
				timerCount++;
				GUI.get.increaseCyclesCounter();
			}
		}, 0, 5*60*1000);
	}
}