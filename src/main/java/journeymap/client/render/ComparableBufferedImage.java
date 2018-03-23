package journeymap.client.render;

import java.awt.image.*;
import java.util.stream.*;

public class ComparableBufferedImage extends BufferedImage
{
    private boolean changed;
    
    public ComparableBufferedImage(final BufferedImage other) {
        super(other.getWidth(), other.getHeight(), other.getType());
        this.changed = false;
        final int width = other.getWidth();
        final int height = other.getHeight();
        this.setRGB(0, 0, width, height, getPixelData(other), 0, width);
    }
    
    public ComparableBufferedImage(final int width, final int height, final int imageType) {
        super(width, height, imageType);
        this.changed = false;
    }
    
    @Override
    public synchronized void setRGB(final int x, final int y, final int rgb) {
        if (!this.changed && super.getRGB(x, y) != rgb) {
            this.changed = true;
        }
        super.setRGB(x, y, rgb);
    }
    
    @Override
    public void setRGB(final int startX, final int startY, final int w, final int h, final int[] rgbArray, final int offset, final int scansize) {
        super.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
    }
    
    public boolean isChanged() {
        return this.changed;
    }
    
    public void setChanged(final boolean val) {
        this.changed = val;
    }
    
    public boolean identicalTo(final BufferedImage other) {
        return areIdentical(this.getPixelData(), getPixelData(other));
    }
    
    public static boolean areIdentical(final int[] pixels, final int[] otherPixels) {
        return IntStream.range(0, pixels.length).map(i -> ~pixels[i] | otherPixels[i]).allMatch(n -> n == -1);
    }
    
    public int[] getPixelData() {
        return getPixelData(this);
    }
    
    public ComparableBufferedImage copy() {
        return new ComparableBufferedImage(this);
    }
    
    public void copyTo(final BufferedImage other) {
        other.setRGB(0, 0, this.getWidth(), this.getHeight(), this.getPixelData(), 0, this.getWidth());
    }
    
    public static int[] getPixelData(final BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int[] data = new int[width * height];
        image.getRGB(0, 0, width, height, data, 0, width);
        return data;
    }
}
