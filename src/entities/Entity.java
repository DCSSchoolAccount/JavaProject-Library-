package entities;

//import city.cs.engine.Walker;
//import Main.Game;

public abstract class Entity /*extends Walker*/{

    protected float x,y;
    protected int width, height;

    public Entity(float x, float y, int width, int height) {
//        super(Game.world);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

}
