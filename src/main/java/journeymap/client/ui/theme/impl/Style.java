package journeymap.client.ui.theme.impl;

import journeymap.client.ui.theme.*;
import net.minecraft.util.text.*;

class Style
{
    Theme.LabelSpec label;
    Colors button;
    Colors toggle;
    Colors text;
    String minimapTexPrefix;
    String buttonTexPrefix;
    String tooltipOnStyle;
    String tooltipOffStyle;
    String tooltipDisabledStyle;
    int iconSize;
    Theme.ColorSpec frameColorSpec;
    Theme.ColorSpec toolbarColorSpec;
    Theme.ColorSpec fullscreenColorSpec;
    int squareFrameThickness;
    int circleFrameThickness;
    int toolbarMargin;
    int toolbarPadding;
    boolean useThemeImages;
    
    Style() {
        this.label = new Theme.LabelSpec();
        this.button = new Colors();
        this.toggle = new Colors();
        this.text = new Colors();
        this.minimapTexPrefix = "";
        this.buttonTexPrefix = "";
        this.tooltipOnStyle = TextFormatting.AQUA.toString();
        this.tooltipOffStyle = TextFormatting.GRAY.toString();
        this.tooltipDisabledStyle = TextFormatting.DARK_GRAY.toString();
        this.iconSize = 24;
        this.frameColorSpec = new Theme.ColorSpec();
        this.toolbarColorSpec = new Theme.ColorSpec();
        this.fullscreenColorSpec = new Theme.ColorSpec();
        this.squareFrameThickness = 8;
        this.circleFrameThickness = 8;
        this.toolbarMargin = 4;
        this.toolbarPadding = 0;
        this.useThemeImages = true;
        this.label.margin = 0;
    }
    
    static class Colors
    {
        String on;
        String off;
        String hover;
        String disabled;
    }
}
