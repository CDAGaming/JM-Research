package ar.com.hjg.pngj;

public class PngjExceptionInternal extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public PngjExceptionInternal(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public PngjExceptionInternal(final String message) {
        super(message);
    }
    
    public PngjExceptionInternal(final Throwable cause) {
        super(cause);
    }
}
