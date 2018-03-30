package journeymap.client.model;

import java.io.*;
import java.nio.file.*;
import journeymap.client.data.*;
import com.google.common.cache.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.io.nbt.*;
import net.minecraft.util.math.*;
import java.util.*;

public class RegionCoord implements Comparable<RegionCoord>
{
    public static final transient int SIZE = 5;
    private static final transient int chunkSqRt;
    public final File worldDir;
    public final Path dimDir;
    public final int regionX;
    public final int regionZ;
    public final int dimension;
    private final int theHashCode;
    private final String theCacheKey;
    
    public RegionCoord(final File worldDir, final int regionX, final int regionZ, final int dimension) {
        this.worldDir = worldDir;
        this.dimDir = getDimPath(worldDir, dimension);
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.dimension = dimension;
        this.theCacheKey = toCacheKey(this.dimDir, regionX, regionZ);
        this.theHashCode = this.theCacheKey.hashCode();
    }
    
    public static RegionCoord fromChunkPos(final File worldDir, final MapView mapView, final int chunkX, final int chunkZ) {
        final int regionX = getRegionPos(chunkX);
        final int regionZ = getRegionPos(chunkZ);
        return fromRegionPos(worldDir, regionX, regionZ, mapView.dimension);
    }
    
    public static RegionCoord fromRegionPos(final File worldDir, final int regionX, final int regionZ, final int dimension) {
        final Cache<String, RegionCoord> cache = DataCache.INSTANCE.getRegionCoords();
        RegionCoord regionCoord = (RegionCoord)cache.getIfPresent((Object)toCacheKey(getDimPath(worldDir, dimension), regionX, regionZ));
        if (regionCoord == null || regionX != regionCoord.regionX || regionZ != regionCoord.regionZ || dimension != regionCoord.dimension) {
            regionCoord = new RegionCoord(worldDir, regionX, regionZ, dimension);
            cache.put((Object)regionCoord.theCacheKey, (Object)regionCoord);
        }
        return regionCoord;
    }
    
    public static Path getDimPath(final File worldDir, final int dimension) {
        return new File(worldDir, "DIM" + dimension).toPath();
    }
    
    public static int getMinChunkX(final int rX) {
        return rX << 5;
    }
    
    public static int getMaxChunkX(final int rX) {
        return getMinChunkX(rX) + (int)Math.pow(2.0, 5.0) - 1;
    }
    
    public static int getMinChunkZ(final int rZ) {
        return rZ << 5;
    }
    
    public static int getMaxChunkZ(final int rZ) {
        return getMinChunkZ(rZ) + (int)Math.pow(2.0, 5.0) - 1;
    }
    
    public static int getRegionPos(final int ChunkPos) {
        return ChunkPos >> 5;
    }
    
    public static String toCacheKey(final Path dimDir, final int regionX, final int regionZ) {
        return regionX + dimDir.toString() + regionZ;
    }
    
    public boolean exists() {
        return RegionLoader.getRegionFile(FMLClientHandler.instance().getClient(), this.getMinChunkX(), this.getMinChunkZ()).exists();
    }
    
    public int getXOffset(final int chunkX) {
        if (chunkX >> 5 != this.regionX) {
            throw new IllegalArgumentException("chunkX " + chunkX + " out of bounds for regionX " + this.regionX);
        }
        int offset = chunkX % RegionCoord.chunkSqRt * 16;
        if (offset < 0) {
            offset += RegionCoord.chunkSqRt * 16;
        }
        return offset;
    }
    
    public int getZOffset(final int chunkZ) {
        if (getRegionPos(chunkZ) != this.regionZ) {
            throw new IllegalArgumentException("chunkZ " + chunkZ + " out of bounds for regionZ " + this.regionZ);
        }
        int offset = chunkZ % RegionCoord.chunkSqRt * 16;
        if (offset < 0) {
            offset += RegionCoord.chunkSqRt * 16;
        }
        return offset;
    }
    
    public int getMinChunkX() {
        return getMinChunkX(this.regionX);
    }
    
    public int getMaxChunkX() {
        return getMaxChunkX(this.regionX);
    }
    
    public int getMinChunkZ() {
        return getMinChunkZ(this.regionZ);
    }
    
    public int getMaxChunkZ() {
        return getMaxChunkZ(this.regionZ);
    }
    
    public ChunkPos getMinChunkCoord() {
        return new ChunkPos(this.getMinChunkX(), this.getMinChunkZ());
    }
    
    public ChunkPos getMaxChunkCoord() {
        return new ChunkPos(this.getMaxChunkX(), this.getMaxChunkZ());
    }
    
    public List<ChunkPos> getChunkCoordsInRegion() {
        final List<ChunkPos> list = new ArrayList<ChunkPos>(1024);
        final ChunkPos min = this.getMinChunkCoord();
        final ChunkPos max = this.getMaxChunkCoord();
        for (int x = min.field_77276_a; x <= max.field_77276_a; ++x) {
            for (int z = min.field_77275_b; z <= max.field_77275_b; ++z) {
                list.add(new ChunkPos(x, z));
            }
        }
        return list;
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RegionCoord [");
        builder.append(this.regionX);
        builder.append(",");
        builder.append(this.regionZ);
        builder.append("]");
        return builder.toString();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final RegionCoord that = (RegionCoord)o;
        return this.dimension == that.dimension && this.regionX == that.regionX && this.regionZ == that.regionZ && this.dimDir.equals(that.dimDir) && this.worldDir.equals(that.worldDir);
    }
    
    public String cacheKey() {
        return this.theCacheKey;
    }
    
    @Override
    public int hashCode() {
        return this.theHashCode;
    }
    
    @Override
    public int compareTo(final RegionCoord o) {
        final int cx = Double.compare(this.regionX, o.regionX);
        return (cx == 0) ? Double.compare(this.regionZ, o.regionZ) : cx;
    }
    
    static {
        chunkSqRt = (int)Math.pow(2.0, 5.0);
    }
}
