package base;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import components.base.Configloader;
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
	
	public JDA jda;
	private EventWaiter eventWaiter = new EventWaiter();
	
	public static void main(String[] arguments) {
		try {
			new Bot();
		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private Bot() throws LoginException, InterruptedException {
		INSTANCE = this;
		System.out.println("Still not ready:\n-> ");
		System.out.println("In developement:\n-> Report system\n-> Ping to move");
		JDABuilder builder = JDABuilder.createDefault(this.getBotConfig("token"));
		builder.addEventListeners(eventWaiter);
		builder.addEventListeners(new Processor());
		builder.setRawEventsEnabled(true);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
		jda = builder.build().awaitReady();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);	    
	    new Thread (() -> {
	    	while (jda.getPresence().getStatus().equals(OnlineStatus.ONLINE)) {
	    		jda.getPresence().setActivity(Activity.playing("Discord"));
	    		this.wait(15000);
	    		jda.getPresence().setActivity(Activity.watching("private messages"));
	    		this.wait(15000);
	    		jda.getPresence().setActivity(Activity.watching("NoLimits"));
	    		this.wait(15000);
	    		jda.getPresence().setActivity(Activity.playing("discord.gg/qHA2vUs"));
	    		this.wait(15000);
	    	}
	    }).start();
    	new Configloader();
    	//new NoLimitsOnly().staticTalksOff();
    	this.readConsole();
    	for (int i = 0; i < Bot.INSTANCE.jda.getGuilds().size(); i++) {
    		Guild guild = Bot.INSTANCE.jda.getGuilds().get(i);    		
    		if (!Configloader.INSTANCE.getGuildConfig(guild, "join2create").equals("")) {
    			guild.getVoiceChannelById(Configloader.INSTANCE.getGuildConfig(guild, "join2create")).putPermissionOverride(guild.getPublicRole()).setAllow(Permission.VIEW_CHANNEL, Permission.VOICE_SPEAK).queue();
    		}
    	}
	}

	public void shutdown() {
		//new NoLimitsOnly().staticTalksOn();
		for (int i = 0; i < Bot.INSTANCE.jda.getGuilds().size(); i++) {
    		Guild guild = Bot.INSTANCE.jda.getGuilds().get(i);    		
    		if (!Configloader.INSTANCE.getGuildConfig(guild, "join2create").equals("")) {
    			guild.getVoiceChannelById(Configloader.INSTANCE.getGuildConfig(guild, "join2create")).putPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL, Permission.VOICE_SPEAK).queue();
    			String j2csraw = Configloader.INSTANCE.getGuildConfig(guild, "j2cs");
    			if (!j2csraw.equals("")) {
    				String[] j2cs = j2csraw.split(";");
    				for (int e = 0; e < j2cs.length; e++) {
        				String[] temp1 = j2cs[e].split("-");
        				guild.getVoiceChannelById(temp1[0]).delete().queue();
        			}
    			}
    		}
    	}
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		System.out.println("Bot offline");
		this.wait(1000);
		System.exit(0);
	}
	
	public EventWaiter getWaiter() {
		return eventWaiter;
	}
	
	private void wait(int time) {
		try { Thread.sleep(time);
        } catch (InterruptedException e){}
	}
	
	private void readConsole() {
		new Thread (() -> {
			String line = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try {
				while((line = reader.readLine()) != null) {
					if(line.equalsIgnoreCase("stop")) {
						this.shutdown();
					}
				}
			} catch (IOException e){}
		}).start();
	}
	
	public String getBotConfig(String key) {
		File propertiesFile = new File("./resources/botconfig.properties");
		Properties properties = new Properties();	 
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile))) {
		  properties.load(bis);
		} catch (Exception ex) {}
		return properties.getProperty(key);
	}
}
