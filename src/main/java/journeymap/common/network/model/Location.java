package journeymap.common.network.model;

import java.io.*;
import com.google.common.base.*;
import com.google.gson.*;

public class Location implements Serializable
{
    public static final Gson GSON;
    private double x;
    private double y;
    private double z;
    private int dim;
    
    public Location() {
    }
    
    public Location(final double x, final double y, final double z, final int dim) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
    }
    
    public double getX() {
        return this.x;
    }
    
    public double getY() {
        return this.y;
    }
    
    public double getZ() {
        return this.z;
    }
    
    public int getDim() {
        return this.dim;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("x", this.x).add("y", this.y).add("z", this.z).add("dim", this.dim).toString();
    }
    
    static {
        GSON = new GsonBuilder().create();
    }
}
