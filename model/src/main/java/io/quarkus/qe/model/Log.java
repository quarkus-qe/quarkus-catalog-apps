package io.quarkus.qe.model;

import java.time.LocalDateTime;

public class Log {
    private Level level;
    private String message;
    private LocalDateTime timestamp;

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static final Log info(String message, Object... args) {
        return trace(Level.INFO, message, args);
    }

    public static final Log warning(String message, Object... args) {
        return trace(Level.WARNING, message, args);
    }

    public static final Log error(String message, Object... args) {
        return trace(Level.ERROR, message, args);
    }

    public static final Log trace(Level level, String message, Object... args) {
        Log log = new Log();
        log.level = level;
        log.message = String.format(message, args);
        log.timestamp = LocalDateTime.now();
        return log;
    }

    public enum Level {
        INFO,
        ERROR,
        WARNING
    }
}
