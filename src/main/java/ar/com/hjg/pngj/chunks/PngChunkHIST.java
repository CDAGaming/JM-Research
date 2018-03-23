package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkHIST extends PngChunkSingle
{
    public static final String ID = "hIST";
    private int[] hist;
    
    public PngChunkHIST(final ImageInfo info) {
        super("hIST", info);
        this.hist = new int[0];
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.AFTER_PLTE_BEFORE_IDAT;
    }
    
    public void parseFromRaw(final ChunkRaw c) {
        if (!this.imgInfo.indexed) {
            throw new PngjException("only indexed images accept a HIST chunk");
        }
        final int nentries = c.data.length / 2;
        this.hist = new int[nentries];
        for (int i = 0; i < this.hist.length; ++i) {
            this.hist[i] = PngHelperInternal.readInt2fromBytes(c.data, i * 2);
        }
    }
    
    public ChunkRaw createRawChunk() {
        if (!this.imgInfo.indexed) {
            throw new PngjException("only indexed images accept a HIST chunk");
        }
        ChunkRaw c = null;
        c = this.createEmptyChunk(this.hist.length * 2, true);
        for (int i = 0; i < this.hist.length; ++i) {
            PngHelperInternal.writeInt2tobytes(this.hist[i], c.data, i * 2);
        }
        return c;
    }
    
    public int[] getHist() {
        return this.hist;
    }
    
    public void setHist(final int[] hist) {
        this.hist = hist;
    }
}
