package ar.com.hjg.pngj;

public class PngjUnsupportedException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public PngjUnsupportedException() {
    }
    
    public PngjUnsupportedException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public PngjUnsupportedException(final String message) {
        super(message);
    }
    
    public PngjUnsupportedException(final Throwable cause) {
        super(cause);
    }
}
