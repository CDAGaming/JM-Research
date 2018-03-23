package journeymap.client.cartography.color;

import java.awt.*;
import com.google.common.base.*;
import journeymap.common.*;
import java.util.*;

public final class RGB
{
    public static final int ALPHA_OPAQUE = -16777216;
    public static final int BLACK_ARGB = -16777216;
    public static final int BLACK_RGB = 0;
    public static final int WHITE_ARGB = -1;
    public static final int WHITE_RGB = 16777215;
    public static final int GREEN_RGB = 65280;
    public static final int RED_RGB = 16711680;
    public static final int BLUE_RGB = 255;
    public static final int CYAN_RGB = 65535;
    public static final int GRAY_RGB = 8421504;
    public static final int DARK_GRAY_RGB = 4210752;
    public static final int LIGHT_GRAY_RGB = 12632256;
    
    public static boolean isBlack(final int rgb) {
        return rgb == -16777216 || rgb == 0;
    }
    
    public static boolean isWhite(final int rgb) {
        return rgb == -1 || rgb == 16777215;
    }
    
    public static Integer max(final Integer... colors) {
        final int[] out = { 0, 0, 0 };
        int used = 0;
        for (final Integer color : colors) {
            if (color != null) {
                final int[] cInts = ints(color);
                out[0] = Math.max(out[0], cInts[0]);
                out[1] = Math.max(out[1], cInts[1]);
                out[2] = Math.max(out[2], cInts[2]);
                ++used;
            }
        }
        if (used == 0) {
            return null;
        }
        return toInteger(out);
    }
    
    public static int toInteger(final float r, final float g, final float b) {
        return 0xFF000000 | ((int)(r * 255.0f + 0.5) & 0xFF) << 16 | ((int)(g * 255.0f + 0.5) & 0xFF) << 8 | ((int)(b * 255.0f + 0.5) & 0xFF);
    }
    
    public static int toInteger(final float[] rgb) {
        return 0xFF000000 | ((int)(rgb[0] * 255.0f + 0.5) & 0xFF) << 16 | ((int)(rgb[1] * 255.0f + 0.5) & 0xFF) << 8 | ((int)(rgb[2] * 255.0f + 0.5) & 0xFF);
    }
    
    public static int toArbg(final int rgbInt, final float alpha) {
        final int[] rgba = ints(rgbInt, alpha);
        return (rgba[3] & 0xFF) << 24 | (rgba[0] & 0xFF) << 16 | (rgba[2] & 0xFF) << 8 | (rgba[1] & 0xFF);
    }
    
    public static int toInteger(final int r, final int g, final int b) {
        return 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }
    
    public static int toInteger(final int[] rgb) {
        return 0xFF000000 | (rgb[0] & 0xFF) << 16 | (rgb[1] & 0xFF) << 8 | (rgb[2] & 0xFF);
    }
    
    public static int rgbaToRgb(final int rgba) {
        return toInteger(rgba & 0xFF, rgba >>> 8 & 0xFF, rgba >>> 16 & 0xFF);
    }
    
    public static int tint(final int rgb, final int rgbaTint) {
        final int[] tint = ints(rgbaTint);
        final int alpha = rgbaTint >>> 24 & 0xFF;
        final int[] old = ints(rgb);
        final int newR = old[0] + (tint[0] - old[0]) * alpha;
        final int newG = old[1] + (tint[1] - old[1]) * alpha;
        final int newB = old[2] + (tint[2] - old[2]) * alpha;
        return toInteger(newR, newG, newB);
    }
    
    public static Color toColor(final Integer rgb) {
        return (rgb == null) ? null : new Color(rgb);
    }
    
    public static String toString(final Integer rgb) {
        if (rgb == null) {
            return "null";
        }
        final int[] ints = ints(rgb);
        return String.format("r=%s,g=%s,b=%s", ints[0], ints[1], ints[2]);
    }
    
    public static String toHexString(final Integer rgb) {
        final int[] ints = ints(rgb);
        return String.format("#%02x%02x%02x", ints[0], ints[1], ints[2]);
    }
    
    public static int adjustBrightness(final int rgb, final float factor) {
        if (factor == 1.0f) {
            return rgb;
        }
        return toInteger(clampFloats(floats(rgb), factor));
    }
    
    public static int greyScale(final int rgb) {
        final int[] ints = ints(rgb);
        final int avg = clampInt((ints[0] + ints[1] + ints[2]) / 3);
        return toInteger(avg, avg, avg);
    }
    
    public static int bevelSlope(final int rgb, final float factor) {
        final float bluer = (factor < 1.0f) ? 0.85f : 1.0f;
        final float[] floats = floats(rgb);
        floats[0] = floats[0] * bluer * factor;
        floats[1] = floats[1] * bluer * factor;
        floats[2] *= factor;
        return toInteger(clampFloats(floats, 1.0f));
    }
    
    public static int darkenAmbient(final int rgb, final float factor, final float[] ambient) {
        final float[] floats = floats(rgb);
        floats[0] *= factor + ambient[0];
        floats[1] *= factor + ambient[1];
        floats[2] *= factor + ambient[2];
        return toInteger(clampFloats(floats, 1.0f));
    }
    
    public static int[] ints(final int rgb) {
        return new int[] { rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF };
    }
    
    public static int[] ints(final int rgb, final int alpha) {
        return new int[] { rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, alpha & 0xFF };
    }
    
    public static int[] ints(final int rgb, final float alpha) {
        return new int[] { rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, (int)(alpha * 255.0f + 0.5) & 0xFF };
    }
    
    public static float[] floats(final int rgb) {
        return new float[] { (rgb >> 16 & 0xFF) / 255.0f, (rgb >> 8 & 0xFF) / 255.0f, (rgb & 0xFF) / 255.0f };
    }
    
    public static float[] floats(final int rgb, final float alpha) {
        return new float[] { (rgb >> 16 & 0xFF) / 255.0f, (rgb >> 8 & 0xFF) / 255.0f, (rgb & 0xFF) / 255.0f, clampFloat(alpha) };
    }
    
    public static int blendWith(final int rgb, final int otherRgb, final float otherAlpha) {
        if (otherAlpha == 1.0f) {
            return otherRgb;
        }
        if (otherAlpha == 0.0f) {
            return rgb;
        }
        final float[] floats = floats(rgb);
        final float[] otherFloats = floats(otherRgb);
        floats[0] = otherFloats[0] * otherAlpha / 1.0f + floats[0] * (1.0f - otherAlpha);
        floats[1] = otherFloats[1] * otherAlpha / 1.0f + floats[1] * (1.0f - otherAlpha);
        floats[2] = otherFloats[2] * otherAlpha / 1.0f + floats[2] * (1.0f - otherAlpha);
        return toInteger(floats);
    }
    
    public static int multiply(final int rgb, final int multiplier) {
        final float[] rgbFloats = floats(rgb);
        final float[] multFloats = floats(multiplier);
        rgbFloats[0] *= multFloats[0];
        rgbFloats[1] *= multFloats[1];
        rgbFloats[2] *= multFloats[2];
        return toInteger(rgbFloats);
    }
    
    public static float clampFloat(final float value) {
        return (value < 0.0f) ? 0.0f : ((value > 1.0f) ? 1.0f : value);
    }
    
    public static float[] clampFloats(final float[] rgbFloats, final float factor) {
        final float r = rgbFloats[0] * factor;
        final float g = rgbFloats[1] * factor;
        final float b = rgbFloats[2] * factor;
        rgbFloats[0] = ((r < 0.0f) ? 0.0f : ((r > 1.0f) ? 1.0f : r));
        rgbFloats[1] = ((g < 0.0f) ? 0.0f : ((g > 1.0f) ? 1.0f : g));
        rgbFloats[2] = ((b < 0.0f) ? 0.0f : ((b > 1.0f) ? 1.0f : b));
        return rgbFloats;
    }
    
    public static int clampInt(final int value) {
        return (value < 0) ? 0 : ((value > 255) ? 255 : value);
    }
    
    public static int toClampedInt(final float value) {
        return clampInt((int)(value * 255.0f));
    }
    
    public static float toScaledFloat(final int value) {
        return clampInt(value) / 255.0f;
    }
    
    public static int hexToInt(final String hexColor) {
        if (!Strings.isNullOrEmpty(hexColor)) {
            try {
                return 0xFF000000 | Integer.parseInt(hexColor.replaceFirst("#", ""), 16);
            }
            catch (Exception e) {
                Journeymap.getLogger().warn("Invalid color string: " + hexColor);
            }
        }
        return 0;
    }
    
    public static int randomColor() {
        final Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        final int min = 100;
        final int max = Math.max(r, Math.max(g, b));
        if (max < min) {
            if (r == max) {
                r = min;
            }
            else if (g == max) {
                g = min;
            }
            else {
                b = min;
            }
        }
        return toInteger(r, g, b);
    }
    
    public static Integer subtract(final int minuend, final int subtrahend) {
        final int[] larger = ints(minuend);
        final int[] smaller = ints(subtrahend);
        return toInteger(larger[0] - smaller[0], larger[1] - smaller[1], larger[2] - smaller[2]);
    }
}
