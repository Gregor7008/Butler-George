package base.engines;

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

public class ConsoleEngine implements UncaughtExceptionHandler, ActionListener{
    
	private DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static ConsoleEngine INSTANCE;
	private ByteArrayOutputStream errStream = new ByteArrayOutputStream();
	private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	
	public ConsoleEngine() {
		INSTANCE = this;
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
		GUI.INSTANCE.increaseErrorCounter();
	}
	
	public void title(String title) {
		this.print(null, null, "---------| " + title + " |---------", true);
	}
	
	public void userInput(String input) {
		this.print("input", null, input, true);
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
			GUI.INSTANCE.console.append(timeCodeText + prefix + className + messageParts[i] + "\n");
		}
	}
	
	private void checkStreams() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (errStream.size() > 0) {
					ConsoleEngine.INSTANCE.error(null, errStream.toString());
					errStream.reset();
				}
				if (outStream.size() > 0) {
					ConsoleEngine.INSTANCE.info(null, outStream.toString());
					outStream.reset();
				}
			}
		}, 0, 2*1000);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String input = GUI.INSTANCE.consoleIn.getText();
		this.userInput(input);
		GUI.INSTANCE.consoleIn.setText("");
		if (Bot.INSTANCE != null && Bot.INSTANCE.jda != null) {
			this.commandListener(input);
		} else {
			this.info(this, "Input ignored as the bot is offline!");
		}
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		Bot.INSTANCE.noErrorOccured = false;
		GUI.INSTANCE.increaseErrorCounter();
		GUI.INSTANCE.updateBotBoolean();
	}
	
	private void commandListener(String line) {
		JDA jda = Bot.INSTANCE.jda;
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
			case "warn":
				ConfigLoader.INSTANCE.getMemberConfig(jda.getGuildById(insplit[1]), jda.getUserById(insplit[2])).getJSONArray("warnings").put("Administrative actions");
				this.debug(this, "User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully warned on " + jda.getGuildById(insplit[1]).getName());
				break;
			case "pushCache":
				if (ConfigLoader.INSTANCE.manager.pushCache()) {
					this.debug(this, "Cache successfully pushed");
				}
				break;
			case "printCache":
				this.title("User-Cache");
				ConfigLoader.INSTANCE.manager.getUserCache().forEach((id, obj) -> {
					this.info(null, "->" + Bot.INSTANCE.jda.getUserById(id).getName());
				});
				if (ConfigLoader.INSTANCE.manager.getUserCache().isEmpty()) {
					this.info(null, "EMPTY");
				}
				this.title("Guild-Cache");
				ConfigLoader.INSTANCE.manager.getGuildCache().forEach((id, obj) -> {
					this.info(null, "->" + Bot.INSTANCE.jda.getGuildById(id).getName());
				});
				if (ConfigLoader.INSTANCE.manager.getGuildCache().isEmpty()) {
					this.info(null, "EMPTY");
				}
				break;
			case "clearCache":
				ConfigLoader.INSTANCE.manager.getGuildCache().clear();
				ConfigLoader.INSTANCE.manager.getUserCache().clear();
				this.info(null, "Cache cleared successfully!");
				break;
			default:
				this.error(this, "Unknown command!");
		}
	}
}