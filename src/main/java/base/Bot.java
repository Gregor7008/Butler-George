package base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.List;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import components.base.AnswerEngine;
import components.base.Configcheck;
import components.base.Configloader;
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
	
	public static Bot INSTANCE;
	
	public JDA jda;
	private EventWaiter eventWaiter = new EventWaiter();
	private PenaltyEngine penaltyEngine;
	private ModEngine modEngine;
	public static String token, environment, noliID;
	
	public static void main(String[] arguments) {
		if (arguments.length <= 0) {
			System.out.println("You have to provide 1. a bot token, 2. the path to my resource folder!");
			System.exit(0);
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
		JDABuilder builder = JDABuilder.createDefault(token);
		builder.addEventListeners(eventWaiter);
		builder.addEventListeners(new Processor());
		builder.setRawEventsEnabled(true);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
    	jda = builder.build().awaitReady();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);	    
	    jda.getPresence().setActivity(Activity.playing("V1.3-beta"));
	    new ServerUtilities().controlChannels(true);
	    new AnswerEngine();
	    penaltyEngine = new PenaltyEngine();
	    modEngine = new ModEngine();
	    this.readConsole();
    	this.checkConfigs();
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
						boolean delete = true;
						try {
							delete = Boolean.parseBoolean(insplit[1]);
						} catch (IndexOutOfBoundsException e) {}
						this.shutdown(delete);
						break;
					case "exit":
						jda.shutdown();
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
						Configloader.INSTANCE.addUserConfig(jda.getGuildById(insplit[1]), jda.getUserById(insplit[2]), "warnings", "Administrative actions");
						System.out.println("User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully warned on " + jda.getGuildById(insplit[1]).getName());
						break;
					case "listbugs":
						try (BufferedReader br = new BufferedReader(new FileReader(new File(environment + "/configs/bugs.txt")))) {
						    String fileline;
						    while ((fileline = br.readLine()) != null) {
						       System.out.println(fileline);
						    }
						}
						break;
					case "addbugs":
						try {
							Writer output = new BufferedWriter(new FileWriter(environment + "/configs/bugs.txt", true));
							String[] bug = line.split(" ", 2);
							output.append(bug[1] + "\n");
							output.close();
						} catch (ArrayIndexOutOfBoundsException e) {
							System.out.println("Invalid arguments!\nAdd the new bug behind the command!");
							break;
						}
						
						break;
					default:
						System.out.println("Unknown command!");
					}
				}
			} catch (IOException e){}
		}).start();
	}
	
	private void shutdown(Boolean delete) {
		new ServerUtilities().controlChannels(false);
		List<Guild> guilds = jda.getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
    		Guild guild = guilds.get(i);
    		if (delete) {
    			String j2csraw = Configloader.INSTANCE.getGuildConfig(guild, "j2cs");
    			if (!j2csraw.equals("")) {
    				String[] j2cs = j2csraw.split(";");
    				for (int e = 0; e < j2cs.length; e++) {
        				String[] temp1 = j2cs[e].split("-");
        				guild.getVoiceChannelById(temp1[0]).delete().queue();
        			}
    			}
    			if (!Configloader.INSTANCE.getGuildConfig(guild, "levelmsgch").equals("")) {
    				String cid = Configloader.INSTANCE.getGuildConfig(guild, "levelmsgch");
    				String msgid = guild.getTextChannelById(cid).sendMessageEmbeds(AnswerEngine.ae.buildMessage("Bot offline", ":warning: | I am going offline for maintenance!"
        					+ "\n:information_source: | You won't be able to execute commands until I go online again!")).complete().getId();
        			Configloader.INSTANCE.setGuildConfig(guild, "offlinemsg", cid + "_" + msgid);
        		}
    		}
    		if (!Configloader.INSTANCE.getGuildConfig(guild, "supportchat").equals("")) {
    			guild.getTextChannelById(Configloader.INSTANCE.getGuildConfig(guild, "supportchat")).putPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
    		}
    		new File(Bot.environment + "/levelcards/cache/temp.png").delete();
    		new File(Bot.environment + "/levelcards/cache/avatar.png").delete();
    	}
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		System.out.println("Bot offline");
		this.wait(2000);
		System.exit(0);
	}
	
	private void checkConfigs() {
		for (int i = 0; i < jda.getGuilds().size(); i++) {
    		Guild guild = jda.getGuilds().get(i);
    		Configcheck.INSTANCE.checkGuildConfigs(guild);
    		Configcheck.INSTANCE.checkUserConfigs(guild);
		}
	}
	
	private void wait(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}
	
	public EventWaiter getWaiter() {
		return eventWaiter;
	}
	
	public void penaltyCheck(Guild guild) {
		new Thread(() -> {
			this.penaltyEngine.run(guild);
		}).start();
	}
	
	public void modCheck(Guild guild) {
		new Thread(() -> {
			this.modEngine.run(guild);
		}).start();
	}
}