package ar.com.hjg.pngj;

import ar.com.hjg.pngj.chunks.*;

public interface IChunkFactory
{
    PngChunk createChunk(final ChunkRaw p0, final ImageInfo p1);
}
