package ar.com.hjg.pngj;

public class DeflatedChunkReader extends ChunkReader
{
    protected final DeflatedChunksSet deflatedChunksSet;
    protected boolean alsoBuffer;
    
    public DeflatedChunkReader(final int clen, final String chunkid, final boolean checkCrc, final long offsetInPng, final DeflatedChunksSet iDatSet) {
        super(clen, chunkid, offsetInPng, ChunkReaderMode.PROCESS);
        this.alsoBuffer = false;
        (this.deflatedChunksSet = iDatSet).appendNewChunk(this);
    }
    
    protected void processData(final int offsetInchunk, final byte[] buf, final int off, final int len) {
        if (len > 0) {
            this.deflatedChunksSet.processBytes(buf, off, len);
            if (this.alsoBuffer) {
                System.arraycopy(buf, off, this.getChunkRaw().data, this.read, len);
            }
        }
    }
    
    protected void chunkDone() {
    }
    
    public void setAlsoBuffer() {
        if (this.read > 0) {
            throw new RuntimeException("too late");
        }
        this.alsoBuffer = true;
        this.getChunkRaw().allocData();
    }
}
