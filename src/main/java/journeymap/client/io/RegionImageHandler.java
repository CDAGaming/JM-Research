package journeymap.client.io;

import javax.imageio.*;
import journeymap.common.log.*;
import journeymap.common.*;
import java.io.*;
import net.minecraft.util.math.*;
import journeymap.client.model.*;
import java.util.*;
import journeymap.client.render.map.*;
import journeymap.client.*;
import java.awt.image.*;
import java.awt.*;

public class RegionImageHandler
{
    public static File getImageDir(final RegionCoord rCoord, final MapView mapView) {
        final File dimDir = rCoord.dimDir.toFile();
        File subDir = null;
        if (mapView.isUnderground()) {
            subDir = new File(dimDir, Integer.toString(mapView.vSlice));
        }
        else {
            subDir = new File(dimDir, mapView.name());
        }
        if (!subDir.exists()) {
            subDir.mkdirs();
        }
        return subDir;
    }
    
    @Deprecated
    public static File getDimensionDir(final File worldDir, final int dimension) {
        final File dimDir = new File(worldDir, "DIM" + dimension);
        if (!dimDir.exists()) {
            dimDir.mkdirs();
        }
        return dimDir;
    }
    
    public static File getRegionImageFile(final RegionCoord rCoord, final MapView mapView, final boolean allowLegacy) {
        final StringBuffer sb = new StringBuffer();
        sb.append(rCoord.regionX).append(",").append(rCoord.regionZ).append(".png");
        final File regionFile = new File(getImageDir(rCoord, mapView), sb.toString());
        return regionFile;
    }
    
    public static BufferedImage createBlankImage(final int width, final int height) {
        final BufferedImage img = new BufferedImage(width, height, 2);
        final Graphics2D g = initRenderingHints(img.createGraphics());
        g.setColor(Color.black);
        g.setComposite(AlphaComposite.Clear);
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();
        return img;
    }
    
    public static BufferedImage readRegionImage(final File regionFile, final boolean returnNull) {
        BufferedImage image = null;
        if (regionFile.canRead()) {
            try {
                image = ImageIO.read(regionFile);
            }
            catch (Exception e) {
                final String error = "Region file produced error: " + regionFile + ": " + LogFormatter.toPartialString(e);
                Journeymap.getLogger().error(error);
            }
        }
        return image;
    }
    
    public static BufferedImage getImage(final File file) {
        try {
            return ImageIO.read(file);
        }
        catch (IOException e) {
            final String error = "Could not get image from file: " + file + ": " + e.getMessage();
            Journeymap.getLogger().error(error);
            return null;
        }
    }
    
    public static synchronized BufferedImage getMergedChunks(final File worldDir, final ChunkPos startCoord, final ChunkPos endCoord, final MapView mapView, final Boolean useCache, BufferedImage image, final Integer imageWidth, final Integer imageHeight, final boolean allowNullImage, final boolean showGrid) {
        int scale = 1;
        scale = Math.max(scale, 1);
        final int initialWidth = Math.min(512, (endCoord.field_77276_a - startCoord.field_77276_a + 1) * 16 / scale);
        final int initialHeight = Math.min(512, (endCoord.field_77275_b - startCoord.field_77275_b + 1) * 16 / scale);
        final BufferedImage blank = null;
        image = createBlankImage(initialWidth, initialHeight);
        final Graphics2D g2D = image.createGraphics();
        final RegionImageCache cache = RegionImageCache.INSTANCE;
        RegionCoord rc = null;
        BufferedImage regionImage = null;
        final int rx1 = RegionCoord.getRegionPos(startCoord.field_77276_a);
        final int rx2 = RegionCoord.getRegionPos(endCoord.field_77276_a);
        final int rz1 = RegionCoord.getRegionPos(startCoord.field_77275_b);
        final int rz2 = RegionCoord.getRegionPos(endCoord.field_77275_b);
        boolean imageDrawn = false;
        for (int rx3 = rx1; rx3 <= rx2; ++rx3) {
            for (int rz3 = rz1; rz3 <= rz2; ++rz3) {
                rc = new RegionCoord(worldDir, rx3, rz3, mapView.dimension);
                regionImage = cache.getRegionImageSet(rc).getImage(mapView);
                if (regionImage != null) {
                    final int rminCx = Math.max(rc.getMinChunkX(), startCoord.field_77276_a);
                    final int rminCz = Math.max(rc.getMinChunkZ(), startCoord.field_77275_b);
                    final int rmaxCx = Math.min(rc.getMaxChunkX(), endCoord.field_77276_a);
                    final int rmaxCz = Math.min(rc.getMaxChunkZ(), endCoord.field_77275_b);
                    int xoffset = rc.getMinChunkX() * 16;
                    int yoffset = rc.getMinChunkZ() * 16;
                    final int sx1 = rminCx * 16 - xoffset;
                    final int sy1 = rminCz * 16 - yoffset;
                    final int sx2 = sx1 + (rmaxCx - rminCx + 1) * 16;
                    final int sy2 = sy1 + (rmaxCz - rminCz + 1) * 16;
                    xoffset = startCoord.field_77276_a * 16;
                    yoffset = startCoord.field_77275_b * 16;
                    final int dx1 = startCoord.field_77276_a * 16 - xoffset;
                    final int dy1 = startCoord.field_77275_b * 16 - yoffset;
                    final int dx2 = dx1 + (endCoord.field_77276_a - startCoord.field_77276_a + 1) * 16;
                    final int dy2 = dy1 + (endCoord.field_77275_b - startCoord.field_77275_b + 1) * 16;
                    g2D.drawImage(regionImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                    imageDrawn = true;
                }
            }
        }
        if (imageDrawn && showGrid) {
            if (mapView.isDay()) {
                g2D.setColor(Color.black);
                g2D.setComposite(AlphaComposite.getInstance(10, 0.25f));
            }
            else {
                g2D.setColor(Color.gray);
                g2D.setComposite(AlphaComposite.getInstance(10, 0.1f));
            }
            for (int x = 0; x <= initialWidth; x += 16) {
                g2D.drawLine(x, 0, x, initialHeight);
            }
            for (int z = 0; z <= initialHeight; z += 16) {
                g2D.drawLine(0, z, initialWidth, z);
            }
        }
        g2D.dispose();
        if (allowNullImage && !imageDrawn) {
            return null;
        }
        if (imageHeight != null && imageWidth != null && (initialHeight != imageHeight || initialWidth != imageWidth)) {
            final BufferedImage scaledImage = createBlankImage(imageWidth, imageHeight);
            final Graphics2D g = initRenderingHints(scaledImage.createGraphics());
            g.drawImage(image, 0, 0, imageWidth, imageHeight, null);
            g.dispose();
            return scaledImage;
        }
        return image;
    }
    
    public static synchronized BufferedImage getMergedChunks(final File worldDir, final ChunkPos startCoord, final ChunkPos endCoord, final MapView mapView, int scale, final boolean showGrid) {
        scale = Math.max(scale, 1);
        final int initialWidth = Math.min(512, (endCoord.field_77276_a - startCoord.field_77276_a + 1) * 16 / scale);
        final int initialHeight = Math.min(512, (endCoord.field_77275_b - startCoord.field_77275_b + 1) * 16 / scale);
        BufferedImage blank = null;
        final BufferedImage image = createBlankImage(initialWidth, initialHeight);
        final Graphics2D g2D = image.createGraphics();
        final int rx1 = RegionCoord.getRegionPos(startCoord.field_77276_a);
        final int rx2 = RegionCoord.getRegionPos(endCoord.field_77276_a);
        final int rz1 = RegionCoord.getRegionPos(startCoord.field_77275_b);
        final int rz2 = RegionCoord.getRegionPos(endCoord.field_77275_b);
        for (int rx3 = rx1; rx3 <= rx2; ++rx3) {
            for (int rz3 = rz1; rz3 <= rz2; ++rz3) {
                final RegionCoord rc = new RegionCoord(worldDir, rx3, rz3, mapView.dimension);
                BufferedImage regionImage = RegionImageCache.INSTANCE.getRegionImageSet(rc).getImage(mapView);
                if (regionImage == null) {
                    if (blank == null) {
                        blank = createBlankImage(512, 512);
                    }
                    regionImage = blank;
                }
                final int rminCx = Math.max(rc.getMinChunkX(), startCoord.field_77276_a);
                final int rminCz = Math.max(rc.getMinChunkZ(), startCoord.field_77275_b);
                final int rmaxCx = Math.min(rc.getMaxChunkX(), endCoord.field_77276_a);
                final int rmaxCz = Math.min(rc.getMaxChunkZ(), endCoord.field_77275_b);
                final int sx1 = (rminCx - rc.getMinChunkX()) * 16;
                final int sy1 = (rminCz - rc.getMinChunkZ()) * 16;
                final int sx2 = sx1 + (rmaxCx - rminCx + 1) * 16;
                final int sy2 = sy1 + (rmaxCz - rminCz + 1) * 16;
                final int dx1 = (rminCx - startCoord.field_77276_a) * 16;
                final int dy1 = (rminCz - startCoord.field_77275_b) * 16;
                final int dx2 = dx1 + (sx2 - sx1);
                final int dy2 = dy1 + (sy2 - sy1);
                g2D.drawImage(regionImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
            }
        }
        if (showGrid) {
            final GridSpec gridSpec = Journeymap.getClient().getCoreProperties().gridSpecs.getSpec(mapView);
            if (gridSpec != null) {
                final BufferedImage gridImage = gridSpec.getTexture().getImage();
                g2D.setXORMode(new Color(gridSpec.getColor()));
                g2D.setComposite(AlphaComposite.getInstance(10, gridSpec.alpha));
                g2D.drawImage(gridImage, 0, 0, initialWidth, initialHeight, null);
            }
        }
        g2D.dispose();
        if (scale > 1) {
            final int scaledWidth = Math.min(512, initialWidth * scale);
            final int scaledHeight = Math.min(512, initialHeight * scale);
            final BufferedImage scaledImage = createBlankImage(scaledWidth, scaledHeight);
            final Graphics2D g = initRenderingHints(scaledImage.createGraphics());
            g.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
            g.dispose();
            return scaledImage;
        }
        return image;
    }
    
    public static synchronized List<TileDrawStep> getTileDrawSteps(final File worldDir, final ChunkPos startCoord, final ChunkPos endCoord, final MapView mapView, final Integer zoom, final boolean highQuality) {
        final List<TileDrawStep> drawSteps = new ArrayList<TileDrawStep>();
        if (mapView.isNone()) {
            return drawSteps;
        }
        final int rx1 = RegionCoord.getRegionPos(startCoord.field_77276_a);
        final int rx2 = RegionCoord.getRegionPos(endCoord.field_77276_a);
        final int rz1 = RegionCoord.getRegionPos(startCoord.field_77275_b);
        final int rz2 = RegionCoord.getRegionPos(endCoord.field_77275_b);
        for (int rx3 = rx1; rx3 <= rx2; ++rx3) {
            for (int rz3 = rz1; rz3 <= rz2; ++rz3) {
                final RegionCoord rc = new RegionCoord(worldDir, rx3, rz3, mapView.dimension);
                final int rminCx = Math.max(rc.getMinChunkX(), startCoord.field_77276_a);
                final int rminCz = Math.max(rc.getMinChunkZ(), startCoord.field_77275_b);
                final int rmaxCx = Math.min(rc.getMaxChunkX(), endCoord.field_77276_a);
                final int rmaxCz = Math.min(rc.getMaxChunkZ(), endCoord.field_77275_b);
                final int xoffset = rc.getMinChunkX() * 16;
                final int yoffset = rc.getMinChunkZ() * 16;
                final int sx1 = rminCx * 16 - xoffset;
                final int sy1 = rminCz * 16 - yoffset;
                final int sx2 = sx1 + (rmaxCx - rminCx + 1) * 16;
                final int sy2 = sy1 + (rmaxCz - rminCz + 1) * 16;
                drawSteps.add(TileDrawStepCache.getOrCreate(mapView, rc, zoom, highQuality, sx1, sy1, sx2, sy2));
            }
        }
        return drawSteps;
    }
    
    public static File getBlank512x512ImageFile() {
        final File dataDir = new File(FileHandler.MinecraftDirectory, Constants.DATA_DIR);
        final File tmpFile = new File(dataDir, "blank512x512.png");
        if (!tmpFile.canRead()) {
            final BufferedImage image = createBlankImage(512, 512);
            try {
                dataDir.mkdirs();
                ImageIO.write(image, "png", tmpFile);
                tmpFile.setReadOnly();
                tmpFile.deleteOnExit();
            }
            catch (IOException e) {
                Journeymap.getLogger().error("Could not create blank temp file " + tmpFile + ": " + LogFormatter.toString(e));
            }
        }
        return tmpFile;
    }
    
    public static Graphics2D initRenderingHints(final Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }
}
