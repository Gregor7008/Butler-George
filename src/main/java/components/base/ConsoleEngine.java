package components.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nullable;

import base.Bot;
import components.base.assets.ConsoleColors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class ConsoleEngine implements UncaughtExceptionHandler{
    
	private DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
	private final JDA jda = Bot.run.jda;
	public static ConsoleEngine out;
	
	public ConsoleEngine() {
		out = this;
		this.commandListener();
		System.out.println(ConsoleColors.BLUE_BRIGHT + "--------------------------| Console Engine V1.0 |--------------------------" + ConsoleColors.RESET);
	}
	
	public void debug(Object object, String message) {
		this.print(ConsoleColors.YELLOW, object, message);
	}
	
	public void info(Object object, String message) {
		this.print(ConsoleColors.GREEN, object, message);
	}
	
	public void error(Object object, String message) {
		this.print(ConsoleColors.RED, object, message);
	}
	
	public void title(String title) {
		this.print(ConsoleColors.BLUE_BRIGHT, null, "---------| " + title + " |---------");
	}
	
	private void print(@Nullable String color, @Nullable Object object, @Nullable String message) {
		if (message == null) {
			message = "No message";
		}
		String output = "";
		if (object == null) {
			output = OffsetDateTime.now().format(format) + " " + message;
		} else {
			String fullClassName[] = object.getClass().getName().split("\\.");
			String className = fullClassName[fullClassName.length - 1];
			output = OffsetDateTime.now().format(format) + " [" + className + "] " + message;
		}
		if (color == null) {
			System.out.println(output);
		} else {
			System.out.println(color + output + ConsoleColors.RESET);
		}
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Bot.run.noErrorOccured = false;
		this.error(t.getClass(), e.getCause().getMessage());
	}
	
	private void commandListener() {
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
						Bot.run.shutdown(delete);
						break;
					case "exit":
						jda.shutdown();
						System.exit(0);
						break;
					case "giverole":
						try {
							jda.getGuildById(insplit[1]).addRoleToMember(User.fromId(insplit[2]), jda.getGuildById(insplit[1]).getRoleById(insplit[3])).queue();
						} catch (Exception e) {
							this.debug(this, "Invalid arguments or no permission! - 1. Server ID | 2. User ID | 3. Role ID");
							break;
						}
						this.debug(this, "Role " + jda.getGuildById(insplit[1]).getRoleById(insplit[3]).getName() + " was successfully given to " + jda.retrieveUserById(insplit[2]).complete().getName());
						break;
					case "removerole":
						try {	
							jda.getGuildById(insplit[1]).removeRoleFromMember(User.fromId(insplit[2]), jda.getGuildById(insplit[1]).getRoleById(insplit[3])).queue();
						} catch (Exception e) {
							this.debug(this, "Invalid arguments or no permission! - 1. Server ID | 2. User ID | 3. Role ID");
							break;
						}
						this.info(this, "Role " + jda.getGuildById(insplit[1]).getRoleById(insplit[3]).getName() + " was successfully removed from " + jda.retrieveUserById(insplit[2]).complete().getName());
						break;
					case "kick":
						try {
							jda.getGuildById(insplit[1]).kick(User.fromId(insplit[2])).queue();
						} catch (Exception e) {
							this.debug(this, "Invalid arguments or no permission! - 1. Server ID | 2. User ID");
							break;
						}
						this.info(this, "User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully kicked from " + jda.getGuildById(insplit[1]).getName());
						break;
					case "ban":
						try {
							jda.getGuildById(insplit[1]).ban(User.fromId(insplit[2]), 0).queue();
						} catch (Exception e) {
							this.debug(this, "Invalid arguments or no permission! - 1. Server ID | 2. User ID");
							break;
						}
						this.info(this, "User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully banned from " + jda.getGuildById(insplit[1]).getName());
						break;
					case "unban":
						try {
							jda.getGuildById(insplit[1]).unban(User.fromId(insplit[2])).queue();
						} catch (Exception e) {
							this.debug(this, "Invalid arguments or no permission! - 1. Server ID | 2. User ID");
							break;
						}
						this.info(this, "User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully unbanned from " + jda.getGuildById(insplit[1]).getName());
						break;
					case "warn":
						ConfigLoader.run.getMemberConfig(jda.getGuildById(insplit[1]), jda.getUserById(insplit[2])).getJSONArray("warnings").put("Administrative actions");
						this.debug(this, "User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully warned on " + jda.getGuildById(insplit[1]).getName());
						break;
					case "printTimer":
						this.info(this, String.valueOf(Bot.run.timerCount));
						break;
					case "pushCache":
						if (ConfigLoader.manager.pushCache()) {
							this.debug(this, "Cache successfully pushed");
						}
						break;
					case "printCache":
						this.title("User-Cache");
						ConfigLoader.manager.getUserCache().forEach((id, obj) -> {
							this.info(ConfigLoader.manager, Bot.run.jda.getUserById(id).getName());
						});
						this.title("Guild-Cache");
						ConfigLoader.manager.getGuildCache().forEach((id, obj) -> {
							this.info(ConfigLoader.manager, Bot.run.jda.getGuildById(id).getName());
						});
						break;
					default:
						this.error(this, "Unknown command!");
					}
				}
			} catch (IOException e){}
		}).start();
	}
}