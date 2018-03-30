package journeymap.client.ui.theme.impl;

import java.util.*;
import journeymap.client.ui.theme.*;

public class FlatTheme extends Theme
{
    public static Theme createPurist() {
        final Style style = new Style();
        style.button.on = "#ffffff";
        style.button.off = "#aaaaaa";
        style.button.hover = "#00ffff";
        style.button.disabled = "#aaaaaa";
        style.toggle.on = "#aaaaaa";
        style.toggle.off = "#ffffff";
        style.toggle.hover = "#00ffff";
        style.toggle.disabled = "#aaaaaa";
        style.label.background.alpha = 0.6f;
        style.label.background.color = "#222222";
        style.label.foreground.alpha = 1.0f;
        style.label.foreground.color = "#dddddd";
        style.label.highlight.alpha = 1.0f;
        style.label.highlight.color = "#ffffff";
        style.label.shadow = true;
        style.fullscreenColorSpec.alpha = 0.8f;
        style.fullscreenColorSpec.color = "#222222";
        style.frameColorSpec.color = "#cccccc";
        style.frameColorSpec.alpha = 1.0f;
        style.toolbarColorSpec.color = "#000000";
        style.toolbarColorSpec.alpha = 0.0f;
        style.useThemeImages = false;
        style.minimapTexPrefix = "pur_";
        style.iconSize = 20;
        style.toolbarPadding = 0;
        final Theme theme = new FlatTheme("Purist", "techbrew", style);
        for (final Minimap.MinimapSpec minimapSpec : Arrays.asList(theme.minimap.circle, theme.minimap.square)) {
            minimapSpec.margin = 6;
            minimapSpec.reticle.color = "#222222";
            minimapSpec.reticleHeading.color = "#222222";
            minimapSpec.labelTop.foreground.color = "#cccccc";
            minimapSpec.labelTop.background.color = "#55555";
            minimapSpec.labelBottom.foreground.color = "#cccccc";
            minimapSpec.labelBottom.background.color = "#55555";
            minimapSpec.compassLabel.foreground.color = "#cccccc";
            minimapSpec.compassLabel.background.color = "#222222";
            minimapSpec.compassLabel.background.alpha = 0.5f;
        }
        return theme;
    }
    
    public static Theme createDesertTemple() {
        final String light = "#FFFFCD";
        final String medium = "#aea87e";
        final String dark = "#B7521E";
        final String darker = "#803915";
        final String darkest = "#361809";
        return createFlatTheme("DesertTemple", light, medium, dark, darker, darkest);
    }
    
    public static Theme createForestMansion() {
        final String light = "#d2e7d2";
        final String medium = "#7ab97a";
        final String dark = "#1b6f1b";
        final String darker = "#114511";
        final String darkest = "#061b06";
        return createFlatTheme("ForestMansion", light, medium, dark, darker, darkest);
    }
    
    public static Theme createNetherFortress() {
        final String light = "#FFFF00";
        final String medium = "#D2D200";
        final String dark = "#6f3634";
        final String darker = "#760000";
        final String darkest = "#3b0000";
        final Theme theme = createFlatTheme("NetherFortress", light, medium, dark, darker, darkest);
        return theme;
    }
    
    public static Theme createStronghold() {
        final String light = "#000000";
        final String medium = "#cccccc";
        final String dark = "#222222";
        final String darker = "#111111";
        final String darkest = "#0a1d33";
        final Theme theme = createFlatTheme("Stronghold", light, medium, dark, darker, darkest);
        theme.container.toolbar.horizontal.inner.alpha = 0.5f;
        theme.container.toolbar.horizontal.inner.color = darker;
        theme.container.toolbar.vertical.inner.alpha = 0.5f;
        theme.container.toolbar.vertical.inner.color = darker;
        theme.control.button.buttonOff.color = medium;
        theme.control.button.buttonDisabled.color = darker;
        theme.control.button.iconDisabled.color = medium;
        theme.control.toggle.buttonOn.color = dark;
        theme.control.toggle.buttonOff.color = dark;
        theme.control.toggle.iconHoverOn.color = medium;
        theme.control.toggle.iconHoverOff.color = medium;
        theme.control.toggle.iconOn.color = medium;
        theme.control.toggle.iconOff.color = "#555555";
        theme.fullscreen.statusLabel.background.color = darker;
        theme.fullscreen.statusLabel.foreground.color = medium;
        final String white = "#ffffff";
        final String black = "#000000";
        for (final Minimap.MinimapSpec minimapSpec : Arrays.asList(theme.minimap.circle, theme.minimap.square)) {
            minimapSpec.labelTop.foreground.color = white;
            minimapSpec.labelTop.background.color = black;
            minimapSpec.labelBottom.foreground.color = white;
            minimapSpec.labelBottom.background.color = black;
            minimapSpec.compassLabel.foreground.color = white;
            minimapSpec.compassLabel.background.color = black;
        }
        return theme;
    }
    
    public static Theme createOceanMonument() {
        final String light = "#dfebec";
        final String medium = "#afcecf";
        final String dark = "#303dc1";
        final String darker = "#212a87";
        final String darkest = "#0e1239";
        final Theme theme = createFlatTheme("OceanMonument", light, medium, dark, darker, darkest);
        theme.control.toggle.iconDisabled.color = "#555555";
        return theme;
    }
    
    public static Theme EndCity() {
        final String light = "#EAEE9A";
        final String dark = "#5A5470";
        final String medium = "#CEB46A";
        final String darker = "#362744";
        final String darkest = "#1F1D2D";
        return createFlatTheme("EndCity", light, medium, dark, darker, darkest);
    }
    
    private static Theme createFlatTheme(final String themeName, final String light, final String medium, final String dark, final String darker, final String darkest) {
        final String white = "#ffffff";
        final Style style = new Style();
        style.toggle.on = light;
        style.toggle.off = dark;
        style.toggle.hover = medium;
        style.toggle.disabled = darker;
        style.button.on = light;
        style.button.off = dark;
        style.button.hover = medium;
        style.button.disabled = darker;
        style.label.background.alpha = 0.7f;
        style.label.background.color = darkest;
        style.label.foreground.alpha = 1.0f;
        style.label.foreground.color = light;
        style.label.highlight.alpha = 1.0f;
        style.label.highlight.color = white;
        style.label.shadow = true;
        style.fullscreenColorSpec.alpha = 0.8f;
        style.fullscreenColorSpec.color = darker;
        style.frameColorSpec.color = darker;
        style.frameColorSpec.alpha = 1.0f;
        style.toolbarColorSpec.color = darkest;
        style.toolbarColorSpec.alpha = 0.8f;
        style.minimapTexPrefix = "flat_";
        style.buttonTexPrefix = "flat_";
        final Theme theme = new FlatTheme(themeName, "techbrew", style);
        theme.minimap.circle.margin = 6;
        theme.minimap.square.margin = 6;
        theme.minimap.circle.reticle.color = dark;
        theme.minimap.circle.reticleHeading.color = dark;
        theme.minimap.square.reticle.color = dark;
        theme.minimap.square.reticleHeading.color = dark;
        return theme;
    }
    
    protected FlatTheme(final String name, final String author, final Style style) {
        this.name = name;
        this.author = author;
        this.schema = 2;
        this.directory = ThemePresets.DEFAULT_DIRECTORY;
        this.control.button = button(style);
        this.control.toggle = toggle(style);
        this.fullscreen = fullscreen(style);
        this.container.toolbar.horizontal = toolbar(style, "h", 0, style.iconSize);
        this.container.toolbar.vertical = toolbar(style, "v", style.iconSize, 0);
        this.icon.width = style.iconSize;
        this.icon.height = style.iconSize;
        this.minimap.square = minimapSquare(style);
        this.minimap.circle = minimapCircle(style);
    }
    
    private static Control.ButtonSpec commonButton(final Style style) {
        final Control.ButtonSpec button = new Control.ButtonSpec();
        button.useThemeImages = style.useThemeImages;
        button.width = style.iconSize;
        button.height = style.iconSize;
        button.prefix = style.buttonTexPrefix;
        button.tooltipOnStyle = style.tooltipOnStyle;
        button.tooltipOffStyle = style.tooltipOffStyle;
        button.tooltipDisabledStyle = style.tooltipDisabledStyle;
        return button;
    }
    
    private static Control.ButtonSpec button(final Style style) {
        final Control.ButtonSpec button = commonButton(style);
        button.iconOn.color = style.button.off;
        button.buttonOn.color = style.button.on;
        button.iconOff.color = style.button.on;
        button.buttonOff.color = style.button.off;
        button.iconHoverOn.color = style.button.hover;
        button.buttonHoverOn.color = style.button.on;
        button.iconHoverOff.color = style.button.hover;
        button.buttonHoverOff.color = style.button.off;
        button.iconDisabled.color = style.button.disabled;
        button.buttonDisabled.color = style.button.off;
        return button;
    }
    
    private static Control.ButtonSpec toggle(final Style style) {
        final Control.ButtonSpec button = commonButton(style);
        button.iconOn.color = style.toggle.off;
        button.buttonOn.color = style.toggle.on;
        button.iconOff.color = style.toggle.on;
        button.buttonOff.color = style.toggle.off;
        button.iconHoverOn.color = style.toggle.hover;
        button.buttonHoverOn.color = style.toggle.on;
        button.iconHoverOff.color = style.toggle.hover;
        button.buttonHoverOff.color = style.toggle.off;
        button.iconDisabled.color = style.toggle.disabled;
        button.buttonDisabled.color = style.toggle.disabled;
        return button;
    }
    
    private static Fullscreen fullscreen(final Style style) {
        final Fullscreen fullscreen = new Fullscreen();
        fullscreen.background = style.fullscreenColorSpec.clone();
        fullscreen.statusLabel = style.label.clone();
        return fullscreen;
    }
    
    private static Container.Toolbar.ToolbarSpec toolbar(final Style style, final String prefix, final int toolbarCapsWidth, final int toolbarCapsHeight) {
        final Container.Toolbar.ToolbarSpec toolbar = new Container.Toolbar.ToolbarSpec();
        toolbar.useThemeImages = true;
        toolbar.prefix = prefix;
        toolbar.margin = style.toolbarMargin;
        toolbar.padding = style.toolbarPadding;
        toolbar.begin = new ImageSpec(toolbarCapsWidth, toolbarCapsHeight);
        toolbar.begin.alpha = style.toolbarColorSpec.alpha;
        toolbar.begin.color = style.toolbarColorSpec.color;
        toolbar.inner = new ImageSpec(style.iconSize, style.iconSize);
        toolbar.inner.alpha = style.toolbarColorSpec.alpha;
        toolbar.inner.color = style.toolbarColorSpec.color;
        toolbar.end = new ImageSpec(toolbarCapsWidth, toolbarCapsHeight);
        toolbar.end.alpha = style.toolbarColorSpec.alpha;
        toolbar.end.color = style.toolbarColorSpec.color;
        return toolbar;
    }
    
    private static Minimap.MinimapSquare minimapSquare(final Style style) {
        final Minimap.MinimapSquare minimap = new Minimap.MinimapSquare();
        applyCommonMinimap(style, minimap);
        minimap.margin = 4;
        minimap.compassPointOffset = -style.squareFrameThickness - 4;
        minimap.reticleOffsetOuter = 24;
        final ImageSpec cornerSpec = new ImageSpec(style.squareFrameThickness, style.squareFrameThickness);
        final ImageSpec sidesSpec = new ImageSpec(style.squareFrameThickness, 1);
        final ImageSpec topBottomSpec = new ImageSpec(1, style.squareFrameThickness);
        final Minimap.MinimapSquare minimapSquare = minimap;
        final Minimap.MinimapSquare minimapSquare2 = minimap;
        final ImageSpec imageSpec = sidesSpec;
        minimapSquare2.right = imageSpec;
        minimapSquare.left = imageSpec;
        final Minimap.MinimapSquare minimapSquare3 = minimap;
        final Minimap.MinimapSquare minimapSquare4 = minimap;
        final ImageSpec imageSpec2 = topBottomSpec;
        minimapSquare4.bottom = imageSpec2;
        minimapSquare3.top = imageSpec2;
        final Minimap.MinimapSquare minimapSquare5 = minimap;
        final Minimap.MinimapSquare minimapSquare6 = minimap;
        final Minimap.MinimapSquare minimapSquare7 = minimap;
        final Minimap.MinimapSquare minimapSquare8 = minimap;
        final ImageSpec imageSpec3 = cornerSpec;
        minimapSquare8.bottomLeft = imageSpec3;
        minimapSquare7.bottomRight = imageSpec3;
        minimapSquare6.topRight = imageSpec3;
        minimapSquare5.topLeft = imageSpec3;
        return minimap;
    }
    
    private static Minimap.MinimapCircle minimapCircle(final Style style) {
        final Minimap.MinimapCircle minimap = new Minimap.MinimapCircle();
        applyCommonMinimap(style, minimap);
        minimap.margin = style.circleFrameThickness;
        minimap.compassPointOffset = -style.circleFrameThickness - 6;
        minimap.reticleHeading.alpha = 0.4f;
        minimap.compassShowEast = true;
        minimap.compassShowNorth = true;
        minimap.compassShowSouth = true;
        minimap.compassShowWest = true;
        minimap.reticleOffsetOuter = 30;
        minimap.rim256 = new ImageSpec(256, 256);
        minimap.rim512 = new ImageSpec(512, 512);
        return minimap;
    }
    
    private static void applyCommonMinimap(final Style style, final Minimap.MinimapSpec minimap) {
        minimap.compassLabel = style.label.clone();
        minimap.compassLabel.background.alpha = 0.0f;
        minimap.compassPoint.height = 16;
        minimap.compassPoint.width = 16;
        minimap.compassPoint.color = style.frameColorSpec.color;
        minimap.compassPoint.alpha = 0.5f;
        minimap.compassPointLabelPad = 0;
        minimap.compassShowEast = true;
        minimap.compassShowNorth = true;
        minimap.compassShowSouth = true;
        minimap.compassShowWest = true;
        minimap.frame = style.frameColorSpec.clone();
        minimap.labelBottom = style.label.clone();
        minimap.labelBottomInside = false;
        minimap.labelTop = style.label.clone();
        minimap.labelTopInside = false;
        minimap.prefix = style.minimapTexPrefix;
        minimap.reticle.alpha = 0.4f;
        minimap.reticle.color = style.button.on;
        minimap.reticleHeading.alpha = 0.4f;
        minimap.reticleHeading.color = style.button.on;
        minimap.reticleHeadingThickness = 2.25;
        minimap.reticleOffsetInner = 20;
        minimap.reticleOffsetOuter = 20;
        minimap.reticleThickness = 1.25;
        minimap.waypointOffset = 0.0;
    }
}
