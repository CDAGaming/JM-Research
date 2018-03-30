package journeymap.client.ui.component;

import org.apache.logging.log4j.*;
import journeymap.common.*;
import journeymap.client.render.texture.*;
import net.minecraft.client.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.render.draw.*;
import net.minecraft.client.gui.*;
import java.awt.*;
import net.minecraft.client.renderer.*;
import journeymap.common.log.*;
import journeymap.client.ui.*;
import java.io.*;
import java.util.*;
import journeymap.common.api.feature.*;
import journeymap.client.feature.*;
import net.minecraft.util.text.*;
import journeymap.client.*;
import org.lwjgl.input.*;
import journeymap.client.ui.dialog.*;

public abstract class JmUI extends GuiScreen
{
    protected final String title;
    protected final int headerHeight = 35;
    protected final Logger logger;
    protected GuiScreen returnDisplay;
    protected int scaleFactor;
    protected TextureImpl logo;
    
    public JmUI(final String title) {
        this(title, null);
    }
    
    public JmUI(final String title, final GuiScreen returnDisplay) {
        this.logger = Journeymap.getLogger();
        this.scaleFactor = 1;
        this.logo = TextureCache.getTexture(TextureCache.Logo);
        this.title = title;
        this.returnDisplay = returnDisplay;
        if (this.returnDisplay != null && this.returnDisplay instanceof JmUI) {
            final JmUI jmReturnDisplay = (JmUI)this.returnDisplay;
            if (jmReturnDisplay.returnDisplay instanceof JmUI) {
                jmReturnDisplay.returnDisplay = null;
            }
        }
    }
    
    public Minecraft getMinecraft() {
        return this.field_146297_k;
    }
    
    public void func_146280_a(final Minecraft minecraft, final int width, final int height) {
        super.func_146280_a(minecraft, width, height);
        this.scaleFactor = new ScaledResolution(minecraft).func_78325_e();
    }
    
    public boolean func_73868_f() {
        return true;
    }
    
    public FontRenderer getFontRenderer() {
        return this.field_146289_q;
    }
    
    public static FontRenderer fontRenderer() {
        return FMLClientHandler.instance().getClient().field_71466_p;
    }
    
    public void sizeDisplay(final boolean scaled) {
        final int glwidth = scaled ? this.field_146294_l : this.field_146297_k.field_71443_c;
        final int glheight = scaled ? this.field_146295_m : this.field_146297_k.field_71440_d;
        DrawUtil.sizeDisplay(glwidth, glheight);
    }
    
    protected boolean isMouseOverButton(final int mouseX, final int mouseY) {
        for (int k = 0; k < this.field_146292_n.size(); ++k) {
            final GuiButton guibutton = this.field_146292_n.get(k);
            if (guibutton instanceof Button) {
                final Button button = (Button)guibutton;
                if (button.mouseOver(mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected void func_146286_b(final int mouseX, final int mouseY, final int mouseEvent) {
        super.func_146286_b(mouseX, mouseY, mouseEvent);
    }
    
    protected void drawLogo() {
        if (this.logo.isDefunct()) {
            this.logo = TextureCache.getTexture(TextureCache.Logo);
        }
        DrawUtil.sizeDisplay(this.field_146297_k.field_71443_c, this.field_146297_k.field_71440_d);
        DrawUtil.drawImage(this.logo, 8.0, 8.0, false, 0.5f, 0.0);
        DrawUtil.sizeDisplay(this.field_146294_l, this.field_146295_m);
    }
    
    protected void drawTitle() {
        DrawUtil.drawRectangle(0.0, 0.0, this.field_146294_l, 35.0, 0, 0.4f);
        DrawUtil.drawLabel(this.title, this.field_146294_l / 2, 17.0, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, 0, 0.0f, Color.CYAN.getRGB(), 1.0f, 1.0, true, 0.0);
        final String apiVersion = "API v2.0-SNAPSHOT";
        DrawUtil.drawLabel(apiVersion, this.field_146294_l - 10, 17.0, DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, 0, 0.0f, 13421772, 1.0f, 0.5, true, 0.0);
    }
    
    public void func_73866_w_() {
        this.field_146292_n.clear();
    }
    
    public void func_146278_c(final int tint) {
        if (Journeymap.clientWorld() == null) {
            this.func_73733_a(0, 0, this.field_146294_l, this.field_146295_m, -1072689136, -804253680);
        }
        else {
            this.func_146276_q_();
        }
    }
    
    protected abstract void layoutButtons();
    
    public List getButtonList() {
        return this.field_146292_n;
    }
    
    public void func_73863_a(final int mouseX, final int mouseY, final float partialTicks) {
        try {
            this.func_146278_c(0);
            this.layoutButtons();
            this.drawTitle();
            this.drawLogo();
            List<String> tooltip = null;
            for (int k = 0; k < this.field_146292_n.size(); ++k) {
                final GuiButton guibutton = this.field_146292_n.get(k);
                guibutton.func_191745_a(this.field_146297_k, mouseX, mouseY, partialTicks);
                if (tooltip == null && guibutton instanceof Button) {
                    final Button button = (Button)guibutton;
                    if (button.mouseOver(mouseX, mouseY)) {
                        tooltip = button.getTooltip();
                    }
                }
            }
            if (tooltip != null && !tooltip.isEmpty()) {
                this.drawHoveringText(tooltip, mouseX, mouseY, this.getFontRenderer());
                RenderHelper.func_74518_a();
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Error in UI %s: %s", this.getClass().getSimpleName(), LogFormatter.toString(t)));
            this.closeAndReturn();
        }
    }
    
    protected void func_73733_a(final int left, final int top, final int right, final int bottom, final int startColor, final int endColor) {
        super.func_73733_a(left, top, right, bottom, startColor, endColor);
    }
    
    public void close() {
    }
    
    protected void closeAndReturn() {
        if (this.returnDisplay == null) {
            if (Journeymap.clientWorld() != null) {
                UIManager.INSTANCE.openFullscreenMap();
            }
            else {
                UIManager.INSTANCE.closeAll();
            }
        }
        else {
            if (this.returnDisplay instanceof JmUI) {
                ((JmUI)this.returnDisplay).returnDisplay = null;
            }
            UIManager.INSTANCE.open(this.returnDisplay);
        }
    }
    
    protected void func_73869_a(final char c, final int i) throws IOException {
        switch (i) {
            case 1: {
                this.closeAndReturn();
                break;
            }
        }
    }
    
    public void drawHoveringText(final String[] tooltip, final int mouseX, final int mouseY) {
        this.drawHoveringText(Arrays.asList(tooltip), mouseX, mouseY, this.getFontRenderer());
    }
    
    public GuiScreen getReturnDisplay() {
        return this.returnDisplay;
    }
    
    public void drawHoveringText(final List tooltip, final int mouseX, final int mouseY, final FontRenderer fontRenderer) {
        super.drawHoveringText(tooltip, mouseX, mouseY, fontRenderer);
    }
    
    protected boolean showDisabled(final Feature feature, final int dimension, final int mouseX, final int mouseY) {
        final boolean disabled = !ClientFeatures.instance().isAllowed(feature, dimension);
        if (disabled) {
            final FontRenderer fr = this.getFontRenderer();
            final int y = this.field_146295_m / 2 - fr.field_78288_b;
            String title = ClientFeatures.getFeatureName(feature);
            final String subtitle = TextFormatting.RED + Constants.getString("jm.common.disabled_feature");
            final int boxWidth = Math.max(fr.func_78256_a(title), fr.func_78256_a(subtitle));
            final int x = (this.field_146294_l - boxWidth) / 2 - 12;
            final boolean mouseIsOver = mouseX >= x + 6 && mouseX <= x + boxWidth + 16 && mouseY >= y - fr.field_78288_b * 2 + 2 && mouseY <= y + fr.field_78288_b + 2;
            if (mouseIsOver) {
                title = TextFormatting.UNDERLINE + title;
            }
            this.func_146283_a((List)Arrays.asList(title, subtitle), x, y);
            RenderHelper.func_74518_a();
            if (mouseIsOver && Mouse.isButtonDown(0)) {
                UIManager.INSTANCE.open(FeatureDialog.class, this);
                this.close();
            }
        }
        return disabled;
    }
}
