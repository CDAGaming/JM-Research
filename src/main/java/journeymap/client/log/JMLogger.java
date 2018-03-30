package journeymap.client.log;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.io.FileHandler;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.config.StringField;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeVersion;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class JMLogger {
    public static final String DEPRECATED_LOG_FILE = "journeyMap.log";
    public static final String LOG_FILE = "journeymap.log";
    private static final HashSet<Integer> singletonErrors;
    private static final AtomicInteger singletonErrorsCounter;
    private static RandomAccessFileAppender fileAppender;

    static {
        singletonErrors = new HashSet<>();
        singletonErrorsCounter = new AtomicInteger(0);
    }

    public static Logger init() {
        final Logger logger = LogManager.getLogger("journeymap");
        if (!logger.isInfoEnabled()) {
            logger.warn("Forge is surpressing INFO-level logging. If you need technical support for JourneyMap, you must return logging to INFO.");
        }
        try {
            final File deprecatedLog = new File(FileHandler.getJourneyMapDir(), "journeyMap.log");
            if (deprecatedLog.exists()) {
                deprecatedLog.delete();
            }
        } catch (Exception e) {
            logger.error("Error removing deprecated logfile: " + e.getMessage());
        }
        try {
            final File logFile = getLogFile();
            if (logFile.exists()) {
                logFile.delete();
            } else {
                logFile.getParentFile().mkdirs();
            }
            final PatternLayout layout = PatternLayout.createLayout("[%d{HH:mm:ss}] [%t/%level] [%C{1}] %msg%n", null, null, null, null, true, false, null, null);
            JMLogger.fileAppender = RandomAccessFileAppender.createAppender(logFile.getAbsolutePath(), "treu", "journeymap-logfile", "true", null, "true", layout, null, "false", null, null);
            ((org.apache.logging.log4j.core.Logger) logger).addAppender(JMLogger.fileAppender);
            if (!JMLogger.fileAppender.isStarted()) {
                JMLogger.fileAppender.start();
            }
            logger.info("JourneyMap log initialized.");
        } catch (SecurityException e2) {
            logger.error("Error adding file handler: " + LogFormatter.toString(e2));
        } catch (Throwable e3) {
            logger.error("Error adding file handler: " + LogFormatter.toString(e3));
        }
        return logger;
    }

    public static void setLevelFromProperties() {
        try {
            final Logger logger = LogManager.getLogger("journeymap");
            ((org.apache.logging.log4j.core.Logger) logger).setLevel(Level.toLevel(Journeymap.getClient().getCoreProperties().logLevel.get(), Level.INFO));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void logProperties() {
        final LogEvent record = new Log4jLogEvent(JourneymapClient.MOD_NAME, MarkerManager.getMarker(JourneymapClient.MOD_NAME), null, Level.INFO, new SimpleMessage(getPropertiesSummary()), null);
        if (JMLogger.fileAppender != null) {
            JMLogger.fileAppender.append(record);
        }
    }

    public static String getPropertiesSummary() {
        final LinkedHashMap<String, String> props = new LinkedHashMap<>();
        props.put("Version", JourneymapClient.MOD_NAME + ", built with Forge " + "14.23.1.2555");
        props.put("Forge", ForgeVersion.getVersion());
        final List<String> envProps = Collections.singletonList("os.name, os.arch, java.version, user.country, user.language");
        StringBuilder sb = new StringBuilder();
        for (final String env : envProps) {
            sb.append(env).append("=").append(System.getProperty(env)).append(", ");
        }
        sb.append("game language=").append(Minecraft.getMinecraft().gameSettings.language).append(", ");
        sb.append("locale=").append(Constants.getLocale());
        props.put("Environment", sb.toString());
        sb = new StringBuilder();
        for (final Map.Entry<String, String> prop : props.entrySet()) {
            if (sb.length() > 0) {
                sb.append(LogFormatter.LINEBREAK);
            }
            sb.append(prop.getKey()).append(": ").append(prop.getValue());
        }
        return sb.toString();
    }

    public static File getLogFile() {
        return new File(FileHandler.getJourneyMapDir(), "journeymap.log");
    }

    public static void logOnce(final String text, final Throwable throwable) {
        if (!JMLogger.singletonErrors.contains(text.hashCode())) {
            JMLogger.singletonErrors.add(text.hashCode());
            Journeymap.getLogger().error(text + " (SUPPRESSED)");
            if (throwable != null) {
                Journeymap.getLogger().error(LogFormatter.toString(throwable));
            }
        } else {
            final int count = JMLogger.singletonErrorsCounter.incrementAndGet();
            if (count > 1000) {
                JMLogger.singletonErrors.clear();
                JMLogger.singletonErrorsCounter.set(0);
            }
        }
    }

    public static class LogLevelStringProvider implements StringField.ValuesProvider {
        @Override
        public List<String> getStrings() {
            final Level[] levels = Level.values();
            final String[] levelStrings = new String[levels.length];
            for (int i = 0; i < levels.length; ++i) {
                levelStrings[i] = levels[i].toString();
            }
            return Arrays.asList(levelStrings);
        }

        @Override
        public String getDefaultString() {
            return Level.INFO.toString();
        }
    }
}
