package javatower.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility for the game.
 * Supports console and file output with different log levels.
 */
public class Logger {
    
    public enum Level {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3);
        
        private final int value;
        
        Level(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    private static Level currentLevel = Level.DEBUG;
    private static boolean fileLoggingEnabled = false;
    private static String logFilePath = "javatower.log";
    private static PrintWriter fileWriter = null;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    // Static initializer
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }));
    }
    
    /**
     * Sets the minimum log level to output.
     * 
     * @param level the minimum level to log
     */
    public static void setLevel(Level level) {
        currentLevel = level;
    }
    
    /**
     * Enables logging to a file.
     * 
     * @param path the path to the log file
     */
    public static void enableFileLogging(String path) {
        logFilePath = path;
        fileLoggingEnabled = true;
        try {
            fileWriter = new PrintWriter(new FileWriter(logFilePath, true));
        } catch (IOException e) {
            System.err.println("Failed to open log file: " + e.getMessage());
            fileLoggingEnabled = false;
        }
    }
    
    /**
     * Disables file logging.
     */
    public static void disableFileLogging() {
        fileLoggingEnabled = false;
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }
    }
    
    private static void log(Level level, String message) {
        if (level.getValue() < currentLevel.getValue()) {
            return;
        }
        
        String timestamp = LocalDateTime.now().format(formatter);
        String logLine = String.format("[%s] [%s] %s", timestamp, level.name(), message);
        
        // Console output
        switch (level) {
            case ERROR:
                System.err.println(logLine);
                break;
            case WARN:
                System.out.println(logLine);
                break;
            default:
                System.out.println(logLine);
                break;
        }
        
        // File output
        if (fileLoggingEnabled && fileWriter != null) {
            fileWriter.println(logLine);
            fileWriter.flush();
        }
    }
    
    private static void log(Level level, String format, Object... args) {
        log(level, String.format(format, args));
    }
    
    /**
     * Logs a debug message.
     * 
     * @param message the message to log
     */
    public static void debug(String message) {
        log(Level.DEBUG, message);
    }
    
    /**
     * Logs a formatted debug message.
     * 
     * @param format the format string
     * @param args the arguments
     */
    public static void debug(String format, Object... args) {
        log(Level.DEBUG, format, args);
    }
    
    /**
     * Logs an info message.
     * 
     * @param message the message to log
     */
    public static void info(String message) {
        log(Level.INFO, message);
    }
    
    /**
     * Logs a formatted info message.
     * 
     * @param format the format string
     * @param args the arguments
     */
    public static void info(String format, Object... args) {
        log(Level.INFO, format, args);
    }
    
    /**
     * Logs a warning message.
     * 
     * @param message the message to log
     */
    public static void warn(String message) {
        log(Level.WARN, message);
    }
    
    /**
     * Logs a formatted warning message.
     * 
     * @param format the format string
     * @param args the arguments
     */
    public static void warn(String format, Object... args) {
        log(Level.WARN, format, args);
    }
    
    /**
     * Logs an error message.
     * 
     * @param message the message to log
     */
    public static void error(String message) {
        log(Level.ERROR, message);
    }
    
    /**
     * Logs a formatted error message.
     * 
     * @param format the format string
     * @param args the arguments
     */
    public static void error(String format, Object... args) {
        log(Level.ERROR, format, args);
    }
    
    /**
     * Logs an error with exception details.
     * 
     * @param message the message to log
     * @param throwable the exception to log
     */
    public static void error(String message, Throwable throwable) {
        log(Level.ERROR, message + ": " + throwable.getMessage());
        if (fileLoggingEnabled && fileWriter != null) {
            throwable.printStackTrace(fileWriter);
            fileWriter.flush();
        }
    }
}
