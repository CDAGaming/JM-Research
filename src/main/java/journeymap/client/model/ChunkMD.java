package journeymap.client.model;

import journeymap.client.world.JmBlockAccess;
import journeymap.common.Journeymap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ChunkMD {
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
        this.properties = new HashMap<>();
        this.blockDataArrays = new BlockDataArrays();
        if (chunk == null) {
            throw new IllegalArgumentException("Chunk can't be null");
        }
        this.coord = new ChunkPos(chunk.x, chunk.z);
        this.setProperty("loaded", System.currentTimeMillis());
        this.properties.put("isSlimeChunk", chunk.getRandomWithSeed(987234911L).nextInt(10) == 0);
        this.chunkReference = new WeakReference<>(chunk);
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
        return JmBlockAccess.INSTANCE.getBlockState(blockPos);
    }

    public BlockMD getBlockMD(final BlockPos blockPos) {
        return BlockMD.getBlockMD(this, blockPos);
    }

    @Nullable
    public Biome getBiome(final BlockPos pos) {
        final Chunk chunk = this.getChunk();
        final byte[] blockBiomeArray = chunk.getBiomeArray();
        final int i = pos.getX() & 0xF;
        final int j = pos.getZ() & 0xF;
        int k = blockBiomeArray[j << 4 | i] & 0xFF;
        if (k == 255) {
            final Biome biome = chunk.getWorld().getBiomeProvider().getBiome(pos, null);
            if (biome == null) {
                return null;
            }
            k = Biome.getIdForBiome(biome);
            blockBiomeArray[j << 4 | i] = (byte) (k & 0xFF);
        }
        return Biome.getBiome(k);
    }

    public int getSavedLightValue(final int localX, final int y, final int localZ) {
        try {
            return this.getChunk().getLightFor(EnumSkyBlock.BLOCK, this.getBlockPos(localX, y, localZ));
        } catch (ArrayIndexOutOfBoundsException e) {
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
                } else if (blockMD.isIgnore() || blockMD.hasFlag(BlockFlag.OpenToSky)) {
                    --y;
                } else {
                    if (!chunk.canSeeSky(blockPos)) {
                        break;
                    }
                    --y;
                }
            }
        } catch (Exception e) {
            Journeymap.getLogger().warn(e + " at " + blockPos, (Throwable) e);
        }
        return Math.max(0, y);
    }

    public boolean hasChunk() {
        final Chunk chunk = this.chunkReference.get();
        return chunk != null && !(chunk instanceof EmptyChunk) && chunk.isLoaded();
    }

    public int getHeight(final BlockPos blockPos) {
        return this.getChunk().getHeight(blockPos);
    }

    public int getPrecipitationHeight(final int localX, final int localZ) {
        return this.getChunk().getPrecipitationHeight(this.getBlockPos(localX, 0, localZ)).getY();
    }

    public int getPrecipitationHeight(final BlockPos blockPos) {
        return this.getChunk().getPrecipitationHeight(blockPos).getY();
    }

    public int getLightOpacity(final BlockMD blockMD, final int localX, final int y, final int localZ) {
        final BlockPos pos = this.getBlockPos(localX, y, localZ);
        return blockMD.getBlockState().getBlock().getLightOpacity(blockMD.getBlockState(), JmBlockAccess.INSTANCE, pos);
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
        final ChunkMD other = (ChunkMD) obj;
        return this.getCoord().equals(other.getCoord());
    }

    public Chunk getChunk() {
        final Chunk chunk = this.chunkReference.get();
        if (chunk == null) {
            throw new ChunkMissingException(this.getCoord());
        }
        return chunk;
    }

    public World getWorld() {
        return this.getChunk().getWorld();
    }

    public int getWorldActualHeight() {
        return this.getWorld().getActualHeight() + 1;
    }

    public Boolean hasNoSky() {
        return !this.getWorld().provider.isSurfaceWorld();
    }

    public boolean canBlockSeeTheSky(final int localX, final int y, final int localZ) {
        return this.getChunk().canSeeSky(this.getBlockPos(localX, y, localZ));
    }

    public ChunkPos getCoord() {
        return this.coord;
    }

    public boolean isSlimeChunk() {
        return (boolean) this.getProperty("isSlimeChunk", Boolean.FALSE);
    }

    public long getLoaded() {
        return (long) this.getProperty("loaded", 0L);
    }

    public void resetRenderTimes() {
        this.getRenderTimes().clear();
    }

    public void resetRenderTime(final MapType mapType) {
        this.getRenderTimes().put(mapType, 0L);
    }

    public void resetBlockData(final MapType mapType) {
        this.getBlockData().get(mapType).clear();
    }

    protected HashMap<MapType, Long> getRenderTimes() {
        Serializable obj = this.properties.get("lastRendered");
        if (!(obj instanceof HashMap)) {
            obj = new HashMap<>();
            this.properties.put("lastRendered", obj);
        }
        return (HashMap<MapType, Long>) obj;
    }

    public long getLastRendered(final MapType mapType) {
        return this.getRenderTimes().getOrDefault(mapType, 0L);
    }

    public long setRendered(final MapType mapType) {
        final long now = System.currentTimeMillis();
        this.getRenderTimes().put(mapType, now);
        return now;
    }

    public BlockPos getBlockPos(final int localX, final int y, final int localZ) {
        return new BlockPos(this.toWorldX(localX), y, this.toWorldZ(localZ));
    }

    public int toWorldX(final int localX) {
        return (this.coord.x << 4) + localX;
    }

    public int toWorldZ(final int localZ) {
        return (this.coord.z << 4) + localZ;
    }

    public BlockDataArrays getBlockData() {
        return this.blockDataArrays;
    }

    public BlockDataArrays.DataArray<Integer> getBlockDataInts(final MapType mapType) {
        return this.blockDataArrays.get(mapType).ints();
    }

    public BlockDataArrays.DataArray<Float> getBlockDataFloats(final MapType mapType) {
        return this.blockDataArrays.get(mapType).floats();
    }

    public BlockDataArrays.DataArray<Boolean> getBlockDataBooleans(final MapType mapType) {
        return this.blockDataArrays.get(mapType).booleans();
    }

    @Override
    public String toString() {
        return "ChunkMD{coord=" + this.coord + ", properties=" + this.properties + '}';
    }

    public int getDimension() {
        return this.getWorld().provider.getDimension();
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

    public static class ChunkMissingException extends RuntimeException {
        ChunkMissingException(final ChunkPos coord) {
            super("Chunk missing: " + coord);
        }
    }
}
