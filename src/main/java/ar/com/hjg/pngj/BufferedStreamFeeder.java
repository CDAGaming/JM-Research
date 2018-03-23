package ar.com.hjg.pngj;

import java.io.*;

public class BufferedStreamFeeder
{
    private InputStream stream;
    private byte[] buf;
    private int pendinglen;
    private int offset;
    private boolean eof;
    private boolean closeStream;
    private boolean failIfNoFeed;
    private static final int DEFAULTSIZE = 8192;
    
    public BufferedStreamFeeder(final InputStream is) {
        this(is, 8192);
    }
    
    public BufferedStreamFeeder(final InputStream is, final int bufsize) {
        this.eof = false;
        this.closeStream = true;
        this.failIfNoFeed = false;
        this.stream = is;
        this.buf = new byte[(bufsize < 1) ? 8192 : bufsize];
    }
    
    public InputStream getStream() {
        return this.stream;
    }
    
    public int feed(final IBytesConsumer consumer) {
        return this.feed(consumer, -1);
    }
    
    public int feed(final IBytesConsumer consumer, final int maxbytes) {
        int n = 0;
        if (this.pendinglen == 0) {
            this.refillBuffer();
        }
        final int tofeed = (maxbytes > 0 && maxbytes < this.pendinglen) ? maxbytes : this.pendinglen;
        if (tofeed > 0) {
            n = consumer.consume(this.buf, this.offset, tofeed);
            if (n > 0) {
                this.offset += n;
                this.pendinglen -= n;
            }
        }
        if (n < 1 && this.failIfNoFeed) {
            throw new PngjInputException("failed feed bytes");
        }
        return n;
    }
    
    public boolean feedFixed(final IBytesConsumer consumer, final int nbytes) {
        int n;
        for (int remain = nbytes; remain > 0; remain -= n) {
            n = this.feed(consumer, remain);
            if (n < 1) {
                return false;
            }
        }
        return true;
    }
    
    protected void refillBuffer() {
        if (this.pendinglen > 0 || this.eof) {
            return;
        }
        try {
            this.offset = 0;
            this.pendinglen = this.stream.read(this.buf);
            if (this.pendinglen < 0) {
                this.close();
            }
        }
        catch (IOException e) {
            throw new PngjInputException(e);
        }
    }
    
    public boolean hasMoreToFeed() {
        if (this.eof) {
            return this.pendinglen > 0;
        }
        this.refillBuffer();
        return this.pendinglen > 0;
    }
    
    public void setCloseStream(final boolean closeStream) {
        this.closeStream = closeStream;
    }
    
    public void close() {
        this.eof = true;
        this.buf = null;
        this.pendinglen = 0;
        this.offset = 0;
        if (this.stream != null && this.closeStream) {
            try {
                this.stream.close();
            }
            catch (Exception ex) {}
        }
        this.stream = null;
    }
    
    public void setInputStream(final InputStream is) {
        this.stream = is;
        this.eof = false;
    }
    
    public boolean isEof() {
        return this.eof;
    }
    
    public void setFailIfNoFeed(final boolean failIfNoFeed) {
        this.failIfNoFeed = failIfNoFeed;
    }
}
