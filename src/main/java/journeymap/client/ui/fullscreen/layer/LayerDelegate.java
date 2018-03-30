package journeymap.client.ui.fullscreen.layer;

import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.cartography.render.BaseRenderer;
import journeymap.client.data.DataCache;
import journeymap.client.io.FileHandler;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.RegionCoord;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LayerDelegate {
    long lastClick;
    BlockPos lastBlockPos;
    private List<DrawStep> drawSteps;
    private List<Layer> layers;

    public LayerDelegate(final Fullscreen fullscreen) {
        this.lastClick = 0L;
        this.lastBlockPos = null;
        this.drawSteps = new ArrayList<>();
        (this.layers = new ArrayList<>()).add(new ModOverlayLayer());
        this.layers.add(new BlockInfoLayer(fullscreen));
        this.layers.add(new WaypointLayer());
        this.layers.add(new KeybindingInfoLayer(fullscreen));
    }

    public void onMouseMove(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final float fontScale, final boolean isScrolling) {
        if (this.lastBlockPos == null || !isScrolling) {
            this.lastBlockPos = this.getBlockPos(mc, gridRenderer, mousePosition);
        }
        this.drawSteps.clear();
        for (final Layer layer : this.layers) {
            try {
                this.drawSteps.addAll(layer.onMouseMove(mc, gridRenderer, mousePosition, this.lastBlockPos, fontScale, isScrolling));
            } catch (Exception e) {
                Journeymap.getLogger().error(LogFormatter.toString(e));
            }
        }
    }

    public void onMouseClicked(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final int button, final float fontScale) {
        this.lastBlockPos = gridRenderer.getBlockAtPixel(mousePosition);
        final long sysTime = Minecraft.getSystemTime();
        final boolean doubleClick = sysTime - this.lastClick < 450L;
        this.lastClick = sysTime;
        this.drawSteps.clear();
        for (final Layer layer : this.layers) {
            try {
                this.drawSteps.addAll(layer.onMouseClick(mc, gridRenderer, mousePosition, this.lastBlockPos, button, doubleClick, fontScale));
                if (!layer.propagateClick()) {
                    break;
                }
            } catch (Exception e) {
                Journeymap.getLogger().error(LogFormatter.toString(e));
            }
        }
    }

    public BlockPos getBlockPos(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition) {
        final BlockPos seaLevel = gridRenderer.getBlockAtPixel(mousePosition);
        final ChunkMD chunkMD = DataCache.INSTANCE.getChunkMD(seaLevel);
        if (chunkMD != null) {
            final ChunkRenderController crc = Journeymap.getClient().getChunkRenderController();
            if (crc != null) {
                final ChunkPos chunkCoord = chunkMD.getCoord();
                final RegionCoord rCoord = RegionCoord.fromChunkPos(FileHandler.getJMWorldDir(mc), gridRenderer.getMapType(), chunkCoord.x, chunkCoord.z);
                final BaseRenderer chunkRenderer = crc.getRenderer(rCoord, gridRenderer.getMapType(), chunkMD);
                final int blockY = chunkRenderer.getBlockHeight(chunkMD, seaLevel);
                return new BlockPos(seaLevel.getX(), blockY, seaLevel.getZ());
            }
        }
        return seaLevel;
    }

    public List<DrawStep> getDrawSteps() {
        return this.drawSteps;
    }

    public interface Layer {
        List<DrawStep> onMouseMove(final Minecraft p0, final GridRenderer p1, final Point2D.Double p2, final BlockPos p3, final float p4, final boolean p5);

        List<DrawStep> onMouseClick(final Minecraft p0, final GridRenderer p1, final Point2D.Double p2, final BlockPos p3, final int p4, final boolean p5, final float p6);

        boolean propagateClick();
    }
}
