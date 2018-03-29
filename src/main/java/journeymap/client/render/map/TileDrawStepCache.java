package journeymap.client.render.map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.model.RegionImageCache;
import journeymap.common.Journeymap;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class TileDrawStepCache {
    private final Logger logger;
    private final Cache<String, TileDrawStep> drawStepCache;
    private File worldDir;
    private MapType mapType;

    private TileDrawStepCache() {
        this.logger = Journeymap.getLogger();
        this.drawStepCache = CacheBuilder.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).removalListener((RemovalListener<String, TileDrawStep>) notification -> {
            final TileDrawStep oldDrawStep = notification.getValue();
            if (oldDrawStep != null) {
                oldDrawStep.clearTexture();
            }
        }).build();
    }

    public static Cache<String, TileDrawStep> instance() {
        return Holder.INSTANCE.drawStepCache;
    }

    public static TileDrawStep getOrCreate(final MapType mapType, final RegionCoord regionCoord, final Integer zoom, final boolean highQuality, final int sx1, final int sy1, final int sx2, final int sy2) {
        return Holder.INSTANCE._getOrCreate(mapType, regionCoord, zoom, highQuality, sx1, sy1, sx2, sy2);
    }

    public static void clear() {
        instance().invalidateAll();
    }

    public static void setContext(final File worldDir, final MapType mapType) {
        if (!worldDir.equals(Holder.INSTANCE.worldDir)) {
            instance().invalidateAll();
        }
        Holder.INSTANCE.worldDir = worldDir;
        Holder.INSTANCE.mapType = mapType;
    }

    public static long size() {
        return instance().size();
    }

    private TileDrawStep _getOrCreate(final MapType mapType, final RegionCoord regionCoord, final Integer zoom, final boolean highQuality, final int sx1, final int sy1, final int sx2, final int sy2) {
        this.checkWorldChange(regionCoord);
        final String key = TileDrawStep.toCacheKey(regionCoord, mapType, zoom, highQuality, sx1, sy1, sx2, sy2);
        TileDrawStep tileDrawStep = this.drawStepCache.getIfPresent(key);
        if (tileDrawStep == null) {
            tileDrawStep = new TileDrawStep(regionCoord, mapType, zoom, highQuality, sx1, sy1, sx2, sy2);
            this.drawStepCache.put(key, tileDrawStep);
        }
        return tileDrawStep;
    }

    private void checkWorldChange(final RegionCoord regionCoord) {
        if (!regionCoord.worldDir.equals(this.worldDir)) {
            this.drawStepCache.invalidateAll();
            RegionImageCache.INSTANCE.clear();
        }
    }

    private static class Holder {
        private static final TileDrawStepCache INSTANCE;

        static {
            INSTANCE = new TileDrawStepCache();
        }
    }
}
