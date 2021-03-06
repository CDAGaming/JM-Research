package journeymap.client.ui.fullscreen.layer;

import journeymap.client.Constants;
import journeymap.client.data.DataCache;
import journeymap.client.io.ThemeLoader;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.option.LocationFormat;
import journeymap.client.ui.theme.Theme;
import journeymap.client.world.JmBlockAccess;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockInfoLayer implements LayerDelegate.Layer {
    private final List<DrawStep> drawStepList;
    private final Fullscreen fullscreen;
    private final Minecraft mc;
    LocationFormat locationFormat;
    LocationFormat.LocationFormatKeys locationFormatKeys;
    BlockPos lastCoord;
    PlayerInfoStep playerInfoStep;
    BlockInfoStep blockInfoStep;
    private boolean isSinglePlayer;

    public BlockInfoLayer(final Fullscreen fullscreen) {
        this.drawStepList = new ArrayList<>(1);
        this.locationFormat = new LocationFormat();
        this.lastCoord = null;
        this.fullscreen = fullscreen;
        this.blockInfoStep = new BlockInfoStep();
        this.playerInfoStep = new PlayerInfoStep();
        this.mc = FMLClientHandler.instance().getClient();
        this.isSinglePlayer = this.mc.isSingleplayer();
    }

    @Override
    public List<DrawStep> onMouseMove(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final BlockPos blockPos, final float fontScale, final boolean isScrolling) {
        final Rectangle2D.Double optionsToolbarRect = this.fullscreen.getOptionsToolbarBounds();
        final Rectangle2D.Double menuToolbarRect = this.fullscreen.getMenuToolbarBounds();
        if (optionsToolbarRect == null || menuToolbarRect == null) {
            return (List<DrawStep>) Collections.EMPTY_LIST;
        }
        if (this.drawStepList.isEmpty()) {
            this.drawStepList.add(this.playerInfoStep);
            this.drawStepList.add(this.blockInfoStep);
        }
        this.playerInfoStep.update(mc.displayWidth / 2, optionsToolbarRect.getMaxY());
        if (!blockPos.equals(this.lastCoord)) {
            final FullMapProperties fullMapProperties = Journeymap.getClient().getFullMapProperties();
            this.locationFormatKeys = this.locationFormat.getFormatKeys(fullMapProperties.locationFormat.get());
            this.lastCoord = blockPos;
            final ChunkMD chunkMD = DataCache.INSTANCE.getChunkMD(blockPos);
            String info;
            if (chunkMD != null && chunkMD.hasChunk()) {
                BlockMD blockMD = chunkMD.getBlockMD(blockPos.up());
                if (blockMD == null || blockMD.isIgnore()) {
                    blockMD = chunkMD.getBlockMD(blockPos.down());
                }
                final Biome biome = JmBlockAccess.INSTANCE.getBiome(blockPos);
                info = this.locationFormatKeys.format(fullMapProperties.locationFormatVerbose.get(), blockPos.getX(), blockPos.getZ(), blockPos.getY(), blockPos.getY() >> 4) + " " + biome.getBiomeName();
                if (!blockMD.isIgnore()) {
                    info = String.format("%s \u25a0 %s", blockMD.getName(), info);
                }
            } else {
                info = Constants.getString("jm.common.location_xz_verbose", blockPos.getX(), blockPos.getZ());
                if (this.isSinglePlayer) {
                    final Biome biome2 = JmBlockAccess.INSTANCE.getBiome(blockPos, null);
                    if (biome2 != null) {
                        info = info + " " + biome2.getBiomeName();
                    }
                }
            }
            this.blockInfoStep.update(info, gridRenderer.getWidth() / 2, menuToolbarRect.getMinY());
        }
        return this.drawStepList;
    }

    @Override
    public List<DrawStep> onMouseClick(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final BlockPos blockCoord, final int button, final boolean doubleClick, final float fontScale) {
        return (List<DrawStep>) Collections.EMPTY_LIST;
    }

    @Override
    public boolean propagateClick() {
        return true;
    }

    class PlayerInfoStep implements DrawStep {
        private Theme.LabelSpec labelSpec;
        private String prefix;
        private double x;
        private double y;

        void update(final double x, final double y) {
            final Theme theme = ThemeLoader.getCurrentTheme();
            this.labelSpec = theme.fullscreen.statusLabel;
            if (this.prefix == null) {
                this.prefix = BlockInfoLayer.this.mc.player.getName() + " \u25a0 ";
            }
            this.x = x;
            this.y = y + theme.container.toolbar.horizontal.margin * BlockInfoLayer.this.fullscreen.getScreenScaleFactor();
        }

        @Override
        public void draw(final Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
            if (pass == Pass.Text) {
                DrawUtil.drawLabel(this.prefix + Fullscreen.state().playerLastPos, this.labelSpec, this.x, this.y, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, fontScale, 0.0);
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

    class BlockInfoStep implements DrawStep {
        private Theme.LabelSpec labelSpec;
        private double x;
        private double y;
        private String text;

        void update(final String text, final double x, final double y) {
            final Theme theme = ThemeLoader.getCurrentTheme();
            this.labelSpec = theme.fullscreen.statusLabel;
            this.text = text;
            this.x = x;
            this.y = y - theme.container.toolbar.horizontal.margin * BlockInfoLayer.this.fullscreen.getScreenScaleFactor();
        }

        @Override
        public void draw(final Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
            if (pass == Pass.Text) {
                DrawUtil.drawLabel(this.text, this.labelSpec, this.x, this.y, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, fontScale, 0.0);
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
