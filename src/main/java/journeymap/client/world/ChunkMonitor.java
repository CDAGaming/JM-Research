package journeymap.client.world;

import com.google.common.cache.CacheLoader;
import journeymap.client.data.DataCache;
import journeymap.client.forge.event.EventHandlerManager;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public enum ChunkMonitor implements IWorldEventListener, EventHandlerManager.EventHandler {
    INSTANCE;

    private World world;

    public void reset() {
        if (this.world != null) {
            this.world.removeEventListener(ChunkMonitor.INSTANCE);
        }
        this.world = null;
    }

    public void resetRenderTimes(final ChunkPos pos) {
        final ChunkMD chunkMD = DataCache.INSTANCE.getChunkMD(pos);
        if (chunkMD != null) {
            chunkMD.resetRenderTimes();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkLoad(final ChunkEvent.Load event) {
        if (this.world == null) {
            (this.world = event.getWorld()).addEventListener(this);
            event.getWorld();
        }
        final Chunk chunk = event.getChunk();
        if (chunk != null && chunk.isLoaded()) {
            DataCache.INSTANCE.addChunkMD(new ChunkMD(chunk));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkUnload(final ChunkEvent.Unload event) {
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        try {
            final World world = event.getWorld();
            if (world == world) {
                this.reset();
            }
        } catch (Exception e) {
            Journeymap.getLogger().error("Error handling WorldEvent.Unload", (Throwable) e);
        }
    }

    public void notifyBlockUpdate(final World worldIn, final BlockPos pos, final IBlockState oldState, final IBlockState newState, final int flags) {
        this.resetRenderTimes(new ChunkPos(pos));
    }

    public void notifyLightSet(final BlockPos pos) {
        this.resetRenderTimes(new ChunkPos(pos));
    }

    public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        final int cx1 = x1 >> 4;
        final int cz1 = z1 >> 4;
        final int cx2 = x2 >> 4;
        final int cz2 = z2 >> 4;
        if (cx1 == cx2 && cz1 == cz2) {
            this.resetRenderTimes(new ChunkPos(cx1, cz1));
        } else {
            for (int chunkXPos = cx1; chunkXPos < cx2; ++chunkXPos) {
                for (int chunkZPos = cz1; chunkZPos < cz2; ++chunkZPos) {
                    this.resetRenderTimes(new ChunkPos(chunkXPos, chunkZPos));
                }
            }
        }
    }

    public void playSoundToAllNearExcept(@Nullable final EntityPlayer player, final SoundEvent soundIn, final SoundCategory category, final double x, final double y, final double z, final float volume, final float pitch) {
    }

    public void playRecord(final SoundEvent soundIn, final BlockPos pos) {
    }

    public void spawnParticle(final int particleID, final boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, final double xSpeed, final double ySpeed, final double zSpeed, final int... parameters) {
    }

    public void spawnParticle(final int id, final boolean ignoreRange, final boolean p_190570_3_, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed, final int... parameters) {
    }

    public void onEntityAdded(final Entity entityIn) {
    }

    public void onEntityRemoved(final Entity entityIn) {
    }

    public void broadcastSound(final int soundID, final BlockPos pos, final int data) {
    }

    public void playEvent(final EntityPlayer player, final int type, final BlockPos blockPosIn, final int data) {
    }

    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress) {
    }

    private static class TimestampLoader extends CacheLoader<ChunkPos, Long> {
        public Long load(final ChunkPos key) throws Exception {
            return System.currentTimeMillis();
        }
    }
}
