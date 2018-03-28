package journeymap.client.task.multi;

import journeymap.client.JourneymapClient;
import journeymap.client.io.MapSaver;
import journeymap.client.model.MapType;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class SaveMapTask implements ITask {
    private static final Logger logger;
    public static MapType MAP_TYPE;

    static {
        logger = Journeymap.getLogger();
    }

    MapSaver mapSaver;

    private SaveMapTask(final MapSaver mapSaver) {
        this.mapSaver = mapSaver;
    }

    @Override
    public int getMaxRuntime() {
        return 120000;
    }

    @Override
    public void performTask(final Minecraft mc, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) {
        this.mapSaver.saveMap();
    }

    public static class Manager implements ITaskManager {
        MapSaver mapSaver;

        @Override
        public Class<? extends ITask> getTaskClass() {
            return SaveMapTask.class;
        }

        @Override
        public boolean enableTask(final Minecraft minecraft, final Object params) {
            if (params != null && params instanceof MapSaver) {
                this.mapSaver = (MapSaver) params;
            }
            return this.isEnabled(minecraft);
        }

        @Override
        public boolean isEnabled(final Minecraft minecraft) {
            return this.mapSaver != null;
        }

        @Override
        public void disableTask(final Minecraft minecraft) {
            this.mapSaver = null;
        }

        @Override
        public SaveMapTask getTask(final Minecraft minecraft) {
            if (this.mapSaver == null) {
                return null;
            }
            return new SaveMapTask(this.mapSaver);
        }

        @Override
        public void taskAccepted(final ITask task, final boolean accepted) {
            this.mapSaver = null;
        }
    }
}
