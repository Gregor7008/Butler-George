package base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.security.auth.login.LoginException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
	private static String token, environment;
	
	public static void main(String[] arguments) {
		   JFrame scframe = new JFrame("NoLimits Bot - Startup Configuration");
	       JPanel scpanel = new JPanel();
	       JButton dbutton = new JButton("Finish");
	       JTextField tfield = new JTextField("", 35);
	       JCheckBox ebox = new JCheckBox("Eclipse");
	       JCheckBox jbox = new JCheckBox("Java RE");
	       
	       scpanel.add(new JLabel("Token:"));
	       scpanel.add(tfield);
	       scpanel.add(new JLabel("Environment:"));
	       scpanel.add(ebox);
	       scpanel.add(jbox);
	       scpanel.add(dbutton);
	       
	       scframe.add(scpanel);
	       scframe.pack();
	       scframe.setLocationRelativeTo(null);
	       scframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	       scframe.setVisible(true);
	       dbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scframe.setVisible(false);
				JFrame eframe = new JFrame("NoLimits Bot - Error");
				JPanel epanel = new JPanel();
				String error = "";
				if (ebox.isSelected() && jbox.isSelected()) {
					error = "Error! You may only select the correct environment, in which the program is running in!";
				}
				if (!ebox.isSelected() && !jbox.isSelected()) {
					error = "Error! You have to select the correct environment, in which the program is running in!";
				}
				if (ebox.isSelected()) {
					environment = "./resources";
				} else {
					environment = "../resources";
				}
				if (tfield.getText().equals("")) {
					error = "Error! You have to input a valid bot token!";
				} else {
					token = tfield.getText();
				}
				if (error.equals("")) {
					try {
						new Bot();
						return;
					} catch (LoginException | InterruptedException | IOException ex) {
						error = "Error! You have to input a valid bot token!";
					}
				}
				epanel.add(new JLabel(error));
				eframe.add(epanel);
				eframe.pack();
				eframe.setLocationRelativeTo(null);
				eframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				eframe.setVisible(true);
			}});
	}
	
	private Bot() throws LoginException, InterruptedException, IOException {
		INSTANCE = this;
		System.out.println("Still not ready:\n-> Multilanguage system for info commands\n--------------------");
		System.out.println("In developement:\n-> \n--------------------");
		System.out.println("In planning:\n-> Ping to move\n-> Warn option for abuse of anonymous modmail\n--------------------");
		JDABuilder builder = JDABuilder.createDefault(token);
		builder.addEventListeners(eventWaiter);
		builder.addEventListeners(new Processor());
		builder.setRawEventsEnabled(true);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
		jda = builder.build().awaitReady();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);	    
	    new Thread (() -> {
	    	while (jda.getPresence().getStatus().equals(OnlineStatus.ONLINE)) {
	    		try {
	    			jda.getPresence().setActivity(Activity.playing("Discord"));
	    			this.wait(15000);
	    			jda.getPresence().setActivity(Activity.watching("private messages"));
	    			this.wait(15000);
	    			jda.getPresence().setActivity(Activity.watching("NoLimits"));
	    			this.wait(15000);
	    			jda.getPresence().setActivity(Activity.playing("discord.gg/qHA2vUs"));
	    			this.wait(15000);
	    		} catch (InterruptedException e) {}
	    	}
	    }).start();
    	new Configloader();
    	//new NoLimitsOnly().staticTalksOff();
    	this.waitForStop();
    	for (int i = 0; i < Bot.INSTANCE.jda.getGuilds().size(); i++) {
    		Guild guild = Bot.INSTANCE.jda.getGuilds().get(i);    		
    		if (!Configloader.INSTANCE.getGuildConfig(guild, "join2create").equals("")) {
    			guild.getVoiceChannelById(Configloader.INSTANCE.getGuildConfig(guild, "join2create")).putPermissionOverride(guild.getPublicRole()).setAllow(Permission.VIEW_CHANNEL, Permission.VOICE_SPEAK).queue();
    		}
    		if (!Configloader.INSTANCE.getGuildConfig(guild, "supportchat").equals("")) {
    			guild.getTextChannelById(Configloader.INSTANCE.getGuildConfig(guild, "supportchat")).putPermissionOverride(guild.getPublicRole()).setAllow(Permission.VIEW_CHANNEL).queue();
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
    		if (!Configloader.INSTANCE.getGuildConfig(guild, "supportchat").equals("")) {
    			guild.getTextChannelById(Configloader.INSTANCE.getGuildConfig(guild, "supportchat")).putPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
    		}
    	}
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		System.out.println("Bot offline");
		try {this.wait(1000);} catch (InterruptedException e) {}
		System.exit(0);
	}
	
	public EventWaiter getWaiter() {
		return eventWaiter;
	}
	
	private void wait(int time) throws InterruptedException {
		Thread.sleep(time);
	}
	
	private void waitForStop() {
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
		if (key.equals("resourcepath")) {
			return environment;
		}
		if (key.equals("NoLiID")) {
			return "708381749826289666";
		}
		return null;
	}
}
