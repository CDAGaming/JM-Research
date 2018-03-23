package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkPLTE extends PngChunkSingle
{
    public static final String ID = "PLTE";
    private int nentries;
    private int[] entries;
    
    public PngChunkPLTE(final ImageInfo info) {
        super("PLTE", info);
        this.nentries = 0;
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.NA;
    }
    
    public ChunkRaw createRawChunk() {
        final int len = 3 * this.nentries;
        final int[] rgb = new int[3];
        final ChunkRaw c = this.createEmptyChunk(len, true);
        int n = 0;
        int i = 0;
        while (n < this.nentries) {
            this.getEntryRgb(n, rgb);
            c.data[i++] = (byte)rgb[0];
            c.data[i++] = (byte)rgb[1];
            c.data[i++] = (byte)rgb[2];
            ++n;
        }
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw chunk) {
        this.setNentries(chunk.len / 3);
        int n = 0;
        int i = 0;
        while (n < this.nentries) {
            this.setEntry(n, chunk.data[i++] & 0xFF, chunk.data[i++] & 0xFF, chunk.data[i++] & 0xFF);
            ++n;
        }
    }
    
    public void setNentries(final int n) {
        this.nentries = n;
        if (this.nentries < 1 || this.nentries > 256) {
            throw new PngjException("invalid pallette - nentries=" + this.nentries);
        }
        if (this.entries == null || this.entries.length != this.nentries) {
            this.entries = new int[this.nentries];
        }
    }
    
    public int getNentries() {
        return this.nentries;
    }
    
    public void setEntry(final int n, final int r, final int g, final int b) {
        this.entries[n] = (r << 16 | g << 8 | b);
    }
    
    public int getEntry(final int n) {
        return this.entries[n];
    }
    
    public void getEntryRgb(final int n, final int[] rgb) {
        this.getEntryRgb(n, rgb, 0);
    }
    
    public void getEntryRgb(final int n, final int[] rgb, final int offset) {
        final int v = this.entries[n];
        rgb[offset + 0] = (v & 0xFF0000) >> 16;
        rgb[offset + 1] = (v & 0xFF00) >> 8;
        rgb[offset + 2] = (v & 0xFF);
    }
    
    public int minBitDepth() {
        if (this.nentries <= 2) {
            return 1;
        }
        if (this.nentries <= 4) {
            return 2;
        }
        if (this.nentries <= 16) {
            return 4;
        }
        return 8;
    }
}
