package ar.com.hjg.pngj;

import ar.com.hjg.pngj.chunks.*;
import java.util.*;

public class ChunkSeqReaderPng extends ChunkSeqReader
{
    protected ImageInfo imageInfo;
    protected Deinterlacer deinterlacer;
    protected int currentChunkGroup;
    protected ChunksList chunksList;
    protected final boolean callbackMode;
    private long bytesChunksLoaded;
    private boolean checkCrc;
    private boolean includeNonBufferedChunks;
    private Set<String> chunksToSkip;
    private long maxTotalBytesRead;
    private long skipChunkMaxSize;
    private long maxBytesMetadata;
    private IChunkFactory chunkFactory;
    private ChunkLoadBehaviour chunkLoadBehaviour;
    
    public ChunkSeqReaderPng(final boolean callbackMode) {
        this.currentChunkGroup = -1;
        this.chunksList = null;
        this.bytesChunksLoaded = 0L;
        this.checkCrc = true;
        this.includeNonBufferedChunks = false;
        this.chunksToSkip = new HashSet<String>();
        this.maxTotalBytesRead = 0L;
        this.skipChunkMaxSize = 0L;
        this.maxBytesMetadata = 0L;
        this.chunkLoadBehaviour = ChunkLoadBehaviour.LOAD_CHUNK_ALWAYS;
        this.callbackMode = callbackMode;
        this.chunkFactory = new ChunkFactory();
    }
    
    private void updateAndCheckChunkGroup(final String id) {
        if (id.equals("IHDR")) {
            if (this.currentChunkGroup >= 0) {
                throw new PngjInputException("unexpected chunk " + id);
            }
            this.currentChunkGroup = 0;
        }
        else if (id.equals("PLTE")) {
            if (this.currentChunkGroup != 0 && this.currentChunkGroup != 1) {
                throw new PngjInputException("unexpected chunk " + id);
            }
            this.currentChunkGroup = 2;
        }
        else if (id.equals("IDAT")) {
            if (this.currentChunkGroup < 0 || this.currentChunkGroup > 4) {
                throw new PngjInputException("unexpected chunk " + id);
            }
            this.currentChunkGroup = 4;
        }
        else if (id.equals("IEND")) {
            if (this.currentChunkGroup < 4) {
                throw new PngjInputException("unexpected chunk " + id);
            }
            this.currentChunkGroup = 6;
        }
        else if (this.currentChunkGroup <= 1) {
            this.currentChunkGroup = 1;
        }
        else if (this.currentChunkGroup <= 3) {
            this.currentChunkGroup = 3;
        }
        else {
            this.currentChunkGroup = 5;
        }
    }
    
    public boolean shouldSkipContent(final int len, final String id) {
        if (super.shouldSkipContent(len, id)) {
            return true;
        }
        if (ChunkHelper.isCritical(id)) {
            return false;
        }
        if (this.maxTotalBytesRead > 0L && len + this.getBytesCount() > this.maxTotalBytesRead) {
            throw new PngjInputException("Maximum total bytes to read exceeeded: " + this.maxTotalBytesRead + " offset:" + this.getBytesCount() + " len=" + len);
        }
        if (this.chunksToSkip.contains(id)) {
            return true;
        }
        if (this.skipChunkMaxSize > 0L && len > this.skipChunkMaxSize) {
            return true;
        }
        if (this.maxBytesMetadata > 0L && len > this.maxBytesMetadata - this.bytesChunksLoaded) {
            return true;
        }
        switch (this.chunkLoadBehaviour) {
            case LOAD_CHUNK_IF_SAFE: {
                if (!ChunkHelper.isSafeToCopy(id)) {
                    return true;
                }
                break;
            }
            case LOAD_CHUNK_NEVER: {
                return true;
            }
        }
        return false;
    }
    
    public long getBytesChunksLoaded() {
        return this.bytesChunksLoaded;
    }
    
    public int getCurrentChunkGroup() {
        return this.currentChunkGroup;
    }
    
    public void setChunksToSkip(final String... chunksToSkip) {
        this.chunksToSkip.clear();
        for (final String c : chunksToSkip) {
            this.chunksToSkip.add(c);
        }
    }
    
    public void addChunkToSkip(final String chunkToSkip) {
        this.chunksToSkip.add(chunkToSkip);
    }
    
    public boolean firstChunksNotYetRead() {
        return this.getCurrentChunkGroup() < 4;
    }
    
    protected void postProcessChunk(final ChunkReader chunkR) {
        super.postProcessChunk(chunkR);
        if (chunkR.getChunkRaw().id.equals("IHDR")) {
            final PngChunkIHDR ch = new PngChunkIHDR(null);
            ch.parseFromRaw(chunkR.getChunkRaw());
            this.imageInfo = ch.createImageInfo();
            if (ch.isInterlaced()) {
                this.deinterlacer = new Deinterlacer(this.imageInfo);
            }
            this.chunksList = new ChunksList(this.imageInfo);
        }
        if (chunkR.mode == ChunkReader.ChunkReaderMode.BUFFER || this.includeNonBufferedChunks) {
            final PngChunk chunk = this.chunkFactory.createChunk(chunkR.getChunkRaw(), this.getImageInfo());
            this.chunksList.appendReadChunk(chunk, this.currentChunkGroup);
        }
        if (this.isDone()) {
            this.processEndPng();
        }
    }
    
    protected DeflatedChunksSet createIdatSet(final String id) {
        final IdatSet ids = new IdatSet(id, this.imageInfo, this.deinterlacer);
        ids.setCallbackMode(this.callbackMode);
        return ids;
    }
    
    public IdatSet getIdatSet() {
        final DeflatedChunksSet c = this.getCurReaderDeflatedSet();
        return (c instanceof IdatSet) ? ((IdatSet)c) : null;
    }
    
    protected boolean isIdatKind(final String id) {
        return id.equals("IDAT");
    }
    
    public int consume(final byte[] buf, final int off, final int len) {
        return super.consume(buf, off, len);
    }
    
    public void setChunkFactory(final IChunkFactory chunkFactory) {
        this.chunkFactory = chunkFactory;
    }
    
    protected void processEndPng() {
    }
    
    public ImageInfo getImageInfo() {
        return this.imageInfo;
    }
    
    public boolean isInterlaced() {
        return this.deinterlacer != null;
    }
    
    public Deinterlacer getDeinterlacer() {
        return this.deinterlacer;
    }
    
    protected void startNewChunk(final int len, final String id, final long offset) {
        this.updateAndCheckChunkGroup(id);
        super.startNewChunk(len, id, offset);
    }
    
    public void close() {
        if (this.currentChunkGroup != 6) {
            this.currentChunkGroup = 6;
        }
        super.close();
    }
    
    public List<PngChunk> getChunks() {
        return this.chunksList.getChunks();
    }
    
    public void setMaxTotalBytesRead(final long maxTotalBytesRead) {
        this.maxTotalBytesRead = maxTotalBytesRead;
    }
    
    public long getSkipChunkMaxSize() {
        return this.skipChunkMaxSize;
    }
    
    public void setSkipChunkMaxSize(final long skipChunkMaxSize) {
        this.skipChunkMaxSize = skipChunkMaxSize;
    }
    
    public long getMaxBytesMetadata() {
        return this.maxBytesMetadata;
    }
    
    public void setMaxBytesMetadata(final long maxBytesMetadata) {
        this.maxBytesMetadata = maxBytesMetadata;
    }
    
    public long getMaxTotalBytesRead() {
        return this.maxTotalBytesRead;
    }
    
    protected boolean shouldCheckCrc(final int len, final String id) {
        return this.checkCrc;
    }
    
    public boolean isCheckCrc() {
        return this.checkCrc;
    }
    
    public void setCheckCrc(final boolean checkCrc) {
        this.checkCrc = checkCrc;
    }
    
    public boolean isCallbackMode() {
        return this.callbackMode;
    }
    
    public Set<String> getChunksToSkip() {
        return this.chunksToSkip;
    }
    
    public void setChunkLoadBehaviour(final ChunkLoadBehaviour chunkLoadBehaviour) {
        this.chunkLoadBehaviour = chunkLoadBehaviour;
    }
    
    public void setIncludeNonBufferedChunks(final boolean includeNonBufferedChunks) {
        this.includeNonBufferedChunks = includeNonBufferedChunks;
    }
}
