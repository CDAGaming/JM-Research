package ar.com.hjg.pngj;

import java.io.*;

abstract class ProgressiveOutputStream extends ByteArrayOutputStream
{
    private int size;
    private long countFlushed;
    
    public ProgressiveOutputStream(final int size) {
        this.countFlushed = 0L;
        this.size = size;
    }
    
    public final void close() throws IOException {
        try {
            this.flush();
        }
        catch (Exception ex) {}
        super.close();
    }
    
    public final void flush() throws IOException {
        super.flush();
        this.checkFlushBuffer(true);
    }
    
    public final void write(final byte[] b, final int off, final int len) {
        super.write(b, off, len);
        this.checkFlushBuffer(false);
    }
    
    public final void write(final byte[] b) throws IOException {
        super.write(b);
        this.checkFlushBuffer(false);
    }
    
    public final void write(final int arg0) {
        super.write(arg0);
        this.checkFlushBuffer(false);
    }
    
    public final synchronized void reset() {
        super.reset();
    }
    
    private final void checkFlushBuffer(final boolean forced) {
        while (forced || this.count >= this.size) {
            int nb = this.size;
            if (nb > this.count) {
                nb = this.count;
            }
            if (nb == 0) {
                return;
            }
            this.flushBuffer(this.buf, nb);
            this.countFlushed += nb;
            final int bytesleft = this.count - nb;
            if ((this.count = bytesleft) <= 0) {
                continue;
            }
            System.arraycopy(this.buf, nb, this.buf, 0, bytesleft);
        }
    }
    
    protected abstract void flushBuffer(final byte[] p0, final int p1);
    
    public void setSize(final int size) {
        this.size = size;
        System.out.println("setting size: " + size + " count" + this.count);
        this.checkFlushBuffer(false);
    }
    
    public long getCountFlushed() {
        return this.countFlushed;
    }
}
