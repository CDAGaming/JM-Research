package journeymap.client.render.map;

import org.apache.logging.log4j.*;
import java.io.*;
import journeymap.common.*;
import java.util.concurrent.*;
import com.google.common.cache.*;
import journeymap.client.model.*;

public class TileDrawStepCache
{
    private final Logger logger;
    private final Cache<String, TileDrawStep> drawStepCache;
    private File worldDir;
    private MapView mapView;
    
    private TileDrawStepCache() {
        this.logger = Journeymap.getLogger();
        this.drawStepCache = (Cache<String, TileDrawStep>)CacheBuilder.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).removalListener((RemovalListener)new RemovalListener<String, TileDrawStep>() {
            public void onRemoval(final RemovalNotification<String, TileDrawStep> notification) {
                final TileDrawStep oldDrawStep = (TileDrawStep)notification.getValue();
                if (oldDrawStep != null) {
                    oldDrawStep.clearTexture();
                }
            }
        }).build();
    }
    
    public static Cache<String, TileDrawStep> instance() {
        return Holder.INSTANCE.drawStepCache;
    }
    
    public static TileDrawStep getOrCreate(final MapView mapView, final RegionCoord regionCoord, final Integer zoom, final boolean highQuality, final int sx1, final int sy1, final int sx2, final int sy2) {
        return Holder.INSTANCE._getOrCreate(mapView, regionCoord, zoom, highQuality, sx1, sy1, sx2, sy2);
    }
    
    public static void clear() {
        instance().invalidateAll();
    }
    
    public static void setContext(final File worldDir, final MapView mapView) {
        if (!worldDir.equals(Holder.INSTANCE.worldDir)) {
            instance().invalidateAll();
        }
        Holder.INSTANCE.worldDir = worldDir;
        Holder.INSTANCE.mapView = mapView;
    }
    
    public static long size() {
        return instance().size();
    }
    
    private TileDrawStep _getOrCreate(final MapView mapView, final RegionCoord regionCoord, final Integer zoom, final boolean highQuality, final int sx1, final int sy1, final int sx2, final int sy2) {
        this.checkWorldChange(regionCoord);
        final String key = TileDrawStep.toCacheKey(regionCoord, mapView, zoom, highQuality, sx1, sy1, sx2, sy2);
        TileDrawStep tileDrawStep = (TileDrawStep)this.drawStepCache.getIfPresent((Object)key);
        if (tileDrawStep == null) {
            tileDrawStep = new TileDrawStep(regionCoord, mapView, zoom, highQuality, sx1, sy1, sx2, sy2);
            this.drawStepCache.put((Object)key, (Object)tileDrawStep);
        }
        return tileDrawStep;
    }
    
    private void checkWorldChange(final RegionCoord regionCoord) {
        if (!regionCoord.worldDir.equals(this.worldDir)) {
            this.drawStepCache.invalidateAll();
            RegionImageCache.INSTANCE.clear();
        }
    }
    
    private static class Holder
    {
        private static final TileDrawStepCache INSTANCE;
        
        static {
            INSTANCE = new TileDrawStepCache(null);
        }
    }
}
