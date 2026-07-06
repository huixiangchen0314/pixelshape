package top.kzre.pixelshape;

import java.awt.geom.Path2D;
import java.util.Random;

/**
 * 像素形状生成器的默认实现（float 精度）。
 */
public class PixelShapeImpl implements PixelShape.Spec {

    @Override
    public float[] circle(int size, float radius, MaskType maskType) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                data[y * size + x] = Utils.applyMask(dist, radius, maskType);
            }
        }
        return data;
    }

    @Override
    public float[] ellipse(int size, float radiusX, float radiusY, float angle, MaskType maskType) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        float cosA = (float) Math.cos(-angle);
        float sinA = (float) Math.sin(-angle);
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float rx = dx * cosA + dy * sinA;
                float ry = -dx * sinA + dy * cosA;
                float nx = rx / radiusX;
                float ny = ry / radiusY;
                float dist = (float) Math.sqrt(nx * nx + ny * ny);
                data[y * size + x] = Utils.applyMask(dist, 1.0f, maskType);
            }
        }
        return data;
    }

    @Override
    public float[] polygon(int size, float radius, int sides, float angle, MaskType maskType) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        float halfSector = (float) Math.PI / sides;
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float a = (float) Math.atan2(dy, dx) - angle;
                a = (a + (float) Math.PI + (float) Math.PI) % (2.0f * (float) Math.PI);
                float sector = a % (2 * halfSector);
                float r;
                if (sector < halfSector) {
                    r = radius / (float) Math.cos(sector - halfSector);
                } else {
                    r = radius / (float) Math.cos(2 * halfSector - sector);
                }
                if (dist <= r) {
                    data[y * size + x] = Utils.applyMask(dist, r, maskType);
                } else {
                    data[y * size + x] = 0.0f;
                }
            }
        }
        return data;
    }

    @Override
    public float[] star(int size, float radius, int points, float innerRatio, MaskType maskType) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        float innerRadius = radius * innerRatio;
        float halfStep = (float) Math.PI / points;
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float a = (float) Math.atan2(dy, dx);
                float sector = (a % (2 * halfStep) + 2 * halfStep) % (2 * halfStep);
                float t = sector / halfStep;
                float r;
                if (t < 1.0f) {
                    r = radius + (innerRadius - radius) * t;
                } else {
                    r = innerRadius + (radius - innerRadius) * (t - 1.0f);
                }
                if (dist <= r) {
                    data[y * size + x] = Utils.applyMask(dist, r, maskType);
                } else {
                    data[y * size + x] = 0.0f;
                }
            }
        }
        return data;
    }

    @Override
    public float[] fromImage(int size, float[] imageData, int imgW, int imgH,
                             float scaleX, float scaleY, float angle) {
        float[] data = new float[size * size];
        if (imageData == null || imgW <= 0 || imgH <= 0) {
            return data;
        }
        float center = size / 2.0f;
        float cosA = (float) Math.cos(angle);
        float sinA = (float) Math.sin(angle);
        float invScaleX = 1.0f / scaleX;
        float invScaleY = 1.0f / scaleY;
        float srcCx = imgW / 2.0f;
        float srcCy = imgH / 2.0f;
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float rx = dx * cosA - dy * sinA;
                float ry = dx * sinA + dy * cosA;
                float sx = rx * invScaleX + srcCx;
                float sy = ry * invScaleY + srcCy;
                data[y * size + x] = Utils.bilinearSample(imageData, imgW, imgH, sx, sy);
            }
        }
        return data;
    }

    @Override
    public float[] splatter(int size, float radius, int count, float spotSize,
                            MaskType maskType, long seed) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        Random rng = new Random(seed);
        float[] spotsX = new float[count];
        float[] spotsY = new float[count];
        float spotRadius = radius * spotSize;
        for (int i = 0; i < count; i++) {
            float a = 2.0f * (float) Math.PI * rng.nextFloat();
            float r = radius * (float) Math.sqrt(rng.nextFloat());
            spotsX[i] = center + r * (float) Math.cos(a);
            spotsY[i] = center + r * (float) Math.sin(a);
        }
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float maxAlpha = 0.0f;
                for (int i = 0; i < count; i++) {
                    float dx = x - spotsX[i];
                    float dy = y - spotsY[i];
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist <= spotRadius) {
                        float alpha = Utils.applyMask(dist, spotRadius, maskType);
                        if (alpha > maxAlpha) maxAlpha = alpha;
                    }
                }
                data[y * size + x] = maxAlpha;
            }
        }
        return data;
    }

    @Override
    public float[] rectangle(int size, float halfWidth, float halfHeight,
                             float cornerRadius, float angle, MaskType maskType) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        float cosA = (float) Math.cos(-angle);
        float sinA = (float) Math.sin(-angle);
        float cr = Math.min(cornerRadius, Math.min(halfWidth, halfHeight));
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float lx = dx * cosA + dy * sinA;
                float ly = -dx * sinA + dy * cosA;
                float qx = Math.abs(lx) - halfWidth + cr;
                float qy = Math.abs(ly) - halfHeight + cr;
                float sd;
                if (qx <= 0 && qy <= 0) {
                    sd = Math.max(qx, qy);
                } else if (qx <= 0) {
                    sd = qy;
                } else if (qy <= 0) {
                    sd = qx;
                } else {
                    sd = (float) Math.sqrt(qx * qx + qy * qy);
                }
                if (sd <= 0) {
                    float edgeDist = -sd;
                    float effectiveR = Math.min(halfWidth, halfHeight);
                    data[y * size + x] = Utils.applyMask(edgeDist, effectiveR, maskType);
                } else {
                    data[y * size + x] = 0.0f;
                }
            }
        }
        return data;
    }

    @Override
    public float[] diamond(int size, float halfWidth, float halfHeight,
                           float angle, MaskType maskType) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        float cosA = (float) Math.cos(-angle);
        float sinA = (float) Math.sin(-angle);
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float lx = dx * cosA + dy * sinA;
                float ly = -dx * sinA + dy * cosA;
                float nx = Math.abs(lx) / halfWidth;
                float ny = Math.abs(ly) / halfHeight;
                float t = nx + ny;
                if (t <= 1.0f) {
                    float edgeDist = (1.0f - t) * Math.min(halfWidth, halfHeight);
                    float effectiveR = Math.min(halfWidth, halfHeight);
                    data[y * size + x] = Utils.applyMask(edgeDist, effectiveR, maskType);
                } else {
                    data[y * size + x] = 0.0f;
                }
            }
        }
        return data;
    }

    @Override
    public float[] trapezoid(int size, float topHalfWidth, float bottomHalfWidth,
                             float halfHeight, float angle, MaskType maskType) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        float cosA = (float) Math.cos(-angle);
        float sinA = (float) Math.sin(-angle);
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float lx = dx * cosA + dy * sinA;
                float ly = -dx * sinA + dy * cosA;
                if (ly < -halfHeight || ly > halfHeight) {
                    data[y * size + x] = 0.0f;
                    continue;
                }
                float t = (ly + halfHeight) / (2.0f * halfHeight);
                float currHalfWidth = bottomHalfWidth + (topHalfWidth - bottomHalfWidth) * t;
                float distToEdge = currHalfWidth - Math.abs(lx);
                if (distToEdge >= 0) {
                    float effectiveR = Math.min(currHalfWidth, halfHeight);
                    data[y * size + x] = Utils.applyMask(distToEdge, effectiveR, maskType);
                } else {
                    data[y * size + x] = 0.0f;
                }
            }
        }
        return data;
    }

    @Override
    public float[] teardrop(int size, float radius, float tailLength,
                            float angle, MaskType maskType) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        float cosA = (float) Math.cos(-angle);
        float sinA = (float) Math.sin(-angle);
        float actualTail = Math.max(tailLength, radius);
        float alpha = (float) Math.asin(radius / actualTail);
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float lx = dx * cosA + dy * sinA;
                float ly = -dx * sinA + dy * cosA;
                float dist = (float) Math.sqrt(lx * lx + ly * ly);
                float theta = (float) Math.atan2(ly, lx);
                float maxR;
                if (Math.abs(theta) <= alpha) {
                    float t = Math.abs(theta) / alpha;
                    maxR = radius + (actualTail - radius) * (1.0f - t);
                } else {
                    maxR = radius;
                }
                if (dist <= maxR) {
                    float edgeDist = maxR - dist;
                    data[y * size + x] = Utils.applyMask(edgeDist, radius, maskType);
                } else {
                    data[y * size + x] = 0.0f;
                }
            }
        }
        return data;
    }

    @Override
    public float[] crescent(int size, float outerRadius, float innerRadius,
                            float innerOffset, float angle, MaskType maskType) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        float cosA = (float) Math.cos(-angle);
        float sinA = (float) Math.sin(-angle);
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float lx = dx * cosA + dy * sinA;
                float ly = -dx * sinA + dy * cosA;
                float distOuter = (float) Math.sqrt(lx * lx + ly * ly);
                float distInner = (float) Math.sqrt((lx - innerOffset) * (lx - innerOffset) + ly * ly);
                if (distOuter <= outerRadius && distInner >= innerRadius) {
                    float edgeDistOuter = outerRadius - distOuter;
                    float edgeDistInner = distInner - innerRadius;
                    float edgeDist = Math.min(edgeDistOuter, edgeDistInner);
                    float effectiveR = Math.min(outerRadius, innerRadius);
                    data[y * size + x] = Utils.applyMask(edgeDist, effectiveR, maskType);
                } else {
                    data[y * size + x] = 0.0f;
                }
            }
        }
        return data;
    }

    @Override
    public float[] path(int size, String svgPath, float scaleX, float scaleY,
                        float angle, MaskType maskType) {
        float[] data = new float[size * size];
        if (svgPath == null || svgPath.isEmpty()) return data;
        try {
            Path2D.Double path = new Path2D.Double(); // AWT 仍用 double
            parseSVGPath(path, svgPath);
            java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform();
            at.translate(size / 2.0, size / 2.0);
            at.rotate(angle);
            at.scale(scaleX, scaleY);
            path.transform(at);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (path.contains(x, y)) {
                        data[y * size + x] = 1.0f; // 硬边填充
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败返回全零
        }
        return data;
    }

    private void parseSVGPath(Path2D.Double path, String d) {
        // 解析逻辑保持不变，仍用 double 读取坐标
        String[] tokens = d.replace(",", " ").trim().split("\\s+");
        double curX = 0, curY = 0, startX = 0, startY = 0;
        int i = 0;
        while (i < tokens.length) {
            String cmd = tokens[i++];
            switch (cmd) {
                case "M":
                    curX = Double.parseDouble(tokens[i++]);
                    curY = Double.parseDouble(tokens[i++]);
                    path.moveTo(curX, curY);
                    startX = curX; startY = curY;
                    break;
                case "L":
                    curX = Double.parseDouble(tokens[i++]);
                    curY = Double.parseDouble(tokens[i++]);
                    path.lineTo(curX, curY);
                    break;
                case "C":
                    double x1 = Double.parseDouble(tokens[i++]);
                    double y1 = Double.parseDouble(tokens[i++]);
                    double x2 = Double.parseDouble(tokens[i++]);
                    double y2 = Double.parseDouble(tokens[i++]);
                    curX = Double.parseDouble(tokens[i++]);
                    curY = Double.parseDouble(tokens[i++]);
                    path.curveTo(x1, y1, x2, y2, curX, curY);
                    break;
                case "Z":
                case "z":
                    path.closePath();
                    curX = startX; curY = startY;
                    break;
                default:
                    // 忽略未知命令
            }
        }
    }

    @Override
    public float[] outline(int size, ShapeType baseShape, float[] shapeParams,
                           float strokeWidth, float blur, MaskType maskType) {
        float[] baseMask;
        switch (baseShape) {
            case CIRCLE:
                baseMask = circle(size, shapeParams[0], MaskType.HARD);
                break;
            case ELLIPSE:
                baseMask = ellipse(size, shapeParams[0], shapeParams[1], shapeParams[2], MaskType.HARD);
                break;
            case POLYGON:
                baseMask = polygon(size, shapeParams[0], (int) shapeParams[1], shapeParams[2], MaskType.HARD);
                break;
            case STAR:
                baseMask = star(size, shapeParams[0], (int) shapeParams[1], shapeParams[2], MaskType.HARD);
                break;
            case RECTANGLE:
                baseMask = rectangle(size, shapeParams[0], shapeParams[1], shapeParams[2], shapeParams[3], MaskType.HARD);
                break;
            case DIAMOND:
                baseMask = diamond(size, shapeParams[0], shapeParams[1], shapeParams[2], MaskType.HARD);
                break;
            case TRAPEZOID:
                baseMask = trapezoid(size, shapeParams[0], shapeParams[1], shapeParams[2], shapeParams[3], MaskType.HARD);
                break;
            case TEARDROP:
                baseMask = teardrop(size, shapeParams[0], shapeParams[1], shapeParams[2], MaskType.HARD);
                break;
            case CRESCENT:
                baseMask = crescent(size, shapeParams[0], shapeParams[1], shapeParams[2], shapeParams[3], MaskType.HARD);
                break;
            default:
                baseMask = circle(size, 10.0f, MaskType.HARD);
        }
        float[] dist = new float[size * size];
        final float INF = 1e9f;
        for (int i = 0; i < dist.length; i++) {
            dist[i] = baseMask[i] > 0.5f ? 0.0f : INF;
        }
        // 前向传播
        for (int y = 1; y < size; y++) {
            for (int x = 1; x < size; x++) {
                int idx = y * size + x;
                float up = dist[(y - 1) * size + x] + 1.0f;
                float left = dist[y * size + (x - 1)] + 1.0f;
                dist[idx] = Math.min(dist[idx], Math.min(up, left));
            }
        }
        // 后向传播
        for (int y = size - 2; y >= 0; y--) {
            for (int x = size - 2; x >= 0; x--) {
                int idx = y * size + x;
                float down = dist[(y + 1) * size + x] + 1.0f;
                float right = dist[y * size + (x + 1)] + 1.0f;
                dist[idx] = Math.min(dist[idx], Math.min(down, right));
            }
        }
        float inner = strokeWidth - blur / 2.0f;
        float outer = strokeWidth + blur / 2.0f;
        float[] result = new float[size * size];
        for (int i = 0; i < result.length; i++) {
            float d = dist[i];
            if (d <= inner) {
                result[i] = 1.0f;
            } else if (d >= outer) {
                result[i] = 0.0f;
            } else {
                float t = (d - inner) / blur;
                result[i] = 1.0f - t;
            }
        }
        return result;
    }

    @Override
    public float[] linearGradient(int size, float angle, float startAlpha, float endAlpha) {
        float[] data = new float[size * size];
        float center = size / 2.0f;
        float cosA = (float) Math.cos(-angle);
        float sinA = (float) Math.sin(-angle);
        float half = size / 2.0f;
        for (int y = 0; y < size; y++) {
            float dy = y - center;
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float lx = dx * cosA + dy * sinA;
                float t = (lx + half) / size;
                t = Utils.clamp01(t);
                data[y * size + x] = Utils.lerp(startAlpha, endAlpha, t);
            }
        }
        return data;
    }

    @Override
    public float[] noise(int size, String noiseType, float frequency,
                         float amplitude, float persistence, long seed, MaskType maskType) {
        float[] data = new float[size * size];
        Random rand = new Random(seed);
        if ("white".equalsIgnoreCase(noiseType)) {
            for (int i = 0; i < data.length; i++) {
                data[i] = rand.nextFloat() * amplitude;
            }
            if (maskType != null) {
                float[] mask = circle(size, size / 2.0f, maskType);
                for (int i = 0; i < data.length; i++) {
                    data[i] *= mask[i];
                }
            }
            return data;
        }
        int[] p = Noises.createPermutation(seed);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float nx = x / (float) size * frequency;
                float ny = y / (float) size * frequency;
                float val = Noises.perlin2D(nx, ny, p) * amplitude;
                val = (val + 1.0f) / 2.0f;
                data[y * size + x] = Utils.clamp01(val);
            }
        }
        if (maskType != null) {
            float[] mask = circle(size, size / 2.0f, maskType);
            for (int i = 0; i < data.length; i++) {
                data[i] *= mask[i];
            }
        }
        return data;
    }
}