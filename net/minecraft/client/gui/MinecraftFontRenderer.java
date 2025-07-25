package net.minecraft.client.gui;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import club.tifality.gui.font.FontRenderer;
import club.tifality.utils.render.OGLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomColors;
import net.optifine.render.GlBlendState;
import net.optifine.util.FontUtils;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MinecraftFontRenderer implements IResourceManagerReloadListener, FontRenderer {
    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    protected final int[] colorCode = new int[32];
    private final TextureManager renderEngine;
    private final byte[] glyphWidth = new byte[65536];
    private final float[] charWidthFloat = new float[256];
    private final GlBlendState oldBlendState = new GlBlendState();
    public static final int FONT_HEIGHT = 9;
    public Random fontRandom = new Random();
    public GameSettings gameSettings;
    public ResourceLocation locationFontTextureBase;
    public float offsetBold = 1.0F;
    protected float posX;
    protected float posY;
    protected boolean unicodeFlag;
    protected boolean bidiFlag;
    protected float red;
    protected float blue;
    protected float green;
    protected float alpha;
    protected int textColor;
    protected boolean randomStyle;
    protected boolean boldStyle;
    protected boolean italicStyle;

    private ResourceLocation locationFontTexture;

    public MinecraftFontRenderer(GameSettings gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn) {
        this.gameSettings = gameSettingsIn;
        this.locationFontTextureBase = location;
        this.locationFontTexture = location;
        this.renderEngine = textureManagerIn;
        this.unicodeFlag = false;
        this.locationFontTexture = FontUtils.getHdFontLocation(this.locationFontTextureBase);
        this.bindTexture(this.locationFontTexture);

        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;

            if (i == 6) {
                k += 85;
            }

            if (gameSettingsIn.anaglyph) {
                int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
                int k1 = (k * 30 + l * 70) / 100;
                int l1 = (k * 30 + i1 * 70) / 100;
                k = j1;
                l = k1;
                i1 = l1;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }

        this.readGlyphSizes();
    }

    /**
     * Checks if the char code is a hexadecimal character, used to set colour.
     */
    private static boolean isFormatColor(char colorChar) {
        return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102 || colorChar >= 65 && colorChar <= 70;
    }

    /**
     * Checks if the char code is O-K...lLrRk-o... used to set special formatting.
     */
    private static boolean isFormatSpecial(char formatChar) {
        return formatChar >= 107 && formatChar <= 111 || formatChar >= 75 && formatChar <= 79 || formatChar == 114 || formatChar == 82;
    }

    /**
     * Digests a string for nonprinting formatting characters then returns a string containing only that formatting.
     */
    public static String getFormatFromString(String text) {
        StringBuilder s = new StringBuilder();
        int i = -1;
        int j = text.length();

        while ((i = text.indexOf(167, i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = text.charAt(i + 1);

                if (isFormatColor(c0)) {
                    s = new StringBuilder("\u00a7" + c0);
                } else if (isFormatSpecial(c0)) {
                    s.append("\u00a7").append(c0);
                }
            }
        }

        return s.toString();
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.locationFontTexture = FontUtils.getHdFontLocation(this.locationFontTextureBase);

        Arrays.fill(unicodePageLocations, null);

        this.readFontTexture();
        this.readGlyphSizes();
    }

    private void readFontTexture() {
        BufferedImage bufferedimage;

        try {
            bufferedimage = TextureUtil.readBufferedImage(this.getResourceInputStream(this.locationFontTexture));
        } catch (IOException ioexception1) {
            throw new RuntimeException(ioexception1);
        }

        Properties properties = FontUtils.readFontProperties(this.locationFontTexture);
        int i = bufferedimage.getWidth();
        int j = bufferedimage.getHeight();
        int k = i / 16;
        int l = j / 16;
        float f = (float) i / 128.0F;
        float f1 = Config.limit(f, 1.0F, 2.0F);
        this.offsetBold = 1.0F / f1;
        float f2 = FontUtils.readFloat(properties, "offsetBold", -1.0F);

        if (f2 >= 0.0F) {
            this.offsetBold = f2;
        }

        int[] aint = new int[i * j];
        bufferedimage.getRGB(0, 0, i, j, aint, 0, i);

        for (int i1 = 0; i1 < 256; ++i1) {
            int j1 = i1 % 16;
            int k1 = i1 / 16;
            int l1 = 0;

            for (l1 = k - 1; l1 >= 0; --l1) {
                int i2 = j1 * k + l1;
                boolean flag = true;

                for (int j2 = 0; j2 < l && flag; ++j2) {
                    int k2 = (k1 * l + j2) * i;
                    int l2 = aint[i2 + k2];
                    int i3 = l2 >> 24 & 255;

                    if (i3 > 16) {
                        flag = false;
                    }
                }

                if (!flag) {
                    break;
                }
            }

            if (i1 == 65) {
                i1 = i1;
            }

            if (i1 == 32) {
                if (k <= 8) {
                    l1 = (int) (2.0F * f);
                } else {
                    l1 = (int) (1.5F * f);
                }
            }

            this.charWidthFloat[i1] = (float) (l1 + 1) / f + 1.0F;
        }

        FontUtils.readCustomCharWidths(properties, this.charWidthFloat);
    }

    private void readGlyphSizes() {
        InputStream inputstream = null;

        try {
            inputstream = this.getResourceInputStream(new ResourceLocation("font/glyph_sizes.bin"));
            inputstream.read(this.glyphWidth);
        } catch (IOException ioexception) {
            throw new RuntimeException(ioexception);
        } finally {
            IOUtils.closeQuietly(inputstream);
        }
    }

    protected float renderChar(char p_181559_1_, int charIndex, boolean p_181559_2_) {
        if (p_181559_1_ != 32 && p_181559_1_ != 160) {
            return charIndex != -1 && !this.unicodeFlag ?
                    this.renderDefaultChar(charIndex, p_181559_2_) :
                    this.renderUnicodeChar(p_181559_1_, p_181559_2_);
        } else {
            return !this.unicodeFlag ? this.charWidthFloat[p_181559_1_] : 4.0F;
        }
    }

    /**
     * Render a single character with the default.png font at current (posX,posY) location...
     */
    private float renderDefaultChar(int p_78266_1_, boolean p_78266_2_) {
        int i = p_78266_1_ % 16 * 8;
        int j = p_78266_1_ / 16 * 8;
        int k = p_78266_2_ ? 1 : 0;
        this.bindTexture(this.locationFontTexture);
        float f = this.charWidthFloat[p_78266_1_];
        float f1 = 7.99F;
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f((float) i / 128.0F, (float) j / 128.0F);
        GL11.glVertex3f(this.posX + (float) k, this.posY, 0.0F);
        GL11.glTexCoord2f((float) i / 128.0F, ((float) j + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX - (float) k, this.posY + 7.99F, 0.0F);
        GL11.glTexCoord2f(((float) i + f1 - 1.0F) / 128.0F, (float) j / 128.0F);
        GL11.glVertex3f(this.posX + f1 - 1.0F + (float) k, this.posY, 0.0F);
        GL11.glTexCoord2f(((float) i + f1 - 1.0F) / 128.0F, ((float) j + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX + f1 - 1.0F - (float) k, this.posY + 7.99F, 0.0F);
        GL11.glEnd();
        return f;
    }

    private ResourceLocation getUnicodePageLocation(int p_111271_1_) {
        if (unicodePageLocations[p_111271_1_] == null) {
            unicodePageLocations[p_111271_1_] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", Integer.valueOf(p_111271_1_)));
            unicodePageLocations[p_111271_1_] = FontUtils.getHdFontLocation(unicodePageLocations[p_111271_1_]);
        }

        return unicodePageLocations[p_111271_1_];
    }

    /**
     * Load one of the /font/glyph_XX.png into a new GL texture and store the texture ID in glyphTextureName array.
     */
    private void loadGlyphTexture(int p_78257_1_) {
        this.bindTexture(this.getUnicodePageLocation(p_78257_1_));
    }

    /**
     * Render a single Unicode character at current (posX,posY) location using one of the /font/glyph_XX.png files...
     */
    private float renderUnicodeChar(char p_78277_1_, boolean p_78277_2_) {
        if (this.glyphWidth[p_78277_1_] == 0) {
            return 0.0F;
        } else {
            int i = p_78277_1_ / 256;
            this.loadGlyphTexture(i);
            int j = this.glyphWidth[p_78277_1_] >>> 4;
            int k = this.glyphWidth[p_78277_1_] & 15;
            float f = (float) j;
            float f1 = (float) (k + 1);
            float f2 = (float) (p_78277_1_ % 16 * 16) + f;
            float f3 = (float) ((p_78277_1_ & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = p_78277_2_ ? 1.0F : 0.0F;
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glTexCoord2f(f2 / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f5, this.posY, 0.0F);
            GL11.glTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX - f5, this.posY + 7.99F, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F + f5, this.posY, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, 0.0F);
            GL11.glEnd();
            return (f1 - f) / 2.0F + 1.0F;
        }
    }

    /**
     * Draws the specified string with a shadow.
     */

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return this.drawString(text, x, y, color, true);
    }

    /**
     * Draws the specified string.
     */

    @Override
    public int drawString(String text, float x, float y, int color) {
        return this.drawString(text, x, y, color, false);
    }

    /**
     * Draws the specified string.
     */
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        this.resetStyles();
        int i;

        if (dropShadow) {
            i = this.renderString(text, x + 1.0F, y + 1.0F, color, true);
            i = Math.max(i, this.renderString(text, x, y, color, false));
        } else {
            i = this.renderString(text, x, y, color, false);
        }

        return i;
    }

    /**
     * Apply Unicode Bidirectional Algorithm to string and return a new possibly reordered string for visual rendering.
     */
    private String bidiReorder(String p_147647_1_) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(p_147647_1_), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        } catch (ArabicShapingException var3) {
            return p_147647_1_;
        }
    }

    /**
     * Reset all style flag fields in the class to false; called at the start of string rendering
     */
    protected void resetStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
    }

    /**
     * Render a single line string at the current (posX,posY) and update posX
     */
    protected void renderStringAtPos(String p_78255_1_, boolean p_78255_2_) {
        for (int i = 0; i < p_78255_1_.length(); ++i) {
            char c0 = p_78255_1_.charAt(i);

            if (c0 == 167 && i + 1 < p_78255_1_.length()) {
                int l = "0123456789abcdefklmnor".indexOf(p_78255_1_.toLowerCase(Locale.ENGLISH).charAt(i + 1));

                if (l < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.italicStyle = false;

                    if (l < 0) {
                        l = 15;
                    }

                    if (p_78255_2_) {
                        l += 16;
                    }

                    int i1 = this.colorCode[l];

                    if (Config.isCustomColors()) {
                        i1 = CustomColors.getTextColor(l, i1);
                    }

                    this.textColor = i1;
                    GL11.glColor4f((float) (i1 >> 16) / 255.0F, (float) (i1 >> 8 & 255) / 255.0F, (float) (i1 & 255) / 255.0F, this.alpha);
                } else if (l == 16) {
                    this.randomStyle = true;
                } else if (l == 17) {
                    this.boldStyle = true;
                } else if (l == 20) {
                    this.italicStyle = true;
                } else {
                    resetStyles();
                    GL11.glColor4f(this.red, this.blue, this.green, this.alpha);
                }

                ++i;
            } else {
                int j = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(c0);

                if (this.randomStyle && j != -1) {
                    int k = this.getCharWidth(c0);
                    char c1;

                    do {
                        j = this.fontRandom.nextInt("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".length());
                        c1 = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".charAt(j);
                    } while (k != this.getCharWidth(c1));

                    c0 = c1;
                }

                float f1 = j != -1 && !this.unicodeFlag ? this.offsetBold : 0.5F;
                boolean flag = (c0 == 0 || j == -1 || this.unicodeFlag) && p_78255_2_;

                if (flag) {
                    this.posX -= f1;
                    this.posY -= f1;
                }

                float f = this.renderChar(c0, j, this.italicStyle);

                if (flag) {
                    this.posX += f1;
                    this.posY += f1;
                }

                if (this.boldStyle) {
                    this.posX += f1;

                    if (flag) {
                        this.posX -= f1;
                        this.posY -= f1;
                    }

                    this.renderChar(c0, j, this.italicStyle);
                    this.posX -= f1;

                    if (flag) {
                        this.posX += f1;
                        this.posY += f1;
                    }

                    f += f1;
                }

                this.posX += f;
            }
        }
    }

    /**
     * Render string either left or right aligned depending on bidiFlag
     */
    private void renderStringAligned(String text, int x, int y, int p_78274_4_, int color, boolean dropShadow) {
        if (this.bidiFlag) {
            int i = this.getStringWidth(this.bidiReorder(text));
            x = x + p_78274_4_ - i;
        }
        this.renderString(text, (float) x, (float) y, color, dropShadow);
    }

    /**
     * Render single line string by setting GL color, current (posX,posY), and calling renderStringAtPos()
     */
    protected int renderString(String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) return 0;

        if (this.bidiFlag) {
            text = this.bidiReorder(text);
        }

        if ((color & -67108864) == 0)
            color |= -16777216;

        if (dropShadow)
            color = (color & 16579836) >> 2 | color & -16777216;

        OGLUtils.enableBlending();
        this.red = (float) (color >> 16 & 255) / 255.0F;
        this.blue = (float) (color >> 8 & 255) / 255.0F;
        this.green = (float) (color & 255) / 255.0F;
        this.alpha = (float) (color >> 24 & 255) / 255.0F;
        GL11.glColor4f(this.red, this.blue, this.green, this.alpha);
        this.posX = x;
        this.posY = y;
        this.renderStringAtPos(text, dropShadow);
        GL11.glDisable(GL11.GL_BLEND);
        return (int) this.posX;
    }

    public int getStringWidth(String text) {
        return (int) getWidth(text);
    }

    /**
     * Returns the width of this string. Equivalent of FontMetrics.stringWidth(String s).
     */
    @Override
    public float getWidth(String text) {
        if (text == null) {
            return 0;
        } else {
            float f = 0.0F;
            boolean flag = false;

            for (int i = 0; i < text.length(); ++i) {
                char c0 = text.charAt(i);
                float f1 = this.getCharWidthFloat(c0);

                if (f1 < 0.0F && i < text.length() - 1) {
                    ++i;
                    c0 = text.charAt(i);

                    if (c0 != 108 && c0 != 76) {
                        if (c0 == 114 || c0 == 82) {
                            flag = false;
                        }
                    } else {
                        flag = true;
                    }

                    f1 = 0.0F;
                }

                f += f1;

                if (flag && f1 > 0.0F) {
                    f += this.unicodeFlag ? 1.0F : this.offsetBold;
                }
            }

            return Math.round(f);
        }
    }

    /**
     * Returns the width of this character as rendered.
     */
    public int getCharWidth(char character) {
        return Math.round(this.getCharWidthFloat(character));
    }

    protected float getCharWidthFloat(char p_getCharWidthFloat_1_) {
        if (p_getCharWidthFloat_1_ == 167) {
            return -1.0F;
        } else if (p_getCharWidthFloat_1_ != 32 && p_getCharWidthFloat_1_ != 160) {
            int i = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(p_getCharWidthFloat_1_);

            if (p_getCharWidthFloat_1_ > 0 && i != -1 && !this.unicodeFlag) {
                return this.charWidthFloat[i];
            } else if (this.glyphWidth[p_getCharWidthFloat_1_] != 0) {
                int j = this.glyphWidth[p_getCharWidthFloat_1_] >>> 4;
                int k = this.glyphWidth[p_getCharWidthFloat_1_] & 15;

                if (k > 7) {
                    k = 15;
                    j = 0;
                }

                ++k;
                return (float) ((k - j) / 2 + 1);
            } else {
                return 0.0F;
            }
        } else {
            return this.charWidthFloat[32];
        }
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < (float) width; k += j) {
            char c0 = text.charAt(k);
            float f1 = this.getCharWidthFloat(c0);

            if (flag) {
                flag = false;

                if (c0 != 108 && c0 != 76) {
                    if (c0 == 114 || c0 == 82) {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (f1 < 0.0F) {
                flag = true;
            } else {
                f += f1;

                if (flag1) {
                    ++f;
                }
            }

            if (f > (float) width) {
                break;
            }

            if (reverse) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    /**
     * Remove all newline characters from the end of the string
     */
    private String trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    /**
     * Splits and draws a String with wordwrap (maximum length is parameter k)
     */
    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        this.resetStyles();
        this.textColor = textColor;
        str = this.trimStringNewline(str);
        this.renderSplitString(str, x, y, wrapWidth, false);
    }

    /**
     * Perform actual work of rendering a multi-line string with wordwrap and with darker drop shadow color if flag is
     * set
     */
    private void renderSplitString(String str, int x, int y, int wrapWidth, boolean addShadow) {
        for (String s : this.listFormattedStringToWidth(str, wrapWidth)) {
            this.renderStringAligned(s, x, y, wrapWidth, this.textColor, addShadow);
            y += this.FONT_HEIGHT;
        }
    }

    /**
     * Returns the width of the wordwrapped String (maximum length is parameter k)
     */
    public int splitStringWidth(String p_78267_1_, int p_78267_2_) {
        return this.FONT_HEIGHT * this.listFormattedStringToWidth(p_78267_1_, p_78267_2_).size();
    }

    /**
     * Get unicodeFlag controlling whether strings should be rendered with Unicode fonts instead of the default.png
     * font.
     */
    public boolean getUnicodeFlag() {
        return this.unicodeFlag;
    }

    /**
     * Set unicodeFlag controlling whether strings should be rendered with Unicode fonts instead of the default.png
     * font.
     */
    public void setUnicodeFlag(boolean unicodeFlagIn) {
        this.unicodeFlag = unicodeFlagIn;
    }

    public void setBidiFlag(boolean bidiFlagIn) {
        this.bidiFlag = bidiFlagIn;
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Arrays.asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    /**
     * Inserts newline and formatting into a string to wrap it within the specified width.
     */
    String wrapFormattedStringToWidth(String str, int wrapWidth) {
        if (str.length() <= 1) {
            return str;
        } else {
            int i = this.sizeStringToWidth(str, wrapWidth);

            if (str.length() <= i) {
                return str;
            } else {
                String s = str.substring(0, i);
                char c0 = str.charAt(i);
                boolean flag = c0 == 32 || c0 == 10;
                String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
                return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
            }
        }
    }

    /**
     * Determines how many characters from the string will fit into the specified width.
     */
    private int sizeStringToWidth(String str, int wrapWidth) {
        int i = str.length();
        float f = 0.0F;
        int j = 0;
        int k = -1;

        for (boolean flag = false; j < i; ++j) {
            char c0 = str.charAt(j);

            switch (c0) {
                case '\n':
                    --j;
                    break;

                case ' ':
                    k = j;

                default:
                    f += (float) this.getCharWidth(c0);

                    if (flag) {
                        ++f;
                    }

                    break;

                case '\u00a7':
                    if (j < i - 1) {
                        ++j;
                        char c1 = str.charAt(j);

                        if (c1 != 108 && c1 != 76) {
                            if (c1 == 114 || c1 == 82 || isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
            }

            if (c0 == 10) {
                ++j;
                k = j;
                break;
            }

            if (Math.round(f) > wrapWidth) {
                break;
            }
        }

        return j != i && k != -1 && k < j ? k : j;
    }

    public int getColorCode(char character) {
        int i = "0123456789abcdef".indexOf(character);

        if (i >= 0 && i < this.colorCode.length) {
            int j = this.colorCode[i];

            if (Config.isCustomColors()) {
                j = CustomColors.getTextColor(i, j);
            }

            return j;
        } else {
            return 16777215;
        }
    }

    protected void bindTexture(ResourceLocation p_bindTexture_1_) {
        this.renderEngine.bindTexture(p_bindTexture_1_);
    }

    protected InputStream getResourceInputStream(ResourceLocation p_getResourceInputStream_1_) throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(p_getResourceInputStream_1_).getInputStream();
    }
}
