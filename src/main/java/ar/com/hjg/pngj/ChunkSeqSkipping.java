package ar.com.hjg.pngj;

import ar.com.hjg.pngj.chunks.*;
import java.util.*;

public class ChunkSeqSkipping extends ChunkSeqReader
{
    private List<ChunkRaw> chunks;
    private boolean skip;
    
    public ChunkSeqSkipping(final boolean skipAll) {
        super(true);
        this.chunks = new ArrayList<ChunkRaw>();
        this.skip = true;
        this.skip = skipAll;
    }
    
    public ChunkSeqSkipping() {
        this(true);
    }
    
    protected ChunkReader createChunkReaderForNewChunk(final String id, final int len, final long offset, final boolean skip) {
        return new ChunkReader(len, id, offset, skip ? ChunkReader.ChunkReaderMode.SKIP : ChunkReader.ChunkReaderMode.PROCESS) {
            protected void chunkDone() {
                ChunkSeqSkipping.this.postProcessChunk(this);
            }
            
            protected void processData(final int offsetinChhunk, final byte[] buf, final int off, final int len) {
                ChunkSeqSkipping.this.processChunkContent(this.getChunkRaw(), offsetinChhunk, buf, off, len);
            }
        };
    }
    
    protected void processChunkContent(final ChunkRaw chunkRaw, final int offsetinChhunk, final byte[] buf, final int off, final int len) {
    }
    
    protected void postProcessChunk(final ChunkReader chunkR) {
        super.postProcessChunk(chunkR);
        this.chunks.add(chunkR.getChunkRaw());
    }
    
    protected boolean shouldSkipContent(final int len, final String id) {
        return this.skip;
    }
    
    protected boolean isIdatKind(final String id) {
        return false;
    }
    
    public List<ChunkRaw> getChunks() {
        return this.chunks;
    }
}
