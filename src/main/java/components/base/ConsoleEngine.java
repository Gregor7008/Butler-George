package components.base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

import base.Bot;
import base.GUI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class ConsoleEngine implements UncaughtExceptionHandler, ActionListener{
    
	private DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static ConsoleEngine out;
	private static ByteArrayOutputStream errStream = new ByteArrayOutputStream();
	private static ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	
	public ConsoleEngine() {
		out = this;
		System.setOut(new PrintStream(outStream));
		System.setErr(new PrintStream(errStream));
		this.print(null, null, "------------------------------------------------------------------------>| Console Engine V1.0 |<------------------------------------------------------------------------", false);
		this.checkStreams();
	}
	
	public void debug(@Nullable Object object, @Nullable String message) {
		this.print("warning", object, message, true);
	}
	
	public void info(@Nullable Object object, @Nullable String message) {
		this.print("info", object, message, true);
	}
	
	public void error(@Nullable Object object, @Nullable String message) {
		this.print("error", object, message, true);
		GUI.get.increaseErrorCounter();
	}
	
	public void title(String title) {
		this.print(null, null, "---------| " + title + " |---------", true);
	}
	
	public void userInput(String input) {
		
	}
	
	private void print(@Nullable String prefix, @Nullable Object object, @Nullable String message, boolean timeCode) {
		String className = "";
		String timeCodeText = "";
		if (timeCode) {
			timeCodeText = OffsetDateTime.now().format(format) + " | ";
		}
		if (prefix == null) {
			prefix = "";
		} else {
			prefix = "[" + prefix.toUpperCase() + "] ";
		}
		if (object == null) {
			className = "";
		} else {
			String fullClassName[] = object.getClass().getName().split("\\.");
			className = "[" + fullClassName[fullClassName.length - 1] + "] ";
		}
		if (message == null) {
			message = "No message";
		}
		String[] messageParts = message.split("\n");
		for (int i = 0; i < messageParts.length; i++) {
			GUI.console.append(timeCodeText + prefix + className + messageParts[i] + "\n");
		}
	}
	
	private void checkStreams() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (errStream.size() > 0) {
					ConsoleEngine.out.error(null, errStream.toString());
					errStream.reset();
				}
				if (outStream.size() > 0) {
					ConsoleEngine.out.info(null, outStream.toString());
					outStream.reset();
				}
			}
		}, 0, 2*1000);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String input = GUI.consoleIn.getText();
		GUI.consoleIn.setText("");
		if (Bot.run != null && Bot.run.jda != null) {
			this.commandListener(input);
		} else {
			this.info(this, "Input ignored as the bot is offline!");
		}
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		Bot.run.noErrorOccured = false;
		GUI.get.increaseErrorCounter();
		GUI.get.setTableValue(7, true);
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
				ConfigLoader.getMemberConfig(jda.getGuildById(insplit[1]), jda.getUserById(insplit[2])).getJSONArray("warnings").put("Administrative actions");
				this.debug(this, "User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully warned on " + jda.getGuildById(insplit[1]).getName());
				break;
			case "pushCache":
				if (ConfigManager.pushCache()) {
					this.debug(this, "Cache successfully pushed");
				}
				break;
			case "printCache":
				this.title("User-Cache");
				ConfigManager.getUserCache().forEach((id, obj) -> {
					this.info(null, "->" + Bot.run.jda.getUserById(id).getName());
				});
				if (ConfigManager.getUserCache().isEmpty()) {
					this.info(null, "EMPTY");
				}
				this.title("Guild-Cache");
				ConfigManager.getGuildCache().forEach((id, obj) -> {
					this.info(null, "->" + Bot.run.jda.getGuildById(id).getName());
				});
				if (ConfigManager.getGuildCache().isEmpty()) {
					this.info(null, "EMPTY");
				}
				break;
			default:
				this.error(this, "Unknown command!");
		}
	}
}