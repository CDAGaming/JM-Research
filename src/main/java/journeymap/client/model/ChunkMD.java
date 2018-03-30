package journeymap.client.model;

import java.lang.ref.*;
import java.util.*;
import java.io.*;
import net.minecraft.block.state.*;
import journeymap.common.*;
import net.minecraft.util.math.*;
import journeymap.client.world.*;
import net.minecraft.world.biome.*;
import javax.annotation.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.*;

public class ChunkMD
{
    public static final String PROP_IS_SLIME_CHUNK = "isSlimeChunk";
    public static final String PROP_LOADED = "loaded";
    public static final String PROP_LAST_RENDERED = "lastRendered";
    private final WeakReference<Chunk> chunkReference;
    private final ChunkPos coord;
    private final HashMap<String, Serializable> properties;
    private BlockDataArrays blockDataArrays;
    private Chunk retainedChunk;
    
    public ChunkMD(final Chunk chunk) {
        this(chunk, false);
    }
    
    public ChunkMD(final Chunk chunk, final boolean forceRetain) {
        this.properties = new HashMap<String, Serializable>();
        this.blockDataArrays = new BlockDataArrays();
        if (chunk == null) {
            throw new IllegalArgumentException("Chunk can't be null");
        }
        this.coord = new ChunkPos(chunk.field_76635_g, chunk.field_76647_h);
        this.setProperty("loaded", System.currentTimeMillis());
        this.properties.put("isSlimeChunk", chunk.func_76617_a(987234911L).nextInt(10) == 0);
        this.chunkReference = new WeakReference<Chunk>(chunk);
        if (forceRetain) {
            this.retainedChunk = chunk;
        }
    }
    
    public IBlockState getBlockState(final int localX, final int y, final int localZ) {
        if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15) {
            Journeymap.getLogger().warn("Expected local coords, got global coords");
        }
        return this.getBlockState(new BlockPos(this.toWorldX(localX), y, this.toWorldZ(localZ)));
    }
    
    public IBlockState getBlockState(final BlockPos blockPos) {
        return JmBlockAccess.INSTANCE.func_180495_p(blockPos);
    }
    
    public BlockMD getBlockMD(final BlockPos blockPos) {
        return BlockMD.getBlockMD(this, blockPos);
    }
    
    @Nullable
    public Biome getBiome(final BlockPos pos) {
        final Chunk chunk = this.getChunk();
        final byte[] blockBiomeArray = chunk.func_76605_m();
        final int i = pos.func_177958_n() & 0xF;
        final int j = pos.func_177952_p() & 0xF;
        int k = blockBiomeArray[j << 4 | i] & 0xFF;
        if (k == 255) {
            final Biome biome = chunk.func_177412_p().func_72959_q().func_180300_a(pos, (Biome)null);
            if (biome == null) {
                return null;
            }
            k = Biome.func_185362_a(biome);
            blockBiomeArray[j << 4 | i] = (byte)(k & 0xFF);
        }
        return Biome.func_150568_d(k);
    }
    
    public int getSavedLightValue(final int localX, final int y, final int localZ) {
        try {
            return this.getChunk().func_177413_a(EnumSkyBlock.BLOCK, this.getBlockPos(localX, y, localZ));
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return 1;
        }
    }
    
    public final BlockMD getBlockMD(final int localX, final int y, final int localZ) {
        return BlockMD.getBlockMD(this, this.getBlockPos(localX, y, localZ));
    }
    
    public int ceiling(final int localX, final int localZ) {
        int y;
        final int chunkHeight = y = this.getPrecipitationHeight(this.getBlockPos(localX, 0, localZ));
        BlockPos blockPos = null;
        try {
            final Chunk chunk = this.getChunk();
            while (y >= 0) {
                blockPos = this.getBlockPos(localX, y, localZ);
                final BlockMD blockMD = this.getBlockMD(blockPos);
                if (blockMD == null) {
                    --y;
                }
                else if (blockMD.isIgnore() || blockMD.hasFlag(BlockFlag.OpenToSky)) {
                    --y;
                }
                else {
                    if (!chunk.func_177444_d(blockPos)) {
                        break;
                    }
                    --y;
                }
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().warn(e + " at " + blockPos, (Throwable)e);
        }
        return Math.max(0, y);
    }
    
    public boolean hasChunk() {
        final Chunk chunk = this.chunkReference.get();
        final boolean result = chunk != null && !(chunk instanceof EmptyChunk) && chunk.func_177410_o();
        return result;
    }
    
    public int getHeight(final BlockPos blockPos) {
        return this.getChunk().func_177433_f(blockPos);
    }
    
    public int getPrecipitationHeight(final int localX, final int localZ) {
        return this.getChunk().func_177440_h(this.getBlockPos(localX, 0, localZ)).func_177956_o();
    }
    
    public int getPrecipitationHeight(final BlockPos blockPos) {
        return this.getChunk().func_177440_h(blockPos).func_177956_o();
    }
    
    public int getLightOpacity(final BlockMD blockMD, final int localX, final int y, final int localZ) {
        final BlockPos pos = this.getBlockPos(localX, y, localZ);
        return blockMD.getBlockState().func_177230_c().getLightOpacity(blockMD.getBlockState(), (IBlockAccess)JmBlockAccess.INSTANCE, pos);
    }
    
    public Serializable getProperty(final String name) {
        return this.properties.get(name);
    }
    
    public Serializable getProperty(final String name, final Serializable defaultValue) {
        Serializable currentValue = this.getProperty(name);
        if (currentValue == null) {
            this.setProperty(name, defaultValue);
            currentValue = defaultValue;
        }
        return currentValue;
    }
    
    public Serializable setProperty(final String name, final Serializable value) {
        return this.properties.put(name, value);
    }
    
    @Override
    public int hashCode() {
        return this.getCoord().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ChunkMD other = (ChunkMD)obj;
        return this.getCoord().equals((Object)other.getCoord());
    }
    
    public Chunk getChunk() {
        final Chunk chunk = this.chunkReference.get();
        if (chunk == null) {
            throw new ChunkMissingException(this.getCoord());
        }
        return chunk;
    }
    
    public World getWorld() {
        return this.getChunk().func_177412_p();
    }
    
    public int getWorldActualHeight() {
        return this.getWorld().func_72940_L() + 1;
    }
    
    public Boolean hasNoSky() {
        return this.getWorld().field_73011_w.func_177495_o();
    }
    
    public boolean canBlockSeeTheSky(final int localX, final int y, final int localZ) {
        return this.getChunk().func_177444_d(this.getBlockPos(localX, y, localZ));
    }
    
    public ChunkPos getCoord() {
        return this.coord;
    }
    
    public boolean isSlimeChunk() {
        return (boolean)this.getProperty("isSlimeChunk", Boolean.FALSE);
    }
    
    public long getLoaded() {
        return (long)this.getProperty("loaded", 0L);
    }
    
    public void resetRenderTimes() {
        this.getRenderTimes().clear();
    }
    
    public void resetRenderTime(final MapView mapView) {
        this.getRenderTimes().put(mapView, 0L);
    }
    
    public void resetBlockData(final MapView mapView) {
        this.getBlockData().get(mapView).clear();
    }
    
    protected HashMap<MapView, Long> getRenderTimes() {
        Serializable obj = this.properties.get("lastRendered");
        if (!(obj instanceof HashMap)) {
            obj = new HashMap<Object, Object>();
            this.properties.put("lastRendered", obj);
        }
        return (HashMap<MapView, Long>)obj;
    }
    
    public long getLastRendered(final MapView mapView) {
        return this.getRenderTimes().getOrDefault(mapView, 0L);
    }
    
    public long setRendered(final MapView mapView) {
        final long now = System.currentTimeMillis();
        this.getRenderTimes().put(mapView, now);
        return now;
    }
    
    public BlockPos getBlockPos(final int localX, final int y, final int localZ) {
        return new BlockPos(this.toWorldX(localX), y, this.toWorldZ(localZ));
    }
    
    public int toWorldX(final int localX) {
        return (this.coord.field_77276_a << 4) + localX;
    }
    
    public int toWorldZ(final int localZ) {
        return (this.coord.field_77275_b << 4) + localZ;
    }
    
    public BlockDataArrays getBlockData() {
        return this.blockDataArrays;
    }
    
    public BlockDataArrays.DataArray<Integer> getBlockDataInts(final MapView mapView) {
        return this.blockDataArrays.get(mapView).ints();
    }
    
    public BlockDataArrays.DataArray<Float> getBlockDataFloats(final MapView mapView) {
        return this.blockDataArrays.get(mapView).floats();
    }
    
    public BlockDataArrays.DataArray<Boolean> getBlockDataBooleans(final MapView mapView) {
        return this.blockDataArrays.get(mapView).booleans();
    }
    
    @Override
    public String toString() {
        return "ChunkMD{coord=" + this.coord + ", properties=" + this.properties + '}';
    }
    
    public int getDimension() {
        return this.getWorld().field_73011_w.getDimension();
    }
    
    public void stopChunkRetention() {
        this.retainedChunk = null;
    }
    
    public boolean hasRetainedChunk() {
        return this.retainedChunk != null;
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (this.retainedChunk != null) {
            super.finalize();
        }
    }
    
    public static class ChunkMissingException extends RuntimeException
    {
        ChunkMissingException(final ChunkPos coord) {
            super("Chunk missing: " + coord);
        }
    }
}
