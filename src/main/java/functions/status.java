package functions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

public class status {
	
	public status(JDA jda) {
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
	
	private void wait(int time) {
		try { Thread.sleep(time);
        } catch (InterruptedException e){}
	}
}
