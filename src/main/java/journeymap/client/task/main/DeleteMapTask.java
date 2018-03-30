package journeymap.client.task.main;

import org.apache.logging.log4j.*;
import journeymap.common.*;
import net.minecraft.client.*;
import journeymap.client.*;
import journeymap.client.task.multi.*;
import journeymap.client.render.map.*;
import journeymap.client.data.*;
import journeymap.client.model.*;
import journeymap.client.ui.fullscreen.*;
import journeymap.client.log.*;

public class DeleteMapTask implements IMainThreadTask
{
    private static String NAME;
    private static Logger LOGGER;
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
                ChatLog.announceI18N("jm.common.deletemap_status_done", new Object[0]);
            }
            else {
                ChatLog.announceI18N("jm.common.deletemap_status_error", new Object[0]);
            }
            if (wasMapping) {
                Journeymap.getClient().startMapping();
                MapPlayerTask.forceNearbyRemap();
            }
            Fullscreen.state().requireRefresh();
        }
        finally {
            GridRenderer.setEnabled(true);
            jm.toggleTask(MapPlayerTask.Manager.class, true, true);
        }
        return null;
    }
    
    @Override
    public String getName() {
        return DeleteMapTask.NAME;
    }
    
    static {
        DeleteMapTask.NAME = "Tick." + MappingMonitorTask.class.getSimpleName();
        DeleteMapTask.LOGGER = Journeymap.getLogger();
    }
}
