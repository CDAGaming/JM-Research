package ar.com.hjg.pngj;

import ar.com.hjg.pngj.chunks.*;
import java.util.*;
import java.io.*;

public class ChunkSeqReader implements IBytesConsumer
{
    protected static final int SIGNATURE_LEN = 8;
    protected final boolean withSignature;
    private byte[] buf0;
    private int buf0len;
    private boolean signatureDone;
    private boolean done;
    private int chunkCount;
    private long bytesCount;
    private DeflatedChunksSet curReaderDeflatedSet;
    private ChunkReader curChunkReader;
    private long idatBytes;
    
    public ChunkSeqReader() {
        this(true);
    }
    
    public ChunkSeqReader(final boolean withSignature) {
        this.buf0 = new byte[8];
        this.buf0len = 0;
        this.signatureDone = false;
        this.done = false;
        this.chunkCount = 0;
        this.bytesCount = 0L;
        this.withSignature = withSignature;
        this.signatureDone = !withSignature;
    }
    
    public int consume(final byte[] buffer, int offset, int len) {
        if (this.done) {
            return -1;
        }
        if (len == 0) {
            return 0;
        }
        if (len < 0) {
            throw new PngjInputException("Bad len: " + len);
        }
        int processed = 0;
        if (this.signatureDone) {
            if (this.curChunkReader == null || this.curChunkReader.isDone()) {
                int read0 = 8 - this.buf0len;
                if (read0 > len) {
                    read0 = len;
                }
                System.arraycopy(buffer, offset, this.buf0, this.buf0len, read0);
                this.buf0len += read0;
                processed += read0;
                this.bytesCount += read0;
                len -= read0;
                offset += read0;
                if (this.buf0len == 8) {
                    ++this.chunkCount;
                    final int clen = PngHelperInternal.readInt4fromBytes(this.buf0, 0);
                    final String cid = ChunkHelper.toString(this.buf0, 4, 4);
                    this.startNewChunk(clen, cid, this.bytesCount - 8L);
                    this.buf0len = 0;
                }
            }
            else {
                final int read2 = this.curChunkReader.feedBytes(buffer, offset, len);
                processed += read2;
                this.bytesCount += read2;
            }
        }
        else {
            int read3 = 8 - this.buf0len;
            if (read3 > len) {
                read3 = len;
            }
            System.arraycopy(buffer, offset, this.buf0, this.buf0len, read3);
            this.buf0len += read3;
            if (this.buf0len == 8) {
                this.checkSignature(this.buf0);
                this.buf0len = 0;
                this.signatureDone = true;
            }
            processed += read3;
            this.bytesCount += read3;
        }
        return processed;
    }
    
    public boolean feedAll(final byte[] buf, int off, int len) {
        while (len > 0) {
            final int n = this.consume(buf, off, len);
            if (n < 1) {
                return false;
            }
            len -= n;
            off += n;
        }
        return true;
    }
    
    protected void startNewChunk(final int len, final String id, final long offset) {
        if (id.equals("IDAT")) {
            this.idatBytes += len;
        }
        final boolean checkCrc = this.shouldCheckCrc(len, id);
        final boolean skip = this.shouldSkipContent(len, id);
        final boolean isIdatType = this.isIdatKind(id);
        boolean forCurrentIdatSet = false;
        if (this.curReaderDeflatedSet != null) {
            forCurrentIdatSet = this.curReaderDeflatedSet.ackNextChunkId(id);
        }
        if (isIdatType && !skip) {
            if (!forCurrentIdatSet) {
                if (this.curReaderDeflatedSet != null) {
                    throw new PngjInputException("too many IDAT (or idatlike) chunks");
                }
                this.curReaderDeflatedSet = this.createIdatSet(id);
            }
            this.curChunkReader = new DeflatedChunkReader(len, id, checkCrc, offset, this.curReaderDeflatedSet) {
                protected void chunkDone() {
                    ChunkSeqReader.this.postProcessChunk(this);
                }
            };
        }
        else {
            this.curChunkReader = this.createChunkReaderForNewChunk(id, len, offset, skip);
            if (!checkCrc) {
                this.curChunkReader.setCrcCheck(false);
            }
        }
    }
    
    protected ChunkReader createChunkReaderForNewChunk(final String id, final int len, final long offset, final boolean skip) {
        return new ChunkReader(len, id, offset, skip ? ChunkReader.ChunkReaderMode.SKIP : ChunkReader.ChunkReaderMode.BUFFER) {
            protected void chunkDone() {
                ChunkSeqReader.this.postProcessChunk(this);
            }
            
            protected void processData(final int offsetinChhunk, final byte[] buf, final int off, final int len) {
                throw new PngjExceptionInternal("should never happen");
            }
        };
    }
    
    protected void postProcessChunk(final ChunkReader chunkR) {
        if (this.chunkCount == 1) {
            final String cid = this.firstChunkId();
            if (cid != null && !cid.equals(chunkR.getChunkRaw().id)) {
                throw new PngjInputException("Bad first chunk: " + chunkR.getChunkRaw().id + " expected: " + this.firstChunkId());
            }
        }
        if (chunkR.getChunkRaw().id.equals(this.endChunkId())) {
            this.done = true;
        }
    }
    
    protected DeflatedChunksSet createIdatSet(final String id) {
        return new DeflatedChunksSet(id, 1024, 1024);
    }
    
    protected boolean isIdatKind(final String id) {
        return false;
    }
    
    protected boolean shouldSkipContent(final int len, final String id) {
        return false;
    }
    
    protected boolean shouldCheckCrc(final int len, final String id) {
        return true;
    }
    
    protected void checkSignature(final byte[] buf) {
        if (!Arrays.equals(buf, PngHelperInternal.getPngIdSignature())) {
            throw new PngjInputException("Bad PNG signature");
        }
    }
    
    public boolean isSignatureDone() {
        return this.signatureDone;
    }
    
    public boolean isDone() {
        return this.done;
    }
    
    public long getBytesCount() {
        return this.bytesCount;
    }
    
    public int getChunkCount() {
        return this.chunkCount;
    }
    
    public ChunkReader getCurChunkReader() {
        return this.curChunkReader;
    }
    
    public DeflatedChunksSet getCurReaderDeflatedSet() {
        return this.curReaderDeflatedSet;
    }
    
    public void close() {
        if (this.curReaderDeflatedSet != null) {
            this.curReaderDeflatedSet.close();
        }
        this.done = true;
    }
    
    public boolean isAtChunkBoundary() {
        return this.bytesCount == 0L || this.bytesCount == 8L || this.done || this.curChunkReader == null || this.curChunkReader.isDone();
    }
    
    protected String firstChunkId() {
        return "IHDR";
    }
    
    public long getIdatBytes() {
        return this.idatBytes;
    }
    
    protected String endChunkId() {
        return "IEND";
    }
    
    public void feedFromFile(final File f) {
        try {
            this.feedFromInputStream(new FileInputStream(f), true);
        }
        catch (FileNotFoundException e) {
            throw new PngjInputException(e.getMessage());
        }
    }
    
    public void feedFromInputStream(final InputStream is, final boolean closeStream) {
        final BufferedStreamFeeder sf = new BufferedStreamFeeder(is);
        sf.setCloseStream(closeStream);
        try {
            while (sf.hasMoreToFeed()) {
                sf.feed(this);
            }
        }
        finally {
            this.close();
            sf.close();
        }
    }
    
    public void feedFromInputStream(final InputStream is) {
        this.feedFromInputStream(is, true);
    }
}
