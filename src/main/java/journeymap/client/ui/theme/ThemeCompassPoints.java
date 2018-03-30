package journeymap.client.ui.theme;

import java.awt.geom.*;
import journeymap.client.render.texture.*;
import journeymap.client.properties.*;
import journeymap.client.*;
import journeymap.client.render.draw.*;

public class ThemeCompassPoints
{
    final String textNorth;
    final String textSouth;
    final String textEast;
    final String textWest;
    final Point2D pointNorth;
    final Point2D pointSouth;
    final Point2D pointWest;
    final Point2D pointEast;
    final boolean showNorth;
    final boolean showSouth;
    final boolean showEast;
    final boolean showWest;
    final float fontScale;
    final int compassLabelHeight;
    final Theme.LabelSpec compassLabel;
    final Theme.ColorSpec compassPoint;
    final TextureImpl compassPointTex;
    final int xOffset;
    final int yOffset;
    final double shiftVert;
    final double shiftHorz;
    final int labelShiftVert;
    private double x;
    private double y;
    
    public ThemeCompassPoints(final int x, final int y, final int halfWidth, final int halfHeight, final Theme.Minimap.MinimapSpec minimapSpec, final MiniMapProperties miniMapProperties, final TextureImpl compassPointTex, final int labelHeight) {
        this.textNorth = Constants.getString("jm.minimap.compass.n");
        this.textSouth = Constants.getString("jm.minimap.compass.s");
        this.textEast = Constants.getString("jm.minimap.compass.e");
        this.textWest = Constants.getString("jm.minimap.compass.w");
        this.x = x;
        this.y = y;
        this.pointNorth = new Point2D.Double(x + halfWidth, y);
        this.pointSouth = new Point2D.Double(x + halfWidth, y + halfHeight + halfHeight);
        this.pointWest = new Point2D.Double(x, y + halfHeight);
        this.pointEast = new Point2D.Double(x + halfWidth + halfWidth, y + halfHeight);
        this.fontScale = miniMapProperties.compassFontScale.get();
        this.compassLabelHeight = labelHeight;
        this.compassLabel = minimapSpec.compassLabel;
        this.compassPoint = minimapSpec.compassPoint;
        this.compassPointTex = compassPointTex;
        if (this.compassPointTex != null) {
            this.shiftVert = minimapSpec.compassPointOffset * this.fontScale;
            this.shiftHorz = minimapSpec.compassPointOffset * this.fontScale;
            this.pointNorth.setLocation(this.pointNorth.getX(), this.pointNorth.getY() - this.shiftVert);
            this.pointSouth.setLocation(this.pointSouth.getX(), this.pointSouth.getY() + this.shiftVert);
            this.pointWest.setLocation(this.pointWest.getX() - this.shiftHorz, this.pointWest.getY());
            this.pointEast.setLocation(this.pointEast.getX() + this.shiftHorz, this.pointEast.getY());
            this.xOffset = (int)(compassPointTex.getWidth() * this.fontScale / 2.0f);
            this.yOffset = (int)(compassPointTex.getHeight() * this.fontScale / 2.0f);
        }
        else {
            this.xOffset = 0;
            this.yOffset = 0;
            this.shiftHorz = 0.0;
            this.shiftVert = 0.0;
        }
        this.labelShiftVert = 0;
        this.showNorth = minimapSpec.compassShowNorth;
        this.showSouth = minimapSpec.compassShowSouth;
        this.showEast = minimapSpec.compassShowEast;
        this.showWest = minimapSpec.compassShowWest;
    }
    
    public static float getCompassPointScale(final int compassLabelHeight, final Theme.Minimap.MinimapSpec minimapSpec, final TextureImpl compassPointTex) {
        return (compassLabelHeight + minimapSpec.compassPointLabelPad) / (compassPointTex.getHeight() * 1.0f);
    }
    
    public void setPosition(final double x, final double y) {
        this.x = x;
        this.y = y;
    }
    
    public void drawPoints(final double rotation) {
        if (this.compassPointTex != null) {
            final int color = this.compassPoint.getColor();
            final float alpha = this.compassPoint.alpha;
            if (this.showNorth) {
                DrawUtil.drawColoredImage(this.compassPointTex, color, alpha, this.pointNorth.getX() - this.xOffset, this.pointNorth.getY() - this.yOffset, this.fontScale, 0.0);
            }
            if (this.showSouth) {
                DrawUtil.drawColoredImage(this.compassPointTex, color, alpha, this.pointSouth.getX() - this.xOffset, this.pointSouth.getY() - this.yOffset, this.fontScale, 180.0);
            }
            if (this.showWest) {
                DrawUtil.drawColoredImage(this.compassPointTex, color, alpha, this.pointWest.getX() - this.xOffset, this.pointWest.getY() - this.yOffset, this.fontScale, -90.0);
            }
            if (this.showEast) {
                DrawUtil.drawColoredImage(this.compassPointTex, color, alpha, this.pointEast.getX() - this.xOffset, this.pointEast.getY() - this.yOffset, this.fontScale, 90.0);
            }
        }
    }
    
    public void drawLabels(final double rotation) {
        if (this.showNorth) {
            DrawUtil.drawLabel(this.textNorth, this.compassLabel, this.pointNorth.getX(), this.pointNorth.getY() + this.labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, this.fontScale, rotation);
        }
        if (this.showSouth) {
            DrawUtil.drawLabel(this.textSouth, this.compassLabel, this.pointSouth.getX(), this.pointSouth.getY() + this.labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, this.fontScale, rotation);
        }
        if (this.showWest) {
            DrawUtil.drawLabel(this.textWest, this.compassLabel, this.pointWest.getX(), this.pointWest.getY() + this.labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, this.fontScale, rotation);
        }
        if (this.showEast) {
            DrawUtil.drawLabel(this.textEast, this.compassLabel, this.pointEast.getX(), this.pointEast.getY() + this.labelShiftVert, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, this.fontScale, rotation);
        }
    }
}
