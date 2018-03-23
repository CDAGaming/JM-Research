package journeymap.client.task.main;

import org.apache.logging.log4j.*;
import journeymap.common.*;
import net.minecraft.client.*;
import journeymap.client.*;
import journeymap.client.ui.fullscreen.*;
import journeymap.client.log.*;
import journeymap.common.log.*;
import net.minecraft.client.gui.*;

public class MappingMonitorTask implements IMainThreadTask
{
    private static String NAME;
    Logger logger;
    private int lastDimension;
    
    public MappingMonitorTask() {
        this.logger = Journeymap.getLogger();
        this.lastDimension = 0;
    }
    
    @Override
    public IMainThreadTask perform(final Minecraft mc, final JourneymapClient jm) {
        try {
            if (!jm.isInitialized()) {
                return this;
            }
            final boolean isDead = mc.field_71462_r != null && mc.field_71462_r instanceof GuiGameOver;
            if (mc.field_71441_e == null) {
                if (jm.isMapping()) {
                    jm.stopMapping();
                }
                final GuiScreen guiScreen = mc.field_71462_r;
                if ((guiScreen instanceof GuiMainMenu || guiScreen instanceof GuiWorldSelection || guiScreen instanceof GuiMultiplayer) && jm.getCurrentWorldId() != null) {
                    this.logger.info("World ID has been reset.");
                    jm.setCurrentWorldId(null);
                }
                return this;
            }
            if (this.lastDimension != mc.field_71439_g.field_71093_bK) {
                this.lastDimension = mc.field_71439_g.field_71093_bK;
                if (jm.isMapping()) {
                    jm.stopMapping();
                }
            }
            else if (!jm.isMapping() && !isDead && Journeymap.getClient().getCoreProperties().mappingEnabled.get()) {
                jm.startMapping();
            }
            final boolean isGamePaused = mc.field_71462_r != null && !(mc.field_71462_r instanceof Fullscreen);
            if (isGamePaused && !jm.isMapping()) {
                return this;
            }
            if (!isGamePaused) {
                ChatLog.showChatAnnouncements(mc);
            }
            if (!jm.isMapping() && Journeymap.getClient().getCoreProperties().mappingEnabled.get()) {
                jm.startMapping();
            }
        }
        catch (Throwable t) {
            this.logger.error("Error in JourneyMap.performMainThreadTasks(): " + LogFormatter.toString(t));
        }
        return this;
    }
    
    @Override
    public String getName() {
        return MappingMonitorTask.NAME;
    }
    
    static {
        MappingMonitorTask.NAME = "Tick." + MappingMonitorTask.class.getSimpleName();
    }
}
