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
    private final Animator animatorRunning, animatorStanding, animatorJumping, animatorClimbing;
    private final Vector2 pinPoint, step, jump;
    private       ActorStates    state;
    private       MoveDirections direction;
    private       float          scale;
    private final Rectangle rectShape;


    public Hero (String fileName, Vector2 pointToPinHeroUp, Vector2 heroStep,
                 ActorStates initialState, MoveDirections dir)
    {
        step = heroStep;
        jump = new Vector2 (heroStep.x, heroStep.y);
        jump.scl (8.0f);

        //                                         c  r  fps            mode                x0  y0  w     h   n
        animatorRunning  = new Animator (fileName, 4, 2, 15, Animation.PlayMode.LOOP,   137    , 0, 256, 196, 8);
        animatorStanding = new Animator (fileName, 1, 1, 15, Animation.PlayMode.NORMAL,   0    , 0,  64,  98, 1);
        animatorJumping  = new Animator (fileName, 1, 1, 15, Animation.PlayMode.NORMAL, 137+128, 0,  64,  98, 1);
        animatorClimbing = new Animator (fileName, 1, 2,  5, Animation.PlayMode.LOOP,   137+ 64, 0,  64, 196, 2);

        Dimension dimentionHero = animatorRunning.getTileDimention();
        pinPoint = pointToPinHeroUp;
        pinPoint.x -= dimentionHero.width / 2.0f;
        pinPoint.y -= dimentionHero.height / 2.0f;
        setState (AS_STANDING);
        rectShape = new Rectangle (pinPoint.x, pinPoint.y, dimentionHero.width, dimentionHero.height);
        state     = initialState;
        direction = dir;
    }

    public Vector2 pinPoint () {   return pinPoint;   }

    public Vector2 step () {   return step.scl (scale);   }

    public Vector2 jump () {   return jump.scl (scale);   }

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
        Animator animator = currentStateAnimator();
        if (animator != null)
            animator.updateTime (deltaTime);
    }

    private Animator currentStateAnimator () {
        Animator animator = null;
        switch (state)
        {
            //case AS_STANDING:       animator = animatorStanding;  System.out.print("S");
            //    break;
            case AS_RUNNING:        animator = animatorRunning;   //System.out.print("R");
                break;
            case AS_JUMPING_FORTH:  animator = animatorJumping;   //System.out.print("Jf");
                break;
            case AS_JUMPING_UP:     animator = animatorJumping;   //System.out.print("Ju");
                break;
            case AS_CLIMBING:       animator = animatorClimbing;  //System.out.print("C");
                break;
            default:                animator = animatorStanding;  //System.out.print("S");
        }
        return animator;
    }

    public void draw (SpriteBatch batch)
    {
        Animator animator = currentStateAnimator();
        TextureRegion tr = animator.getCurrentTile();

        if (direction.equals (MD_LEFT) && !tr.isFlipX())
            tr.flip (true, false);
        else
        if (direction.equals (MD_RIGHT) && tr.isFlipX())
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

    public void dispose () {
        animatorRunning.dispose();
        animatorStanding.dispose();
        animatorJumping.dispose();
        animatorClimbing.dispose();
    }
}