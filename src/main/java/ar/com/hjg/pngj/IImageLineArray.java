package ar.com.hjg.pngj;

public interface IImageLineArray
{
    ImageInfo getImageInfo();
    
    FilterType getFilterType();
    
    int getSize();
    
    int getElem(final int p0);
}
