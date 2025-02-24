/*
For this project I have utilised free resources, for images and sprites,
which can be found in the res folder (https://pixelfrog-assets.itch.io/treasure-hunters)
 The creator states that attribution is not required but nonetheless, here is the link to
 their main domain (https://pixelfrog-assets.itch.io/).

The license can be found here: https://creativecommons.org/publicdomain/zero/1.0/
 */

package Main;
import city.cs.engine.*;
import entities.Player;
import levels.LevelManager;

import java.awt.Graphics;


public class Game implements Runnable{

    private GameWindow gameWindow;
    private GamePanel gamePanel;
    private Thread gameThread;
    private final int FPS_SET = 120;
    private final int UPS_SET = 200;
    private Player player;
    private LevelManager levelManager;

    public final static int TILE_DEFAULT_SIZE =32;
    public final static float SCALE = 2f;
    public final static int TILES_IN_WIDTH = 26;
    public final static int TILES_IN_HEIGHT = 14;
    public final static int TILES_SIZE = (int)(TILE_DEFAULT_SIZE*SCALE);
    public final static int GAME_WIDTH = TILES_SIZE*TILES_IN_WIDTH;
    public final static int GAME_HEIGHT = TILES_SIZE*TILES_IN_HEIGHT;


    public Game(){
        iniClasses();

        gamePanel = new GamePanel(this);
        gameWindow = new GameWindow(gamePanel);
        gamePanel.requestFocus();

        startGameLoop();

    }

    private void iniClasses() {
        player = new Player(200 ,200,(int)(64*SCALE),(int)(40*SCALE));
        levelManager = new LevelManager(this);
    }

    private void startGameLoop(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void update(){
        player.update();
        levelManager.update();
    }

    public void render(Graphics g){
        player.render(g);
        levelManager.draw(g);
    }

    @Override
    public void run() {

        double timePerUpdate= 1000000000.0/UPS_SET;
        double timePerFrame = 1000000000.0/FPS_SET;
        long previousTime = System.nanoTime();

        int updates = 0;
        int frames = 0;
        long lastCheck = System.currentTimeMillis();

        double deltaU = 0;
        double deltaF = 0;

        while(true){
            long currentTime = System.nanoTime();


            deltaU += (currentTime - previousTime) / timePerUpdate;
            deltaF += (currentTime - previousTime) / timePerFrame;
            previousTime = currentTime;

            if (deltaU >= 1){
                update();
                updates++;
                deltaU --;
            }

            if(deltaF >= 1){
                gamePanel.repaint();
                frames++;
                deltaF --;
            }



            if (System.currentTimeMillis() - lastCheck >= 1000){
                lastCheck = System.currentTimeMillis();
                System.out.println("FPS: " + frames+" | UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }


    }

    public Player getPlayer() {
        return player;
    }

    public void windowFocusLost() {

        player.resetDirBooleans();

    }
}
