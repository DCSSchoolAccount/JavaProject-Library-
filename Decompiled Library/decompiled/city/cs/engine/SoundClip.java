/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundClip {
    private static boolean DEBUG = false;
    private transient Clip clip;
    private transient FloatControl gainControl;
    private final String fileName;
    private URL url;
    private long pausePosition;
    private boolean looping;

    public SoundClip(String fileName) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.fileName = new File(fileName).getAbsolutePath();
        try {
            this.url = new URL("file", "localhost", -1, this.fileName);
        }
        catch (MalformedURLException e) {
            throw new UnsupportedAudioFileException("Malformed audio file name: " + this.fileName);
        }
        this.init();
    }

    public SoundClip(URL url) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.url = url;
        this.fileName = url.toExternalForm();
        this.init();
    }

    private void init() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        DataLine.Info info;
        boolean bIsSupportedDirectly;
        boolean bBigEndian = false;
        int nSampleSizeInBits = 16;
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(this.url);
        if (DEBUG) {
            this.out("primary AIS: " + audioInputStream);
        }
        AudioFormat audioFormat = audioInputStream.getFormat();
        if (DEBUG) {
            this.out("primary format: " + audioFormat);
        }
        if (!(bIsSupportedDirectly = AudioSystem.isLineSupported(info = new DataLine.Info(Clip.class, audioFormat, -1)))) {
            AudioFormat sourceFormat = audioFormat;
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), nSampleSizeInBits, sourceFormat.getChannels(), sourceFormat.getChannels() * (nSampleSizeInBits / 8), sourceFormat.getSampleRate(), bBigEndian);
            if (DEBUG) {
                this.out("source format: " + sourceFormat);
                this.out("target format: " + targetFormat);
            }
            audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
            audioFormat = audioInputStream.getFormat();
            if (DEBUG) {
                this.out("converted AIS: " + audioInputStream);
            }
            if (DEBUG) {
                this.out("converted format: " + audioFormat);
            }
            info = new DataLine.Info(Clip.class, audioFormat, -1);
        }
        this.clip = (Clip)AudioSystem.getLine(info);
        if (this.clip == null) {
            if (DEBUG) {
                this.out("cannot get SourceDataLine for format " + audioFormat);
            }
            throw new LineUnavailableException("cannot get SourceDataLine for format " + audioFormat);
        }
        if (DEBUG) {
            this.out("clip: " + this.clip);
        }
        if (DEBUG) {
            this.out("clip format: " + this.clip.getFormat());
        }
        if (DEBUG) {
            this.out("clip buffer size: " + this.clip.getBufferSize());
        }
        this.clip.open(audioInputStream);
        this.clip.setLoopPoints(0, -1);
        this.gainControl = null;
        try {
            this.gainControl = (FloatControl)this.clip.getControl(FloatControl.Type.MASTER_GAIN);
        }
        catch (IllegalArgumentException e) {
            this.out("Master Gain control not available");
        }
    }

    private void out(String strMessage) {
        System.err.println("SoundClip(" + this.fileName + "): " + strMessage);
    }

    public void setVolume(double volume) {
        if (this.gainControl != null) {
            double epsilon = 1.0E-6;
            if (volume < epsilon) {
                volume = epsilon;
            }
            this.gainControl.setValue((float)(20.0 * Math.log10(volume)));
        } else {
            System.err.println("Could not set volume for clip " + this.fileName);
        }
    }

    public void play() {
        if (this.clip == null) {
            return;
        }
        this.pausePosition = 0L;
        this.looping = false;
        this.clip.setFramePosition(0);
        this.clip.start();
    }

    public void loop() {
        if (this.clip == null) {
            return;
        }
        this.clip.setFramePosition(0);
        this.looping = true;
        this.clip.loop(-1);
    }

    public void stop() {
        if (this.clip == null) {
            return;
        }
        this.clip.stop();
        this.looping = false;
    }

    public void resume() {
        if (this.clip == null) {
            return;
        }
        this.clip.setMicrosecondPosition(this.pausePosition);
        if (this.looping) {
            this.clip.loop(-1);
        } else {
            this.clip.start();
        }
    }

    public void pause() {
        if (this.clip == null) {
            return;
        }
        this.pausePosition = this.clip.getMicrosecondPosition();
        this.clip.stop();
    }

    public void close() {
        if (this.clip == null) {
            return;
        }
        this.clip.close();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            this.init();
        }
        catch (Exception e) {
            System.err.println(e);
        }
    }

    protected void finalize() throws Throwable {
        try {
            this.close();
        }
        finally {
            super.finalize();
        }
    }
}

