package journeymap.client.io.nbt;

import org.apache.logging.log4j.*;
import net.minecraft.client.*;
import journeymap.common.*;
import java.io.*;
import journeymap.client.model.*;
import net.minecraft.util.datafix.*;
import net.minecraft.world.chunk.storage.*;
import journeymap.client.io.*;
import net.minecraft.util.math.*;
import java.util.*;
import net.minecraft.world.*;
import java.util.regex.*;
import net.minecraft.client.entity.*;

public class RegionLoader
{
    private static final Pattern anvilPattern;
    final Logger logger;
    final MapView mapView;
    final Stack<RegionCoord> regions;
    final int regionsFound;
    
    public RegionLoader(final Minecraft minecraft, final MapView mapView, final boolean all) throws IOException {
        this.logger = Journeymap.getLogger();
        this.mapView = mapView;
        this.regions = this.findRegions(minecraft, mapView, all);
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
        return this.mapView.isUnderground();
    }
    
    public Integer getVSlice() {
        return this.mapView.vSlice;
    }
    
    Stack<RegionCoord> findRegions(final Minecraft mc, final MapView mapView, final boolean all) {
        final File mcWorldDir = FileHandler.getMCWorldDir(mc, mapView.dimension);
        final File regionDir = new File(mcWorldDir, "region");
        if (!regionDir.exists() && !regionDir.mkdirs()) {
            this.logger.warn("MC world region directory isn't usable: " + regionDir);
            return new Stack<RegionCoord>();
        }
        RegionImageCache.INSTANCE.flushToDisk(false);
        RegionImageCache.INSTANCE.clear();
        final File jmImageWorldDir = FileHandler.getJMWorldDir(mc);
        final Stack<RegionCoord> stack = new Stack<RegionCoord>();
        final AnvilChunkLoader anvilChunkLoader = new AnvilChunkLoader(FileHandler.getWorldSaveDir(mc), DataFixesManager.func_188279_a());
        int validFileCount = 0;
        int existingImageCount = 0;
        final File[] anvilFiles = regionDir.listFiles();
        final World world = Journeymap.clientWorld();
        for (final File anvilFile : anvilFiles) {
            final Matcher matcher = RegionLoader.anvilPattern.matcher(anvilFile.getName());
            if (!anvilFile.isDirectory() && matcher.matches()) {
                ++validFileCount;
                final String x = matcher.group(1);
                final String z = matcher.group(2);
                if (x != null && z != null) {
                    final RegionCoord rc = new RegionCoord(jmImageWorldDir, Integer.parseInt(x), Integer.parseInt(z), mapView.dimension);
                    if (all) {
                        stack.add(rc);
                    }
                    else if (!RegionImageHandler.getRegionImageFile(rc, mapView, false).exists()) {
                        final List<ChunkPos> chunkCoords = rc.getChunkCoordsInRegion();
                        for (final ChunkPos coord : chunkCoords) {
                            if (anvilChunkLoader.chunkExists(world, coord.field_77276_a, coord.field_77275_b)) {
                                stack.add(rc);
                                break;
                            }
                        }
                    }
                    else {
                        ++existingImageCount;
                    }
                }
            }
        }
        if (stack.isEmpty() && validFileCount != existingImageCount) {
            this.logger.warn("Anvil region files in " + regionDir + ": " + validFileCount + ", matching image files: " + existingImageCount + ", but found nothing to do for mapView " + mapView);
        }
        final EntityPlayerSP player = Journeymap.clientPlayer();
        final RegionCoord playerRc = RegionCoord.fromChunkPos(jmImageWorldDir, mapView, player.field_70176_ah, player.field_70164_aj);
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
    
    public MapView getMapType() {
        return this.mapView;
    }
    
    static {
        anvilPattern = Pattern.compile("r\\.([^\\.]+)\\.([^\\.]+)\\.mca");
    }
}
