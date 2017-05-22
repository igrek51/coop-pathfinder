package igrek.mgr.robopath.logger;

public class Logs {
	
	// CONFIG
	private static final LogLevel CONSOLE_LEVEL = LogLevel.ALL;
	private static final LogLevel SHOW_TRACE_DETAILS_LEVEL = LogLevel.TRACE;
	private static final boolean SHOW_EXCEPTIONS_TRACE = true;
	
	private static final Object PRINT_LOCK = new Object();
	
	// console text formatting characters
	private static String C_RESET = "\033[0m";
	private static String C_BOLD = "\033[1m";
	private static String C_DIM = "\033[2m";
	private static String C_ITALIC = "\033[3m";
	private static String C_UNDERLINE = "\033[4m";
	
	private static String C_BLACK = "\033[30m";
	private static String C_RED = "\033[31m";
	private static String C_GREEN = "\033[32m";
	private static String C_YELLOW = "\033[33m";
	private static String C_BLUE = "\033[34m";
	private static String C_MAGENTA = "\033[35m";
	private static String C_CYAN = "\033[36m";
	private static String C_WHITE = "\033[37m";
	
	public static void error(String message) {
		log(message, LogLevel.ERROR, C_BOLD + C_RED + "[ERROR] " + C_RESET);
	}
	
	public static void error(Throwable ex) {
		synchronized (PRINT_LOCK) {
			if (SHOW_EXCEPTIONS_TRACE) {
				System.out.print(C_BOLD + C_RED + "[ERROR] " + C_RESET);
				ex.printStackTrace();
				System.out.print(C_RESET);
			} else {
				log(ex.getMessage(), LogLevel.ERROR, C_BOLD + C_RED + "[ERROR] " + C_RESET);
			}
		}
	}
	
	public static void error(String message, Throwable ex) {
		error(message);
		error(ex);
	}
	
	public static void fatal(String message) {
		error(message);
		System.exit(1);
	}
	
	public static void fatal(Throwable ex) {
		error(ex);
		System.exit(1);
	}
	
	public static void fatal(String message, Throwable ex) {
		error(message);
		error(ex);
		System.exit(1);
	}
	
	public static void warn(String message) {
		log(message, LogLevel.WARN, C_BOLD + C_YELLOW + "[warn] " + C_RESET);
	}
	
	public static void info(String message) {
		log(message, LogLevel.INFO, C_BOLD + C_BLUE + "[info] " + C_RESET);
	}
	
	public static void debug(String message) {
		log(message, LogLevel.DEBUG, C_BOLD + C_GREEN + "[debug] " + C_RESET);
	}
	
	public static void trace(String message) {
		log(message, LogLevel.TRACE, C_GREEN + "[trace] " + C_RESET);
	}
	
	
	private static void log(String message, LogLevel level, String logPrefix) {
		
		if (level.lowerOrEqual(CONSOLE_LEVEL)) {
			
			String consoleMessage;
			
			if (level.higherOrEqual(SHOW_TRACE_DETAILS_LEVEL)) {
				final int stackTraceIndex = 3;
				
				StackTraceElement ste = Thread.currentThread().getStackTrace()[stackTraceIndex];
				
				String methodName = ste.getMethodName();
				String fileName = ste.getFileName();
				int lineNumber = ste.getLineNumber();
				
				consoleMessage = logPrefix + C_DIM + methodName + "(" + fileName + ":" + lineNumber + "): " + C_RESET + message;
			} else {
				consoleMessage = logPrefix + message;
			}
			
			synchronized (PRINT_LOCK) {
				System.out.println(consoleMessage);
			}
		}
	}
	
	public static void printStackTrace() {
		synchronized (PRINT_LOCK) {
			int i = 0;
			for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
				i++;
				if (i <= 3)
					continue;
				String methodName = ste.getMethodName();
				String fileName = ste.getFileName();
				int lineNumber = ste.getLineNumber();
				String consoleMessage = "[trace] STACK TRACE " + (i - 3) + ": " + methodName + "(" + fileName + ":" + lineNumber + ")";
				System.out.println(consoleMessage);
			}
		}
	}
	
}
