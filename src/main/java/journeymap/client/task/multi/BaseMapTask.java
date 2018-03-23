package journeymap.client.task.multi;

import org.apache.logging.log4j.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import journeymap.client.cartography.*;
import net.minecraft.client.*;
import journeymap.client.*;
import java.io.*;
import journeymap.client.log.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.data.*;
import journeymap.common.*;
import journeymap.common.log.*;
import java.util.*;
import journeymap.client.model.*;

public abstract class BaseMapTask implements ITask
{
    static final Logger logger;
    protected static ChunkPos[] keepAliveOffsets;
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
            if (mc.field_71441_e == null) {
                this.complete(count, true, false);
                return;
            }
            final Iterator<ChunkPos> chunkIter = this.chunkCoords.iterator();
            final int currentDimension = FMLClientHandler.instance().getClient().field_71439_g.field_70170_p.field_73011_w.getDimension();
            if (currentDimension != this.mapType.dimension) {
                if (threadLogging) {
                    BaseMapTask.logger.debug("Dimension changed, map task obsolete.");
                }
                timer.cancel();
                this.complete(count, true, false);
                return;
            }
            final ChunkPos playerChunk = new ChunkPos(FMLClientHandler.instance().getClient().field_71439_g.func_180425_c());
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
                    final RegionCoord rCoord = RegionCoord.fromChunkPos(jmWorldDir, this.mapType, chunkMd.getCoord().field_77276_a, chunkMd.getCoord().field_77275_b);
                    final boolean rendered = this.renderController.renderChunk(rCoord, this.mapType, chunkMd);
                    if (!rendered) {
                        continue;
                    }
                    ++count;
                }
                catch (Throwable t) {
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
        }
        catch (InterruptedException t2) {
            Journeymap.getLogger().warn("Task thread interrupted: " + this);
            timer.cancel();
            throw t2;
        }
        catch (Throwable t3) {
            final String error = "Unexpected error in BaseMapTask: " + LogFormatter.toString(t3);
            Journeymap.getLogger().error(error);
            this.complete(count, false, true);
            timer.cancel();
        }
        finally {
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
    
    static {
        logger = Journeymap.getLogger();
        BaseMapTask.keepAliveOffsets = new ChunkPos[] { new ChunkPos(0, -1), new ChunkPos(-1, 0), new ChunkPos(-1, -1) };
    }
}
