package journeymap.client.task.main;

import journeymap.client.JourneymapClient;
import journeymap.client.data.DataCache;
import journeymap.client.forge.event.MiniMapOverlayHandler;
import journeymap.client.io.ThemeLoader;
import journeymap.client.log.JMLogger;
import journeymap.client.render.map.TileDrawStepCache;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

public class SoftResetTask implements IMainThreadTask {
    private static String NAME;

    static {
        SoftResetTask.NAME = "Tick." + SoftResetTask.class.getSimpleName();
    }

    Logger logger;

    private SoftResetTask() {
        this.logger = Journeymap.getLogger();
    }

    public static void queue() {
        Journeymap.getClient().queueMainThreadTask(new SoftResetTask());
    }

    @Override
    public IMainThreadTask perform(final Minecraft mc, final JourneymapClient jm) {
        jm.loadConfigProperties();
        JMLogger.setLevelFromProperties();
        DataCache.INSTANCE.purge();
        TileDrawStepCache.instance().invalidateAll();
        UIManager.INSTANCE.reset();
        WaypointStore.INSTANCE.reset();
        MiniMapOverlayHandler.checkEventConfig();
        ThemeLoader.getCurrentTheme(true);
        MiniMap.state().requireRefresh();
        Fullscreen.state().requireRefresh();
        UIManager.INSTANCE.getMiniMap().updateDisplayVars(true);
        return null;
    }

    @Override
    public String getName() {
        return SoftResetTask.NAME;
    }
}
