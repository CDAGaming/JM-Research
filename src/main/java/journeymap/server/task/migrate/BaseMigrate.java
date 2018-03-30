package journeymap.server.task.migrate;

import java.io.*;
import org.apache.logging.log4j.*;
import com.google.common.base.*;
import journeymap.server.*;
import journeymap.common.version.*;
import journeymap.server.properties.*;
import journeymap.common.log.*;
import java.util.concurrent.atomic.*;
import java.nio.file.*;

public abstract class BaseMigrate
{
    protected Logger logger;
    protected final String legacyVersion;
    protected final String currentVersion;
    protected final File legacyConfigDir;
    protected final File currentConfigDir;
    
    public BaseMigrate(final String legacyVersion, final String currentVersion) {
        this.logger = LogManager.getLogger("journeymap");
        this.legacyVersion = legacyVersion;
        this.currentVersion = currentVersion;
        final String legacyPath = Joiner.on(File.separator).join((Object)Constants.MC_DATA_DIR, (Object)Constants.JOURNEYMAP_DIR, new Object[] { "config", this.legacyVersion });
        this.legacyConfigDir = new File(legacyPath);
        this.currentConfigDir = new File(Constants.CONFIG_DIR);
    }
    
    public boolean isActive(final Version currentVersion) {
        if (!this.currentVersion.equals(currentVersion.toMajorMinorString())) {
            return false;
        }
        if (!this.legacyConfigDir.canRead()) {
            return false;
        }
        final File globalConfig = new GlobalProperties(true).getFile();
        final File overworldConfig = new DimensionProperties(0, true).getFile();
        return !globalConfig.exists() || !overworldConfig.exists();
    }
    
    protected boolean migrateConfigs() {
        try {
            this.logger.info(String.format("Migrating server configs from %s to %s", this.legacyVersion, this.currentVersion));
            final boolean errorFree = this.copyConfigFiles();
            PropertiesManager.getInstance();
            if (!errorFree) {
                this.logger.warn("MIGRATION OF CONFIG FILES DID NOT COMPLETELY SUCCEED. You should examine the results in " + this.currentConfigDir);
            }
            return true;
        }
        catch (Throwable t) {
            this.logger.error(String.format("Unexpected error migrating configs: %s", LogFormatter.toPartialString(t)));
            return false;
        }
    }
    
    protected boolean copyConfigFiles() throws Exception {
        final AtomicBoolean errorFree = new AtomicBoolean(true);
        final File currentConfigDir = new File(Constants.CONFIG_DIR);
        final File file;
        final File currentFile;
        final AtomicBoolean atomicBoolean;
        Files.list(this.legacyConfigDir.toPath()).forEach(legacyFile -> {
            if (legacyFile.startsWith("journeymap.server.") && legacyFile.endsWith(".config")) {
                currentFile = new File(file, legacyFile.getFileName().toString());
                if (!currentFile.exists()) {
                    try {
                        Files.copy(legacyFile, currentFile.toPath(), new CopyOption[0]);
                    }
                    catch (Exception e) {
                        atomicBoolean.set(false);
                        this.logger.error("Unable to copy config file: " + LogFormatter.toPartialString(e));
                    }
                }
            }
            return;
        });
        return errorFree.get();
    }
}
