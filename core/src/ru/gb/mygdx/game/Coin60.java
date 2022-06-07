package ru.gb.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Coin60
{
    private final Animator  animator;
    private final FPoint    pinPoint;
    private final Rectangle rectShape;
    private       float     scale;

    public Coin60 (Animator coinAnimator, FPoint pPoint, float cScale) {
        animator = coinAnimator;
        pinPoint = pPoint;
        scale = cScale;
        rectShape = new Rectangle (pinPoint.x, pinPoint.y, animator.tileWidth, animator.tileHeight);
    }

    public void draw (SpriteBatch batch, float zoom)
    {
        batch.draw (animator.getTile(),
                    pinPoint.x / zoom, pinPoint.y / zoom,
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

    public void shift (float deltaX, float deltaY) {
        pinPoint.x -= deltaX;
        pinPoint.y -= deltaY;
        rectShape.x -= deltaX;
        rectShape.y -= deltaY;
    }

    public boolean isOverlapped (Rectangle other) {
        return rectShape.overlaps (other);
    }
}
