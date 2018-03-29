package journeymap.client.task.migrate;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.client.Constants;
import journeymap.client.io.FileHandler;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.migrate.MigrationTask;
import journeymap.common.properties.PropertiesBase;
import journeymap.common.version.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class Migrate54to55 implements MigrationTask {
    protected static final Charset UTF8;

    static {
        UTF8 = Charset.forName("UTF-8");
    }

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
            return Strings.isNullOrEmpty(optionsManagerViewed);
        }
        return false;
    }

    @Override
    public Boolean call() throws Exception {
        return this.migrateConfigs();
    }

    private boolean migrateConfigs() {
        try {
            final String path5_4 = Joiner.on(File.separator).join(Constants.JOURNEYMAP_DIR, "config", "5.4");
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
                    } catch (Throwable t) {
                        this.logger.error(String.format("Unexpected error in migrateConfigs(): %s", LogFormatter.toString(t)));
                    }
                }
            }
            Journeymap.getClient().getCoreProperties().optionsManagerViewed.set("5.4");
            return true;
        } catch (Throwable t2) {
            this.logger.error(String.format("Unexpected error in migrateConfigs(): %s", LogFormatter.toString(t2)));
            return false;
        }
    }
}
