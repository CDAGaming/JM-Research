package journeymap.client.ui.component;

import net.minecraft.client.*;
import java.util.*;

public class ButtonSpacer extends Button
{
    public ButtonSpacer() {
        super("");
    }
    
    public ButtonSpacer(final int size) {
        super(size, size, "");
    }
    
    @Override
    public void drawPartialScrollable(final Minecraft minecraft, final int x, final int y, final int width, final int height) {
    }
    
    @Override
    public void func_191745_a(final Minecraft minecraft, final int mouseX, final int mouseY, final float f) {
    }
    
    @Override
    public void drawUnderline() {
    }
    
    @Override
    public boolean func_146116_c(final Minecraft minecraft, final int mouseX, final int mouseY) {
        return false;
    }
    
    @Override
    public ArrayList<String> getTooltip() {
        return null;
    }
    
    @Override
    public boolean mouseOver(final int mouseX, final int mouseY) {
        return false;
    }
}
