package journeymap.client.forge.event;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.log.JMLogger;
import journeymap.client.log.StatTimer;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.client.ui.UIManager;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MiniMapOverlayHandler implements EventHandlerManager.EventHandler {
    private static final String DEBUG_PREFIX;
    private static final String DEBUG_SUFFIX = "";
    private static RenderGameOverlayEvent.ElementType EVENT_TYPE;
    private static boolean EVENT_PRE;

    static {
        DEBUG_PREFIX = TextFormatting.AQUA + "[JM] " + TextFormatting.RESET;
        MiniMapOverlayHandler.EVENT_TYPE = RenderGameOverlayEvent.ElementType.ALL;
        MiniMapOverlayHandler.EVENT_PRE = true;
    }

    private final Minecraft mc;
    private JourneymapClient jm;
    private long statTimerCheck;
    private List<String> statTimerReport;

    public MiniMapOverlayHandler() {
        this.mc = FMLClientHandler.instance().getClient();
        this.statTimerReport = (List<String>) Collections.EMPTY_LIST;
    }

    public static void checkEventConfig() {
        MiniMapOverlayHandler.EVENT_TYPE = Journeymap.getClient().getCoreProperties().renderOverlayEventTypeName.get();
        MiniMapOverlayHandler.EVENT_PRE = Journeymap.getClient().getCoreProperties().renderOverlayPreEvent.get();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlayDebug(final RenderGameOverlayEvent.Text event) {
        try {
            if (this.mc.gameSettings.showDebugInfo) {
                event.getLeft().add(null);
                if (Journeymap.getClient().getCoreProperties().mappingEnabled.get()) {
                    for (final String line : MapPlayerTask.getDebugStats()) {
                        event.getLeft().add(MiniMapOverlayHandler.DEBUG_PREFIX + line + "");
                    }
                } else {
                    event.getLeft().add(Constants.getString("jm.common.enable_mapping_false_text") + "");
                }
                if (this.mc.gameSettings.showDebugProfilerChart) {
                    if (System.currentTimeMillis() - this.statTimerCheck > 3000L) {
                        this.statTimerReport = StatTimer.getReportByTotalTime(MiniMapOverlayHandler.DEBUG_PREFIX, "");
                        this.statTimerCheck = System.currentTimeMillis();
                    }
                    event.getLeft().add(null);
                    for (final String line2 : this.statTimerReport) {
                        event.getLeft().add(line2);
                    }
                }
            }
        } catch (Throwable t) {
            JMLogger.logOnce("Unexpected error during onRenderOverlayEarly: " + t, t);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(final RenderGameOverlayEvent event) {
        try {
            if (event.getType() == MiniMapOverlayHandler.EVENT_TYPE && event.isCancelable() == MiniMapOverlayHandler.EVENT_PRE) {
                UIManager.INSTANCE.drawMiniMap();
            }
        } catch (Throwable t) {
            JMLogger.logOnce("Unexpected error during onRenderOverlayEarly: " + t, t);
        }
    }
}
