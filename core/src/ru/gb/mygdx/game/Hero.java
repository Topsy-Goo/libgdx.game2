package ru.gb.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static ru.gb.mygdx.game.HeroStates.HS_RUNNING;
import static ru.gb.mygdx.game.HeroStates.HS_STANDING;

import java.awt.Dimension;

public class Hero
{
    private final Animator animatorRunning, animatorStanding;
    private final FPoint   center, step;
    //private dimentionHero;
    private HeroStates state;
    private float scale;


    public Hero (FPoint heroStep, FPoint heroCenter) {
        step = heroStep;
        animatorRunning = new Animator ("mario02.png", 4, 2, 15, Animation.PlayMode.LOOP,
                                        137, 0, 256, 196, 8);
        animatorStanding = new Animator ("mario02.png", 1, 1, 15, Animation.PlayMode.NORMAL,
                                         0, 0, 64, 98, 1);
        Dimension dimentionHero = animatorRunning.getTileDimention();
        center = heroCenter;
        center.x -= dimentionHero.width / 2.0f;
        center.y -= dimentionHero.height / 2.0f;
        setState (HS_STANDING);
    }

    public FPoint center () {   return center;   }

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
                    center().x,
                    center().y,
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
