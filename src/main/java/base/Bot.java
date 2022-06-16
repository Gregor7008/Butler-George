package base;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import org.json.JSONObject;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import components.base.AnswerEngine;
import components.base.ConfigVerifier;
import components.base.ConfigLoader;
import components.base.ConsoleEngine;
import components.base.assets.ConfigManager;
import components.moderation.ModEngine;
import components.moderation.PenaltyEngine;
import components.moderation.ServerUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Bot {
	
	public static Bot run;
	public static String version = "V2.0-dev";
	public static String name = "Butler George";
	public static String homeID = "708381749826289666";
	public JDA jda;
	private EventWaiter eventWaiter = new EventWaiter();
	private Timer timer = new Timer();
	public int timerCount = 0;
	public boolean noErrorOccured = true;
	
	public Bot(String token) throws LoginException, InterruptedException, IOException {
		run = this;
	    GUI.get.setProgress(25);
	    //Create Bot
		JDABuilder builder = JDABuilder.createDefault(token);
		builder.addEventListeners(eventWaiter);
		builder.addEventListeners(new Processor());
		builder.setRawEventsEnabled(true);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
		GUI.get.setProgress(50);
    	jda = builder.build().awaitReady();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);	    
	    jda.getPresence().setActivity(Activity.playing(version));
	    GUI.get.setBotRunning(true);
	    GUI.get.setProgress(75);
	    //Startup engines
	    Thread.setDefaultUncaughtExceptionHandler(ConsoleEngine.out);
	    new ConfigVerifier();
	    new PenaltyEngine();
	    new ModEngine();
	    ServerUtilities.controlChannels(true);
	    GUI.get.setProgress(100);
    	this.checkConfigs();
    	this.startTimer();
    	GUI.get.setProgress(0);
    	
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
    			if (ConfigLoader.getGuildConfig(guild).getLong("systeminfochannel") != 0) {
    				long chid = ConfigLoader.getGuildConfig(guild).getLong("systeminfochannel");
    				long msgid = guild.getTextChannelById(chid).sendMessageEmbeds(AnswerEngine.fetchMessage(guild, null, "/base/bot:offline").convert()).complete().getIdLong();
        			ConfigLoader.getGuildConfig(guild).put("offlinemsg", msgid);
        		}
    		}
    		if (ConfigLoader.getGuildConfig(guild).getLong("supportchat") != 0) {
    			guild.getTextChannelById(ConfigLoader.getGuildConfig(guild).getLong("supportchat")).upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
    		}
    	}
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		GUI.get.setBotRunning(false);
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
					PenaltyEngine.run.guildCheck(guild);
					ModEngine.run.guildCheck(guild);
				}
				if (timerCount > 0 && noErrorOccured) {
					ConfigManager.pushCache();
				}
				timerCount++;
			}
		}, 0, 5*60*1000);
	}
	
	public EventWaiter getWaiter() {
		return eventWaiter;
	}
}