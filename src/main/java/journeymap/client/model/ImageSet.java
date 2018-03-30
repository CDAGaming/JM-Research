package journeymap.client.model;

import java.awt.image.*;
import journeymap.common.*;
import java.util.*;
import com.google.common.base.*;
import java.io.*;

public abstract class ImageSet
{
    protected final Map<MapView, ImageHolder> imageHolders;
    
    public ImageSet() {
        this.imageHolders = Collections.synchronizedMap(new HashMap<MapView, ImageHolder>(8));
    }
    
    protected abstract ImageHolder getHolder(final MapView p0);
    
    @Override
    public abstract int hashCode();
    
    @Override
    public abstract boolean equals(final Object p0);
    
    public BufferedImage getImage(final MapView mapView) {
        return this.getHolder(mapView).getImage();
    }
    
    public int writeToDiskAsync(final boolean force) {
        return this.writeToDisk(force, true);
    }
    
    public int writeToDisk(final boolean force) {
        return this.writeToDisk(force, false);
    }
    
    private int writeToDisk(final boolean force, final boolean async) {
        final long now = System.currentTimeMillis();
        int count = 0;
        try {
            synchronized (this.imageHolders) {
                for (final ImageHolder imageHolder : this.imageHolders.values()) {
                    if (imageHolder.isDirty() && (force || now - imageHolder.getImageTimestamp() > 10000L)) {
                        imageHolder.writeToDisk(async);
                        ++count;
                    }
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error writing ImageSet to disk: " + t);
        }
        return count;
    }
    
    public boolean updatedSince(final MapView mapView, final long time) {
        synchronized (this.imageHolders) {
            if (mapView == null) {
                for (final ImageHolder holder : this.imageHolders.values()) {
                    if (holder != null && holder.getImageTimestamp() >= time) {
                        return true;
                    }
                }
            }
            else {
                final ImageHolder imageHolder = this.imageHolders.get(mapView);
                if (imageHolder != null && imageHolder.getImageTimestamp() >= time) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void clear() {
        synchronized (this.imageHolders) {
            for (final ImageHolder imageHolder : this.imageHolders.values()) {
                imageHolder.clear();
            }
            this.imageHolders.clear();
        }
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("imageHolders", (Object)this.imageHolders.entrySet()).toString();
    }
    
    protected abstract int getImageSize();
    
    protected ImageHolder addHolder(final MapView mapView, final File imageFile) {
        return this.addHolder(new ImageHolder(mapView, imageFile, this.getImageSize()));
    }
    
    protected ImageHolder addHolder(final ImageHolder imageHolder) {
        this.imageHolders.put(imageHolder.mapView, imageHolder);
        return imageHolder;
    }
}
