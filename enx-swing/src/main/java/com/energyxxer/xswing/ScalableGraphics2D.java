package com.energyxxer.xswing;

import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Arrays;
import java.util.Map;

public class ScalableGraphics2D extends Graphics2D {
    protected Graphics2D g;
    public static double SCALE_FACTOR = 1.0;

    public ScalableGraphics2D(Graphics g) {
        this.g = (Graphics2D) g;

        this.g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        this.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        this.g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    @Override
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.draw3DRect(x, y, width, height, raised);
    }

    @Override
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.fill3DRect(x, y, width, height, raised);
    }

    @Override
    public void draw(Shape s) {
        Debug.log("Cannot scale: " + Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-2]);
        g.draw(s);
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return g.drawImage(img, xform, obs);
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        g.drawImage(img, op, x, y);
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        g.drawRenderedImage(img, xform);
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        g.drawRenderableImage(img, xform);
    }

    @Override
    public void drawString(String str, int x, int y) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        g.drawString(str, x, y);
    }

    @Override
    public void drawString(String str, float x, float y) {
        x = (float) (x * SCALE_FACTOR);
        y = (float) (y * SCALE_FACTOR);
        g.drawString(str, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        g.drawString(iterator, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        x = (float) (x * SCALE_FACTOR);
        y = (float) (y * SCALE_FACTOR);
        g.drawString(iterator, x, y);
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        x = (float) (x * SCALE_FACTOR);
        y = (float) (y * SCALE_FACTOR);
        this.g.drawGlyphVector(g, x, y);
    }

    @Override
    public void fill(Shape s) {
        Debug.log("Cannot scale: " + Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-2]);
        g.fill(s);
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        Debug.log("Cannot scale: " + Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-2]);
        return g.hit(rect, s, onStroke);
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return g.getDeviceConfiguration();
    }

    @Override
    public void setComposite(Composite comp) {
        g.setComposite(comp);
    }

    @Override
    public void setPaint(Paint paint) {
        g.setPaint(paint);
    }

    @Override
    public void setStroke(Stroke s) {
        g.setStroke(s);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        g.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return g.getRenderingHint(hintKey);
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        g.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        g.addRenderingHints(hints);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return g.getRenderingHints();
    }

    @Override
    public void translate(int x, int y) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        g.translate(x, y);
    }

    @Override
    public void translate(double tx, double ty) {
        tx *= SCALE_FACTOR;
        ty *= SCALE_FACTOR;
        g.translate(tx, ty);
    }

    @Override
    public void rotate(double theta) {
        g.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        x *= SCALE_FACTOR;
        y *= SCALE_FACTOR;
        g.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
        sx *= SCALE_FACTOR;
        sy *= SCALE_FACTOR;
        g.scale(sx, sy);
    }

    @Override
    public void shear(double shx, double shy) {
        shx *= SCALE_FACTOR;
        shy *= SCALE_FACTOR;
        g.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform Tx) {
        Debug.log("Cannot scale: " + Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-2]);
        g.transform(Tx);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        Debug.log("Cannot scale: " + Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-2]);
        g.setTransform(Tx);
    }

    @Override
    public AffineTransform getTransform() {
        return g.getTransform();
    }

    @Override
    public Paint getPaint() {
        return g.getPaint();
    }

    @Override
    public Composite getComposite() {
        return g.getComposite();
    }

    @Override
    public void setBackground(Color color) {
        g.setBackground(color);
    }

    @Override
    public Color getBackground() {
        return g.getBackground();
    }

    @Override
    public Stroke getStroke() {
        return g.getStroke();
    }

    @Override
    public void clip(Shape s) {
        Debug.log("Cannot scale: " + Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-2]);
        g.clip(s);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return g.getFontRenderContext();
    }

    @Override
    public Graphics create() {
        return new ScalableGraphics2D(g.create());
    }

    @Override
    public Graphics create(int x, int y, int width, int height) {
        return new ScalableGraphics2D(g.create(x, y, width, height));
    }

    @Override
    public Color getColor() {
        return g.getColor();
    }

    @Override
    public void setColor(Color c) {
        g.setColor(c);
    }

    @Override
    public void setPaintMode() {
        g.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
        g.setXORMode(c1);
    }

    @Override
    public Font getFont() {
        return g.getFont();
    }

    @Override
    public void setFont(Font font) {
        g.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics() {
        return new ScalableFontMetrics(g.getFontMetrics());
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        return new ScalableFontMetrics(g.getFontMetrics(f));
    }

    @Override
    public Rectangle getClipBounds() {
        return g.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.clipRect(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.setClip(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        return g.getClip();
    }

    @Override
    public void setClip(Shape clip) {
        Debug.log("Cannot scale: " + Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-2]);
        g.setClip(clip);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        dx = (int) Math.round(dx * SCALE_FACTOR);
        dy = (int) Math.round(dy * SCALE_FACTOR);
        g.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        x1 = (int) Math.round(x1 * SCALE_FACTOR);
        y1 = (int) Math.round(y1 * SCALE_FACTOR);
        x2 = (int) Math.round(x2 * SCALE_FACTOR);
        y2 = (int) Math.round(y2 * SCALE_FACTOR);
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.fillRect(x, y, width, height);
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.drawRect(x, y, width, height);
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.clearRect(x, y, width, height);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        arcWidth = (int) Math.round(arcWidth * SCALE_FACTOR);
        arcHeight = (int) Math.round(arcHeight * SCALE_FACTOR);
        g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        arcWidth = (int) Math.round(arcWidth * SCALE_FACTOR);
        arcHeight = (int) Math.round(arcHeight * SCALE_FACTOR);
        g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.drawOval(x, y, width, height);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.fillOval(x, y, width, height);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        g.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        int[] xPointsScaled = Arrays.copyOf(xPoints, xPoints.length);
        int[] yPointsScaled = Arrays.copyOf(yPoints, yPoints.length);
        for(int i = 0; i < nPoints; i++) {
            xPointsScaled[i] = (int) Math.round(xPointsScaled[i] * SCALE_FACTOR);
            yPointsScaled[i] = (int) Math.round(yPointsScaled[i] * SCALE_FACTOR);
        }

        g.drawPolyline(xPointsScaled, yPointsScaled, nPoints);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        int[] xPointsScaled = Arrays.copyOf(xPoints, xPoints.length);
        int[] yPointsScaled = Arrays.copyOf(yPoints, yPoints.length);
        for(int i = 0; i < nPoints; i++) {
            xPointsScaled[i] = (int) Math.round(xPointsScaled[i] * SCALE_FACTOR);
            yPointsScaled[i] = (int) Math.round(yPointsScaled[i] * SCALE_FACTOR);
        }
        g.drawPolygon(xPointsScaled, yPointsScaled, nPoints);
    }

    @Override
    public void drawPolygon(Polygon p) {
        Debug.log("Cannot scale: " + Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-2]);
        g.drawPolygon(p);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        int[] xPointsScaled = Arrays.copyOf(xPoints, xPoints.length);
        int[] yPointsScaled = Arrays.copyOf(yPoints, yPoints.length);
        for(int i = 0; i < nPoints; i++) {
            xPointsScaled[i] = (int) Math.round(xPointsScaled[i] * SCALE_FACTOR);
            yPointsScaled[i] = (int) Math.round(yPointsScaled[i] * SCALE_FACTOR);
        }
        g.fillPolygon(xPointsScaled, yPointsScaled, nPoints);
    }

    @Override
    public void fillPolygon(Polygon p) {
        g.fillPolygon(p);
    }

    @Override
    public void drawChars(char[] data, int offset, int length, int x, int y) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        g.drawChars(data, offset, length, x, y);
    }

    @Override
    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        g.drawBytes(data, offset, length, x, y);
    }

    private void prepareInterpolation(Image img, int drawWidth, int drawHeight, ImageObserver observer) {
        this.g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                drawWidth > img.getWidth(observer) ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );
    }

    private void resetInterpolation() {
        this.g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return this.drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);

        prepareInterpolation(img, width, height, observer);
        boolean b = g.drawImage(img, x, y, width, height, observer);
        resetInterpolation();
        return b;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return this.drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        prepareInterpolation(img, width, height, observer);
        boolean b = g.drawImage(img, x, y, width, height, bgcolor, observer);
        resetInterpolation();
        return b;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        dx1 = (int) Math.round(dx1 * SCALE_FACTOR);
        dy1 = (int) Math.round(dy1 * SCALE_FACTOR);
        dx2 = (int) Math.round(dx2 * SCALE_FACTOR);
        dy2 = (int) Math.round(dy2 * SCALE_FACTOR);
        sx1 = (int) Math.round(sx1 * SCALE_FACTOR);
        sy1 = (int) Math.round(sy1 * SCALE_FACTOR);
        sx2 = (int) Math.round(sx2 * SCALE_FACTOR);
        sy2 = (int) Math.round(sy2 * SCALE_FACTOR);
        prepareInterpolation(img, dx2-dx1, dy2-dy1, observer);
        boolean b = g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        resetInterpolation();
        return b;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        dx1 = (int) Math.round(dx1 * SCALE_FACTOR);
        dy1 = (int) Math.round(dy1 * SCALE_FACTOR);
        dx2 = (int) Math.round(dx2 * SCALE_FACTOR);
        dy2 = (int) Math.round(dy2 * SCALE_FACTOR);
        sx1 = (int) Math.round(sx1 * SCALE_FACTOR);
        sy1 = (int) Math.round(sy1 * SCALE_FACTOR);
        sx2 = (int) Math.round(sx2 * SCALE_FACTOR);
        sy2 = (int) Math.round(sy2 * SCALE_FACTOR);
        prepareInterpolation(img, dx2-dx1, dy2-dy1, observer);
        boolean b = g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        resetInterpolation();
        return b;
    }

    @Override
    public void dispose() {
        g.dispose();
    }

    @Override
    public void finalize() {
        g.finalize();
    }

    @Override
    public String toString() {
        return g.toString();
    }

    @Override
    @Deprecated
    public Rectangle getClipRect() {
        return g.getClipRect();
    }

    @Override
    public boolean hitClip(int x, int y, int width, int height) {
        x = (int) Math.round(x * SCALE_FACTOR);
        y = (int) Math.round(y * SCALE_FACTOR);
        width = (int) Math.round(width * SCALE_FACTOR);
        height = (int) Math.round(height * SCALE_FACTOR);
        return g.hitClip(x, y, width, height);
    }

    @Override
    public Rectangle getClipBounds(Rectangle r) {
        return g.getClipBounds(r);
    }

    public static final class ScalableFontMetrics extends FontMetrics {
        private final FontMetrics metrics;

        public ScalableFontMetrics(FontMetrics metrics) {
            super(null);
            this.metrics = metrics;
        }

        @Override
        public Font getFont() {
            return metrics.getFont();
        }

        @Override
        public FontRenderContext getFontRenderContext() {
            return metrics.getFontRenderContext();
        }

        @Override
        public int getLeading() {
            return (int)(metrics.getLeading() / SCALE_FACTOR);
        }

        @Override
        public int getAscent() {
            return (int)(metrics.getAscent() / SCALE_FACTOR);
        }

        @Override
        public int getDescent() {
            return (int)(metrics.getDescent() / SCALE_FACTOR);
        }

        @Override
        public int getHeight() {
            return (int)(metrics.getHeight() / SCALE_FACTOR);
        }

        @Override
        public int getMaxAscent() {
            return (int)(metrics.getMaxAscent() / SCALE_FACTOR);
        }

        @Override
        public int getMaxDescent() {
            return (int)(metrics.getMaxDescent() / SCALE_FACTOR);
        }

        @Override
        @Deprecated
        public int getMaxDecent() {
            return (int)(metrics.getMaxDecent() / SCALE_FACTOR);
        }

        @Override
        public int getMaxAdvance() {
            return (int)(metrics.getMaxAdvance() / SCALE_FACTOR);
        }

        @Override
        public int charWidth(int codePoint) {
            return (int)(metrics.charWidth(codePoint) / SCALE_FACTOR);
        }

        @Override
        public int charWidth(char ch) {
            return (int)(metrics.charWidth(ch) / SCALE_FACTOR);
        }

        @Override
        public int stringWidth(@NotNull String str) {
            return (int)(metrics.stringWidth(str) / SCALE_FACTOR);
        }

        @Override
        public int charsWidth(char[] data, int off, int len) {
            return (int)(metrics.charsWidth(data, off, len) / SCALE_FACTOR);
        }

        @Override
        public int bytesWidth(byte[] data, int off, int len) {
            return (int)(metrics.bytesWidth(data, off, len) / SCALE_FACTOR);
        }

        @Override
        public int[] getWidths() {
            int[] widths = metrics.getWidths();
            for(int i = 0; i < widths.length; i++) {
                widths[i] = (int)(widths[i] / SCALE_FACTOR);
            }
            return widths;
        }

        @Override
        public boolean hasUniformLineMetrics() {
            return metrics.hasUniformLineMetrics();
        }

        @Override
        public LineMetrics getLineMetrics(String str, Graphics context) {
            return metrics.getLineMetrics(str, context);
        }

        @Override
        public LineMetrics getLineMetrics(String str, int beginIndex, int limit, Graphics context) {
            return metrics.getLineMetrics(str, beginIndex, limit, context);
        }

        @Override
        public LineMetrics getLineMetrics(char[] chars, int beginIndex, int limit, Graphics context) {
            return metrics.getLineMetrics(chars, beginIndex, limit, context);
        }

        @Override
        public LineMetrics getLineMetrics(CharacterIterator ci, int beginIndex, int limit, Graphics context) {
            return metrics.getLineMetrics(ci, beginIndex, limit, context);
        }

        @Override
        public Rectangle2D getStringBounds(String str, Graphics context) {
            return metrics.getStringBounds(str, context);
        }

        @Override
        public Rectangle2D getStringBounds(String str, int beginIndex, int limit, Graphics context) {
            return metrics.getStringBounds(str, beginIndex, limit, context);
        }

        @Override
        public Rectangle2D getStringBounds(char[] chars, int beginIndex, int limit, Graphics context) {
            return metrics.getStringBounds(chars, beginIndex, limit, context);
        }

        @Override
        public Rectangle2D getStringBounds(CharacterIterator ci, int beginIndex, int limit, Graphics context) {
            return metrics.getStringBounds(ci, beginIndex, limit, context);
        }

        @Override
        public Rectangle2D getMaxCharBounds(Graphics context) {
            return metrics.getMaxCharBounds(context);
        }

        @Override
        public String toString() {
            return metrics.toString();
        }
    }
}
