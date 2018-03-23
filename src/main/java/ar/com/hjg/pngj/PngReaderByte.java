package ar.com.hjg.pngj;

import java.io.*;

public class PngReaderByte extends PngReader
{
    public PngReaderByte(final File file) {
        super(file);
        this.setLineSetFactory(ImageLineSetDefault.getFactoryByte());
    }
    
    public PngReaderByte(final InputStream inputStream) {
        super(inputStream);
        this.setLineSetFactory(ImageLineSetDefault.getFactoryByte());
    }
    
    public ImageLineByte readRowByte() {
        return (ImageLineByte)this.readRow();
    }
}
