package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
	public static String token, environment, noliID;
	
	public static void main(String[] arguments) {
		if (arguments[0].equals("") || arguments[1].equals("")) {
			System.out.println("You have to provide 1. a bot token, 2. the path to my resource folder!");
		}
		token = arguments[0];
		environment = arguments[1];
		noliID = "708381749826289666";
		try {
			new Bot(token);
		} catch (LoginException | InterruptedException | IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private Bot(String token) throws LoginException, InterruptedException, IOException {
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
    	this.readConsole();
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

	private void shutdown() {
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
	
	private void readConsole() {
		new Thread (() -> {
			String line = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try {
				while((line = reader.readLine()) != null) {
					String[] insplit = line.split(" ");
					String command = insplit[0];
					switch (command) {
					case "stop": 
						this.shutdown();
						break;
					case "exit":
						System.exit(0);
						break;
					case "giverole":
						try {
							jda.getGuildById(insplit[1]).addRoleToMember(insplit[2], jda.getGuildById(insplit[1]).getRoleById(insplit[3])).queue();
						} catch (Exception e) {
							System.out.println("Invalid arguments or no permission!\n1. Server ID | 2. User ID | 3. Role ID");
							break;
						}
						System.out.println("Role " + jda.getGuildById(insplit[1]).getRoleById(insplit[3]).getName() + " was successfully given to " + jda.retrieveUserById(insplit[2]).complete().getName());
						break;
					case "removerole":
						try {	
							jda.getGuildById(insplit[1]).removeRoleFromMember(insplit[2], jda.getGuildById(insplit[1]).getRoleById(insplit[3])).queue();
						} catch (Exception e) {
							System.out.println("Invalid arguments or no permission!\n1. Server ID | 2. User ID | 3. Role ID");
							break;
						}
						System.out.println("Role " + jda.getGuildById(insplit[1]).getRoleById(insplit[3]).getName() + " was successfully removed from " + jda.retrieveUserById(insplit[2]).complete().getName());
						break;
					case "kick":
						try {
							jda.getGuildById(insplit[1]).kick(insplit[2]).queue();
						} catch (Exception e) {
							System.out.println("Invalid arguments or no permission!\n1. Server ID | 2. User ID");
							break;
						}
						System.out.println("User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully kicked from " + jda.getGuildById(insplit[1]).getName());
						break;
					case "ban":
						try {
							jda.getGuildById(insplit[1]).ban(insplit[2], 0).queue();
						} catch (Exception e) {
							System.out.println("Invalid arguments or no permission!\n1. Server ID | 2. User ID");
							break;
						}
						System.out.println("User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully banned from " + jda.getGuildById(insplit[1]).getName());
						break;
					case "unban":
						try {
							jda.getGuildById(insplit[1]).unban(insplit[2]).queue();
						} catch (Exception e) {
							System.out.println("Invalid arguments or no permission!\n1. Server ID | 2. User ID");
							break;
						}
						System.out.println("User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully unbanned from " + jda.getGuildById(insplit[1]).getName());
						break;
					case "warn":
						Configloader.INSTANCE.addUserConfig(jda.getGuildById(insplit[1]).getMemberById(insplit[2]), "warnings", "Administrative actions");
						System.out.println("User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully warned on " + jda.getGuildById(insplit[1]).getName());
						break;
					default:
						System.out.println("Unknown command!");
					}
				}
			} catch (IOException e){}
		}).start();
	}
	
	public EventWaiter getWaiter() {
		return eventWaiter;
	}
	
	private void wait(int time) throws InterruptedException {
		Thread.sleep(time);
	}
}
