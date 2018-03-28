package journeymap.client.task.main;

import journeymap.client.JourneymapClient;
import journeymap.client.log.ChatLog;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import org.apache.logging.log4j.Logger;

public class MappingMonitorTask implements IMainThreadTask {
    private static String NAME;

    static {
        MappingMonitorTask.NAME = "Tick." + MappingMonitorTask.class.getSimpleName();
    }

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
            final boolean isDead = mc.currentScreen != null && mc.currentScreen instanceof GuiGameOver;
            if (mc.world == null) {
                if (jm.isMapping()) {
                    jm.stopMapping();
                }
                final GuiScreen guiScreen = mc.currentScreen;
                if ((guiScreen instanceof GuiMainMenu || guiScreen instanceof GuiWorldSelection || guiScreen instanceof GuiMultiplayer) && jm.getCurrentWorldId() != null) {
                    this.logger.info("World ID has been reset.");
                    jm.setCurrentWorldId(null);
                }
                return this;
            }
            if (this.lastDimension != mc.player.dimension) {
                this.lastDimension = mc.player.dimension;
                if (jm.isMapping()) {
                    jm.stopMapping();
                }
            } else if (!jm.isMapping() && !isDead && Journeymap.getClient().getCoreProperties().mappingEnabled.get()) {
                jm.startMapping();
            }
            final boolean isGamePaused = mc.currentScreen != null && !(mc.currentScreen instanceof Fullscreen);
            if (isGamePaused && !jm.isMapping()) {
                return this;
            }
            if (!isGamePaused) {
                ChatLog.showChatAnnouncements(mc);
            }
            if (!jm.isMapping() && Journeymap.getClient().getCoreProperties().mappingEnabled.get()) {
                jm.startMapping();
            }
        } catch (Throwable t) {
            this.logger.error("Error in JourneyMap.performMainThreadTasks(): " + LogFormatter.toString(t));
        }
        return this;
    }

    @Override
    public String getName() {
        return MappingMonitorTask.NAME;
    }
}
