package journeymap.client.ui.dialog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

public class OptionsGuiFactory implements IModGuiFactory {
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
