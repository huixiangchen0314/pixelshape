package top.kzre.pixelshape;

import java.awt.geom.Path2D;
import java.util.Random;

/**
 * 像素形状生成器的默认实现，包含所有内建形状的数学算法。
 */
public class PixelShapeImpl implements PixelShape.Spec {
    // ── 已存在的形状实现 ─────────────────────

    @Override
    public double[] circle(int size, double radius, MaskType maskType) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double dist = Math.sqrt(dx * dx + dy * dy);
                data[y * size + x] = Utils.applyMask(dist, radius, maskType);
            }
        }
        return data;
    }

    @Override
    public double[] ellipse(int size, double radiusX, double radiusY, double angle, MaskType maskType) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        double cosA = Math.cos(-angle);
        double sinA = Math.sin(-angle);
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double rx = dx * cosA + dy * sinA;
                double ry = -dx * sinA + dy * cosA;
                double nx = rx / radiusX;
                double ny = ry / radiusY;
                double dist = Math.sqrt(nx * nx + ny * ny);
                data[y * size + x] = Utils.applyMask(dist, 1.0, maskType);
            }
        }
        return data;
    }

    @Override
    public double[] polygon(int size, double radius, int sides, double angle, MaskType maskType) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        double halfSector = Math.PI / sides;
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double dist = Math.sqrt(dx * dx + dy * dy);
                double a = Math.atan2(dy, dx) - angle;
                a = (a + Math.PI + Math.PI) % (2 * Math.PI);
                double sector = a % (2 * halfSector);
                double r;
                if (sector < halfSector) {
                    r = radius / Math.cos(sector - halfSector);
                } else {
                    r = radius / Math.cos(2 * halfSector - sector);
                }
                if (dist <= r) {
                    data[y * size + x] = Utils.applyMask(dist, r, maskType);
                } else {
                    data[y * size + x] = 0.0;
                }
            }
        }
        return data;
    }

    @Override
    public double[] star(int size, double radius, int points, double innerRatio, MaskType maskType) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        double innerRadius = radius * innerRatio;
        double halfStep = Math.PI / points;
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double dist = Math.sqrt(dx * dx + dy * dy);
                double a = Math.atan2(dy, dx);
                double sector = (a % (2 * halfStep) + 2 * halfStep) % (2 * halfStep);
                double t = sector / halfStep;
                double r;
                if (t < 1.0) {
                    r = radius + (innerRadius - radius) * t;
                } else {
                    r = innerRadius + (radius - innerRadius) * (t - 1.0);
                }
                if (dist <= r) {
                    data[y * size + x] = Utils.applyMask(dist, r, maskType);
                } else {
                    data[y * size + x] = 0.0;
                }
            }
        }
        return data;
    }

    @Override
    public double[] fromImage(int size, double[] imageData, int imgW, int imgH,
                              double scaleX, double scaleY, double angle) {
        double[] data = new double[size * size];
        if (imageData == null || imgW <= 0 || imgH <= 0) {
            return data;
        }
        double center = size / 2.0;
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        double invScaleX = 1.0 / scaleX;
        double invScaleY = 1.0 / scaleY;
        double srcCx = imgW / 2.0;
        double srcCy = imgH / 2.0;
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double rx = dx * cosA - dy * sinA;
                double ry = dx * sinA + dy * cosA;
                double sx = rx * invScaleX + srcCx;
                double sy = ry * invScaleY + srcCy;
                data[y * size + x] = Utils.bilinearSample(imageData, imgW, imgH, sx, sy);
            }
        }
        return data;
    }

    @Override
    public double[] splatter(int size, double radius, int count, double spotSize,
                             MaskType maskType, long seed) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        Random rng = new Random(seed);
        double[] spotsX = new double[count];
        double[] spotsY = new double[count];
        double spotRadius = radius * spotSize;
        for (int i = 0; i < count; i++) {
            double a = 2 * Math.PI * rng.nextDouble();
            double r = radius * Math.sqrt(rng.nextDouble());
            spotsX[i] = center + r * Math.cos(a);
            spotsY[i] = center + r * Math.sin(a);
        }
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double maxAlpha = 0.0;
                for (int i = 0; i < count; i++) {
                    double dx = x - spotsX[i];
                    double dy = y - spotsY[i];
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist <= spotRadius) {
                        double alpha = Utils.applyMask(dist, spotRadius, maskType);
                        if (alpha > maxAlpha) maxAlpha = alpha;
                    }
                }
                data[y * size + x] = maxAlpha;
            }
        }
        return data;
    }

    // ── 新增形状实现 ─────────────────────────

    @Override
    public double[] rectangle(int size, double halfWidth, double halfHeight,
                              double cornerRadius, double angle, MaskType maskType) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        double cosA = Math.cos(-angle);
        double sinA = Math.sin(-angle);
        double cr = Math.min(cornerRadius, Math.min(halfWidth, halfHeight));
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double lx = dx * cosA + dy * sinA;
                double ly = -dx * sinA + dy * cosA;
                // 有符号距离场：圆角矩形
                double qx = Math.abs(lx) - halfWidth + cr;
                double qy = Math.abs(ly) - halfHeight + cr;
                double sd;
                if (qx <= 0 && qy <= 0) {
                    sd = Math.max(qx, qy); // 内部，负值
                } else if (qx <= 0) {
                    sd = qy; // 在左右直边外部
                } else if (qy <= 0) {
                    sd = qx; // 在上下直边外部
                } else {
                    sd = Math.sqrt(qx * qx + qy * qy); // 在圆角外部
                }
                // sd <= 0 表示在形状内部，内部点到边缘的距离为 -sd
                if (sd <= 0) {
                    double edgeDist = -sd;
                    double effectiveR = Math.min(halfWidth, halfHeight);
                    data[y * size + x] = Utils.applyMask(edgeDist, effectiveR, maskType);
                } else {
                    data[y * size + x] = 0.0;
                }
            }
        }
        return data;
    }

    @Override
    public double[] diamond(int size, double halfWidth, double halfHeight,
                            double angle, MaskType maskType) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        double cosA = Math.cos(-angle);
        double sinA = Math.sin(-angle);
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double lx = dx * cosA + dy * sinA;
                double ly = -dx * sinA + dy * cosA;
                double nx = Math.abs(lx) / halfWidth;
                double ny = Math.abs(ly) / halfHeight;
                double t = nx + ny; // 边界上 t = 1
                if (t <= 1.0) {
                    // 内部点到边界的近似距离：按最近边的距离计算
                    // 实际菱形边界距离可简化为 (1 - t) * min(halfWidth, halfHeight) / sqrt(2) 缩放
                    double edgeDist = (1.0 - t) * Math.min(halfWidth, halfHeight);
                    double effectiveR = Math.min(halfWidth, halfHeight);
                    data[y * size + x] = Utils.applyMask(edgeDist, effectiveR, maskType);
                } else {
                    data[y * size + x] = 0.0;
                }
            }
        }
        return data;
    }

    @Override
    public double[] trapezoid(int size, double topHalfWidth, double bottomHalfWidth,
                              double halfHeight, double angle, MaskType maskType) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        double cosA = Math.cos(-angle);
        double sinA = Math.sin(-angle);
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double lx = dx * cosA + dy * sinA;
                double ly = -dx * sinA + dy * cosA;
                if (ly < -halfHeight || ly > halfHeight) {
                    data[y * size + x] = 0.0;
                    continue;
                }
                double t = (ly + halfHeight) / (2.0 * halfHeight); // 0..1 从下底到上底
                double currHalfWidth = bottomHalfWidth + (topHalfWidth - bottomHalfWidth) * t;
                double distToEdge = currHalfWidth - Math.abs(lx);
                if (distToEdge >= 0) {
                    double effectiveR = Math.min(currHalfWidth, halfHeight);
                    data[y * size + x] = Utils.applyMask(distToEdge, effectiveR, maskType);
                } else {
                    data[y * size + x] = 0.0;
                }
            }
        }
        return data;
    }

    @Override
    public double[] teardrop(int size, double radius, double tailLength,
                             double angle, MaskType maskType) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        double cosA = Math.cos(-angle);
        double sinA = Math.sin(-angle);
        double actualTail = Math.max(tailLength, radius);
        double alpha = Math.asin(radius / actualTail); // 切线半角
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double lx = dx * cosA + dy * sinA;
                double ly = -dx * sinA + dy * cosA;
                double dist = Math.sqrt(lx * lx + ly * ly);
                double theta = Math.atan2(ly, lx);
                double maxR;
                if (Math.abs(theta) <= alpha) {
                    double t = Math.abs(theta) / alpha;
                    maxR = radius + (actualTail - radius) * (1.0 - t);
                } else {
                    maxR = radius;
                }
                if (dist <= maxR) {
                    double edgeDist = maxR - dist;
                    data[y * size + x] = Utils.applyMask(edgeDist, radius, maskType);
                } else {
                    data[y * size + x] = 0.0;
                }
            }
        }
        return data;
    }

    @Override
    public double[] crescent(int size, double outerRadius, double innerRadius,
                             double innerOffset, double angle, MaskType maskType) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        double cosA = Math.cos(-angle);
        double sinA = Math.sin(-angle);
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double lx = dx * cosA + dy * sinA;
                double ly = -dx * sinA + dy * cosA;
                double distOuter = Math.sqrt(lx * lx + ly * ly);
                double distInner = Math.sqrt((lx - innerOffset) * (lx - innerOffset) + ly * ly);
                if (distOuter <= outerRadius && distInner >= innerRadius) {
                    double edgeDistOuter = outerRadius - distOuter;
                    double edgeDistInner = distInner - innerRadius;
                    double edgeDist = Math.min(edgeDistOuter, edgeDistInner);
                    double effectiveR = Math.min(outerRadius, innerRadius);
                    data[y * size + x] = Utils.applyMask(edgeDist, effectiveR, maskType);
                } else {
                    data[y * size + x] = 0.0;
                }
            }
        }
        return data;
    }

    @Override
    public double[] path(int size, String svgPath, double scaleX, double scaleY,
                         double angle, MaskType maskType) {
        double[] data = new double[size * size];
        if (svgPath == null || svgPath.isEmpty()) return data;
        try {
            Path2D.Double path = new Path2D.Double();
            parseSVGPath(path, svgPath);
            java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform();
            at.translate(size / 2.0, size / 2.0);
            at.rotate(angle);
            at.scale(scaleX, scaleY);
            path.transform(at);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (path.contains(x, y)) {
                        data[y * size + x] = 1.0; // 硬边填充，若需柔和边缘可扩展距离场
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败返回全零
        }
        return data;
    }

    /** 极简 SVG 路径解析器，支持 M L C Z (绝对坐标) */
    private void parseSVGPath(Path2D.Double path, String d) {
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
    public double[] outline(int size, ShapeType baseShape, double[] shapeParams,
                            double strokeWidth, double blur, MaskType maskType) {
        // 先生成基础形状的硬边遮罩 (内部 1.0，外部 0.0)
        double[] baseMask;
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
                baseMask = circle(size, 10, MaskType.HARD);
        }
        // 使用倒角距离变换计算距离场
        double[] dist = new double[size * size];

        final double INF = 1e9;
        for (int i = 0; i < dist.length; i++) {
            dist[i] = baseMask[i] > 0.5 ? 0.0 : INF;
        }
        // 前向传播
        for (int y = 1; y < size; y++) {
            for (int x = 1; x < size; x++) {
                int idx = y * size + x;
                double up = dist[(y - 1) * size + x] + 1;
                double left = dist[y * size + (x - 1)] + 1;
                dist[idx] = Math.min(dist[idx], Math.min(up, left));
            }
        }
        // 后向传播
        for (int y = size - 2; y >= 0; y--) {
            for (int x = size - 2; x >= 0; x--) {
                int idx = y * size + x;
                double down = dist[(y + 1) * size + x] + 1;
                double right = dist[y * size + (x + 1)] + 1;
                dist[idx] = Math.min(dist[idx], Math.min(down, right));
            }
        }
        // 根据 strokeWidth 和 blur 生成轮廓遮罩
        double inner = strokeWidth - blur / 2.0;
        double outer = strokeWidth + blur / 2.0;
        double[] result = new double[size * size];
        for (int i = 0; i < result.length; i++) {
            double d = dist[i];
            if (d <= inner) {
                result[i] = 1.0;
            } else if (d >= outer) {
                result[i] = 0.0;
            } else {
                double t = (d - inner) / blur;
                result[i] = 1.0 - t;
            }
        }
        return result;
    }

    @Override
    public double[] linearGradient(int size, double angle, double startAlpha, double endAlpha) {
        double[] data = new double[size * size];
        double center = size / 2.0;
        double cosA = Math.cos(-angle);
        double sinA = Math.sin(-angle);
        double half = size / 2.0;
        for (int y = 0; y < size; y++) {
            double dy = y - center;
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double lx = dx * cosA + dy * sinA;
                double t = (lx + half) / size;
                t = Utils.clamp01(t);
                data[y * size + x] = Utils.lerp(startAlpha, endAlpha, t);
            }
        }
        return data;
    }

    @Override
    public double[] noise(int size, String noiseType, double frequency,
                          double amplitude, double persistence, long seed, MaskType maskType) {
        double[] data = new double[size * size];
        Random rand = new Random(seed);
        if ("white".equalsIgnoreCase(noiseType)) {
            for (int i = 0; i < data.length; i++) {
                data[i] = rand.nextDouble() * amplitude;
            }
            if (maskType != null) {
                double[] mask = circle(size, size / 2.0, maskType);
                for (int i = 0; i < data.length; i++) {
                    data[i] *= mask[i];
                }
            }
            return data;
        }
        // Perlin 噪声：创建排列表并复用
        int[] p = Noises.createPermutation(seed);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double nx = x / (double) size * frequency;
                double ny = y / (double) size * frequency;
                double val = Noises.perlin2D(nx, ny, p) * amplitude;
                val = (val + 1.0) / 2.0; // 映射到 0..1
                data[y * size + x] = Utils.clamp01(val);
            }
        }
        if (maskType != null) {
            double[] mask = circle(size, size / 2.0, maskType);
            for (int i = 0; i < data.length; i++) {
                data[i] *= mask[i];
            }
        }
        return data;
    }

}