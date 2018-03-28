package journeymap.client.model;

import com.google.common.cache.*;
import journeymap.client.data.DataCache;
import journeymap.client.io.FileHandler;
import journeymap.client.io.RegionImageHandler;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public enum RegionImageCache {
    INSTANCE;

    static final Logger logger;

    static {
        logger = Journeymap.getLogger();
    }

    public long firstFileFlushIntervalSecs;
    public long flushFileIntervalSecs;
    public long textureCacheAgeSecs;
    private volatile long lastFlush;

    private RegionImageCache() {
        this.firstFileFlushIntervalSecs = 5L;
        this.flushFileIntervalSecs = 60L;
        this.textureCacheAgeSecs = 30L;
        this.lastFlush = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.firstFileFlushIntervalSecs);
    }

    public LoadingCache<RegionImageSet.Key, RegionImageSet> initRegionImageSetsCache(final CacheBuilder<Object, Object> builder) {
        return (LoadingCache<RegionImageSet.Key, RegionImageSet>) builder.expireAfterAccess(this.textureCacheAgeSecs, TimeUnit.SECONDS).removalListener((RemovalListener) new RemovalListener<RegionImageSet.Key, RegionImageSet>() {
            @ParametersAreNonnullByDefault
            public void onRemoval(final RemovalNotification<RegionImageSet.Key, RegionImageSet> notification) {
                final RegionImageSet regionImageSet = (RegionImageSet) notification.getValue();
                if (regionImageSet != null) {
                    final int count = regionImageSet.writeToDisk(false);
                    if (count > 0 && Journeymap.getLogger().isDebugEnabled()) {
                        Journeymap.getLogger().debug("Wrote to disk before removal from cache: " + regionImageSet);
                    }
                    regionImageSet.clear();
                }
            }
        }).build((CacheLoader) new CacheLoader<RegionImageSet.Key, RegionImageSet>() {
            @ParametersAreNonnullByDefault
            public RegionImageSet load(final RegionImageSet.Key key) throws Exception {
                return new RegionImageSet(key);
            }
        });
    }

    public RegionImageSet getRegionImageSet(final ChunkMD chunkMd, final MapType mapType) {
        if (chunkMd.hasChunk()) {
            final Minecraft mc = FMLClientHandler.instance().getClient();
            final Chunk chunk = chunkMd.getChunk();
            final RegionCoord rCoord = RegionCoord.fromChunkPos(FileHandler.getJMWorldDir(mc), mapType, chunk.x, chunk.z);
            return this.getRegionImageSet(rCoord);
        }
        return null;
    }

    public RegionImageSet getRegionImageSet(final RegionCoord rCoord) {
        return (RegionImageSet) DataCache.INSTANCE.getRegionImageSets().getUnchecked((RegionImageSet.Key.from(rCoord)));
    }

    public RegionImageSet getRegionImageSet(final RegionImageSet.Key rCoordKey) {
        return (RegionImageSet) DataCache.INSTANCE.getRegionImageSets().getUnchecked(rCoordKey);
    }

    private Collection<RegionImageSet> getRegionImageSets() {
        return (Collection<RegionImageSet>) DataCache.INSTANCE.getRegionImageSets().asMap().values();
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
            } else {
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

    public List<RegionCoord> getChangedSince(final MapType mapType, final long time) {
        final ArrayList<RegionCoord> list = new ArrayList<RegionCoord>();
        for (final RegionImageSet regionImageSet : this.getRegionImageSets()) {
            if (regionImageSet.updatedSince(mapType, time)) {
                list.add(regionImageSet.getRegionCoord());
            }
        }
        if (RegionImageCache.logger.isEnabled(Level.DEBUG)) {
            RegionImageCache.logger.debug("Dirty regions: " + list.size() + " of " + DataCache.INSTANCE.getRegionImageSets().size());
        }
        return list;
    }

    public boolean isDirtySince(final RegionCoord rc, final MapType mapType, final long time) {
        final RegionImageSet ris = this.getRegionImageSet(rc);
        return ris != null && ris.updatedSince(mapType, time);
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
        final File imageDir = RegionImageHandler.getImageDir(fakeRc, MapType.day(state.getDimension())).getParentFile();
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
        } else {
            dirs = new File[]{imageDir};
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
}
