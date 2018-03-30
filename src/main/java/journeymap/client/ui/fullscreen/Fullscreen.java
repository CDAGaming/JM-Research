package journeymap.client.ui.fullscreen;

import net.minecraft.util.*;
import journeymap.client.render.map.*;
import journeymap.client.ui.fullscreen.layer.*;
import org.apache.logging.log4j.*;
import journeymap.client.ui.option.*;
import net.minecraft.client.entity.*;
import journeymap.common.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.api.util.*;
import net.minecraft.entity.player.*;
import journeymap.client.ui.*;
import net.minecraft.client.renderer.*;
import journeymap.common.api.feature.*;
import journeymap.common.log.*;
import net.minecraft.client.*;
import journeymap.client.ui.theme.*;
import journeymap.client.data.*;
import journeymap.client.*;
import net.minecraft.util.text.*;
import journeymap.common.version.*;
import journeymap.client.io.*;
import journeymap.client.log.*;
import journeymap.client.task.multi.*;
import journeymap.client.ui.dialog.*;
import journeymap.client.task.main.*;
import journeymap.client.ui.component.*;
import net.minecraft.client.gui.*;
import journeymap.client.model.*;
import journeymap.client.feature.*;
import org.lwjgl.input.*;
import javax.annotation.*;
import java.io.*;
import java.awt.geom.*;
import journeymap.client.waypoint.*;
import journeymap.client.render.draw.*;
import journeymap.client.render.texture.*;
import journeymap.client.api.model.*;
import journeymap.client.api.impl.*;
import journeymap.client.api.display.*;
import net.minecraft.util.math.*;
import java.util.*;
import journeymap.client.ui.minimap.*;
import journeymap.client.properties.*;

public class Fullscreen extends JmUI implements ITabCompleter
{
    static final MapState state;
    static final GridRenderer gridRenderer;
    final WaypointDrawStepFactory waypointRenderer;
    final RadarDrawStepFactory radarRenderer;
    final LayerDelegate layerDelegate;
    FullMapProperties fullMapProperties;
    CoreProperties coreProperties;
    boolean firstLayoutPass;
    Boolean isScrolling;
    int msx;
    int msy;
    int mx;
    int my;
    Logger logger;
    MapChat chat;
    ThemeButton buttonFollow;
    ThemeButton buttonZoomIn;
    ThemeButton buttonZoomOut;
    ThemeButton buttonDay;
    ThemeButton buttonNight;
    ThemeButton buttonTopo;
    ThemeButton buttonLayers;
    ThemeButton buttonCaves;
    ThemeButton buttonAlert;
    ThemeButton buttonOptions;
    ThemeButton buttonClose;
    ThemeButton buttonTheme;
    ThemeButton buttonWaypointManager;
    ThemeButton buttonMobs;
    ThemeButton buttonAnimals;
    ThemeButton buttonPets;
    ThemeButton buttonVillagers;
    ThemeButton buttonPlayers;
    ThemeButton buttonGrid;
    ThemeButton buttonKeys;
    ThemeButton buttonAutomap;
    ThemeButton buttonSavemap;
    ThemeButton buttonDeletemap;
    ThemeButton buttonDisable;
    ThemeButton buttonResetPalette;
    ThemeButton buttonBrowser;
    ThemeButton buttonAbout;
    ThemeButton buttonFeatures;
    ThemeToolbar mapTypeToolbar;
    ThemeToolbar optionsToolbar;
    ThemeToolbar menuToolbar;
    ThemeToolbar zoomToolbar;
    int bgColor;
    Theme.LabelSpec statusLabelSpec;
    StatTimer drawScreenTimer;
    StatTimer drawMapTimer;
    StatTimer drawMapTimerWithRefresh;
    LocationFormat locationFormat;
    List<Overlay> tempOverlays;
    private IntSliderButton sliderCaveLayer;
    private List<String> autoMapOnTooltip;
    private List<String> autoMapOffTooltip;
    private Rectangle2D.Double mapTypeToolbarBounds;
    private Rectangle2D.Double optionsToolbarBounds;
    private Rectangle2D.Double menuToolbarBounds;
    private EntityPlayerSP player;
    
    public Fullscreen() {
        super(null);
        this.waypointRenderer = new WaypointDrawStepFactory();
        this.radarRenderer = new RadarDrawStepFactory();
        this.fullMapProperties = Journeymap.getClient().getFullMapProperties();
        this.coreProperties = Journeymap.getClient().getCoreProperties();
        this.firstLayoutPass = true;
        this.isScrolling = false;
        this.logger = Journeymap.getLogger();
        this.bgColor = 2236962;
        this.drawScreenTimer = StatTimer.get("Fullscreen.drawScreen");
        this.drawMapTimer = StatTimer.get("Fullscreen.drawScreen.drawMap", 50);
        this.drawMapTimerWithRefresh = StatTimer.get("Fullscreen.drawMap+refreshState", 5);
        this.locationFormat = new LocationFormat();
        this.tempOverlays = new ArrayList<Overlay>();
        this.player = Journeymap.clientPlayer();
        this.field_146297_k = FMLClientHandler.instance().getClient();
        this.layerDelegate = new LayerDelegate(this);
        Fullscreen.state.follow.set(true);
    }
    
    public static synchronized MapState state() {
        return Fullscreen.state;
    }
    
    public static synchronized UIState uiState() {
        return Fullscreen.gridRenderer.getUIState();
    }
    
    public void reset() {
        Fullscreen.state.requireRefresh();
        Fullscreen.gridRenderer.clear();
        this.field_146292_n.clear();
    }
    
    @Override
    public void func_73866_w_() {
        this.fullMapProperties = Journeymap.getClient().getFullMapProperties();
        Fullscreen.state.requireRefresh();
        Fullscreen.state.refresh(this.field_146297_k, (EntityPlayer)this.player, this.fullMapProperties);
        MapView mapView = Fullscreen.state.getMapView();
        if (!mapView.isAllowed() || mapView.isNone()) {
            mapView = Fullscreen.state.toggleMapType();
        }
        Keyboard.enableRepeatEvents(true);
        if (mapView.isNone()) {
            Fullscreen.gridRenderer.clear();
        }
        else if (mapView.dimension != this.player.field_71093_bK) {
            Fullscreen.gridRenderer.clear();
        }
        this.initButtons();
        final String thisVersion = Journeymap.JM_VERSION.toString();
        final String splashViewed = Journeymap.getClient().getCoreProperties().splashViewed.get();
        if (splashViewed == null || !thisVersion.equals(splashViewed)) {
            UIManager.INSTANCE.openSplash(this);
        }
    }
    
    @Override
    public void func_73863_a(final int mouseX, final int mouseY, final float partialTicks) {
        try {
            this.func_146278_c(0);
            this.drawMap();
            this.drawScreenTimer.start();
            this.layoutButtons();
            List<String> tooltip = null;
            if (this.firstLayoutPass) {
                this.layoutButtons();
                this.updateMapType(Fullscreen.state.getMapView());
                this.firstLayoutPass = false;
            }
            else {
                for (int k = 0; k < this.field_146292_n.size(); ++k) {
                    final GuiButton guibutton = this.field_146292_n.get(k);
                    guibutton.func_191745_a(this.field_146297_k, mouseX, mouseY, partialTicks);
                    if (tooltip == null && guibutton instanceof Button) {
                        final Button button = (Button)guibutton;
                        if (button.mouseOver(this.mx, this.my)) {
                            tooltip = button.getTooltip();
                        }
                    }
                }
            }
            if (this.chat != null) {
                this.chat.func_73863_a(mouseX, mouseY, partialTicks);
            }
            if (tooltip != null && !tooltip.isEmpty()) {
                this.drawHoveringText(tooltip, this.mx, this.my, this.getFontRenderer());
                RenderHelper.func_74518_a();
            }
            this.showDisabled(Feature.Display.Fullscreen, Fullscreen.state.getMapView().dimension, mouseX, mouseY);
        }
        catch (Throwable e) {
            this.logger.error("Unexpected exception in jm.fullscreen.drawScreen(): " + LogFormatter.toString(e));
            UIManager.INSTANCE.closeAll();
        }
        finally {
            this.drawScreenTimer.stop();
        }
    }
    
    protected void func_146284_a(final GuiButton guibutton) {
        if (guibutton instanceof ThemeToolbar) {
            return;
        }
        if (guibutton instanceof OnOffButton) {
            ((OnOffButton)guibutton).toggle();
        }
        if (this.optionsToolbar.contains(guibutton)) {
            this.refreshState();
        }
    }
    
    @Override
    public void func_146280_a(final Minecraft minecraft, final int width, final int height) {
        super.func_146280_a(minecraft, width, height);
        Fullscreen.state.requireRefresh();
        if (this.chat == null) {
            this.chat = new MapChat("", true);
        }
        if (this.chat != null) {
            this.chat.func_146280_a(minecraft, width, height);
        }
        this.func_73866_w_();
        this.refreshState();
        this.drawMap();
    }
    
    void initButtons() {
        if (this.field_146292_n.isEmpty()) {
            this.firstLayoutPass = true;
            final Theme theme = ThemeLoader.getCurrentTheme();
            final MapView mapView = Fullscreen.state.getMapView();
            this.bgColor = theme.fullscreen.background.getColor();
            this.statusLabelSpec = theme.fullscreen.statusLabel;
            this.buttonDay = new ThemeToggle(theme, "jm.fullscreen.map_day", "day");
            this.buttonNight = new ThemeToggle(theme, "jm.fullscreen.map_night", "night");
            this.buttonTopo = new ThemeToggle(theme, "jm.fullscreen.map_topo", "topo");
            this.buttonLayers = new ThemeToggle(theme, "jm.fullscreen.map_cave_layers", "layers");
            this.buttonDay.setToggled(mapView.isDay(), false);
            this.buttonDay.setEnabled(Fullscreen.state.isDayMappingAllowed());
            this.buttonDay.showDisabledOnHover(true);
            this.buttonDay.setStaysOn(true);
            this.buttonDay.addToggleListener((button, toggled) -> {
                if (button.field_146124_l) {
                    this.updateMapType(MapView.day(Fullscreen.state.getDimension()));
                }
                return button.field_146124_l;
            });
            this.buttonNight.setToggled(mapView.isNight(), false);
            this.buttonNight.setEnabled(Fullscreen.state.isNightMappingAllowed());
            this.buttonNight.showDisabledOnHover(true);
            this.buttonNight.setStaysOn(true);
            this.buttonNight.addToggleListener((button, toggled) -> {
                if (button.field_146124_l) {
                    this.updateMapType(MapView.night(Fullscreen.state.getDimension()));
                }
                return button.field_146124_l;
            });
            this.buttonTopo.setDrawButton(this.coreProperties.mapTopography.get());
            this.buttonTopo.setEnabled(Fullscreen.state.isTopoMappingAllowed());
            this.buttonTopo.setToggled(mapView.isTopo(), false);
            this.buttonTopo.showDisabledOnHover(true);
            this.buttonTopo.setStaysOn(true);
            this.buttonTopo.addToggleListener((button, toggled) -> {
                if (button.field_146124_l) {
                    this.updateMapType(MapView.topo(Fullscreen.state.getDimension()));
                }
                return button.field_146124_l;
            });
            this.buttonLayers.setToggled(mapView.isUnderground(), false);
            this.buttonLayers.setEnabled(Fullscreen.state.isCaveMappingAllowed());
            this.buttonLayers.showDisabledOnHover(true);
            this.buttonLayers.setStaysOn(true);
            this.buttonLayers.addToggleListener((button, toggled) -> {
                if (button.field_146124_l) {
                    this.updateMapType(MapView.underground(DataCache.getPlayer()));
                }
                return button.field_146124_l;
            });
            final FontRenderer fontRenderer = this.getFontRenderer();
            (this.sliderCaveLayer = new IntSliderButton(Fullscreen.state.getLastSlice(), Constants.getString("jm.fullscreen.map_cave_layers.button") + " ", "")).func_175211_a(this.sliderCaveLayer.getFitWidth(fontRenderer) + fontRenderer.func_78256_a("0"));
            this.sliderCaveLayer.setDefaultStyle(false);
            this.sliderCaveLayer.setDrawBackground(true);
            final Theme.Control.ButtonSpec buttonSpec = this.buttonLayers.getButtonSpec();
            this.sliderCaveLayer.setBackgroundColors(buttonSpec.buttonDisabled.getColor(), buttonSpec.buttonOff.getColor(), buttonSpec.buttonOff.getColor());
            this.sliderCaveLayer.setLabelColors(buttonSpec.iconHoverOff.getColor(), buttonSpec.iconHoverOn.getColor(), buttonSpec.iconDisabled.getColor());
            this.sliderCaveLayer.addClickListener(button -> {
                Fullscreen.state.setMapType(MapView.underground(this.sliderCaveLayer.getValue(), Fullscreen.state.getDimension()));
                this.refreshState();
                return true;
            });
            this.field_146292_n.add(this.sliderCaveLayer);
            (this.buttonFollow = new ThemeButton(theme, "jm.fullscreen.follow", "follow")).addToggleListener((button, toggled) -> {
                this.toggleFollow();
                return true;
            });
            this.buttonZoomIn = new ThemeButton(theme, "jm.fullscreen.zoom_in", "zoomin");
            final ThemeButton buttonZoomIn = this.buttonZoomIn;
            final int intValue = this.fullMapProperties.zoomLevel.get();
            Fullscreen.state.getClass();
            buttonZoomIn.setEnabled(intValue < 5);
            this.buttonZoomIn.addToggleListener((button, toggled) -> {
                this.zoomIn();
                return true;
            });
            this.buttonZoomOut = new ThemeButton(theme, "jm.fullscreen.zoom_out", "zoomout");
            final ThemeButton buttonZoomOut = this.buttonZoomOut;
            final int intValue2 = this.fullMapProperties.zoomLevel.get();
            Fullscreen.state.getClass();
            buttonZoomOut.setEnabled(intValue2 > 0);
            this.buttonZoomOut.addToggleListener((button, toggled) -> {
                this.zoomOut();
                return true;
            });
            (this.buttonWaypointManager = new ThemeButton(theme, "jm.waypoint.waypoints_button", "waypoints")).addToggleListener((button, toggled) -> {
                UIManager.INSTANCE.openWaypointManager(null, this);
                return true;
            });
            (this.buttonTheme = new ThemeButton(theme, "jm.common.ui_theme", "theme")).addToggleListener((button, toggled) -> {
                ThemeLoader.loadNextTheme();
                UIManager.INSTANCE.getMiniMap().reset();
                this.field_146292_n.clear();
                return false;
            });
            final String[] tooltips = { TextFormatting.ITALIC + Constants.getString("jm.common.ui_theme_name", theme.name), TextFormatting.ITALIC + Constants.getString("jm.common.ui_theme_author", theme.author) };
            this.buttonTheme.setAdditionalTooltips(Arrays.asList(tooltips));
            (this.buttonOptions = new ThemeButton(theme, "jm.common.options_button", "options")).addToggleListener((button, toggled) -> {
                try {
                    UIManager.INSTANCE.openOptionsManager();
                    this.field_146292_n.clear();
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            });
            final String versionAvailable = Constants.getString("jm.common.new_version_available", VersionCheck.getVersionAvailable());
            (this.buttonAlert = new ThemeButton(theme, versionAvailable, versionAvailable, false, "alert")).setDrawButton(VersionCheck.getVersionIsChecked() && !VersionCheck.getVersionIsCurrent());
            this.buttonAlert.setToggled(true);
            this.buttonAlert.addToggleListener((button, toggled) -> {
                FullscreenActions.launchDownloadWebsite();
                this.buttonAlert.setDrawButton(false);
                return true;
            });
            (this.buttonClose = new ThemeButton(theme, "jm.common.close", "close")).addToggleListener((button, toggled) -> {
                UIManager.INSTANCE.closeAll();
                return true;
            });
            (this.buttonCaves = new ThemeToggle(theme, "jm.common.show_caves", "caves", this.fullMapProperties.showCaves)).setTooltip(Constants.getString("jm.common.show_caves.tooltip"));
            this.buttonCaves.setDrawButton(Fullscreen.state.isCaveMappingAllowed());
            this.buttonCaves.showDisabledOnHover(true);
            final EntityDTO player;
            this.buttonCaves.addToggleListener((button, toggled) -> {
                player = DataCache.getPlayer();
                if (toggled && player.underground) {
                    this.updateMapType(MapView.underground(player));
                }
                return true;
            });
            (this.buttonMobs = new ThemeToggle(theme, "jm.common.show_mobs", "monsters", this.fullMapProperties.showMobs)).showDisabledOnHover(true);
            this.buttonMobs.setTooltip(Constants.getString("jm.common.show_mobs.tooltip"));
            (this.buttonAnimals = new ThemeToggle(theme, "jm.common.show_animals", "animals", this.fullMapProperties.showAnimals)).showDisabledOnHover(true);
            this.buttonAnimals.setTooltip(Constants.getString("jm.common.show_animals.tooltip"));
            (this.buttonPets = new ThemeToggle(theme, "jm.common.show_pets", "pets", this.fullMapProperties.showPets)).showDisabledOnHover(true);
            this.buttonPets.setTooltip(Constants.getString("jm.common.show_pets.tooltip"));
            (this.buttonVillagers = new ThemeToggle(theme, "jm.common.show_villagers", "villagers", this.fullMapProperties.showVillagers)).showDisabledOnHover(true);
            this.buttonVillagers.setTooltip(Constants.getString("jm.common.show_villagers.tooltip"));
            (this.buttonPlayers = new ThemeToggle(theme, "jm.common.show_players", "players", this.fullMapProperties.showPlayers)).setTooltip(Constants.getString("jm.common.show_players.tooltip"));
            this.buttonPlayers.showDisabledOnHover(true);
            this.buttonPlayers.setDrawButton(!FMLClientHandler.instance().getClient().func_71356_B());
            (this.buttonGrid = new ThemeToggle(theme, "jm.common.show_grid", "grid", this.fullMapProperties.showGrid)).setTooltip(Constants.getString("jm.common.show_grid_shift.tooltip"));
            this.buttonGrid.setTooltip(Constants.getString("jm.common.show_grid_shift.tooltip"));
            final boolean shiftDown;
            this.buttonGrid.addToggleListener((button, toggled) -> {
                shiftDown = (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
                if (shiftDown) {
                    UIManager.INSTANCE.openGridEditor(this);
                    this.buttonGrid.setValue(true);
                    return false;
                }
                else {
                    return true;
                }
            });
            (this.buttonKeys = new ThemeToggle(theme, "jm.common.show_keys", "keys", this.fullMapProperties.showKeys)).setTooltip(Constants.getString("jm.common.show_keys.tooltip"));
            (this.buttonAbout = new ThemeButton(theme, "jm.common.splash_about", "about")).addToggleListener((button, toggled) -> {
                UIManager.INSTANCE.openSplash(this);
                return true;
            });
            (this.buttonFeatures = new ThemeButton(theme, "jm.common.features", "features")).setTooltip(Constants.getString("jm.common.features.tooltip"));
            this.buttonFeatures.addToggleListener((button, toggled) -> {
                UIManager.INSTANCE.open(FeatureDialog.class, this);
                return true;
            });
            MapSaver mapSaver;
            (this.buttonSavemap = new ThemeButton(theme, "jm.common.save_map", "savemap")).addToggleListener((button, toggled) -> {
                this.buttonSavemap.setEnabled(false);
                try {
                    mapSaver = new MapSaver(Fullscreen.state.getWorldDir(), Fullscreen.state.getMapView());
                    if (mapSaver.isValid()) {
                        Journeymap.getClient().toggleTask(SaveMapTask.Manager.class, true, mapSaver);
                        ChatLog.announceI18N("jm.common.save_filename", mapSaver.getSaveFileName());
                    }
                }
                finally {
                    this.buttonSavemap.setToggled(false);
                    this.buttonSavemap.setEnabled(true);
                }
                return true;
            });
            this.buttonBrowser = new ThemeButton(theme, "jm.common.use_browser", "browser");
            final boolean webMapEnabled = Journeymap.getClient().getWebMapProperties().enabled.get();
            this.buttonBrowser.setEnabled(webMapEnabled);
            this.buttonBrowser.setDrawButton(webMapEnabled);
            this.buttonBrowser.addToggleListener((button, toggled) -> {
                FullscreenActions.launchLocalhost();
                return true;
            });
            final boolean automapRunning = Journeymap.getClient().isTaskManagerEnabled(MapRegionTask.Manager.class);
            final String autoMapOn = Constants.getString("jm.common.automap_stop_title");
            final String autoMapOff = Constants.getString("jm.common.automap_title");
            this.autoMapOnTooltip = (List<String>)fontRenderer.func_78271_c(Constants.getString("jm.common.automap_stop_text"), 200);
            this.autoMapOffTooltip = (List<String>)fontRenderer.func_78271_c(Constants.getString("jm.common.automap_text"), 200);
            (this.buttonAutomap = new ThemeToggle(theme, autoMapOn, autoMapOff, "automap")).setEnabled(FMLClientHandler.instance().getClient().func_71356_B() && Journeymap.getClient().getCoreProperties().mappingEnabled.get());
            this.buttonAutomap.setToggled(automapRunning, false);
            this.buttonAutomap.addToggleListener((button, toggled) -> {
                if (toggled) {
                    UIManager.INSTANCE.open(AutoMapConfirmation.class, this);
                }
                else {
                    Journeymap.getClient().toggleTask(MapRegionTask.Manager.class, false, null);
                    this.buttonAutomap.setToggled(false, false);
                    this.field_146292_n.clear();
                }
                return true;
            });
            (this.buttonDeletemap = new ThemeButton(theme, "jm.common.deletemap_title", "delete")).setAdditionalTooltips(fontRenderer.func_78271_c(Constants.getString("jm.common.deletemap_text"), 200));
            this.buttonDeletemap.addToggleListener((button, toggled) -> {
                UIManager.INSTANCE.open(DeleteMapConfirmation.class, this);
                return false;
            });
            (this.buttonDisable = new ThemeToggle(theme, "jm.common.enable_mapping_false", "disable")).addToggleListener((button, toggled) -> {
                Journeymap.getClient().getCoreProperties().mappingEnabled.set(Boolean.valueOf(!toggled));
                if (Journeymap.getClient().getCoreProperties().mappingEnabled.get()) {
                    DataCache.INSTANCE.invalidateChunkMDCache();
                    ChatLog.announceI18N("jm.common.enable_mapping_true_text", new Object[0]);
                }
                else {
                    Journeymap.getClient().stopMapping();
                    BlockMD.reset();
                    ChatLog.announceI18N("jm.common.enable_mapping_false_text", new Object[0]);
                }
                return true;
            });
            (this.buttonResetPalette = new ThemeButton(theme, "jm.common.colorreset_title", "reset")).setAdditionalTooltips(fontRenderer.func_78271_c(Constants.getString("jm.common.colorreset_text"), 200));
            this.buttonResetPalette.addToggleListener((button, toggled) -> {
                Journeymap.getClient().queueMainThreadTask(new EnsureCurrentColorsTask(true, true));
                return false;
            });
            (this.mapTypeToolbar = new ThemeToolbar(theme, new Button[] { this.buttonLayers, this.buttonTopo, this.buttonNight, this.buttonDay })).addAllButtons(this);
            (this.optionsToolbar = new ThemeToolbar(theme, new Button[] { this.buttonCaves, this.buttonMobs, this.buttonAnimals, this.buttonPets, this.buttonVillagers, this.buttonPlayers, this.buttonGrid, this.buttonKeys })).addAllButtons(this);
            this.optionsToolbar.field_146125_m = false;
            (this.menuToolbar = new ThemeToolbar(theme, new Button[] { this.buttonWaypointManager, this.buttonFeatures, this.buttonOptions, this.buttonAbout, this.buttonBrowser, this.buttonTheme, this.buttonResetPalette, this.buttonDeletemap, this.buttonSavemap, this.buttonAutomap, this.buttonDisable })).addAllButtons(this);
            this.menuToolbar.field_146125_m = false;
            (this.zoomToolbar = new ThemeToolbar(theme, new Button[] { this.buttonFollow, this.buttonZoomIn, this.buttonZoomOut })).setLayout(ButtonList.Layout.Vertical, ButtonList.Direction.LeftToRight);
            this.zoomToolbar.addAllButtons(this);
            this.field_146292_n.add(this.buttonAlert);
            this.field_146292_n.add(this.buttonClose);
        }
    }
    
    @Override
    protected void layoutButtons() {
        if (this.buttonDay != null && !this.buttonDay.hasValidTextures()) {
            this.field_146292_n.clear();
        }
        if (this.field_146292_n.isEmpty()) {
            this.initButtons();
        }
        this.menuToolbar.setDrawToolbar(!this.isChatOpen());
        MapView mapView = Fullscreen.state.getMapView();
        if (!mapView.isAllowed() || mapView.isNone()) {
            mapView = Fullscreen.state.toggleMapType();
        }
        final boolean fullscreenEnabled = ClientFeatures.instance().isAllowed(Feature.Display.Fullscreen, mapView.dimension);
        if (!fullscreenEnabled) {
            this.mapTypeToolbar.getButtonList().setDrawButtons(false);
            this.optionsToolbar.getButtonList().setDrawButtons(false);
            this.zoomToolbar.getButtonList().setDrawButtons(false);
        }
        else {
            this.buttonDay.setEnabled(Fullscreen.state.isDayMappingAllowed());
            this.buttonDay.setToggled(this.buttonDay.field_146124_l && mapView.isDay());
            this.buttonNight.setEnabled(Fullscreen.state.isNightMappingAllowed());
            this.buttonNight.setToggled(this.buttonNight.field_146124_l && mapView.isNight());
            this.buttonTopo.setEnabled(Fullscreen.state.isTopoMappingAllowed());
            this.buttonTopo.setToggled(this.buttonTopo.field_146124_l && mapView.isTopo());
            this.buttonCaves.setEnabled(Fullscreen.state.isCaveMappingAllowed());
            this.buttonCaves.setToggled(this.buttonCaves.field_146124_l && mapView.isUnderground());
            this.buttonMobs.setEnabled(ClientFeatures.instance().isAllowed(Feature.Radar.HostileMob, Fullscreen.state.getDimension()));
            this.buttonAnimals.setEnabled(ClientFeatures.instance().isAllowed(Feature.Radar.PassiveMob, Fullscreen.state.getDimension()));
            this.buttonPets.setEnabled(ClientFeatures.instance().isAllowed(Feature.Radar.PassiveMob, Fullscreen.state.getDimension()));
            this.buttonVillagers.setEnabled(ClientFeatures.instance().isAllowed(Feature.Radar.NPC, Fullscreen.state.getDimension()));
            this.buttonPlayers.setEnabled(!this.field_146297_k.func_71356_B() && ClientFeatures.instance().isAllowed(Feature.Radar.Player, Fullscreen.state.getDimension()));
            this.buttonFollow.setEnabled(!Fullscreen.state.follow.get());
        }
        final boolean automapRunning = Journeymap.getClient().isTaskManagerEnabled(MapRegionTask.Manager.class);
        final boolean mappingEnabled = Journeymap.getClient().getCoreProperties().mappingEnabled.get();
        this.buttonAutomap.setToggled(automapRunning, false);
        this.buttonAutomap.setEnabled(mappingEnabled && fullscreenEnabled);
        this.buttonAutomap.setAdditionalTooltips(automapRunning ? this.autoMapOnTooltip : this.autoMapOffTooltip);
        final boolean webMapEnabled = Journeymap.getClient().getWebMapProperties().enabled.get();
        this.buttonBrowser.setEnabled(webMapEnabled && mappingEnabled && ClientFeatures.instance().isAllowed(Feature.Display.Webmap, Fullscreen.state.getDimension()));
        this.buttonBrowser.setDrawButton(webMapEnabled);
        this.buttonSavemap.setEnabled(fullscreenEnabled);
        this.buttonResetPalette.setEnabled(mappingEnabled);
        this.buttonDeletemap.setEnabled(true);
        this.buttonDisable.setEnabled(true);
        this.buttonDisable.setToggled(!mappingEnabled, false);
        final int padding = this.mapTypeToolbar.getToolbarSpec().padding;
        this.zoomToolbar.layoutCenteredVertical(this.zoomToolbar.getHMargin(), this.field_146295_m / 2, true, padding);
        final int topY = this.mapTypeToolbar.getVMargin();
        final int margin = this.mapTypeToolbar.getHMargin();
        this.buttonClose.leftOf(this.field_146294_l - this.zoomToolbar.getHMargin()).below(this.mapTypeToolbar.getVMargin());
        this.buttonAlert.leftOf(this.field_146294_l - this.zoomToolbar.getHMargin()).below(this.buttonClose, padding);
        final int toolbarsWidth = this.mapTypeToolbar.getWidth() + this.optionsToolbar.getWidth() + margin + padding;
        final int startX = (this.field_146294_l - toolbarsWidth) / 2;
        Rectangle2D.Double oldBounds = this.mapTypeToolbar.getBounds();
        this.mapTypeToolbar.layoutHorizontal(startX + this.mapTypeToolbar.getWidth(), topY, false, padding);
        if (!this.mapTypeToolbar.getBounds().equals(oldBounds)) {
            this.mapTypeToolbarBounds = null;
        }
        oldBounds = this.optionsToolbar.getBounds();
        this.optionsToolbar.layoutHorizontal(this.mapTypeToolbar.getRightX() + margin, topY, true, padding);
        this.optionsToolbar.field_146125_m = true;
        if (!this.optionsToolbar.getBounds().equals(oldBounds)) {
            this.optionsToolbarBounds = null;
        }
        oldBounds = this.menuToolbar.getBounds();
        this.menuToolbar.layoutCenteredHorizontal(this.field_146294_l / 2, this.field_146295_m - this.menuToolbar.field_146121_g - this.menuToolbar.getVMargin(), true, padding);
        if (!this.menuToolbar.getBounds().equals(oldBounds)) {
            this.menuToolbarBounds = null;
        }
        final boolean showCaveLayers = this.buttonLayers.getToggled();
        if (showCaveLayers) {
            final Rectangle2D.Double bounds = this.getMapTypeToolbarBounds();
            if (bounds != null) {
                final boolean alreadyVisible = this.sliderCaveLayer.isVisible() && Mouse.isButtonDown(0);
                this.sliderCaveLayer.setDrawButton(alreadyVisible || bounds.contains(this.mx, this.my));
            }
        }
        else {
            this.sliderCaveLayer.setDrawButton(false);
        }
        if (this.sliderCaveLayer.isVisible()) {
            this.sliderCaveLayer.below(this.buttonLayers, 1).centerHorizontalOn(this.buttonLayers.getCenterX());
            final int slice = this.sliderCaveLayer.getValue();
            final int minY = Math.max(slice << 4, 0);
            final int maxY = (slice + 1 << 4) - 1;
            this.sliderCaveLayer.setTooltip(Constants.getString("jm.fullscreen.map_cave_layers.button.tooltip", minY, maxY));
        }
    }
    
    @Nullable
    public Rectangle2D.Double getOptionsToolbarBounds() {
        if (this.optionsToolbar != null && this.optionsToolbar.isVisible()) {
            final Rectangle2D.Double unscaled = this.optionsToolbar.getBounds();
            this.optionsToolbarBounds = new Rectangle2D.Double(unscaled.x * this.scaleFactor, unscaled.y * this.scaleFactor, unscaled.width * this.scaleFactor, unscaled.height * this.scaleFactor);
        }
        return this.optionsToolbarBounds;
    }
    
    @Nullable
    public Rectangle2D.Double getMenuToolbarBounds() {
        if (this.menuToolbar != null && this.menuToolbar.isVisible()) {
            final Rectangle2D.Double unscaled = this.menuToolbar.getBounds();
            this.menuToolbarBounds = new Rectangle2D.Double(unscaled.x * this.scaleFactor, unscaled.y * this.scaleFactor, unscaled.width * this.scaleFactor, unscaled.height * this.scaleFactor);
        }
        return this.menuToolbarBounds;
    }
    
    @Nullable
    public Rectangle2D.Double getMapTypeToolbarBounds() {
        if (this.mapTypeToolbar != null && this.mapTypeToolbar.isVisible()) {
            final Rectangle2D.Double unscaled = this.mapTypeToolbar.getBounds();
            (this.mapTypeToolbarBounds = new Rectangle2D.Double(unscaled.x * this.scaleFactor, unscaled.y * this.scaleFactor, unscaled.width * this.scaleFactor, unscaled.height * this.scaleFactor)).add(this.sliderCaveLayer.getBounds());
        }
        return this.mapTypeToolbarBounds;
    }
    
    public void func_146274_d() throws IOException {
        try {
            if (this.chat != null && !this.chat.isHidden()) {
                this.chat.func_146274_d();
            }
            this.mx = Mouse.getEventX() * this.field_146294_l / this.field_146297_k.field_71443_c;
            this.my = this.field_146295_m - Mouse.getEventY() * this.field_146295_m / this.field_146297_k.field_71440_d - 1;
            if (Mouse.getEventButtonState()) {
                this.func_73864_a(this.mx, this.my, Mouse.getEventButton());
            }
            else {
                final int wheel = Mouse.getEventDWheel();
                if (wheel > 0) {
                    this.zoomIn();
                }
                else if (wheel < 0) {
                    this.zoomOut();
                }
                else {
                    this.func_146286_b(this.mx, this.my, Mouse.getEventButton());
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }
    
    protected void func_73864_a(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        try {
            if (this.chat != null && !this.chat.isHidden()) {
                this.chat.func_73864_a(mouseX, mouseY, mouseButton);
            }
            super.func_73864_a(mouseX, mouseY, mouseButton);
            final Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), Fullscreen.gridRenderer.getHeight() - Mouse.getEventY());
            this.layerDelegate.onMouseClicked(this.field_146297_k, Fullscreen.gridRenderer, mousePosition, mouseButton, this.getMapFontScale());
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }
    
    @Override
    protected void func_146286_b(final int mouseX, final int mouseY, final int which) {
        try {
            super.func_146286_b(mouseX, mouseY, which);
            if (this.isMouseOverButton(mouseX, mouseY) || this.sliderCaveLayer.isVisible()) {
                return;
            }
            final int blockSize = (int)Math.pow(2.0, this.fullMapProperties.zoomLevel.get());
            if (Mouse.isButtonDown(0) && !this.isScrolling) {
                this.isScrolling = true;
                this.msx = this.mx;
                this.msy = this.my;
            }
            else if (!Mouse.isButtonDown(0) && this.isScrolling && !this.isMouseOverButton(this.msx, this.msy)) {
                this.isScrolling = false;
                final int mouseDragX = (this.mx - this.msx) * Math.max(1, this.scaleFactor) / blockSize;
                final int mouseDragY = (this.my - this.msy) * Math.max(1, this.scaleFactor) / blockSize;
                this.msx = this.mx;
                this.msy = this.my;
                try {
                    Fullscreen.gridRenderer.move(-mouseDragX, -mouseDragY);
                    Fullscreen.gridRenderer.updateTiles(Fullscreen.state.getMapView(), Fullscreen.state.getZoom(), Fullscreen.state.isHighQuality(), this.field_146297_k.field_71443_c, this.field_146297_k.field_71440_d, false, 0.0, 0.0);
                    Fullscreen.gridRenderer.setZoom(this.fullMapProperties.zoomLevel.get());
                }
                catch (Exception e) {
                    this.logger.error("Error moving grid: " + e);
                }
                this.setFollow(false);
                this.refreshState();
            }
            final Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), Fullscreen.gridRenderer.getHeight() - Mouse.getEventY());
            this.layerDelegate.onMouseMove(this.field_146297_k, Fullscreen.gridRenderer, mousePosition, this.getMapFontScale(), this.isScrolling);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }
    
    public void toggleMapType() {
        this.updateMapType(Fullscreen.state.toggleMapType());
    }
    
    private void updateMapType(MapView newType) {
        if (!newType.isAllowed()) {
            newType = Fullscreen.state.getMapView();
        }
        Fullscreen.state.setMapType(newType);
        this.buttonDay.setToggled(newType.isDay(), false);
        this.buttonNight.setToggled(newType.isNight(), false);
        this.buttonTopo.setToggled(newType.isTopo(), false);
        this.buttonLayers.setToggled(newType.isUnderground(), false);
        if (newType.isUnderground()) {
            this.sliderCaveLayer.setValue(newType.vSlice);
        }
        Fullscreen.state.requireRefresh();
    }
    
    public void zoomIn() {
        final int intValue = this.fullMapProperties.zoomLevel.get();
        Fullscreen.state.getClass();
        if (intValue < 5) {
            this.setZoom(this.fullMapProperties.zoomLevel.get() + 1);
        }
    }
    
    public void zoomOut() {
        final int intValue = this.fullMapProperties.zoomLevel.get();
        Fullscreen.state.getClass();
        if (intValue > 0) {
            this.setZoom(this.fullMapProperties.zoomLevel.get() - 1);
        }
    }
    
    private void setZoom(final int zoom) {
        if (Fullscreen.state.setZoom(zoom)) {
            final ThemeButton buttonZoomOut = this.buttonZoomOut;
            final int intValue = this.fullMapProperties.zoomLevel.get();
            Fullscreen.state.getClass();
            buttonZoomOut.setEnabled(intValue > 0);
            final ThemeButton buttonZoomIn = this.buttonZoomIn;
            final int intValue2 = this.fullMapProperties.zoomLevel.get();
            Fullscreen.state.getClass();
            buttonZoomIn.setEnabled(intValue2 < 5);
            this.refreshState();
        }
    }
    
    void toggleFollow() {
        final boolean isFollow = !Fullscreen.state.follow.get();
        this.setFollow(isFollow);
        if (isFollow && this.player != null) {
            this.sliderCaveLayer.setValue(this.player.field_70162_ai);
            if (Fullscreen.state.getMapView().isUnderground()) {
                this.sliderCaveLayer.checkClickListeners();
            }
        }
    }
    
    void setFollow(final Boolean follow) {
        Fullscreen.state.follow.set(follow);
        if (follow) {
            Fullscreen.state.resetMapType();
            this.refreshState();
        }
    }
    
    private Waypoint createWaypoint(final BlockPos pos) {
        return WaypointStore.create(Fullscreen.state.getDimension(), pos);
    }
    
    public void createWaypointAtMouse() {
        final Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), Fullscreen.gridRenderer.getHeight() - Mouse.getEventY());
        final BlockPos blockPos = this.layerDelegate.getBlockPos(this.field_146297_k, Fullscreen.gridRenderer, mousePosition);
        final Waypoint waypoint = this.createWaypoint(blockPos);
        UIManager.INSTANCE.openWaypointEditor(waypoint, true, this);
    }
    
    public void chatPositionAtMouse() {
        final Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), Fullscreen.gridRenderer.getHeight() - Mouse.getEventY());
        final BlockPos blockPos = this.layerDelegate.getBlockPos(this.field_146297_k, Fullscreen.gridRenderer, mousePosition);
        final Waypoint waypoint = this.createWaypoint(blockPos);
        this.openChat(WaypointChatParser.toChatString(waypoint));
    }
    
    public boolean isChatOpen() {
        return this.chat != null && !this.chat.isHidden();
    }
    
    public void func_73869_a(final char c, final int key) throws IOException {
        if (this.isChatOpen()) {
            this.chat.func_73869_a(c, key);
            return;
        }
        if (this.field_146297_k.field_71474_y.field_74310_D.func_151463_i() == key) {
            this.openChat("");
            return;
        }
        if (this.field_146297_k.field_71474_y.field_74323_J.func_151463_i() == key) {
            this.openChat("/");
            return;
        }
        if (1 == key) {
            UIManager.INSTANCE.closeAll();
        }
    }
    
    public void func_73876_c() {
        super.func_73876_c();
        if (this.chat != null) {
            this.chat.func_73876_c();
        }
    }
    
    @Override
    public void func_146278_c(final int layer) {
        DrawUtil.drawRectangle(0.0, 0.0, this.field_146294_l, this.field_146295_m, this.bgColor, 1.0f);
    }
    
    void drawMap() {
        final boolean refreshReady = this.isRefreshReady();
        final StatTimer timer = refreshReady ? this.drawMapTimerWithRefresh : this.drawMapTimer;
        final MapView mapView = Fullscreen.state.getMapView();
        timer.start();
        try {
            this.sizeDisplay(false);
            int xOffset = 0;
            int yOffset = 0;
            if (this.isScrolling) {
                final int blockSize = (int)Math.pow(2.0, this.fullMapProperties.zoomLevel.get());
                final int mouseDragX = (this.mx - this.msx) * Math.max(1, this.scaleFactor) / blockSize;
                final int mouseDragY = (this.my - this.msy) * Math.max(1, this.scaleFactor) / blockSize;
                xOffset = mouseDragX * blockSize;
                yOffset = mouseDragY * blockSize;
            }
            else if (refreshReady) {
                this.refreshState();
            }
            else {
                Fullscreen.gridRenderer.setContext(Fullscreen.state.getWorldDir(), mapView);
            }
            if (ClientFeatures.instance().isAllowed(Feature.Display.Fullscreen, Fullscreen.state.getDimension())) {
                Fullscreen.gridRenderer.clearGlErrors(false);
                Fullscreen.gridRenderer.updateRotation(0.0);
                if (Fullscreen.state.follow.get()) {
                    Fullscreen.gridRenderer.center(Fullscreen.state.getWorldDir(), mapView, this.player.field_70165_t, this.player.field_70161_v, this.fullMapProperties.zoomLevel.get());
                }
                Fullscreen.gridRenderer.updateTiles(mapView, Fullscreen.state.getZoom(), Fullscreen.state.isHighQuality(), this.field_146297_k.field_71443_c, this.field_146297_k.field_71440_d, false, 0.0, 0.0);
                Fullscreen.gridRenderer.draw(1.0f, xOffset, yOffset, this.fullMapProperties.showGrid.get());
                Fullscreen.gridRenderer.draw(Fullscreen.state.getDrawSteps(), xOffset, yOffset, this.getMapFontScale(), 0.0);
                Fullscreen.gridRenderer.draw(Fullscreen.state.getDrawWaypointSteps(), xOffset, yOffset, this.getMapFontScale(), 0.0);
                if (this.fullMapProperties.showSelf.get()) {
                    final Point2D playerPixel = Fullscreen.gridRenderer.getPixel(this.player.field_70165_t, this.player.field_70161_v);
                    if (playerPixel != null) {
                        final boolean large = this.fullMapProperties.playerDisplay.get().isLarge();
                        final TextureImpl bgTex = large ? TextureCache.getTexture(TextureCache.PlayerArrowBG_Large) : TextureCache.getTexture(TextureCache.PlayerArrowBG);
                        final TextureImpl fgTex = large ? TextureCache.getTexture(TextureCache.PlayerArrow_Large) : TextureCache.getTexture(TextureCache.PlayerArrow);
                        DrawUtil.drawColoredEntity(playerPixel.getX() + xOffset, playerPixel.getY() + yOffset, bgTex, 16777215, 1.0f, 1.0f, this.player.field_70759_as);
                        final int playerColor = this.coreProperties.getColor(this.coreProperties.colorSelf);
                        DrawUtil.drawColoredEntity(playerPixel.getX() + xOffset, playerPixel.getY() + yOffset, fgTex, playerColor, 1.0f, 1.0f, this.player.field_70759_as);
                    }
                }
                Fullscreen.gridRenderer.draw(this.layerDelegate.getDrawSteps(), xOffset, yOffset, this.getMapFontScale(), 0.0);
            }
            this.drawLogo();
            this.sizeDisplay(true);
        }
        finally {
            timer.stop();
            Fullscreen.gridRenderer.clearGlErrors(true);
        }
    }
    
    private int getMapFontScale() {
        return this.fullMapProperties.fontScale.get();
    }
    
    public void centerOn(final Waypoint waypoint) {
        if (waypoint.isDisplayed(Fullscreen.state.getDimension())) {
            Fullscreen.state.follow.set(false);
            Fullscreen.state.requireRefresh();
            final BlockPos pos = waypoint.getPosition(Fullscreen.state.getDimension());
            final int x = pos.func_177958_n();
            final int z = pos.func_177952_p();
            Fullscreen.gridRenderer.center(Fullscreen.state.getWorldDir(), Fullscreen.state.getMapView(), x, z, this.fullMapProperties.zoomLevel.get());
            if (!waypoint.isPersistent()) {
                this.addTempMarker(waypoint);
            }
            this.refreshState();
            this.func_73876_c();
        }
    }
    
    public void addTempMarker(final Waypoint waypoint) {
        try {
            final BlockPos pos = waypoint.getPosition(Fullscreen.state.getDimension());
            final PolygonOverlay polygonOverlay = new PolygonOverlay("journeymap", waypoint.getName(), this.player.field_71093_bK, new ShapeProperties().setStrokeColor(255).setStrokeOpacity(1.0f).setStrokeWidth(1.5f), new MapPolygon(new BlockPos[] { pos.func_177982_a(-1, 0, 2), pos.func_177982_a(2, 0, 2), pos.func_177982_a(2, 0, -1), pos.func_177982_a(-1, 0, -1) }));
            polygonOverlay.setActiveMapTypes(EnumSet.allOf(Feature.MapType.class));
            polygonOverlay.setActiveUIs(EnumSet.of(Feature.Display.Fullscreen));
            polygonOverlay.setLabel(waypoint.getName());
            this.tempOverlays.add(polygonOverlay);
            ClientAPI.INSTANCE.show(polygonOverlay);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error showing temp location marker: " + LogFormatter.toPartialString(t));
        }
    }
    
    void refreshState() {
        this.player = Journeymap.clientPlayer();
        if (this.player == null) {
            this.logger.warn("Could not get player");
            return;
        }
        final StatTimer timer = StatTimer.get("Fullscreen.refreshState");
        timer.start();
        try {
            this.menuToolbarBounds = null;
            this.optionsToolbarBounds = null;
            this.fullMapProperties = Journeymap.getClient().getFullMapProperties();
            Fullscreen.state.refresh(this.field_146297_k, (EntityPlayer)this.player, this.fullMapProperties);
            final MapView mapView = Fullscreen.state.getMapView();
            Fullscreen.gridRenderer.setContext(Fullscreen.state.getWorldDir(), mapView);
            if (Fullscreen.state.follow.get()) {
                Fullscreen.gridRenderer.center(Fullscreen.state.getWorldDir(), mapView, this.player.field_70165_t, this.player.field_70161_v, this.fullMapProperties.zoomLevel.get());
            }
            else {
                Fullscreen.gridRenderer.setZoom(this.fullMapProperties.zoomLevel.get());
            }
            if (!mapView.isNone()) {
                Fullscreen.gridRenderer.updateTiles(mapView, Fullscreen.state.getZoom(), Fullscreen.state.isHighQuality(), this.field_146297_k.field_71443_c, this.field_146297_k.field_71440_d, true, 0.0, 0.0);
            }
            Fullscreen.state.generateDrawSteps(this.field_146297_k, Fullscreen.gridRenderer, this.waypointRenderer, this.radarRenderer, this.fullMapProperties, false);
            final LocationFormat.LocationFormatKeys locationFormatKeys = this.locationFormat.getFormatKeys(this.fullMapProperties.locationFormat.get());
            Fullscreen.state.playerLastPos = locationFormatKeys.format(this.fullMapProperties.locationFormatVerbose.get(), MathHelper.func_76128_c(this.player.field_70165_t), MathHelper.func_76128_c(this.player.field_70161_v), MathHelper.func_76128_c(this.player.func_174813_aQ().field_72338_b), this.player.field_70162_ai) + " " + Fullscreen.state.getPlayerBiome();
            Fullscreen.state.updateLastRefresh();
        }
        finally {
            timer.stop();
        }
        final Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), Fullscreen.gridRenderer.getHeight() - Mouse.getEventY());
        this.layerDelegate.onMouseMove(this.field_146297_k, Fullscreen.gridRenderer, mousePosition, this.getMapFontScale(), this.isScrolling);
    }
    
    public void openChat(final String defaultText) {
        if (this.chat != null) {
            this.chat.setText(defaultText);
            this.chat.setHidden(false);
        }
        else {
            (this.chat = new MapChat(defaultText, false)).func_146280_a(this.field_146297_k, this.field_146294_l, this.field_146295_m);
        }
    }
    
    @Override
    public void close() {
        for (final Overlay temp : this.tempOverlays) {
            ClientAPI.INSTANCE.remove(temp);
        }
        Fullscreen.gridRenderer.updateUIState(false);
        if (this.chat != null) {
            this.chat.close();
        }
    }
    
    public void func_146281_b() {
        Keyboard.enableRepeatEvents(false);
    }
    
    boolean isRefreshReady() {
        return !this.isScrolling && (Fullscreen.state.shouldRefresh(super.field_146297_k, this.fullMapProperties) || Fullscreen.gridRenderer.hasUnloadedTile());
    }
    
    public int getScreenScaleFactor() {
        return this.scaleFactor;
    }
    
    public void moveCanvas(final int deltaBlockX, final int deltaBlockz) {
        this.refreshState();
        Fullscreen.gridRenderer.move(deltaBlockX, deltaBlockz);
        Fullscreen.gridRenderer.updateTiles(Fullscreen.state.getMapView(), Fullscreen.state.getZoom(), Fullscreen.state.isHighQuality(), this.field_146297_k.field_71443_c, this.field_146297_k.field_71440_d, true, 0.0, 0.0);
        ClientAPI.INSTANCE.flagOverlaysForRerender();
        this.setFollow(false);
    }
    
    public void showCaveLayers() {
        if (!Fullscreen.state.isUnderground()) {
            this.updateMapType(MapView.underground(3, Fullscreen.state.getDimension()));
        }
    }
    
    @Override
    protected void drawLogo() {
        if (this.logo.isDefunct()) {
            this.logo = TextureCache.getTexture(TextureCache.Logo);
        }
        DrawUtil.sizeDisplay(this.field_146297_k.field_71443_c, this.field_146297_k.field_71440_d);
        final Theme.Container.Toolbar toolbar = ThemeLoader.getCurrentTheme().container.toolbar;
        final float scale = this.scaleFactor * 2;
        DrawUtil.sizeDisplay(this.field_146294_l, this.field_146295_m);
        DrawUtil.drawImage(this.logo, toolbar.horizontal.margin, toolbar.vertical.margin, false, 1.0f / scale, 0.0);
    }
    
    @Override
    public final boolean func_73868_f() {
        return false;
    }
    
    public void setTheme(final String name) {
        try {
            final MiniMapProperties mmp = Journeymap.getClient().getMiniMapProperties(Journeymap.getClient().getActiveMinimapId());
            mmp.shape.set(Shape.Rectangle);
            mmp.sizePercent.set(20);
            mmp.save();
            final Theme theme = ThemeLoader.getThemeByName(name);
            ThemeLoader.setCurrentTheme(theme);
            UIManager.INSTANCE.getMiniMap().reset();
            ChatLog.announceI18N("jm.common.ui_theme_applied", new Object[0]);
            UIManager.INSTANCE.closeAll();
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Could not load Theme: " + LogFormatter.toString(e));
        }
    }
    
    public void func_184072_a(final String... newCompletions) {
        this.chat.func_184072_a(newCompletions);
    }
    
    static {
        state = new MapState();
        gridRenderer = new GridRenderer(Feature.Display.Fullscreen, 5);
    }
}
