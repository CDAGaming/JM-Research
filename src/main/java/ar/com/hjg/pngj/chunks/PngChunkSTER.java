package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkSTER extends PngChunkSingle
{
    public static final String ID = "sTER";
    private byte mode;
    
    public PngChunkSTER(final ImageInfo info) {
        super("sTER", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.BEFORE_IDAT;
    }
    
    public ChunkRaw createRawChunk() {
        final ChunkRaw c = this.createEmptyChunk(1, true);
        c.data[0] = this.mode;
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw chunk) {
        if (chunk.len != 1) {
            throw new PngjException("bad chunk length " + chunk);
        }
        this.mode = chunk.data[0];
    }
    
    public byte getMode() {
        return this.mode;
    }
    
    public void setMode(final byte mode) {
        this.mode = mode;
    }
}
