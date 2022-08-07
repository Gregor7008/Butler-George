package base.engines.logging;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

public class Logger implements org.slf4j.Logger {

	private String name;
	private boolean traceEnabled = false;
	private boolean debugEnabled = false;
	private boolean infoEnabled = false;
	private boolean warnEnabled = false;
	private boolean errorEnabled = false;
	
	public Logger(String name) {
		this.name = name;
		this.setLevelThreshold(Level.INFO);
	}
	
	public Logger setLevelThreshold(Level level) {
		switch (level) {
		case TRACE:
			traceEnabled = true;
		case DEBUG:
			debugEnabled = true;
		case INFO:
			infoEnabled = true;
		case WARN:
			warnEnabled = true;
		case ERROR:
			errorEnabled = true;
		default:
			break;
		}
		return this;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isTraceEnabled() {
		return traceEnabled;
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return traceEnabled;
	}

	@Override
	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return debugEnabled;
	}

	@Override
	public boolean isInfoEnabled() {
		return infoEnabled;
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return infoEnabled;
	}

	@Override
	public boolean isWarnEnabled() {
		return warnEnabled;
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return warnEnabled;
	}

	@Override
	public boolean isErrorEnabled() {
		return errorEnabled;
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return errorEnabled;
	}

	@Override
	public void trace(String msg) {
		this.handleCall(Level.TRACE, msg);
	}

	@Override
	public void trace(String format, Object arg) {
		this.handleCall(Level.TRACE, format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		this.handleCall(Level.TRACE, format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object... arguments) {
		this.handleCall(Level.TRACE, format, arguments);
	}

	@Override
	public void trace(String msg, Throwable t) {
		this.handleCall(Level.TRACE, msg, t);
	}

	@Override
	public void trace(Marker marker, String msg) {
		this.handleCall(Level.TRACE, msg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		this.handleCall(Level.TRACE, format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		this.handleCall(Level.TRACE, format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... arguments) {
		this.handleCall(Level.TRACE, format, arguments);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		this.handleCall(Level.TRACE, msg, t);
	}

	@Override
	public void debug(String msg) {
		this.handleCall(Level.DEBUG, msg);
	}

	@Override
	public void debug(String format, Object arg) {
		this.handleCall(Level.DEBUG, format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		this.handleCall(Level.DEBUG, format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object... arguments) {
		this.handleCall(Level.DEBUG, format, arguments);
	}

	@Override
	public void debug(String msg, Throwable t) {
		this.handleCall(Level.DEBUG, msg, t);
	}

	@Override
	public void debug(Marker marker, String msg) {
		this.handleCall(Level.DEBUG, msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		this.handleCall(Level.DEBUG, format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		this.handleCall(Level.DEBUG, format, arg1, arg2);
	}

	@Override
	public void debug(Marker marker, String format, Object... arguments) {
		this.handleCall(Level.DEBUG, format, arguments);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		this.handleCall(Level.DEBUG, msg, t);
	}

	@Override
	public void info(String msg) {
		this.handleCall(Level.INFO, msg);
	}

	@Override
	public void info(String format, Object arg) {
		this.handleCall(Level.INFO, format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		this.handleCall(Level.INFO, format, arg1, arg2);
	}

	@Override
	public void info(String format, Object... arguments) {
		this.handleCall(Level.INFO, format, arguments);
	}

	@Override
	public void info(String msg, Throwable t) {
		this.handleCall(Level.INFO, msg, t);
	}

	@Override
	public void info(Marker marker, String msg) {
		this.handleCall(Level.INFO, msg);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		this.handleCall(Level.INFO, format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		this.handleCall(Level.INFO, format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... arguments) {
		this.handleCall(Level.INFO, format, arguments);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		this.handleCall(Level.INFO, msg, t);
	}

	@Override
	public void warn(String msg) {
		this.handleCall(Level.WARN, msg);
	}

	@Override
	public void warn(String format, Object arg) {
		this.handleCall(Level.WARN, format, arg);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		this.handleCall(Level.WARN, format, arg1, arg2);
	}

	@Override
	public void warn(String format, Object... arguments) {
		this.handleCall(Level.WARN, format, arguments);
	}

	@Override
	public void warn(String msg, Throwable t) {
		this.handleCall(Level.WARN, msg, t);
	}

	@Override
	public void warn(Marker marker, String msg) {
		this.handleCall(Level.WARN, msg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		this.handleCall(Level.WARN, format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		this.handleCall(Level.WARN, format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object... arguments) {
		this.handleCall(Level.WARN, format, arguments);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		this.handleCall(Level.WARN, msg, t);
	}

	@Override
	public void error(String msg) {
		this.handleCall(Level.ERROR, msg);
	}

	@Override
	public void error(String format, Object arg) {
		this.handleCall(Level.ERROR, format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		this.handleCall(Level.ERROR, format, arg1, arg2);
	}

	@Override
	public void error(String format, Object... arguments) {
		this.handleCall(Level.ERROR, format, arguments);
	}

	@Override
	public void error(String msg, Throwable t) {
		this.handleCall(Level.ERROR, msg, t);
	}

	@Override
	public void error(Marker marker, String msg) {
		this.handleCall(Level.ERROR, msg);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		this.handleCall(Level.ERROR, format, arg);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		this.handleCall(Level.ERROR, format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... arguments) {
		this.handleCall(Level.ERROR, format, arguments);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		this.handleCall(Level.ERROR, msg, t);
	}
	
	public void title(String title) {
		ConsoleEngine.getInstance().print(null, null, "---------| " + title + " |---------");
	}
	
	private void handleCall(Level level, String msg) {
		ConsoleEngine.getInstance().print(level, name, msg);
	}
	
	private void handleCall(Level level, String format, Object... arguments) {
		FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
		this.handleCall(level, tp.getMessage(), tp.getThrowable());
	}
	
	private void handleCall(Level level, String msg, Throwable t) {
		if (t != null)
			msg += "\n" + t.getClass().getName() + ": " + t.getMessage();
		ConsoleEngine.getInstance().print(level, name, msg);
	}
}