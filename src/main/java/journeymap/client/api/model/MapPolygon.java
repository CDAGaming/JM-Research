package journeymap.client.api.model;

import net.minecraft.util.math.*;
import java.util.*;
import com.google.common.base.*;

public final class MapPolygon
{
    private List<BlockPos> points;
    
    public MapPolygon(final BlockPos... points) {
        this(Arrays.asList(points));
    }
    
    public MapPolygon(final List<BlockPos> points) {
        this.setPoints(points);
    }
    
    public List<BlockPos> getPoints() {
        return this.points;
    }
    
    public MapPolygon setPoints(final List<BlockPos> points) {
        if (points.size() < 3) {
            throw new IllegalArgumentException("MapPolygon must have at least 3 points.");
        }
        this.points = Collections.unmodifiableList((List<? extends BlockPos>)points);
        return this;
    }
    
    public Iterator<BlockPos> iterator() {
        return this.points.iterator();
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("points", (Object)this.points).toString();
    }
}
