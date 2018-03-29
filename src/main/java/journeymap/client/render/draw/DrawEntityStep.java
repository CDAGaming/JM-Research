package journeymap.client.render.draw;

import com.google.common.cache.CacheLoader;
import journeymap.client.data.DataCache;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.minimap.EntityDisplay;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;

import java.awt.geom.Point2D;
import java.lang.ref.WeakReference;

public class DrawEntityStep implements DrawStep {
    static final Integer labelBg;
    static final int labelBgAlpha = 180;
    static final Integer labelFg;
    static final int labelFgAlpha = 225;

    static {
        labelBg = 0;
        labelFg = 16777215;
    }

    boolean useDots;
    int elevationOffset;
    int color;
    boolean hideSneaks;
    boolean showHeading;
    boolean showName;
    Minecraft minecraft;
    TextureImpl entityTexture;
    TextureImpl locatorTexture;
    WeakReference<EntityLivingBase> entityLivingRef;
    String customName;
    String playerTeamName;
    Point2D screenPosition;
    float drawScale;

    private DrawEntityStep(final EntityLivingBase entityLiving) {
        this.showHeading = true;
        this.showName = true;
        this.minecraft = Minecraft.getMinecraft();
        this.drawScale = 1.0f;
        this.entityLivingRef = new WeakReference<>(entityLiving);
        this.hideSneaks = Journeymap.getClient().getCoreProperties().hideSneakingEntities.get();
    }

    public void update(final EntityDisplay entityDisplay, final TextureImpl locatorTexture, final TextureImpl entityTexture, final int color, final boolean showHeading, final boolean showName) {
        final EntityLivingBase entityLiving = this.entityLivingRef.get();
        if (showName && entityLiving != null) {
            this.customName = DataCache.INSTANCE.getEntityDTO(entityLiving).customName;
        }
        this.useDots = entityDisplay.isDots();
        this.color = color;
        this.locatorTexture = locatorTexture;
        this.entityTexture = entityTexture;
        this.drawScale = ((entityDisplay == EntityDisplay.SmallIcons) ? 0.6666667f : 1.0f);
        this.showHeading = showHeading;
        this.showName = showName;
        if (entityLiving instanceof EntityPlayer) {
            final Team team = entityLiving.getTeam();
            if (team != null) {
                this.playerTeamName = ScorePlayerTeam.formatPlayerName(entityLiving.getTeam(), entityLiving.getName());
            } else {
                this.playerTeamName = null;
            }
        }
    }

    @Override
    public void draw(final Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
        if (pass == Pass.Tooltip) {
            return;
        }
        final EntityLivingBase entityLiving = this.entityLivingRef.get();
        if (pass == Pass.Object) {
            if (entityLiving == null || entityLiving.isDead || entityLiving.isInvisibleToPlayer(this.minecraft.player) || !entityLiving.addedToChunk || (this.hideSneaks && entityLiving.isSneaking())) {
                this.screenPosition = null;
                return;
            }
            this.screenPosition = gridRenderer.getPixel(entityLiving.posX, entityLiving.posZ);
        }
        if (this.screenPosition != null) {
            final double heading = entityLiving.rotationYawHead;
            final double drawX = this.screenPosition.getX() + xOffset;
            final double drawY = this.screenPosition.getY() + yOffset;
            float alpha = 1.0f;
            if (entityLiving.posY > this.minecraft.player.posY) {
                alpha = 1.0f - Math.max(0.1f, (float) ((entityLiving.posY - this.minecraft.player.posY) / 32.0));
            }
            if (entityLiving instanceof EntityPlayer) {
                this.drawPlayer(pass, drawX, drawY, gridRenderer, alpha, heading, fontScale, rotation);
            } else {
                this.drawCreature(pass, drawX, drawY, gridRenderer, alpha, heading, fontScale, rotation);
            }
        }
    }

    private void drawPlayer(final Pass pass, final double drawX, final double drawY, final GridRenderer gridRenderer, final float alpha, final double heading, final double fontScale, final double rotation) {
        final EntityLivingBase entityLiving = this.entityLivingRef.get();
        if (entityLiving == null) {
            return;
        }
        if (pass == Pass.Object) {
            if (this.locatorTexture != null) {
                DrawUtil.drawColoredEntity(drawX, drawY, this.locatorTexture, this.color, alpha, this.drawScale, this.showHeading ? heading : (-rotation));
            }
            if (this.entityTexture != null) {
                if (this.useDots) {
                    boolean flip = false;
                    this.elevationOffset = (int) (DataCache.getPlayer().posY - entityLiving.posY);
                    if (this.elevationOffset < -1 || this.elevationOffset > 1) {
                        flip = (this.elevationOffset < -1);
                        DrawUtil.drawColoredEntity(drawX, drawY, this.entityTexture, this.color, alpha, this.drawScale, flip ? (-rotation + 180.0) : (-rotation));
                    }
                } else {
                    DrawUtil.drawColoredEntity(drawX, drawY, this.entityTexture, this.color, alpha, this.drawScale, -rotation);
                }
            }
        }
        if (pass == Pass.Text) {
            final int labelOffset = (this.entityTexture == null) ? 0 : ((rotation == 0.0) ? (-this.entityTexture.getHeight() / 2) : (this.entityTexture.getHeight() / 2));
            final Point2D labelPoint = gridRenderer.shiftWindowPosition((int) drawX, (int) drawY, 0, -labelOffset);
            if (this.playerTeamName != null) {
                DrawUtil.drawLabel(this.playerTeamName, labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.8f, 16777215, 1.0f, fontScale, false, rotation);
            } else {
                DrawUtil.drawLabel(entityLiving.getName(), labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.8f, 65280, 1.0f, fontScale, false, rotation);
            }
        }
    }

    private void drawCreature(final Pass pass, final double drawX, final double drawY, final GridRenderer gridRenderer, final float alpha, final double heading, final double fontScale, final double rotation) {
        final EntityLivingBase entityLiving = this.entityLivingRef.get();
        if (entityLiving == null) {
            return;
        }
        if (pass == Pass.Object && this.locatorTexture != null) {
            DrawUtil.drawColoredEntity(drawX, drawY, this.locatorTexture, this.color, alpha, this.drawScale, this.showHeading ? heading : (-rotation));
        }
        final int labelOffset = (this.entityTexture == null) ? 8 : ((rotation == 0.0) ? this.entityTexture.getHeight() : (-this.entityTexture.getHeight()));
        if (pass == Pass.Text && this.showName && this.customName != null) {
            final Point2D labelPoint = gridRenderer.shiftWindowPosition(drawX, drawY, 0, labelOffset);
            DrawUtil.drawCenteredLabel(this.customName, labelPoint.getX(), labelPoint.getY(), DrawEntityStep.labelBg, 180.0f, 16777215, 225.0f, fontScale, rotation);
        }
        if (pass == Pass.Object && this.entityTexture != null) {
            if (this.useDots) {
                boolean flip = false;
                this.elevationOffset = (int) (DataCache.getPlayer().posY - entityLiving.posY);
                if (this.elevationOffset < -1 || this.elevationOffset > 1) {
                    flip = (this.elevationOffset < -1);
                    DrawUtil.drawColoredEntity(drawX, drawY, this.entityTexture, this.color, alpha, this.drawScale, flip ? (-rotation + 180.0) : (-rotation));
                }
            } else {
                DrawUtil.drawEntity(drawX, drawY, -rotation, this.entityTexture, alpha, this.drawScale, 0.0);
            }
        }
    }

    @Override
    public int getDisplayOrder() {
        return (this.customName != null) ? 1 : 0;
    }

    @Override
    public String getModId() {
        return "journeymap";
    }

    public static class SimpleCacheLoader extends CacheLoader<EntityLivingBase, DrawEntityStep> {
        public DrawEntityStep load(final EntityLivingBase entityLiving) throws Exception {
            return new DrawEntityStep(entityLiving);
        }
    }
}
