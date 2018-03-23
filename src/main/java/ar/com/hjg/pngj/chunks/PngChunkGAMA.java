package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkGAMA extends PngChunkSingle
{
    public static final String ID = "gAMA";
    private double gamma;
    
    public PngChunkGAMA(final ImageInfo info) {
        super("gAMA", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.BEFORE_PLTE_AND_IDAT;
    }
    
    public ChunkRaw createRawChunk() {
        final ChunkRaw c = this.createEmptyChunk(4, true);
        final int g = (int)(this.gamma * 100000.0 + 0.5);
        PngHelperInternal.writeInt4tobytes(g, c.data, 0);
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw chunk) {
        if (chunk.len != 4) {
            throw new PngjException("bad chunk " + chunk);
        }
        final int g = PngHelperInternal.readInt4fromBytes(chunk.data, 0);
        this.gamma = g / 100000.0;
    }
    
    public double getGamma() {
        return this.gamma;
    }
    
    public void setGamma(final double gamma) {
        this.gamma = gamma;
    }
}
