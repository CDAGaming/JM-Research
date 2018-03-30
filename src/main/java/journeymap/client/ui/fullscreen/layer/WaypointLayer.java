package journeymap.client.ui.fullscreen.layer;

import journeymap.client.api.display.*;
import net.minecraft.client.*;
import journeymap.client.render.map.*;
import java.awt.geom.*;
import journeymap.common.*;
import net.minecraft.util.math.*;
import journeymap.client.waypoint.*;
import journeymap.client.ui.fullscreen.*;
import net.minecraft.world.*;
import net.minecraft.client.entity.*;
import journeymap.client.ui.*;
import journeymap.client.ui.component.*;
import java.util.*;
import org.lwjgl.input.*;
import journeymap.client.render.draw.*;

public class WaypointLayer implements LayerDelegate.Layer
{
    private final long hoverDelay = 100L;
    private final List<DrawStep> drawStepList;
    private final BlockOutlineDrawStep clickDrawStep;
    BlockPos lastCoord;
    long startHover;
    DrawWayPointStep selectedWaypointStep;
    Waypoint selected;
    
    public WaypointLayer() {
        this.lastCoord = null;
        this.startHover = 0L;
        this.selectedWaypointStep = null;
        this.selected = null;
        this.drawStepList = new ArrayList<DrawStep>(1);
        this.clickDrawStep = new BlockOutlineDrawStep(new BlockPos(0, 0, 0));
    }
    
    @Override
    public List<DrawStep> onMouseMove(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final BlockPos blockCoord, final float fontScale, final boolean isScrolling) {
        this.drawStepList.clear();
        if (this.lastCoord == null) {
            this.lastCoord = blockCoord;
        }
        final long now = Minecraft.func_71386_F();
        final int proximity = (int)Math.max(1.0, 8.0 / gridRenderer.getUIState().blockSize);
        if (this.clickDrawStep.blockCoord != null && !blockCoord.equals((Object)this.clickDrawStep.blockCoord)) {
            this.unclick();
        }
        else {
            this.drawStepList.add(this.clickDrawStep);
        }
        final World world = Journeymap.clientWorld();
        final AxisAlignedBB area = new AxisAlignedBB((double)(blockCoord.func_177958_n() - proximity), -1.0, (double)(blockCoord.func_177952_p() - proximity), (double)(blockCoord.func_177958_n() + proximity), (double)(world.func_72940_L() + 1), (double)(blockCoord.func_177952_p() + proximity));
        if (!this.lastCoord.equals((Object)blockCoord)) {
            if (!area.func_72318_a(new Vec3d((double)this.lastCoord.func_177958_n(), 1.0, (double)this.lastCoord.func_177952_p()))) {
                this.selected = null;
                this.lastCoord = blockCoord;
                this.startHover = now;
                return this.drawStepList;
            }
        }
        else if (this.selected != null) {
            this.select(this.selected);
            return this.drawStepList;
        }
        if (now - this.startHover < 100L) {
            return this.drawStepList;
        }
        final EntityPlayerSP player = Journeymap.clientPlayer();
        final int dimension = player.field_71093_bK;
        final Collection<Waypoint> waypoints = WaypointStore.INSTANCE.getAll(dimension);
        final ArrayList<Waypoint> proximal = new ArrayList<Waypoint>();
        final int dim = Fullscreen.state().getDimension();
        for (final Waypoint waypoint : waypoints) {
            if (waypoint.isDisplayed(dim) && area.func_72318_a(waypoint.getVec(dim))) {
                proximal.add(waypoint);
            }
        }
        if (!proximal.isEmpty()) {
            if (proximal.size() > 1) {
                this.sortByDistance(proximal, blockCoord, dimension);
            }
            this.select(proximal.get(0));
        }
        return this.drawStepList;
    }
    
    @Override
    public List<DrawStep> onMouseClick(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final BlockPos blockCoord, final int button, final boolean doubleClick, final float fontScale) {
        if (!this.drawStepList.contains(this.clickDrawStep)) {
            this.drawStepList.add(this.clickDrawStep);
        }
        if (!doubleClick) {
            this.click(gridRenderer, blockCoord);
        }
        else if (this.selected != null) {
            UIManager.INSTANCE.openWaypointManager(this.selected, new Fullscreen());
            return this.drawStepList;
        }
        return this.drawStepList;
    }
    
    @Override
    public boolean propagateClick() {
        return true;
    }
    
    private void sortByDistance(final List<Waypoint> waypoints, final BlockPos blockCoord, final int dimension) {
        Collections.sort(waypoints, new Comparator<Waypoint>() {
            @Override
            public int compare(final Waypoint o1, final Waypoint o2) {
                return Double.compare(this.getDistance(o1), this.getDistance(o2));
            }
            
            private double getDistance(final Waypoint waypoint) {
                final BlockPos pos = waypoint.getPosition(dimension);
                final double dx = pos.func_177958_n() - blockCoord.func_177958_n();
                final double dz = pos.func_177952_p() - blockCoord.func_177952_p();
                return Math.sqrt(dx * dx + dz * dz);
            }
        });
    }
    
    private void select(final Waypoint waypoint) {
        this.selected = waypoint;
        this.selectedWaypointStep = new DrawWayPointStep(waypoint, true);
        this.drawStepList.add(this.selectedWaypointStep);
    }
    
    private void click(final GridRenderer gridRenderer, final BlockPos blockCoord) {
        final BlockOutlineDrawStep clickDrawStep = this.clickDrawStep;
        this.lastCoord = blockCoord;
        clickDrawStep.blockCoord = blockCoord;
        this.clickDrawStep.pixel = gridRenderer.getBlockPixelInGrid(blockCoord);
        if (!this.drawStepList.contains(this.clickDrawStep)) {
            this.drawStepList.add(this.clickDrawStep);
        }
    }
    
    private void unclick() {
        this.clickDrawStep.blockCoord = null;
        this.drawStepList.remove(this.clickDrawStep);
    }
    
    class BlockOutlineDrawStep implements DrawStep
    {
        BlockPos blockCoord;
        Point2D.Double pixel;
        
        BlockOutlineDrawStep(final BlockPos blockCoord) {
            this.blockCoord = blockCoord;
        }
        
        @Override
        public void draw(final Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
            if (pass != Pass.Object) {
                return;
            }
            if (this.blockCoord == null) {
                return;
            }
            if (Mouse.isButtonDown(0)) {
                return;
            }
            if (xOffset != 0.0 || yOffset != 0.0) {
                return;
            }
            final double size = gridRenderer.getUIState().blockSize;
            final double thick = (gridRenderer.getZoom() < 2) ? 1.0 : 2.0;
            final double x = this.pixel.x + xOffset;
            final double y = this.pixel.y + yOffset;
            if (gridRenderer.isOnScreen(this.pixel)) {
                DrawUtil.drawRectangle(x - thick * thick, y - thick * thick, size + thick * 4.0, thick, 0, 0.6f);
                DrawUtil.drawRectangle(x - thick, y - thick, size + thick * thick, thick, 16777215, 0.6f);
                DrawUtil.drawRectangle(x - thick * thick, y - thick, thick, size + thick * thick, 0, 0.6f);
                DrawUtil.drawRectangle(x - thick, y, thick, size, 16777215, 0.6f);
                DrawUtil.drawRectangle(x + size, y, thick, size, 16777215, 0.6f);
                DrawUtil.drawRectangle(x + size + thick, y - thick, thick, size + thick * thick, 0, 0.6f);
                DrawUtil.drawRectangle(x - thick, y + size, size + thick * thick, thick, 16777215, 0.6f);
                DrawUtil.drawRectangle(x - thick * thick, y + size + thick, size + thick * 4.0, thick, 0, 0.6f);
            }
        }
        
        @Override
        public int getDisplayOrder() {
            return 0;
        }
        
        @Override
        public String getModId() {
            return "journeymap";
        }
    }
}
