package top.kzre.pixelshape;

/**
 * 像素形状生成所需的通用数学与采样工具（float 精度）。
 */
public final class Utils {

    private Utils() {}

    public static float applyMask(float dist, float radius, MaskType maskType) {
        if (dist >= radius) return 0.0f;
        switch (maskType) {
            case HARD:
                return 1.0f;
            case SOFT:
                return 1.0f - dist / radius;
            case GAUSSIAN: {
                float x = dist / radius;
                return (float) Math.exp(-3.0 * x * x);
            }
            default:
                return 0.0f;
        }
    }

    public static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    public static float clamp01(float value) {
        if (value < 0.0f) return 0.0f;
        if (value > 1.0f) return 1.0f;
        return value;
    }

    public static float getPixel(float[] img, int w, int h, int x, int y) {
        if (x < 0 || x >= w || y < 0 || y >= h) return 0.0f;
        return img[y * w + x];
    }

    public static float bilinearSample(float[] img, int w, int h, float x, float y) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        float fx = x - x0;
        float fy = y - y0;
        float v00 = getPixel(img, w, h, x0, y0);
        float v10 = getPixel(img, w, h, x1, y0);
        float v01 = getPixel(img, w, h, x0, y1);
        float v11 = getPixel(img, w, h, x1, y1);
        return lerp(lerp(v00, v10, fx), lerp(v01, v11, fx), fy);
    }
}