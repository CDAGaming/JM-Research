package journeymap.client.model;

import journeymap.common.api.feature.*;

public class GridSpecs
{
    public static final GridSpec DEFAULT_DAY;
    public static final GridSpec DEFAULT_NIGHT;
    public static final GridSpec DEFAULT_UNDERGROUND;
    private GridSpec day;
    private GridSpec night;
    private GridSpec underground;
    
    public GridSpecs() {
        this(GridSpecs.DEFAULT_DAY.clone(), GridSpecs.DEFAULT_NIGHT.clone(), GridSpecs.DEFAULT_UNDERGROUND.clone());
    }
    
    public GridSpecs(final GridSpec day, final GridSpec night, final GridSpec underground) {
        this.day = day;
        this.night = night;
        this.underground = underground;
    }
    
    public GridSpec getSpec(final MapView mapView) {
        if (mapView.isNone()) {
            return this.day;
        }
        switch (mapView.mapType) {
            case Day: {
                return this.day;
            }
            case Night: {
                return this.night;
            }
            case Underground: {
                return this.underground;
            }
            default: {
                return this.day;
            }
        }
    }
    
    public void setSpec(final MapView mapView, final GridSpec newSpec) {
        switch (mapView.mapType) {
            case Day: {
                this.day = newSpec.clone();
            }
            case Night: {
                this.night = newSpec.clone();
            }
            case Underground: {
                this.underground = newSpec.clone();
            }
            default: {
                this.day = newSpec.clone();
            }
        }
    }
    
    public GridSpecs clone() {
        return new GridSpecs(this.day.clone(), this.night.clone(), this.underground.clone());
    }
    
    public void updateFrom(final GridSpecs other) {
        this.day = other.day.clone();
        this.night = other.night.clone();
        this.underground = other.underground.clone();
    }
    
    static {
        DEFAULT_DAY = new GridSpec(GridSpec.Style.Squares, 0.5f, 0.5f, 0.5f, 0.5f);
        DEFAULT_NIGHT = new GridSpec(GridSpec.Style.Squares, 0.5f, 0.5f, 1.0f, 0.3f);
        DEFAULT_UNDERGROUND = new GridSpec(GridSpec.Style.Squares, 0.5f, 0.5f, 0.5f, 0.3f);
    }
}
