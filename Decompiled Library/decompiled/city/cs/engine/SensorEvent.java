/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.Sensor;
import city.cs.engine.SolidFixture;
import java.util.EventObject;

public class SensorEvent
extends EventObject {
    private final Sensor sensor;
    private final SolidFixture otherFixture;

    SensorEvent(Sensor sensor, SolidFixture otherFixture) {
        super(sensor);
        this.sensor = sensor;
        this.otherFixture = otherFixture;
    }

    @Override
    public String toString() {
        String s = super.toString();
        s = s + "[" + this.getSensorBody() + "<-" + this.getContactBody() + "]";
        return s;
    }

    public Body getContactBody() {
        return this.getContactFixture().getBody();
    }

    private Body getSensorBody() {
        return this.getSensor().getBody();
    }

    public SolidFixture getContactFixture() {
        return this.otherFixture;
    }

    public Sensor getSensor() {
        return this.sensor;
    }
}

