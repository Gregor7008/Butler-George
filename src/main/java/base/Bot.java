package base;

import javax.security.auth.login.LoginException;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {
	
	public static String prefix = "#";
	private static EventWaiter eventWaiter = new EventWaiter();
	private static JDA jda;
	
	public static void main(String[] arguments) throws LoginException, InterruptedException {
	    new Bot("Token here");
	}
	
	private Bot(String token) throws LoginException, InterruptedException {
		jda = JDABuilder.createDefault(token).addEventListeners(eventWaiter).build().awaitReady();
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

	public static void shutdown() {
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		wait(5000);
		System.exit(0);
	}
	
	public static EventWaiter getWaiter() {
		return eventWaiter;
	}
	
	private static void wait(int time) {
		try { Thread.sleep(time);
        } catch (InterruptedException e){}
	}
}
