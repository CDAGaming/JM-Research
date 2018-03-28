package journeymap.client.ui.fullscreen;

import java.io.*;
import net.minecraft.client.renderer.*;
import org.lwjgl.opengl.*;
import net.minecraft.client.gui.*;

public class MapChat extends GuiChat
{
    protected boolean hidden;
    protected int cursorCounter;
    
    public MapChat(final String defaultText, final boolean hidden) {
        super(defaultText);
        this.hidden = false;
        this.hidden = hidden;
    }
    
    public void onGuiClosed() {
        super.onGuiClosed();
        this.hidden = true;
    }
    
    public void close() {
        this.onGuiClosed();
    }
    
    public void updateScreen() {
        if (this.hidden) {
            return;
        }
        super.updateScreen();
    }
    
    public void keyTyped(final char typedChar, final int keyCode) throws IOException {
        if (this.hidden) {
            return;
        }
        if (keyCode == 1) {
            this.close();
        }
        else if (keyCode != 28 && keyCode != 156) {
            super.keyTyped(typedChar, keyCode);
        }
        else {
            final String s = this.inputField.getText().trim();
            if (!s.isEmpty()) {
                this.sendChatMessage(s);
            }
            this.inputField.setText("");
            this.mc.ingameGUI.getChatGUI().resetScroll();
        }
    }
    
    public void handleMouseInput() throws IOException {
        if (this.hidden) {
            return;
        }
        super.handleMouseInput();
    }
    
    public void mouseClicked(final int par1, final int par2, final int par3) throws IOException {
        if (this.hidden) {
            return;
        }
        super.mouseClicked(par1, par2, par3);
    }
    
    public void confirmClicked(final boolean par1, final int par2) {
        if (this.hidden) {
            return;
        }
        super.confirmClicked(par1, par2);
    }
    
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        GlStateManager.pushMatrix();
        GL11.glTranslatef(0.0f, this.height - 47.5f, 0.0f);
        if (this.mc != null && this.mc.ingameGUI != null && this.mc.ingameGUI.getChatGUI() != null) {
            final GuiNewChat getChatGUI = this.mc.ingameGUI.getChatGUI();
            int n;
            if (this.hidden) {
                n = this.mc.ingameGUI.getUpdateCounter();
            }
            else {
                this.cursorCounter = (n = this.cursorCounter) + 1;
            }
            getChatGUI.drawChat(n);
        }
        GlStateManager.popMatrix();
        if (this.hidden) {
            return;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    public boolean isHidden() {
        return this.hidden;
    }
    
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }
    
    public void setText(final String defaultText) {
        this.inputField.setText(defaultText);
    }
}
