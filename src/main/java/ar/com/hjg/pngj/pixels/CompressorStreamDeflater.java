package ar.com.hjg.pngj.pixels;

import java.util.zip.*;
import ar.com.hjg.pngj.*;
import java.io.*;

public class CompressorStreamDeflater extends CompressorStream
{
    protected Deflater deflater;
    protected byte[] buf;
    protected boolean deflaterIsOwn;
    
    public CompressorStreamDeflater(final OutputStream os, final int maxBlockLen, final long totalLen) {
        this(os, maxBlockLen, totalLen, null);
    }
    
    public CompressorStreamDeflater(final OutputStream os, final int maxBlockLen, final long totalLen, final Deflater def) {
        super(os, maxBlockLen, totalLen);
        this.buf = new byte[4092];
        this.deflaterIsOwn = true;
        this.deflater = ((def == null) ? new Deflater() : def);
        this.deflaterIsOwn = (def == null);
    }
    
    public CompressorStreamDeflater(final OutputStream os, final int maxBlockLen, final long totalLen, final int deflaterCompLevel, final int deflaterStrategy) {
        this(os, maxBlockLen, totalLen, new Deflater(deflaterCompLevel));
        this.deflaterIsOwn = true;
        this.deflater.setStrategy(deflaterStrategy);
    }
    
    public void mywrite(final byte[] b, final int off, final int len) {
        if (this.deflater.finished() || this.done || this.closed) {
            throw new PngjOutputException("write beyond end of stream");
        }
        this.deflater.setInput(b, off, len);
        this.bytesIn += len;
        while (!this.deflater.needsInput()) {
            this.deflate();
        }
    }
    
    protected void deflate() {
        final int len = this.deflater.deflate(this.buf, 0, this.buf.length);
        if (len > 0) {
            this.bytesOut += len;
            try {
                if (this.os != null) {
                    this.os.write(this.buf, 0, len);
                }
            }
            catch (IOException e) {
                throw new PngjOutputException(e);
            }
        }
    }
    
    public void done() {
        if (this.done) {
            return;
        }
        if (!this.deflater.finished()) {
            this.deflater.finish();
            while (!this.deflater.finished()) {
                this.deflate();
            }
        }
        this.done = true;
        this.flush();
    }
    
    public void close() {
        this.done();
        try {
            if (this.deflaterIsOwn) {
                this.deflater.end();
            }
        }
        catch (Exception ex) {}
        super.close();
    }
    
    public void reset() {
        super.reset();
        this.deflater.reset();
    }
}
