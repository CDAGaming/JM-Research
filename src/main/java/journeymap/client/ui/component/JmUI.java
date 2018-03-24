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
import java.util.List;

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
        return this.mc;
    }
    
    public void setWorldAndResolution(final Minecraft minecraft, final int width, final int height) {
        super.setWorldAndResolution(minecraft, width, height);
        this.scaleFactor = new ScaledResolution(minecraft).getScaleFactor();
    }
    
    public boolean doesGuiPauseGame() {
        return true;
    }
    
    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }
    
    public void sizeDisplay(final boolean scaled) {
        final int glwidth = scaled ? this.width : this.mc.displayWidth;
        final int glheight = scaled ? this.height : this.mc.displayHeight;
        DrawUtil.sizeDisplay(glwidth, glheight);
    }
    
    protected boolean isMouseOverButton(final int mouseX, final int mouseY) {
        for (int k = 0; k < this.buttonList.size(); ++k) {
            final GuiButton guibutton = this.buttonList.get(k);
            if (guibutton instanceof Button) {
                final Button button = (Button)guibutton;
                if (button.mouseOver(mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected void mouseReleased(final int mouseX, final int mouseY, final int mouseEvent) {
        super.mouseReleased(mouseX, mouseY, mouseEvent);
    }
    
    protected void drawLogo() {
        if (this.logo.isDefunct()) {
            this.logo = TextureCache.getTexture(TextureCache.Logo);
        }
        DrawUtil.sizeDisplay(this.mc.displayWidth, this.mc.displayHeight);
        DrawUtil.drawImage(this.logo, 8.0, 8.0, false, 0.5f, 0.0);
        DrawUtil.sizeDisplay(this.width, this.height);
    }
    
    protected void drawTitle() {
        DrawUtil.drawRectangle(0.0, 0.0, this.width, 35.0, 0, 0.4f);
        DrawUtil.drawLabel(this.title, this.width / 2, 17.0, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, 0, 0.0f, Color.CYAN.getRGB(), 1.0f, 1.0, true, 0.0);
        final String apiVersion = "API v1.4";
        DrawUtil.drawLabel(apiVersion, this.width - 10, 17.0, DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, 0, 0.0f, 13421772, 1.0f, 0.5, true, 0.0);
    }
    
    public void initGui() {
        this.buttonList.clear();
    }
    
    public void drawBackground(final int tint) {
        if (this.mc.world == null) {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        }
        else {
            this.drawDefaultBackground();
        }
    }
    
    protected abstract void layoutButtons();
    
    public List getButtonList() {
        return this.buttonList;
    }
    
    public void drawScreen(final int x, final int y, final float par3) {
        try {
            this.drawBackground(0);
            this.layoutButtons();
            this.drawTitle();
            this.drawLogo();
            List<String> tooltip = null;
            for (int k = 0; k < this.buttonList.size(); ++k) {
                final GuiButton guibutton = this.buttonList.get(k);
                guibutton.drawButton(this.mc, x, y, 0.0f);
                if (tooltip == null && guibutton instanceof Button) {
                    final Button button = (Button)guibutton;
                    if (button.mouseOver(x, y)) {
                        tooltip = button.getTooltip();
                    }
                }
            }
            if (tooltip != null && !tooltip.isEmpty()) {
                this.drawHoveringText(tooltip, x, y, this.getFontRenderer());
                RenderHelper.disableStandardItemLighting();
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error in UI: " + LogFormatter.toString(t));
            this.closeAndReturn();
        }
    }
    
    public void drawGradientRect(final int left, final int top, final int right, final int bottom, final int startColor, final int endColor) {
        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }
    
    public void close() {
    }
    
    protected void closeAndReturn() {
        if (this.returnDisplay == null) {
            if (this.mc.world != null) {
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
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int maxLineWidth = 0;
            for (final String line : tooltip) {
                int lineWidth = fontRenderer.getStringWidth(line);
                if (fontRenderer.getBidiFlag()) {
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
            if (drawX + maxLineWidth > this.width) {
                drawX -= 28 + maxLineWidth;
            }
            if (drawY + boxHeight + 6 > this.height) {
                drawY = this.height - boxHeight - 6;
            }
            this.zLevel = 300.0f;
            this.itemRender.zLevel = 300.0f;
            final int j1 = -267386864;
            this.drawGradientRect(drawX - 3, drawY - 4, drawX + maxLineWidth + 3, drawY - 3, j1, j1);
            this.drawGradientRect(drawX - 3, drawY + boxHeight + 3, drawX + maxLineWidth + 3, drawY + boxHeight + 4, j1, j1);
            this.drawGradientRect(drawX - 3, drawY - 3, drawX + maxLineWidth + 3, drawY + boxHeight + 3, j1, j1);
            this.drawGradientRect(drawX - 4, drawY - 3, drawX - 3, drawY + boxHeight + 3, j1, j1);
            this.drawGradientRect(drawX + maxLineWidth + 3, drawY - 3, drawX + maxLineWidth + 4, drawY + boxHeight + 3, j1, j1);
            final int k1 = 1347420415;
            final int l1 = (k1 & 0xFEFEFE) >> 1 | (k1 & 0xFF000000);
            this.drawGradientRect(drawX - 3, drawY - 3 + 1, drawX - 3 + 1, drawY + boxHeight + 3 - 1, k1, l1);
            this.drawGradientRect(drawX + maxLineWidth + 2, drawY - 3 + 1, drawX + maxLineWidth + 3, drawY + boxHeight + 3 - 1, k1, l1);
            this.drawGradientRect(drawX - 3, drawY - 3, drawX + maxLineWidth + 3, drawY - 3 + 1, k1, k1);
            this.drawGradientRect(drawX - 3, drawY + boxHeight + 2, drawX + maxLineWidth + 3, drawY + boxHeight + 3, l1, l1);
            for (int i2 = 0; i2 < tooltip.size(); ++i2) {
                final String line2 = (String) tooltip.get(i2);
                if (fontRenderer.getBidiFlag()) {
                    final int lineWidth2 = (int)Math.ceil(fontRenderer.getStringWidth(line2) * 1.1);
                    fontRenderer.drawStringWithShadow(line2, (float)(drawX + maxLineWidth - lineWidth2), (float)drawY, -1);
                }
                else {
                    fontRenderer.drawStringWithShadow(line2, (float)drawX, (float)drawY, -1);
                }
                if (i2 == 0) {
                    drawY += 2;
                }
                drawY += 10;
            }
            this.zLevel = 0.0f;
            this.itemRender.zLevel = 0.0f;
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(32826);
        }
    }
}
