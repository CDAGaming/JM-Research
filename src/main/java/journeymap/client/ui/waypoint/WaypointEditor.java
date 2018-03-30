package journeymap.client.ui.waypoint;

import journeymap.client.api.display.*;
import journeymap.client.ui.option.*;
import java.awt.geom.*;
import journeymap.common.*;
import journeymap.client.waypoint.*;
import journeymap.client.ui.*;
import net.minecraft.client.entity.*;
import journeymap.client.*;
import journeymap.client.render.texture.*;
import org.lwjgl.input.*;
import journeymap.common.log.*;
import journeymap.client.data.*;
import journeymap.client.log.*;
import journeymap.client.properties.*;
import net.minecraft.util.math.*;
import java.util.*;
import journeymap.client.api.model.*;
import journeymap.client.render.draw.*;
import net.minecraft.client.renderer.*;
import java.awt.image.*;
import java.awt.*;
import net.minecraft.client.gui.*;
import java.io.*;
import journeymap.client.cartography.color.*;
import com.google.common.base.*;
import journeymap.client.ui.fullscreen.*;
import journeymap.client.ui.component.*;

public class WaypointEditor extends JmUI
{
    private final TextureImpl wpTexture;
    private final TextureImpl colorPickTexture;
    private final Waypoint originalWaypoint;
    private final boolean isNew;
    String labelName;
    String locationTitle;
    String colorTitle;
    String dimensionsTitle;
    String labelX;
    String labelY;
    String labelZ;
    String labelR;
    String labelG;
    String labelB;
    String currentLocation;
    LocationFormat.LocationFormatKeys locationFormatKeys;
    private Button buttonRandomize;
    private Button buttonRemove;
    private Button buttonReset;
    private Button buttonSave;
    private Button buttonClose;
    private TextField fieldName;
    private TextField fieldR;
    private TextField fieldG;
    private TextField fieldB;
    private TextField fieldX;
    private TextField fieldY;
    private TextField fieldZ;
    private ArrayList<TextField> fieldList;
    private ArrayList<DimensionButton> dimButtonList;
    private ScrollPane dimScrollPane;
    private Integer currentColor;
    private Rectangle2D.Double colorPickRect;
    private BufferedImage colorPickImg;
    private List<String> colorPickTooltip;
    private Waypoint editedWaypoint;
    private ButtonList bottomButtons;
    
    public static void openPlayerWaypoint() {
        openPlayerWaypoint(null);
    }
    
    public static void openPlayerWaypoint(final JmUI returnDisplay) {
        final EntityPlayerSP player = Journeymap.clientPlayer();
        if (player != null) {
            final Waypoint waypoint = WaypointStore.create(player.field_71093_bK, player.func_180425_c());
            UIManager.INSTANCE.openWaypointEditor(waypoint, true, returnDisplay);
        }
    }
    
    public WaypointEditor(final Waypoint waypoint, final boolean isNew, final JmUI returnDisplay) {
        super(Constants.getString(isNew ? "jm.waypoint.new_title" : "jm.waypoint.edit_title"), returnDisplay);
        this.labelName = Constants.getString("jm.waypoint.name");
        this.locationTitle = Constants.getString("jm.waypoint.location");
        this.colorTitle = Constants.getString("jm.waypoint.color");
        this.dimensionsTitle = Constants.getString("jm.waypoint.dimensions");
        this.labelX = Constants.getString("jm.waypoint.x");
        this.labelY = Constants.getString("jm.waypoint.y");
        this.labelZ = Constants.getString("jm.waypoint.z");
        this.labelR = Constants.getString("jm.waypoint.red_abbreviated");
        this.labelG = Constants.getString("jm.waypoint.green_abbreviated");
        this.labelB = Constants.getString("jm.waypoint.blue_abbreviated");
        this.currentLocation = "";
        this.fieldList = new ArrayList<TextField>();
        this.dimButtonList = new ArrayList<DimensionButton>();
        this.originalWaypoint = waypoint;
        this.editedWaypoint = new Waypoint(waypoint);
        this.isNew = isNew;
        this.wpTexture = TextureCache.getTexture(WaypointStore.getWaypointIcon(this.editedWaypoint).getImageLocation());
        final String tooltip = Constants.birthdayMessage();
        this.colorPickTooltip = ((tooltip == null) ? null : Collections.singletonList(tooltip));
        this.colorPickTexture = ((tooltip == null) ? TextureCache.getTexture(TextureCache.ColorPicker) : TextureCache.getTexture(TextureCache.ColorPicker2));
        try {
            this.colorPickRect = new Rectangle2D.Double(0.0, 0.0, this.colorPickTexture.getWidth(), this.colorPickTexture.getHeight());
            this.colorPickImg = this.colorPickTexture.getImage();
            Keyboard.enableRepeatEvents(true);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error during WaypointEditor ctor: " + LogFormatter.toPartialString(t));
            UIManager.INSTANCE.closeAll();
        }
    }
    
    @Override
    public void func_73866_w_() {
        try {
            final FullMapProperties fullMapProperties = Journeymap.getClient().getFullMapProperties();
            final LocationFormat locationFormat = new LocationFormat();
            this.locationFormatKeys = locationFormat.getFormatKeys(fullMapProperties.locationFormat.get());
            final EntityPlayerSP player = Journeymap.clientPlayer();
            final String posLabel = this.locationFormatKeys.format(fullMapProperties.locationFormatVerbose.get(), MathHelper.func_76128_c(player.field_70165_t), MathHelper.func_76128_c(player.field_70161_v), MathHelper.func_76128_c(player.func_174813_aQ().field_72338_b), MathHelper.func_76141_d((float)player.field_70162_ai));
            this.currentLocation = Constants.getString("jm.waypoint.current_location", posLabel);
            if (this.fieldList.isEmpty()) {
                final FontRenderer fr = this.getFontRenderer();
                (this.fieldName = new TextField(this.editedWaypoint.getName(), fr, 160, 20)).func_146195_b(true);
                if (this.isNew) {
                    this.fieldName.func_146202_e();
                    this.fieldName.func_146199_i(0);
                }
                this.fieldList.add(this.fieldName);
                final int width9chars = this.getFontRenderer().func_78256_a("-30000000") + 10;
                final int width3chars = this.getFontRenderer().func_78256_a("255") + 10;
                final int h = 20;
                final BlockPos pos = this.editedWaypoint.getPosition();
                (this.fieldX = new TextField(pos.func_177958_n(), fr, width9chars, h, true, true)).setClamp(-30000000, 30000000);
                this.fieldList.add(this.fieldX);
                (this.fieldZ = new TextField(pos.func_177952_p(), fr, width9chars, h, true, true)).setClamp(-30000000, 30000000);
                this.fieldList.add(this.fieldZ);
                final int y = pos.func_177956_o();
                (this.fieldY = new TextField((y < 0) ? "" : y, fr, width3chars, h, true, true)).setClamp(0, Journeymap.clientWorld().func_72800_K() - 1);
                this.fieldY.setMinLength(1);
                this.fieldList.add(this.fieldY);
                (this.fieldR = new TextField("", fr, width3chars, h, true, false)).setClamp(0, 255);
                this.fieldR.func_146203_f(3);
                this.fieldList.add(this.fieldR);
                (this.fieldG = new TextField("", fr, width3chars, h, true, false)).setClamp(0, 255);
                this.fieldG.func_146203_f(3);
                this.fieldList.add(this.fieldG);
                (this.fieldB = new TextField("", fr, width3chars, h, true, false)).setClamp(0, 255);
                this.fieldB.func_146203_f(3);
                this.fieldList.add(this.fieldB);
                for (final WorldData.DimensionProvider provider : WorldData.getDimensionProviders(WaypointStore.INSTANCE.getLoadedDimensions())) {
                    final int dim = provider.getDimension();
                    String dimName = Integer.toString(dim);
                    try {
                        dimName = WorldData.getSafeDimensionName(provider);
                    }
                    catch (Exception e) {
                        JMLogger.logOnce("Can't get dimension name from provider: ", e);
                    }
                    this.dimButtonList.add(new DimensionButton(0, dim, dimName, this.editedWaypoint.isDisplayed(dim)));
                }
                (this.dimScrollPane = new ScrollPane(this.field_146297_k, 0, 0, this.dimButtonList, this.dimButtonList.get(0).getHeight(), 4)).func_193651_b(false);
            }
            if (this.field_146292_n.isEmpty()) {
                final String on = Constants.getString("jm.common.on");
                final String off = Constants.getString("jm.common.off");
                this.buttonRandomize = new Button(Constants.getString("jm.waypoint.randomize"));
                (this.buttonRemove = new Button(Constants.getString("jm.waypoint.remove"))).setEnabled(!this.isNew);
                this.buttonReset = new Button(Constants.getString("jm.waypoint.reset"));
                this.buttonSave = new Button(Constants.getString("jm.waypoint.save"));
                final String closeLabel = this.isNew ? "jm.waypoint.cancel" : "jm.common.close";
                this.buttonClose = new Button(Constants.getString(closeLabel));
                this.field_146292_n.add(this.buttonRandomize);
                this.field_146292_n.add(this.buttonRemove);
                this.field_146292_n.add(this.buttonReset);
                this.field_146292_n.add(this.buttonSave);
                this.field_146292_n.add(this.buttonClose);
                (this.bottomButtons = new ButtonList(new Button[] { this.buttonRemove, this.buttonReset, this.buttonSave, this.buttonClose })).equalizeWidths(this.getFontRenderer());
                final MapImage icon = WaypointStore.getWaypointIcon(this.originalWaypoint);
                this.setFormColor(icon.getColor());
                this.validate();
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
            final FontRenderer fr = this.getFontRenderer();
            final int vpad = 5;
            final int hgap = fr.func_78256_a("X") * 3;
            final int vgap = this.fieldX.getHeight() + 5;
            final int startY = Math.max(40, (this.field_146295_m - 150) / 2);
            int dcw = fr.func_78256_a(this.dimensionsTitle);
            dcw = 8 + Math.max(dcw, this.dimScrollPane.getFitWidth(fr));
            final int leftWidth = hgap * 2 + this.fieldX.func_146200_o() + this.fieldY.func_146200_o() + this.fieldZ.func_146200_o();
            final int rightWidth = dcw;
            final int totalWidth = leftWidth + 10 + rightWidth;
            final int leftX = (this.field_146294_l - totalWidth) / 2;
            final int leftXEnd = leftX + leftWidth;
            final int rightX = leftXEnd + 10;
            final int rightXEnd = rightX + rightWidth;
            int leftRowY = startY;
            this.drawLabel(this.labelName, leftX, leftRowY);
            leftRowY += 12;
            this.fieldName.setWidth(leftWidth);
            this.fieldName.setX(leftX);
            this.fieldName.setY(leftRowY);
            if (!this.fieldName.func_146206_l()) {
                this.fieldName.func_146199_i(this.fieldName.func_146179_b().length());
            }
            this.fieldName.func_146194_f();
            leftRowY += vgap + 5;
            this.drawLabel(this.locationTitle, leftX, leftRowY);
            leftRowY += 12;
            this.drawLabelAndField(this.labelX, this.fieldX, leftX, leftRowY);
            this.drawLabelAndField(this.labelZ, this.fieldZ, this.fieldX.getX() + this.fieldX.func_146200_o() + hgap, leftRowY);
            this.drawLabelAndField(this.labelY, this.fieldY, this.fieldZ.getX() + this.fieldZ.func_146200_o() + hgap, leftRowY);
            leftRowY += vgap + 5;
            this.drawLabel(this.colorTitle, leftX, leftRowY);
            leftRowY += 12;
            this.drawLabelAndField(this.labelR, this.fieldR, leftX, leftRowY);
            this.drawLabelAndField(this.labelG, this.fieldG, this.fieldR.getX() + this.fieldR.func_146200_o() + hgap, leftRowY);
            this.drawLabelAndField(this.labelB, this.fieldB, this.fieldG.getX() + this.fieldG.func_146200_o() + hgap, leftRowY);
            this.buttonRandomize.func_175211_a(4 + Math.max(this.fieldB.getX() + this.fieldB.func_146200_o() - this.fieldR.getX(), 10 + fr.func_78256_a(this.buttonRandomize.field_146126_j)));
            this.buttonRandomize.setPosition(this.fieldR.getX() - 2, leftRowY += vgap);
            final int cpY = this.fieldB.getY();
            final int cpSize = this.buttonRandomize.getY() + this.buttonRandomize.getHeight() - cpY - 2;
            final int cpHAreaX = this.fieldB.getX() + this.fieldB.func_146200_o();
            final int cpHArea = this.fieldName.getX() + this.fieldName.func_146200_o() - cpHAreaX;
            final int cpX = cpHAreaX + (cpHArea - cpSize);
            this.drawColorPicker(cpX, cpY, cpSize);
            final int iconX = cpHAreaX + (cpX - cpHAreaX) / 2 - this.wpTexture.getWidth() / 2 + 1;
            final int iconY = this.buttonRandomize.getY() - 2;
            this.drawWaypoint(iconX, iconY);
            leftRowY += vgap;
            int rightRow = startY;
            this.drawLabel(this.dimensionsTitle, rightX, rightRow);
            rightRow += 12;
            final int scrollHeight = this.buttonRandomize.getY() + this.buttonRandomize.getHeight() - 2 - rightRow;
            this.dimScrollPane.setDimensions(dcw, scrollHeight, 0, 0, rightX, rightRow);
            final int totalRow = Math.max(leftRowY + vgap, rightRow + vgap);
            this.bottomButtons.layoutFilledHorizontal(fr, leftX - 2, totalRow, rightXEnd + 2, 4, true);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error during WaypointEditor layout: " + LogFormatter.toPartialString(t));
            UIManager.INSTANCE.closeAll();
        }
    }
    
    @Override
    public void func_73863_a(final int mouseX, final int mouseY, final float partialTicks) {
        try {
            this.func_146278_c(0);
            this.validate();
            this.layoutButtons();
            this.dimScrollPane.func_148128_a(mouseX, mouseY, partialTicks);
            DrawUtil.drawLabel(this.currentLocation, this.field_146294_l / 2, this.buttonClose.getY() - 5, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, 0, 1.0f, 12632256, 1.0f, 1.0, true);
            for (int k = 0; k < this.field_146292_n.size(); ++k) {
                final GuiButton guibutton = this.field_146292_n.get(k);
                guibutton.func_191745_a(this.field_146297_k, mouseX, mouseY, partialTicks);
            }
            if (this.colorPickTooltip != null && this.colorPickRect.contains(mouseX, mouseY)) {
                this.drawHoveringText(this.colorPickTooltip, mouseX, mouseY, this.getFontRenderer());
                RenderHelper.func_74518_a();
            }
            this.drawTitle();
            this.drawLogo();
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error during WaypointEditor layout: " + LogFormatter.toPartialString(t));
            UIManager.INSTANCE.closeAll();
        }
    }
    
    protected void drawWaypoint(final int x, final int y) {
        DrawUtil.drawColoredImage(this.wpTexture, this.currentColor, 1.0f, x, y - this.wpTexture.getHeight() / 2, 0.0);
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
    }
    
    protected void drawLabelAndField(final String label, final TextField field, final int x, final int y) {
        field.setX(x);
        field.setY(y);
        final FontRenderer fr = this.getFontRenderer();
        final int width = fr.func_78256_a(label) + 4;
        this.func_73731_b(this.getFontRenderer(), label, x - width, y + (field.getHeight() - 8) / 2, Color.cyan.getRGB());
        field.func_146194_f();
    }
    
    protected void drawLabel(final String label, final int x, final int y) {
        this.func_73731_b(this.getFontRenderer(), label, x, y, Color.cyan.getRGB());
    }
    
    @Override
    protected void func_73869_a(final char par1, final int par2) {
        switch (par2) {
            case 1: {
                this.closeAndReturn();
            }
            case 28: {
                this.save();
            }
            case 15: {
                this.validate();
                this.onTab();
            }
            default: {
                for (final GuiTextField field : this.fieldList) {
                    final boolean done = field.func_146201_a(par1, par2);
                    if (done) {
                        break;
                    }
                }
                this.updateWaypointFromForm();
                this.validate();
            }
        }
    }
    
    public void func_146274_d() throws IOException {
        super.func_146274_d();
        this.dimScrollPane.func_178039_p();
    }
    
    protected void func_146273_a(final int par1, final int par2, final int par3, final long par4) {
        this.checkColorPicker(par1, par2);
    }
    
    protected void func_73864_a(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        super.func_73864_a(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            for (final GuiTextField field : this.fieldList) {
                field.func_146192_a(mouseX, mouseY, mouseButton);
            }
            this.checkColorPicker(mouseX, mouseY);
            final Button button = this.dimScrollPane.mouseClicked(mouseX, mouseY, mouseButton);
            if (button != null) {
                this.func_146284_a(button);
            }
        }
    }
    
    protected void checkColorPicker(final int mouseX, final int mouseY) {
        if (this.colorPickRect.contains(mouseX, mouseY)) {
            final int x = mouseX - (int)this.colorPickRect.x;
            final int y = mouseY - (int)this.colorPickRect.y;
            this.setFormColor(this.colorPickImg.getRGB(x, y));
        }
    }
    
    protected void setFormColor(final Integer color) {
        this.currentColor = color;
        final int[] c = RGB.ints(color);
        this.fieldR.func_146180_a(Integer.toString(c[0]));
        this.fieldG.func_146180_a(Integer.toString(c[1]));
        this.fieldB.func_146180_a(Integer.toString(c[2]));
        this.updateWaypointFromForm();
    }
    
    protected void func_146284_a(final GuiButton guibutton) {
        if (this.dimButtonList.contains(guibutton)) {
            final DimensionButton dimButton = (DimensionButton)guibutton;
            dimButton.toggle();
            this.updateWaypointFromForm();
        }
        else {
            if (guibutton == this.buttonRandomize) {
                this.setRandomColor();
                return;
            }
            if (guibutton == this.buttonRemove) {
                this.remove();
                return;
            }
            if (guibutton == this.buttonReset) {
                this.resetForm();
                return;
            }
            if (guibutton == this.buttonSave) {
                this.save();
                return;
            }
            if (guibutton == this.buttonClose) {
                this.refreshAndClose(this.originalWaypoint);
            }
        }
    }
    
    protected void setRandomColor() {
        final int color = RGB.randomColor();
        this.editedWaypoint.setIconColor(color).setLabelColor(RGB.labelSafe(color));
        this.setFormColor(color);
    }
    
    protected void onTab() {
        boolean focusNext = false;
        boolean foundFocus = false;
        for (final TextField field : this.fieldList) {
            if (focusNext) {
                field.func_146195_b(true);
                foundFocus = true;
                break;
            }
            if (!field.func_146206_l()) {
                continue;
            }
            field.func_146195_b(false);
            field.clamp();
            focusNext = true;
        }
        if (!foundFocus) {
            this.fieldList.get(0).func_146195_b(true);
        }
    }
    
    protected boolean validate() {
        boolean valid = true;
        if (this.fieldName != null) {
            valid = this.fieldName.hasMinLength();
        }
        if (valid && this.fieldY != null) {
            valid = this.fieldY.hasMinLength();
        }
        if (this.buttonSave != null) {
            this.buttonSave.setEnabled(valid && (this.isNew || !this.originalWaypoint.equals(this.editedWaypoint)));
        }
        return valid;
    }
    
    protected void remove() {
        WaypointStore.INSTANCE.remove(this.originalWaypoint);
        this.refreshAndClose(null);
    }
    
    protected void save() {
        if (!this.validate()) {
            return;
        }
        this.updateWaypointFromForm();
        this.originalWaypoint.updateFrom(this.editedWaypoint);
        WaypointStore.INSTANCE.save(this.originalWaypoint);
        this.refreshAndClose(this.originalWaypoint);
    }
    
    protected void resetForm() {
        this.editedWaypoint = new Waypoint(this.originalWaypoint);
        this.dimButtonList.clear();
        this.fieldList.clear();
        this.field_146292_n.clear();
        this.func_73866_w_();
        this.validate();
    }
    
    protected void updateWaypointFromForm() {
        try {
            final int r = this.getSafeColorInt(this.fieldR);
            final int g = this.getSafeColorInt(this.fieldG);
            final int b = this.getSafeColorInt(this.fieldB);
            this.currentColor = RGB.toInteger(r, g, b);
            this.editedWaypoint.setIconColor(this.currentColor);
            final int labelColor = RGB.labelSafe(this.currentColor);
            this.editedWaypoint.setLabelColor(labelColor);
            this.fieldName.func_146193_g(labelColor);
            for (final DimensionButton db : this.dimButtonList) {
                this.editedWaypoint.setDisplayed(db.dimension, db.getToggled());
            }
            String name = this.fieldName.func_146179_b();
            if (Strings.isNullOrEmpty(name)) {
                name = Constants.getString("jm.waypoint.new_title");
            }
            this.editedWaypoint.setName(name);
            this.editedWaypoint.setPosition(this.editedWaypoint.getDimension(), new BlockPos(this.getSafeCoordInt(this.fieldX), this.getSafeCoordInt(this.fieldY), this.getSafeCoordInt(this.fieldZ)));
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error updating waypoint from form: " + LogFormatter.toPartialString(e));
        }
    }
    
    protected int getSafeColorInt(final TextField field) {
        field.clamp();
        final String text = field.func_146179_b();
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int val = 0;
        try {
            val = Integer.parseInt(text);
        }
        catch (NumberFormatException ex) {}
        return Math.max(0, Math.min(255, val));
    }
    
    protected int getSafeCoordInt(final TextField field) {
        final String text = field.func_146179_b();
        if (text == null || text.isEmpty() || text.equals("-")) {
            return 0;
        }
        int val = 0;
        try {
            val = Integer.parseInt(text);
        }
        catch (NumberFormatException ex) {}
        return val;
    }
    
    protected void refreshAndClose(final Waypoint focusWaypoint) {
        if (this.returnDisplay != null && this.returnDisplay instanceof WaypointManager) {
            UIManager.INSTANCE.openWaypointManager(focusWaypoint, new Fullscreen());
            return;
        }
        Fullscreen.state().requireRefresh();
        this.closeAndReturn();
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
    
    class DimensionButton extends OnOffButton
    {
        public final int dimension;
        
        DimensionButton(final int id, final int dimension, final String dimensionName, final boolean toggled) {
            super(id, String.format("%s: %s", dimensionName, Constants.getString("jm.common.on")), String.format("%s: %s", dimensionName, Constants.getString("jm.common.off")), toggled);
            this.dimension = dimension;
            this.setToggled(toggled);
        }
    }
}
