package ar.com.hjg.pngj;

import java.io.*;
import java.util.*;
import ar.com.hjg.pngj.chunks.*;

public class PngReaderFilter extends FilterInputStream
{
    private ChunkSeqReaderPng chunkseq;
    
    public PngReaderFilter(final InputStream arg0) {
        super(arg0);
        this.chunkseq = this.createChunkSequenceReader();
    }
    
    protected ChunkSeqReaderPng createChunkSequenceReader() {
        return new ChunkSeqReaderPng(true) {
            public boolean shouldSkipContent(final int len, final String id) {
                return super.shouldSkipContent(len, id) || id.equals("IDAT");
            }
            
            protected boolean shouldCheckCrc(final int len, final String id) {
                return false;
            }
            
            protected void postProcessChunk(final ChunkReader chunkR) {
                super.postProcessChunk(chunkR);
            }
        };
    }
    
    public void close() throws IOException {
        super.close();
        this.chunkseq.close();
    }
    
    public int read() throws IOException {
        final int r = super.read();
        if (r > 0) {
            this.chunkseq.feedAll(new byte[] { (byte)r }, 0, 1);
        }
        return r;
    }
    
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int res = super.read(b, off, len);
        if (res > 0) {
            this.chunkseq.feedAll(b, off, res);
        }
        return res;
    }
    
    public int read(final byte[] b) throws IOException {
        final int res = super.read(b);
        if (res > 0) {
            this.chunkseq.feedAll(b, 0, res);
        }
        return res;
    }
    
    public void readUntilEndAndClose() throws IOException {
        final BufferedStreamFeeder br = new BufferedStreamFeeder(this.in);
        while (!this.chunkseq.isDone() && br.hasMoreToFeed()) {
            br.feed(this.chunkseq);
        }
        this.close();
    }
    
    public List<PngChunk> getChunksList() {
        return this.chunkseq.getChunks();
    }
    
    public ChunkSeqReaderPng getChunkseq() {
        return this.chunkseq;
    }
}
