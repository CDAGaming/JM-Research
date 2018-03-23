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
    
    public void func_146281_b() {
        super.func_146281_b();
        this.hidden = true;
    }
    
    public void close() {
        this.func_146281_b();
    }
    
    public void func_73876_c() {
        if (this.hidden) {
            return;
        }
        super.func_73876_c();
    }
    
    public void func_73869_a(final char typedChar, final int keyCode) throws IOException {
        if (this.hidden) {
            return;
        }
        if (keyCode == 1) {
            this.close();
        }
        else if (keyCode != 28 && keyCode != 156) {
            super.func_73869_a(typedChar, keyCode);
        }
        else {
            final String s = this.field_146415_a.func_146179_b().trim();
            if (!s.isEmpty()) {
                this.func_175275_f(s);
            }
            this.field_146415_a.func_146180_a("");
            this.field_146297_k.field_71456_v.func_146158_b().func_146240_d();
        }
    }
    
    public void func_146274_d() throws IOException {
        if (this.hidden) {
            return;
        }
        super.func_146274_d();
    }
    
    public void func_73864_a(final int par1, final int par2, final int par3) throws IOException {
        if (this.hidden) {
            return;
        }
        super.func_73864_a(par1, par2, par3);
    }
    
    public void func_73878_a(final boolean par1, final int par2) {
        if (this.hidden) {
            return;
        }
        super.func_73878_a(par1, par2);
    }
    
    public void func_73863_a(final int mouseX, final int mouseY, final float partialTicks) {
        GlStateManager.func_179094_E();
        GL11.glTranslatef(0.0f, this.field_146295_m - 47.5f, 0.0f);
        if (this.field_146297_k != null && this.field_146297_k.field_71456_v != null && this.field_146297_k.field_71456_v.func_146158_b() != null) {
            final GuiNewChat func_146158_b = this.field_146297_k.field_71456_v.func_146158_b();
            int n;
            if (this.hidden) {
                n = this.field_146297_k.field_71456_v.func_73834_c();
            }
            else {
                this.cursorCounter = (n = this.cursorCounter) + 1;
            }
            func_146158_b.func_146230_a(n);
        }
        GlStateManager.func_179121_F();
        if (this.hidden) {
            return;
        }
        super.func_73863_a(mouseX, mouseY, partialTicks);
    }
    
    public boolean isHidden() {
        return this.hidden;
    }
    
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }
    
    public void setText(final String defaultText) {
        this.field_146415_a.func_146180_a(defaultText);
    }
}
