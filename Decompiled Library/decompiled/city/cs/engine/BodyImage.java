/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import javax.swing.ImageIcon;

public class BodyImage {
    public static final float DEFAULT_HEIGHT = 1.0f;
    private transient Image image;
    private URL url;
    private float height;

    public BodyImage(String fileName) {
        this(fileName, 1.0f);
    }

    public BodyImage(String fileName, float height) {
        this(BodyImage.makeURL(fileName), height);
    }

    public BodyImage(URL url) {
        this(url, 1.0f);
    }

    public BodyImage(URL url, float height) {
        this.url = url;
        this.height = height;
        this.image = this.loadImage(url);
    }

    private static URL makeURL(String fileName) {
        try {
            File file = new File(fileName);
            String absolute = file.getAbsolutePath();
            if (!file.canRead()) {
                throw new FileNotFoundException("Cannot read image file \"" + fileName + "\"");
            }
            return new URL("file", "localhost", -1, absolute);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Image loadImage(URL url) {
        return new ImageIcon(url).getImage();
    }

    void draw(Graphics2D g, AffineTransform transform, ImageObserver o) {
        AffineTransform imageTransform = this.localTransform(o);
        imageTransform.preConcatenate(transform);
        g.drawImage(this.image, imageTransform, o);
    }

    private AffineTransform localTransform(ImageObserver o) {
        int iw = this.image.getWidth(o);
        int ih = this.image.getHeight(o);
        boolean flipH = true;
        int flipV = -1;
        float scale = this.height / (float)ih;
        AffineTransform imageTransform = AffineTransform.getTranslateInstance((double)(-iw) / 2.0, (double)(-ih) / 2.0);
        imageTransform.preConcatenate(AffineTransform.getScaleInstance((float)flipH * scale, (float)flipV * scale));
        return imageTransform;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.url != null) {
            this.image = this.loadImage(this.url);
        }
    }
}

