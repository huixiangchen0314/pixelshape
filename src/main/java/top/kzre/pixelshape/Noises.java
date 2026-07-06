package top.kzre.pixelshape;

import java.util.Random;

/**
 * 噪声生成工具（float 精度）。
 */
public final class Noises {

    private Noises() {}

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

    public static float perlin2D(float x, float y, int[] p) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        float xf = x - (float) Math.floor(x);
        float yf = y - (float) Math.floor(y);
        float u = fade(xf);
        float v = fade(yf);
        int aa = p[p[xi] + yi];
        int ab = p[p[xi] + yi + 1];
        int ba = p[p[xi + 1] + yi];
        int bb = p[p[xi + 1] + yi + 1];
        float x1 = Utils.lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u);
        float x2 = Utils.lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u);
        return Utils.lerp(x1, x2, v);
    }

    private static void shuffle(int[] arr, Random rand) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    private static float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static float grad(int hash, float x, float y) {
        int h = hash & 3;
        float u = h < 2 ? x : y;
        float v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}