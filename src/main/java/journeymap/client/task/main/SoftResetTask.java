package journeymap.client.task.main;

import org.apache.logging.log4j.*;
import journeymap.common.*;
import net.minecraft.client.*;
import journeymap.client.*;
import journeymap.client.log.*;
import journeymap.client.data.*;
import journeymap.client.render.map.*;
import journeymap.client.ui.*;
import journeymap.client.waypoint.*;
import journeymap.client.forge.event.*;
import journeymap.client.io.*;
import journeymap.client.ui.minimap.*;
import journeymap.client.ui.fullscreen.*;

public class SoftResetTask implements IMainThreadTask
{
    private static String NAME;
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
    
    static {
        SoftResetTask.NAME = "Tick." + SoftResetTask.class.getSimpleName();
    }
}
