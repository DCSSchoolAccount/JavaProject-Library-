/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.BodyImage;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import org.jbox2d.common.Vec2;

public class AttachedImage {
    private final BodyImage image;
    private TransformData transformData;
    private final TransformData originalTransformData;

    public AttachedImage(Body body, BodyImage image, float scale, float rotation, Vec2 offset) {
        this.image = image;
        this.transformData = new TransformData();
        this.transformData.scale = scale;
        this.transformData.rotation = rotation;
        this.transformData.offsetX = offset.x;
        this.transformData.offsetY = offset.y;
        this.transformData.flipH = 1;
        this.transformData.flipV = 1;
        this.originalTransformData = new TransformData(this.transformData);
        body.attachImage(this);
    }

    public BodyImage getBodyImage() {
        return this.image;
    }

    void paint(Graphics2D g, AffineTransform transform, ImageObserver o) {
        AffineTransform imageTransform = this.localTransform(o);
        imageTransform.preConcatenate(transform);
        this.image.draw(g, imageTransform, o);
    }

    private AffineTransform localTransform(ImageObserver o) {
        AffineTransform imageTransform = AffineTransform.getScaleInstance(this.transformData.scale, this.transformData.scale);
        imageTransform.preConcatenate(AffineTransform.getRotateInstance(this.transformData.rotation));
        imageTransform.preConcatenate(AffineTransform.getTranslateInstance(this.transformData.offsetX, this.transformData.offsetY));
        imageTransform.preConcatenate(AffineTransform.getScaleInstance(this.transformData.flipH, this.transformData.flipV));
        return imageTransform;
    }

    public void reset() {
        this.transformData.scale = this.originalTransformData.scale;
        this.transformData.rotation = this.originalTransformData.rotation;
        this.transformData.offsetX = this.originalTransformData.offsetX;
        this.transformData.offsetY = this.originalTransformData.offsetY;
    }

    public void flipHorizontal() {
        this.transformData.flipH *= -1;
    }

    public void flipVertical() {
        this.transformData.flipV *= -1;
    }

    public float getScale() {
        return this.transformData.scale;
    }

    public void setScale(float scale) {
        this.transformData.scale = scale;
    }

    public float getRotation() {
        return this.transformData.rotation;
    }

    public void setRotation(float rotation) {
        this.transformData.rotation = rotation;
    }

    public Vec2 getOffset() {
        return new Vec2(this.transformData.offsetX, this.transformData.offsetY);
    }

    public void setOffset(Vec2 offset) {
        this.transformData.offsetX = offset.x;
        this.transformData.offsetY = offset.y;
    }

    public boolean isFlippedHorizontal() {
        return this.transformData.flipH != 1;
    }

    public boolean isFlippedVertical() {
        return this.transformData.flipV != 1;
    }

    private class TransformData {
        float scale;
        float rotation;
        float offsetX;
        float offsetY;
        int flipH;
        int flipV;

        TransformData() {
        }

        TransformData(TransformData d) {
            this.scale = d.scale;
            this.rotation = d.rotation;
            this.offsetX = d.offsetX;
            this.offsetY = d.offsetY;
            this.flipH = d.flipH;
            this.flipV = d.flipV;
        }
    }
}

