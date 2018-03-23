package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkSBIT extends PngChunkSingle
{
    public static final String ID = "sBIT";
    private int graysb;
    private int alphasb;
    private int redsb;
    private int greensb;
    private int bluesb;
    
    public PngChunkSBIT(final ImageInfo info) {
        super("sBIT", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.BEFORE_PLTE_AND_IDAT;
    }
    
    private int getCLen() {
        int len = this.imgInfo.greyscale ? 1 : 3;
        if (this.imgInfo.alpha) {
            ++len;
        }
        return len;
    }
    
    public void parseFromRaw(final ChunkRaw c) {
        if (c.len != this.getCLen()) {
            throw new PngjException("bad chunk length " + c);
        }
        if (this.imgInfo.greyscale) {
            this.graysb = PngHelperInternal.readInt1fromByte(c.data, 0);
            if (this.imgInfo.alpha) {
                this.alphasb = PngHelperInternal.readInt1fromByte(c.data, 1);
            }
        }
        else {
            this.redsb = PngHelperInternal.readInt1fromByte(c.data, 0);
            this.greensb = PngHelperInternal.readInt1fromByte(c.data, 1);
            this.bluesb = PngHelperInternal.readInt1fromByte(c.data, 2);
            if (this.imgInfo.alpha) {
                this.alphasb = PngHelperInternal.readInt1fromByte(c.data, 3);
            }
        }
    }
    
    public ChunkRaw createRawChunk() {
        ChunkRaw c = null;
        c = this.createEmptyChunk(this.getCLen(), true);
        if (this.imgInfo.greyscale) {
            c.data[0] = (byte)this.graysb;
            if (this.imgInfo.alpha) {
                c.data[1] = (byte)this.alphasb;
            }
        }
        else {
            c.data[0] = (byte)this.redsb;
            c.data[1] = (byte)this.greensb;
            c.data[2] = (byte)this.bluesb;
            if (this.imgInfo.alpha) {
                c.data[3] = (byte)this.alphasb;
            }
        }
        return c;
    }
    
    public void setGraysb(final int gray) {
        if (!this.imgInfo.greyscale) {
            throw new PngjException("only greyscale images support this");
        }
        this.graysb = gray;
    }
    
    public int getGraysb() {
        if (!this.imgInfo.greyscale) {
            throw new PngjException("only greyscale images support this");
        }
        return this.graysb;
    }
    
    public void setAlphasb(final int a) {
        if (!this.imgInfo.alpha) {
            throw new PngjException("only images with alpha support this");
        }
        this.alphasb = a;
    }
    
    public int getAlphasb() {
        if (!this.imgInfo.alpha) {
            throw new PngjException("only images with alpha support this");
        }
        return this.alphasb;
    }
    
    public void setRGB(final int r, final int g, final int b) {
        if (this.imgInfo.greyscale || this.imgInfo.indexed) {
            throw new PngjException("only rgb or rgba images support this");
        }
        this.redsb = r;
        this.greensb = g;
        this.bluesb = b;
    }
    
    public int[] getRGB() {
        if (this.imgInfo.greyscale || this.imgInfo.indexed) {
            throw new PngjException("only rgb or rgba images support this");
        }
        return new int[] { this.redsb, this.greensb, this.bluesb };
    }
}
