package journeymap.common.network.model;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;

public class Location implements Serializable {
    public static final Gson GSON;

    static {
        GSON = new GsonBuilder().create();
    }

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
        return MoreObjects.toStringHelper(this).add("x", this.x).add("y", this.y).add("z", this.z).add("dim", this.dim).toString();
    }
}
