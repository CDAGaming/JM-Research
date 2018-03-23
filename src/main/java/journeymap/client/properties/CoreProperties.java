package journeymap.client.properties;

import journeymap.common.properties.config.*;
import journeymap.client.task.multi.*;
import net.minecraftforge.client.event.*;
import journeymap.client.model.*;
import journeymap.client.log.*;
import journeymap.client.io.*;
import journeymap.common.properties.*;
import net.minecraftforge.fml.client.*;
import java.util.*;
import journeymap.client.cartography.color.*;

public class CoreProperties extends ClientPropertiesBase implements Comparable<CoreProperties>
{
    public static final String PATTERN_COLOR = "^#[a-f0-9]{6}$";
    public final StringField logLevel;
    public final IntegerField autoMapPoll;
    public final IntegerField cacheAnimalsData;
    public final IntegerField cacheMobsData;
    public final IntegerField cachePlayerData;
    public final IntegerField cachePlayersData;
    public final IntegerField cacheVillagersData;
    public final BooleanField announceMod;
    public final BooleanField checkUpdates;
    public final BooleanField recordCacheStats;
    public final IntegerField browserPoll;
    public final StringField themeName;
    public final BooleanField caveIgnoreGlass;
    public final BooleanField mapBathymetry;
    public final BooleanField mapTopography;
    public final BooleanField mapTransparency;
    public final BooleanField mapCaveLighting;
    public final BooleanField mapAntialiasing;
    public final BooleanField mapPlantShadows;
    public final BooleanField mapPlants;
    public final BooleanField mapCrops;
    public final BooleanField mapBlendGrass;
    public final BooleanField mapBlendFoliage;
    public final BooleanField mapBlendWater;
    public final BooleanField mapSurfaceAboveCaves;
    public final IntegerField renderDistanceCaveMax;
    public final IntegerField renderDistanceSurfaceMax;
    public final IntegerField renderDelay;
    public final EnumField<RenderSpec.RevealShape> revealShape;
    public final BooleanField alwaysMapCaves;
    public final BooleanField alwaysMapSurface;
    public final BooleanField tileHighDisplayQuality;
    public final IntegerField maxAnimalsData;
    public final IntegerField maxMobsData;
    public final IntegerField maxPlayersData;
    public final IntegerField maxVillagersData;
    public final BooleanField hideSneakingEntities;
    public final IntegerField radarLateralDistance;
    public final IntegerField radarVerticalDistance;
    public final IntegerField tileRenderType;
    public final BooleanField mappingEnabled;
    public final EnumField<RenderGameOverlayEvent.ElementType> renderOverlayEventTypeName;
    public final BooleanField renderOverlayPreEvent;
    public final StringField optionsManagerViewed;
    public final StringField splashViewed;
    public final GridSpecs gridSpecs;
    public final StringField colorPassive;
    public final StringField colorHostile;
    public final StringField colorPet;
    public final StringField colorVillager;
    public final StringField colorPlayer;
    public final StringField colorSelf;
    public final BooleanField verboseColorPalette;
    private transient HashMap<StringField, Integer> mobColors;
    
    public CoreProperties() {
        this.logLevel = new StringField(ClientCategory.Advanced, "jm.advanced.loglevel", JMLogger.LogLevelStringProvider.class);
        this.autoMapPoll = new IntegerField(ClientCategory.Advanced, "jm.advanced.automappoll", 500, 10000, 2000);
        this.cacheAnimalsData = new IntegerField(ClientCategory.Advanced, "jm.advanced.cache_animals", 1000, 10000, 3100);
        this.cacheMobsData = new IntegerField(ClientCategory.Advanced, "jm.advanced.cache_mobs", 1000, 10000, 3000);
        this.cachePlayerData = new IntegerField(ClientCategory.Advanced, "jm.advanced.cache_player", 500, 2000, 1000);
        this.cachePlayersData = new IntegerField(ClientCategory.Advanced, "jm.advanced.cache_players", 1000, 10000, 2000);
        this.cacheVillagersData = new IntegerField(ClientCategory.Advanced, "jm.advanced.cache_villagers", 1000, 10000, 2200);
        this.announceMod = new BooleanField(ClientCategory.Advanced, "jm.advanced.announcemod", true);
        this.checkUpdates = new BooleanField(ClientCategory.Advanced, "jm.advanced.checkupdates", true);
        this.recordCacheStats = new BooleanField(ClientCategory.Advanced, "jm.advanced.recordcachestats", false);
        this.browserPoll = new IntegerField(ClientCategory.Advanced, "jm.advanced.browserpoll", 1000, 10000, 2000);
        this.themeName = new StringField(ClientCategory.FullMap, "jm.common.ui_theme", ThemeLoader.ThemeValuesProvider.class);
        this.caveIgnoreGlass = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_caveignoreglass", true);
        this.mapBathymetry = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_bathymetry", false);
        this.mapTopography = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_topography", true);
        this.mapTransparency = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_transparency", true);
        this.mapCaveLighting = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_cavelighting", true);
        this.mapAntialiasing = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_antialiasing", true);
        this.mapPlantShadows = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_plantshadows", false);
        this.mapPlants = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_plants", false);
        this.mapCrops = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_crops", true);
        this.mapBlendGrass = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_blendgrass", true);
        this.mapBlendFoliage = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_blendfoliage", true);
        this.mapBlendWater = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_blendwater", false);
        this.mapSurfaceAboveCaves = new BooleanField(ClientCategory.Cartography, "jm.common.map_style_caveshowsurface", true);
        this.renderDistanceCaveMax = new IntegerField(ClientCategory.Cartography, "jm.common.renderdistance_cave_max", 1, 32, 3, 102);
        this.renderDistanceSurfaceMax = new IntegerField(ClientCategory.Cartography, "jm.common.renderdistance_surface_max", 1, 32, 7, 104);
        this.renderDelay = new IntegerField(ClientCategory.Cartography, "jm.common.renderdelay", 0, 10, 2);
        this.revealShape = new EnumField<RenderSpec.RevealShape>(ClientCategory.Cartography, "jm.common.revealshape", RenderSpec.RevealShape.Circle);
        this.alwaysMapCaves = new BooleanField(ClientCategory.Cartography, "jm.common.alwaysmapcaves", false);
        this.alwaysMapSurface = new BooleanField(ClientCategory.Cartography, "jm.common.alwaysmapsurface", false);
        this.tileHighDisplayQuality = new BooleanField(ClientCategory.Advanced, "jm.common.tile_display_quality", true);
        this.maxAnimalsData = new IntegerField(ClientCategory.Advanced, "jm.common.radar_max_animals", 1, 128, 32);
        this.maxMobsData = new IntegerField(ClientCategory.Advanced, "jm.common.radar_max_mobs", 1, 128, 32);
        this.maxPlayersData = new IntegerField(ClientCategory.Advanced, "jm.common.radar_max_players", 1, 128, 32);
        this.maxVillagersData = new IntegerField(ClientCategory.Advanced, "jm.common.radar_max_villagers", 1, 128, 32);
        this.hideSneakingEntities = new BooleanField(ClientCategory.Advanced, "jm.common.radar_hide_sneaking", true);
        this.radarLateralDistance = new IntegerField(ClientCategory.Advanced, "jm.common.radar_lateral_distance", 16, 512, 64);
        this.radarVerticalDistance = new IntegerField(ClientCategory.Advanced, "jm.common.radar_vertical_distance", 8, 256, 16);
        this.tileRenderType = new IntegerField(ClientCategory.Advanced, "jm.advanced.tile_render_type", 1, 4, 1);
        this.mappingEnabled = new BooleanField(Category.Hidden, "", true);
        this.renderOverlayEventTypeName = new EnumField<RenderGameOverlayEvent.ElementType>(Category.Hidden, "", RenderGameOverlayEvent.ElementType.ALL);
        this.renderOverlayPreEvent = new BooleanField(Category.Hidden, "", true);
        this.optionsManagerViewed = new StringField(Category.Hidden, "", null);
        this.splashViewed = new StringField(Category.Hidden, "", null);
        this.gridSpecs = new GridSpecs();
        this.colorPassive = new StringField(Category.Hidden, "jm.common.radar_color_passive", null, "#bbbbbb").pattern("^#[a-f0-9]{6}$");
        this.colorHostile = new StringField(Category.Hidden, "jm.common.radar_color_hostile", null, "#ff0000").pattern("^#[a-f0-9]{6}$");
        this.colorPet = new StringField(Category.Hidden, "jm.common.radar_color_pet", null, "#0077ff").pattern("^#[a-f0-9]{6}$");
        this.colorVillager = new StringField(Category.Hidden, "jm.common.radar_color_villager", null, "#88e188").pattern("^#[a-f0-9]{6}$");
        this.colorPlayer = new StringField(Category.Hidden, "jm.common.radar_color_player", null, "#ffffff").pattern("^#[a-f0-9]{6}$");
        this.colorSelf = new StringField(Category.Hidden, "jm.common.radar_color_self", null, "#0000ff").pattern("^#[a-f0-9]{6}$");
        this.verboseColorPalette = new BooleanField(Category.Hidden, "", false);
        this.mobColors = new HashMap<StringField, Integer>(6);
    }
    
    @Override
    public String getName() {
        return "core";
    }
    
    @Override
    public int compareTo(final CoreProperties other) {
        return Integer.valueOf(this.hashCode()).compareTo(Integer.valueOf(other.hashCode()));
    }
    
    @Override
    public <T extends PropertiesBase> void updateFrom(final T otherInstance) {
        super.updateFrom(otherInstance);
        if (otherInstance instanceof CoreProperties) {
            this.gridSpecs.updateFrom(((CoreProperties)otherInstance).gridSpecs);
        }
        this.mobColors.clear();
    }
    
    @Override
    public boolean isValid(final boolean fix) {
        boolean valid = super.isValid(fix);
        if (FMLClientHandler.instance().getClient() != null) {
            final int gameRenderDistance = FMLClientHandler.instance().getClient().field_71474_y.field_151451_c;
            for (final IntegerField prop : Arrays.asList(this.renderDistanceCaveMax, this.renderDistanceSurfaceMax)) {
                if (prop.get() > gameRenderDistance) {
                    this.warn(String.format("Render distance %s is less than %s", gameRenderDistance, prop.getDeclaredField()));
                    if (fix) {
                        prop.set(gameRenderDistance);
                    }
                    else {
                        valid = false;
                    }
                }
            }
        }
        return valid;
    }
    
    public int getColor(final StringField colorField) {
        Integer color = this.mobColors.get(colorField);
        if (color == null) {
            color = RGB.hexToInt(colorField.get());
            this.mobColors.put(colorField, color);
        }
        return color;
    }
}
