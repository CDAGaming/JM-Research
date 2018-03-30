package journeymap.client.render.draw;

import journeymap.client.api.display.*;
import journeymap.client.render.map.*;
import java.util.concurrent.*;
import net.minecraft.util.*;
import journeymap.client.render.texture.*;
import journeymap.common.*;
import java.awt.geom.*;
import journeymap.client.api.model.*;

public class DrawImageStep extends BaseOverlayDrawStep<ImageOverlay>
{
    private Point2D.Double northWestPosition;
    private Point2D.Double southEastPosition;
    private volatile Future<TextureImpl> iconFuture;
    private TextureImpl iconTexture;
    private boolean hasError;
    
    public DrawImageStep(final ImageOverlay marker) {
        super(marker);
    }
    
    @Override
    public void draw(final DrawStep.Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
        if (!this.isOnScreen(xOffset, yOffset, gridRenderer, rotation)) {
            return;
        }
        if (pass == DrawStep.Pass.Object) {
            this.ensureTexture();
            if (!this.hasError && this.iconTexture != null) {
                final MapImage icon = ((ImageOverlay)this.overlay).getImage();
                final double width = this.screenBounds.width;
                final double height = this.screenBounds.height;
                DrawUtil.drawColoredSprite(this.iconTexture, width, height, icon.getTextureX(), icon.getTextureY(), icon.getDisplayWidth(), icon.getDisplayHeight(), icon.getColor(), icon.getOpacity(), this.northWestPosition.x + xOffset, this.northWestPosition.y + yOffset, 1.0f, icon.getRotation());
            }
        }
        else {
            super.drawText(pass, xOffset, yOffset, gridRenderer, fontScale, rotation);
        }
    }
    
    protected void ensureTexture() {
        if (this.iconTexture != null) {
            return;
        }
        try {
            if (this.iconFuture == null || this.iconFuture.isCancelled()) {
                this.iconFuture = TextureCache.scheduleTextureTask((Callable<TextureImpl>)new Callable<TextureImpl>() {
                    @Override
                    public TextureImpl call() throws Exception {
                        final MapImage image = ((ImageOverlay)DrawImageStep.this.overlay).getImage();
                        ResourceLocation resourceLocation = image.getImageLocation();
                        if (resourceLocation == null) {
                            resourceLocation = new ResourceLocation("fake:" + ((ImageOverlay)DrawImageStep.this.overlay).getGuid());
                            final TextureImpl texture = TextureCache.getTexture(resourceLocation);
                            texture.setImage(image.getImage(), true);
                            return texture;
                        }
                        return TextureCache.getTexture(resourceLocation);
                    }
                });
            }
            else if (this.iconFuture.isDone()) {
                this.iconTexture = this.iconFuture.get();
                if (this.iconTexture.isBindNeeded()) {
                    this.iconTexture.bindTexture();
                }
                this.iconFuture = null;
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error getting ImageOverlay marimage upperTexture: " + e, (Throwable)e);
            this.hasError = true;
        }
    }
    
    @Override
    protected void updatePositions(final GridRenderer gridRenderer, final double rotation) {
        this.northWestPosition = gridRenderer.getBlockPixelInGrid(((ImageOverlay)this.overlay).getNorthWestPoint());
        this.southEastPosition = gridRenderer.getBlockPixelInGrid(((ImageOverlay)this.overlay).getSouthEastPoint());
        (this.screenBounds = new Rectangle2D.Double(this.northWestPosition.x, this.northWestPosition.y, 0.0, 0.0)).add(this.southEastPosition);
        final TextProperties textProperties = ((ImageOverlay)this.overlay).getTextProperties();
        this.labelPosition.setLocation(this.screenBounds.getCenterX() + textProperties.getOffsetX(), this.screenBounds.getCenterY() + textProperties.getOffsetY());
    }
}
