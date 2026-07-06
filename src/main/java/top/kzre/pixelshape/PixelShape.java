package top.kzre.pixelshape;

/**
 * 像素形状生成器静态入口，返回 float 数组。
 */
public final class PixelShape {
    private static final Spec impl = new PixelShapeImpl();

    private PixelShape() {}

    public static float[] circle(int size, float radius, MaskType maskType) {
        return impl.circle(size, radius, maskType);
    }

    public static float[] ellipse(int size, float radiusX, float radiusY, float angle, MaskType maskType) {
        return impl.ellipse(size, radiusX, radiusY, angle, maskType);
    }

    public static float[] polygon(int size, float radius, int sides, float angle, MaskType maskType) {
        return impl.polygon(size, radius, sides, angle, maskType);
    }

    public static float[] star(int size, float radius, int points, float innerRatio, MaskType maskType) {
        return impl.star(size, radius, points, innerRatio, maskType);
    }

    public static float[] fromImage(int size, float[] imageData, int imgW, int imgH,
                                    float scaleX, float scaleY, float angle) {
        return impl.fromImage(size, imageData, imgW, imgH, scaleX, scaleY, angle);
    }

    public static float[] splatter(int size, float radius, int count, float spotSize,
                                   MaskType maskType, long seed) {
        return impl.splatter(size, radius, count, spotSize, maskType, seed);
    }

    public static float[] rectangle(int size, float halfWidth, float halfHeight,
                                    float cornerRadius, float angle, MaskType maskType) {
        return impl.rectangle(size, halfWidth, halfHeight, cornerRadius, angle, maskType);
    }

    public static float[] diamond(int size, float halfWidth, float halfHeight,
                                  float angle, MaskType maskType) {
        return impl.diamond(size, halfWidth, halfHeight, angle, maskType);
    }

    public static float[] trapezoid(int size, float topHalfWidth, float bottomHalfWidth,
                                    float halfHeight, float angle, MaskType maskType) {
        return impl.trapezoid(size, topHalfWidth, bottomHalfWidth, halfHeight, angle, maskType);
    }

    public static float[] teardrop(int size, float radius, float tailLength,
                                   float angle, MaskType maskType) {
        return impl.teardrop(size, radius, tailLength, angle, maskType);
    }

    public static float[] crescent(int size, float outerRadius, float innerRadius,
                                   float innerOffset, float angle, MaskType maskType) {
        return impl.crescent(size, outerRadius, innerRadius, innerOffset, angle, maskType);
    }

    public static float[] path(int size, String svgPath, float scaleX, float scaleY,
                               float angle, MaskType maskType) {
        return impl.path(size, svgPath, scaleX, scaleY, angle, maskType);
    }

    public static float[] outline(int size, ShapeType baseShape, float[] shapeParams,
                                  float strokeWidth, float blur, MaskType maskType) {
        return impl.outline(size, baseShape, shapeParams, strokeWidth, blur, maskType);
    }

    public static float[] linearGradient(int size, float angle, float startAlpha, float endAlpha) {
        return impl.linearGradient(size, angle, startAlpha, endAlpha);
    }

    public static float[] noise(int size, String noiseType, float frequency,
                                float amplitude, float persistence, long seed, MaskType maskType) {
        return impl.noise(size, noiseType, frequency, amplitude, persistence, seed, maskType);
    }

    public interface Spec {
        float[] circle(int size, float radius, MaskType maskType);
        float[] ellipse(int size, float radiusX, float radiusY, float angle, MaskType maskType);
        float[] polygon(int size, float radius, int sides, float angle, MaskType maskType);
        float[] star(int size, float radius, int points, float innerRatio, MaskType maskType);
        float[] fromImage(int size, float[] imageData, int imgW, int imgH,
                          float scaleX, float scaleY, float angle);
        float[] splatter(int size, float radius, int count, float spotSize,
                         MaskType maskType, long seed);
        float[] rectangle(int size, float halfWidth, float halfHeight,
                          float cornerRadius, float angle, MaskType maskType);
        float[] diamond(int size, float halfWidth, float halfHeight,
                        float angle, MaskType maskType);
        float[] trapezoid(int size, float topHalfWidth, float bottomHalfWidth,
                          float halfHeight, float angle, MaskType maskType);
        float[] teardrop(int size, float radius, float tailLength,
                         float angle, MaskType maskType);
        float[] crescent(int size, float outerRadius, float innerRadius,
                         float innerOffset, float angle, MaskType maskType);
        float[] path(int size, String svgPath, float scaleX, float scaleY,
                     float angle, MaskType maskType);
        float[] outline(int size, ShapeType baseShape, float[] shapeParams,
                        float strokeWidth, float blur, MaskType maskType);
        float[] linearGradient(int size, float angle, float startAlpha, float endAlpha);
        float[] noise(int size, String noiseType, float frequency,
                      float amplitude, float persistence, long seed, MaskType maskType);
    }
}