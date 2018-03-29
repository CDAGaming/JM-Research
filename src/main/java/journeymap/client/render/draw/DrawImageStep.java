package journeymap.client.render.draw;

import journeymap.client.api.display.ImageOverlay;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.model.TextProperties;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.common.Journeymap;
import net.minecraft.util.ResourceLocation;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class DrawImageStep extends BaseOverlayDrawStep<ImageOverlay> {
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
                final MapImage icon = this.overlay.getImage();
                final double width = this.screenBounds.width;
                final double height = this.screenBounds.height;
                DrawUtil.drawColoredSprite(this.iconTexture, width, height, 0.0, 0.0, icon.getDisplayWidth(), icon.getDisplayHeight(), icon.getColor(), icon.getOpacity(), this.northWestPosition.x + xOffset, this.northWestPosition.y + yOffset, 1.0f, icon.getRotation());
            }
        } else {
            super.drawText(pass, xOffset, yOffset, gridRenderer, fontScale, rotation);
        }
    }

    protected void ensureTexture() {
        if (this.iconTexture != null) {
            return;
        }
        try {
            if (this.iconFuture == null || this.iconFuture.isCancelled()) {
                this.iconFuture = TextureCache.scheduleTextureTask(() -> {
                    final MapImage image = DrawImageStep.this.overlay.getImage();
                    ResourceLocation resourceLocation = image.getImageLocation();
                    if (resourceLocation == null) {
                        resourceLocation = new ResourceLocation("fake:" + DrawImageStep.this.overlay.getGuid());
                        final TextureImpl texture = TextureCache.getTexture(resourceLocation);
                        texture.setImage(image.getImage(), true);
                        return texture;
                    }
                    return TextureCache.getTexture(resourceLocation);
                });
            } else if (this.iconFuture.isDone()) {
                this.iconTexture = this.iconFuture.get();
                if (this.iconTexture.isBindNeeded()) {
                    this.iconTexture.bindTexture();
                }
                this.iconFuture = null;
            }
        } catch (Exception e) {
            Journeymap.getLogger().error("Error getting ImageOverlay marimage upperTexture: " + e, e);
            this.hasError = true;
        }
    }

    @Override
    protected void updatePositions(final GridRenderer gridRenderer, final double rotation) {
        this.northWestPosition = gridRenderer.getBlockPixelInGrid(this.overlay.getNorthWestPoint());
        this.southEastPosition = gridRenderer.getBlockPixelInGrid(this.overlay.getSouthEastPoint());
        (this.screenBounds = new Rectangle2D.Double(this.northWestPosition.x, this.northWestPosition.y, 0.0, 0.0)).add(this.southEastPosition);
        final TextProperties textProperties = this.overlay.getTextProperties();
        this.labelPosition.setLocation(this.screenBounds.getCenterX() + textProperties.getOffsetX(), this.screenBounds.getCenterY() + textProperties.getOffsetY());
    }
}
