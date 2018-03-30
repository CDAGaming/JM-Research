package journeymap.client.model;

import java.util.concurrent.*;
import journeymap.common.*;
import javax.annotation.*;
import com.google.common.cache.*;
import net.minecraftforge.fml.client.*;
import net.minecraft.client.*;
import net.minecraft.world.chunk.*;
import journeymap.client.data.*;
import org.apache.logging.log4j.*;
import java.util.*;
import journeymap.client.io.*;
import java.io.*;

public enum RegionImageCache
{
    INSTANCE;
    
    public long firstFileFlushIntervalSecs;
    public long flushFileIntervalSecs;
    public long textureCacheAgeSecs;
    static final Logger logger;
    private volatile long lastFlush;
    
    private RegionImageCache() {
        this.firstFileFlushIntervalSecs = 5L;
        this.flushFileIntervalSecs = 60L;
        this.textureCacheAgeSecs = 30L;
        this.lastFlush = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.firstFileFlushIntervalSecs);
    }
    
    public LoadingCache<RegionImageSet.Key, RegionImageSet> initRegionImageSetsCache(final CacheBuilder<Object, Object> builder) {
        return (LoadingCache<RegionImageSet.Key, RegionImageSet>)builder.expireAfterAccess(this.textureCacheAgeSecs, TimeUnit.SECONDS).removalListener((RemovalListener)new RemovalListener<RegionImageSet.Key, RegionImageSet>() {
            @ParametersAreNonnullByDefault
            public void onRemoval(final RemovalNotification<RegionImageSet.Key, RegionImageSet> notification) {
                final RegionImageSet regionImageSet = (RegionImageSet)notification.getValue();
                if (regionImageSet != null) {
                    final int count = regionImageSet.writeToDisk(false);
                    if (count > 0 && Journeymap.getLogger().isDebugEnabled()) {
                        Journeymap.getLogger().debug("Wrote to disk before removal from cache: " + regionImageSet);
                    }
                    regionImageSet.clear();
                }
            }
        }).build((CacheLoader)new CacheLoader<RegionImageSet.Key, RegionImageSet>() {
            @ParametersAreNonnullByDefault
            public RegionImageSet load(final RegionImageSet.Key key) throws Exception {
                return new RegionImageSet(key);
            }
        });
    }
    
    public RegionImageSet getRegionImageSet(final ChunkMD chunkMd, final MapView mapView) {
        if (chunkMd.hasChunk()) {
            final Minecraft mc = FMLClientHandler.instance().getClient();
            final Chunk chunk = chunkMd.getChunk();
            final RegionCoord rCoord = RegionCoord.fromChunkPos(FileHandler.getJMWorldDir(mc), mapView, chunk.field_76635_g, chunk.field_76647_h);
            return this.getRegionImageSet(rCoord);
        }
        return null;
    }
    
    public RegionImageSet getRegionImageSet(final RegionCoord rCoord) {
        return (RegionImageSet)DataCache.INSTANCE.getRegionImageSets().getUnchecked((Object)RegionImageSet.Key.from(rCoord));
    }
    
    public RegionImageSet getRegionImageSet(final RegionImageSet.Key rCoordKey) {
        return (RegionImageSet)DataCache.INSTANCE.getRegionImageSets().getUnchecked((Object)rCoordKey);
    }
    
    private Collection<RegionImageSet> getRegionImageSets() {
        return (Collection<RegionImageSet>)DataCache.INSTANCE.getRegionImageSets().asMap().values();
    }
    
    public void updateTextures(final boolean forceFlush, final boolean async) {
        for (final RegionImageSet regionImageSet : this.getRegionImageSets()) {
            regionImageSet.finishChunkUpdates();
        }
        if (forceFlush || this.lastFlush + TimeUnit.SECONDS.toMillis(this.flushFileIntervalSecs) < System.currentTimeMillis()) {
            if (!forceFlush && RegionImageCache.logger.isEnabled(Level.DEBUG)) {
                RegionImageCache.logger.debug("RegionImageCache auto-flushing");
            }
            if (async) {
                this.flushToDiskAsync(false);
            }
            else {
                this.flushToDisk(false);
            }
        }
    }
    
    public void flushToDiskAsync(final boolean force) {
        int count = 0;
        for (final RegionImageSet regionImageSet : this.getRegionImageSets()) {
            count += regionImageSet.writeToDiskAsync(force);
        }
        this.lastFlush = System.currentTimeMillis();
    }
    
    public void flushToDisk(final boolean force) {
        for (final RegionImageSet regionImageSet : this.getRegionImageSets()) {
            regionImageSet.writeToDisk(force);
        }
        this.lastFlush = System.currentTimeMillis();
    }
    
    public long getLastFlush() {
        return this.lastFlush;
    }
    
    public List<RegionCoord> getChangedSince(final MapView mapView, final long time) {
        final ArrayList<RegionCoord> list = new ArrayList<RegionCoord>();
        for (final RegionImageSet regionImageSet : this.getRegionImageSets()) {
            if (regionImageSet.updatedSince(mapView, time)) {
                list.add(regionImageSet.getRegionCoord());
            }
        }
        if (RegionImageCache.logger.isEnabled(Level.DEBUG)) {
            RegionImageCache.logger.debug("Dirty regions: " + list.size() + " of " + DataCache.INSTANCE.getRegionImageSets().size());
        }
        return list;
    }
    
    public boolean isDirtySince(final RegionCoord rc, final MapView mapView, final long time) {
        final RegionImageSet ris = this.getRegionImageSet(rc);
        return ris != null && ris.updatedSince(mapView, time);
    }
    
    public void clear() {
        for (final RegionImageSet regionImageSet : this.getRegionImageSets()) {
            regionImageSet.clear();
        }
        DataCache.INSTANCE.getRegionImageSets().invalidateAll();
        DataCache.INSTANCE.getRegionImageSets().cleanUp();
    }
    
    public boolean deleteMap(final MapState state, final boolean allDims) {
        final RegionCoord fakeRc = new RegionCoord(state.getWorldDir(), 0, 0, state.getDimension());
        final File imageDir = RegionImageHandler.getImageDir(fakeRc, MapView.day(state.getDimension())).getParentFile();
        if (!imageDir.getName().startsWith("DIM")) {
            RegionImageCache.logger.error("Expected DIM directory, got " + imageDir);
            return false;
        }
        File[] dirs;
        if (allDims) {
            dirs = imageDir.getParentFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return dir.isDirectory() && name.startsWith("DIM");
                }
            });
        }
        else {
            dirs = new File[] { imageDir };
        }
        if (dirs != null && dirs.length > 0) {
            this.clear();
            boolean result = true;
            for (final File dir : dirs) {
                if (dir.exists()) {
                    FileHandler.delete(dir);
                    RegionImageCache.logger.info(String.format("Deleted image directory %s: %s", dir, !dir.exists()));
                    if (dir.exists()) {
                        result = false;
                    }
                }
            }
            RegionImageCache.logger.info("Done deleting directories");
            return result;
        }
        RegionImageCache.logger.info("Found no DIM directories in " + imageDir);
        return true;
    }
    
    static {
        logger = Journeymap.getLogger();
    }
}
