/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.EngineerView;
import city.cs.engine.World;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

public class DebugViewer
extends JFrame {
    private final EngineerView view;
    private final JButton pauseButton;
    private final JButton stepButton;
    private final JButton quitButton;

    public DebugViewer(World world, int width, int height) {
        super("Debug viewer");
        this.view = new EngineerView(world, width, height);
        Box buttonBar = new Box(0);
        this.pauseButton = new JButton("Pause");
        this.stepButton = new JButton("Step");
        this.quitButton = new JButton("Quit");
        this.stepButton.setEnabled(!world.isRunning());
        this.pauseButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (DebugViewer.this.view.getWorld().isRunning()) {
                    DebugViewer.this.view.getWorld().stop();
                    DebugViewer.this.pauseButton.setText("Play");
                    DebugViewer.this.stepButton.setEnabled(true);
                } else {
                    DebugViewer.this.view.getWorld().start();
                    DebugViewer.this.pauseButton.setText("Pause");
                    DebugViewer.this.stepButton.setEnabled(false);
                }
            }
        });
        this.stepButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!DebugViewer.this.view.getWorld().isRunning()) {
                    DebugViewer.this.view.getWorld().oneStep();
                }
            }
        });
        this.quitButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        buttonBar.add(this.pauseButton);
        buttonBar.add(this.stepButton);
        buttonBar.add(this.quitButton);
        Container pane = this.getContentPane();
        pane.setLayout(new BoxLayout(pane, 1));
        pane.add(this.view);
        pane.add(buttonBar);
        this.pack();
        this.setVisible(true);
    }

    public void setWorld(World world) {
        this.view.setWorld(world);
        this.view.initControls();
        this.stepButton.setEnabled(!world.isRunning());
    }

    public World getWorld() {
        return this.view.getWorld();
    }
}

