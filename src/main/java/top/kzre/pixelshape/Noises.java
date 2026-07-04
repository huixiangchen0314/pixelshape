package top.kzre.pixelshape;

import java.util.Random;

/**
 * 噪声生成工具，提供 Perlin 二维噪声及排列表创建。
 */
public final class Noises {

    private Noises() {}

    /**
     * 根据种子创建一个长度为 512 的排列表，供 perlin2D 使用。
     */
    public static int[] createPermutation(long seed) {
        int[] perm = new int[256];
        for (int i = 0; i < 256; i++) {
            perm[i] = i;
        }
        shuffle(perm, new Random(seed));
        int[] p = new int[512];
        for (int i = 0; i < 512; i++) {
            p[i] = perm[i % 256];
        }
        return p;
    }

    /**
     * Perlin 二维噪声。
     * @param x x 坐标（通常已缩放）
     * @param y y 坐标
     * @param p 排列表，由 createPermutation 生成
     * @return 噪声值，范围约 [-1, 1]
     */
    public static double perlin2D(double x, double y, int[] p) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);
        double u = fade(xf);
        double v = fade(yf);
        int aa = p[p[xi] + yi];
        int ab = p[p[xi] + yi + 1];
        int ba = p[p[xi + 1] + yi];
        int bb = p[p[xi + 1] + yi + 1];
        double x1 = Utils.lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u);
        double x2 = Utils.lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u);
        return Utils.lerp(x1, x2, v);
    }

    // ---- 内部辅助 ----

    private static void shuffle(int[] arr, Random rand) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double grad(int hash, double x, double y) {
        int h = hash & 3;
        double u = h < 2 ? x : y;
        double v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}