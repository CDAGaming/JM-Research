package journeymap.client.world;

import mcp.*;
import net.minecraft.tileentity.*;
import net.minecraft.block.state.*;
import journeymap.client.model.*;
import net.minecraft.world.biome.*;
import net.minecraft.init.*;
import net.minecraftforge.fml.client.*;
import net.minecraft.server.*;
import javax.annotation.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import journeymap.client.data.*;
import net.minecraft.util.math.*;

@MethodsReturnNonnullByDefault
public enum JmBlockAccess implements IBlockAccess
{
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
            final MinecraftServer server = (MinecraftServer)FMLClientHandler.instance().getClient().getIntegratedServer();
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
        return (World)FMLClientHandler.instance().getClient().world;
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
