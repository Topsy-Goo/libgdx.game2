package ru.gb.mygdx.game.buttons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import ru.gb.mygdx.game.Animator;

public class Coin60
{
    private final Animator  animator;
    private final Vector2   pinPoint;
    private final Rectangle rectShape;
    private       float     scale;
    public  final boolean   visible;

    public Coin60 (Animator coinAnimator, Vector2 pPoint, float cScale, boolean visibl)
    {
        animator = coinAnimator;
        pinPoint = pPoint;
        scale    = cScale;
        visible  = visibl;
        rectShape = new Rectangle (pinPoint.x, pinPoint.y,
                                   animator.tileWidth * scale, animator.tileHeight * scale);
    }

    public void draw (SpriteBatch batch, float zoom)
    {
        batch.draw (animator.getCurrentTile(),
                    pinPoint.x / zoom, pinPoint.y / zoom,
                    0, 0, animator.tileWidth, animator.tileHeight,
                    scale, scale, 0);
    }

    public void drawShape (ShapeRenderer shaper, Color color) {
        shaper.setColor (color);
        shaper.rect (rectShape.x, rectShape.y,
                     rectShape.width, rectShape.height);
    }

    public void setScale (float factor) {
        if (factor > 0.0f) {
            scale = factor;
        }
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
