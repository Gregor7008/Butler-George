package main;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class Startup {
	
	public static String prefix = "#";
	
	public static void main(String[] arguments) throws Exception {
	    JDA jda = JDABuilder.createDefault("Token_Here!").build();
	    jda.getPresence().setStatus(OnlineStatus.ONLINE);
	    jda.addEventListener(new Commands());
	    while (true) {
			jda.getPresence().setActivity(Activity.listening("Gregor"));
			wait(15000);
			jda.getPresence().setActivity(Activity.competing("NoLimits"));
			wait(15000);
			jda.getPresence().setActivity(Activity.watching("NoLimits"));
			wait(15000);
			jda.getPresence().setActivity(Activity.playing("discord.gg/qHA2vUs"));
			wait(15000);
	    }
	}
	
	public static void endMe() {
		System.exit(0);
	}
	
	private static void wait(int time) {
		try { Thread.sleep(time);
        } catch (InterruptedException e){}
	}
}
