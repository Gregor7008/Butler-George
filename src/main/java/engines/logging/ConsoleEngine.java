package engines.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.slf4j.ILoggerFactory;
import org.slf4j.event.Level;

import assets.logging.Logger;
import base.Bot;
import base.GUI;

public class ConsoleEngine implements ILoggerFactory, UncaughtExceptionHandler {
    
	private static ConsoleEngine INSTANCE;
	private static Logger LOG = ConsoleEngine.getLogger(ConsoleEngine.class);
	
	public Timer streamConnector = new Timer();
	
	private ByteArrayOutputStream errStream = new ByteArrayOutputStream();
	private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	private DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
	private DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEEE - dd.MM.uuuu");
	private OffsetDateTime currentDay = OffsetDateTime.now();
	private ConcurrentHashMap<String, Logger> loggerCache = new ConcurrentHashMap<>();
	
	public static ConsoleEngine getInstance() {
		if (INSTANCE == null)
			INSTANCE = new ConsoleEngine();
		return INSTANCE;
	}
	
	public static Logger getLogger(Object caller) {
		return getLogger(caller.getClass());
	}
	
	public static <T> Logger getLogger(Class<T> caller) {
		return ConsoleEngine.getInstance().getLogger(caller.getSimpleName());
	}
	
	public ConsoleEngine() {
		System.setOut(new PrintStream(outStream));
		System.setErr(new PrintStream(errStream));
		this.print(null, null, currentDay.format(dayFormat) + " " + "-".repeat(10) + ">| Console Engine V1.0 |<" + "-".repeat(32), false);
		this.checkStreams();
	}
	
	@Override
	public Logger getLogger(String name) {
		Logger logger = loggerCache.get(name);
		if (logger == null) {
			logger = new Logger(name);
			loggerCache.put(name, logger);
		}
		return logger;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Bot.INSTANCE.onErrorOccurrence();
		e.printStackTrace();
	}
	
	public void print(@Nullable Level level, @Nullable String callerName, @Nullable String message) {
		if (level == null) {
			this.print(null, callerName, message, true);
		} else {
			this.print(level.toString(), callerName, message, true);
		}
	}
	
	public void print(@Nullable String prefix, @Nullable String callerName, @Nullable String message, boolean timeCode) {
		String timeCodeText = "";
		String callerNameText = "";
		OffsetDateTime now = OffsetDateTime.now();
		if (currentDay.getDayOfMonth() != now.getDayOfMonth()) {
			currentDay = now;
			this.print(null, null, currentDay.format(dayFormat) + " " + "-".repeat(68), false);
		}
		if (timeCode)
			timeCodeText = now.format(hourFormat) + " | ";
		if (prefix == null) {
			prefix = "";
		} else {
			prefix = "[" + prefix.toUpperCase() + "] ";
		}
		if (callerName == null) {
			callerNameText = "";
		} else if (callerName.isBlank()) {
			callerNameText = "";
		} else {
			String[] callerNameSplit = callerName.split("\\.");
			String callerNameRaw = callerNameSplit[callerNameSplit.length - 1];
			callerNameText = "[" + callerNameRaw.substring(0, 1).toUpperCase() + callerNameRaw.substring(1) + "] ";
		}
		if (message == null)
			message = "No message";
		String[] messageParts = message.split("\n");
		for (int i = 0; i < messageParts.length; i++) {
			GUI.INSTANCE.console.append(timeCodeText + prefix + callerNameText + messageParts[i] + "\n");
		}
	}
	
	private void checkStreams() {
		this.streamConnector.schedule(new TimerTask() {
			@Override
			public void run() {
				if (errStream.size() > 0) {
					LOG.error(errStream.toString());
					errStream.reset();
				}
				if (outStream.size() > 0) {
					LOG.info(outStream.toString());
					outStream.reset();
				}
			}
		}, 0, 1000);
	}
}