package journeymap.client.ui.dialog;

import journeymap.client.Constants;
import journeymap.client.cartography.color.ColorManager;
import journeymap.client.data.DataCache;
import journeymap.client.forge.event.KeyEventHandler;
import journeymap.client.io.ThemeLoader;
import journeymap.client.log.JMLogger;
import journeymap.client.mod.ModBlockDelegate;
import journeymap.client.model.BlockMD;
import journeymap.client.properties.ClientCategory;
import journeymap.client.properties.CoreProperties;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.TileDrawStepCache;
import journeymap.client.service.WebServer;
import journeymap.client.task.main.SoftResetTask;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.client.task.multi.RenderSpec;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.*;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.client.ui.option.CategorySlot;
import journeymap.client.ui.option.OptionSlotFactory;
import journeymap.client.ui.option.SlotMetadata;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.properties.Category;
import journeymap.common.properties.PropertiesBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.IOException;
import java.util.*;

public class OptionsManager extends JmUI {
    protected static Set<Category> openCategories;

    static {
        OptionsManager.openCategories = new HashSet<>();
    }

    protected final int inGameMinimapId;
    protected Category[] initialCategories;
    protected CheckBox minimap1PreviewButton;
    protected CheckBox minimap2PreviewButton;
    protected Button buttonClose;
    protected Button buttonAbout;
    protected Button renderStatsButton;
    protected Button editGridMinimap1Button;
    protected Button editGridMinimap2Button;
    protected Button editGridFullscreenButton;
    protected SlotMetadata renderStatsSlotMetadata;
    protected CategorySlot cartographyCategorySlot;
    protected ScrollListPane<CategorySlot> optionsListPane;
    protected Map<Category, List<SlotMetadata>> toolbars;
    protected Set<Category> changedCategories;
    protected boolean forceMinimapUpdate;
    protected ButtonList editGridButtons;

    public OptionsManager() {
        this(null);
    }

    public OptionsManager(final GuiScreen returnDisplay) {
        this(returnDisplay, (Category[]) OptionsManager.openCategories.toArray(new Category[0]));
    }

    public OptionsManager(final GuiScreen returnDisplay, final Category... initialCategories) {
        super(String.format("JourneyMap %s %s", Journeymap.JM_VERSION, Constants.getString("jm.common.options")), returnDisplay);
        this.changedCategories = new HashSet<>();
        this.editGridButtons = new ButtonList();
        this.initialCategories = initialCategories;
        this.inGameMinimapId = Journeymap.getClient().getActiveMinimapId();
    }

    @Override
    public void initGui() {
        try {
            this.buttonList.clear();
            if (this.editGridMinimap1Button == null) {
                final String name = Constants.getString("jm.common.grid_edit");
                final String tooltip = Constants.getString("jm.common.grid_edit.tooltip");
                (this.editGridMinimap1Button = new Button(name)).setTooltip(tooltip);
                this.editGridMinimap1Button.setDrawBackground(false);
                (this.editGridMinimap2Button = new Button(name)).setTooltip(tooltip);
                this.editGridMinimap2Button.setDrawBackground(false);
                (this.editGridFullscreenButton = new Button(name)).setTooltip(tooltip);
                this.editGridFullscreenButton.setDrawBackground(false);
                this.editGridButtons = new ButtonList(this.editGridMinimap1Button, this.editGridMinimap2Button, this.editGridFullscreenButton);
            }
            if (this.minimap1PreviewButton == null) {
                final String name = String.format("%s %s", Constants.getString("jm.minimap.preview"), "1");
                final String tooltip = Constants.getString("jm.minimap.preview.tooltip", KeyEventHandler.INSTANCE.kbMinimapPreset.getDisplayName());
                (this.minimap1PreviewButton = new CheckBox(name, false)).setTooltip(tooltip);
                if (FMLClientHandler.instance().getClient().world == null) {
                    this.minimap1PreviewButton.setEnabled(false);
                }
            }
            if (this.minimap2PreviewButton == null) {
                final String name = String.format("%s %s", Constants.getString("jm.minimap.preview"), "2");
                final String tooltip = Constants.getString("jm.minimap.preview.tooltip", KeyEventHandler.INSTANCE.kbMinimapPreset.getDisplayName());
                (this.minimap2PreviewButton = new CheckBox(name, false)).setTooltip(tooltip);
                if (FMLClientHandler.instance().getClient().world == null) {
                    this.minimap2PreviewButton.setEnabled(false);
                }
            }
            if (this.renderStatsButton == null) {
                (this.renderStatsButton = new LabelButton(150, "jm.common.renderstats", 0, 0, 0)).setEnabled(false);
            }
            if (this.optionsListPane == null) {
                final List<ScrollListPane.ISlot> categorySlots = new ArrayList<>();
                final Minecraft mc = this.mc;
                final int width = this.width;
                final int height = this.height;
                this.getClass();
                (this.optionsListPane = new ScrollListPane<>(this, mc, width, height, 35, this.height - 30, 20)).setAlignTop(true);
                this.optionsListPane.setSlots(OptionSlotFactory.getSlots(this.getToolbars()));
                if (this.initialCategories != null) {
                    for (final Category initialCategory : this.initialCategories) {
                        for (final CategorySlot categorySlot : this.optionsListPane.getRootSlots()) {
                            if (categorySlot.getCategory() == initialCategory) {
                                categorySlot.setSelected(true);
                                categorySlots.add(categorySlot);
                            }
                        }
                    }
                }
                for (final ScrollListPane.ISlot rootSlot : this.optionsListPane.getRootSlots()) {
                    if (rootSlot instanceof CategorySlot) {
                        final CategorySlot categorySlot2 = (CategorySlot) rootSlot;
                        final Category category = categorySlot2.getCategory();
                        if (category == null) {
                        }
                        final ResetButton resetButton = new ResetButton(category);
                        final SlotMetadata resetSlotMetadata = new SlotMetadata(resetButton, 1);
                        if (category == ClientCategory.MiniMap1) {
                            if (FMLClientHandler.instance().getClient().world != null) {
                                categorySlot2.getAllChildMetadata().add(new SlotMetadata(this.minimap1PreviewButton, 4));
                            }
                            categorySlot2.getAllChildMetadata().add(new SlotMetadata(this.editGridMinimap1Button, 3));
                        } else if (category == ClientCategory.MiniMap2) {
                            if (FMLClientHandler.instance().getClient().world != null) {
                                categorySlot2.getAllChildMetadata().add(new SlotMetadata(this.minimap2PreviewButton, 4));
                            }
                            categorySlot2.getAllChildMetadata().add(new SlotMetadata(this.editGridMinimap2Button, 3));
                        } else if (category == ClientCategory.FullMap) {
                            categorySlot2.getAllChildMetadata().add(new SlotMetadata(this.editGridMinimap2Button, 3));
                        } else {
                            if (category != ClientCategory.Cartography) {
                                continue;
                            }
                            this.cartographyCategorySlot = categorySlot2;
                            this.renderStatsSlotMetadata = new SlotMetadata(this.renderStatsButton, Constants.getString("jm.common.renderstats.title"), Constants.getString("jm.common.renderstats.tooltip"), 2);
                            categorySlot2.getAllChildMetadata().add(this.renderStatsSlotMetadata);
                        }
                    }
                }
                this.optionsListPane.updateSlots();
                if (!categorySlots.isEmpty()) {
                    this.optionsListPane.scrollTo(categorySlots.get(0));
                }
            } else {
                this.optionsListPane.setDimensions(this.width, this.height, 35, this.height - 30);
                this.optionsListPane.updateSlots();
            }
            this.buttonClose = new Button(Constants.getString("jm.common.close"));
            this.buttonAbout = new Button(Constants.getString("jm.common.splash_about"));
            final ButtonList bottomRow = new ButtonList(this.buttonAbout, this.buttonClose);
            bottomRow.equalizeWidths(this.getFontRenderer());
            bottomRow.setWidths(Math.max(150, this.buttonAbout.getWidth()));
            bottomRow.layoutCenteredHorizontal(this.width / 2, this.height - 25, true, 4);
            this.buttonList.addAll(bottomRow);
        } catch (Throwable t) {
            JMLogger.logOnce("Error in OptionsManager.initGui(): " + t, t);
        }
    }

    @Override
    protected void layoutButtons() {
        if (this.buttonList.isEmpty()) {
            this.initGui();
        }
    }

    @Override
    public void drawScreen(final int x, final int y, final float par3) {
        try {
            if (this.forceMinimapUpdate) {
                if (this.minimap1PreviewButton.isActive()) {
                    UIManager.INSTANCE.switchMiniMapPreset(1);
                } else if (this.minimap2PreviewButton.isActive()) {
                    UIManager.INSTANCE.switchMiniMapPreset(2);
                }
            }
            if (this.mc.world != null) {
                this.updateRenderStats();
            }
            final String[] lastTooltip = this.optionsListPane.lastTooltip;
            final long lastTooltipTime = this.optionsListPane.lastTooltipTime;
            this.optionsListPane.lastTooltip = null;
            this.optionsListPane.drawScreen(x, y, par3);
            super.drawScreen(x, y, par3);
            if (this.previewMiniMap()) {
                UIManager.INSTANCE.getMiniMap().drawMap(true);
                RenderHelper.disableStandardItemLighting();
            }
            if (this.optionsListPane.lastTooltip != null && Arrays.equals(this.optionsListPane.lastTooltip, lastTooltip)) {
                this.optionsListPane.lastTooltipTime = lastTooltipTime;
                if (System.currentTimeMillis() - this.optionsListPane.lastTooltipTime > this.optionsListPane.hoverDelay) {
                    final Button button = this.optionsListPane.lastTooltipMetadata.getButton();
                    this.drawHoveringText(this.optionsListPane.lastTooltip, x, button.getBottomY() + 15);
                }
            }
        } catch (Throwable t) {
            JMLogger.logOnce("Error in OptionsManager.drawScreen(): " + t, t);
        }
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.optionsListPane.handleMouseInput();
    }

    private void updateRenderStats() {
        RenderSpec.getSurfaceSpec();
        RenderSpec.getTopoSpec();
        RenderSpec.getUndergroundSpec();
        for (final ScrollListPane.ISlot rootSlot : this.optionsListPane.getRootSlots()) {
            if (rootSlot instanceof CategorySlot) {
                final CategorySlot categorySlot = (CategorySlot) rootSlot;
                if (categorySlot.getCategory() != ClientCategory.Cartography) {
                    continue;
                }
                final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
                for (final SlotMetadata slotMetadata : categorySlot.getAllChildMetadata()) {
                    if (slotMetadata.getButton() instanceof IConfigFieldHolder) {
                        final Object property = ((IConfigFieldHolder) slotMetadata.getButton()).getConfigField();
                        boolean limitButtonRange = false;
                        if (property == coreProperties.renderDistanceCaveMax) {
                            limitButtonRange = true;
                            slotMetadata.getButton().resetLabelColors();
                        } else if (property == coreProperties.renderDistanceSurfaceMax) {
                            limitButtonRange = true;
                            slotMetadata.getButton().resetLabelColors();
                        }
                        if (!limitButtonRange) {
                            continue;
                        }
                        final IntSliderButton button = (IntSliderButton) slotMetadata.getButton();
                        button.maxValue = this.mc.gameSettings.renderDistanceChunks;
                        if (button.getValue() <= this.mc.gameSettings.renderDistanceChunks) {
                            continue;
                        }
                        button.setValue(this.mc.gameSettings.renderDistanceChunks);
                    }
                }
            }
        }
        this.renderStatsButton.displayString = (Journeymap.getClient().getCoreProperties().mappingEnabled.get() ? MapPlayerTask.getSimpleStats() : Constants.getString("jm.common.enable_mapping_false_text"));
        if (this.cartographyCategorySlot != null) {
            this.renderStatsButton.setWidth(this.cartographyCategorySlot.getCurrentColumnWidth());
        }
    }

    @Override
    public void drawBackground(final int layer) {
    }

    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseEvent) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseEvent);
        final boolean pressed = this.optionsListPane.mouseClicked(mouseX, mouseY, mouseEvent);
        if (pressed) {
            this.checkPressedButton();
        }
    }

    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int mouseEvent) {
        super.mouseReleased(mouseX, mouseY, mouseEvent);
        this.optionsListPane.mouseReleased(mouseX, mouseY, mouseEvent);
    }

    protected void mouseClickMove(final int mouseX, final int mouseY, final int lastButtonClicked, final long timeSinceMouseClick) {
        super.mouseClickMove(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        this.checkPressedButton();
    }

    protected void checkPressedButton() {
        final SlotMetadata slotMetadata = this.optionsListPane.getLastPressed();
        if (slotMetadata != null) {
            if (slotMetadata.getButton() instanceof ResetButton) {
                this.resetOptions(((ResetButton) slotMetadata.getButton()).category);
            }
            if (slotMetadata.getName().equals(Constants.getString("jm.common.ui_theme"))) {
                ThemeLoader.getCurrentTheme(true);
                if (this.previewMiniMap()) {
                    UIManager.INSTANCE.getMiniMap().updateDisplayVars(true);
                }
            }
            if (this.editGridButtons.contains(slotMetadata.getButton())) {
                UIManager.INSTANCE.openGridEditor(this);
                return;
            }
            if (slotMetadata.getButton() == this.minimap1PreviewButton) {
                this.minimap2PreviewButton.setToggled(false);
                UIManager.INSTANCE.switchMiniMapPreset(1);
                UIManager.INSTANCE.getMiniMap().resetInitTime();
            }
            if (slotMetadata.getButton() == this.minimap2PreviewButton) {
                this.minimap1PreviewButton.setToggled(false);
                UIManager.INSTANCE.switchMiniMapPreset(2);
                UIManager.INSTANCE.getMiniMap().resetInitTime();
            }
        }
        final CategorySlot categorySlot = (CategorySlot) this.optionsListPane.getLastPressedParentSlot();
        if (categorySlot != null) {
            final Category category = categorySlot.getCategory();
            this.changedCategories.add(category);
            if (category == ClientCategory.MiniMap1 || category == ClientCategory.MiniMap2) {
                this.refreshMinimapOptions();
                DataCache.INSTANCE.resetRadarCaches();
                UIManager.INSTANCE.getMiniMap().updateDisplayVars(true);
            }
            if (category == ClientCategory.Cartography) {
                Journeymap.getClient().getCoreProperties().save();
                RenderSpec.resetRenderSpecs();
            }
        }
    }

    protected void actionPerformed(final GuiButton button) {
        if (button == this.buttonClose) {
            this.closeAndReturn();
            return;
        }
        if (button == this.buttonAbout) {
            UIManager.INSTANCE.openSplash(this);
            return;
        }
        if (button == this.minimap1PreviewButton) {
            this.minimap2PreviewButton.setToggled(false);
            UIManager.INSTANCE.switchMiniMapPreset(1);
        }
        if (button == this.minimap2PreviewButton) {
            this.minimap1PreviewButton.setToggled(false);
            UIManager.INSTANCE.switchMiniMapPreset(2);
        }
    }

    @Override
    protected void keyTyped(final char c, final int key) {
        switch (key) {
            case 1: {
                if (this.previewMiniMap()) {
                    this.minimap1PreviewButton.setToggled(false);
                    this.minimap2PreviewButton.setToggled(false);
                    break;
                }
                this.closeAndReturn();
                break;
            }
        }
        final boolean optionUpdated = this.optionsListPane.keyTyped(c, key);
        if (optionUpdated && this.previewMiniMap()) {
            UIManager.INSTANCE.getMiniMap().updateDisplayVars(true);
        }
    }

    protected void resetOptions(final Category category) {
        final Set<PropertiesBase> updatedProperties = new HashSet<PropertiesBase>();
        for (final CategorySlot categorySlot : this.optionsListPane.getRootSlots()) {
            if (category.equals(categorySlot.getCategory())) {
                for (final SlotMetadata slotMetadata : categorySlot.getAllChildMetadata()) {
                    slotMetadata.resetToDefaultValue();
                    if (slotMetadata.hasConfigField()) {
                        final PropertiesBase properties = slotMetadata.getProperties();
                        if (properties == null) {
                            continue;
                        }
                        updatedProperties.add(properties);
                    }
                }
                break;
            }
        }
        for (final PropertiesBase properties2 : updatedProperties) {
            properties2.save();
        }
        RenderSpec.resetRenderSpecs();
    }

    public boolean previewMiniMap() {
        return this.minimap1PreviewButton.getToggled() || this.minimap2PreviewButton.getToggled();
    }

    public void refreshMinimapOptions() {
        final Set<Category> cats = new HashSet<>();
        cats.add(ClientCategory.MiniMap1);
        cats.add(ClientCategory.MiniMap2);
        for (final CategorySlot categorySlot : this.optionsListPane.getRootSlots()) {
            if (cats.contains(categorySlot.getCategory())) {
                for (final SlotMetadata slotMetadata : categorySlot.getAllChildMetadata()) {
                    slotMetadata.getButton().refresh();
                }
            }
        }
    }

    @Override
    protected void closeAndReturn() {
        Journeymap.getClient().getCoreProperties().optionsManagerViewed.set(Journeymap.JM_VERSION.toString());
        Journeymap.getClient().saveConfigProperties();
        if (this.mc.world != null) {
            UIManager.INSTANCE.getMiniMap().setMiniMapProperties(Journeymap.getClient().getMiniMapProperties(this.inGameMinimapId));
            for (final Category category : this.changedCategories) {
                if (category == ClientCategory.MiniMap1) {
                    DataCache.INSTANCE.resetRadarCaches();
                    UIManager.INSTANCE.getMiniMap().reset();
                } else if (category == ClientCategory.MiniMap2) {
                    DataCache.INSTANCE.resetRadarCaches();
                } else if (category == ClientCategory.FullMap) {
                    DataCache.INSTANCE.resetRadarCaches();
                    ThemeLoader.getCurrentTheme(true);
                } else if (category == ClientCategory.WebMap) {
                    DataCache.INSTANCE.resetRadarCaches();
                    WebServer.setEnabled(Journeymap.getClient().getWebMapProperties().enabled.get(), true);
                } else if (category == ClientCategory.Waypoint) {
                    WaypointStore.INSTANCE.reset();
                } else {
                    if (category == ClientCategory.WaypointBeacon) {
                        continue;
                    }
                    if (category == ClientCategory.Cartography) {
                        ColorManager.INSTANCE.reset();
                        ModBlockDelegate.INSTANCE.reset();
                        BlockMD.reset();
                        RenderSpec.resetRenderSpecs();
                        TileDrawStepCache.instance().invalidateAll();
                        MiniMap.state().requireRefresh();
                        Fullscreen.state().requireRefresh();
                        MapPlayerTask.forceNearbyRemap();
                    } else {
                        if (category != ClientCategory.Advanced) {
                            continue;
                        }
                        SoftResetTask.queue();
                        WebServer.setEnabled(Journeymap.getClient().getWebMapProperties().enabled.get(), false);
                    }
                }
            }
            UIManager.INSTANCE.getMiniMap().reset();
            UIManager.INSTANCE.getMiniMap().updateDisplayVars(true);
        }
        if (this.returnDisplay != null && this.returnDisplay instanceof Fullscreen) {
            ((Fullscreen) this.returnDisplay).reset();
        }
        OptionsManager.openCategories.clear();
        for (final CategorySlot categorySlot : this.optionsListPane.getRootSlots()) {
            if (categorySlot.isSelected()) {
                OptionsManager.openCategories.add(categorySlot.getCategory());
            }
        }
        super.closeAndReturn();
    }

    Map<Category, List<SlotMetadata>> getToolbars() {
        if (this.toolbars == null) {
            this.toolbars = new HashMap<>();
            for (final Category category : ClientCategory.values) {
                final String name = Constants.getString("jm.config.reset");
                final String tooltip = Constants.getString("jm.config.reset.tooltip");
                final SlotMetadata toolbarSlotMetadata = new SlotMetadata(new ResetButton(category), name, tooltip);
                this.toolbars.put(category, Collections.singletonList(toolbarSlotMetadata));
            }
        }
        return this.toolbars;
    }

    public static class ResetButton extends Button {
        public final Category category;

        public ResetButton(final Category category) {
            super(Constants.getString("jm.config.reset"));
            this.category = category;
            this.setTooltip(Constants.getString("jm.config.reset.tooltip"));
            this.setDrawBackground(false);
            this.setLabelColors(16711680, 16711680, null);
        }
    }

    public static class LabelButton extends Button {
        DrawUtil.HAlign hAlign;

        public LabelButton(final int width, final String key, final Object... labelArgs) {
            super(Constants.getString(key, labelArgs));
            this.hAlign = DrawUtil.HAlign.Left;
            this.setTooltip(Constants.getString(key + ".tooltip"));
            this.setDrawBackground(false);
            this.setDrawFrame(false);
            this.setEnabled(false);
            this.setLabelColors(12632256, 12632256, 12632256);
            this.setWidth(width);
        }

        @Override
        public int getFitWidth(final FontRenderer fr) {
            return this.width;
        }

        @Override
        public void fitWidth(final FontRenderer fr) {
        }

        public void setHAlign(final DrawUtil.HAlign hAlign) {
            this.hAlign = hAlign;
        }

        @Override
        public void drawButton(final Minecraft minecraft, final int mouseX, final int mouseY, final float ticks) {
            int labelX;
            switch (this.hAlign) {
                case Left: {
                    labelX = this.getRightX();
                    break;
                }
                case Right: {
                    labelX = this.getX();
                    break;
                }
                default: {
                    labelX = this.getCenterX();
                    break;
                }
            }
            DrawUtil.drawLabel(this.displayString, labelX, this.getMiddleY(), this.hAlign, DrawUtil.VAlign.Middle, null, 0.0f, this.labelColor, 1.0f, 1.0, this.drawLabelShadow);
        }
    }
}
