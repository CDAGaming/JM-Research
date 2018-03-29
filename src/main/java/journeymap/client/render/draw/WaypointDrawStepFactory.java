package journeymap.client.render.draw;

import journeymap.client.data.DataCache;
import journeymap.client.model.Waypoint;
import journeymap.client.render.map.GridRenderer;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WaypointDrawStepFactory {
    final List<DrawWayPointStep> drawStepList;

    public WaypointDrawStepFactory() {
        this.drawStepList = new ArrayList<>();
    }

    public List<DrawWayPointStep> prepareSteps(final Collection<Waypoint> waypoints, final GridRenderer grid, boolean checkDistance, final boolean showLabel) {
        final Minecraft mc = FMLClientHandler.instance().getClient();
        final EntityPlayer player = mc.player;
        final int dimension = player.dimension;
        final int maxDistance = Journeymap.getClient().getWaypointProperties().maxDistance.get();
        checkDistance = (checkDistance && maxDistance > 0);
        final Vec3d playerVec = checkDistance ? player.getPositionVector() : null;
        this.drawStepList.clear();
        try {
            for (final Waypoint waypoint : waypoints) {
                if (waypoint.isEnable() && waypoint.isInPlayerDimension()) {
                    if (checkDistance) {
                        final double actualDistance = playerVec.distanceTo(waypoint.getPosition());
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
        } catch (Throwable t) {
            Journeymap.getLogger().error("Error during prepareSteps: " + LogFormatter.toString(t));
        }
        return this.drawStepList;
    }
}
