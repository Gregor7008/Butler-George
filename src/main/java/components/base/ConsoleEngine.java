package components.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nullable;

import base.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class ConsoleEngine {
    
	private DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
	private final JDA jda = Bot.INSTANCE.jda;
	
	public ConsoleEngine() {
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
	
	private void print(@Nullable String color, Object object, String message) {
		String fullClassName[] = object.getClass().getName().split("\\.");
		String className = fullClassName[fullClassName.length - 1];
		String output = OffsetDateTime.now().format(format) + " [" + className + "] " + message;
		if (color == null) {
			System.out.println(color);
		} else {
			System.out.println(color + output + ConsoleColors.RESET);
		}
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
						Bot.INSTANCE.shutdown(delete);
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
						Configloader.INSTANCE.addUserConfig(jda.getGuildById(insplit[1]), jda.getUserById(insplit[2]), "warnings", "Administrative actions");
						this.debug(this, "User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully warned on " + jda.getGuildById(insplit[1]).getName());
						break;
					case "listbugs":
						try (BufferedReader br = new BufferedReader(new FileReader(new File(Bot.environment + "/configs/bugs.txt")))) {
						    String fileline;
						    while ((fileline = br.readLine()) != null) {
						       this.info(this, fileline);
						    }
						}
						break;
					case "addbug":
						try {
							Writer output = new BufferedWriter(new FileWriter(Bot.environment + "/configs/bugs.txt", true));
							String[] bug = line.split(" ", 2);
							output.append(bug[1] + "\n");
							output.close();
						} catch (ArrayIndexOutOfBoundsException e) {
							this.debug(this, "Invalid arguments! - Add the new bug behind the command!");
							break;
						}
						
						break;
					default:
						this.error(this, "Unknown command!");
					}
				}
			} catch (IOException e){}
		}).start();
	}
}