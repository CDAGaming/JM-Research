package journeymap.client.ui.minimap;

import java.awt.geom.*;
import net.minecraft.util.*;
import journeymap.client.ui.theme.*;
import journeymap.client.ui.option.*;
import net.minecraft.client.*;
import journeymap.client.properties.*;
import journeymap.client.io.*;
import journeymap.client.render.draw.*;
import net.minecraft.client.gui.*;
import java.util.*;
import journeymap.client.model.*;
import journeymap.client.render.texture.*;

public class DisplayVars
{
    final Position position;
    final Shape shape;
    final Orientation orientation;
    final double fontScale;
    final int displayWidth;
    final int displayHeight;
    final float terrainAlpha;
    final ScaledResolution scaledResolution;
    final int minimapWidth;
    final int minimapHeight;
    final int textureX;
    final int textureY;
    final int translateX;
    final int translateY;
    final double reticleSegmentLength;
    final Point2D.Double centerPoint;
    final boolean showCompass;
    final boolean showReticle;
    final List<Tuple<LabelVars, ThemeLabelSource>> labels;
    final Theme theme;
    final ThemeMinimapFrame minimapFrame;
    final ThemeCompassPoints minimapCompassPoints;
    final Theme.Minimap.MinimapSpec minimapSpec;
    final LocationFormat.LocationFormatKeys locationFormatKeys;
    final boolean locationFormatVerbose;
    final boolean frameRotates;
    int marginX;
    int marginY;
    MapTypeStatus mapTypeStatus;
    MapPresetStatus mapPresetStatus;
    
    DisplayVars(final Minecraft mc, final MiniMapProperties miniMapProperties) {
        this.labels = new ArrayList<Tuple<LabelVars, ThemeLabelSource>>(4);
        this.scaledResolution = new ScaledResolution(mc);
        this.showCompass = miniMapProperties.showCompass.get();
        this.showReticle = miniMapProperties.showReticle.get();
        this.position = miniMapProperties.position.get();
        this.orientation = miniMapProperties.orientation.get();
        this.displayWidth = mc.field_71443_c;
        this.displayHeight = mc.field_71440_d;
        this.terrainAlpha = Math.max(0.0f, Math.min(1.0f, miniMapProperties.terrainAlpha.get() / 100.0f));
        this.locationFormatKeys = new LocationFormat().getFormatKeys(miniMapProperties.locationFormat.get());
        this.locationFormatVerbose = miniMapProperties.locationFormatVerbose.get();
        this.theme = ThemeLoader.getCurrentTheme();
        Label_0428: {
            switch (miniMapProperties.shape.get()) {
                case Rectangle: {
                    if (this.theme.minimap.square != null) {
                        this.shape = Shape.Rectangle;
                        this.minimapSpec = this.theme.minimap.square;
                        final double ratio = mc.field_71443_c * 1.0 / mc.field_71440_d;
                        this.minimapHeight = miniMapProperties.getSize();
                        this.minimapWidth = (int)(this.minimapHeight * ratio);
                        this.reticleSegmentLength = this.minimapWidth / 1.5;
                        break Label_0428;
                    }
                }
                case Circle: {
                    if (this.theme.minimap.circle != null) {
                        this.shape = Shape.Circle;
                        this.minimapSpec = this.theme.minimap.circle;
                        this.minimapWidth = miniMapProperties.getSize();
                        this.minimapHeight = miniMapProperties.getSize();
                        this.reticleSegmentLength = this.minimapHeight / 2;
                        break Label_0428;
                    }
                    break;
                }
            }
            this.shape = Shape.Square;
            this.minimapSpec = this.theme.minimap.square;
            this.minimapWidth = miniMapProperties.getSize();
            this.minimapHeight = miniMapProperties.getSize();
            this.reticleSegmentLength = Math.sqrt(this.minimapHeight * this.minimapHeight + this.minimapWidth * this.minimapWidth) / 2.0;
        }
        this.fontScale = miniMapProperties.fontScale.get();
        final FontRenderer fontRenderer = mc.field_71466_p;
        final int topInfoLabelsHeight = this.getInfoLabelAreaHeight(fontRenderer, this.minimapSpec.labelTop, miniMapProperties.info1Label.get(), miniMapProperties.info2Label.get());
        final int bottomInfoLabelsHeight = this.getInfoLabelAreaHeight(fontRenderer, this.minimapSpec.labelBottom, miniMapProperties.info3Label.get(), miniMapProperties.info4Label.get());
        final int compassFontScale = miniMapProperties.compassFontScale.get();
        int compassLabelHeight = 0;
        if (this.showCompass) {
            compassLabelHeight = DrawUtil.getLabelHeight(fontRenderer, this.minimapSpec.compassLabel.shadow) * compassFontScale;
        }
        this.minimapFrame = new ThemeMinimapFrame(this.theme, this.minimapSpec, miniMapProperties, this.minimapWidth, this.minimapHeight);
        final int margin = this.minimapSpec.margin;
        this.marginY = margin;
        this.marginX = margin;
        final int halfWidth = this.minimapWidth / 2;
        final int halfHeight = this.minimapHeight / 2;
        if (this.showCompass) {
            final boolean compassExists = this.minimapSpec.compassPoint != null && this.minimapSpec.compassPoint.width > 0;
            double compassPointMargin;
            if (compassExists) {
                final TextureImpl compassPointTex = this.minimapFrame.getCompassPoint();
                final float compassPointScale = ThemeCompassPoints.getCompassPointScale(compassLabelHeight, this.minimapSpec, compassPointTex);
                compassPointMargin = compassPointTex.getWidth() / 2 * compassPointScale;
            }
            else {
                compassPointMargin = compassLabelHeight;
            }
            this.marginX = (int)Math.max(this.marginX, Math.ceil(compassPointMargin));
            this.marginY = (int)Math.max(this.marginY, Math.ceil(compassPointMargin) + compassLabelHeight / 2);
        }
        switch (this.position) {
            case BottomRight: {
                if (!this.minimapSpec.labelBottomInside) {
                    this.marginY += bottomInfoLabelsHeight;
                }
                this.textureX = mc.field_71443_c - this.minimapWidth - this.marginX;
                this.textureY = mc.field_71440_d - this.minimapHeight - this.marginY;
                this.translateX = mc.field_71443_c / 2 - halfWidth - this.marginX;
                this.translateY = mc.field_71440_d / 2 - halfHeight - this.marginY;
                break;
            }
            case TopLeft: {
                if (!this.minimapSpec.labelTopInside) {
                    this.marginY = Math.max(this.marginY, topInfoLabelsHeight + 2 * this.minimapSpec.margin);
                }
                this.textureX = this.marginX;
                this.textureY = this.marginY;
                this.translateX = -(mc.field_71443_c / 2) + halfWidth + this.marginX;
                this.translateY = -(mc.field_71440_d / 2) + halfHeight + this.marginY;
                break;
            }
            case BottomLeft: {
                if (!this.minimapSpec.labelBottomInside) {
                    this.marginY += bottomInfoLabelsHeight;
                }
                this.textureX = this.marginX;
                this.textureY = mc.field_71440_d - this.minimapHeight - this.marginY;
                this.translateX = -(mc.field_71443_c / 2) + halfWidth + this.marginX;
                this.translateY = mc.field_71440_d / 2 - halfHeight - this.marginY;
                break;
            }
            case TopCenter: {
                if (!this.minimapSpec.labelTopInside) {
                    this.marginY = Math.max(this.marginY, topInfoLabelsHeight + 2 * this.minimapSpec.margin);
                }
                this.textureX = (mc.field_71443_c - this.minimapWidth) / 2;
                this.textureY = this.marginY;
                this.translateX = 0;
                this.translateY = -(mc.field_71440_d / 2) + halfHeight + this.marginY;
                break;
            }
            case Center: {
                this.textureX = (mc.field_71443_c - this.minimapWidth) / 2;
                this.textureY = (mc.field_71440_d - this.minimapHeight) / 2;
                this.translateX = 0;
                this.translateY = 0;
                break;
            }
            default: {
                if (!this.minimapSpec.labelTopInside) {
                    this.marginY = Math.max(this.marginY, topInfoLabelsHeight + 2 * this.minimapSpec.margin);
                }
                this.textureX = mc.field_71443_c - this.minimapWidth - this.marginX;
                this.textureY = this.marginY;
                this.translateX = mc.field_71443_c / 2 - halfWidth - this.marginX;
                this.translateY = -(mc.field_71440_d / 2) + halfHeight + this.marginY;
                break;
            }
        }
        this.minimapFrame.setPosition(this.textureX, this.textureY);
        this.centerPoint = new Point2D.Double(this.textureX + halfWidth, this.textureY + halfHeight);
        this.minimapCompassPoints = new ThemeCompassPoints(this.textureX, this.textureY, halfWidth, halfHeight, this.minimapSpec, miniMapProperties, this.minimapFrame.getCompassPoint(), compassLabelHeight);
        if (this.shape == Shape.Circle) {
            this.frameRotates = ((Theme.Minimap.MinimapCircle)this.minimapSpec).rotates;
        }
        else {
            this.frameRotates = false;
        }
        final int centerX = (int)Math.floor(this.textureX + this.minimapWidth / 2);
        if (topInfoLabelsHeight > 0) {
            final int startY = this.minimapSpec.labelTopInside ? (this.textureY + this.minimapSpec.margin) : (this.textureY - this.minimapSpec.margin - topInfoLabelsHeight);
            this.positionLabels(fontRenderer, centerX, startY, this.minimapSpec.labelTop, miniMapProperties.info1Label.get(), miniMapProperties.info2Label.get());
        }
        if (bottomInfoLabelsHeight > 0) {
            int startY = this.textureY + this.minimapHeight;
            startY += (this.minimapSpec.labelBottomInside ? (-this.minimapSpec.margin - bottomInfoLabelsHeight) : this.minimapSpec.margin);
            this.positionLabels(fontRenderer, centerX, startY, this.minimapSpec.labelBottom, miniMapProperties.info3Label.get(), miniMapProperties.info4Label.get());
        }
        ThemeLabelSource.resetCaches();
    }
    
    private int getInfoLabelAreaHeight(final FontRenderer fontRenderer, final Theme.LabelSpec labelSpec, final ThemeLabelSource... themeLabelSources) {
        final int labelHeight = this.getInfoLabelHeight(fontRenderer, labelSpec);
        int areaHeight = 0;
        for (final ThemeLabelSource themeLabelSource : themeLabelSources) {
            areaHeight += (themeLabelSource.isShown() ? labelHeight : 0);
        }
        return areaHeight;
    }
    
    private int getInfoLabelHeight(final FontRenderer fontRenderer, final Theme.LabelSpec labelSpec) {
        return (int)((DrawUtil.getLabelHeight(fontRenderer, labelSpec.shadow) + labelSpec.margin) * this.fontScale);
    }
    
    private void positionLabels(final FontRenderer fontRenderer, final int centerX, final int startY, final Theme.LabelSpec labelSpec, final ThemeLabelSource... themeLabelSources) {
        final int labelHeight = this.getInfoLabelHeight(fontRenderer, labelSpec);
        int labelY = startY;
        for (final ThemeLabelSource themeLabelSource : themeLabelSources) {
            if (themeLabelSource.isShown()) {
                final LabelVars labelVars = new LabelVars(this, centerX, labelY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, this.fontScale, labelSpec);
                final Tuple<LabelVars, ThemeLabelSource> tuple = (Tuple<LabelVars, ThemeLabelSource>)new Tuple((Object)labelVars, (Object)themeLabelSource);
                this.labels.add(tuple);
                labelY += labelHeight;
            }
        }
    }
    
    public void drawInfoLabels(final long currentTimeMillis) {
        for (final Tuple<LabelVars, ThemeLabelSource> label : this.labels) {
            ((LabelVars)label.func_76341_a()).draw(((ThemeLabelSource)label.func_76340_b()).getLabelText(currentTimeMillis));
        }
    }
    
    MapPresetStatus getMapPresetStatus(final MapType mapType, final int miniMapId) {
        if (this.mapPresetStatus == null || !mapType.equals(this.mapPresetStatus.mapType) || miniMapId != this.mapPresetStatus.miniMapId) {
            this.mapPresetStatus = new MapPresetStatus(mapType, miniMapId);
        }
        return this.mapPresetStatus;
    }
    
    MapTypeStatus getMapTypeStatus(final MapType mapType) {
        if (this.mapTypeStatus == null || !mapType.equals(this.mapTypeStatus.mapType)) {
            this.mapTypeStatus = new MapTypeStatus(mapType);
        }
        return this.mapTypeStatus;
    }
    
    class MapPresetStatus
    {
        private int miniMapId;
        private int scale;
        private MapType mapType;
        private String name;
        private Integer color;
        
        MapPresetStatus(final MapType mapType, final int miniMapId) {
            this.scale = 4;
            this.miniMapId = miniMapId;
            this.mapType = mapType;
            this.color = 16777215;
            this.name = Integer.toString(miniMapId);
        }
        
        void draw(final Point2D.Double mapCenter, final float alpha, final double rotation) {
            DrawUtil.drawLabel(this.name, mapCenter.getX(), mapCenter.getY() + 8.0, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.0f, this.color, alpha, this.scale, true, rotation);
        }
    }
    
    class MapTypeStatus
    {
        private MapType mapType;
        private String name;
        private TextureImpl tex;
        private Integer color;
        private Integer opposite;
        private double x;
        private double y;
        private float bgScale;
        private float scaleHeightOffset;
        
        MapTypeStatus(final MapType mapType) {
            this.mapType = mapType;
            this.name = (mapType.isUnderground() ? "caves" : mapType.name());
            this.tex = TextureCache.getThemeTexture(DisplayVars.this.theme, String.format("icon/%s.png", this.name));
            this.color = 16777215;
            this.opposite = 4210752;
            this.bgScale = 1.15f;
            this.scaleHeightOffset = (this.tex.getHeight() * this.bgScale - this.tex.getHeight()) / 2.0f;
        }
        
        void draw(final Point2D.Double mapCenter, final float alpha, final double rotation) {
            this.x = mapCenter.getX() - this.tex.getWidth() / 2;
            this.y = mapCenter.getY() - this.tex.getHeight() - 8.0;
            DrawUtil.drawColoredImage(this.tex, this.opposite, alpha, mapCenter.getX() - this.tex.getWidth() * this.bgScale / 2.0f, mapCenter.getY() - this.tex.getHeight() * this.bgScale + this.scaleHeightOffset - 8.0, this.bgScale, rotation);
            DrawUtil.drawColoredImage(this.tex, this.color, alpha, this.x, this.y, 1.0f, 0.0);
        }
    }
}
