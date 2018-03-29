package journeymap.client.world;

import journeymap.client.data.DataCache;
import journeymap.client.model.ChunkMD;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.FMLClientHandler;

import javax.annotation.Nullable;

@MethodsReturnNonnullByDefault
public enum JmBlockAccess implements IBlockAccess {
    INSTANCE;

    public TileEntity getTileEntity(final BlockPos pos) {
        return this.getWorld().getTileEntity(pos);
    }

    public int getCombinedLight(final BlockPos pos, final int min) {
        return this.getWorld().getCombinedLight(pos, min);
    }

    public IBlockState getBlockState(final BlockPos pos) {
        if (!this.isValid(pos)) {
            return Blocks.AIR.getDefaultState();
        }
        final ChunkMD chunkMD = this.getChunkMDFromBlockCoords(pos);
        if (chunkMD != null && chunkMD.hasChunk()) {
            return chunkMD.getChunk().getBlockState(pos.getX() & 0xF, pos.getY(), pos.getZ() & 0xF);
        }
        return Blocks.AIR.getDefaultState();
    }

    public boolean isAirBlock(final BlockPos pos) {
        return this.getWorld().isAirBlock(pos);
    }

    public Biome getBiome(final BlockPos pos) {
        return this.getBiome(pos, Biomes.PLAINS);
    }

    @Nullable
    public Biome getBiome(final BlockPos pos, final Biome defaultBiome) {
        final ChunkMD chunkMD = this.getChunkMDFromBlockCoords(pos);
        if (chunkMD != null && chunkMD.hasChunk()) {
            final Biome biome = chunkMD.getBiome(pos);
            if (biome != null) {
                return biome;
            }
        }
        if (FMLClientHandler.instance().getClient().isSingleplayer()) {
            final MinecraftServer server = FMLClientHandler.instance().getClient().getIntegratedServer();
            if (server != null) {
                return server.getEntityWorld().getBiomeProvider().getBiome(pos);
            }
        }
        return defaultBiome;
    }

    public int getStrongPower(final BlockPos pos, final EnumFacing direction) {
        return this.getWorld().getStrongPower(pos, direction);
    }

    public World getWorld() {
        return FMLClientHandler.instance().getClient().world;
    }

    public WorldType getWorldType() {
        return this.getWorld().getWorldType();
    }

    public boolean isSideSolid(final BlockPos pos, final EnumFacing side, final boolean _default) {
        return this.getWorld().isSideSolid(pos, side, _default);
    }

    private boolean isValid(final BlockPos pos) {
        return pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000 && pos.getY() >= 0 && pos.getY() < 256;
    }

    @Nullable
    private ChunkMD getChunkMDFromBlockCoords(final BlockPos pos) {
        return DataCache.INSTANCE.getChunkMD(new ChunkPos(pos));
    }
}
