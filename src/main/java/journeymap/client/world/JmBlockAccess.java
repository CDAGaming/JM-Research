package journeymap.client.world;

import net.minecraft.tileentity.*;
import net.minecraft.block.state.*;
import journeymap.client.model.*;
import net.minecraft.world.biome.*;
import net.minecraft.init.*;
import net.minecraftforge.fml.client.*;
import net.minecraft.server.*;
import javax.annotation.*;
import net.minecraft.util.*;
import journeymap.common.*;
import net.minecraft.world.*;
import journeymap.client.data.*;
import net.minecraft.util.math.*;

@ParametersAreNonnullByDefault
public enum JmBlockAccess implements IBlockAccess
{
    INSTANCE;
    
    public TileEntity func_175625_s(final BlockPos pos) {
        return this.getWorld().func_175625_s(pos);
    }
    
    public int func_175626_b(final BlockPos pos, final int min) {
        return this.getWorld().func_175626_b(pos, min);
    }
    
    public IBlockState func_180495_p(final BlockPos pos) {
        if (!this.isValid(pos)) {
            return Blocks.field_150350_a.func_176223_P();
        }
        final ChunkMD chunkMD = this.getChunkMDFromBlockCoords(pos);
        if (chunkMD != null && chunkMD.hasChunk()) {
            return chunkMD.getChunk().func_186032_a(pos.func_177958_n() & 0xF, pos.func_177956_o(), pos.func_177952_p() & 0xF);
        }
        return Blocks.field_150350_a.func_176223_P();
    }
    
    public boolean func_175623_d(final BlockPos pos) {
        return this.getWorld().func_175623_d(pos);
    }
    
    public Biome func_180494_b(final BlockPos pos) {
        return this.getBiome(pos, Biomes.field_76772_c);
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
        if (FMLClientHandler.instance().getClient().func_71356_B()) {
            final MinecraftServer server = (MinecraftServer)FMLClientHandler.instance().getClient().func_71401_C();
            if (server != null) {
                return server.func_130014_f_().func_72959_q().func_180631_a(pos);
            }
        }
        return defaultBiome;
    }
    
    public int func_175627_a(final BlockPos pos, final EnumFacing direction) {
        return this.getWorld().func_175627_a(pos, direction);
    }
    
    public World getWorld() {
        return Journeymap.clientWorld();
    }
    
    public WorldType func_175624_G() {
        return this.getWorld().func_175624_G();
    }
    
    public boolean isSideSolid(final BlockPos pos, final EnumFacing side, final boolean _default) {
        return this.getWorld().isSideSolid(pos, side, _default);
    }
    
    private boolean isValid(final BlockPos pos) {
        return pos.func_177958_n() >= -30000000 && pos.func_177952_p() >= -30000000 && pos.func_177958_n() < 30000000 && pos.func_177952_p() < 30000000 && pos.func_177956_o() >= 0 && pos.func_177956_o() < 256;
    }
    
    @Nullable
    private ChunkMD getChunkMDFromBlockCoords(final BlockPos pos) {
        return DataCache.INSTANCE.getChunkMD(new ChunkPos(pos));
    }
}
