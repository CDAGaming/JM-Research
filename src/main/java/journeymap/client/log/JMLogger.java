package journeymap.client.log;

import java.util.concurrent.atomic.*;
import org.apache.logging.log4j.core.appender.*;
import journeymap.client.io.*;
import org.apache.logging.log4j.core.layout.*;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.pattern.*;
import java.nio.charset.*;
import journeymap.common.log.*;
import journeymap.common.*;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.impl.*;
import org.apache.logging.log4j.message.*;
import org.apache.logging.log4j.core.*;
import net.minecraftforge.common.*;
import net.minecraft.client.*;
import journeymap.client.*;
import java.util.*;
import journeymap.common.properties.config.*;
import java.io.*;

public class JMLogger
{
    public static final String DEPRECATED_LOG_FILE = "journeyMap.log";
    public static final String LOG_FILE = "journeymap.log";
    private static final HashSet<Integer> singletonErrors;
    private static final AtomicInteger singletonErrorsCounter;
    private static RandomAccessFileAppender fileAppender;
    
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
        }
        catch (Exception e) {
            logger.error("Error removing deprecated logfile: " + e.getMessage());
        }
        try {
            final File logFile = getLogFile();
            if (logFile.exists()) {
                logFile.delete();
            }
            else {
                logFile.getParentFile().mkdirs();
            }
            final PatternLayout layout = PatternLayout.createLayout("[%d{HH:mm:ss}] [%t/%level] [%C{1}] %msg%n", (PatternSelector)null, (Configuration)null, (RegexReplacement)null, (Charset)null, true, false, (String)null, (String)null);
            JMLogger.fileAppender = RandomAccessFileAppender.createAppender(logFile.getAbsolutePath(), "treu", "journeymap-logfile", "true", (String)null, "true", (Layout)layout, (Filter)null, "false", (String)null, (Configuration)null);
            ((org.apache.logging.log4j.core.Logger)logger).addAppender((Appender)JMLogger.fileAppender);
            if (!JMLogger.fileAppender.isStarted()) {
                JMLogger.fileAppender.start();
            }
            logger.info("JourneyMap log initialized.");
        }
        catch (SecurityException e2) {
            logger.error("Error adding file handler: " + LogFormatter.toString(e2));
        }
        catch (Throwable e3) {
            logger.error("Error adding file handler: " + LogFormatter.toString(e3));
        }
        return logger;
    }
    
    public static void setLevelFromProperties() {
        try {
            final Logger logger = LogManager.getLogger("journeymap");
            ((org.apache.logging.log4j.core.Logger)logger).setLevel(Level.toLevel(Journeymap.getClient().getCoreProperties().logLevel.get(), Level.INFO));
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public static void logProperties() {
        final LogEvent record = (LogEvent)new Log4jLogEvent(JourneymapClient.MOD_NAME, MarkerManager.getMarker(JourneymapClient.MOD_NAME), (String)null, Level.INFO, (Message)new SimpleMessage(getPropertiesSummary()), (Throwable)null);
        if (JMLogger.fileAppender != null) {
            JMLogger.fileAppender.append(record);
        }
    }
    
    public static String getPropertiesSummary() {
        final LinkedHashMap<String, String> props = new LinkedHashMap<String, String>();
        props.put("Version", JourneymapClient.MOD_NAME + ", built with Forge " + "14.23.1.2555");
        props.put("Forge", ForgeVersion.getVersion());
        final List<String> envProps = Arrays.asList("os.name, os.arch, java.version, user.country, user.language");
        StringBuilder sb = new StringBuilder();
        for (final String env : envProps) {
            sb.append(env).append("=").append(System.getProperty(env)).append(", ");
        }
        sb.append("game language=").append(Minecraft.func_71410_x().field_71474_y.field_74363_ab).append(", ");
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
        }
        else {
            final int count = JMLogger.singletonErrorsCounter.incrementAndGet();
            if (count > 1000) {
                JMLogger.singletonErrors.clear();
                JMLogger.singletonErrorsCounter.set(0);
            }
        }
    }
    
    static {
        singletonErrors = new HashSet<Integer>();
        singletonErrorsCounter = new AtomicInteger(0);
    }
    
    public static class LogLevelStringProvider implements StringField.ValuesProvider
    {
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
