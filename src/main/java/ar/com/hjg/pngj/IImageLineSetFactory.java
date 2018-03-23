package ar.com.hjg.pngj;

public interface IImageLineSetFactory<T extends IImageLine>
{
    IImageLineSet<T> create(final ImageInfo p0, final boolean p1, final int p2, final int p3, final int p4);
}
