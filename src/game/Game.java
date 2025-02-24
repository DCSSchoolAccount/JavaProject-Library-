package game;

import city.cs.engine.*;
import city.cs.engine.Shape;
import org.jbox2d.common.Vec2;

import javax.swing.JFrame;

import java.awt.*;
import java.awt.event.KeyListener;
import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Your main game entry point
 */
public class Game {


    /** Initialise a new Game. */
    public Game() {

        //1. make an empty game world
        World world = new World();

        //2. populate it with bodies (ex: platforms, collectibles, characters)

        //make a ground platform
        Shape groundBasic = new BoxShape(30, 0.5f);
        StaticBody ground1 = new StaticBody(world, groundBasic);
        ground1.setPosition(new Vec2(0f, -11.5f));


        //make a character (with an overlaid image)
        Shape playerShape = new BoxShape(1,2);
        DynamicBody player = new DynamicBody(world, playerShape);
        player.setPosition(new Vec2(4,-5));
        player.addImage(new BodyImage("", 4));


        //3. make a view to look into the game world
        UserView view = new UserView(world, 500, 500);
        // view.setGridResolution(1);

        //4. create a Java window (frame) and add the game
        final JFrame frame = new JFrame("City Game");
        frame.add(view);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
       //frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        //optional: uncomment this to make a debugging view
         JFrame debugView = new DebugViewer(world, 500, 500);

        // start our game world simulation!
        world.start();
    }

    /** Run the game. */
    public static void main(String[] args) {

        new Game();
    }
}
