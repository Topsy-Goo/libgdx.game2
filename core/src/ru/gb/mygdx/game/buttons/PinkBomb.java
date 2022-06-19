package ru.gb.mygdx.game.buttons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import ru.gb.mygdx.game.Animator;
import java.util.Iterator;
import java.util.Random;

import static ru.gb.mygdx.game.Constants.PINKB_W;import static ru.gb.mygdx.game.Constants.WAKE;
import static ru.gb.mygdx.game.Constants.ZOOM;import static ru.gb.mygdx.game.Constants.pinkBombScale;

public class PinkBomb
{
    private       Animator  animator;
    private final Animator  animatorWaiting, animatorJumping;
    private final Vector2   pinPoint;
    private final Rectangle rectShape;
    private       float     scale;
    public  final Object    triggerObject;
    private       Fixture   footSensor, aroundSensor;
    public  final String    name;
    public        boolean   aroundSensorTriggered, footSensorTriggered, jumping;
    public  final Random random = new Random (47);


    public PinkBomb (Animator a, Animator b, Vector2 pPoint, float cScale, Object trigger, String pinkname)
    {
        animator = animatorWaiting = a;
        animatorJumping = b;
        pinPoint = pPoint;
        scale    = cScale / ZOOM;
        rectShape = new Rectangle (pinPoint.x, pinPoint.y,
                                   animator.tileWidth * cScale, animator.tileHeight * cScale);
        triggerObject = trigger;
        name = pinkname;
    }

    public Rectangle getRectShape () {    return rectShape;    }

    //public void setScale (float factor) {
    //    if (factor > 0.0f)
    //        scale = factor;
    //}

    public void setFootSensor (Fixture f) {   footSensor = f;   }
    public Fixture getFootSensor () {   return footSensor;   }

    public void setAroundSensor (Fixture f) {   aroundSensor = f;   }
    public Fixture getAroundSensor () {   return aroundSensor;   }

    public void draw (SpriteBatch batch, Iterator<PinkBomb> iterator, Vector2 mapToScreenOriginOffset)
    {
        if (!isSensorsTriggered (iterator))
        {
            Body pinkBody = aroundSensor.getBody();
            Vector2 bodyPosition = pinkBody.getPosition();
            pinPoint.x = bodyPosition.x - mapToScreenOriginOffset.x - PINKB_W/2f * pinkBombScale;
            pinPoint.y = bodyPosition.y - mapToScreenOriginOffset.y - PINKB_W/2f * pinkBombScale;


            batch.draw (animator.getCurrentTile(),
                        pinPoint.x / ZOOM, pinPoint.y / ZOOM,
                        0, 0, animator.tileWidth, animator.tileHeight,
                        scale, scale, 0);
        }
    }

    private boolean isSensorsTriggered (Iterator<PinkBomb> iterator)
    {
        Body pinkBody;
        if (aroundSensorTriggered) {
        //Взрываем бомбочку:
            pinkBody = aroundSensor.getBody();
            pinkBody.getWorld().destroyBody (pinkBody);
            iterator.remove();
            //бабах!
            return true;
        }
        else if (footSensorTriggered) {
        //Удаляем footSensor, чтобы бомбочка не размахивала им во время движения:
            pinkBody = footSensor.getBody();
            pinkBody.destroyFixture (footSensor);
            setFootSensor (null);
            footSensorTriggered = false;

        //Придаём бомбочке импульс:
            Vector2 bodyCenter = pinkBody.getLocalCenter();
            pinkBody.setLinearVelocity ((float) Math.random() * 100f -50f,
                                        (float) Math.random() * -50f -50f);
            jumping = true;
            animator = animatorJumping;
            return true;
        }
        return false;
    }

    public void drawShape (ShapeRenderer shaper, Color color) {
        shaper.setColor (color);
        shaper.rect (rectShape.x, rectShape.y, rectShape.width, rectShape.height);
    }

    public void shift (float deltaX, float deltaY) {
        pinPoint.x -= deltaX;
        pinPoint.y -= deltaY;
        rectShape.x -= deltaX;
        rectShape.y -= deltaY;
    }

    //public boolean isOverlapped (Rectangle other) {   return rectShape.overlaps (other);    }
}
