package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkSRGB extends PngChunkSingle
{
    public static final String ID = "sRGB";
    public static final int RENDER_INTENT_Perceptual = 0;
    public static final int RENDER_INTENT_Relative_colorimetric = 1;
    public static final int RENDER_INTENT_Saturation = 2;
    public static final int RENDER_INTENT_Absolute_colorimetric = 3;
    private int intent;
    
    public PngChunkSRGB(final ImageInfo info) {
        super("sRGB", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.BEFORE_PLTE_AND_IDAT;
    }
    
    public void parseFromRaw(final ChunkRaw c) {
        if (c.len != 1) {
            throw new PngjException("bad chunk length " + c);
        }
        this.intent = PngHelperInternal.readInt1fromByte(c.data, 0);
    }
    
    public ChunkRaw createRawChunk() {
        ChunkRaw c = null;
        c = this.createEmptyChunk(1, true);
        c.data[0] = (byte)this.intent;
        return c;
    }
    
    public int getIntent() {
        return this.intent;
    }
    
    public void setIntent(final int intent) {
        this.intent = intent;
    }
}
