package journeymap.client.io.nbt;

import journeymap.client.io.FileHandler;
import journeymap.client.io.RegionImageHandler;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.model.RegionImageCache;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegionLoader {
    private static final Pattern anvilPattern;

    static {
        anvilPattern = Pattern.compile("r\\.([^\\.]+)\\.([^\\.]+)\\.mca");
    }

    final Logger logger;
    final MapType mapType;
    final Stack<RegionCoord> regions;
    final int regionsFound;

    public RegionLoader(final Minecraft minecraft, final MapType mapType, final boolean all) throws IOException {
        this.logger = Journeymap.getLogger();
        this.mapType = mapType;
        this.regions = this.findRegions(minecraft, mapType, all);
        this.regionsFound = this.regions.size();
    }

    public static File getRegionFile(final Minecraft minecraft, final int dimension, final int chunkX, final int chunkZ) {
        final File regionDir = new File(FileHandler.getWorldSaveDir(minecraft), "region");
        final File regionFile = new File(regionDir, String.format("r.%s.%s.mca", chunkX >> 5, chunkZ >> 5));
        return regionFile;
    }

    public static File getRegionFile(final Minecraft minecraft, final int chunkX, final int chunkZ) {
        final File regionDir = new File(FileHandler.getWorldSaveDir(minecraft), "region");
        final File regionFile = new File(regionDir, String.format("r.%s.%s.mca", chunkX >> 5, chunkZ >> 5));
        return regionFile;
    }

    public Iterator<RegionCoord> regionIterator() {
        return this.regions.iterator();
    }

    public Stack<RegionCoord> getRegions() {
        return this.regions;
    }

    public int getRegionsFound() {
        return this.regionsFound;
    }

    public boolean isUnderground() {
        return this.mapType.isUnderground();
    }

    public Integer getVSlice() {
        return this.mapType.vSlice;
    }

    Stack<RegionCoord> findRegions(final Minecraft mc, final MapType mapType, final boolean all) {
        final File mcWorldDir = FileHandler.getMCWorldDir(mc, mapType.dimension);
        final File regionDir = new File(mcWorldDir, "region");
        if (!regionDir.exists() && !regionDir.mkdirs()) {
            this.logger.warn("MC world region directory isn't usable: " + regionDir);
            return new Stack<RegionCoord>();
        }
        RegionImageCache.INSTANCE.flushToDisk(false);
        RegionImageCache.INSTANCE.clear();
        final File jmImageWorldDir = FileHandler.getJMWorldDir(mc);
        final Stack<RegionCoord> stack = new Stack<RegionCoord>();
        final AnvilChunkLoader anvilChunkLoader = new AnvilChunkLoader(FileHandler.getWorldSaveDir(mc), DataFixesManager.createFixer());
        int validFileCount = 0;
        int existingImageCount = 0;
        final File[] listFiles;
        final File[] anvilFiles = listFiles = regionDir.listFiles();
        for (final File anvilFile : listFiles) {
            final Matcher matcher = RegionLoader.anvilPattern.matcher(anvilFile.getName());
            if (!anvilFile.isDirectory() && matcher.matches()) {
                ++validFileCount;
                final String x = matcher.group(1);
                final String z = matcher.group(2);
                if (x != null && z != null) {
                    final RegionCoord rc = new RegionCoord(jmImageWorldDir, Integer.parseInt(x), Integer.parseInt(z), mapType.dimension);
                    if (all) {
                        stack.add(rc);
                    } else if (!RegionImageHandler.getRegionImageFile(rc, mapType, false).exists()) {
                        final List<ChunkPos> chunkCoords = rc.getChunkCoordsInRegion();
                        for (final ChunkPos coord : chunkCoords) {
                            if (anvilChunkLoader.chunkExists((World) mc.world, coord.x, coord.z)) {
                                stack.add(rc);
                                break;
                            }
                        }
                    } else {
                        ++existingImageCount;
                    }
                }
            }
        }
        if (stack.isEmpty() && validFileCount != existingImageCount) {
            this.logger.warn("Anvil region files in " + regionDir + ": " + validFileCount + ", matching image files: " + existingImageCount + ", but found nothing to do for mapType " + mapType);
        }
        final RegionCoord playerRc = RegionCoord.fromChunkPos(jmImageWorldDir, mapType, mc.player.chunkCoordX, mc.player.chunkCoordZ);
        if (stack.contains(playerRc)) {
            stack.remove(playerRc);
        }
        Collections.sort(stack, new Comparator<RegionCoord>() {
            @Override
            public int compare(final RegionCoord o1, final RegionCoord o2) {
                final Float d1 = this.distanceToPlayer(o1);
                final Float d2 = this.distanceToPlayer(o2);
                final int comp = d2.compareTo(d1);
                if (comp == 0) {
                    return o2.compareTo(o1);
                }
                return comp;
            }

            float distanceToPlayer(final RegionCoord rc) {
                final float x = rc.regionX - playerRc.regionX;
                final float z = rc.regionZ - playerRc.regionZ;
                return x * x + z * z;
            }
        });
        stack.add(playerRc);
        return stack;
    }

    public MapType getMapType() {
        return this.mapType;
    }
}
