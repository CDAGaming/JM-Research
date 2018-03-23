package journeymap.client.task.migrate;

import journeymap.common.migrate.*;
import java.nio.charset.*;
import com.google.gson.*;
import org.apache.logging.log4j.*;
import journeymap.common.version.*;
import journeymap.common.*;
import java.io.*;
import com.google.common.base.*;
import journeymap.client.*;
import journeymap.client.io.*;
import journeymap.client.properties.*;
import journeymap.common.properties.*;
import journeymap.common.log.*;
import java.util.*;

public class Migrate54to55 implements MigrationTask
{
    protected static final Charset UTF8;
    protected final transient Gson gson;
    Logger logger;
    
    public Migrate54to55() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.logger = LogManager.getLogger("journeymap");
    }
    
    @Override
    public boolean isActive(final Version currentVersion) {
        if (currentVersion.toMajorMinorString().equals("5.5")) {
            if (Journeymap.getClient().getCoreProperties() == null) {
                Journeymap.getClient().loadConfigProperties();
            }
            final String optionsManagerViewed = Journeymap.getClient().getCoreProperties().optionsManagerViewed.get();
            if (Strings.isNullOrEmpty(optionsManagerViewed)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Boolean call() throws Exception {
        return this.migrateConfigs();
    }
    
    private boolean migrateConfigs() {
        try {
            final String path5_4 = Joiner.on(File.separator).join((Object)Constants.JOURNEYMAP_DIR, (Object)"config", new Object[] { "5.4" });
            final File legacyConfigDir = new File(FileHandler.MinecraftDirectory, path5_4);
            if (!legacyConfigDir.canRead()) {
                return true;
            }
            this.logger.info("Migrating configs from 5.4 to 5.5");
            final List<? extends PropertiesBase> propertiesList = Arrays.asList(Journeymap.getClient().getCoreProperties(), Journeymap.getClient().getFullMapProperties(), Journeymap.getClient().getMiniMapProperties(1), Journeymap.getClient().getMiniMapProperties(2), Journeymap.getClient().getWaypointProperties(), Journeymap.getClient().getWebMapProperties());
            for (final PropertiesBase properties : propertiesList) {
                final File oldConfigfile = new File(legacyConfigDir, properties.getFile().getName());
                if (oldConfigfile.canRead()) {
                    try {
                        properties.load(oldConfigfile, false);
                        properties.save();
                    }
                    catch (Throwable t) {
                        this.logger.error(String.format("Unexpected error in migrateConfigs(): %s", LogFormatter.toString(t)));
                    }
                }
            }
            Journeymap.getClient().getCoreProperties().optionsManagerViewed.set("5.4");
            return true;
        }
        catch (Throwable t2) {
            this.logger.error(String.format("Unexpected error in migrateConfigs(): %s", LogFormatter.toString(t2)));
            return false;
        }
    }
    
    static {
        UTF8 = Charset.forName("UTF-8");
    }
}
