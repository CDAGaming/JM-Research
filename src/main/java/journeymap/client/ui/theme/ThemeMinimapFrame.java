package journeymap.client.ui.theme;

import journeymap.client.ui.minimap.*;
import java.awt.geom.*;
import journeymap.client.properties.*;
import journeymap.client.render.texture.*;
import journeymap.client.render.draw.*;

public class ThemeMinimapFrame
{
    private final Theme theme;
    private final Theme.Minimap.MinimapSpec minimapSpec;
    private final ReticleOrientation reticleOrientation;
    private final String resourcePattern;
    private TextureImpl textureTopLeft;
    private TextureImpl textureTop;
    private TextureImpl textureTopRight;
    private TextureImpl textureRight;
    private TextureImpl textureBottomRight;
    private TextureImpl textureBottom;
    private TextureImpl textureBottomLeft;
    private TextureImpl textureLeft;
    private double[] coordsTopLeft;
    private double[] coordsTop;
    private double[] coordsTopRight;
    private double[] coordsRight;
    private double[] coordsBottomRight;
    private double[] coordsBottom;
    private double[] coordsBottomLeft;
    private double[] coordsLeft;
    private TextureImpl textureCircle;
    private TextureImpl textureCircleMask;
    private TextureImpl textureCompassPoint;
    private final double ttlw;
    private final double tth;
    private final double ttl;
    private final double ttlh;
    private final double tblw;
    private final double tbh;
    private final double trw;
    private final double ttrh;
    private final double ttrw;
    private final double tblh;
    private final double tbrw;
    private final double tbrh;
    private double x;
    private double y;
    private int width;
    private int height;
    private boolean isSquare;
    private boolean showReticle;
    private int reticleOffsetOuter;
    private int reticleOffsetInner;
    private double reticleThickness;
    private double reticleHeadingThickness;
    private Rectangle2D.Double frameBounds;
    private double[] retNorth;
    private double[] retSouth;
    private double[] retEast;
    private double[] retWest;
    private final float frameAlpha;
    
    public ThemeMinimapFrame(final Theme theme, final Theme.Minimap.MinimapSpec minimapSpec, final MiniMapProperties miniMapProperties, final int width, final int height) {
        this.retNorth = null;
        this.retSouth = null;
        this.retEast = null;
        this.retWest = null;
        this.theme = theme;
        this.minimapSpec = minimapSpec;
        this.width = width;
        this.height = height;
        this.reticleOrientation = miniMapProperties.reticleOrientation.get();
        if (minimapSpec instanceof Theme.Minimap.MinimapSquare) {
            this.isSquare = true;
            final Theme.Minimap.MinimapSquare minimapSquare = (Theme.Minimap.MinimapSquare)minimapSpec;
            this.resourcePattern = "minimap/square/" + minimapSquare.prefix + "%s.png";
            this.textureTopLeft = this.getTexture("topleft", minimapSquare.topLeft);
            this.textureTop = this.getTexture("top", width - minimapSquare.topLeft.width / 2 - minimapSquare.topRight.width / 2, minimapSquare.top.height, true, false);
            this.textureTopRight = this.getTexture("topright", minimapSquare.topRight);
            this.textureRight = this.getTexture("right", minimapSquare.right.width, height - minimapSquare.topRight.height / 2 - minimapSquare.bottomRight.height / 2, true, false);
            this.textureBottomRight = this.getTexture("bottomright", minimapSquare.bottomRight);
            this.textureBottom = this.getTexture("bottom", width - minimapSquare.bottomLeft.width / 2 - minimapSquare.bottomRight.width / 2, minimapSquare.bottom.height, true, false);
            this.textureBottomLeft = this.getTexture("bottomleft", minimapSquare.bottomLeft);
            this.textureLeft = this.getTexture("left", minimapSquare.left.width, height - minimapSquare.topLeft.height / 2 - minimapSquare.bottomLeft.height / 2, true, false);
            this.ttlw = this.textureTopLeft.getWidth() / 2.0;
            this.tth = this.textureTop.getHeight() / 2.0;
            this.ttl = this.textureLeft.getWidth() / 2.0;
            this.ttlh = this.textureTopLeft.getHeight() / 2.0;
            this.tblw = this.textureBottomLeft.getWidth() / 2.0;
            this.tbh = this.textureBottom.getHeight() / 2.0;
            this.trw = this.textureRight.getWidth() / 2.0;
            this.ttrh = this.textureTopRight.getHeight() / 2.0;
            this.ttrw = this.textureTopRight.getWidth() / 2.0;
            this.tblh = this.textureBottomLeft.getHeight() / 2.0;
            this.tbrw = this.textureBottomRight.getWidth() / 2.0;
            this.tbrh = this.textureBottomRight.getHeight() / 2.0;
        }
        else {
            final Theme.Minimap.MinimapCircle minimapCircle = (Theme.Minimap.MinimapCircle)minimapSpec;
            final int imgSize = (width <= 256) ? 256 : 512;
            this.resourcePattern = "minimap/circle/" + minimapCircle.prefix + "%s.png";
            final TextureImpl tempMask = this.getTexture("mask_" + imgSize, imgSize, imgSize, false, true);
            this.textureCircleMask = TextureCache.getScaledCopy("scaledCircleMask", tempMask, width, height, 1.0f);
            final TextureImpl tempCircle = this.getTexture("rim_" + imgSize, imgSize, imgSize, false, true);
            this.textureCircle = TextureCache.getScaledCopy("scaledCircleRim", tempCircle, width, height, minimapSpec.frame.alpha);
            this.ttlw = 0.0;
            this.tth = 0.0;
            this.ttl = 0.0;
            this.ttlh = 0.0;
            this.tblw = 0.0;
            this.tbh = 0.0;
            this.trw = 0.0;
            this.ttrh = 0.0;
            this.ttrw = 0.0;
            this.tblh = 0.0;
            this.tbrw = 0.0;
            this.tbrh = 0.0;
        }
        if (minimapSpec.compassPoint != null && minimapSpec.compassPoint.width > 0 && minimapSpec.compassPoint.height > 0) {
            this.textureCompassPoint = this.getTexture("compass_point", minimapSpec.compassPoint);
        }
        this.reticleThickness = minimapSpec.reticleThickness;
        this.reticleHeadingThickness = minimapSpec.reticleHeadingThickness;
        this.reticleOffsetOuter = minimapSpec.reticleOffsetOuter;
        this.reticleOffsetInner = minimapSpec.reticleOffsetInner;
        this.showReticle = (miniMapProperties.showReticle.get() && (minimapSpec.reticle.alpha > 0.0f || minimapSpec.reticleHeading.alpha > 0.0f));
        this.frameAlpha = Math.max(0.0f, Math.min(1.0f, miniMapProperties.frameAlpha.get() / 100.0f));
    }
    
    public void setPosition(final double x, final double y) {
        this.x = x;
        this.y = y;
        this.frameBounds = new Rectangle2D.Double(x, y, this.width, this.height);
        final double centerX = x + this.width / 2;
        final double centerY = y + this.height / 2;
        final double segLengthNorthSouth = centerY - this.reticleOffsetInner - y - this.reticleOffsetOuter;
        final double segLengthEastWest = centerX - this.reticleOffsetInner - x - this.reticleOffsetOuter;
        double thick;
        Theme.ColorSpec colorSpec;
        if (this.reticleOrientation == ReticleOrientation.Compass) {
            thick = this.reticleHeadingThickness;
            colorSpec = this.minimapSpec.reticleHeading;
        }
        else {
            thick = this.reticleThickness;
            colorSpec = this.minimapSpec.reticle;
        }
        this.retNorth = null;
        if (thick > 0.0 && colorSpec.alpha > 0.0f) {
            (this.retNorth = new double[6])[0] = centerX - thick / 2.0;
            this.retNorth[1] = y + this.reticleOffsetOuter;
            this.retNorth[2] = thick;
            this.retNorth[3] = segLengthNorthSouth;
            this.retNorth[4] = colorSpec.getColor();
            this.retNorth[5] = colorSpec.alpha;
        }
        if (this.reticleOrientation == ReticleOrientation.PlayerHeading) {
            thick = this.reticleHeadingThickness;
            colorSpec = this.minimapSpec.reticleHeading;
        }
        else {
            thick = this.reticleThickness;
            colorSpec = this.minimapSpec.reticle;
        }
        this.retSouth = null;
        if (thick > 0.0 && colorSpec.alpha > 0.0f) {
            (this.retSouth = new double[6])[0] = centerX - thick / 2.0;
            this.retSouth[1] = centerY + this.reticleOffsetInner;
            this.retSouth[2] = thick;
            this.retSouth[3] = segLengthNorthSouth;
            this.retSouth[4] = colorSpec.getColor();
            this.retSouth[5] = colorSpec.alpha;
        }
        thick = this.reticleThickness;
        colorSpec = this.minimapSpec.reticle;
        this.retWest = null;
        if (thick > 0.0 && colorSpec.alpha > 0.0f) {
            (this.retWest = new double[6])[0] = centerX - this.reticleOffsetInner - segLengthEastWest;
            this.retWest[1] = centerY - thick / 2.0;
            this.retWest[2] = segLengthEastWest;
            this.retWest[3] = this.reticleThickness;
            this.retWest[4] = colorSpec.getColor();
            this.retWest[5] = colorSpec.alpha;
        }
        this.retEast = null;
        if (thick > 0.0 && colorSpec.alpha > 0.0f) {
            (this.retEast = new double[6])[0] = centerX + this.reticleOffsetInner;
            this.retEast[1] = centerY - thick / 2.0;
            this.retEast[2] = segLengthEastWest;
            this.retEast[3] = this.reticleThickness;
            this.retEast[4] = colorSpec.getColor();
            this.retEast[5] = colorSpec.alpha;
        }
        if (this.isSquare) {
            final int frameColor = this.minimapSpec.frame.getColor();
            final float alpha = this.minimapSpec.frame.alpha * this.frameAlpha;
            this.coordsTopLeft = new double[] { frameColor, alpha, x - this.ttlw, y - this.ttlh, 1.0, 0.0 };
            this.coordsTop = new double[] { frameColor, alpha, x + this.ttlw, y - this.tth, 1.0, 0.0 };
            this.coordsTopRight = new double[] { frameColor, alpha, x + this.width - this.ttrw, y - this.ttrh, 1.0, 0.0 };
            this.coordsRight = new double[] { frameColor, alpha, x + this.width - this.trw, y + this.ttrh, 1.0, 0.0 };
            this.coordsBottomRight = new double[] { frameColor, alpha, x + this.width - this.tbrw, y + this.height - this.tbrh, 1.0, 0.0 };
            this.coordsBottom = new double[] { frameColor, alpha, x + this.tblw, y + this.height - this.tbh, 1.0, 0.0 };
            this.coordsBottomLeft = new double[] { frameColor, alpha, x - this.tblw, y + this.height - this.tblh, 1.0, 0.0 };
            this.coordsLeft = new double[] { frameColor, alpha, x - this.ttl, y + this.ttlh, 1.0, 0.0 };
        }
    }
    
    public void drawMask() {
        if (this.isSquare) {
            DrawUtil.drawRectangle(this.x, this.y, this.width, this.height, 16777215, 1.0f);
        }
        else {
            DrawUtil.drawQuad(this.textureCircleMask, 16777215, 1.0f, this.x, this.y, this.width, this.height, 0.0, 0.0, 1.0, 1.0, 0.0, false, true, 770, 771, true);
        }
    }
    
    public void drawReticle() {
        if (this.showReticle) {
            if (this.retNorth != null) {
                DrawUtil.drawRectangle(this.retNorth[0], this.retNorth[1], this.retNorth[2], this.retNorth[3], (int)this.retNorth[4], (float)this.retNorth[5]);
            }
            if (this.retSouth != null) {
                DrawUtil.drawRectangle(this.retSouth[0], this.retSouth[1], this.retSouth[2], this.retSouth[3], (int)this.retSouth[4], (float)this.retSouth[5]);
            }
            if (this.retWest != null) {
                DrawUtil.drawRectangle(this.retWest[0], this.retWest[1], this.retWest[2], this.retWest[3], (int)this.retWest[4], (float)this.retWest[5]);
            }
            if (this.retEast != null) {
                DrawUtil.drawRectangle(this.retEast[0], this.retEast[1], this.retEast[2], this.retEast[3], (int)this.retEast[4], (float)this.retEast[5]);
            }
        }
    }
    
    public void drawFrame() {
        if (this.minimapSpec.frame.alpha > 0.0f) {
            if (this.isSquare) {
                DrawUtil.drawClampedImage(this.textureTopLeft, (int)this.coordsTopLeft[0], (float)this.coordsTopLeft[1], this.coordsTopLeft[2], this.coordsTopLeft[3], (float)this.coordsTopLeft[4], this.coordsTopLeft[5]);
                DrawUtil.drawClampedImage(this.textureTop, (int)this.coordsTop[0], (float)this.coordsTop[1], this.coordsTop[2], this.coordsTop[3], (float)this.coordsTop[4], this.coordsTop[5]);
                DrawUtil.drawClampedImage(this.textureTopRight, (int)this.coordsTopRight[0], (float)this.coordsTopRight[1], this.coordsTopRight[2], this.coordsTopRight[3], (float)this.coordsTopRight[4], this.coordsTopRight[5]);
                DrawUtil.drawClampedImage(this.textureRight, (int)this.coordsRight[0], (float)this.coordsRight[1], this.coordsRight[2], this.coordsRight[3], (float)this.coordsRight[4], this.coordsRight[5]);
                DrawUtil.drawClampedImage(this.textureBottomRight, (int)this.coordsBottomRight[0], (float)this.coordsBottomRight[1], this.coordsBottomRight[2], this.coordsBottomRight[3], (float)this.coordsBottomRight[4], this.coordsBottomRight[5]);
                DrawUtil.drawClampedImage(this.textureBottom, (int)this.coordsBottom[0], (float)this.coordsBottom[1], this.coordsBottom[2], this.coordsBottom[3], (float)this.coordsBottom[4], this.coordsBottom[5]);
                DrawUtil.drawClampedImage(this.textureBottomLeft, (int)this.coordsBottomLeft[0], (float)this.coordsBottomLeft[1], this.coordsBottomLeft[2], this.coordsBottomLeft[3], (float)this.coordsBottomLeft[4], this.coordsBottomLeft[5]);
                DrawUtil.drawClampedImage(this.textureLeft, (int)this.coordsLeft[0], (float)this.coordsLeft[1], this.coordsLeft[2], this.coordsLeft[3], (float)this.coordsLeft[4], this.coordsLeft[5]);
            }
            else {
                final float alpha = this.minimapSpec.frame.alpha * this.frameAlpha;
                DrawUtil.drawQuad(this.textureCircle, this.minimapSpec.frame.getColor(), alpha, this.x, this.y, this.width, this.height, 0.0, 0.0, 1.0, 1.0, 0.0, false, true, 770, 771, true);
            }
        }
    }
    
    public TextureImpl getCompassPoint() {
        return this.textureCompassPoint;
    }
    
    private TextureImpl getTexture(final String suffix, final Theme.ImageSpec imageSpec) {
        return this.getTexture(suffix, imageSpec.width, imageSpec.height, true, false);
    }
    
    private TextureImpl getTexture(final String suffix, final int width, final int height, final boolean resize, final boolean retain) {
        return TextureCache.getSizedThemeTexture(this.theme, String.format(this.resourcePattern, suffix), width, height, resize, 1.0f, retain);
    }
    
    public Rectangle2D.Double getFrameBounds() {
        return this.frameBounds;
    }
    
    public double getX() {
        return this.x;
    }
    
    public void setX(final double x) {
        this.x = x;
    }
    
    public double getY() {
        return this.y;
    }
    
    public void setY(final double y) {
        this.y = y;
    }
    
    public double getWidth() {
        return this.width;
    }
    
    public double getHeight() {
        return this.height;
    }
    
    public ReticleOrientation getReticleOrientation() {
        return this.reticleOrientation;
    }
}
