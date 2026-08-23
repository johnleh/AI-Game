package org.example.aigame.main;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class PortraitGenerator {

    static final int W = 128, H = 128;

    static int[] c(int r, int g, int b) { return new int[]{r, g, b}; }

    static int[] lerpColor(int[] a, int[] b, double t) {
        return new int[]{
                (int) Math.round(a[0] + (b[0] - a[0]) * t),
                (int) Math.round(a[1] + (b[1] - a[1]) * t),
                (int) Math.round(a[2] + (b[2] - a[2]) * t)
        };
    }

    static int clamp255(double v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return (int) Math.round(v);
    }

    static int[] clampColor(int[] col) {
        return new int[]{clamp255(col[0]), clamp255(col[1]), clamp255(col[2])};
    }

    static int[] addColor(int[] a, int delta) {
        return new int[]{clamp255(a[0] + delta), clamp255(a[1] + delta), clamp255(a[2] + delta)};
    }

    static Color toColor(int[] rgb) {
        return new Color(clamp255(rgb[0]), clamp255(rgb[1]), clamp255(rgb[2]));
    }

    static void setPixel(BufferedImage img, int x, int y, int[] rgb) {
        if (x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight()) return;
        int r = clamp255(rgb[0]), g = clamp255(rgb[1]), b = clamp255(rgb[2]);
        img.setRGB(x, y, (r << 16) | (g << 8) | b);
    }

    static double[] gaussianKernel1D(double sigma) {
        int radius = Math.max(1, (int) Math.ceil(sigma * 3));
        int size = radius * 2 + 1;
        double[] kernel = new double[size];
        double sum = 0;
        for (int i = -radius; i <= radius; i++) {
            double v = Math.exp(-(i * i) / (2.0 * sigma * sigma));
            kernel[i + radius] = v;
            sum += v;
        }
        for (int i = 0; i < size; i++) kernel[i] /= sum;
        return kernel;
    }

    static BufferedImage gaussianBlurRGB(BufferedImage src, double sigma) {
        int w = src.getWidth(), h = src.getHeight();
        double[] kernel = gaussianKernel1D(sigma);
        int radius = kernel.length / 2;

        double[][] r = new double[h][w], g = new double[h][w], b = new double[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                r[y][x] = (rgb >> 16) & 0xFF;
                g[y][x] = (rgb >> 8) & 0xFF;
                b[y][x] = rgb & 0xFF;
            }
        }

        double[][] tr = new double[h][w], tg = new double[h][w], tb = new double[h][w];
        // horizontal pass
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double sr = 0, sg = 0, sb = 0;
                for (int k = -radius; k <= radius; k++) {
                    int xx = Math.min(w - 1, Math.max(0, x + k));
                    double wgt = kernel[k + radius];
                    sr += r[y][xx] * wgt;
                    sg += g[y][xx] * wgt;
                    sb += b[y][xx] * wgt;
                }
                tr[y][x] = sr; tg[y][x] = sg; tb[y][x] = sb;
            }
        }
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                double sr = 0, sg = 0, sb = 0;
                for (int k = -radius; k <= radius; k++) {
                    int yy = Math.min(h - 1, Math.max(0, y + k));
                    double wgt = kernel[k + radius];
                    sr += tr[yy][x] * wgt;
                    sg += tg[yy][x] * wgt;
                    sb += tb[yy][x] * wgt;
                }
                out.setRGB(x, y, (clamp255(sr) << 16) | (clamp255(sg) << 8) | clamp255(sb));
            }
        }
        return out;
    }

    static double[][] gaussianBlurGray(double[][] src, double sigma) {
        int h = src.length, w = src[0].length;
        double[] kernel = gaussianKernel1D(sigma);
        int radius = kernel.length / 2;

        double[][] tmp = new double[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double s = 0;
                for (int k = -radius; k <= radius; k++) {
                    int xx = Math.min(w - 1, Math.max(0, x + k));
                    s += src[y][xx] * kernel[k + radius];
                }
                tmp[y][x] = s;
            }
        }
        double[][] out = new double[h][w];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                double s = 0;
                for (int k = -radius; k <= radius; k++) {
                    int yy = Math.min(h - 1, Math.max(0, y + k));
                    s += tmp[yy][x] * kernel[k + radius];
                }
                out[y][x] = s;
            }
        }
        return out;
    }

    static BufferedImage sharpen3x3(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        int[][] kernel = {
                {-2, -2, -2},
                {-2, 32, -2},
                {-2, -2, -2}
        };
        int scale = 16;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double sr = 0, sg = 0, sb = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int xx = Math.min(w - 1, Math.max(0, x + kx));
                        int yy = Math.min(h - 1, Math.max(0, y + ky));
                        int rgb = src.getRGB(xx, yy);
                        double wgt = kernel[ky + 1][kx + 1];
                        sr += ((rgb >> 16) & 0xFF) * wgt;
                        sg += ((rgb >> 8) & 0xFF) * wgt;
                        sb += (rgb & 0xFF) * wgt;
                    }
                }
                int r = clamp255(sr / scale), g = clamp255(sg / scale), b = clamp255(sb / scale);
                out.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    public static void makePortrait(
            long seed,
            String filename,
            int[] skinBase, int[] skinShadow, int[] skinHighlight, int[] skinMid,
            int[] hairBase, int[] hairHi, int[] hairShadow,
            int[] topBase, int[] topShadow, int[] topHi,
            int[] bgTop, int[] bgBot,
            String hairStyle,      // "short" or "long"
            int[] eyeIris,
            int[] mouthCol,
            int[] warmGlow
    ) throws IOException {

        Random rnd = new Random(seed);
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < H; y++) {
            double t = y / (double) (H - 1);
            int[] col = lerpColor(bgTop, bgBot, t);
            for (int x = 0; x < W; x++) {
                double n = (rnd.nextDouble() * 16.0) - 8.0; // uniform(-8, 8)
                setPixel(img, x, y, addColor(col, (int) Math.round(n)));
            }
        }

        double[][] glow = new double[H][W];
        {
            BufferedImage glowImg = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D gg = glowImg.createGraphics();
            gg.setColor(new Color(90, 90, 90));
            gg.fillOval(24, 10, 104 - 24, 90 - 10);
            gg.dispose();
            for (int y = 0; y < H; y++)
                for (int x = 0; x < W; x++)
                    glow[y][x] = glowImg.getRaster().getSample(x, y, 0);
            glow = gaussianBlurGray(glow, 18 / 3.0); // approximate PIL radius->sigma scaling
        }
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double gAmt = (glow[y][x] / 255.0) * 0.35;
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                r = clamp255(r * (1 - gAmt) + warmGlow[0] * gAmt);
                g = clamp255(g * (1 - gAmt) + warmGlow[1] * gAmt);
                b = clamp255(b * (1 - gAmt) + warmGlow[2] * gAmt);
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // ---- shoulders / top garment ----
        fillPoly(g2, topBase, 18,128, 30,92, 50,84, 78,84, 98,92, 112,128);
        fillPoly(g2, topShadow, 18,128, 30,92, 44,90, 40,128);
        fillPoly(g2, topHi, 84,86, 98,92, 108,118, 96,112);
        if (!hairStyle.equals("long")) {
            fillPoly(g2, addColor(skinHighlight, 20), 56,86, 64,96, 72,86, 64,80);
        }

        // ---- neck ----
        fillPoly(g2, skinShadow, 56,78, 72,78, 74,92, 54,92);
        fillPoly(g2, skinMid, 58,78, 70,78, 70,88, 58,88);

        // ---- head ----
        g2.setColor(toColor(skinBase));
        g2.fill(new Ellipse2D.Double(40, 34, 48, 50));
        fillPoly(g2, skinBase, 44,66, 46,78, 64,84, 82,78, 84,66, 78,74, 64,78, 50,74);

        // ---- shading ----
        fillPoly(g2, skinShadow, 40,40, 48,34, 48,76, 42,70, 40,55);
        fillPoly(g2, skinHighlight, 74,38, 86,46, 86,68, 78,76, 72,60);
        fillPoly(g2, skinMid, 48,58, 58,64, 62,74, 50,74, 46,66);
        g2.setColor(toColor(skinHighlight));
        g2.fill(new Ellipse2D.Double(50, 36, 24, 12));

        // ---- ears ----
        g2.setColor(toColor(skinMid));
        g2.fill(new Ellipse2D.Double(37, 54, 8, 14));
        g2.fill(new Ellipse2D.Double(83, 54, 8, 14));

        // ---- hair ----
        if (hairStyle.equals("short")) {
            g2.setColor(toColor(hairBase));
            g2.fill(new Arc2D.Double(40, 24, 48, 42, 180, 180, Arc2D.PIE));
            fillPoly(g2, hairBase,
                    40,46, 40,34, 50,26, 64,24, 78,26, 88,34, 88,48,
                    80,40, 70,36, 64,34, 58,36, 48,40);
            g2.setColor(toColor(hairHi));
            g2.setStroke(new BasicStroke(2));
            drawLine(g2, 46,34, 52,40);
            drawLine(g2, 56,28, 60,36);
            drawLine(g2, 70,30, 76,38);
            drawLine(g2, 78,36, 84,44);
            fillPoly(g2, hairBase, 41,46, 46,46, 44,60, 40,58);
            fillPoly(g2, hairBase, 87,46, 82,46, 84,60, 88,58);
        } else {
            g2.setColor(toColor(hairBase));
            g2.fill(new Arc2D.Double(38, 20, 52, 44, 180, 180, Arc2D.PIE));
            fillPoly(g2, hairBase,
                    38,46, 38,24, 50,18, 64,16, 78,18, 90,24, 90,48,
                    80,38, 70,32, 64,30, 58,32, 48,38);
            fillPoly(g2, hairBase, 36,44, 46,44, 48,96, 34,100, 30,70);
            fillPoly(g2, hairBase, 92,44, 82,44, 80,96, 94,100, 98,70);
            fillPoly(g2, hairShadow, 36,60, 44,58, 44,92, 36,94);
            fillPoly(g2, hairShadow, 92,60, 84,58, 84,92, 92,94);
            g2.setColor(toColor(hairHi));
            g2.setStroke(new BasicStroke(2));
            drawLine(g2, 44,28, 50,36);
            drawLine(g2, 56,22, 60,30);
            drawLine(g2, 70,24, 76,32);
            drawLine(g2, 78,30, 84,38);
            drawLine(g2, 34,60, 40,80);
            drawLine(g2, 88,60, 94,80);
        }

        // ---- eyebrows ----
        g2.setColor(toColor(addColor(hairBase, -10)));
        g2.setStroke(new BasicStroke(2));
        drawLine(g2, 48,52, 58,50);
        drawLine(g2, 70,50, 80,52);

        // ---- eyes ----
        int[] eyeWhite = c(222, 214, 198);
        int[] eyeDark = c(30, 28, 26);
        g2.setColor(toColor(eyeWhite));
        g2.fill(new Ellipse2D.Double(49, 54, 10, 6));
        g2.setColor(toColor(eyeIris));
        g2.fill(new Ellipse2D.Double(52, 55, 5, 5));
        g2.setColor(toColor(eyeDark));
        g2.fill(new Ellipse2D.Double(53.5, 56, 2.5, 2.5));
        g2.setColor(toColor(c(70,50,42)));
        g2.setStroke(new BasicStroke(1));
        drawLine(g2, 49,54, 59,53.5);

        g2.setColor(toColor(eyeWhite));
        g2.fill(new Ellipse2D.Double(69, 54, 10, 6));
        g2.setColor(toColor(eyeIris));
        g2.fill(new Ellipse2D.Double(71, 55, 5, 5));
        g2.setColor(toColor(eyeDark));
        g2.fill(new Ellipse2D.Double(72, 56, 2.5, 2.5));
        g2.setColor(toColor(c(70,50,42)));
        drawLine(g2, 69,53.5, 79,54);

        g2.setColor(toColor(skinShadow));
        drawLine(g2, 50,61, 56,62);
        drawLine(g2, 72,62, 78,61);

        // ---- nose ----
        g2.setColor(toColor(skinShadow));
        g2.setStroke(new BasicStroke(2));
        drawLine(g2, 64,58, 62,68);
        g2.setStroke(new BasicStroke(1));
        drawLine(g2, 62,68, 67,70);
        g2.setColor(toColor(skinMid));
        g2.fill(new Ellipse2D.Double(61, 69, 3, 2));

        // ---- mouth ----
        g2.setColor(toColor(mouthCol));
        g2.setStroke(new BasicStroke(2));
        drawLine(g2, 56,75, 64,77);
        drawLine(g2, 64,77, 73,74);
        g2.setColor(toColor(skinShadow));
        g2.setStroke(new BasicStroke(1));
        drawLine(g2, 57,78, 71,77);
        setPixel(img, 55, 74, skinShadow);
        setPixel(img, 74, 73, skinShadow);

        g2.dispose();

        // ---- per-pixel noise ----
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                double n = rnd.nextGaussian() * 6.0;
                r = clamp255(r + n); g = clamp255(g + n); b = clamp255(b + n);
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        // ---- soft blur + sharpen (painterly brushed look) ----
        img = gaussianBlurRGB(img, 0.6);
        img = sharpen3x3(img);

        // ---- vignette ----
        double[][] vign;
        {
            BufferedImage vignImg = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D vg = vignImg.createGraphics();
            vg.setColor(Color.WHITE);
            vg.fillOval(-20, -20, W + 40, H + 40);
            vg.dispose();
            double[][] raw = new double[H][W];
            for (int y = 0; y < H; y++)
                for (int x = 0; x < W; x++)
                    raw[y][x] = vignImg.getRaster().getSample(x, y, 0);
            vign = gaussianBlurGray(raw, 10.0); // radius 30 -> sigma approx
        }
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double vAmt = 0.55 + 0.45 * (vign[y][x] / 255.0);
                int rgb = img.getRGB(x, y);
                int r = (int) ((( rgb >> 16) & 0xFF) * vAmt);
                int g = (int) ((( rgb >> 8) & 0xFF) * vAmt);
                int b = (int) ((rgb & 0xFF) * vAmt);
                img.setRGB(x, y, (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b));
            }
        }

        File outDir = new File("src/main/resources/assets/portraits");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        ImageIO.write(img, "png", new File(outDir, filename));
    }

    // ---------- drawing helpers ----------

    static void fillPoly(Graphics2D g2, int[] rgb, double... coords) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(coords[0], coords[1]);
        for (int i = 2; i < coords.length; i += 2) {
            path.lineTo(coords[i], coords[i + 1]);
        }
        path.closePath();
        g2.setColor(toColor(rgb));
        g2.fill(path);
    }

    static void drawLine(Graphics2D g2, double x1, double y1, double x2, double y2) {
        g2.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            args = new String[] {
                    "7", "person_portrait.png",
                    "167,109,55", "104,64,34", "196,142,88", "140,90,48",
                    "24,153,163", "60,200,210", "16,110,118",
                    "225,205,30", "170,150,20", "245,230,90",
                    "44,60,62", "70,66,50",
                    "short",
                    "90,70,50", "150,82,40", "150,120,60"
            };
        }

        if (args.length != 17) {
            System.err.println("Usage: <seed> <filename> <13 colors as r,g,b> <hairStyle> <3 colors as r,g,b>");
            System.err.println("Expected 17 args, got " + args.length);
            return;
        }

        int i = 0;
        int seed = Integer.parseInt(args[i++]);
        String filename = args[i++];

        int[] color1 = parseColor(args[i++]);
        int[] color2 = parseColor(args[i++]);
        int[] color3 = parseColor(args[i++]);
        int[] color4 = parseColor(args[i++]);
        int[] color5 = parseColor(args[i++]);
        int[] color6 = parseColor(args[i++]);
        int[] color7 = parseColor(args[i++]);
        int[] color8 = parseColor(args[i++]);
        int[] color9 = parseColor(args[i++]);
        int[] color10 = parseColor(args[i++]);
        int[] color11 = parseColor(args[i++]);
        int[] color12 = parseColor(args[i++]);
        int[] color13 = parseColor(args[i++]);

        String hairStyle = args[i++];

        int[] color14 = parseColor(args[i++]);
        int[] color15 = parseColor(args[i++]);
        int[] color16 = parseColor(args[i++]);

        makePortrait(
                seed, filename,
                color1, color2, color3, color4,
                color5, color6, color7,
                color8, color9, color10,
                color11, color12,
                hairStyle,
                color14, color15, color16
        );

        System.out.println("done");
    }

    private static int[] parseColor(String arg) {
        String[] parts = arg.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Color arg must be 'r,g,b': " + arg);
        }
        int r = Integer.parseInt(parts[0].trim());
        int g = Integer.parseInt(parts[1].trim());
        int b = Integer.parseInt(parts[2].trim());
        return c(r, g, b);
    }
}