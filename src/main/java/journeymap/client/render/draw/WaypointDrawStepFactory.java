package journeymap.client.render.draw;

import journeymap.client.api.display.*;
import journeymap.client.render.map.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import journeymap.common.*;
import journeymap.client.data.*;
import journeymap.common.log.*;
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
        this.drawStepList.clear();
        final int dimension = grid.getMapView().dimension;
        if (!ClientFeatures.instance().isAllowed(Feature.Radar.Waypoint, dimension)) {
            return this.drawStepList;
        }
        final EntityPlayer player = (EntityPlayer)Journeymap.clientPlayer();
        final int maxDistance = Journeymap.getClient().getWaypointProperties().maxDistance.get();
        checkDistance = (checkDistance && maxDistance > 0);
        final Vec3d playerVec = checkDistance ? player.func_174791_d() : null;
        try {
            for (final Waypoint waypoint : waypoints) {
                if (waypoint.isDisplayed(dimension)) {
                    if (checkDistance) {
                        final double actualDistance = playerVec.func_72438_d(waypoint.getVec(dimension));
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
