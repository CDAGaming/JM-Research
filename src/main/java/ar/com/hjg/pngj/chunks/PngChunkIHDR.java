package ar.com.hjg.pngj.chunks;

import java.io.*;
import ar.com.hjg.pngj.*;

public class PngChunkIHDR extends PngChunkSingle
{
    public static final String ID = "IHDR";
    private int cols;
    private int rows;
    private int bitspc;
    private int colormodel;
    private int compmeth;
    private int filmeth;
    private int interlaced;
    
    public PngChunkIHDR(final ImageInfo info) {
        super("IHDR", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.NA;
    }
    
    public ChunkRaw createRawChunk() {
        final ChunkRaw c = new ChunkRaw(13, ChunkHelper.b_IHDR, true);
        int offset = 0;
        PngHelperInternal.writeInt4tobytes(this.cols, c.data, offset);
        offset += 4;
        PngHelperInternal.writeInt4tobytes(this.rows, c.data, offset);
        offset += 4;
        c.data[offset++] = (byte)this.bitspc;
        c.data[offset++] = (byte)this.colormodel;
        c.data[offset++] = (byte)this.compmeth;
        c.data[offset++] = (byte)this.filmeth;
        c.data[offset++] = (byte)this.interlaced;
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw c) {
        if (c.len != 13) {
            throw new PngjException("Bad IDHR len " + c.len);
        }
        final ByteArrayInputStream st = c.getAsByteStream();
        this.cols = PngHelperInternal.readInt4(st);
        this.rows = PngHelperInternal.readInt4(st);
        this.bitspc = PngHelperInternal.readByte(st);
        this.colormodel = PngHelperInternal.readByte(st);
        this.compmeth = PngHelperInternal.readByte(st);
        this.filmeth = PngHelperInternal.readByte(st);
        this.interlaced = PngHelperInternal.readByte(st);
    }
    
    public int getCols() {
        return this.cols;
    }
    
    public void setCols(final int cols) {
        this.cols = cols;
    }
    
    public int getRows() {
        return this.rows;
    }
    
    public void setRows(final int rows) {
        this.rows = rows;
    }
    
    public int getBitspc() {
        return this.bitspc;
    }
    
    public void setBitspc(final int bitspc) {
        this.bitspc = bitspc;
    }
    
    public int getColormodel() {
        return this.colormodel;
    }
    
    public void setColormodel(final int colormodel) {
        this.colormodel = colormodel;
    }
    
    public int getCompmeth() {
        return this.compmeth;
    }
    
    public void setCompmeth(final int compmeth) {
        this.compmeth = compmeth;
    }
    
    public int getFilmeth() {
        return this.filmeth;
    }
    
    public void setFilmeth(final int filmeth) {
        this.filmeth = filmeth;
    }
    
    public int getInterlaced() {
        return this.interlaced;
    }
    
    public void setInterlaced(final int interlaced) {
        this.interlaced = interlaced;
    }
    
    public boolean isInterlaced() {
        return this.getInterlaced() == 1;
    }
    
    public ImageInfo createImageInfo() {
        this.check();
        final boolean alpha = (this.getColormodel() & 0x4) != 0x0;
        final boolean palette = (this.getColormodel() & 0x1) != 0x0;
        final boolean grayscale = this.getColormodel() == 0 || this.getColormodel() == 4;
        return new ImageInfo(this.getCols(), this.getRows(), this.getBitspc(), alpha, grayscale, palette);
    }
    
    public void check() {
        if (this.cols < 1 || this.rows < 1 || this.compmeth != 0 || this.filmeth != 0) {
            throw new PngjInputException("bad IHDR: col/row/compmethod/filmethod invalid");
        }
        if (this.bitspc != 1 && this.bitspc != 2 && this.bitspc != 4 && this.bitspc != 8 && this.bitspc != 16) {
            throw new PngjInputException("bad IHDR: bitdepth invalid");
        }
        if (this.interlaced < 0 || this.interlaced > 1) {
            throw new PngjInputException("bad IHDR: interlace invalid");
        }
        switch (this.colormodel) {
            case 0: {
                break;
            }
            case 3: {
                if (this.bitspc == 16) {
                    throw new PngjInputException("bad IHDR: bitdepth invalid");
                }
                break;
            }
            case 2:
            case 4:
            case 6: {
                if (this.bitspc != 8 && this.bitspc != 16) {
                    throw new PngjInputException("bad IHDR: bitdepth invalid");
                }
                break;
            }
            default: {
                throw new PngjInputException("bad IHDR: invalid colormodel");
            }
        }
    }
}
