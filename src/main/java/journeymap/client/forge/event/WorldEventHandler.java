package journeymap.client.forge.event;

import journeymap.client.data.DataCache;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
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

@SideOnly(Side.CLIENT)
public class WorldEventHandler implements IWorldEventListener, EventHandlerManager.EventHandler {
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onWorldLoad(final WorldEvent.Load event) {
        try {
            final World world = event.getWorld();
            if (world != null) {
                world.addEventListener(this);
            }
        } catch (Exception e) {
            Journeymap.getLogger().error("Error handling WorldEvent.Load: " + LogFormatter.toPartialString(e));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        try {
            final World world = event.getWorld();
            if (world != null) {
                world.removeEventListener(this);
                final EntityPlayerSP player = Journeymap.clientPlayer();
                if (player != null && player.dimension == world.provider.getDimension()) {
                    Journeymap.getClient().stopMapping();
                }
            }
        } catch (Exception e) {
            Journeymap.getLogger().error("Error handling WorldEvent.Unload: " + LogFormatter.toPartialString(e));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkLoad(final ChunkEvent.Load event) {
        try {
            final EntityPlayerSP player = Journeymap.clientPlayer();
            if (player == null) {
                return;
            }
            final World world = event.getWorld();
            final Chunk chunk = event.getChunk();
            if (world.provider.getDimension() == player.dimension && chunk != null && chunk.isLoaded()) {
                DataCache.INSTANCE.addChunkMD(new ChunkMD(chunk));
            }
        } catch (Exception e) {
            Journeymap.getLogger().error("Error handling WorldEvent.Load: " + LogFormatter.toPartialString(e));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkUnload(final ChunkEvent.Unload event) {
    }

    public void notifyBlockUpdate(final World worldIn, final BlockPos pos, final IBlockState oldState, final IBlockState newState, final int flags) {
        try {
            final EntityPlayerSP player = Journeymap.clientPlayer();
            if (player == null) {
                return;
            }
            if (worldIn.provider.getDimension() == player.dimension) {
                this.resetRenderTimes(new ChunkPos(pos));
            } else {
                Journeymap.getLogger().info("Ignoring notifyBlockUpdate " + pos + " in dim " + worldIn.provider.getDimension());
            }
        } catch (Exception e) {
            Journeymap.getLogger().error("Error handling IWorldEventListener.notifyBlockUpdate: " + LogFormatter.toPartialString(e));
        }
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
            for (int x3 = cx1; x3 < cx2; ++x3) {
                for (int z3 = cz1; z3 < cz2; ++z3) {
                    this.resetRenderTimes(new ChunkPos(x3, z3));
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

    private void resetRenderTimes(final ChunkPos pos) {
        final ChunkMD chunkMD = DataCache.INSTANCE.getChunkMD(pos);
        if (chunkMD != null) {
            chunkMD.resetRenderTimes();
        }
    }
}
