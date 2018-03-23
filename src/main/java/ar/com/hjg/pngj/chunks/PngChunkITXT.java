package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;
import java.io.*;

public class PngChunkITXT extends PngChunkTextVar
{
    public static final String ID = "iTXt";
    private boolean compressed;
    private String langTag;
    private String translatedTag;
    
    public PngChunkITXT(final ImageInfo info) {
        super("iTXt", info);
        this.compressed = false;
        this.langTag = "";
        this.translatedTag = "";
    }
    
    public ChunkRaw createRawChunk() {
        if (this.key == null || this.key.trim().length() == 0) {
            throw new PngjException("Text chunk key must be non empty");
        }
        try {
            final ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(ChunkHelper.toBytes(this.key));
            ba.write(0);
            ba.write(this.compressed ? 1 : 0);
            ba.write(0);
            ba.write(ChunkHelper.toBytes(this.langTag));
            ba.write(0);
            ba.write(ChunkHelper.toBytesUTF8(this.translatedTag));
            ba.write(0);
            byte[] textbytes = ChunkHelper.toBytesUTF8(this.val);
            if (this.compressed) {
                textbytes = ChunkHelper.compressBytes(textbytes, true);
            }
            ba.write(textbytes);
            final byte[] b = ba.toByteArray();
            final ChunkRaw chunk = this.createEmptyChunk(b.length, false);
            chunk.data = b;
            return chunk;
        }
        catch (IOException e) {
            throw new PngjException(e);
        }
    }
    
    public void parseFromRaw(final ChunkRaw c) {
        int nullsFound = 0;
        final int[] nullsIdx = new int[3];
        for (int i = 0; i < c.data.length; ++i) {
            if (c.data[i] == 0) {
                nullsIdx[nullsFound] = i;
                if (++nullsFound == 1) {
                    i += 2;
                }
                if (nullsFound == 3) {
                    break;
                }
            }
        }
        if (nullsFound != 3) {
            throw new PngjException("Bad formed PngChunkITXT chunk");
        }
        this.key = ChunkHelper.toString(c.data, 0, nullsIdx[0]);
        int i = nullsIdx[0] + 1;
        this.compressed = (c.data[i] != 0);
        ++i;
        if (this.compressed && c.data[i] != 0) {
            throw new PngjException("Bad formed PngChunkITXT chunk - bad compression method ");
        }
        this.langTag = ChunkHelper.toString(c.data, i, nullsIdx[1] - i);
        this.translatedTag = ChunkHelper.toStringUTF8(c.data, nullsIdx[1] + 1, nullsIdx[2] - nullsIdx[1] - 1);
        i = nullsIdx[2] + 1;
        if (this.compressed) {
            final byte[] bytes = ChunkHelper.compressBytes(c.data, i, c.data.length - i, false);
            this.val = ChunkHelper.toStringUTF8(bytes);
        }
        else {
            this.val = ChunkHelper.toStringUTF8(c.data, i, c.data.length - i);
        }
    }
    
    public boolean isCompressed() {
        return this.compressed;
    }
    
    public void setCompressed(final boolean compressed) {
        this.compressed = compressed;
    }
    
    public String getLangtag() {
        return this.langTag;
    }
    
    public void setLangtag(final String langtag) {
        this.langTag = langtag;
    }
    
    public String getTranslatedTag() {
        return this.translatedTag;
    }
    
    public void setTranslatedTag(final String translatedTag) {
        this.translatedTag = translatedTag;
    }
}
