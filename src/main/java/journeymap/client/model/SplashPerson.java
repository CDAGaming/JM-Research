package journeymap.client.model;

import journeymap.client.ui.component.*;
import journeymap.client.*;
import journeymap.client.render.texture.*;
import net.minecraft.client.gui.*;
import java.awt.geom.*;
import java.util.*;

public class SplashPerson
{
    public final String name;
    public final String ign;
    public final String title;
    public Button button;
    public int width;
    public double moveX;
    public double moveY;
    private double moveDistance;
    private Random r;
    
    public SplashPerson(final String ign, final String name, final String titleKey) {
        this.moveDistance = 1.0;
        this.r = new Random();
        this.ign = ign;
        this.name = name;
        if (titleKey != null) {
            this.title = Constants.getString(titleKey);
        }
        else {
            this.title = "";
        }
    }
    
    public Button getButton() {
        return this.button;
    }
    
    public void setButton(final Button button) {
        this.button = button;
        this.randomizeVector();
    }
    
    public TextureImpl getSkin() {
        return TextureCache.getPlayerSkin(this.ign);
    }
    
    public int getWidth(final FontRenderer fr) {
        this.width = fr.func_78256_a(this.title);
        final String[] split;
        final String[] nameParts = split = this.name.trim().split(" ");
        for (final String part : split) {
            this.width = Math.max(this.width, fr.func_78256_a(part));
        }
        return this.width;
    }
    
    public void setWidth(final int minWidth) {
        this.width = minWidth;
    }
    
    public void randomizeVector() {
        this.moveDistance = this.r.nextDouble() + 0.5;
        this.moveX = (this.r.nextBoolean() ? this.moveDistance : (-this.moveDistance));
        this.moveDistance = this.r.nextDouble() + 0.5;
        this.moveY = (this.r.nextBoolean() ? this.moveDistance : (-this.moveDistance));
    }
    
    public void adjustVector(final Rectangle2D.Double screenBounds) {
        final Rectangle2D.Double buttonBounds = this.button.getBounds();
        if (!screenBounds.contains(buttonBounds)) {
            final int xMargin = this.button.getWidth();
            final int yMargin = this.button.getHeight();
            if (buttonBounds.getMinX() <= xMargin) {
                this.moveX = this.moveDistance;
            }
            else if (buttonBounds.getMaxX() >= screenBounds.getWidth() - xMargin) {
                this.moveX = -this.moveDistance;
            }
            if (buttonBounds.getMinY() <= yMargin) {
                this.moveY = this.moveDistance;
            }
            else if (buttonBounds.getMaxY() >= screenBounds.getHeight() - yMargin) {
                this.moveY = -this.moveDistance;
            }
        }
        this.continueVector();
    }
    
    public void continueVector() {
        this.button.setX((int)Math.round(this.button.field_146128_h + this.moveX));
        this.button.setY((int)Math.round(this.button.field_146129_i + this.moveY));
    }
    
    public void avoid(final List<SplashPerson> others) {
        for (final SplashPerson other : others) {
            if (this == other) {
                continue;
            }
            if (this.getDistance(other) <= this.button.getWidth()) {
                this.randomizeVector();
                break;
            }
        }
    }
    
    public double getDistance(final SplashPerson other) {
        final double px = this.button.getCenterX() - other.button.getCenterX();
        final double py = this.button.getMiddleY() - other.button.getMiddleY();
        return Math.sqrt(px * px + py * py);
    }
    
    public static class Fake extends SplashPerson
    {
        private TextureImpl texture;
        
        public Fake(final String name, final String title, final TextureImpl texture) {
            super(name, title, null);
            this.texture = texture;
        }
        
        @Override
        public TextureImpl getSkin() {
            return this.texture;
        }
    }
}
