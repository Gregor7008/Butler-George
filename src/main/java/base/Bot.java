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
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import components.music.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {
	
	public static Bot INSTANCE;
	
	public JDA jda;
	public AudioPlayerManager apm;
	public PlayerManager pm;
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
		
		JDABuilder builder = JDABuilder.createDefault(this.getConfig("token"));
				   builder.addEventListeners(eventWaiter);
				   builder.addEventListeners(new MessageProcessor());
				   this.apm = new DefaultAudioPlayerManager();
				   this.pm = new PlayerManager();
		jda = builder.build();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);
		System.out.println("Bot online");
		this.readConsole();
		
		AudioSourceManagers.registerRemoteSources(apm);
		
	    while (true) {
			jda.getPresence().setActivity(Activity.listening("Gregor"));
			this.wait(15000);
			jda.getPresence().setActivity(Activity.competing("NoLimits"));
			this.wait(15000);
			jda.getPresence().setActivity(Activity.watching("NoLimits"));
			this.wait(15000);
			jda.getPresence().setActivity(Activity.playing("discord.gg/qHA2vUs"));
			this.wait(15000);
	    }
	}

	public void shutdown() {
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		System.out.println("Bot offline");
		this.wait(5000);
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
						shutdown();
					} else {
						System.out.println("Command undefined");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}).start();
	}
	
	public String getConfig(String key) {
		File propertiesFile = new File("./src/main/resources/base/config.properties");
		Properties properties = new Properties();	 
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile))) {
		  properties.load(bis);
		} catch (Exception ex) {}
		return properties.getProperty(key);
	}
}
