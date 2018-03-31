package journeymap.client.forge.event;

import journeymap.client.render.texture.TextureCache;
import journeymap.client.task.main.EnsureCurrentColorsTask;
import journeymap.client.task.main.IMainThreadTask;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TextureAtlasHandler implements EventHandlerManager.EventHandler {
    IMainThreadTask task;

    public TextureAtlasHandler() {
        this.task = new EnsureCurrentColorsTask();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTextureStiched(final TextureStitchEvent.Post event) {
        try {
            TextureCache.reset();
            UIManager.INSTANCE.getMiniMap().reset();
            Fullscreen.state().requireRefresh();
            MiniMap.state().requireRefresh();
            Journeymap.getClient().queueMainThreadTask(this.task);
        } catch (Exception e) {
            Journeymap.getLogger().warn("Error queuing TextureAtlasHandlerTask: " + LogFormatter.toString(e));
        }
    }
}
