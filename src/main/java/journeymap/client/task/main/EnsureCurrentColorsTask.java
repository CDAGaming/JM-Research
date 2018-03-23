package journeymap.client.task.main;

import journeymap.client.log.*;
import net.minecraft.client.*;
import journeymap.client.*;
import journeymap.client.data.*;
import journeymap.client.mod.*;
import journeymap.client.cartography.color.*;
import journeymap.client.task.multi.*;

public class EnsureCurrentColorsTask implements IMainThreadTask
{
    final boolean forceReset;
    final boolean announce;
    
    public EnsureCurrentColorsTask() {
        this(false, false);
    }
    
    public EnsureCurrentColorsTask(final boolean forceReset, final boolean announce) {
        this.forceReset = forceReset;
        this.announce = announce;
        if (announce) {
            ChatLog.announceI18N("jm.common.colorreset_start", new Object[0]);
        }
    }
    
    @Override
    public IMainThreadTask perform(final Minecraft mc, final JourneymapClient jm) {
        if (this.forceReset) {
            DataCache.INSTANCE.resetBlockMetadata();
            ModBlockDelegate.INSTANCE.reset();
            ColorManager.INSTANCE.reset();
        }
        ColorManager.INSTANCE.ensureCurrent(this.forceReset);
        if (this.announce) {
            ChatLog.announceI18N("jm.common.colorreset_complete", new Object[0]);
        }
        if (this.forceReset) {
            MapPlayerTask.forceNearbyRemap();
        }
        return null;
    }
    
    @Override
    public String getName() {
        return "EnsureCurrentColorsTask";
    }
}
