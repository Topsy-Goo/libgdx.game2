package ru.gb.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import static ru.gb.mygdx.game.HeroStates.HS_RUNNING;
import static ru.gb.mygdx.game.HeroStates.HS_STANDING;

import java.awt.Dimension;

public class Hero
{
    private final Animator animatorRunning, animatorStanding;
    private final FPoint pinPoint, step;
    private       HeroStates state;
    private       float scale;
    private       Rectangle rectShape;


    public Hero (FPoint pointToPinHeroUp, FPoint heroStep, HeroStates initialState)
    {
        step = heroStep;
        animatorRunning = new Animator ("mario02.png", 4, 2, 15, Animation.PlayMode.LOOP,
                                        137, 0, 256, 196, 8);
        animatorStanding = new Animator ("mario02.png", 1, 1, 15, Animation.PlayMode.NORMAL,
                                         0, 0, 64, 98, 1);
        Dimension dimentionHero = animatorRunning.getTileDimention();
        pinPoint = pointToPinHeroUp;
        pinPoint.x -= dimentionHero.width / 2.0f;
        pinPoint.y -= dimentionHero.height / 2.0f;
        setState (HS_STANDING);
        rectShape = new Rectangle (pinPoint.x, pinPoint.y, dimentionHero.width, dimentionHero.height);
        state = initialState;
    }

    public FPoint pinPoint () {   return pinPoint;   }

    public FPoint step () {   return step.scale (scale);   }

    public void setState (HeroStates newState) {
        if (newState != null)
            state = newState;
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
        batch.draw (animator.getTile(),
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
    }
}
