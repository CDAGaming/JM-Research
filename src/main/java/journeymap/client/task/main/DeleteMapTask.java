package journeymap.client.task.main;

import journeymap.client.JourneymapClient;
import journeymap.client.data.DataCache;
import journeymap.client.log.ChatLog;
import journeymap.client.model.RegionImageCache;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.client.task.multi.MapRegionTask;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

public class DeleteMapTask implements IMainThreadTask {
    private static String NAME;
    private static Logger LOGGER;

    static {
        DeleteMapTask.NAME = "Tick." + MappingMonitorTask.class.getSimpleName();
        DeleteMapTask.LOGGER = Journeymap.getLogger();
    }

    boolean allDims;

    private DeleteMapTask(final boolean allDims) {
        this.allDims = allDims;
    }

    public static void queue(final boolean allDims) {
        Journeymap.getClient().queueMainThreadTask(new DeleteMapTask(allDims));
    }

    @Override
    public final IMainThreadTask perform(final Minecraft mc, final JourneymapClient jm) {
        try {
            jm.toggleTask(MapPlayerTask.Manager.class, false, false);
            jm.toggleTask(MapRegionTask.Manager.class, false, false);
            GridRenderer.setEnabled(false);
            final boolean wasMapping = Journeymap.getClient().isMapping();
            if (wasMapping) {
                Journeymap.getClient().stopMapping();
            }
            DataCache.INSTANCE.invalidateChunkMDCache();
            final boolean ok = RegionImageCache.INSTANCE.deleteMap(Fullscreen.state(), this.allDims);
            if (ok) {
                ChatLog.announceI18N("jm.common.deletemap_status_done");
            } else {
                ChatLog.announceI18N("jm.common.deletemap_status_error");
            }
            if (wasMapping) {
                Journeymap.getClient().startMapping();
                MapPlayerTask.forceNearbyRemap();
            }
            Fullscreen.state().requireRefresh();
        } finally {
            GridRenderer.setEnabled(true);
            jm.toggleTask(MapPlayerTask.Manager.class, true, true);
        }
        return null;
    }

    @Override
    public String getName() {
        return DeleteMapTask.NAME;
    }
}
