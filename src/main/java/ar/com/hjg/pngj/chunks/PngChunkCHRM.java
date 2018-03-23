package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkCHRM extends PngChunkSingle
{
    public static final String ID = "cHRM";
    private double whitex;
    private double whitey;
    private double redx;
    private double redy;
    private double greenx;
    private double greeny;
    private double bluex;
    private double bluey;
    
    public PngChunkCHRM(final ImageInfo info) {
        super("cHRM", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.AFTER_PLTE_BEFORE_IDAT;
    }
    
    public ChunkRaw createRawChunk() {
        ChunkRaw c = null;
        c = this.createEmptyChunk(32, true);
        PngHelperInternal.writeInt4tobytes(PngHelperInternal.doubleToInt100000(this.whitex), c.data, 0);
        PngHelperInternal.writeInt4tobytes(PngHelperInternal.doubleToInt100000(this.whitey), c.data, 4);
        PngHelperInternal.writeInt4tobytes(PngHelperInternal.doubleToInt100000(this.redx), c.data, 8);
        PngHelperInternal.writeInt4tobytes(PngHelperInternal.doubleToInt100000(this.redy), c.data, 12);
        PngHelperInternal.writeInt4tobytes(PngHelperInternal.doubleToInt100000(this.greenx), c.data, 16);
        PngHelperInternal.writeInt4tobytes(PngHelperInternal.doubleToInt100000(this.greeny), c.data, 20);
        PngHelperInternal.writeInt4tobytes(PngHelperInternal.doubleToInt100000(this.bluex), c.data, 24);
        PngHelperInternal.writeInt4tobytes(PngHelperInternal.doubleToInt100000(this.bluey), c.data, 28);
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw c) {
        if (c.len != 32) {
            throw new PngjException("bad chunk " + c);
        }
        this.whitex = PngHelperInternal.intToDouble100000(PngHelperInternal.readInt4fromBytes(c.data, 0));
        this.whitey = PngHelperInternal.intToDouble100000(PngHelperInternal.readInt4fromBytes(c.data, 4));
        this.redx = PngHelperInternal.intToDouble100000(PngHelperInternal.readInt4fromBytes(c.data, 8));
        this.redy = PngHelperInternal.intToDouble100000(PngHelperInternal.readInt4fromBytes(c.data, 12));
        this.greenx = PngHelperInternal.intToDouble100000(PngHelperInternal.readInt4fromBytes(c.data, 16));
        this.greeny = PngHelperInternal.intToDouble100000(PngHelperInternal.readInt4fromBytes(c.data, 20));
        this.bluex = PngHelperInternal.intToDouble100000(PngHelperInternal.readInt4fromBytes(c.data, 24));
        this.bluey = PngHelperInternal.intToDouble100000(PngHelperInternal.readInt4fromBytes(c.data, 28));
    }
    
    public void setChromaticities(final double whitex, final double whitey, final double redx, final double redy, final double greenx, final double greeny, final double bluex, final double bluey) {
        this.whitex = whitex;
        this.redx = redx;
        this.greenx = greenx;
        this.bluex = bluex;
        this.whitey = whitey;
        this.redy = redy;
        this.greeny = greeny;
        this.bluey = bluey;
    }
    
    public double[] getChromaticities() {
        return new double[] { this.whitex, this.whitey, this.redx, this.redy, this.greenx, this.greeny, this.bluex, this.bluey };
    }
}
