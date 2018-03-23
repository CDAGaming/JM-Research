package journeymap.client.render.draw;

import journeymap.client.model.*;
import journeymap.client.render.map.*;
import net.minecraftforge.fml.client.*;
import journeymap.common.*;
import journeymap.client.data.*;
import journeymap.common.log.*;
import net.minecraft.client.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import java.util.*;

public class WaypointDrawStepFactory
{
    final List<DrawWayPointStep> drawStepList;
    
    public WaypointDrawStepFactory() {
        this.drawStepList = new ArrayList<DrawWayPointStep>();
    }
    
    public List<DrawWayPointStep> prepareSteps(final Collection<Waypoint> waypoints, final GridRenderer grid, boolean checkDistance, final boolean showLabel) {
        final Minecraft mc = FMLClientHandler.instance().getClient();
        final EntityPlayer player = (EntityPlayer)mc.field_71439_g;
        final int dimension = player.field_71093_bK;
        final int maxDistance = Journeymap.getClient().getWaypointProperties().maxDistance.get();
        checkDistance = (checkDistance && maxDistance > 0);
        final Vec3d playerVec = checkDistance ? player.func_174791_d() : null;
        this.drawStepList.clear();
        try {
            for (final Waypoint waypoint : waypoints) {
                if (waypoint.isEnable() && waypoint.isInPlayerDimension()) {
                    if (checkDistance) {
                        final double actualDistance = playerVec.func_72438_d(waypoint.getPosition());
                        if (actualDistance > maxDistance) {
                            continue;
                        }
                    }
                    final DrawWayPointStep wayPointStep = DataCache.INSTANCE.getDrawWayPointStep(waypoint);
                    if (wayPointStep == null) {
                        continue;
                    }
                    this.drawStepList.add(wayPointStep);
                    wayPointStep.setShowLabel(showLabel);
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error during prepareSteps: " + LogFormatter.toString(t));
        }
        return this.drawStepList;
    }
}
