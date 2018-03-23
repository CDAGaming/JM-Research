package ar.com.hjg.pngj;

public interface IImageLineSet<T extends IImageLine>
{
    T getImageLine(final int p0);
    
    boolean hasImageLine(final int p0);
    
    int size();
}
