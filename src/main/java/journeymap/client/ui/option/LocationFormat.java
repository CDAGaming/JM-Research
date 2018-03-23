package journeymap.client.ui.option;

import journeymap.common.*;
import journeymap.client.*;
import journeymap.client.ui.component.*;
import java.util.*;
import journeymap.common.properties.config.*;

public class LocationFormat
{
    private static String[] locationFormatIds;
    private HashMap<String, LocationFormatKeys> idToFormat;
    
    public LocationFormat() {
        this.idToFormat = new HashMap<String, LocationFormatKeys>();
        for (final String id : LocationFormat.locationFormatIds) {
            this.idToFormat.put(id, new LocationFormatKeys(id));
        }
    }
    
    public LocationFormatKeys getFormatKeys(final String id) {
        LocationFormatKeys locationLocationFormatKeys = this.idToFormat.get(id);
        if (locationLocationFormatKeys == null) {
            Journeymap.getLogger().warn("Invalid location format id: " + id);
            locationLocationFormatKeys = this.idToFormat.get(LocationFormat.locationFormatIds[0]);
        }
        return locationLocationFormatKeys;
    }
    
    public String getLabel(final String id) {
        return Constants.getString(this.getFormatKeys(id).label_key);
    }
    
    static {
        LocationFormat.locationFormatIds = new String[] { "xzyv", "xyvz", "xzy", "xyz", "xz" };
    }
    
    public static class IdProvider implements StringField.ValuesProvider
    {
        @Override
        public List<String> getStrings() {
            return Arrays.asList(LocationFormat.locationFormatIds);
        }
        
        @Override
        public String getDefaultString() {
            return LocationFormat.locationFormatIds[0];
        }
    }
    
    public static class LocationFormatKeys
    {
        final String id;
        final String label_key;
        final String verbose_key;
        final String plain_key;
        
        LocationFormatKeys(final String id) {
            this.id = id;
            this.label_key = String.format("jm.common.location_%s_label", id);
            this.verbose_key = String.format("jm.common.location_%s_verbose", id);
            this.plain_key = String.format("jm.common.location_%s_plain", id);
        }
        
        public String format(final boolean verbose, final int x, final int z, final int y, final int vslice) {
            if (verbose) {
                return Constants.getString(this.verbose_key, x, z, y, vslice);
            }
            return Constants.getString(this.plain_key, x, z, y, vslice);
        }
    }
    
    public static class Button extends ListPropertyButton<String>
    {
        LocationFormat locationFormat;
        
        public Button(final StringField valueHolder) {
            super(Arrays.asList(LocationFormat.locationFormatIds), Constants.getString("jm.common.location_format"), valueHolder);
            if (this.locationFormat == null) {
                this.locationFormat = new LocationFormat();
            }
        }
        
        public String getFormattedLabel(final String id) {
            if (this.locationFormat == null) {
                this.locationFormat = new LocationFormat();
            }
            return String.format("%1$s : %2$s %3$s %2$s", this.baseLabel, "\u21d5", this.locationFormat.getLabel(id));
        }
        
        public String getLabel(final String id) {
            return this.locationFormat.getLabel(id);
        }
    }
}
