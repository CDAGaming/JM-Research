package journeymap.client.properties;

import journeymap.client.ui.minimap.EntityDisplay;
import journeymap.client.ui.option.LocationFormat;
import journeymap.common.properties.Category;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;

public abstract class InGameMapProperties extends MapProperties {
    public final EnumField<EntityDisplay> playerDisplay;
    public final BooleanField showPlayerHeading;
    public final EnumField<EntityDisplay> mobDisplay;
    public final BooleanField showMobHeading;
    public final BooleanField showMobs;
    public final BooleanField showAnimals;
    public final BooleanField showVillagers;
    public final BooleanField showPets;
    public final BooleanField showPlayers;
    public final IntegerField fontScale;
    public final BooleanField showWaypointLabels;
    public final BooleanField locationFormatVerbose;
    public final StringField locationFormat;

    protected InGameMapProperties() {
        this.playerDisplay = new EnumField<>(Category.Inherit, "jm.minimap.player_display", EntityDisplay.SmallDots);
        this.showPlayerHeading = new BooleanField(Category.Inherit, "jm.minimap.player_heading", true);
        this.mobDisplay = new EnumField<>(Category.Inherit, "jm.minimap.mob_display", EntityDisplay.SmallDots);
        this.showMobHeading = new BooleanField(Category.Inherit, "jm.minimap.mob_heading", true);
        this.showMobs = new BooleanField(Category.Inherit, "jm.common.show_mobs", true);
        this.showAnimals = new BooleanField(Category.Inherit, "jm.common.show_animals", true);
        this.showVillagers = new BooleanField(Category.Inherit, "jm.common.show_villagers", true);
        this.showPets = new BooleanField(Category.Inherit, "jm.common.show_pets", true);
        this.showPlayers = new BooleanField(Category.Inherit, "jm.common.show_players", true);
        this.fontScale = new IntegerField(Category.Inherit, "jm.common.font_scale", 1, 4, 1);
        this.showWaypointLabels = new BooleanField(Category.Inherit, "jm.minimap.show_waypointlabels", true);
        this.locationFormatVerbose = new BooleanField(Category.Inherit, "jm.common.location_format_verbose", true);
        this.locationFormat = new StringField(Category.Inherit, "jm.common.location_format", LocationFormat.IdProvider.class);
    }
}
