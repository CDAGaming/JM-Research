package journeymap.client.forge.event;

import journeymap.client.task.main.*;
import net.minecraftforge.client.event.*;
import journeymap.client.render.texture.*;
import journeymap.client.ui.*;
import journeymap.client.ui.fullscreen.*;
import journeymap.client.ui.minimap.*;
import journeymap.common.*;
import journeymap.common.log.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.fml.common.eventhandler.*;

public class TextureAtlasHandler implements EventHandlerManager.EventHandler
{
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
        }
        catch (Exception e) {
            Journeymap.getLogger().warn("Error queuing TextureAtlasHandlerTask: " + LogFormatter.toString(e));
        }
    }
}
