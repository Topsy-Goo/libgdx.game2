package ru.gb.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Coin60
{
    private final Animator animator;
    private final FPoint pinPoint;
    private       Rectangle rectangle;
    private       float scale;

    public Coin60 (Animator coinAnimator, FPoint pPoint, float cScale) {
        animator = coinAnimator;
        pinPoint = pPoint;
        scale = cScale;
    }

    public void draw (SpriteBatch batch, float zoom)
    {
        batch.draw (animator.getTile(),
                    pinPoint.x / zoom, pinPoint.y / zoom,
                    0, 0, animator.tileWidth, animator.tileHeight,
                    scale, scale, 0);

    }

    public void setScale (float factor) {
        if (factor > 0.0f)
            scale = factor;
    }

    public void shift (float deltaX, float deltaY) {
        pinPoint.x -= deltaX;
        pinPoint.y -= deltaY;
    }
}
