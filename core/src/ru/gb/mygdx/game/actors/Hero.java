package ru.gb.mygdx.game.actors;

import com.badlogic.gdx.Gdx;import com.badlogic.gdx.Graphics;import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;import com.badlogic.gdx.utils.Disposable;
import java.awt.Dimension;
import ru.gb.mygdx.game.Animator;

import static ru.gb.mygdx.game.Constants.*;
import static ru.gb.mygdx.game.actors.ActorStates.*;
import static ru.gb.mygdx.game.actors.MoveDirections.*;

public class Hero implements Disposable
{
    private final Animator animatorRunning, animatorStanding, animatorJumping, animatorClimbing;
    private final Vector2 pinPoint, step, jump;
    private       ActorStates    state;
    private       MoveDirections direction;
    private       float          scale;
    private       Rectangle rectShape;
    private       Fixture fixture;
    //private       Body body;


    public Hero (String fileName, Vector2 inWindowPoint, ActorStates initialState, MoveDirections dir)
    {
        scale = 1.0f / ZOOM;
        step = new Vector2 (heroStep);
        jump = new Vector2 (heroStep);
        jump.x = Math.max (1f, jump.x * STEP_TO_JUMP_FACTOR);

        animatorRunning  = new Animator (fileName, HERO_COLS, HERO_ROWS, 15, Animation.PlayMode.LOOP,
                                         TEX_OFFS_X,        //< ЛВУгол фрагмента текстуры
                                         TEX_OFFS_Y,        //< ЛВУгол фрагмента текстуры
                                         HERO_COLS * HERO_W,    //< ширина фрагмента текстуры
                                         HERO_ROWS * HERO_H,    //< высота фрагмента текстуры
                                         8);                    //< количество изображений во фрагменте (может отличаться от rows * cols).
        animatorStanding = new Animator (fileName, 1, 1, 15, Animation.PlayMode.NORMAL,
                                         0,
                                         TEX_OFFS_Y,
                                         HERO_W,
                                         HERO_H,
                                         1);
        animatorJumping  = new Animator (fileName, 1, 1, 15, Animation.PlayMode.NORMAL,
                                         2* HERO_W +TEX_OFFS_X,
                                         TEX_OFFS_Y,
                                         HERO_W,
                                         HERO_H,
                                         1);
        animatorClimbing = new Animator (fileName, 1, 2,  5, Animation.PlayMode.LOOP,
                                         HERO_W +TEX_OFFS_X,
                                         TEX_OFFS_Y,
                                         HERO_W,
                                         HERO_ROWS * HERO_H,
                                         2);

        Dimension dimentionHero = animatorRunning.getTileDimention();
        pinPoint = inWindowPoint;
        pinPoint.x -= dimentionHero.width / 2.0f / ZOOM;
        pinPoint.y -= dimentionHero.height / 2.0f / ZOOM;
        setState (AS_STANDING);
        rectShape = new Rectangle (pinPoint.x, pinPoint.y,
                                   dimentionHero.width, dimentionHero.height);
        state     = initialState;
        direction = dir;
    }
//----------------- геттеры и сеттеры ----------------------------------

    public Vector2 getPinPoint () {   return pinPoint;   }

    public Vector2 getStep () {   return step;   }

    public Vector2 getJump () {   return jump;   }

    public ActorStates getState () { return state; }
    public void setState (ActorStates value) {    if (value != null)   state = value;    }

    //public void setBody (Body value) {   body = value;   }

    public MoveDirections getDirection () { return direction; }
    public void setDirection (MoveDirections value) {    if (value != null)    direction = value;    }

    public void setFixture (Fixture value) {   fixture = value;   }
    public Fixture getFixture () {    return fixture;    }
//----------------------------------------------------------------------

    public void startFalling () {
        setState (AS_FALLING);
    }

    public void updateTime (float deltaTime)
    {
        Animator animator = currentStateAnimator();
        if (animator != null)
            animator.updateTime (deltaTime);
    }

    private Animator currentStateAnimator ()
    {
        Animator animator = null;
        switch (state)
        {
            case AS_STANDING:       animator = animatorStanding;  //System.out.print("S");
                break;
            case AS_RUNNING:        animator = animatorRunning;   //System.out.print("R");
                break;
            case AS_JUMPING_FORTH:  animator = animatorJumping;   //System.out.print("Jf");
                break;
            case AS_JUMPING_UP:     animator = animatorJumping;   //System.out.print("Ju");
                break;
            case AS_CLIMBING:       animator = animatorClimbing;  //System.out.print("C");
                break;
            case AS_FALLING:        animator = animatorRunning;   //System.out.print("F");
                break;
            default:   throw new RuntimeException("*** Unknown hero state. ***");
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

    public Rectangle shape () { return rectShape; }

    public void setScale (float factor, Vector2 mapToScreenOriginOffset) {
        if (factor > 0.0f) {
            scale = factor;

            //Body body = fixture.getBody();
            //Vector2 v = body.getPosition();
            //pinPoint.x = v.x - mapToScreenOriginOffset.x;
            //pinPoint.y = v.y - mapToScreenOriginOffset.y;

            Dimension dimentionHero = animatorRunning.getTileDimention();
            float viewportWidth = Gdx.graphics.getWidth();
            float viewportHeight = Gdx.graphics.getHeight();
            pinPoint.x = viewportWidth / 2f - dimentionHero.width / 2.0f / ZOOM;
            pinPoint.y = viewportHeight / 2f - dimentionHero.height / 2.0f / ZOOM;

            rectShape.x = pinPoint.x;
            rectShape.y = pinPoint.y;
            rectShape.setWidth (dimentionHero.width * scale);
            rectShape.setHeight (dimentionHero.height * scale);
        }
    }

    @Override public void dispose () {
        animatorRunning.dispose();
        animatorStanding.dispose();
        animatorJumping.dispose();
        animatorClimbing.dispose();
    }
}
