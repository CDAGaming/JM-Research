package journeymap.client.ui.theme;

import journeymap.client.Constants;
import journeymap.common.properties.config.BooleanField;
import net.minecraft.client.Minecraft;

public class ThemeToggle extends ThemeButton {
    public ThemeToggle(final Theme theme, final String rawlabel, final String iconName) {
        super(theme, Constants.getString(rawlabel), Constants.getString(rawlabel), iconName, null);
    }

    public ThemeToggle(final Theme theme, final String labelOn, final String labelOff, final String iconName) {
        super(theme, labelOn, labelOff, iconName, null);
    }

    public ThemeToggle(final Theme theme, final String rawlabel, final String iconName, final BooleanField field) {
        super(theme, Constants.getString(rawlabel), Constants.getString(rawlabel), iconName, field);
        if (field != null) {
            this.setToggled(field.get());
        }
    }

    @Override
    public boolean mousePressed(final Minecraft minecraft, final int mouseX, final int mouseY) {
        return (!this.toggled || !this.staysOn) && super.mousePressed(minecraft, mouseX, mouseY);
    }

    @Override
    protected String getPathPattern() {
        return "control/%stoggle_%s.png";
    }

    @Override
    protected Theme.Control.ButtonSpec getButtonSpec(final Theme theme) {
        return theme.control.toggle;
    }
}
