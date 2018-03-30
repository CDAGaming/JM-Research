package journeymap.client.ui.theme;

import com.google.gson.annotations.*;
import java.awt.*;
import com.google.common.base.*;
import net.minecraftforge.fml.common.*;

public class Theme
{
    public static final double VERSION = 2.0;
    @Since(2.0)
    public int schema;
    @Since(1.0)
    public String author;
    @Since(1.0)
    public String name;
    @Since(1.0)
    public String directory;
    @Since(1.0)
    public Container container;
    @Since(1.0)
    public Control control;
    @Since(1.0)
    public Fullscreen fullscreen;
    @Since(1.0)
    public ImageSpec icon;
    @Since(1.0)
    public Minimap minimap;
    
    public Theme() {
        this.container = new Container();
        this.control = new Control();
        this.fullscreen = new Fullscreen();
        this.icon = new ImageSpec();
        this.minimap = new Minimap();
    }
    
    public static String toHexColor(final Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    public static String toHexColor(final int rgb) {
        return toHexColor(new Color(rgb));
    }
    
    private static int getColor(final String hexColor) {
        if (!Strings.isNullOrEmpty(hexColor)) {
            try {
                final int color = Integer.parseInt(hexColor.replaceFirst("#", ""), 16);
                return color;
            }
            catch (Exception e) {
                FMLLog.warning("Journeymap theme has an invalid color string: " + hexColor, new Object[0]);
            }
        }
        return 16777215;
    }
    
    @Override
    public String toString() {
        if (Strings.isNullOrEmpty(this.name)) {
            return "???";
        }
        return this.name;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Theme theme = (Theme)o;
        Label_0062: {
            if (this.directory != null) {
                if (this.directory.equals(theme.directory)) {
                    break Label_0062;
                }
            }
            else if (theme.directory == null) {
                break Label_0062;
            }
            return false;
        }
        if (this.name != null) {
            if (this.name.equals(theme.name)) {
                return true;
            }
        }
        else if (theme.name == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.name != null) ? this.name.hashCode() : 0;
        result = 31 * result + ((this.directory != null) ? this.directory.hashCode() : 0);
        return result;
    }
    
    public static class Container
    {
        @Since(1.0)
        public Toolbar toolbar;
        
        public Container() {
            this.toolbar = new Toolbar();
        }
        
        public static class Toolbar
        {
            @Since(1.0)
            public ToolbarSpec horizontal;
            @Since(1.0)
            public ToolbarSpec vertical;
            
            public Toolbar() {
                this.horizontal = new ToolbarSpec();
                this.vertical = new ToolbarSpec();
            }
            
            public static class ToolbarSpec
            {
                @Since(1.0)
                public boolean useThemeImages;
                @Since(1.0)
                public String prefix;
                @Since(1.0)
                public int margin;
                @Since(1.0)
                public int padding;
                @Since(1.0)
                public ImageSpec begin;
                @Since(1.0)
                public ImageSpec inner;
                @Since(1.0)
                public ImageSpec end;
                
                public ToolbarSpec() {
                    this.prefix = "";
                }
            }
        }
    }
    
    public static class Control
    {
        @Since(1.0)
        public ButtonSpec button;
        @Since(1.0)
        public ButtonSpec toggle;
        
        public Control() {
            this.button = new ButtonSpec();
            this.toggle = new ButtonSpec();
        }
        
        public static class ButtonSpec
        {
            @Since(1.0)
            public boolean useThemeImages;
            @Since(1.0)
            public int width;
            @Since(1.0)
            public int height;
            @Since(1.0)
            public String prefix;
            @Since(1.0)
            public String tooltipOnStyle;
            @Since(1.0)
            public String tooltipOffStyle;
            @Since(1.0)
            public String tooltipDisabledStyle;
            @Since(2.0)
            public ColorSpec iconOn;
            @Since(2.0)
            public ColorSpec iconOff;
            @Since(2.0)
            public ColorSpec iconHoverOn;
            @Since(2.0)
            public ColorSpec iconHoverOff;
            @Since(2.0)
            public ColorSpec iconDisabled;
            @Since(2.0)
            public ColorSpec buttonOn;
            @Since(2.0)
            public ColorSpec buttonOff;
            @Since(2.0)
            public ColorSpec buttonHoverOn;
            @Since(2.0)
            public ColorSpec buttonHoverOff;
            @Since(2.0)
            public ColorSpec buttonDisabled;
            
            public ButtonSpec() {
                this.prefix = "";
                this.tooltipOnStyle = "";
                this.tooltipOffStyle = "";
                this.tooltipDisabledStyle = "";
                this.iconOn = new ColorSpec();
                this.iconOff = new ColorSpec();
                this.iconHoverOn = new ColorSpec();
                this.iconHoverOff = new ColorSpec();
                this.iconDisabled = new ColorSpec();
                this.buttonOn = new ColorSpec();
                this.buttonOff = new ColorSpec();
                this.buttonHoverOn = new ColorSpec();
                this.buttonHoverOff = new ColorSpec();
                this.buttonDisabled = new ColorSpec();
            }
        }
    }
    
    public static class Fullscreen
    {
        @Since(2.0)
        public ColorSpec background;
        @Since(1.0)
        public LabelSpec statusLabel;
        
        public Fullscreen() {
            this.background = new ColorSpec();
            this.statusLabel = new LabelSpec();
        }
    }
    
    public static class ColorSpec implements Cloneable
    {
        @Since(2.0)
        public String color;
        private transient Integer _color;
        @Since(2.0)
        public float alpha;
        
        public ColorSpec() {
            this.color = "#ffffff";
            this.alpha = 1.0f;
        }
        
        public ColorSpec(final String color, final float alpha) {
            this.color = "#ffffff";
            this.alpha = 1.0f;
            this.color = color;
            this.alpha = alpha;
        }
        
        public int getColor() {
            if (this._color == null) {
                this._color = getColor(this.color);
            }
            return this._color;
        }
        
        public ColorSpec clone() {
            final ColorSpec clone = new ColorSpec();
            clone.color = this.color;
            clone.alpha = this.alpha;
            return clone;
        }
    }
    
    public static class ImageSpec extends ColorSpec
    {
        @Since(1.0)
        public int width;
        @Since(1.0)
        public int height;
        
        public ImageSpec() {
        }
        
        public ImageSpec(final int width, final int height) {
            this.width = width;
            this.height = height;
        }
    }
    
    public static class Minimap
    {
        @Since(1.0)
        public MinimapCircle circle;
        @Since(1.0)
        public MinimapSquare square;
        
        public Minimap() {
            this.circle = new MinimapCircle();
            this.square = new MinimapSquare();
        }
        
        public abstract static class MinimapSpec
        {
            @Since(1.0)
            public int margin;
            @Since(2.0)
            public LabelSpec labelTop;
            @Since(2.0)
            public boolean labelTopInside;
            @Since(2.0)
            public LabelSpec labelBottom;
            @Since(2.0)
            public boolean labelBottomInside;
            @Since(1.0)
            public LabelSpec compassLabel;
            @Since(1.0)
            public ImageSpec compassPoint;
            @Since(1.0)
            public int compassPointLabelPad;
            @Since(1.0)
            public double compassPointOffset;
            @Since(1.0)
            public boolean compassShowNorth;
            @Since(1.0)
            public boolean compassShowSouth;
            @Since(1.0)
            public boolean compassShowEast;
            @Since(1.0)
            public boolean compassShowWest;
            @Since(1.0)
            public double waypointOffset;
            @Since(2.0)
            public ColorSpec reticle;
            @Since(2.0)
            public ColorSpec reticleHeading;
            @Since(1.0)
            public double reticleThickness;
            @Since(1.0)
            public double reticleHeadingThickness;
            @Since(2.0)
            public int reticleOffsetOuter;
            @Since(2.0)
            public int reticleOffsetInner;
            @Since(2.0)
            public ColorSpec frame;
            @Since(1.0)
            public String prefix;
            
            public MinimapSpec() {
                this.labelTop = new LabelSpec();
                this.labelTopInside = false;
                this.labelBottom = new LabelSpec();
                this.labelBottomInside = false;
                this.compassLabel = new LabelSpec();
                this.compassPoint = new ImageSpec();
                this.compassShowNorth = true;
                this.compassShowSouth = true;
                this.compassShowEast = true;
                this.compassShowWest = true;
                this.reticle = new ColorSpec();
                this.reticleHeading = new ColorSpec();
                this.reticleThickness = 2.25;
                this.reticleHeadingThickness = 2.5;
                this.reticleOffsetOuter = 16;
                this.reticleOffsetInner = 16;
                this.frame = new ColorSpec();
                this.prefix = "";
            }
        }
        
        public static class MinimapCircle extends MinimapSpec
        {
            @Since(1.0)
            public ImageSpec rim256;
            @Since(1.0)
            public ImageSpec mask256;
            @Since(1.0)
            public ImageSpec rim512;
            @Since(1.0)
            public ImageSpec mask512;
            @Since(2.0)
            public boolean rotates;
            
            public MinimapCircle() {
                this.rim256 = new ImageSpec(256, 256);
                this.mask256 = new ImageSpec(256, 256);
                this.rim512 = new ImageSpec(512, 512);
                this.mask512 = new ImageSpec(512, 512);
                this.rotates = false;
            }
        }
        
        public static class MinimapSquare extends MinimapSpec
        {
            @Since(1.0)
            public ImageSpec topLeft;
            @Since(1.0)
            public ImageSpec top;
            @Since(1.0)
            public ImageSpec topRight;
            @Since(1.0)
            public ImageSpec right;
            @Since(1.0)
            public ImageSpec bottomRight;
            @Since(1.0)
            public ImageSpec bottom;
            @Since(1.0)
            public ImageSpec bottomLeft;
            @Since(1.0)
            public ImageSpec left;
            
            public MinimapSquare() {
                this.topLeft = new ImageSpec();
                this.top = new ImageSpec();
                this.topRight = new ImageSpec();
                this.right = new ImageSpec();
                this.bottomRight = new ImageSpec();
                this.bottom = new ImageSpec();
                this.bottomLeft = new ImageSpec();
                this.left = new ImageSpec();
            }
        }
    }
    
    public static class LabelSpec implements Cloneable
    {
        @Since(2.0)
        public int margin;
        @Since(2.0)
        public ColorSpec background;
        @Since(2.0)
        public ColorSpec foreground;
        @Since(2.0)
        public ColorSpec highlight;
        @Since(1.0)
        public boolean shadow;
        
        public LabelSpec() {
            this.margin = 2;
            this.background = new ColorSpec("#000000", 0.0f);
            this.foreground = new ColorSpec();
            this.highlight = new ColorSpec();
            this.shadow = false;
        }
        
        public LabelSpec clone() {
            final LabelSpec clone = new LabelSpec();
            clone.margin = this.margin;
            clone.background = this.background.clone();
            clone.foreground = this.foreground.clone();
            clone.highlight = this.highlight.clone();
            return clone;
        }
    }
    
    public static class DefaultPointer
    {
        @Since(1.0)
        public String directory;
        @Since(1.0)
        public String filename;
        @Since(1.0)
        public String name;
        
        protected DefaultPointer() {
        }
        
        public DefaultPointer(final Theme theme) {
            this.name = theme.name;
            this.filename = theme.name;
            this.directory = theme.directory;
        }
    }
}
