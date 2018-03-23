package ar.com.hjg.pngj;

import java.io.*;

public class PngReaderInt extends PngReader
{
    public PngReaderInt(final File file) {
        super(file);
    }
    
    public PngReaderInt(final InputStream inputStream) {
        super(inputStream);
    }
    
    public ImageLineInt readRowInt() {
        final IImageLine line = this.readRow();
        if (line instanceof ImageLineInt) {
            return (ImageLineInt)line;
        }
        throw new PngjException("This is not a ImageLineInt : " + line.getClass());
    }
}
