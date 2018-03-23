package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkBKGD extends PngChunkSingle
{
    public static final String ID = "bKGD";
    private int gray;
    private int red;
    private int green;
    private int blue;
    private int paletteIndex;
    
    public PngChunkBKGD(final ImageInfo info) {
        super("bKGD", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.AFTER_PLTE_BEFORE_IDAT;
    }
    
    public ChunkRaw createRawChunk() {
        ChunkRaw c = null;
        if (this.imgInfo.greyscale) {
            c = this.createEmptyChunk(2, true);
            PngHelperInternal.writeInt2tobytes(this.gray, c.data, 0);
        }
        else if (this.imgInfo.indexed) {
            c = this.createEmptyChunk(1, true);
            c.data[0] = (byte)this.paletteIndex;
        }
        else {
            c = this.createEmptyChunk(6, true);
            PngHelperInternal.writeInt2tobytes(this.red, c.data, 0);
            PngHelperInternal.writeInt2tobytes(this.green, c.data, 0);
            PngHelperInternal.writeInt2tobytes(this.blue, c.data, 0);
        }
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw c) {
        if (this.imgInfo.greyscale) {
            this.gray = PngHelperInternal.readInt2fromBytes(c.data, 0);
        }
        else if (this.imgInfo.indexed) {
            this.paletteIndex = (c.data[0] & 0xFF);
        }
        else {
            this.red = PngHelperInternal.readInt2fromBytes(c.data, 0);
            this.green = PngHelperInternal.readInt2fromBytes(c.data, 2);
            this.blue = PngHelperInternal.readInt2fromBytes(c.data, 4);
        }
    }
    
    public void setGray(final int gray) {
        if (!this.imgInfo.greyscale) {
            throw new PngjException("only gray images support this");
        }
        this.gray = gray;
    }
    
    public int getGray() {
        if (!this.imgInfo.greyscale) {
            throw new PngjException("only gray images support this");
        }
        return this.gray;
    }
    
    public void setPaletteIndex(final int i) {
        if (!this.imgInfo.indexed) {
            throw new PngjException("only indexed (pallete) images support this");
        }
        this.paletteIndex = i;
    }
    
    public int getPaletteIndex() {
        if (!this.imgInfo.indexed) {
            throw new PngjException("only indexed (pallete) images support this");
        }
        return this.paletteIndex;
    }
    
    public void setRGB(final int r, final int g, final int b) {
        if (this.imgInfo.greyscale || this.imgInfo.indexed) {
            throw new PngjException("only rgb or rgba images support this");
        }
        this.red = r;
        this.green = g;
        this.blue = b;
    }
    
    public int[] getRGB() {
        if (this.imgInfo.greyscale || this.imgInfo.indexed) {
            throw new PngjException("only rgb or rgba images support this");
        }
        return new int[] { this.red, this.green, this.blue };
    }
}
