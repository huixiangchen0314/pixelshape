package top.kzre.pixelshape;

/**
 * 像素形状生成所需的通用数学与采样工具。
 */
public final class Utils {

    private Utils() {}

    /**
     * 根据点到边缘的距离和蒙版类型计算不透明度。
     * @param dist     当前点到形状边缘的距离（非负）
     * @param radius   形状的特征半径（用于衰减计算）
     * @param maskType 蒙版类型
     * @return 不透明度，范围 [0, 1]
     */
    public static double applyMask(double dist, double radius, MaskType maskType) {
        if (dist >= radius) return 0.0;
        switch (maskType) {
            case HARD:
                return 1.0;
            case SOFT:
                return 1.0 - dist / radius;
            case GAUSSIAN: {
                double x = dist / radius;
                return Math.exp(-3.0 * x * x);
            }
            default:
                return 0.0;
        }
    }

    /** 线性插值 */
    public static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    /** 将值限制在 [0, 1] 范围内 */
    public static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /** 安全读取图像数组中的像素值，越界返回 0.0 */
    public static double getPixel(double[] img, int w, int h, int x, int y) {
        if (x < 0 || x >= w || y < 0 || y >= h) return 0.0;
        return img[y * w + x];
    }

    /** 双线性插值采样 */
    public static double bilinearSample(double[] img, int w, int h, double x, double y) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        double fx = x - x0;
        double fy = y - y0;
        double v00 = getPixel(img, w, h, x0, y0);
        double v10 = getPixel(img, w, h, x1, y0);
        double v01 = getPixel(img, w, h, x0, y1);
        double v11 = getPixel(img, w, h, x1, y1);
        return lerp(lerp(v00, v10, fx), lerp(v01, v11, fx), fy);
    }
}