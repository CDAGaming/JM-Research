package journeymap.client.model;

import journeymap.common.api.feature.*;
import journeymap.client.data.*;
import journeymap.client.feature.*;
import java.util.*;

public class MapView
{
    public static final MapView NONE;
    public final Integer vSlice;
    public final int dimension;
    public final Feature.MapType mapType;
    private final int theHashCode;
    private final String theCacheKey;
    
    private MapView() {
        this.vSlice = null;
        this.dimension = 0;
        this.mapType = null;
        this.theCacheKey = "NONE";
        this.theHashCode = 0;
    }
    
    public MapView(final Feature.MapType mapType, Integer vSlice, final int dimension) {
        vSlice = ((mapType != Feature.MapType.Underground) ? null : vSlice);
        this.vSlice = vSlice;
        this.dimension = dimension;
        this.mapType = mapType;
        this.theCacheKey = toCacheKey(mapType, vSlice, dimension);
        this.theHashCode = this.theCacheKey.hashCode();
    }
    
    public static MapView from(final Feature.MapType mapType, final Integer vSlice, final int dimension) {
        return DataCache.INSTANCE.getMapView(mapType, vSlice, dimension);
    }
    
    public static MapView from(final Integer vSlice, final int dimension) {
        return from((vSlice == null) ? Feature.MapType.Day : Feature.MapType.Underground, vSlice, dimension);
    }
    
    public static MapView from(final Feature.MapType mapType, final EntityDTO player) {
        return from(mapType, player.chunkCoordY, player.dimension);
    }
    
    public static MapView day(final int dimension) {
        return from(Feature.MapType.Day, null, dimension);
    }
    
    public static MapView day(final EntityDTO player) {
        return from(Feature.MapType.Day, null, player.dimension);
    }
    
    public static MapView night(final int dimension) {
        return from(Feature.MapType.Night, null, dimension);
    }
    
    public static MapView night(final EntityDTO player) {
        return from(Feature.MapType.Night, null, player.dimension);
    }
    
    public static MapView topo(final int dimension) {
        return from(Feature.MapType.Topo, null, dimension);
    }
    
    public static MapView topo(final EntityDTO player) {
        return from(Feature.MapType.Topo, null, player.dimension);
    }
    
    public static MapView underground(final EntityDTO player) {
        return from(Feature.MapType.Underground, player.chunkCoordY, player.dimension);
    }
    
    public static MapView underground(final Integer vSlice, final int dimension) {
        return from(Feature.MapType.Underground, vSlice, dimension);
    }
    
    public static MapView none() {
        return MapView.NONE;
    }
    
    public static String toCacheKey(final Feature.MapType mapType, final Integer vSlice, final int dimension) {
        final String mapTypeName = (mapType == null) ? "NONE" : mapType.name().toLowerCase();
        return String.format("%s|%s|%s", dimension, mapTypeName, (vSlice == null) ? "_" : vSlice);
    }
    
    public String toCacheKey() {
        return this.theCacheKey;
    }
    
    @Override
    public String toString() {
        return this.theCacheKey;
    }
    
    public String name() {
        return this.mapType.name().toLowerCase();
    }
    
    public boolean isUnderground() {
        return this.mapType == Feature.MapType.Underground;
    }
    
    public boolean isSurface() {
        return this.isDayOrNight();
    }
    
    public boolean isDay() {
        return this.mapType == Feature.MapType.Day;
    }
    
    public boolean isNight() {
        return this.mapType == Feature.MapType.Night;
    }
    
    public boolean isTopo() {
        return this.mapType == Feature.MapType.Topo;
    }
    
    public boolean isBiome() {
        return this.mapType == Feature.MapType.Biome;
    }
    
    public boolean isDayOrNight() {
        return this.isDay() || this.isNight();
    }
    
    public boolean isNone() {
        return this == MapView.NONE;
    }
    
    public boolean isAllowed() {
        return this.isNone() || ClientFeatures.instance().isAllowed(this.mapType, this.dimension);
    }
    
    @Override
    public int hashCode() {
        return this.theHashCode;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MapView otherView = (MapView)o;
        return this.dimension == otherView.dimension && this.mapType == otherView.mapType && Objects.equals(this.vSlice, otherView.vSlice);
    }
    
    static {
        NONE = new MapView();
    }
}
