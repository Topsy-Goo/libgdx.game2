package ru.gb.mygdx.game.buttons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.gb.mygdx.game.Animator;

import static ru.gb.mygdx.game.Constants.ZOOM;

public class Coin60
{
    public  final String    inmapObjectName;
    private final Animator  animator;
    private final Vector2   pinPoint;
    private final Rectangle rectShape;
    private       float     scale;
    public  final boolean   visible;

    public Coin60 (String inmapName, Animator coinAnimator, Vector2 pPoint, float cScale, boolean visibl)
    {
        inmapObjectName = inmapName;
        animator  = coinAnimator;
        pinPoint  = pPoint;
        scale     = cScale / ZOOM;
        visible   = visibl;
        rectShape = new Rectangle (pinPoint.x, pinPoint.y,
                                    animator.tileWidth * cScale, animator.tileHeight * cScale);
    }

    public Rectangle getRectShape () {    return rectShape;    }

    public void draw (SpriteBatch batch)
    {
        batch.draw (animator.getCurrentTile(),
                    pinPoint.x / ZOOM, pinPoint.y / ZOOM,
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
