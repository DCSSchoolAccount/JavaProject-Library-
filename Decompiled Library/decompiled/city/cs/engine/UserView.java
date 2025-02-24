/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.AttachedImage;
import city.cs.engine.Body;
import city.cs.engine.World;
import city.cs.engine.WorldView;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.List;

public class UserView
extends WorldView {
    private static final float[] DASH1 = new float[]{2.0f};
    private static final BasicStroke DASHED = new BasicStroke(1.0f, 0, 0, 1.0f, DASH1, 0.0f);
    private static final BasicStroke UNDASHED = new BasicStroke();

    public UserView(World w, int width, int height) {
        super(w, width, height);
    }

    @Override
    void paintBody(Graphics2D g, Body body) {
        Shape shape = this.transformToView(body.awtShape());
        List<AttachedImage> images = body.getImages();
        if (!images.isEmpty()) {
            g.setColor(Color.BLACK);
            AffineTransform transform = body.awtTransform();
            transform.preConcatenate(this.cameraTransform);
            if (body.isClipped()) {
                Shape savedClip = g.getClip();
                g.setClip(shape);
                this.paintImages(g, transform, images);
                g.setClip(savedClip);
            } else {
                this.paintImages(g, transform, images);
            }
        } else {
            g.setColor(body.getFillColor());
            g.fill(shape);
        }
        if (images.isEmpty() || body.isAlwaysOutline()) {
            g.setColor(body.getLineColor());
            g.setStroke(UNDASHED);
            g.draw(shape);
        }
    }

    private void paintImages(Graphics2D g, AffineTransform transform, List<AttachedImage> images) {
        for (AttachedImage image : images) {
            image.paint(g, transform, this);
        }
    }
}

