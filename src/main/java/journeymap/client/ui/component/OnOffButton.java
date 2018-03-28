package journeymap.client.ui.component;

import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;

public class OnOffButton extends Button {
    protected Boolean toggled;
    protected String labelOn;
    protected String labelOff;
    protected ArrayList<ToggleListener> toggleListeners;

    public OnOffButton(final String labelOn, final String labelOff, final boolean toggled) {
        this(0, labelOn, labelOff, toggled);
    }

    public OnOffButton(final int id, final String labelOn, final String labelOff, final boolean toggled) {
        super(toggled ? labelOn : labelOff);
        this.toggled = true;
        this.toggleListeners = new ArrayList<ToggleListener>(0);
        this.labelOn = labelOn;
        this.labelOff = labelOff;
        this.setToggled(toggled);
        this.finishInit();
    }

    public void setLabels(final String labelOn, final String labelOff) {
        this.labelOn = labelOn;
        this.labelOff = labelOff;
        this.updateLabel();
    }

    @Override
    protected void updateLabel() {
        if (this.labelOn != null && this.labelOff != null) {
            super.displayString = (this.getToggled() ? this.labelOn : this.labelOff);
        }
    }

    public void toggle() {
        this.setToggled(!this.getToggled());
    }

    @Override
    public int getFitWidth(final FontRenderer fr) {
        int max = fr.getStringWidth(this.displayString);
        if (this.labelOn != null) {
            max = Math.max(max, fr.getStringWidth(this.labelOn));
        }
        if (this.labelOff != null) {
            max = Math.max(max, fr.getStringWidth(this.labelOff));
        }
        return max + this.WIDTH_PAD;
    }

    @Override
    public boolean isActive() {
        return this.isEnabled() && this.toggled;
    }

    public Boolean getToggled() {
        return this.toggled;
    }

    public void setToggled(final Boolean toggled) {
        this.setToggled(toggled, true);
    }

    public void setToggled(final Boolean toggled, final boolean notifyToggleListener) {
        if (this.toggled == toggled || !this.isEnabled() || !this.visible) {
            return;
        }
        boolean allowChange = true;
        try {
            if (notifyToggleListener && !this.toggleListeners.isEmpty()) {
                for (final ToggleListener listener : this.toggleListeners) {
                    allowChange = listener.onToggle(this, toggled);
                    if (!allowChange) {
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("Error trying to toggle button '" + this.displayString + "': " + LogFormatter.toString(t));
            allowChange = false;
        }
        if (allowChange) {
            this.toggled = toggled;
            this.updateLabel();
        }
    }

    public void addToggleListener(final ToggleListener toggleListener) {
        this.toggleListeners.add(toggleListener);
    }

    public interface ToggleListener {
        boolean onToggle(final OnOffButton p0, final boolean p1);
    }
}
