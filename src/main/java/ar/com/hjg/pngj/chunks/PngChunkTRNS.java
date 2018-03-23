package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkTRNS extends PngChunkSingle
{
    public static final String ID = "tRNS";
    private int gray;
    private int red;
    private int green;
    private int blue;
    private int[] paletteAlpha;
    
    public PngChunkTRNS(final ImageInfo info) {
        super("tRNS", info);
        this.paletteAlpha = new int[0];
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
            c = this.createEmptyChunk(this.paletteAlpha.length, true);
            for (int n = 0; n < c.len; ++n) {
                c.data[n] = (byte)this.paletteAlpha[n];
            }
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
            final int nentries = c.data.length;
            this.paletteAlpha = new int[nentries];
            for (int n = 0; n < nentries; ++n) {
                this.paletteAlpha[n] = (c.data[n] & 0xFF);
            }
        }
        else {
            this.red = PngHelperInternal.readInt2fromBytes(c.data, 0);
            this.green = PngHelperInternal.readInt2fromBytes(c.data, 2);
            this.blue = PngHelperInternal.readInt2fromBytes(c.data, 4);
        }
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
    
    public int getRGB888() {
        if (this.imgInfo.greyscale || this.imgInfo.indexed) {
            throw new PngjException("only rgb or rgba images support this");
        }
        return this.red << 16 | this.green << 8 | this.blue;
    }
    
    public void setGray(final int g) {
        if (!this.imgInfo.greyscale) {
            throw new PngjException("only grayscale images support this");
        }
        this.gray = g;
    }
    
    public int getGray() {
        if (!this.imgInfo.greyscale) {
            throw new PngjException("only grayscale images support this");
        }
        return this.gray;
    }
    
    public void setPalletteAlpha(final int[] palAlpha) {
        if (!this.imgInfo.indexed) {
            throw new PngjException("only indexed images support this");
        }
        this.paletteAlpha = palAlpha;
    }
    
    public void setIndexEntryAsTransparent(final int palAlphaIndex) {
        if (!this.imgInfo.indexed) {
            throw new PngjException("only indexed images support this");
        }
        this.paletteAlpha = new int[] { palAlphaIndex + 1 };
        for (int i = 0; i < palAlphaIndex; ++i) {
            this.paletteAlpha[i] = 255;
        }
        this.paletteAlpha[palAlphaIndex] = 0;
    }
    
    public int[] getPalletteAlpha() {
        return this.paletteAlpha;
    }
}
