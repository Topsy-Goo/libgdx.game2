package ru.gb.mygdx.game.buttons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import ru.gb.mygdx.game.Animator;

import static ru.gb.mygdx.game.Constants.COIN_W;import static ru.gb.mygdx.game.Constants.PINKB_W;import static ru.gb.mygdx.game.Constants.ZOOM;import static ru.gb.mygdx.game.Constants.coin60Scale;import static ru.gb.mygdx.game.Constants.pinkBombScale;

public class Coin60
{
    private       float scale;
    public  final String    inmapObjectName;
    private final Animator  animator;
    private final Vector2   pinPoint;
    private final Rectangle rectShape;
    public  final boolean   visible;
    public  final Vector2   impapPosition;

    public Coin60 (String inmapName, Animator coinAnimator, Vector2 pPoint, float cScale, boolean visibl, Rectangle rm)
    {
        inmapObjectName = inmapName;
        animator  = coinAnimator;
        pinPoint  = pPoint;
        scale     = cScale / ZOOM;
        visible   = visibl;
        rectShape = new Rectangle (pinPoint.x, pinPoint.y,
                                   animator.tileWidth * cScale, animator.tileHeight * cScale);
        impapPosition = new Vector2 (rm.x, rm.y);
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

    public void setScale (float factor, Vector2 mapToScreenOriginOffset) {
        if (factor > 0.0f) {
            scale = factor;

            Vector2 shift = new Vector2 (-COIN_W / 2.0f * coin60Scale,   0.0f);
            pinPoint.x = impapPosition.x - mapToScreenOriginOffset.x + shift.x;
            pinPoint.y = impapPosition.y - mapToScreenOriginOffset.y + shift.y;

            rectShape.x = pinPoint.x / ZOOM;
            rectShape.y = pinPoint.y / ZOOM;
            rectShape.setWidth (animator.tileWidth * scale);
            rectShape.setHeight (animator.tileHeight * scale);
        }
    }

    public void shift (float deltaX, float deltaY) {
        pinPoint.x -= deltaX;
        pinPoint.y -= deltaY;
        rectShape.x -= deltaX / ZOOM;
        rectShape.y -= deltaY / ZOOM;
    }

    public boolean isOverlapped (Rectangle other) {
        return rectShape.overlaps (other);
    }
}
