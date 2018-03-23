package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public abstract class PngChunkSingle extends PngChunk
{
    protected PngChunkSingle(final String id, final ImageInfo imgInfo) {
        super(id, imgInfo);
    }
    
    public final boolean allowsMultiple() {
        return false;
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }
    
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final PngChunkSingle other = (PngChunkSingle)obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
