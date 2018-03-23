package journeymap.client.ui.component;

import org.apache.logging.log4j.*;
import journeymap.common.*;
import journeymap.client.render.texture.*;
import net.minecraft.client.*;
import journeymap.client.render.draw.*;
import net.minecraft.client.gui.*;
import java.awt.*;
import journeymap.common.log.*;
import journeymap.client.ui.*;
import java.io.*;
import org.lwjgl.opengl.*;
import net.minecraft.client.renderer.*;
import java.util.*;

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
        final String apiVersion = "API v1.4";
        DrawUtil.drawLabel(apiVersion, this.field_146294_l - 10, 17.0, DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, 0, 0.0f, 13421772, 1.0f, 0.5, true, 0.0);
    }
    
    public void func_73866_w_() {
        this.field_146292_n.clear();
    }
    
    public void func_146278_c(final int tint) {
        if (this.field_146297_k.field_71441_e == null) {
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
    
    public void func_73863_a(final int x, final int y, final float par3) {
        try {
            this.func_146278_c(0);
            this.layoutButtons();
            this.drawTitle();
            this.drawLogo();
            List<String> tooltip = null;
            for (int k = 0; k < this.field_146292_n.size(); ++k) {
                final GuiButton guibutton = this.field_146292_n.get(k);
                guibutton.func_191745_a(this.field_146297_k, x, y, 0.0f);
                if (tooltip == null && guibutton instanceof Button) {
                    final Button button = (Button)guibutton;
                    if (button.mouseOver(x, y)) {
                        tooltip = button.getTooltip();
                    }
                }
            }
            if (tooltip != null && !tooltip.isEmpty()) {
                this.drawHoveringText(tooltip, x, y, this.getFontRenderer());
                RenderHelper.func_74518_a();
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error in UI: " + LogFormatter.toString(t));
            this.closeAndReturn();
        }
    }
    
    public void func_73733_a(final int p_73733_1_, final int p_73733_2_, final int p_73733_3_, final int p_73733_4_, final int p_73733_5_, final int p_73733_6_) {
        super.func_73733_a(p_73733_1_, p_73733_2_, p_73733_3_, p_73733_4_, p_73733_5_, p_73733_6_);
    }
    
    public void close() {
    }
    
    protected void closeAndReturn() {
        if (this.returnDisplay == null) {
            if (this.field_146297_k.field_71441_e != null) {
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
        if (!tooltip.isEmpty()) {
            GL11.glDisable(32826);
            RenderHelper.func_74518_a();
            GlStateManager.func_179140_f();
            GlStateManager.func_179097_i();
            int maxLineWidth = 0;
            for (final String line : tooltip) {
                int lineWidth = fontRenderer.func_78256_a(line);
                if (fontRenderer.func_78260_a()) {
                    lineWidth = (int)Math.ceil(lineWidth * 1.25);
                }
                if (lineWidth > maxLineWidth) {
                    maxLineWidth = lineWidth;
                }
            }
            int drawX = mouseX + 12;
            int drawY = mouseY - 12;
            int boxHeight = 8;
            if (tooltip.size() > 1) {
                boxHeight += 2 + (tooltip.size() - 1) * 10;
            }
            if (drawX + maxLineWidth > this.field_146294_l) {
                drawX -= 28 + maxLineWidth;
            }
            if (drawY + boxHeight + 6 > this.field_146295_m) {
                drawY = this.field_146295_m - boxHeight - 6;
            }
            this.field_73735_i = 300.0f;
            this.field_146296_j.field_77023_b = 300.0f;
            final int j1 = -267386864;
            this.func_73733_a(drawX - 3, drawY - 4, drawX + maxLineWidth + 3, drawY - 3, j1, j1);
            this.func_73733_a(drawX - 3, drawY + boxHeight + 3, drawX + maxLineWidth + 3, drawY + boxHeight + 4, j1, j1);
            this.func_73733_a(drawX - 3, drawY - 3, drawX + maxLineWidth + 3, drawY + boxHeight + 3, j1, j1);
            this.func_73733_a(drawX - 4, drawY - 3, drawX - 3, drawY + boxHeight + 3, j1, j1);
            this.func_73733_a(drawX + maxLineWidth + 3, drawY - 3, drawX + maxLineWidth + 4, drawY + boxHeight + 3, j1, j1);
            final int k1 = 1347420415;
            final int l1 = (k1 & 0xFEFEFE) >> 1 | (k1 & 0xFF000000);
            this.func_73733_a(drawX - 3, drawY - 3 + 1, drawX - 3 + 1, drawY + boxHeight + 3 - 1, k1, l1);
            this.func_73733_a(drawX + maxLineWidth + 2, drawY - 3 + 1, drawX + maxLineWidth + 3, drawY + boxHeight + 3 - 1, k1, l1);
            this.func_73733_a(drawX - 3, drawY - 3, drawX + maxLineWidth + 3, drawY - 3 + 1, k1, k1);
            this.func_73733_a(drawX - 3, drawY + boxHeight + 2, drawX + maxLineWidth + 3, drawY + boxHeight + 3, l1, l1);
            for (int i2 = 0; i2 < tooltip.size(); ++i2) {
                final String line2 = tooltip.get(i2);
                if (fontRenderer.func_78260_a()) {
                    final int lineWidth2 = (int)Math.ceil(fontRenderer.func_78256_a(line2) * 1.1);
                    fontRenderer.func_175063_a(line2, (float)(drawX + maxLineWidth - lineWidth2), (float)drawY, -1);
                }
                else {
                    fontRenderer.func_175063_a(line2, (float)drawX, (float)drawY, -1);
                }
                if (i2 == 0) {
                    drawY += 2;
                }
                drawY += 10;
            }
            this.field_73735_i = 0.0f;
            this.field_146296_j.field_77023_b = 0.0f;
            GlStateManager.func_179145_e();
            GlStateManager.func_179126_j();
            RenderHelper.func_74519_b();
            GL11.glEnable(32826);
        }
    }
}
