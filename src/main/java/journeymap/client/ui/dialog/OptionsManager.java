package journeymap.client.ui.dialog;

import journeymap.common.*;
import journeymap.client.*;
import journeymap.client.forge.event.*;
import journeymap.client.ui.option.*;
import journeymap.client.log.*;
import net.minecraft.client.*;
import journeymap.client.ui.*;
import net.minecraft.client.renderer.*;
import java.io.*;
import journeymap.client.ui.component.*;
import journeymap.client.task.multi.*;
import journeymap.client.properties.*;
import journeymap.client.io.*;
import journeymap.client.data.*;
import journeymap.common.properties.*;
import journeymap.client.service.*;
import journeymap.client.waypoint.*;
import journeymap.client.cartography.color.*;
import journeymap.client.mod.*;
import journeymap.client.model.*;
import journeymap.client.render.map.*;
import journeymap.client.ui.minimap.*;
import journeymap.client.ui.fullscreen.*;
import journeymap.client.task.main.*;
import java.util.*;
import journeymap.client.render.draw.*;
import net.minecraft.client.gui.*;

public class OptionsManager extends JmUI
{
    protected static Set<Category> openCategories;
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
        this((GuiScreen)null);
    }
    
    public OptionsManager(final GuiScreen returnDisplay) {
        this(returnDisplay, (Category[])OptionsManager.openCategories.toArray(new Category[0]));
    }
    
    public OptionsManager(final GuiScreen returnDisplay, final Category... initialCategories) {
        super(String.format("JourneyMap %s %s", Journeymap.JM_VERSION, Constants.getString("jm.common.options")), returnDisplay);
        this.changedCategories = new HashSet<Category>();
        this.editGridButtons = new ButtonList();
        this.initialCategories = initialCategories;
        this.inGameMinimapId = Journeymap.getClient().getActiveMinimapId();
    }
    
    @Override
    public void func_73866_w_() {
        try {
            this.field_146292_n.clear();
            if (this.editGridMinimap1Button == null) {
                final String name = Constants.getString("jm.common.grid_edit");
                final String tooltip = Constants.getString("jm.common.grid_edit.tooltip");
                (this.editGridMinimap1Button = new Button(name)).setTooltip(tooltip);
                this.editGridMinimap1Button.setDrawBackground(false);
                (this.editGridMinimap2Button = new Button(name)).setTooltip(tooltip);
                this.editGridMinimap2Button.setDrawBackground(false);
                (this.editGridFullscreenButton = new Button(name)).setTooltip(tooltip);
                this.editGridFullscreenButton.setDrawBackground(false);
                this.editGridButtons = new ButtonList(new Button[] { this.editGridMinimap1Button, this.editGridMinimap2Button, this.editGridFullscreenButton });
            }
            if (this.minimap1PreviewButton == null) {
                final String name = String.format("%s %s", Constants.getString("jm.minimap.preview"), "1");
                final String tooltip = Constants.getString("jm.minimap.preview.tooltip", KeyEventHandler.INSTANCE.kbMinimapPreset.getDisplayName());
                (this.minimap1PreviewButton = new CheckBox(name, false)).setTooltip(tooltip);
                if (Journeymap.clientWorld() == null) {
                    this.minimap1PreviewButton.setEnabled(false);
                }
            }
            if (this.minimap2PreviewButton == null) {
                final String name = String.format("%s %s", Constants.getString("jm.minimap.preview"), "2");
                final String tooltip = Constants.getString("jm.minimap.preview.tooltip", KeyEventHandler.INSTANCE.kbMinimapPreset.getDisplayName());
                (this.minimap2PreviewButton = new CheckBox(name, false)).setTooltip(tooltip);
                if (Journeymap.clientWorld() == null) {
                    this.minimap2PreviewButton.setEnabled(false);
                }
            }
            if (this.renderStatsButton == null) {
                (this.renderStatsButton = new LabelButton(150, "jm.common.renderstats", new Object[] { 0, 0, 0 })).setEnabled(false);
            }
            if (this.optionsListPane == null) {
                final List<ScrollListPane.ISlot> categorySlots = new ArrayList<ScrollListPane.ISlot>();
                final Minecraft field_146297_k = this.field_146297_k;
                final int field_146294_l = this.field_146294_l;
                final int field_146295_m = this.field_146295_m;
                this.getClass();
                (this.optionsListPane = new ScrollListPane<CategorySlot>(this, field_146297_k, field_146294_l, field_146295_m, 35, this.field_146295_m - 30, 20)).setAlignTop(true);
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
                        final CategorySlot categorySlot2 = (CategorySlot)rootSlot;
                        final Category category = categorySlot2.getCategory();
                        if (category == null) {}
                        final ResetButton resetButton = new ResetButton(category);
                        final SlotMetadata resetSlotMetadata = new SlotMetadata(resetButton, 1);
                        if (category == ClientCategory.MiniMap1) {
                            if (Journeymap.clientWorld() != null) {
                                categorySlot2.getAllChildMetadata().add(new SlotMetadata(this.minimap1PreviewButton, 4));
                            }
                            categorySlot2.getAllChildMetadata().add(new SlotMetadata(this.editGridMinimap1Button, 3));
                        }
                        else if (category == ClientCategory.MiniMap2) {
                            if (Journeymap.clientWorld() != null) {
                                categorySlot2.getAllChildMetadata().add(new SlotMetadata(this.minimap2PreviewButton, 4));
                            }
                            categorySlot2.getAllChildMetadata().add(new SlotMetadata(this.editGridMinimap2Button, 3));
                        }
                        else if (category == ClientCategory.FullMap) {
                            categorySlot2.getAllChildMetadata().add(new SlotMetadata(this.editGridMinimap2Button, 3));
                        }
                        else {
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
            }
            else {
                this.optionsListPane.func_148122_a(this.field_146294_l, this.field_146295_m, 35, this.field_146295_m - 30);
                this.optionsListPane.updateSlots();
            }
            this.buttonClose = new Button(Constants.getString("jm.common.close"));
            this.buttonAbout = new Button(Constants.getString("jm.common.splash_about"));
            final ButtonList bottomRow = new ButtonList(new Button[] { this.buttonAbout, this.buttonClose });
            bottomRow.equalizeWidths(this.getFontRenderer());
            bottomRow.setWidths(Math.max(150, this.buttonAbout.getWidth()));
            bottomRow.layoutCenteredHorizontal(this.field_146294_l / 2, this.field_146295_m - 25, true, 4);
            this.field_146292_n.addAll(bottomRow);
        }
        catch (Throwable t) {
            JMLogger.logOnce("Error in OptionsManager.initGui(): " + t, t);
        }
    }
    
    @Override
    protected void layoutButtons() {
        if (this.field_146292_n.isEmpty()) {
            this.func_73866_w_();
        }
    }
    
    @Override
    public void func_73863_a(final int mouseX, final int mouseY, final float par3) {
        try {
            if (this.forceMinimapUpdate) {
                if (this.minimap1PreviewButton.isActive()) {
                    UIManager.INSTANCE.switchMiniMapPreset(1);
                }
                else if (this.minimap2PreviewButton.isActive()) {
                    UIManager.INSTANCE.switchMiniMapPreset(2);
                }
            }
            if (Journeymap.clientWorld() != null) {
                this.updateRenderStats();
            }
            final String[] lastTooltip = this.optionsListPane.lastTooltip;
            final long lastTooltipTime = this.optionsListPane.lastTooltipTime;
            this.optionsListPane.lastTooltip = null;
            this.optionsListPane.func_148128_a(mouseX, mouseY, par3);
            super.func_73863_a(mouseX, mouseY, par3);
            if (this.previewMiniMap()) {
                UIManager.INSTANCE.getMiniMap().drawMap(true);
                RenderHelper.func_74518_a();
            }
            if (this.optionsListPane.lastTooltip != null && Arrays.equals(this.optionsListPane.lastTooltip, lastTooltip)) {
                this.optionsListPane.lastTooltipTime = lastTooltipTime;
                if (System.currentTimeMillis() - this.optionsListPane.lastTooltipTime > this.optionsListPane.hoverDelay) {
                    final Button button = this.optionsListPane.lastTooltipMetadata.getButton();
                    this.drawHoveringText(this.optionsListPane.lastTooltip, mouseX, button.getBottomY() + 15);
                }
            }
        }
        catch (Throwable t) {
            JMLogger.logOnce("Error in OptionsManager.drawScreen(): " + t, t);
        }
    }
    
    public void func_146274_d() throws IOException {
        super.func_146274_d();
        this.optionsListPane.func_178039_p();
    }
    
    private void updateRenderStats() {
        RenderSpec.getSurfaceSpec();
        RenderSpec.getTopoSpec();
        RenderSpec.getUndergroundSpec();
        for (final ScrollListPane.ISlot rootSlot : this.optionsListPane.getRootSlots()) {
            if (rootSlot instanceof CategorySlot) {
                final CategorySlot categorySlot = (CategorySlot)rootSlot;
                if (categorySlot.getCategory() != ClientCategory.Cartography) {
                    continue;
                }
                final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
                for (final SlotMetadata slotMetadata : categorySlot.getAllChildMetadata()) {
                    if (slotMetadata.getButton() instanceof IConfigFieldHolder) {
                        final Object property = ((IConfigFieldHolder)slotMetadata.getButton()).getConfigField();
                        boolean limitButtonRange = false;
                        if (property == coreProperties.renderDistanceCaveMax) {
                            limitButtonRange = true;
                            slotMetadata.getButton().resetLabelColors();
                        }
                        else if (property == coreProperties.renderDistanceSurfaceMax) {
                            limitButtonRange = true;
                            slotMetadata.getButton().resetLabelColors();
                        }
                        if (!limitButtonRange) {
                            continue;
                        }
                        final IntSliderButton button = (IntSliderButton)slotMetadata.getButton();
                        button.maxValue = this.field_146297_k.field_71474_y.field_151451_c;
                        if (button.getValue() <= this.field_146297_k.field_71474_y.field_151451_c) {
                            continue;
                        }
                        button.setValue(this.field_146297_k.field_71474_y.field_151451_c);
                    }
                }
            }
        }
        this.renderStatsButton.field_146126_j = (Journeymap.getClient().getCoreProperties().mappingEnabled.get() ? MapPlayerTask.getSimpleStats() : Constants.getString("jm.common.enable_mapping_false_text"));
        if (this.cartographyCategorySlot != null) {
            this.renderStatsButton.func_175211_a(this.cartographyCategorySlot.getCurrentColumnWidth());
        }
    }
    
    @Override
    public void func_146278_c(final int layer) {
    }
    
    protected void func_73864_a(final int mouseX, final int mouseY, final int mouseEvent) throws IOException {
        super.func_73864_a(mouseX, mouseY, mouseEvent);
        final boolean pressed = this.optionsListPane.func_148179_a(mouseX, mouseY, mouseEvent);
        if (pressed) {
            this.checkPressedButton();
        }
    }
    
    @Override
    protected void func_146286_b(final int mouseX, final int mouseY, final int mouseEvent) {
        super.func_146286_b(mouseX, mouseY, mouseEvent);
        this.optionsListPane.func_148181_b(mouseX, mouseY, mouseEvent);
    }
    
    protected void func_146273_a(final int mouseX, final int mouseY, final int lastButtonClicked, final long timeSinceMouseClick) {
        super.func_146273_a(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        this.checkPressedButton();
    }
    
    protected void checkPressedButton() {
        final SlotMetadata slotMetadata = this.optionsListPane.getLastPressed();
        if (slotMetadata != null) {
            if (slotMetadata.getButton() instanceof ResetButton) {
                this.resetOptions(((ResetButton)slotMetadata.getButton()).category);
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
        final CategorySlot categorySlot = (CategorySlot)this.optionsListPane.getLastPressedParentSlot();
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
    
    protected void func_146284_a(final GuiButton button) {
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
    protected void func_73869_a(final char c, final int key) {
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
        final Set<Category> cats = new HashSet<Category>();
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
        if (Journeymap.clientWorld() != null) {
            UIManager.INSTANCE.getMiniMap().setMiniMapProperties(Journeymap.getClient().getMiniMapProperties(this.inGameMinimapId));
            for (final Category category : this.changedCategories) {
                if (category == ClientCategory.MiniMap1) {
                    DataCache.INSTANCE.resetRadarCaches();
                    UIManager.INSTANCE.getMiniMap().reset();
                }
                else if (category == ClientCategory.MiniMap2) {
                    DataCache.INSTANCE.resetRadarCaches();
                }
                else if (category == ClientCategory.FullMap) {
                    DataCache.INSTANCE.resetRadarCaches();
                    ThemeLoader.getCurrentTheme(true);
                }
                else if (category == ClientCategory.WebMap) {
                    DataCache.INSTANCE.resetRadarCaches();
                    WebServer.setEnabled(Journeymap.getClient().getWebMapProperties().enabled.get(), true);
                }
                else if (category == ClientCategory.Waypoint) {
                    WaypointStore.INSTANCE.reset();
                }
                else {
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
                    }
                    else {
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
            ((Fullscreen)this.returnDisplay).reset();
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
            this.toolbars = new HashMap<Category, List<SlotMetadata>>();
            for (final Category category : ClientCategory.values) {
                final String name = Constants.getString("jm.config.reset");
                final String tooltip = Constants.getString("jm.config.reset.tooltip");
                final SlotMetadata toolbarSlotMetadata = new SlotMetadata(new ResetButton(category), name, tooltip);
                this.toolbars.put(category, (List<SlotMetadata>)Arrays.asList(toolbarSlotMetadata));
            }
        }
        return this.toolbars;
    }
    
    static {
        OptionsManager.openCategories = new HashSet<Category>();
    }
    
    public static class ResetButton extends Button
    {
        public final Category category;
        
        public ResetButton(final Category category) {
            super(Constants.getString("jm.config.reset"));
            this.category = category;
            this.setTooltip(Constants.getString("jm.config.reset.tooltip"));
            this.setDrawBackground(false);
            this.setLabelColors(16711680, 16711680, null);
        }
    }
    
    public static class LabelButton extends Button
    {
        DrawUtil.HAlign hAlign;
        
        public LabelButton(final int width, final String key, final Object... labelArgs) {
            super(Constants.getString(key, labelArgs));
            this.hAlign = DrawUtil.HAlign.Left;
            this.setTooltip(Constants.getString(key + ".tooltip"));
            this.setDrawBackground(false);
            this.setDrawFrame(false);
            this.setEnabled(false);
            this.setLabelColors(12632256, 12632256, 12632256);
            this.func_175211_a(width);
        }
        
        @Override
        public int getFitWidth(final FontRenderer fr) {
            return this.field_146120_f;
        }
        
        @Override
        public void fitWidth(final FontRenderer fr) {
        }
        
        public void setHAlign(final DrawUtil.HAlign hAlign) {
            this.hAlign = hAlign;
        }
        
        @Override
        public void func_191745_a(final Minecraft minecraft, final int mouseX, final int mouseY, final float ticks) {
            int labelX = 0;
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
            DrawUtil.drawLabel(this.field_146126_j, labelX, this.getMiddleY(), this.hAlign, DrawUtil.VAlign.Middle, null, 0.0f, this.labelColor, 1.0f, 1.0, this.drawLabelShadow);
        }
    }
}
