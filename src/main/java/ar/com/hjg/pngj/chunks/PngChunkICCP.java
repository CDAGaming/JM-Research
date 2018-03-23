package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkICCP extends PngChunkSingle
{
    public static final String ID = "iCCP";
    private String profileName;
    private byte[] compressedProfile;
    
    public PngChunkICCP(final ImageInfo info) {
        super("iCCP", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.BEFORE_PLTE_AND_IDAT;
    }
    
    public ChunkRaw createRawChunk() {
        final ChunkRaw c = this.createEmptyChunk(this.profileName.length() + this.compressedProfile.length + 2, true);
        System.arraycopy(ChunkHelper.toBytes(this.profileName), 0, c.data, 0, this.profileName.length());
        c.data[this.profileName.length()] = 0;
        c.data[this.profileName.length() + 1] = 0;
        System.arraycopy(this.compressedProfile, 0, c.data, this.profileName.length() + 2, this.compressedProfile.length);
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw chunk) {
        final int pos0 = ChunkHelper.posNullByte(chunk.data);
        this.profileName = ChunkHelper.toString(chunk.data, 0, pos0);
        final int comp = chunk.data[pos0 + 1] & 0xFF;
        if (comp != 0) {
            throw new PngjException("bad compression for ChunkTypeICCP");
        }
        final int compdatasize = chunk.data.length - (pos0 + 2);
        this.compressedProfile = new byte[compdatasize];
        System.arraycopy(chunk.data, pos0 + 2, this.compressedProfile, 0, compdatasize);
    }
    
    public void setProfileNameAndContent(final String name, final byte[] profile) {
        this.profileName = name;
        this.compressedProfile = ChunkHelper.compressBytes(profile, true);
    }
    
    public void setProfileNameAndContent(final String name, final String profile) {
        this.setProfileNameAndContent(name, ChunkHelper.toBytes(profile));
    }
    
    public String getProfileName() {
        return this.profileName;
    }
    
    public byte[] getProfile() {
        return ChunkHelper.compressBytes(this.compressedProfile, false);
    }
    
    public String getProfileAsString() {
        return ChunkHelper.toString(this.getProfile());
    }
}
