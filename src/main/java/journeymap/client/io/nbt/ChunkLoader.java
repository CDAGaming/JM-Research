package journeymap.client.io.nbt;

import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ChunkLoader {
    private static Logger logger;

    static {
        ChunkLoader.logger = Journeymap.getLogger();
    }

    public static ChunkMD getChunkMD(final AnvilChunkLoader loader, final Minecraft mc, final ChunkPos coord, final boolean forceRetain) {
        try {
            if (RegionLoader.getRegionFile(mc, coord.x, coord.z).exists()) {
                if (loader.chunkExists(mc.world, coord.x, coord.z)) {
                    final Chunk chunk = loader.loadChunk(mc.world, coord.x, coord.z);
                    if (chunk != null) {
                        if (!chunk.isLoaded()) {
                            chunk.markLoaded(true);
                        }
                        return new ChunkMD(chunk, forceRetain);
                    }
                    ChunkLoader.logger.warn("AnvilChunkLoader returned null for chunk: " + coord);
                }
            } else {
                ChunkLoader.logger.warn("Region doesn't exist for chunk: " + coord);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ChunkMD getChunkMdFromMemory(final World world, final int chunkX, final int chunkZ) {
        if (world != null) {
            final IChunkProvider provider = world.getChunkProvider();
            if (provider != null) {
                final Chunk theChunk = provider.getLoadedChunk(chunkX, chunkZ);
                if (theChunk != null && theChunk.isLoaded() && !(theChunk instanceof EmptyChunk)) {
                    return new ChunkMD(theChunk);
                }
            }
        }
        return null;
    }
}
