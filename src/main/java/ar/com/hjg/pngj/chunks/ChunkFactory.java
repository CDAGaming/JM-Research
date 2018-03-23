package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class ChunkFactory implements IChunkFactory
{
    boolean parse;
    
    public ChunkFactory() {
        this(true);
    }
    
    public ChunkFactory(final boolean parse) {
        this.parse = parse;
    }
    
    public final PngChunk createChunk(final ChunkRaw chunkRaw, final ImageInfo imgInfo) {
        PngChunk c = this.createEmptyChunkKnown(chunkRaw.id, imgInfo);
        if (c == null) {
            c = this.createEmptyChunkExtended(chunkRaw.id, imgInfo);
        }
        if (c == null) {
            c = this.createEmptyChunkUnknown(chunkRaw.id, imgInfo);
        }
        c.setRaw(chunkRaw);
        if (this.parse && chunkRaw.data != null) {
            c.parseFromRaw(chunkRaw);
        }
        return c;
    }
    
    protected final PngChunk createEmptyChunkKnown(final String id, final ImageInfo imgInfo) {
        if (id.equals("IDAT")) {
            return new PngChunkIDAT(imgInfo);
        }
        if (id.equals("IHDR")) {
            return new PngChunkIHDR(imgInfo);
        }
        if (id.equals("PLTE")) {
            return new PngChunkPLTE(imgInfo);
        }
        if (id.equals("IEND")) {
            return new PngChunkIEND(imgInfo);
        }
        if (id.equals("tEXt")) {
            return new PngChunkTEXT(imgInfo);
        }
        if (id.equals("iTXt")) {
            return new PngChunkITXT(imgInfo);
        }
        if (id.equals("zTXt")) {
            return new PngChunkZTXT(imgInfo);
        }
        if (id.equals("bKGD")) {
            return new PngChunkBKGD(imgInfo);
        }
        if (id.equals("gAMA")) {
            return new PngChunkGAMA(imgInfo);
        }
        if (id.equals("pHYs")) {
            return new PngChunkPHYS(imgInfo);
        }
        if (id.equals("iCCP")) {
            return new PngChunkICCP(imgInfo);
        }
        if (id.equals("tIME")) {
            return new PngChunkTIME(imgInfo);
        }
        if (id.equals("tRNS")) {
            return new PngChunkTRNS(imgInfo);
        }
        if (id.equals("cHRM")) {
            return new PngChunkCHRM(imgInfo);
        }
        if (id.equals("sBIT")) {
            return new PngChunkSBIT(imgInfo);
        }
        if (id.equals("sRGB")) {
            return new PngChunkSRGB(imgInfo);
        }
        if (id.equals("hIST")) {
            return new PngChunkHIST(imgInfo);
        }
        if (id.equals("sPLT")) {
            return new PngChunkSPLT(imgInfo);
        }
        return null;
    }
    
    protected final PngChunk createEmptyChunkUnknown(final String id, final ImageInfo imgInfo) {
        return new PngChunkUNKNOWN(id, imgInfo);
    }
    
    protected PngChunk createEmptyChunkExtended(final String id, final ImageInfo imgInfo) {
        if (id.equals("oFFs")) {
            return new PngChunkOFFS(imgInfo);
        }
        if (id.equals("sTER")) {
            return new PngChunkSTER(imgInfo);
        }
        return null;
    }
}
