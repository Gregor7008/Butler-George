package base.engines.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.slf4j.ILoggerFactory;
import org.slf4j.event.Level;

import base.GUI;

public class ConsoleEngine implements ILoggerFactory {
    
	private static ConsoleEngine INSTANCE;
	private static Logger LOG = ConsoleEngine.getLogger(ConsoleEngine.class);
	
	public Timer streamConnector = new Timer();
	
	private ByteArrayOutputStream errStream = new ByteArrayOutputStream();
	private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	private DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
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
		this.print(null, null, "----------------------------->| Console Engine V1.0 |<-----------------------------", false);
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
	
	public void print(@Nullable Level level, @Nullable String callerName, @Nullable String message) {
		this.print(level.toString(), callerName, message, true);
	}
	
	public void print(@Nullable String prefix, @Nullable String callerName, @Nullable String message, boolean timeCode) {
		String timeCodeText = "";
		if (timeCode)
			timeCodeText = OffsetDateTime.now().format(format) + " | ";
		if (prefix == null) {
			prefix = "";
		} else {
			prefix = "[" + prefix.toUpperCase() + "] ";
		}
		if (callerName == null) {
			callerName = "";
		} else if (callerName.isBlank()) {
			callerName = "";
		} else {
			callerName = "[" + callerName + "] ";
		}
		if (message == null)
			message = "No message";
		String[] messageParts = message.split("\n");
		for (int i = 0; i < messageParts.length; i++) {
			GUI.INSTANCE.console.append(timeCodeText + prefix + callerName + messageParts[i] + "\n");
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