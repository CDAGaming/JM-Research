package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkUNKNOWN extends PngChunkMultiple
{
    public PngChunkUNKNOWN(final String id, final ImageInfo info) {
        super(id, info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.NONE;
    }
    
    public ChunkRaw createRawChunk() {
        return this.raw;
    }
    
    public void parseFromRaw(final ChunkRaw c) {
    }
    
    public byte[] getData() {
        return this.raw.data;
    }
    
    public void setData(final byte[] data) {
        this.raw.data = data;
    }
}
