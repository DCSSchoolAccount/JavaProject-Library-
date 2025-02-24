/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.Fixture;
import city.cs.engine.SensorEvent;
import city.cs.engine.SensorListener;
import city.cs.engine.Shape;
import java.awt.BasicStroke;
import java.util.LinkedList;
import java.util.List;
import org.jbox2d.dynamics.FixtureDef;

public class Sensor
extends Fixture {
    private List<SensorListener> listeners = new LinkedList<SensorListener>();
    private static final float[] DOT1 = new float[]{1.0f, 2.0f};
    private static final BasicStroke stroke = new BasicStroke(1.0f, 0, 2, 0.0f, DOT1, 0.0f);

    public Sensor(Body body, Shape shape, float density) {
        super(body, shape, Sensor.makeFixtureDef(density));
    }

    public Sensor(Body body, Shape shape) {
        this(body, shape, body.defaultDensity());
    }

    private static FixtureDef makeFixtureDef(float density) {
        FixtureDef def = new FixtureDef();
        def.density = density;
        def.isSensor = true;
        return def;
    }

    public void addSensorListener(SensorListener listener) {
        this.listeners.add(listener);
    }

    public void removeSensorListener(SensorListener listener) {
        this.listeners.remove(listener);
    }

    public void removeAllSensorListeners() {
        this.listeners.clear();
    }

    void beginContact(SensorEvent e) {
        for (SensorListener sl : this.listeners) {
            sl.beginContact(e);
        }
    }

    void endContact(SensorEvent e) {
        for (SensorListener sl : this.listeners) {
            sl.endContact(e);
        }
    }

    @Override
    boolean isVisible() {
        return false;
    }

    @Override
    BasicStroke getOutlineStyle() {
        return stroke;
    }
}

