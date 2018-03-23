package ar.com.hjg.pngj;

public class ChunkSeqBuffering extends ChunkSeqReader
{
    protected boolean checkCrc;
    
    public ChunkSeqBuffering() {
        this.checkCrc = true;
    }
    
    protected boolean isIdatKind(final String id) {
        return false;
    }
    
    protected boolean shouldCheckCrc(final int len, final String id) {
        return this.checkCrc;
    }
    
    public void setCheckCrc(final boolean checkCrc) {
        this.checkCrc = checkCrc;
    }
}
