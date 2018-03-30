package journeymap.client.ui.dialog;

import net.minecraftforge.fml.client.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import java.util.*;

public class OptionsGuiFactory implements IModGuiFactory
{
    public void initialize(final Minecraft minecraftInstance) {
    }
    
    public boolean hasConfigGui() {
        return false;
    }
    
    public GuiScreen createConfigGui(final GuiScreen parentScreen) {
        return null;
    }
    
    public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
