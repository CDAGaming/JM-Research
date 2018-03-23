package journeymap.client.render.draw;

import net.minecraftforge.fml.client.*;
import net.minecraft.client.gui.*;
import journeymap.client.ui.theme.*;
import net.minecraft.client.renderer.*;
import journeymap.client.cartography.color.*;
import journeymap.client.render.texture.*;
import org.lwjgl.opengl.*;
import java.util.*;
import java.awt.geom.*;
import journeymap.client.api.model.*;
import net.minecraft.client.renderer.vertex.*;

public class DrawUtil
{
    public static double zLevel;
    static Tessellator tessellator;
    static BufferBuilder worldrenderer;
    
    public static void drawCenteredLabel(final String text, final double x, final double y, final Integer bgColor, final float bgAlpha, final Integer color, final float alpha, final double fontScale) {
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, true, 0.0);
    }
    
    public static void drawCenteredLabel(final String text, final double x, final double y, final Integer bgColor, final float bgAlpha, final Integer color, final float alpha, final double fontScale, final boolean fontShadow) {
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, fontShadow, 0.0);
    }
    
    public static void drawCenteredLabel(final String text, final double x, final double y, final Integer bgColor, final float bgAlpha, final Integer color, final float alpha, final double fontScale, final double rotation) {
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, true, rotation);
    }
    
    public static void drawLabel(final String text, final double x, final double y, final HAlign hAlign, final VAlign vAlign, final Integer bgColor, final float bgAlpha, final int color, final float alpha, final double fontScale, final boolean fontShadow) {
        drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, color, alpha, fontScale, fontShadow, 0.0);
    }
    
    public static void drawLabels(final String[] lines, final double x, double y, final HAlign hAlign, final VAlign vAlign, Integer bgColor, final float bgAlpha, final Integer color, final float alpha, final double fontScale, final boolean fontShadow, final double rotation) {
        if (lines.length == 0) {
            return;
        }
        if (lines.length == 1) {
            drawLabel(lines[0], x, y, hAlign, vAlign, bgColor, bgAlpha, color, alpha, fontScale, fontShadow, rotation);
            return;
        }
        final FontRenderer fontRenderer = FMLClientHandler.instance().getClient().field_71466_p;
        final double vpad = fontRenderer.func_82883_a() ? 0.0 : (fontShadow ? 6.0 : 4.0);
        final double lineHeight = fontRenderer.field_78288_b * fontScale;
        double bgHeight = lineHeight * lines.length + vpad;
        double bgWidth = 0.0;
        if (bgColor != null && bgAlpha > 0.0f) {
            for (final String line : lines) {
                bgWidth = Math.max(bgWidth, fontRenderer.func_78256_a(line) * fontScale);
            }
            if (bgWidth % 2.0 == 0.0) {
                ++bgWidth;
            }
        }
        if (lines.length > 1) {
            switch (vAlign) {
                case Above: {
                    y -= lineHeight * lines.length;
                    bgHeight += vpad / 2.0;
                    break;
                }
                case Middle: {
                    y -= bgHeight / 2.0;
                    break;
                }
            }
        }
        for (final String line : lines) {
            drawLabel(line, x, y, hAlign, vAlign, bgColor, bgAlpha, bgWidth, bgHeight, color, alpha, fontScale, fontShadow, rotation);
            bgColor = null;
            y += lineHeight;
        }
    }
    
    public static void drawLabel(final String text, final Theme.LabelSpec labelSpec, final double x, final double y, final HAlign hAlign, final VAlign vAlign, final double fontScale, final double rotation) {
        drawLabel(text, x, y, hAlign, vAlign, labelSpec.background.getColor(), labelSpec.background.alpha, labelSpec.foreground.getColor(), labelSpec.foreground.alpha, fontScale, labelSpec.shadow, rotation);
    }
    
    public static void drawLabel(final String text, final double x, final double y, final HAlign hAlign, final VAlign vAlign, final Integer bgColor, final float bgAlpha, final Integer color, final float alpha, final double fontScale, final boolean fontShadow, final double rotation) {
        double bgWidth = 0.0;
        double bgHeight = 0.0;
        if (bgColor != null && bgAlpha > 0.0f) {
            final FontRenderer fontRenderer = FMLClientHandler.instance().getClient().field_71466_p;
            bgWidth = fontRenderer.func_78256_a(text);
            bgHeight = getLabelHeight(fontRenderer, fontShadow);
        }
        drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, bgWidth, bgHeight, color, alpha, fontScale, fontShadow, rotation);
    }
    
    public static void drawLabel(final String text, double x, double y, final HAlign hAlign, final VAlign vAlign, final Integer bgColor, final float bgAlpha, final double bgWidth, final double bgHeight, Integer color, final float alpha, final double fontScale, final boolean fontShadow, final double rotation) {
        if (text == null || text.length() == 0) {
            return;
        }
        final FontRenderer fontRenderer = FMLClientHandler.instance().getClient().field_71466_p;
        final boolean drawRect = bgColor != null && bgAlpha > 0.0f;
        final double width = fontRenderer.func_78256_a(text);
        int height = drawRect ? getLabelHeight(fontRenderer, fontShadow) : fontRenderer.field_78288_b;
        if (!drawRect && fontRenderer.func_82883_a()) {
            --height;
        }
        GlStateManager.func_179094_E();
        try {
            if (fontScale != 1.0) {
                x /= fontScale;
                y /= fontScale;
                GlStateManager.func_179139_a(fontScale, fontScale, 0.0);
            }
            float textX = (float)x;
            float textY = (float)y;
            double rectX = x;
            double rectY = y;
            switch (hAlign) {
                case Left: {
                    textX = (float)(x - width);
                    rectX = textX;
                    break;
                }
                case Center: {
                    textX = (float)(x - width / 2.0 + ((fontScale > 1.0) ? 0.5 : 0.0));
                    rectX = (float)(x - Math.max(1.0, bgWidth) / 2.0 + ((fontScale > 1.0) ? 0.5 : 0.0));
                    break;
                }
                case Right: {
                    textX = (float)x;
                    rectX = (float)x;
                    break;
                }
            }
            final double vpad = drawRect ? ((height - fontRenderer.field_78288_b) / 2.0) : 0.0;
            switch (vAlign) {
                case Above: {
                    rectY = y - height;
                    textY = (float)(rectY + vpad + (fontRenderer.func_82883_a() ? 0 : 1));
                    break;
                }
                case Middle: {
                    rectY = y - height / 2 + ((fontScale > 1.0) ? 0.5 : 0.0);
                    textY = (float)(rectY + vpad);
                    break;
                }
                case Below: {
                    rectY = y;
                    textY = (float)(rectY + vpad);
                    break;
                }
            }
            if (rotation != 0.0) {
                GlStateManager.func_179137_b(x, y, 0.0);
                GlStateManager.func_179114_b((float)(-rotation), 0.0f, 0.0f, 1.0f);
                GlStateManager.func_179137_b(-x, -y, 0.0);
            }
            if (drawRect) {
                final int hpad = 2;
                drawRectangle(rectX - 2.0 - 0.5, rectY, bgWidth + 4.0, bgHeight, bgColor, bgAlpha);
            }
            if (alpha < 1.0f) {
                color = RGB.toArbg(color, alpha);
            }
            GlStateManager.func_179137_b(textX - Math.floor(textX), textY - Math.floor(textY), 0.0);
            fontRenderer.func_175065_a(text, textX, textY, (int)color, fontShadow);
        }
        finally {
            GlStateManager.func_179121_F();
        }
    }
    
    public static int getLabelHeight(final FontRenderer fr, final boolean fontShadow) {
        final int vpad = fr.func_82883_a() ? 0 : (fontShadow ? 6 : 4);
        return fr.field_78288_b + vpad;
    }
    
    public static void drawImage(final TextureImpl texture, final double x, final double y, final boolean flip, final float scale, final double rotation) {
        drawQuad(texture, x, y, texture.getWidth() * scale, texture.getHeight() * scale, flip, rotation);
    }
    
    public static void drawImage(final TextureImpl texture, final float alpha, final double x, final double y, final boolean flip, final float scale, final double rotation) {
        drawQuad(texture, 16777215, alpha, x, y, texture.getWidth() * scale, texture.getHeight() * scale, false, rotation);
    }
    
    public static void drawClampedImage(final TextureImpl texture, final double x, final double y, final float scale, final double rotation) {
        drawClampedImage(texture, 16777215, 1.0f, x, y, scale, rotation);
    }
    
    public static void drawClampedImage(final TextureImpl texture, final int color, final float alpha, final double x, final double y, final float scale, final double rotation) {
        drawQuad(texture, color, alpha, x, y, texture.getWidth() * scale, texture.getHeight() * scale, false, rotation);
    }
    
    public static void drawColoredImage(final TextureImpl texture, final int color, final float alpha, final double x, final double y, final float scale, final double rotation) {
        drawQuad(texture, color, alpha, x, y, texture.getWidth() * scale, texture.getHeight() * scale, false, rotation);
    }
    
    public static void drawColoredSprite(final TextureImpl texture, final double displayWidth, final double displayHeight, final double spriteX, final double spriteY, final double spriteWidth, final double spriteHeight, final Integer color, final float alpha, final double x, final double y, final float scale, final double rotation) {
        final double texWidth = texture.getWidth();
        final double texHeight = texture.getHeight();
        final double minU = Math.max(0.0, spriteX / texWidth);
        final double minV = Math.max(0.0, spriteY / texHeight);
        final double maxU = Math.min(1.0, (spriteX + spriteWidth) / texWidth);
        final double maxV = Math.min(1.0, (spriteY + spriteHeight) / texHeight);
        drawQuad(texture, color, alpha, x, y, displayWidth * scale, displayHeight * scale, minU, minV, maxU, maxV, rotation, false, true, 770, 771, false);
    }
    
    public static void drawColoredImage(final TextureImpl texture, final int color, final float alpha, final double x, final double y, final double rotation) {
        drawQuad(texture, color, alpha, x, y, texture.getWidth(), texture.getHeight(), false, rotation);
    }
    
    public static void drawColoredImage(final TextureImpl texture, final int color, final float alpha, final double x, final double y, final int width, final int height, final double rotation) {
        drawQuad(texture, color, alpha, x, y, width, height, false, rotation);
    }
    
    public static void drawQuad(final TextureImpl texture, final double x, final double y, final double width, final double height, final boolean flip, final double rotation) {
        drawQuad(texture, 16777215, 1.0f, x, y, width, height, 0.0, 0.0, 1.0, 1.0, rotation, flip, true, 770, 771, false);
    }
    
    public static void drawQuad(final TextureImpl texture, final int color, final float alpha, final double x, final double y, final double width, final double height, final boolean flip, final double rotation) {
        drawQuad(texture, color, alpha, x, y, width, height, 0.0, 0.0, 1.0, 1.0, rotation, flip, true, 770, 771, false);
    }
    
    public static void drawQuad(final TextureImpl texture, final int color, float alpha, final double x, final double y, final double width, final double height, final double minU, final double minV, final double maxU, final double maxV, final double rotation, final boolean flip, final boolean blend, final int glBlendSfactor, final int glBlendDFactor, final boolean clampTexture) {
        GlStateManager.func_179094_E();
        try {
            if (blend) {
                GlStateManager.func_179147_l();
                GlStateManager.func_179120_a(glBlendSfactor, glBlendDFactor, 1, 0);
            }
            GlStateManager.func_179098_w();
            GlStateManager.func_179144_i(texture.func_110552_b());
            if (alpha > 1.0f) {
                alpha /= 255.0f;
            }
            if (blend) {
                final float[] c = RGB.floats(color);
                GlStateManager.func_179131_c(c[0], c[1], c[2], alpha);
            }
            else {
                GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, alpha);
            }
            GL11.glTexParameteri(3553, 10241, 9729);
            GL11.glTexParameteri(3553, 10240, 9729);
            final int texEdgeBehavior = clampTexture ? 33071 : 10497;
            GL11.glTexParameteri(3553, 10242, texEdgeBehavior);
            GL11.glTexParameteri(3553, 10243, texEdgeBehavior);
            if (rotation != 0.0) {
                final double transX = x + width / 2.0;
                final double transY = y + height / 2.0;
                GlStateManager.func_179137_b(transX, transY, 0.0);
                GlStateManager.func_179114_b((float)rotation, 0.0f, 0.0f, 1.0f);
                GlStateManager.func_179137_b(-transX, -transY, 0.0);
            }
            final double direction = flip ? (-maxU) : maxU;
            startDrawingQuads(false);
            addVertexWithUV(x, height + y, DrawUtil.zLevel, minU, maxV);
            addVertexWithUV(x + width, height + y, DrawUtil.zLevel, direction, maxV);
            addVertexWithUV(x + width, y, DrawUtil.zLevel, direction, minV);
            addVertexWithUV(x, y, DrawUtil.zLevel, minU, minV);
            draw();
            if (blend) {
                GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
                if (glBlendSfactor != 770 || glBlendDFactor != 771) {
                    GlStateManager.func_179147_l();
                    GlStateManager.func_179120_a(770, 771, 1, 0);
                }
            }
        }
        finally {
            GlStateManager.func_179121_F();
        }
    }
    
    public static void drawRectangle(final double x, final double y, final double width, final double height, final int color, final float alpha) {
        GlStateManager.func_179147_l();
        GlStateManager.func_179090_x();
        GlStateManager.func_179118_c();
        GlStateManager.func_179120_a(770, 771, 1, 0);
        final int[] rgba = RGB.ints(color, alpha);
        startDrawingQuads(true);
        addVertex(x, height + y, DrawUtil.zLevel, rgba);
        addVertex(x + width, height + y, DrawUtil.zLevel, rgba);
        addVertex(x + width, y, DrawUtil.zLevel, rgba);
        addVertex(x, y, DrawUtil.zLevel, rgba);
        draw();
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.func_179098_w();
        GlStateManager.func_179141_d();
        GlStateManager.func_179084_k();
    }
    
    public static void drawPolygon(final double xOffset, final double yOffset, final List<Point2D.Double> screenPoints, final ShapeProperties shapeProperties) {
        GlStateManager.func_179147_l();
        GlStateManager.func_179090_x();
        GlStateManager.func_179141_d();
        GlStateManager.func_179120_a(770, 771, 1, 0);
        if (shapeProperties.getFillOpacity() >= 0.01f) {
            final float[] rgba = RGB.floats(shapeProperties.getFillColor(), shapeProperties.getFillOpacity());
            GlStateManager.func_179131_c(rgba[0], rgba[1], rgba[2], rgba[3]);
            final int lastIndex = screenPoints.size() - 1;
            GL11.glBegin(9);
            for (int i = 0; i <= lastIndex; ++i) {
                final int j = (i < lastIndex) ? (i + 1) : 0;
                final Point2D.Double first = screenPoints.get(i);
                final Point2D.Double second = screenPoints.get(j);
                GL11.glVertex2d(first.getX() + xOffset, first.getY() + yOffset);
                GL11.glVertex2d(second.getX() + xOffset, second.getY() + yOffset);
            }
            GL11.glEnd();
        }
        if (shapeProperties.getStrokeOpacity() >= 0.01f && shapeProperties.getStrokeWidth() > 0.0f) {
            final float[] rgba = RGB.floats(shapeProperties.getStrokeColor(), shapeProperties.getFillOpacity());
            GlStateManager.func_179131_c(rgba[0], rgba[1], rgba[2], rgba[3]);
            final float stroke = shapeProperties.getStrokeWidth();
            GL11.glLineWidth(stroke);
            final int lastIndex2 = screenPoints.size() - 1;
            GL11.glBegin(3);
            for (int k = 0; k <= lastIndex2; ++k) {
                final int l = (k < lastIndex2) ? (k + 1) : 0;
                final Point2D.Double first2 = screenPoints.get(k);
                final Point2D.Double second2 = screenPoints.get(l);
                GL11.glVertex2d(first2.getX() + xOffset, first2.getY() + yOffset);
                GL11.glVertex2d(second2.getX() + xOffset, second2.getY() + yOffset);
            }
            GL11.glEnd();
        }
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.func_179098_w();
        GlStateManager.func_179141_d();
        GlStateManager.func_179084_k();
    }
    
    public static void drawGradientRect(final double x, final double y, final double width, final double height, final int startColor, float startAlpha, final int endColor, float endAlpha) {
        if (startAlpha > 1.0f) {
            startAlpha /= 255.0f;
        }
        if (endAlpha > 1.0f) {
            endAlpha /= 255.0f;
        }
        final int[] rgbaStart = RGB.ints(startColor, startAlpha);
        final int[] rgbaEnd = RGB.ints(endColor, endAlpha);
        GlStateManager.func_179090_x();
        GlStateManager.func_179147_l();
        GlStateManager.func_179118_c();
        GlStateManager.func_179120_a(770, 771, 1, 0);
        GlStateManager.func_179103_j(7425);
        startDrawingQuads(true);
        addVertexWithUV(x, height + y, DrawUtil.zLevel, 0.0, 1.0, rgbaEnd);
        addVertexWithUV(x + width, height + y, DrawUtil.zLevel, 1.0, 1.0, rgbaEnd);
        addVertexWithUV(x + width, y, DrawUtil.zLevel, 1.0, 0.0, rgbaStart);
        addVertexWithUV(x, y, DrawUtil.zLevel, 0.0, 0.0, rgbaStart);
        draw();
        GlStateManager.func_179103_j(7424);
        GlStateManager.func_179098_w();
        GlStateManager.func_179141_d();
        GlStateManager.func_179147_l();
    }
    
    public static void drawBoundTexture(final double startU, final double startV, final double startX, final double startY, final double z, final double endU, final double endV, final double endX, final double endY) {
        startDrawingQuads(false);
        addVertexWithUV(startX, endY, z, startU, endV);
        addVertexWithUV(endX, endY, z, endU, endV);
        addVertexWithUV(endX, startY, z, endU, startV);
        addVertexWithUV(startX, startY, z, startU, startV);
        draw();
    }
    
    public static void drawEntity(final double x, final double y, final double heading, final TextureImpl texture, final float scale, final double rotation) {
        drawEntity(x, y, heading, texture, 1.0f, scale, rotation);
    }
    
    public static void drawEntity(final double x, final double y, final double heading, final TextureImpl texture, final float alpha, final float scale, final double rotation) {
        final double width = texture.getWidth() * scale;
        final double height = texture.getHeight() * scale;
        final double drawX = x - width / 2.0;
        final double drawY = y - height / 2.0;
        drawImage(texture, alpha, drawX, drawY, false, scale, heading);
    }
    
    public static void drawColoredEntity(final double x, final double y, final TextureImpl texture, final int color, final float alpha, final float scale, final double rotation) {
        final double width = texture.getWidth() * scale;
        final double height = texture.getHeight() * scale;
        final double drawX = x - width / 2.0;
        final double drawY = y - height / 2.0;
        drawColoredImage(texture, color, alpha, drawX, drawY, scale, rotation);
    }
    
    public static void sizeDisplay(final double width, final double height) {
        GlStateManager.func_179086_m(256);
        GlStateManager.func_179128_n(5889);
        GlStateManager.func_179096_D();
        GlStateManager.func_179130_a(0.0, width, height, 0.0, 100.0, 3000.0);
        GlStateManager.func_179128_n(5888);
        GlStateManager.func_179096_D();
        GlStateManager.func_179109_b(0.0f, 0.0f, -2000.0f);
    }
    
    public static void draw() {
        DrawUtil.tessellator.func_78381_a();
    }
    
    public static void startDrawingQuads(final boolean useColor) {
        if (useColor) {
            DrawUtil.worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181709_i);
        }
        else {
            DrawUtil.worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        }
    }
    
    public static void addVertexWithUV(final double x, final double y, final double z, final double u, final double v) {
        DrawUtil.worldrenderer.func_181662_b(x, y, z).func_187315_a(u, v).func_181675_d();
    }
    
    public static void addVertex(final double x, final double y, final double z, final int[] rgba) {
        DrawUtil.worldrenderer.func_181662_b(x, y, z).func_187315_a(1.0, 1.0).func_181669_b(rgba[0], rgba[1], rgba[2], rgba[3]).func_181675_d();
    }
    
    public static void addVertexWithUV(final double x, final double y, final double z, final double u, final double v, final int[] rgba) {
        DrawUtil.worldrenderer.func_181662_b(x, y, z).func_187315_a(u, v).func_181669_b(rgba[0], rgba[1], rgba[2], rgba[3]).func_181675_d();
    }
    
    static {
        DrawUtil.zLevel = 0.0;
        DrawUtil.tessellator = Tessellator.func_178181_a();
        DrawUtil.worldrenderer = DrawUtil.tessellator.func_178180_c();
    }
    
    public enum HAlign
    {
        Left, 
        Center, 
        Right;
    }
    
    public enum VAlign
    {
        Above, 
        Middle, 
        Below;
    }
}
