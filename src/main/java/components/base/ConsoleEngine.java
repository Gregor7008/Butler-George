package components.base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Thread.UncaughtExceptionHandler;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nullable;

import base.Bot;
import base.GUI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class ConsoleEngine implements UncaughtExceptionHandler, ActionListener{
    
	private DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static ConsoleEngine out;
	
	public ConsoleEngine() {
		out = this;
		//TODO System.setOut();
		//TODO System.setErr();
		this.print(null, null, "---------->| Console Engine V1.0 |<----------");
	}
	
	public void debug(Object object, String message) {
		this.print("warning", object, message);
	}
	
	public void info(Object object, String message) {
		this.print("info", object, message);
	}
	
	public void error(Object object, String message) {
		this.print("error", object, message);
	}
	
	public void title(String title) {
		this.print(null, null, "---------| " + title + " |---------");
	}
	
	private void print(@Nullable String prefix, @Nullable Object object, @Nullable String message) {
		String className = "";
		String timeCodeText = OffsetDateTime.now().format(format);
		if (prefix == null) {
			prefix = "";
		} else {
			prefix = "[" + prefix.toUpperCase() + "]";
		}
		if (object == null) {
			className = "";
		} else {
			String fullClassName[] = object.getClass().getName().split("\\.");
			className = "[" + fullClassName[fullClassName.length - 1] + "]";
		}
		if (message == null) {
			message = "No message";
		}
		GUI.console.append(timeCodeText + " | " + prefix + " " + className + " " + message + "\n");
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Bot.run.noErrorOccured = false;
		this.error(t.getClass(), e.getCause().getMessage());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String input = GUI.consoleIn.getText();
		GUI.consoleIn.setText("");
		if (Bot.run != null) {
			if (Bot.run.jda != null) {
				this.commandListener(input);
			}
		}
	}
	
	private void commandListener(String line) {
		JDA jda = Bot.run.jda;
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
					this.info(null, "->" + Bot.run.jda.getUserById(id).getName());
				});
				if (ConfigLoader.manager.getUserCache().isEmpty()) {
					this.info(null, "EMPTY");
				}
				this.title("Guild-Cache");
				ConfigLoader.manager.getGuildCache().forEach((id, obj) -> {
					this.info(null, "->" + Bot.run.jda.getGuildById(id).getName());
				});
				if (ConfigLoader.manager.getGuildCache().isEmpty()) {
					this.info(null, "EMPTY");
				}
				break;
			default:
				this.error(this, "Unknown command!");
		}
	}
}