package ru.gb.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static ru.gb.mygdx.game.HeroStates.HS_RUNNING;
import static ru.gb.mygdx.game.HeroStates.HS_STANDING;

import java.awt.Dimension;

public class Hero
{
    private final Animator animatorRunning, animatorStanding;
    private final FPoint pinPoint, step;
    //private dimentionHero;
    private HeroStates state;
    private float scale;


    public Hero (FPoint pointToPinHeroUp, FPoint heroStep) {
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

    public void setScale (float factor) {
        if (factor > 0.0f)
            scale = factor;
    }

    public void dispose () {
        animatorRunning.dispose();
        animatorStanding.dispose();
    }
}
