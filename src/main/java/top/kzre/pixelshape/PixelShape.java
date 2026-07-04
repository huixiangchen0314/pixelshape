package top.kzre.pixelshape;

/**
 * 像素形状生成器静态入口，提供所有 dab 形状的灰度遮罩。
 * 每个方法返回 size×size 的 double 数组，行主序，值域 [0,1]。
 */
public final class PixelShape {
    private static final Spec impl = new PixelShapeImpl();

    private PixelShape() {}

    public static double[] circle(int size, double radius, MaskType maskType) {
        return impl.circle(size, radius, maskType);
    }

    public static double[] ellipse(int size, double radiusX, double radiusY, double angle, MaskType maskType) {
        return impl.ellipse(size, radiusX, radiusY, angle, maskType);
    }

    public static double[] polygon(int size, double radius, int sides, double angle, MaskType maskType) {
        return impl.polygon(size, radius, sides, angle, maskType);
    }

    public static double[] star(int size, double radius, int points, double innerRatio, MaskType maskType) {
        return impl.star(size, radius, points, innerRatio, maskType);
    }

    public static double[] fromImage(int size, double[] imageData, int imgW, int imgH,
                                     double scaleX, double scaleY, double angle) {
        return impl.fromImage(size, imageData, imgW, imgH, scaleX, scaleY, angle);
    }

    public static double[] splatter(int size, double radius, int count, double spotSize,
                                    MaskType maskType, long seed) {
        return impl.splatter(size, radius, count, spotSize, maskType, seed);
    }

    public static double[] rectangle(int size, double halfWidth, double halfHeight,
                                     double cornerRadius, double angle, MaskType maskType) {
        return impl.rectangle(size, halfWidth, halfHeight, cornerRadius, angle, maskType);
    }

    public static double[] diamond(int size, double halfWidth, double halfHeight,
                                   double angle, MaskType maskType) {
        return impl.diamond(size, halfWidth, halfHeight, angle, maskType);
    }

    public static double[] trapezoid(int size, double topHalfWidth, double bottomHalfWidth,
                                     double halfHeight, double angle, MaskType maskType) {
        return impl.trapezoid(size, topHalfWidth, bottomHalfWidth, halfHeight, angle, maskType);
    }

    public static double[] teardrop(int size, double radius, double tailLength,
                                    double angle, MaskType maskType) {
        return impl.teardrop(size, radius, tailLength, angle, maskType);
    }

    public static double[] crescent(int size, double outerRadius, double innerRadius,
                                    double innerOffset, double angle, MaskType maskType) {
        return impl.crescent(size, outerRadius, innerRadius, innerOffset, angle, maskType);
    }

    public static double[] path(int size, String svgPath, double scaleX, double scaleY,
                                double angle, MaskType maskType) {
        return impl.path(size, svgPath, scaleX, scaleY, angle, maskType);
    }

    public static double[] outline(int size, ShapeType baseShape, double[] shapeParams,
                                   double strokeWidth, double blur, MaskType maskType) {
        return impl.outline(size, baseShape, shapeParams, strokeWidth, blur, maskType);
    }

    public static double[] linearGradient(int size, double angle, double startAlpha, double endAlpha) {
        return impl.linearGradient(size, angle, startAlpha, endAlpha);
    }

    public static double[] noise(int size, String noiseType, double frequency,
                                 double amplitude, double persistence, long seed, MaskType maskType) {
        return impl.noise(size, noiseType, frequency, amplitude, persistence, seed, maskType);
    }

    public interface Spec {
        double[] circle(int size, double radius, MaskType maskType);
        double[] ellipse(int size, double radiusX, double radiusY, double angle, MaskType maskType);
        double[] polygon(int size, double radius, int sides, double angle, MaskType maskType);
        double[] star(int size, double radius, int points, double innerRatio, MaskType maskType);
        double[] fromImage(int size, double[] imageData, int imgW, int imgH,
                           double scaleX, double scaleY, double angle);
        double[] splatter(int size, double radius, int count, double spotSize,
                          MaskType maskType, long seed);

        double[] rectangle(int size, double halfWidth, double halfHeight,
                           double cornerRadius, double angle, MaskType maskType);
        double[] diamond(int size, double halfWidth, double halfHeight,
                         double angle, MaskType maskType);
        double[] trapezoid(int size, double topHalfWidth, double bottomHalfWidth,
                           double halfHeight, double angle, MaskType maskType);
        double[] teardrop(int size, double radius, double tailLength,
                          double angle, MaskType maskType);
        double[] crescent(int size, double outerRadius, double innerRadius,
                          double innerOffset, double angle, MaskType maskType);

        // 自定义路径（使用 AWT Shape 解析）
        double[] path(int size, String svgPath, double scaleX, double scaleY,
                      double angle, MaskType maskType);

        // 描边/轮廓笔尖
        double[] outline(int size, ShapeType baseShape, double[] shapeParams,
                         double strokeWidth, double blur, MaskType maskType);

        // 纯渐变
        double[] linearGradient(int size, double angle, double startAlpha, double endAlpha);

        // 噪声纹理
        double[] noise(int size, String noiseType, double frequency,
                       double amplitude, double persistence, long seed, MaskType maskType);

    }
}