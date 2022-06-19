package ru.gb.mygdx.game.physics;

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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import java.util.LinkedList;import java.util.List;

import static ru.gb.mygdx.game.Constants.*;import static ru.gb.mygdx.game.actors.ActorStates.AS_STANDING;import static ru.gb.mygdx.game.actors.MoveDirections.MD_RIGHT;import ru.gb.mygdx.game.actors.Hero;import ru.gb.mygdx.game.buttons.Coin60;import ru.gb.mygdx.game.buttons.PinkBomb;

public class Physics implements Disposable
{
    private final World world1;
    private final Box2DDebugRenderer debugRenderer;
    private final PolygonShape polygonShape; //< Если инициализировать до иниц-ции world1, то будет брошено исключение UnsatisfiedLinkError.
    private final CircleShape  circleShape;  //< Если инициализировать до иниц-ции world1, то будет брошено исключение UnsatisfiedLinkError.
    private final BodyDef      bodyDef;
    private final FixtureDef   fixtureDef;
    private       Fixture fixtureHeroFootSensor;
    private final List<Fixture> pinkSensors = new LinkedList<>();
    private       Body heroBody;
    private       Hero hero;

    public Physics (boolean drawAabbs, boolean drawVelocities)
    {
        world1 = new World (new Vector2 (0f, -20*G), SLEEPABLE_WORLD);
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

/*    public void addAllDotsToWorldAsObjects (MapObjects mooo, float radiusOfMark) {
        for (MapObject mo : mooo)
            addMapDotToWorldAsObjects (mo, radiusOfMark);
    }*/

    public void addBodyToPinkBombDot (RectangleMapObject dot, PinkBomb pink)
    {
        if (DEBUG) System.out.print ("Start creating body for : "+ dot.getName());

        reinitDefsWith (dot.getProperties());
        fixtureDef.shape = getCircleShapeOfDot (dot);

        Body body = world1.createBody (bodyDef);
        body.createFixture (fixtureDef);
        createPinkSensors (body, pink, dot, fixtureDef.shape.getRadius());

        if (MARK_RADIUS > MARK_RADIUS_MIN)
            body.createFixture (getBodyMarkFixture());

        //body.setAwake (dot.getProperties().get("awake", false, Boolean.class));
        if (DEBUG) System.out.println ("... Done.");
    }

    private void createPinkSensors (Body body, PinkBomb pink, RectangleMapObject dot, float pbRadius)
    {
        Rectangle rectC = new Rectangle (((Coin60)pink.triggerObject).getRectShape()); //< создаётся копия фигуры.
        Rectangle recbP = pink.getRectShape();
        Fixture f;
        float distance = Math.abs (rectC.y - recbP.y);
        float width = rectC.width;

        rectC.setHeight (distance);

        polygonShape.setAsBox (width/2, distance/2,
                               new Vector2 (0f, -(pbRadius + rectC.height/2f)),
                               0f);
        circleShape.setPosition (new Vector2());
        circleShape.setRadius (pbRadius);

        fixtureDef.shape = polygonShape;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        fixtureDef.density = 0f;
        fixtureDef.isSensor = true;
        pinkSensors.add (f = body.createFixture (fixtureDef));
        pink.setFootSensor (f);
        f.setUserData (pink);

        fixtureDef.shape = circleShape;
        pinkSensors.add (f = body.createFixture (fixtureDef));
        pink.setAroundSensor (f);
        f.setUserData (pink);
    }

    public void addAllObjectsToWorld (MapObjects mooo, List<String> skipList)
    {
        for (MapObject mo : mooo)
        {
            String name = mo.getName();
            if (name != null && !skipList.contains (name))
                addMapObjectToWorld (mo);
        }
    }

/** Помещаем в мир указанный объект.
 @return Тело, к которому привязан добавленный объект.
 */
    public Body addMapObjectToWorld (MapObject mo)
    {
        if (DEBUG) System.out.print ("Start creating body for : "+ mo.getName());

        MapProperties mprops  = mo.getProperties();
        boolean forHero = mo.getName().equals (NAME_MAPOBJECT_PERSONAGE);

        reinitDefsWith (mprops);

        if (mo instanceof RectangleMapObject)
            fixtureDef.shape = getPolygonShapeOf ((RectangleMapObject) mo);
        else
        if (mo instanceof EllipseMapObject)
            fixtureDef.shape = getCircleShapeOf ((EllipseMapObject) mo, forHero);
        else
            throw new RuntimeException ("*** Unknown shape. ***");

        if (forHero) {
            fixtureDef.friction = 0f;  //< чтобы тело не вращалось, иначе сенсор провернётся набок вместе с телом (существенно при gravityScale > 0).
            bodyDef.gravityScale = 0f; //< чтобы тело не падало относительно персонажа при прыжке (существенно только при использовании Body.setTransform(…)).
        }

        Body body = world1.createBody (bodyDef);
        Fixture fixture = body.createFixture (fixtureDef);

        if (forHero) {
            heroBody = body;
            hero.setFixture (fixture);
        }

        if (MARK_RADIUS > MARK_RADIUS_MIN)
            body.createFixture (getBodyMarkFixture());

        if (mprops.get (PROPNAME_FOOTSENSOR, false, Boolean.class))
            if (forHero) {
                fixtureHeroFootSensor = body.createFixture (createHeroFootSensor());
            }
        if (DEBUG) System.out.println ("... Done.");
        return body;
    }

/** Считываем свойства объекта и (ре)инициализируем bodyDef и fixtureDef. */
    private void reinitDefsWith (MapProperties mprops)
    {
        String propName        = mprops.get (PROPNAME_BODYTYPE, "StaticBody",  String.class);
        bodyDef.type = BodyDef.BodyType.valueOf (propName);
        bodyDef.gravityScale   = mprops.get (PROPNAME_GRAVITYSCALE, 0f, Float.class);
        bodyDef.awake          = mprops.get (PROPNAME_AWAKE,      true, Boolean.class);

        fixtureDef.friction    = mprops.get (PROPNAME_FRICTION,     0f, Float.class);
        fixtureDef.restitution = mprops.get (PROPNAME_RESTITUTION,  0f, Float.class);
        fixtureDef.density     = mprops.get (PROPNAME_DENSITY,      0f, Float.class);
        fixtureDef.isSensor    = false;
    }

/** Реинициализируем свойства polygonShape и bodyDef.position. */
    private PolygonShape getPolygonShapeOf (RectangleMapObject rmo)
    {
        Vector2 v = new Vector2();
        Rectangle r = rmo.getRectangle(); //< запоминаем текущее положение тела.
        float hw = r.width / 2f,
              hh = r.height / 2f;
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

/** Реинициализируем свойства polygonShape и bodyDef.position. */
    private CircleShape getCircleShapeOf (EllipseMapObject emo, boolean hero)
    {
        Ellipse e = emo.getEllipse(); //< у эллипса X и Y находятся в ЛНУглу описанного прямоугольника.
        float radius = e.height / 2f,
              bodyPosX = e.x + radius,
              bodyPosY = e.y + radius;

        bodyDef.position.set (bodyPosX, bodyPosY); //< помещаем ТЕЛО в середину фигуры.
        circleShape.setRadius (radius);
        if (hero)
            circleShape.setPosition (new Vector2 (0f, -(HERO_H / 2f - radius)));
        //else
        //    circleShape.setPosition (new Vector2());
        return circleShape;
    }

    private CircleShape getCircleShapeOfDot (RectangleMapObject rmo)
    {
        float diameter = 1f;
        if (rmo.getName().startsWith (PREFIX_PINKBOMB_NAME))
            diameter = PINKB_W * pinkBombScale;

        Rectangle dot = rmo.getRectangle();
        Ellipse e = new Ellipse (dot.x, dot.y, diameter, diameter);

        float radius = e.height / 2f,
              bodyPosX = e.x,
              bodyPosY = e.y + radius;

        bodyDef.position.set (bodyPosX, bodyPosY);
        circleShape.setRadius (radius);
        circleShape.setPosition (new Vector2());
        return circleShape;
    }

    private FixtureDef createHeroFootSensor ()
    {
        polygonShape.setAsBox (HERO_FOOTSENSOR_HW, HERO_FOOTSENSOR_HH,
                               new Vector2 (0f, -(HERO_H / 2f/* - HERO_FOOTSENSOR_HW*/)),
                               0f);
        fixtureDef.shape = polygonShape;

        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        fixtureDef.density = 0f;
        fixtureDef.isSensor = true;
        return fixtureDef;
    }

/** Рисуем небольшой кружочек в том месте фигуры (фикстуры), где раполагается её тело. */
    private FixtureDef getBodyMarkFixture ()
    {
        circleShape.setRadius (MARK_RADIUS); //< теперь это будет маркер расположения тела.
        circleShape.setPosition (new Vector2());

        fixtureDef.shape       = circleShape;
        fixtureDef.friction    = 0f;
        fixtureDef.restitution = 0f;
        fixtureDef.density     = 0f;
        fixtureDef.isSensor    = false;
        return fixtureDef;
    }

//Контакты обсчитываются только во время World.step(…).
//В слушателе контактов НЕЛЬЗЯ трогать объекты (перемещать, удалять, …) !!!!!!!!!!!!!!
    public class HeroFootContactListener implements ContactListener
    {
        private       int  heroContacts;
        private final Parabola parabola;

        public HeroFootContactListener (Hero he, Parabola par) {
            hero = he;
            parabola = par;
            world1.setContactListener (this);
        }

        //Вызывается для каждой пары пересекающихся фикстур и для каждой новой точки пересечения.
        //Фикстуры именуются как A и B.
        @Override public void beginContact (Contact contact)
        {
            Fixture fixtA = contact.getFixtureA();
            Fixture fixtB = contact.getFixtureB();
            if (fixtA == fixtureHeroFootSensor  ||  fixtB == fixtureHeroFootSensor)
            {
                if (heroContacts == 0)
                    hero.setState (AS_STANDING);
                heroContacts ++;
            }
            else
            if (fixtA == hero.getFixture()  ||  fixtB == hero.getFixture())
            {
                if (pinkSensors.contains (fixtA))
                    pinkSensorContact (fixtB, fixtA);
                else
                if (pinkSensors.contains(fixtB))
                    pinkSensorContact (fixtA, fixtB);
            }
        }

        private void pinkSensorContact (Fixture fh, Fixture fpink)
        {
            Object o = fpink.getUserData();
            PinkBomb pink = (o instanceof PinkBomb) ? (PinkBomb) o : null;
            if (pink == null)
                return;

            if (pink.getAroundSensor() == fpink)
                pink.aroundSensorTriggered = true;
            else
            if (pink.getFootSensor() == fpink)
                pink.footSensorTriggered = true;
        }

        //Вызывается для каждой пары пересекающихся фикстур и для каждой бывшей точки пересечения.
        //Фикстуры именуются как A и B.
        @Override public void endContact (Contact contact)
        {
            Fixture fixtA = contact.getFixtureA();
            Fixture fixtB = contact.getFixtureB();
            if (fixtA == fixtureHeroFootSensor  ||  fixtB == fixtureHeroFootSensor)
            {
                heroContacts --;
                if (heroContacts == 0) {
                    if (hero.getState().canTurnToFall)
                    {
                        hero.startFalling();
                        boolean right = hero.getDirection().equals (MD_RIGHT);
                        parabola.reset (!right); /* < падать начинаем из противоположной точки: например,
    обычный прыжок выполняется из точки (x; y) вперёд и вверх, то, при том же направлении персонажа,
    если падение начинается из точки (-x; y) вперёд и вниз. Это правило — некое упрощение, переиспользование уже
    имеющихся констант и вычислений. */
                    }
                }
            }
        }

    //(Следующие 2 метода работают не с сенсорами, а с твёрдыми телами.)
        @Override public void preSolve (Contact contact, Manifold oldManifold) {}
        @Override public void postSolve (Contact contact, ContactImpulse impulse) {}
    }

    @Override public void dispose () {
        circleShape.dispose();
        polygonShape.dispose();
        debugRenderer.dispose();
        world1.dispose();
    }
}
