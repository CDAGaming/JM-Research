package ar.com.hjg.pngj;

public interface IImageLine
{
    void readFromPngRaw(final byte[] p0, final int p1, final int p2, final int p3);
    
    void endReadFromPngRaw();
    
    void writeToPngRaw(final byte[] p0);
}
