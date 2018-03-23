package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkPHYS extends PngChunkSingle
{
    public static final String ID = "pHYs";
    private long pixelsxUnitX;
    private long pixelsxUnitY;
    private int units;
    
    public PngChunkPHYS(final ImageInfo info) {
        super("pHYs", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.BEFORE_IDAT;
    }
    
    public ChunkRaw createRawChunk() {
        final ChunkRaw c = this.createEmptyChunk(9, true);
        PngHelperInternal.writeInt4tobytes((int)this.pixelsxUnitX, c.data, 0);
        PngHelperInternal.writeInt4tobytes((int)this.pixelsxUnitY, c.data, 4);
        c.data[8] = (byte)this.units;
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw chunk) {
        if (chunk.len != 9) {
            throw new PngjException("bad chunk length " + chunk);
        }
        this.pixelsxUnitX = PngHelperInternal.readInt4fromBytes(chunk.data, 0);
        if (this.pixelsxUnitX < 0L) {
            this.pixelsxUnitX += 4294967296L;
        }
        this.pixelsxUnitY = PngHelperInternal.readInt4fromBytes(chunk.data, 4);
        if (this.pixelsxUnitY < 0L) {
            this.pixelsxUnitY += 4294967296L;
        }
        this.units = PngHelperInternal.readInt1fromByte(chunk.data, 8);
    }
    
    public long getPixelsxUnitX() {
        return this.pixelsxUnitX;
    }
    
    public void setPixelsxUnitX(final long pixelsxUnitX) {
        this.pixelsxUnitX = pixelsxUnitX;
    }
    
    public long getPixelsxUnitY() {
        return this.pixelsxUnitY;
    }
    
    public void setPixelsxUnitY(final long pixelsxUnitY) {
        this.pixelsxUnitY = pixelsxUnitY;
    }
    
    public int getUnits() {
        return this.units;
    }
    
    public void setUnits(final int units) {
        this.units = units;
    }
    
    public double getAsDpi() {
        if (this.units != 1 || this.pixelsxUnitX != this.pixelsxUnitY) {
            return -1.0;
        }
        return this.pixelsxUnitX * 0.0254;
    }
    
    public double[] getAsDpi2() {
        if (this.units != 1) {
            return new double[] { -1.0, -1.0 };
        }
        return new double[] { this.pixelsxUnitX * 0.0254, this.pixelsxUnitY * 0.0254 };
    }
    
    public void setAsDpi(final double dpi) {
        this.units = 1;
        this.pixelsxUnitX = (long)(dpi / 0.0254 + 0.5);
        this.pixelsxUnitY = this.pixelsxUnitX;
    }
    
    public void setAsDpi2(final double dpix, final double dpiy) {
        this.units = 1;
        this.pixelsxUnitX = (long)(dpix / 0.0254 + 0.5);
        this.pixelsxUnitY = (long)(dpiy / 0.0254 + 0.5);
    }
}
