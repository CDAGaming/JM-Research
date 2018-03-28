package journeymap.client.ui.fullscreen;

import journeymap.client.model.Waypoint;
import net.minecraft.util.*;
import journeymap.client.render.map.*;
import journeymap.client.ui.fullscreen.layer.*;
import journeymap.client.ui.option.*;
import journeymap.common.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.api.util.*;
import net.minecraft.entity.player.*;
import journeymap.client.ui.*;
import net.minecraft.client.renderer.*;
import org.apache.logging.log4j.*;
import journeymap.common.log.*;
import net.minecraft.client.*;
import journeymap.client.ui.theme.*;
import journeymap.client.feature.*;
import journeymap.client.*;
import journeymap.client.data.*;
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
import org.lwjgl.input.*;
import javax.annotation.*;
import java.io.*;
import java.awt.geom.*;
import journeymap.client.render.draw.*;
import journeymap.client.render.texture.*;
import journeymap.client.api.model.*;
import journeymap.client.api.impl.*;
import journeymap.client.api.display.*;
import net.minecraft.util.math.*;
import net.minecraft.client.entity.*;
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
        this.mc = FMLClientHandler.instance().getClient();
        this.layerDelegate = new LayerDelegate(this);
        if (Journeymap.getClient().getFullMapProperties().showCaves.get() && DataCache.getPlayer().underground && Fullscreen.state.follow.get()) {
            Fullscreen.state.setMapType(MapType.underground(DataCache.getPlayer()));
        }
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
        this.buttonList.clear();
    }
    
    @Override
    public void initGui() {
        this.fullMapProperties = Journeymap.getClient().getFullMapProperties();
        Fullscreen.state.requireRefresh();
        Fullscreen.state.refresh(this.mc, (EntityPlayer)this.mc.player, this.fullMapProperties);
        final MapType mapType = Fullscreen.state.getMapType();
        Keyboard.enableRepeatEvents(true);
        if (mapType.dimension != this.mc.player.dimension) {
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
    public void drawScreen(final int width, final int height, final float f) {
        try {
            this.drawBackground(0);
            this.drawMap();
            this.drawScreenTimer.start();
            this.layoutButtons();
            List<String> tooltip = null;
            if (this.firstLayoutPass) {
                this.layoutButtons();
                this.updateMapType(Fullscreen.state.getMapType());
                this.firstLayoutPass = false;
            }
            else {
                for (int k = 0; k < this.buttonList.size(); ++k) {
                    final GuiButton guibutton = this.buttonList.get(k);
                    guibutton.drawButton(this.mc, width, height, f);
                    if (tooltip == null && guibutton instanceof Button) {
                        final Button button = (Button)guibutton;
                        if (button.mouseOver(this.mx, this.my)) {
                            tooltip = button.getTooltip();
                        }
                    }
                }
            }
            if (this.chat != null) {
                this.chat.drawScreen(width, height, f);
            }
            if (tooltip != null && !tooltip.isEmpty()) {
                this.drawHoveringText(tooltip, this.mx, this.my, this.getFontRenderer());
                RenderHelper.disableStandardItemLighting();
            }
        }
        catch (Throwable e) {
            this.logger.log(Level.ERROR, "Unexpected exception in jm.fullscreen.drawScreen(): " + LogFormatter.toString(e));
            UIManager.INSTANCE.closeAll();
        }
        finally {
            this.drawScreenTimer.stop();
        }
    }
    
    protected void actionPerformed(final GuiButton guibutton) {
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
    public void setWorldAndResolution(final Minecraft minecraft, final int width, final int height) {
        super.setWorldAndResolution(minecraft, width, height);
        Fullscreen.state.requireRefresh();
        if (this.chat == null) {
            this.chat = new MapChat("", true);
        }
        if (this.chat != null) {
            this.chat.setWorldAndResolution(minecraft, width, height);
        }
        this.initGui();
        this.refreshState();
        this.drawMap();
    }
    
    void initButtons() {
        if (this.buttonList.isEmpty()) {
            this.firstLayoutPass = true;
            final Theme theme = ThemeLoader.getCurrentTheme();
            final MapType mapType = Fullscreen.state.getMapType();
            this.bgColor = theme.fullscreen.background.getColor();
            this.statusLabelSpec = theme.fullscreen.statusLabel;
            this.buttonDay = new ThemeToggle(theme, "jm.fullscreen.map_day", "day");
            this.buttonNight = new ThemeToggle(theme, "jm.fullscreen.map_night", "night");
            this.buttonTopo = new ThemeToggle(theme, "jm.fullscreen.map_topo", "topo");
            this.buttonLayers = new ThemeToggle(theme, "jm.fullscreen.map_cave_layers", "layers");
            this.buttonDay.setToggled(mapType.isDay(), false);
            this.buttonDay.setStaysOn(true);
            this.buttonDay.addToggleListener((button, toggled) -> {
                if (button.enabled) {
                    this.updateMapType(MapType.day(Fullscreen.state.getDimension()));
                }
                return button.enabled;
            });
            this.buttonNight.setToggled(mapType.isNight(), false);
            this.buttonNight.setStaysOn(true);
            this.buttonNight.addToggleListener((button, toggled) -> {
                if (button.enabled) {
                    this.updateMapType(MapType.night(Fullscreen.state.getDimension()));
                }
                return button.enabled;
            });
            this.buttonTopo.setDrawButton(this.coreProperties.mapTopography.get());
            this.buttonTopo.setToggled(mapType.isTopo(), false);
            this.buttonTopo.setStaysOn(true);
            this.buttonTopo.addToggleListener((button, toggled) -> {
                if (button.enabled) {
                    this.updateMapType(MapType.topo(Fullscreen.state.getDimension()));
                }
                return button.enabled;
            });
            this.buttonLayers.setEnabled(FeatureManager.isAllowed(Feature.MapCaves));
            this.buttonLayers.setToggled(mapType.isUnderground(), false);
            this.buttonLayers.setStaysOn(true);
            this.buttonLayers.addToggleListener((button, toggled) -> {
                if (button.enabled) {
                    this.updateMapType(MapType.underground(DataCache.getPlayer()));
                }
                return button.enabled;
            });
            final FontRenderer fontRenderer = this.getFontRenderer();
            (this.sliderCaveLayer = new IntSliderButton(Fullscreen.state.getLastSlice(), Constants.getString("jm.fullscreen.map_cave_layers.button") + " ", "")).setWidth(this.sliderCaveLayer.getFitWidth(fontRenderer) + fontRenderer.getStringWidth("0"));
            this.sliderCaveLayer.setDefaultStyle(false);
            this.sliderCaveLayer.setDrawBackground(true);
            final Theme.Control.ButtonSpec buttonSpec = this.buttonLayers.getButtonSpec();
            this.sliderCaveLayer.setBackgroundColors(buttonSpec.buttonDisabled.getColor(), buttonSpec.buttonOff.getColor(), buttonSpec.buttonOff.getColor());
            this.sliderCaveLayer.setLabelColors(buttonSpec.iconHoverOff.getColor(), buttonSpec.iconHoverOn.getColor(), buttonSpec.iconDisabled.getColor());
            this.sliderCaveLayer.addClickListener(button -> {
                Fullscreen.state.setMapType(MapType.underground(this.sliderCaveLayer.getValue(), Fullscreen.state.getDimension()));
                this.refreshState();
                return true;
            });
            this.buttonList.add(this.sliderCaveLayer);
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
            (this.buttonWaypointManager = new ThemeButton(theme, "jm.waypoint.waypoints_button", "waypoints")).setDrawButton(WaypointsData.isManagerEnabled());
            this.buttonWaypointManager.addToggleListener((button, toggled) -> {
                UIManager.INSTANCE.openWaypointManager(null, this);
                return true;
            });
            (this.buttonTheme = new ThemeButton(theme, "jm.common.ui_theme", "theme")).addToggleListener((button, toggled) -> {
                ThemeLoader.loadNextTheme();
                UIManager.INSTANCE.getMiniMap().reset();
                this.buttonList.clear();
                return false;
            });
            final String[] tooltips = { TextFormatting.ITALIC + Constants.getString("jm.common.ui_theme_name", theme.name), TextFormatting.ITALIC + Constants.getString("jm.common.ui_theme_author", theme.author) };
            this.buttonTheme.setAdditionalTooltips(Arrays.asList(tooltips));
            (this.buttonOptions = new ThemeButton(theme, "jm.common.options_button", "options")).addToggleListener((button, toggled) -> {
                try {
                    UIManager.INSTANCE.openOptionsManager();
                    this.buttonList.clear();
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
            final EntityDTO[] player = new EntityDTO[1];
            this.buttonCaves.addToggleListener((button, toggled) -> {
                player[0] = DataCache.getPlayer();
                if (toggled && player[0].underground) {
                    this.updateMapType(MapType.underground(player[0]));
                }
                return true;
            });
            (this.buttonMobs = new ThemeToggle(theme, "jm.common.show_mobs", "monsters", this.fullMapProperties.showMobs)).setTooltip(Constants.getString("jm.common.show_mobs.tooltip"));
            this.buttonMobs.setDrawButton(FeatureManager.isAllowed(Feature.RadarMobs));
            (this.buttonAnimals = new ThemeToggle(theme, "jm.common.show_animals", "animals", this.fullMapProperties.showAnimals)).setTooltip(Constants.getString("jm.common.show_animals.tooltip"));
            this.buttonAnimals.setDrawButton(FeatureManager.isAllowed(Feature.RadarAnimals));
            (this.buttonPets = new ThemeToggle(theme, "jm.common.show_pets", "pets", this.fullMapProperties.showPets)).setTooltip(Constants.getString("jm.common.show_pets.tooltip"));
            this.buttonPets.setDrawButton(FeatureManager.isAllowed(Feature.RadarAnimals));
            (this.buttonVillagers = new ThemeToggle(theme, "jm.common.show_villagers", "villagers", this.fullMapProperties.showVillagers)).setTooltip(Constants.getString("jm.common.show_villagers.tooltip"));
            this.buttonVillagers.setDrawButton(FeatureManager.isAllowed(Feature.RadarVillagers));
            (this.buttonPlayers = new ThemeToggle(theme, "jm.common.show_players", "players", this.fullMapProperties.showPlayers)).setTooltip(Constants.getString("jm.common.show_players.tooltip"));
            this.buttonPlayers.setDrawButton(!this.mc.isSingleplayer() && FeatureManager.isAllowed(Feature.RadarPlayers));
            (this.buttonGrid = new ThemeToggle(theme, "jm.common.show_grid", "grid", this.fullMapProperties.showGrid)).setTooltip(Constants.getString("jm.common.show_grid_shift.tooltip"));
            this.buttonGrid.setTooltip(Constants.getString("jm.common.show_grid_shift.tooltip"));
            final boolean[] shiftDown = new boolean[1];
            this.buttonGrid.addToggleListener((button, toggled) -> {
                shiftDown[0] = (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
                if (shiftDown[0]) {
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
            final MapSaver[] mapSaver = new MapSaver[1];
            (this.buttonSavemap = new ThemeButton(theme, "jm.common.save_map", "savemap")).addToggleListener((button, toggled) -> {
                this.buttonSavemap.setEnabled(false);
                try {
                    mapSaver[0] = new MapSaver(Fullscreen.state.getWorldDir(), Fullscreen.state.getMapType());
                    if (mapSaver[0].isValid()) {
                        Journeymap.getClient().toggleTask(SaveMapTask.Manager.class, true, mapSaver[0]);
                        ChatLog.announceI18N("jm.common.save_filename", mapSaver[0].getSaveFileName());
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
            this.autoMapOnTooltip = (List<String>)fontRenderer.listFormattedStringToWidth(Constants.getString("jm.common.automap_stop_text"), 200);
            this.autoMapOffTooltip = (List<String>)fontRenderer.listFormattedStringToWidth(Constants.getString("jm.common.automap_text"), 200);
            (this.buttonAutomap = new ThemeToggle(theme, autoMapOn, autoMapOff, "automap")).setEnabled(FMLClientHandler.instance().getClient().isSingleplayer() && Journeymap.getClient().getCoreProperties().mappingEnabled.get());
            this.buttonAutomap.setToggled(automapRunning, false);
            this.buttonAutomap.addToggleListener((button, toggled) -> {
                if (toggled) {
                    UIManager.INSTANCE.open(AutoMapConfirmation.class, this);
                }
                else {
                    Journeymap.getClient().toggleTask(MapRegionTask.Manager.class, false, null);
                    this.buttonAutomap.setToggled(false, false);
                    this.buttonList.clear();
                }
                return true;
            });
            (this.buttonDeletemap = new ThemeButton(theme, "jm.common.deletemap_title", "delete")).setAdditionalTooltips(fontRenderer.listFormattedStringToWidth(Constants.getString("jm.common.deletemap_text"), 200));
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
            (this.buttonResetPalette = new ThemeButton(theme, "jm.common.colorreset_title", "reset")).setAdditionalTooltips(fontRenderer.listFormattedStringToWidth(Constants.getString("jm.common.colorreset_text"), 200));
            this.buttonResetPalette.addToggleListener((button, toggled) -> {
                Journeymap.getClient().queueMainThreadTask(new EnsureCurrentColorsTask(true, true));
                return false;
            });
            (this.mapTypeToolbar = new ThemeToolbar(theme, new Button[] { this.buttonLayers, this.buttonTopo, this.buttonNight, this.buttonDay })).addAllButtons(this);
            (this.optionsToolbar = new ThemeToolbar(theme, new Button[] { this.buttonCaves, this.buttonMobs, this.buttonAnimals, this.buttonPets, this.buttonVillagers, this.buttonPlayers, this.buttonGrid, this.buttonKeys })).addAllButtons(this);
            this.optionsToolbar.visible = false;
            (this.menuToolbar = new ThemeToolbar(theme, new Button[] { this.buttonWaypointManager, this.buttonOptions, this.buttonAbout, this.buttonBrowser, this.buttonTheme, this.buttonResetPalette, this.buttonDeletemap, this.buttonSavemap, this.buttonAutomap, this.buttonDisable })).addAllButtons(this);
            this.menuToolbar.visible = false;
            (this.zoomToolbar = new ThemeToolbar(theme, new Button[] { this.buttonFollow, this.buttonZoomIn, this.buttonZoomOut })).setLayout(ButtonList.Layout.Vertical, ButtonList.Direction.LeftToRight);
            this.zoomToolbar.addAllButtons(this);
            this.buttonList.add(this.buttonAlert);
            this.buttonList.add(this.buttonClose);
        }
    }
    
    @Override
    protected void layoutButtons() {
        if (this.buttonDay != null && !this.buttonDay.hasValidTextures()) {
            this.buttonList.clear();
        }
        if (this.buttonList.isEmpty()) {
            this.initButtons();
        }
        this.menuToolbar.setDrawToolbar(!this.isChatOpen());
        final MapType mapType = Fullscreen.state.getMapType();
        this.buttonDay.setEnabled(Fullscreen.state.isSurfaceMappingAllowed());
        this.buttonDay.setToggled(this.buttonDay.enabled && mapType.isDay());
        this.buttonNight.setEnabled(Fullscreen.state.isSurfaceMappingAllowed());
        this.buttonNight.setToggled(this.buttonNight.enabled && mapType.isNight());
        this.buttonTopo.setEnabled(Fullscreen.state.isTopoMappingAllowed());
        this.buttonTopo.setToggled(this.buttonTopo.enabled && mapType.isTopo());
        this.buttonCaves.setEnabled(Fullscreen.state.isCaveMappingAllowed());
        this.buttonCaves.setToggled(this.buttonCaves.enabled && mapType.isUnderground());
        this.buttonFollow.setEnabled(!Fullscreen.state.follow.get());
        final boolean automapRunning = Journeymap.getClient().isTaskManagerEnabled(MapRegionTask.Manager.class);
        final boolean mappingEnabled = Journeymap.getClient().getCoreProperties().mappingEnabled.get();
        this.buttonDisable.setToggled(!mappingEnabled, false);
        this.buttonAutomap.setToggled(automapRunning, false);
        this.buttonAutomap.setEnabled(mappingEnabled);
        this.buttonAutomap.setAdditionalTooltips(automapRunning ? this.autoMapOnTooltip : this.autoMapOffTooltip);
        final boolean webMapEnabled = Journeymap.getClient().getWebMapProperties().enabled.get();
        this.buttonBrowser.setEnabled(webMapEnabled && mappingEnabled);
        this.buttonBrowser.setDrawButton(webMapEnabled);
        final boolean mainThreadActive = Journeymap.getClient().isMainThreadTaskActive();
        this.buttonResetPalette.setEnabled(!mainThreadActive && mappingEnabled);
        this.buttonDeletemap.setEnabled(!mainThreadActive);
        this.buttonDisable.setEnabled(!mainThreadActive);
        final int padding = this.mapTypeToolbar.getToolbarSpec().padding;
        this.zoomToolbar.layoutCenteredVertical(this.zoomToolbar.getHMargin(), this.height / 2, true, padding);
        final int topY = this.mapTypeToolbar.getVMargin();
        final int margin = this.mapTypeToolbar.getHMargin();
        this.buttonClose.leftOf(this.width - this.zoomToolbar.getHMargin()).below(this.mapTypeToolbar.getVMargin());
        this.buttonAlert.leftOf(this.width - this.zoomToolbar.getHMargin()).below(this.buttonClose, padding);
        final int toolbarsWidth = this.mapTypeToolbar.getWidth() + this.optionsToolbar.getWidth() + margin + padding;
        final int startX = (this.width - toolbarsWidth) / 2;
        Rectangle2D.Double oldBounds = this.mapTypeToolbar.getBounds();
        this.mapTypeToolbar.layoutHorizontal(startX + this.mapTypeToolbar.getWidth(), topY, false, padding);
        if (!this.mapTypeToolbar.getBounds().equals(oldBounds)) {
            this.mapTypeToolbarBounds = null;
        }
        oldBounds = this.optionsToolbar.getBounds();
        this.optionsToolbar.layoutHorizontal(this.mapTypeToolbar.getRightX() + margin, topY, true, padding);
        this.optionsToolbar.visible = true;
        if (!this.optionsToolbar.getBounds().equals(oldBounds)) {
            this.optionsToolbarBounds = null;
        }
        oldBounds = this.menuToolbar.getBounds();
        this.menuToolbar.layoutCenteredHorizontal(this.width / 2, this.height - this.menuToolbar.height - this.menuToolbar.getVMargin(), true, padding);
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
    
    public void handleMouseInput() throws IOException {
        try {
            if (this.chat != null && !this.chat.isHidden()) {
                this.chat.handleMouseInput();
            }
            this.mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
            this.my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            if (Mouse.getEventButtonState()) {
                this.mouseClicked(this.mx, this.my, Mouse.getEventButton());
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
                    this.mouseReleased(this.mx, this.my, Mouse.getEventButton());
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }
    
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        try {
            if (this.chat != null && !this.chat.isHidden()) {
                this.chat.mouseClicked(mouseX, mouseY, mouseButton);
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
            final Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), Fullscreen.gridRenderer.getHeight() - Mouse.getEventY());
            this.layerDelegate.onMouseClicked(this.mc, Fullscreen.gridRenderer, mousePosition, mouseButton, this.getMapFontScale());
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }
    
    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int which) {
        try {
            super.mouseReleased(mouseX, mouseY, which);
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
                    Fullscreen.gridRenderer.updateTiles(Fullscreen.state.getMapType(), Fullscreen.state.getZoom(), Fullscreen.state.isHighQuality(), this.mc.displayWidth, this.mc.displayHeight, false, 0.0, 0.0);
                    Fullscreen.gridRenderer.setZoom(this.fullMapProperties.zoomLevel.get());
                }
                catch (Exception e) {
                    this.logger.error("Error moving grid: " + e);
                }
                this.setFollow(false);
                this.refreshState();
            }
            final Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), Fullscreen.gridRenderer.getHeight() - Mouse.getEventY());
            this.layerDelegate.onMouseMove(this.mc, Fullscreen.gridRenderer, mousePosition, this.getMapFontScale(), this.isScrolling);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }
    
    public void toggleMapType() {
        this.updateMapType(Fullscreen.state.toggleMapType());
    }
    
    private void updateMapType(MapType newType) {
        if (!newType.isAllowed()) {
            newType = Fullscreen.state.getMapType();
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
        if (isFollow && this.mc.player != null) {
            this.sliderCaveLayer.setValue(this.mc.player.chunkCoordY);
            if (Fullscreen.state.getMapType().isUnderground()) {
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
    
    public void createWaypointAtMouse() {
        final Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), Fullscreen.gridRenderer.getHeight() - Mouse.getEventY());
        final BlockPos blockPos = this.layerDelegate.getBlockPos(this.mc, Fullscreen.gridRenderer, mousePosition);
        final Waypoint waypoint = Waypoint.at(blockPos, Waypoint.Type.Normal, this.mc.player.dimension);
        UIManager.INSTANCE.openWaypointEditor(waypoint, true, this);
    }
    
    public void chatPositionAtMouse() {
        final Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), Fullscreen.gridRenderer.getHeight() - Mouse.getEventY());
        final BlockPos blockPos = this.layerDelegate.getBlockPos(this.mc, Fullscreen.gridRenderer, mousePosition);
        final Waypoint waypoint = Waypoint.at(blockPos, Waypoint.Type.Normal, Fullscreen.state.getDimension());
        this.openChat(waypoint.toChatString());
    }
    
    public boolean isChatOpen() {
        return this.chat != null && !this.chat.isHidden();
    }
    
    public void keyTyped(final char c, final int key) throws IOException {
        if (this.isChatOpen()) {
            this.chat.keyTyped(c, key);
            return;
        }
        if (this.mc.gameSettings.keyBindChat.getKeyCode() == key) {
            this.openChat("");
            return;
        }
        if (this.mc.gameSettings.keyBindCommand.getKeyCode() == key) {
            this.openChat("/");
            return;
        }
        if (1 == key) {
            UIManager.INSTANCE.closeAll();
        }
    }
    
    public void updateScreen() {
        super.updateScreen();
        if (this.chat != null) {
            this.chat.updateScreen();
        }
    }
    
    @Override
    public void drawBackground(final int layer) {
        DrawUtil.drawRectangle(0.0, 0.0, this.width, this.height, this.bgColor, 1.0f);
    }
    
    void drawMap() {
        final boolean refreshReady = this.isRefreshReady();
        final StatTimer timer = refreshReady ? this.drawMapTimerWithRefresh : this.drawMapTimer;
        final MapType mapType = Fullscreen.state.getMapType();
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
                Fullscreen.gridRenderer.setContext(Fullscreen.state.getWorldDir(), mapType);
            }
            Fullscreen.gridRenderer.clearGlErrors(false);
            Fullscreen.gridRenderer.updateRotation(0.0);
            if (Fullscreen.state.follow.get()) {
                Fullscreen.gridRenderer.center(Fullscreen.state.getWorldDir(), mapType, this.mc.player.posX, this.mc.player.posZ, this.fullMapProperties.zoomLevel.get());
            }
            Fullscreen.gridRenderer.updateTiles(mapType, Fullscreen.state.getZoom(), Fullscreen.state.isHighQuality(), this.mc.displayWidth, this.mc.displayHeight, false, 0.0, 0.0);
            Fullscreen.gridRenderer.draw(1.0f, xOffset, yOffset, this.fullMapProperties.showGrid.get());
            Fullscreen.gridRenderer.draw(Fullscreen.state.getDrawSteps(), xOffset, yOffset, this.getMapFontScale(), 0.0);
            Fullscreen.gridRenderer.draw(Fullscreen.state.getDrawWaypointSteps(), xOffset, yOffset, this.getMapFontScale(), 0.0);
            if (this.fullMapProperties.showSelf.get()) {
                final Point2D playerPixel = Fullscreen.gridRenderer.getPixel(this.mc.player.posX, this.mc.player.posZ);
                if (playerPixel != null) {
                    final boolean large = this.fullMapProperties.playerDisplay.get().isLarge();
                    final TextureImpl bgTex = large ? TextureCache.getTexture(TextureCache.PlayerArrowBG_Large) : TextureCache.getTexture(TextureCache.PlayerArrowBG);
                    final TextureImpl fgTex = large ? TextureCache.getTexture(TextureCache.PlayerArrow_Large) : TextureCache.getTexture(TextureCache.PlayerArrow);
                    DrawUtil.drawColoredEntity(playerPixel.getX() + xOffset, playerPixel.getY() + yOffset, bgTex, 16777215, 1.0f, 1.0f, this.mc.player.rotationYawHead);
                    final int playerColor = this.coreProperties.getColor(this.coreProperties.colorSelf);
                    DrawUtil.drawColoredEntity(playerPixel.getX() + xOffset, playerPixel.getY() + yOffset, fgTex, playerColor, 1.0f, 1.0f, this.mc.player.rotationYawHead);
                }
            }
            Fullscreen.gridRenderer.draw(this.layerDelegate.getDrawSteps(), xOffset, yOffset, this.getMapFontScale(), 0.0);
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
        if (waypoint.getDimensions().contains(this.mc.player.dimension)) {
            Fullscreen.state.follow.set(false);
            Fullscreen.state.requireRefresh();
            final int x = waypoint.getX();
            final int z = waypoint.getZ();
            Fullscreen.gridRenderer.center(Fullscreen.state.getWorldDir(), Fullscreen.state.getMapType(), x, z, this.fullMapProperties.zoomLevel.get());
            if (!waypoint.isPersistent()) {
                this.addTempMarker(waypoint);
            }
            this.refreshState();
            this.updateScreen();
        }
    }
    
    public void addTempMarker(final Waypoint waypoint) {
        try {
            final BlockPos pos = waypoint.getBlockPos();
            final PolygonOverlay polygonOverlay = new PolygonOverlay("journeymap", waypoint.getName(), this.mc.player.dimension, new ShapeProperties().setStrokeColor(255).setStrokeOpacity(1.0f).setStrokeWidth(1.5f), new MapPolygon(new BlockPos[] { pos.add(-1, 0, 2), pos.add(2, 0, 2), pos.add(2, 0, -1), pos.add(-1, 0, -1) }));
            polygonOverlay.setActiveMapTypes(EnumSet.allOf(Context.MapType.class));
            polygonOverlay.setActiveUIs(EnumSet.of(Context.UI.Fullscreen));
            polygonOverlay.setLabel(waypoint.getName());
            this.tempOverlays.add(polygonOverlay);
            ClientAPI.INSTANCE.show(polygonOverlay);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error showing temp location marker: " + LogFormatter.toPartialString(t));
        }
    }
    
    void refreshState() {
        final EntityPlayerSP player = this.mc.player;
        if (player == null) {
            this.logger.warn("Could not get player");
            return;
        }
        final StatTimer timer = StatTimer.get("Fullscreen.refreshState");
        timer.start();
        try {
            this.menuToolbarBounds = null;
            this.optionsToolbarBounds = null;
            this.fullMapProperties = Journeymap.getClient().getFullMapProperties();
            Fullscreen.state.refresh(this.mc, (EntityPlayer)player, this.fullMapProperties);
            final MapType mapType = Fullscreen.state.getMapType();
            Fullscreen.gridRenderer.setContext(Fullscreen.state.getWorldDir(), mapType);
            if (Fullscreen.state.follow.get()) {
                Fullscreen.gridRenderer.center(Fullscreen.state.getWorldDir(), mapType, this.mc.player.posX, this.mc.player.posZ, this.fullMapProperties.zoomLevel.get());
            }
            else {
                Fullscreen.gridRenderer.setZoom(this.fullMapProperties.zoomLevel.get());
            }
            Fullscreen.gridRenderer.updateTiles(mapType, Fullscreen.state.getZoom(), Fullscreen.state.isHighQuality(), this.mc.displayWidth, this.mc.displayHeight, true, 0.0, 0.0);
            Fullscreen.state.generateDrawSteps(this.mc, Fullscreen.gridRenderer, this.waypointRenderer, this.radarRenderer, this.fullMapProperties, false);
            final LocationFormat.LocationFormatKeys locationFormatKeys = this.locationFormat.getFormatKeys(this.fullMapProperties.locationFormat.get());
            Fullscreen.state.playerLastPos = locationFormatKeys.format(this.fullMapProperties.locationFormatVerbose.get(), MathHelper.floor(this.mc.player.posX), MathHelper.floor(this.mc.player.posZ), MathHelper.floor(this.mc.player.getEntityBoundingBox().minY), this.mc.player.chunkCoordY) + " " + Fullscreen.state.getPlayerBiome();
            Fullscreen.state.updateLastRefresh();
        }
        finally {
            timer.stop();
        }
        final Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), Fullscreen.gridRenderer.getHeight() - Mouse.getEventY());
        this.layerDelegate.onMouseMove(this.mc, Fullscreen.gridRenderer, mousePosition, this.getMapFontScale(), this.isScrolling);
    }
    
    public void openChat(final String defaultText) {
        if (this.chat != null) {
            this.chat.setText(defaultText);
            this.chat.setHidden(false);
        }
        else {
            (this.chat = new MapChat(defaultText, false)).setWorldAndResolution(this.mc, this.width, this.height);
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
    
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    
    boolean isRefreshReady() {
        return !this.isScrolling && (Fullscreen.state.shouldRefresh(super.mc, this.fullMapProperties) || Fullscreen.gridRenderer.hasUnloadedTile());
    }
    
    public int getScreenScaleFactor() {
        return this.scaleFactor;
    }
    
    public void moveCanvas(final int deltaBlockX, final int deltaBlockz) {
        this.refreshState();
        Fullscreen.gridRenderer.move(deltaBlockX, deltaBlockz);
        Fullscreen.gridRenderer.updateTiles(Fullscreen.state.getMapType(), Fullscreen.state.getZoom(), Fullscreen.state.isHighQuality(), this.mc.displayWidth, this.mc.displayHeight, true, 0.0, 0.0);
        ClientAPI.INSTANCE.flagOverlaysForRerender();
        this.setFollow(false);
    }
    
    public void showCaveLayers() {
        if (!Fullscreen.state.isUnderground()) {
            this.updateMapType(MapType.underground(3, Fullscreen.state.getDimension()));
        }
    }
    
    @Override
    protected void drawLogo() {
        if (this.logo.isDefunct()) {
            this.logo = TextureCache.getTexture(TextureCache.Logo);
        }
        DrawUtil.sizeDisplay(this.mc.displayWidth, this.mc.displayHeight);
        final Theme.Container.Toolbar toolbar = ThemeLoader.getCurrentTheme().container.toolbar;
        final float scale = this.scaleFactor * 2;
        DrawUtil.sizeDisplay(this.width, this.height);
        DrawUtil.drawImage(this.logo, toolbar.horizontal.margin, toolbar.vertical.margin, false, 1.0f / scale, 0.0);
    }
    
    @Override
    public final boolean doesGuiPauseGame() {
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
    
    public void setCompletions(final String... newCompletions) {
        this.chat.setCompletions(newCompletions);
    }
    
    static {
        state = new MapState();
        gridRenderer = new GridRenderer(Context.UI.Fullscreen, 5);
    }
}
