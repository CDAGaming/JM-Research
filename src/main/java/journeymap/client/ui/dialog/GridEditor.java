package journeymap.client.ui.dialog;

import journeymap.client.model.*;
import java.awt.geom.*;
import journeymap.client.ui.component.*;
import net.minecraft.util.*;
import journeymap.client.*;
import journeymap.client.render.texture.*;
import journeymap.common.*;
import org.lwjgl.input.*;
import journeymap.common.properties.*;
import java.util.*;
import journeymap.common.properties.config.*;
import journeymap.client.io.*;
import journeymap.common.log.*;
import journeymap.client.ui.*;
import journeymap.client.ui.theme.*;
import net.minecraft.client.gui.*;
import java.awt.image.*;
import journeymap.client.render.draw.*;
import java.awt.*;

public class GridEditor extends JmUI
{
    private final TextureImpl colorPickTexture;
    private final int tileSize = 128;
    private final int sampleTextureSize = 128;
    private GridSpecs gridSpecs;
    private ListPropertyButton<GridSpec.Style> buttonStyle;
    private IntSliderButton buttonOpacity;
    private CheckBox checkDay;
    private CheckBox checkNight;
    private CheckBox checkUnderground;
    private ThemeToggle buttonDay;
    private ThemeToggle buttonNight;
    private ThemeToggle buttonUnderground;
    private Integer activeColor;
    private MapView activeMapType;
    private Button buttonReset;
    private Button buttonCancel;
    private Button buttonClose;
    private Rectangle2D.Double colorPickRect;
    private BufferedImage colorPickImg;
    private ButtonList topButtons;
    private ButtonList leftButtons;
    private ButtonList leftChecks;
    private ButtonList bottomButtons;
    private ResourceLocation colorPicResource;
    
    public GridEditor(final JmUI returnDisplay) {
        super(Constants.getString("jm.common.grid_editor"), returnDisplay);
        this.colorPicResource = ((Constants.birthdayMessage() == null) ? TextureCache.ColorPicker : TextureCache.ColorPicker2);
        this.colorPickImg = TextureCache.resolveImage(this.colorPicResource);
        this.colorPickTexture = TextureCache.getTexture(this.colorPicResource);
        this.colorPickRect = new Rectangle2D.Double(0.0, 0.0, this.colorPickTexture.getWidth(), this.colorPickTexture.getHeight());
        this.gridSpecs = Journeymap.getClient().getCoreProperties().gridSpecs.clone();
        this.activeMapType = MapView.day(0);
        this.activeColor = this.gridSpecs.getSpec(this.activeMapType).getColor();
        Keyboard.enableRepeatEvents(true);
    }
    
    @Override
    public void func_73866_w_() {
        try {
            if (this.field_146292_n.isEmpty()) {
                final GridSpec spec = this.gridSpecs.getSpec(this.activeMapType);
                this.buttonStyle = new ListPropertyButton<GridSpec.Style>(EnumSet.allOf(GridSpec.Style.class), Constants.getString("jm.common.grid_style"), new EnumField<GridSpec.Style>(Category.Hidden, "", spec.style));
                this.buttonOpacity = new IntSliderButton(new IntegerField(Category.Hidden, "", 0, 100, (int)Math.ceil(spec.alpha * 100.0f)), Constants.getString("jm.common.grid_opacity") + " : ", "", 0, 100, true);
                (this.topButtons = new ButtonList(new Button[] { this.buttonStyle, this.buttonOpacity })).equalizeWidths(this.getFontRenderer());
                this.checkDay = new CheckBox("", this.activeMapType == MapView.day(0));
                this.checkNight = new CheckBox("", this.activeMapType == MapView.night(0));
                this.checkUnderground = new CheckBox("", this.activeMapType.isUnderground());
                this.leftChecks = new ButtonList(new Button[] { this.checkDay, this.checkNight, this.checkUnderground });
                final Theme theme = ThemeLoader.getCurrentTheme();
                (this.buttonDay = new ThemeToggle(theme, "jm.fullscreen.map_day", "day")).setToggled(this.activeMapType == MapView.day(0), false);
                (this.buttonNight = new ThemeToggle(theme, "jm.fullscreen.map_night", "night")).setToggled(this.activeMapType == MapView.night(0), false);
                (this.buttonUnderground = new ThemeToggle(theme, "jm.fullscreen.map_caves", "caves")).setToggled(this.activeMapType.isUnderground(), false);
                this.leftButtons = new ButtonList(new Button[] { this.buttonDay, this.buttonNight, this.buttonUnderground });
                this.buttonReset = new Button(Constants.getString("jm.waypoint.reset"));
                this.buttonCancel = new Button(Constants.getString("jm.waypoint.cancel"));
                this.buttonClose = new Button(Constants.getString("jm.waypoint.save"));
                (this.bottomButtons = new ButtonList(new Button[] { this.buttonReset, this.buttonCancel, this.buttonClose })).equalizeWidths(this.getFontRenderer());
                this.field_146292_n.addAll(this.topButtons);
                this.field_146292_n.addAll(this.leftChecks);
                this.field_146292_n.addAll(this.leftButtons);
                this.field_146292_n.addAll(this.bottomButtons);
                this.updatePreview(this.activeMapType);
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toString(t));
            UIManager.INSTANCE.closeAll();
        }
    }
    
    @Override
    protected void layoutButtons() {
        try {
            this.func_73866_w_();
            final int hgap = 6;
            final int vgap = 6;
            final int startY = Math.max(40, (this.field_146295_m - 230) / 2);
            final int centerX = this.field_146294_l / 2;
            final int cpSize = this.topButtons.getHeight(6);
            final int topRowWidth = 6 + cpSize + this.topButtons.get(0).getWidth();
            final int topRowLeft = centerX - topRowWidth / 2;
            this.topButtons.layoutVertical(topRowLeft + 6 + cpSize, startY, true, 6);
            this.drawColorPicker(topRowLeft, this.topButtons.getTopY(), cpSize);
            final int tileX = centerX - 64;
            final int tileY = this.topButtons.getBottomY() + 12;
            this.drawMapTile(tileX, tileY);
            this.leftButtons.layoutVertical(tileX - this.leftButtons.get(0).getWidth() - 6, tileY + 6, true, 6);
            this.leftChecks.setHeights(this.leftButtons.get(0).getHeight());
            this.leftChecks.setWidths(15);
            this.leftChecks.layoutVertical(this.leftButtons.getLeftX() - this.checkDay.getWidth(), this.leftButtons.getTopY(), true, 6);
            final int bottomY = Math.min(tileY + 128 + 12, this.field_146295_m - 10 - this.buttonClose.getHeight());
            this.bottomButtons.equalizeWidths(this.getFontRenderer(), 6, this.topButtons.get(0).getRightX() - topRowLeft);
            this.bottomButtons.layoutCenteredHorizontal(centerX, bottomY, true, 6);
        }
        catch (Throwable t) {
            this.logger.error("Error in GridEditor.layoutButtons: " + LogFormatter.toString(t));
        }
    }
    
    @Override
    public void func_73863_a(final int mouseX, final int mouseY, final float par3) {
        try {
            this.func_146278_c(0);
            this.layoutButtons();
            for (int k = 0; k < this.field_146292_n.size(); ++k) {
                final GuiButton guibutton = this.field_146292_n.get(k);
                guibutton.func_191745_a(this.field_146297_k, mouseX, mouseY, par3);
            }
            this.drawTitle();
            this.drawLogo();
        }
        catch (Throwable t) {
            this.logger.error("Error in GridEditor.drawScreen: " + LogFormatter.toString(t));
        }
    }
    
    protected void drawColorPicker(final int x, final int y, final float size) {
        final int sizeI = (int)size;
        func_73734_a(x - 1, y - 1, x + sizeI + 1, y + sizeI + 1, -6250336);
        if (this.colorPickRect.width != size) {
            final Image image = this.colorPickTexture.getImage().getScaledInstance(sizeI, sizeI, 2);
            this.colorPickImg = new BufferedImage(sizeI, sizeI, 1);
            final Graphics g = this.colorPickImg.createGraphics();
            g.drawImage(image, 0, 0, sizeI, sizeI, null);
            g.dispose();
        }
        this.colorPickRect.setRect(x, y, size, size);
        final float scale = size / this.colorPickTexture.getWidth();
        DrawUtil.drawImage(this.colorPickTexture, x, y, false, scale, 0.0);
        final GridSpec activeSpec = this.gridSpecs.getSpec(this.activeMapType);
        int colorX = activeSpec.getColorX();
        int colorY = activeSpec.getColorY();
        if (colorX > 0 && colorY > 0) {
            colorX += x;
            colorY += y;
            DrawUtil.drawRectangle(colorX - 2, colorY - 2, 5.0, 5.0, Color.darkGray.getRGB(), 0.8f);
            DrawUtil.drawRectangle(colorX - 1, colorY, 3.0, 1.0, this.activeColor, 1.0f);
            DrawUtil.drawRectangle(colorX, colorY - 1, 1.0, 3.0, this.activeColor, 1.0f);
        }
    }
    
    protected void drawMapTile(final int x, final int y) {
        final float scale = 1.0f;
        func_73734_a(x - 1, y - 1, x + 128 + 1, y + 128 + 1, -6250336);
        final TextureImpl tileTex = this.getTileSample(this.activeMapType);
        DrawUtil.drawImage(tileTex, x, y, false, 1.0f, 0.0);
        if (scale == 2.0f) {
            DrawUtil.drawImage(tileTex, x + 128, y, true, 1.0f, 0.0);
            DrawUtil.drawImage(tileTex, x, y + 128, true, 1.0f, 180.0);
            DrawUtil.drawImage(tileTex, x + 128, y + 128, false, 1.0f, 180.0);
        }
        final GridSpec gridSpec = this.gridSpecs.getSpec(this.activeMapType);
        gridSpec.beginTexture(9728, 33071, 1.0f);
        DrawUtil.drawBoundTexture(0.0, 0.0, x, y, 0.0, 0.25, 0.25, x + 128, y + 128);
        gridSpec.finishTexture();
    }
    
    protected void drawLabel(final String label, final int x, final int y) {
        this.func_73731_b(this.getFontRenderer(), label, x, y, Color.cyan.getRGB());
    }
    
    @Override
    protected void func_73869_a(final char par1, final int par2) {
        try {
            switch (par2) {
                case 1: {
                    this.closeAndReturn();
                }
                case 28: {
                    this.saveAndClose();
                }
            }
        }
        catch (Throwable t) {
            this.logger.error("Error in GridEditor.keyTyped: " + LogFormatter.toString(t));
        }
    }
    
    protected void func_146273_a(final int par1, final int par2, final int par3, final long par4) {
        try {
            if (this.buttonOpacity.dragging) {
                this.updateGridSpecs();
            }
            else {
                this.checkColorPicker(par1, par2);
            }
        }
        catch (Throwable t) {
            this.logger.error("Error in GridEditor.mouseClickMove: " + LogFormatter.toString(t));
        }
    }
    
    protected void func_73864_a(final int mouseX, final int mouseY, final int mouseButton) {
        try {
            super.func_73864_a(mouseX, mouseY, mouseButton);
            if (mouseButton == 0) {
                this.checkColorPicker(mouseX, mouseY);
            }
        }
        catch (Throwable t) {
            this.logger.error("Error in GridEditor.mouseClicked: " + LogFormatter.toString(t));
        }
    }
    
    protected void checkColorPicker(final int mouseX, final int mouseY) {
        if (this.colorPickRect.contains(mouseX, mouseY)) {
            final int x = mouseX - (int)this.colorPickRect.x;
            final int y = mouseY - (int)this.colorPickRect.y;
            this.activeColor = this.colorPickImg.getRGB(x, y);
            final GridSpec activeSpec = this.gridSpecs.getSpec(this.activeMapType);
            activeSpec.setColorCoords(x, y);
            this.updateGridSpecs();
        }
    }
    
    protected void func_146284_a(final GuiButton guibutton) {
        try {
            if (guibutton == this.buttonDay) {
                this.updatePreview(MapView.day(0));
            }
            else if (guibutton == this.buttonNight) {
                this.updatePreview(MapView.night(0));
            }
            else if (guibutton == this.buttonUnderground) {
                this.updatePreview(MapView.underground(0, 0));
            }
            this.updateGridSpecs();
            if (guibutton == this.buttonReset) {
                this.resetGridSpecs();
                return;
            }
            if (guibutton == this.buttonCancel) {
                this.resetGridSpecs();
                this.closeAndReturn();
                return;
            }
            if (guibutton == this.buttonClose) {
                this.saveAndClose();
            }
        }
        catch (Throwable t) {
            this.logger.error("Error in GridEditor.actionPerformed: " + LogFormatter.toString(t));
        }
    }
    
    protected void updatePreview(final MapView mapView) {
        this.activeMapType = mapView;
        final GridSpec activeSpec = this.gridSpecs.getSpec(this.activeMapType);
        this.activeColor = activeSpec.getColor();
        this.buttonOpacity.setValue((int)(activeSpec.alpha * 100.0f));
        this.buttonStyle.setValue(activeSpec.style);
        this.checkDay.setToggled(mapView.isDay());
        this.checkNight.setToggled(mapView.isNight());
        this.checkUnderground.setToggled(mapView.isUnderground());
        this.buttonDay.setToggled(mapView.isDay());
        this.buttonNight.setToggled(mapView.isNight());
        this.buttonUnderground.setToggled(mapView.isUnderground());
    }
    
    protected void updateGridSpecs() {
        final GridSpec activeSpec = this.gridSpecs.getSpec(this.activeMapType);
        final int colorX = activeSpec.getColorX();
        final int colorY = activeSpec.getColorY();
        final GridSpec newSpec = new GridSpec(this.buttonStyle.getField().get(), new Color(this.activeColor), this.buttonOpacity.getValue() / 100.0f).setColorCoords(colorX, colorY);
        if (this.checkDay.getToggled()) {
            this.gridSpecs.setSpec(MapView.day(0), newSpec);
        }
        if (this.checkNight.getToggled()) {
            this.gridSpecs.setSpec(MapView.night(0), newSpec);
        }
        if (this.checkUnderground.getToggled()) {
            this.gridSpecs.setSpec(MapView.underground(0, 0), newSpec);
        }
    }
    
    protected void saveAndClose() {
        this.updateGridSpecs();
        Journeymap.getClient().getCoreProperties().gridSpecs.updateFrom(this.gridSpecs);
        Journeymap.getClient().getCoreProperties().save();
        this.closeAndReturn();
    }
    
    protected void resetGridSpecs() {
        if (this.checkDay.getToggled()) {
            this.gridSpecs.setSpec(MapView.day(0), GridSpecs.DEFAULT_DAY.clone());
        }
        if (this.checkNight.getToggled()) {
            this.gridSpecs.setSpec(MapView.night(0), GridSpecs.DEFAULT_NIGHT.clone());
        }
        if (this.checkUnderground.getToggled()) {
            this.gridSpecs.setSpec(MapView.underground(0, 0), GridSpecs.DEFAULT_UNDERGROUND.clone());
        }
        this.field_146292_n.clear();
        this.func_73866_w_();
    }
    
    @Override
    protected void closeAndReturn() {
        if (this.returnDisplay == null) {
            UIManager.INSTANCE.closeAll();
        }
        else {
            UIManager.INSTANCE.open(this.returnDisplay);
        }
    }
    
    public TextureImpl getTileSample(final MapView mapView) {
        if (mapView.isNight()) {
            return TextureCache.getTexture(TextureCache.TileSampleNight);
        }
        if (mapView.isUnderground()) {
            return TextureCache.getTexture(TextureCache.TileSampleUnderground);
        }
        return TextureCache.getTexture(TextureCache.TileSampleDay);
    }
}
