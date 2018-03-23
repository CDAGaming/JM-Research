package journeymap.client.io.nbt;

import org.apache.logging.log4j.*;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.client.*;
import net.minecraft.util.math.*;
import journeymap.client.model.*;
import net.minecraft.world.*;
import java.io.*;
import net.minecraft.world.chunk.*;
import journeymap.common.*;

public class ChunkLoader
{
    private static Logger logger;
    
    public static ChunkMD getChunkMD(final AnvilChunkLoader loader, final Minecraft mc, final ChunkPos coord, final boolean forceRetain) {
        try {
            if (RegionLoader.getRegionFile(mc, coord.x, coord.z).exists()) {
                if (loader.chunkExists((World)mc.world, coord.x, coord.z)) {
                    final Chunk chunk = loader.loadChunk((World)mc.world, coord.x, coord.z);
                    if (chunk != null) {
                        if (!chunk.isLoaded()) {
                            chunk.markLoaded(true);
                        }
                        return new ChunkMD(chunk, forceRetain);
                    }
                    ChunkLoader.logger.warn("AnvilChunkLoader returned null for chunk: " + coord);
                }
            }
            else {
                ChunkLoader.logger.warn("Region doesn't exist for chunk: " + coord);
            }
        }
        catch (IOException e) {
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
    
    static {
        ChunkLoader.logger = Journeymap.getLogger();
    }
}
