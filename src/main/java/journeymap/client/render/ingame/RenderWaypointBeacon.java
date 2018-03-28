package journeymap.client.render.ingame;

import journeymap.client.Constants;
import journeymap.client.cartography.color.RGB;
import journeymap.client.model.Waypoint;
import journeymap.client.properties.WaypointProperties;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import java.util.Collection;

public class RenderWaypointBeacon {
    static final ResourceLocation beam;
    static Minecraft mc;
    static RenderManager renderManager;
    static String distanceLabel;
    static WaypointProperties waypointProperties;

    static {
        beam = new ResourceLocation("textures/entity/beacon_beam.png");
        RenderWaypointBeacon.mc = FMLClientHandler.instance().getClient();
        RenderWaypointBeacon.renderManager = RenderWaypointBeacon.mc.getRenderManager();
        RenderWaypointBeacon.distanceLabel = Constants.getString("jm.waypoint.distance_meters", "%1.0f");
    }

    public static void resetStatTimers() {
    }

    public static void renderAll() {
        try {
            RenderWaypointBeacon.waypointProperties = Journeymap.getClient().getWaypointProperties();
            final Collection<Waypoint> waypoints = WaypointStore.INSTANCE.getAll();
            final int playerDim = RenderWaypointBeacon.mc.player.dimension;
            for (final Waypoint wp : waypoints) {
                if (wp.isEnable() && wp.getDimensions().contains(playerDim)) {
                    try {
                        doRender(wp);
                    } catch (Throwable t) {
                        Journeymap.getLogger().error("EntityWaypoint failed to render for " + wp + ": " + LogFormatter.toString(t));
                    }
                }
            }
        } catch (Throwable t2) {
            Journeymap.getLogger().error("Error rendering waypoints: " + LogFormatter.toString(t2));
        }
    }

    static void doRender(final Waypoint waypoint) {
        if (RenderWaypointBeacon.renderManager.renderViewEntity == null) {
            return;
        }
        RenderHelper.enableStandardItemLighting();
        try {
            final Vec3d playerVec = RenderWaypointBeacon.renderManager.renderViewEntity.getPositionVector();
            Vec3d waypointVec = waypoint.getPosition().addVector(0.0, 0.118, 0.0);
            final double actualDistance = playerVec.distanceTo(waypointVec);
            final int maxDistance = RenderWaypointBeacon.waypointProperties.maxDistance.get();
            if (maxDistance > 0 && actualDistance > maxDistance) {
                return;
            }
            float fadeAlpha = 1.0f;
            final int minDistance = RenderWaypointBeacon.waypointProperties.minDistance.get();
            if (minDistance > 0) {
                if ((int) actualDistance <= minDistance) {
                    return;
                }
                if ((int) actualDistance <= minDistance + 4) {
                    fadeAlpha = (float) (actualDistance - minDistance) / 3.0f;
                }
            }
            double viewDistance = actualDistance;
            final double maxRenderDistance = RenderWaypointBeacon.mc.gameSettings.renderDistanceChunks * 16;
            if (viewDistance > maxRenderDistance) {
                final Vec3d delta = waypointVec.subtract(playerVec).normalize();
                waypointVec = playerVec.addVector(delta.x * maxRenderDistance, delta.y * maxRenderDistance, delta.z * maxRenderDistance);
                viewDistance = maxRenderDistance;
            }
            final double shiftX = waypointVec.x - RenderWaypointBeacon.renderManager.viewerPosX;
            final double shiftY = waypointVec.y - RenderWaypointBeacon.renderManager.viewerPosY;
            final double shiftZ = waypointVec.z - RenderWaypointBeacon.renderManager.viewerPosZ;
            final boolean showStaticBeam = RenderWaypointBeacon.waypointProperties.showStaticBeam.get();
            final boolean showRotatingBeam = RenderWaypointBeacon.waypointProperties.showRotatingBeam.get();
            if (showStaticBeam || showRotatingBeam) {
                renderBeam(shiftX, -RenderWaypointBeacon.renderManager.viewerPosY, shiftZ, waypoint.getColor(), fadeAlpha, showStaticBeam, showRotatingBeam);
            }
            String label = waypoint.getName();
            boolean labelHidden = false;
            if (viewDistance > 0.5 && RenderWaypointBeacon.waypointProperties.autoHideLabel.get()) {
                final int angle = 5;
                final double yaw = Math.atan2(RenderWaypointBeacon.renderManager.viewerPosZ - waypointVec.z, RenderWaypointBeacon.renderManager.viewerPosX - waypointVec.x);
                double degrees = Math.toDegrees(yaw) + 90.0;
                if (degrees < 0.0) {
                    degrees += 360.0;
                }
                double playerYaw = RenderWaypointBeacon.renderManager.renderViewEntity.getRotationYawHead() % 360.0f;
                if (playerYaw < 0.0) {
                    playerYaw += 360.0;
                }
                playerYaw = Math.toRadians(playerYaw);
                double playerDegrees = Math.toDegrees(playerYaw);
                degrees += angle;
                playerDegrees += angle;
                labelHidden = (Math.abs(degrees + angle - (playerDegrees + angle)) > angle);
            }
            double scale = 0.00390625 * ((viewDistance + 4.0) / 3.0);
            final TextureImpl texture = waypoint.getTexture();
            final double halfTexHeight = texture.getHeight() / 2;
            final boolean showName = RenderWaypointBeacon.waypointProperties.showName.get() && label != null && label.length() > 0;
            final boolean showDistance = RenderWaypointBeacon.waypointProperties.showDistance.get();
            if (!labelHidden && (showName || showDistance)) {
                final StringBuilder sb = new StringBuilder();
                if (RenderWaypointBeacon.waypointProperties.boldLabel.get()) {
                    sb.append(TextFormatting.BOLD);
                }
                if (showName) {
                    sb.append(label);
                }
                if (showName && showDistance) {
                    sb.append(" ");
                }
                if (showDistance) {
                    sb.append(String.format(RenderWaypointBeacon.distanceLabel, actualDistance));
                }
                if (sb.length() > 0) {
                    label = sb.toString();
                    GlStateManager.pushMatrix();
                    GlStateManager.disableLighting();
                    GL11.glNormal3d(0.0, 0.0, -1.0 * scale);
                    GlStateManager.translate(shiftX, shiftY, shiftZ);
                    GlStateManager.rotate(-RenderWaypointBeacon.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
                    GlStateManager.rotate(RenderWaypointBeacon.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
                    GlStateManager.scale(-scale, -scale, scale);
                    GlStateManager.depthMask(true);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    final int fontScale = RenderWaypointBeacon.waypointProperties.fontScale.get();
                    final double labelY = 0.0 - halfTexHeight - 8.0;
                    DrawUtil.drawLabel(label, 1.0, labelY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, 0, 0.6f * fadeAlpha, waypoint.getSafeColor(), fadeAlpha, fontScale, false);
                    GlStateManager.disableDepth();
                    GlStateManager.depthMask(false);
                    DrawUtil.drawLabel(label, 1.0, labelY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, 0, 0.4f * fadeAlpha, waypoint.getSafeColor(), fadeAlpha, fontScale, false);
                    GlStateManager.popMatrix();
                }
            }
            if (viewDistance > 0.1 && RenderWaypointBeacon.waypointProperties.showTexture.get()) {
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                GL11.glNormal3d(0.0, 0.0, -1.0 * scale);
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
                scale *= (RenderWaypointBeacon.waypointProperties.textureSmall.get() ? 1 : 2);
                GlStateManager.translate(shiftX, shiftY, shiftZ);
                GlStateManager.rotate(-RenderWaypointBeacon.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
                GlStateManager.rotate(RenderWaypointBeacon.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
                GlStateManager.scale(-scale, -scale, scale);
                GL11.glNormal3d(0.0, 0.0, -1.0 * scale);
                DrawUtil.drawColoredImage(texture, waypoint.getColor(), fadeAlpha, 0 - texture.getWidth() / 2 + 0.5, 0.0 - halfTexHeight + 0.2, 0.0);
                GlStateManager.popMatrix();
            }
        } finally {
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.disableFog();
            RenderHelper.disableStandardItemLighting();
        }
    }

    static void renderBeam(double x, final double y, double z, final Integer color, final float alpha, final boolean staticBeam, final boolean rotatingBeam) {
        RenderWaypointBeacon.mc.renderEngine.bindTexture(RenderWaypointBeacon.beam);
        GL11.glTexParameterf(3553, 10242, 10497.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        float time = RenderWaypointBeacon.mc.world.getTotalWorldTime();
        if (RenderWaypointBeacon.mc.isGamePaused()) {
            time = Minecraft.getSystemTime() / 50L;
        }
        final float texOffset = -(-time * 0.2f - MathHelper.floor(-time * 0.1f)) * 0.6f;
        if (rotatingBeam) {
            final byte b0 = 1;
            final double d3 = time * 0.025 * (1.0 - (b0 & 0x1) * 2.5);
            final int[] rgba = RGB.ints(color, alpha * 0.45f);
            DrawUtil.startDrawingQuads(true);
            GlStateManager.enableBlend();
            final double d4 = b0 * 0.2;
            final double d5 = Math.cos(d3 + 2.356194490192345) * d4;
            final double d6 = Math.sin(d3 + 2.356194490192345) * d4;
            final double d7 = Math.cos(d3 + 0.7853981633974483) * d4;
            final double d8 = Math.sin(d3 + 0.7853981633974483) * d4;
            final double d9 = Math.cos(d3 + 3.9269908169872414) * d4;
            final double d10 = Math.sin(d3 + 3.9269908169872414) * d4;
            final double d11 = Math.cos(d3 + 5.497787143782138) * d4;
            final double d12 = Math.sin(d3 + 5.497787143782138) * d4;
            final double d13 = 256.0f * alpha;
            final double d14 = 0.0;
            final double d15 = 1.0;
            final double d16 = -1.0f + texOffset;
            final double d17 = 256.0f * alpha * (0.5 / d4) + d16;
            DrawUtil.addVertexWithUV(x + d5, y + d13, z + d6, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d5, y, z + d6, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d7, y, z + d8, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d7, y + d13, z + d8, d14, d17, rgba);
            DrawUtil.addVertexWithUV(x + d11, y + d13, z + d12, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d11, y, z + d12, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d9, y, z + d10, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d9, y + d13, z + d10, d14, d17, rgba);
            DrawUtil.addVertexWithUV(x + d7, y + d13, z + d8, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d7, y, z + d8, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d11, y, z + d12, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d11, y + d13, z + d12, d14, d17, rgba);
            DrawUtil.addVertexWithUV(x + d9, y + d13, z + d10, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d9, y, z + d10, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d5, y, z + d6, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d5, y + d13, z + d6, d14, d17, rgba);
            DrawUtil.draw();
        }
        if (staticBeam) {
            GlStateManager.disableCull();
            final double d18 = 256.0f * alpha;
            final double d19 = -1.0f + texOffset;
            final double d20 = 256.0f * alpha + d19;
            x -= 0.5;
            z -= 0.5;
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.depthMask(false);
            final int[] rgba2 = RGB.ints(color, alpha * 0.4f);
            DrawUtil.startDrawingQuads(true);
            DrawUtil.addVertexWithUV(x + 0.2, y + d18, z + 0.2, 1.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y, z + 0.2, 1.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y, z + 0.2, 0.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y + d18, z + 0.2, 0.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y + d18, z + 0.8, 1.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y, z + 0.8, 1.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y, z + 0.8, 0.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y + d18, z + 0.8, 0.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y + d18, z + 0.2, 1.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y, z + 0.2, 1.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y, z + 0.8, 0.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y + d18, z + 0.8, 0.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y + d18, z + 0.8, 1.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y, z + 0.8, 1.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y, z + 0.2, 0.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y + d18, z + 0.2, 0.0, d20, rgba2);
            DrawUtil.draw();
            GlStateManager.disableBlend();
        }
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }
}
