package journeymap.client.task.multi;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.data.DataCache;
import journeymap.client.log.StatTimer;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.model.RegionImageCache;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

public abstract class BaseMapTask implements ITask {
    static final Logger logger;
    protected static ChunkPos[] keepAliveOffsets;

    static {
        logger = Journeymap.getLogger();
        BaseMapTask.keepAliveOffsets = new ChunkPos[]{new ChunkPos(0, -1), new ChunkPos(-1, 0), new ChunkPos(-1, -1)};
    }

    final World world;
    final Collection<ChunkPos> chunkCoords;
    final boolean flushCacheWhenDone;
    final ChunkRenderController renderController;
    final int elapsedLimit;
    final MapType mapType;
    final boolean asyncFileWrites;

    public BaseMapTask(final ChunkRenderController renderController, final World world, final MapType mapType, final Collection<ChunkPos> chunkCoords, final boolean flushCacheWhenDone, final boolean asyncFileWrites, final int elapsedLimit) {
        this.renderController = renderController;
        this.world = world;
        this.mapType = mapType;
        this.chunkCoords = chunkCoords;
        this.asyncFileWrites = asyncFileWrites;
        this.flushCacheWhenDone = flushCacheWhenDone;
        this.elapsedLimit = elapsedLimit;
    }

    public void initTask(final Minecraft mc, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException {
    }

    @Override
    public void performTask(final Minecraft mc, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException {
        if (!this.mapType.isAllowed()) {
            this.complete(0, true, false);
            return;
        }
        final StatTimer timer = StatTimer.get(this.getClass().getSimpleName() + ".performTask", 5, this.elapsedLimit).start();
        this.initTask(mc, jm, jmWorldDir, threadLogging);
        int count = 0;
        try {
            if (mc.world == null) {
                this.complete(count, true, false);
                return;
            }
            final Iterator<ChunkPos> chunkIter = this.chunkCoords.iterator();
            final int currentDimension = FMLClientHandler.instance().getClient().player.world.provider.getDimension();
            if (currentDimension != this.mapType.dimension) {
                if (threadLogging) {
                    BaseMapTask.logger.debug("Dimension changed, map task obsolete.");
                }
                timer.cancel();
                this.complete(count, true, false);
                return;
            }
            final ChunkPos playerChunk = new ChunkPos(FMLClientHandler.instance().getClient().player.getPosition());
            while (chunkIter.hasNext()) {
                if (!jm.isMapping()) {
                    if (threadLogging) {
                        BaseMapTask.logger.debug("JM isn't mapping, aborting");
                    }
                    timer.cancel();
                    this.complete(count, true, false);
                    return;
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final ChunkPos coord = chunkIter.next();
                final ChunkMD chunkMd = DataCache.INSTANCE.getChunkMD(coord);
                if (chunkMd == null || !chunkMd.hasChunk()) {
                    continue;
                }
                try {
                    final RegionCoord rCoord = RegionCoord.fromChunkPos(jmWorldDir, this.mapType, chunkMd.getCoord().x, chunkMd.getCoord().z);
                    final boolean rendered = this.renderController.renderChunk(rCoord, this.mapType, chunkMd);
                    if (!rendered) {
                        continue;
                    }
                    ++count;
                } catch (Throwable t) {
                    BaseMapTask.logger.warn("Error rendering chunk " + chunkMd + ": " + t.getMessage());
                }
            }
            if (!jm.isMapping()) {
                if (threadLogging) {
                    BaseMapTask.logger.debug("JM isn't mapping, aborting.");
                }
                timer.cancel();
                this.complete(count, true, false);
                return;
            }
            if (Thread.interrupted()) {
                timer.cancel();
                throw new InterruptedException();
            }
            RegionImageCache.INSTANCE.updateTextures(this.flushCacheWhenDone, this.asyncFileWrites);
            this.chunkCoords.clear();
            this.complete(count, false, false);
            timer.stop();
        } catch (InterruptedException t2) {
            Journeymap.getLogger().warn("Task thread interrupted: " + this);
            timer.cancel();
            throw t2;
        } catch (Throwable t3) {
            final String error = "Unexpected error in BaseMapTask: " + LogFormatter.toString(t3);
            Journeymap.getLogger().error(error);
            this.complete(count, false, true);
            timer.cancel();
        } finally {
            if (threadLogging) {
                timer.report();
            }
        }
    }

    protected abstract void complete(final int p0, final boolean p1, final boolean p2);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{world=" + this.world + ", mapType=" + this.mapType + ", chunkCoords=" + this.chunkCoords + ", flushCacheWhenDone=" + this.flushCacheWhenDone + '}';
    }
}
