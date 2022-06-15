package ru.gb.mygdx.game;

import static java.lang.String.format;
import static ru.gb.mygdx.game.Constants.*;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import java.util.List;


public class Physics implements Disposable
{
    private final World world1;
    private final Box2DDebugRenderer debugRenderer;
    private final PolygonShape polygonShape; //< Если инициализировать здесь, то будет брошено исключение UnsatisfiedLinkError.
    private final CircleShape  circleShape;  //< Если инициализировать здесь, то будет брошено исключение UnsatisfiedLinkError.
    private final BodyDef      bodyDef;
    private final FixtureDef   fixtureDef;


    public Physics (boolean drawAabbs, boolean drawVelocities, boolean sleepable)
    {
        world1 = new World (new Vector2 (0f, -20*G), sleepable);
        //(DO_SLEEP позволяет использовать спящие объекты: спящий объект не обсчитывается, плюс, если
        // объект не используется какое-то время, то он заснёт.)
        debugRenderer = new Box2DDebugRenderer (DRAW_BODIES, DRAW_JOINTS, drawAabbs,
                                                DRAW_INACTIVE_BODIES, drawVelocities, !DRAW_CONTACTS);
        polygonShape = new PolygonShape();
        circleShape  = new CircleShape();
        bodyDef      = new BodyDef();
        fixtureDef   = new FixtureDef();
    }

    public World getWorld1 () { return world1; }

    public void recalcWorld () {
        world1.step (1f/fps, 3, 3); //< команда посчитать физику;
        // параметры: timeStep - прошло времени с последнего обсчёта (должен оставаться постоянным),
        //            velocityIterations - ?скорость обсчёта?,
        //            positionIterations - плотность покрытия единицы длины просчитанными точками.
    }

    public void debugDraw (Camera camera) {
        debugRenderer.render (world1, camera.combined);
    }

    public void addAllObjectsToWorld (MapObjects mooo, List<String> skipList, float radiusOfMark)
    {
        for (MapObject mo : mooo)
        {
            String name = mo.getName();
            if (name != null && !skipList.contains (name))
                addMapObjectToWorld (mo, radiusOfMark);
        }
    }

/** Помещаем в мир указанный объект.
 @param radiusOfMark Если > 0f, то у прямоугольных предметов помечаем расположение тела небольшой окружностью
 радиуса radiusOfMark.
 @return Тело, к которому привязан добавленный объект.
 */
    public Body addMapObjectToWorld (MapObject mo, float radiusOfMark)
    {
        if (DEBUG) System.out.print ("Start creating body for : "+ mo.getName());

        MapProperties mprops  = mo.getProperties();
        reinitDefsWith (mprops);

        if (mo instanceof RectangleMapObject)
            fixtureDef.shape = getPolygonShapeOf ((RectangleMapObject) mo);
        else
        if (mo instanceof EllipseMapObject)
            fixtureDef.shape = getCircleShapeOf ((EllipseMapObject) mo);
        else
            throw new RuntimeException ("*** Unknown shape. ***");

        Body body = world1.createBody (bodyDef);
        body.createFixture (fixtureDef).setUserData (mo.getName());

        if (radiusOfMark > MARK_RADIUS_MIN)
            putMarkOnBodyPosition (body, radiusOfMark);

        if (DEBUG) System.out.println ("... Done.");
        return body;
    }

/** Считываем свойства объекта и (ре)инициализируем bodyDef и fixtureDef. */
    private void reinitDefsWith (MapProperties mprops)
    {
        String propName = (String) mprops.get (PROPNAME_BODYTYPE);
        bodyDef.type = BodyDef.BodyType.valueOf (propName);
        bodyDef.gravityScale   = (Float) mprops.get (PROPNAME_GRAVITYSCALE);

        fixtureDef.restitution = (float) mprops.get (PROPNAME_RESTITUTION);
        fixtureDef.friction    = (float) mprops.get (PROPNAME_FRICTION);
        fixtureDef.density     = (float) mprops.get (PROPNAME_DENSITY);
    }

/** Считая MapObject mo прямоугольником, (ре)инициализируем свойства polygonShape и bodyDef.position. */
    private PolygonShape getPolygonShapeOf (RectangleMapObject rmo)
    {
        Vector2 v = new Vector2();
        Rectangle r = rmo.getRectangle(); //< запоминаем текущее положение тела.
        float hw = r.width/2f,
              hh = r.height/2f;
        Float degrees = (Float) rmo.getProperties().get("rotation");

        if (degrees == null) {
            polygonShape.setAsBox (hw, hh);
            bodyDef.position.set (r.getCenter(v));
        }
        else {
        //Если поверхность наклонная, то удобнее переместить тело из центра контура (shape) в ЛВУгол контура
        //и повернуть на угол проивоположный тому, который указан в карте. Начало отсчёта — ЛНУгол.

            //Рисуем контур вокруг тела, смещаем центр контура (относительно тела) и поворачиваем контур
            // вокруг его геометрического центра:
            degrees *= -1f;
            polygonShape.setAsBox (hw, hh, //< размеры прямоугольника
                                   v.set (hw, -hh).rotateDeg (degrees), //< смещение относительно тела (с поворотом вектора смещения)
                                   degrees * MathUtils.degreesToRadians); //< поворот прямоугольника

            //Перемещаем тело туда, где находился ЛВУгол прямоугольника r:
            bodyDef.position.set (r.getCenter(v).add (-hw, hh));
        }
        return polygonShape;
    }

/** Считая MapObject mo кругом, (ре)инициализируем свойства polygonShape и bodyDef.position. */
    private CircleShape getCircleShapeOf (EllipseMapObject emo)
    {
        Ellipse e = emo.getEllipse();
        bodyDef.position.set (e.x + e.height/2f, e.y + e.width/2f);
        circleShape.setRadius (e.height/2f);
        return circleShape;
    }

/** Рисуем небольшой кружочек в том месте фигуры (фикстуры), где раполагается её тело. */
    private void putMarkOnBodyPosition (Body body, float radiusOfMark)
    {
        circleShape.setRadius (radiusOfMark); //< теперь это будет маркер расположения тела.
        circleShape.setPosition (new Vector2 (0f, 0f));
        fixtureDef.shape = circleShape;
        body.createFixture (fixtureDef);
    }

    @Override public void dispose () {
        circleShape.dispose();
        polygonShape.dispose();
        debugRenderer.dispose();
        world1.dispose();
    }
}
