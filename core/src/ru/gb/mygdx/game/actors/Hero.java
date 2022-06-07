package ru.gb.mygdx.game.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import static ru.gb.mygdx.game.actors.ActorStates.*;
import static ru.gb.mygdx.game.actors.MoveDirections.*;

import java.awt.Dimension;

import ru.gb.mygdx.game.Animator;

public class Hero
{
    private final Animator animatorRunning, animatorStanding/*, animatorClimbing*/;
    private final Vector2 pinPoint, step;
    private ActorStates    state;
    private MoveDirections direction;
    private float          scale;
    private       Rectangle rectShape;


    public Hero (Vector2 pointToPinHeroUp, Vector2 heroStep, ActorStates initialState, MoveDirections dir)
    {
        step = heroStep;
        animatorRunning = new Animator ("mario02.png", 4, 2, 15, Animation.PlayMode.LOOP,
                                        137, 0, 256, 196, 8);
        animatorStanding = new Animator ("mario02.png", 1, 1, 15, Animation.PlayMode.NORMAL,
                                         0, 0, 64, 98, 1);
/*        animatorClimbing = new Animator ("", 1, 1, 15, Animation.PlayMode.LOOP,
                                         , , , , );*/
        Dimension dimentionHero = animatorRunning.getTileDimention();
        pinPoint = pointToPinHeroUp;
        pinPoint.x -= dimentionHero.width / 2.0f;
        pinPoint.y -= dimentionHero.height / 2.0f;
        setState (HS_STANDING);
        rectShape = new Rectangle (pinPoint.x, pinPoint.y, dimentionHero.width, dimentionHero.height);
        state     = initialState;
        direction = dir;
    }

    public Vector2 pinPoint () {   return pinPoint;   }

    public Vector2 step () {   return step.scl (scale);   }

    public ActorStates getState () { return state; }

    public MoveDirections getDirection () { return direction; }

    public void setState (ActorStates newState) {
        if (newState != null)
            state = newState;
    }

    public void setDirection (MoveDirections newDirection) {
        if (newDirection != null)
            direction = newDirection;
    }

    public void updateTime (float deltaTime)
    {
        if (state.equals (HS_RUNNING))
            animatorRunning.updateTime (deltaTime);
        else
        if (state.equals (HS_STANDING))
            animatorStanding.updateTime (deltaTime);
    }

    public void draw (SpriteBatch batch)
    {
        Animator animator = state == HS_STANDING ? animatorStanding : animatorRunning;
        TextureRegion tr = animator.getCurrentTile();

        if (direction.equals (HD_LEFT) && !tr.isFlipX())
            tr.flip (true, false);
        else
        if (direction.equals (HD_RIGHT) && tr.isFlipX())
            tr.flip (true, false);

        batch.draw (tr,
                    pinPoint.x,
                    pinPoint.y,
                    0, 0, animator.tileWidth, animator.tileHeight,
                    scale, scale, 0);
    }

    public void drawShape (ShapeRenderer shaper, Color color) {
        shaper.setColor (color);
        shaper.rect (rectShape.x, rectShape.y, rectShape.width, rectShape.height);
    }

    public void setScale (float factor) {
        if (factor > 0.0f)
            scale = factor;
    }

    public Rectangle shape () { return rectShape; }

    //public void flip () {
    //    animatorRunning.t;
    //}

    public void dispose () {
        animatorRunning.dispose();
        animatorStanding.dispose();
    }
}
