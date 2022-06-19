package ru.gb.mygdx.game;

import static java.lang.String.format;
import static ru.gb.mygdx.game.actors.ActorStates.*;
import static ru.gb.mygdx.game.actors.MoveDirections.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Graphics;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ru.gb.mygdx.game.actors.Hero;
import ru.gb.mygdx.game.actors.MoveDirections;
import ru.gb.mygdx.game.buttons.Coin60;
import ru.gb.mygdx.game.buttons.Lable;
import ru.gb.mygdx.game.buttons.PinkBomb;
import ru.gb.mygdx.game.physics.Parabola;
import ru.gb.mygdx.game.physics.Physics;

import static ru.gb.mygdx.game.Constants.*;

public class MyGdxGame extends ApplicationAdapter
{
    private SpriteBatch batch;
    private Texture  landScape, sky;  //TODO: выбрать пейзаж и небо.
    private TiledMap map;
    private OrthogonalTiledMapRenderer ortoMapRenderer;
    private OrthographicCamera         ortoCamera;
    private Animator animatorCoin60, animatorPinkBomb_Waiting, animatorPinkBomb_Jumping;
    private List<Coin60>  coins60;
    private List<PinkBomb> pinkBombs;
    private int           coins;
    private TextureRegion coinScoreImage;
    private Vector2       scoreImagePinPoint;
    private Lable         lableCrore; //< Для каждого размера или начертания нужно создавать отдельный объект.
    private ShapeRenderer shaper;
    private Hero          hero;

    private Physics physics;
    private Body  heroBody;
    private boolean recalc_world;
    private Parabola parabola;
    Vector2 mapToScreenOriginOffset;

//--------------------------------------------------------------------------------------create
    @Override public void create ()
    {
        Graphics graphics = Gdx.graphics;
        float viewportWidth = graphics.getWidth();
        float viewportHeight = graphics.getHeight();

        batch = new SpriteBatch();
        shaper = new ShapeRenderer();
        shaper.scale (1f / ZOOM, 1f / ZOOM, 1f);

        map = new TmxMapLoader().load (FILENAME_MAP);
        parabola = new Parabola (map);
        ortoMapRenderer = new OrthogonalTiledMapRenderer (map);
/*
    В редакторе карт координаты карты отсчитываются от ЛВУгла.
    RectangleMapObject.rectangle преобразует координаты карты так, чтобы они отсчитывались от ЛНУгла.
    На экране они отсчитываются от ЛНУгла.
    Камера.position, судя по всему, указывает на центр вьюпорта.
*/
        MapObjects coinLayerObjects = map.getLayers()
                                         .get (LAYERNAME_COINS)
                                         .getObjects();
        RectangleMapObject rmo = (RectangleMapObject) coinLayerObjects.get (POINTNAME_START_CAMERA);
        Vector2 cameraPosition = rmo.getRectangle().getPosition (new Vector2());

        initCamera (cameraPosition, viewportWidth, viewportHeight);
        mapToScreenOriginOffset = new Vector2 (cameraPosition.x - viewportWidth * ZOOM / 2.0f,
                                               cameraPosition.y - viewportHeight * ZOOM / 2.0f);
        initCoins (coinLayerObjects);
        initScoreString (graphics);
        initHero (viewportWidth / 2.0f, viewportHeight / 2.0f);
        initBackground();

        createWorld (cameraPosition, !DRAW_AABBS, DRAW_VELOCITIES);
    }

    private void createWorld (Vector2 cameraPosition, boolean drawAabbs, boolean drawVelocities)
    {
        physics = new Physics (drawAabbs, drawVelocities);
        physics.new HeroFootContactListener (hero, parabola);

        MapObjects groundMapObjects = map.getLayers()
                                         .get (LAYERNAME_GROUND)
                                         .getObjects();
        List<String> skipList = Collections.singletonList (NAME_MAPOBJECT_PERSONAGE);
        physics.addAllObjectsToWorld (groundMapObjects, skipList);

        MapObjects trapsMapObjects = map.getLayers()
                                         .get (LAYERNAME_TRAPS)
                                         .getObjects();
        initPinkBombs (trapsMapObjects);
        createHeroBody (cameraPosition);
    }

    private void initPinkBombs (MapObjects trapsMapObjects)
    {
        //Смещение монетки нужно для того, чтобы точка на карте соответствовала не ЛНуглу бомбочки,
        // а середине основания.
        Vector2 shift = new Vector2 (-PINKB_W/2.0f * pinkBombScale,   0.0f);
        pinkBombs = new LinkedList<>();
        animatorPinkBomb_Waiting = new Animator (FILENAME_TEXTURE_PINKBOMB, 6, 1, PINKBOMB_FPS,
                                                 Animation.PlayMode.LOOP_PINGPONG, 0, 0,
                                                 PINKB_W *6, PINKB_H, 6);

        animatorPinkBomb_Jumping = new Animator (FILENAME_TEXTURE_PINKBOMB, 1, 1, PINKBOMB_FPS,
                                                 Animation.PlayMode.NORMAL, PINKB_W *5, 0,
                                                 PINKB_W, PINKB_H, 1);
        if (physics != null)
        for (MapObject dot : trapsMapObjects)
        {
            String name = dot.getName();
            String triggerName = dot.getProperties().get (PROPNAME_TRIGGEROBJECT, null, String.class);

            if (name != null && name.startsWith (PREFIX_PINKBOMB_NAME))
            {
                Rectangle rm = ((RectangleMapObject) dot).getRectangle();
                Vector2 pinPoint = new Vector2 (rm.x - mapToScreenOriginOffset.x + shift.x,
                                                rm.y - mapToScreenOriginOffset.y + shift.y);

                PinkBomb pink = new PinkBomb (animatorPinkBomb_Waiting, animatorPinkBomb_Jumping,
                                              pinPoint, pinkBombScale,
                                              inlineGetCoinByName (triggerName),
                                              dot.getName());
                physics.addBodyToPinkBombDot ((RectangleMapObject) dot, pink); //< на карте точки — это прямоугольники без размеров.
                pinkBombs.add (pink);
            }
        }
    }

    //private Vector2 getPinkPinPoint () {
    //}

    private Coin60 inlineGetCoinByName (String name) {
        for (Coin60 coin : coins60)
            if (coin.inmapObjectName.equals (name))
                return coin;
        return null;
    }

/** @param cameraPosition Точка, в которую будет перемещено тело персонажа после создания. */
    private void createHeroBody (Vector2 cameraPosition)
    {
        MapObject mo = map.getLayers()
                          .get (LAYERNAME_GROUND)
                          .getObjects()
                          .get (NAME_MAPOBJECT_PERSONAGE);
        heroBody = physics.addMapObjectToWorld (mo);
        heroBody.setTransform (cameraPosition, 0f);
    }

    private void initHero (float inWindowPoint_x, float inWindowPoint_y)
    {
        hero = new Hero (FILENAME_TEXTURE_HERO,
                         new Vector2 (inWindowPoint_x, inWindowPoint_y),
                         AS_STANDING, MD_RIGHT);
    }

    private void initCamera (Vector2 cameraPosition, float viewportWidth, float viewportHeight)
    {
        ortoCamera = new OrthographicCamera (viewportWidth, viewportHeight); //< Значения можно менять.
        ortoCamera.zoom = ZOOM;

        ortoCamera.position.x = cameraPosition.x;
        ortoCamera.position.y = cameraPosition.y;
        ortoCamera.update(); //< Вызываем каждый раз после изменения камеры.
    }

/** Расставляем монетки оконным координатам, указанным в карте. */
    private void initCoins (MapObjects mapObjects)
    {
        animatorCoin60 = new Animator (FILENAME_TEXTURE_COINS, 6, 1, COIN_FPS,
                                       Animation.PlayMode.LOOP, 194, 24, COIN_W *6, COIN_H, 6);
        coins60 = new LinkedList<>();

        //Смещение монетки нужно для того, чтобы точка на карте соответствовала не ЛНуглу монетки,
        // а середине основания.
        Vector2 shift = new Vector2 (-COIN_W / 2.0f * coin60Scale,   0.0f);
        for (MapObject mo : mapObjects)
        {
            String name = mo.getName();
            if (name != null && name.startsWith (PREFIX_COIN_NAME))
            {
                Rectangle rm = ((RectangleMapObject) mo).getRectangle();
                Vector2 pinPoint = new Vector2 (rm.x - mapToScreenOriginOffset.x + shift.x,
                                                rm.y - mapToScreenOriginOffset.y + shift.y);
                coins60.add (new Coin60 (name, animatorCoin60, pinPoint, coin60Scale, mo.isVisible()));
            }
        }
        coins = coins60.size();
    }

    /** Рисуем строку счёта собранных монеток и изображение монетки рядом с ней. */
    private void initScoreString (Graphics graphics)
    {
    //Нам нравится большой шрифт, поэтому не уменьшаем его вслед размеру монетки, а подгоняем к нему размер
    // изображения монетки — рисуем монетку в центре отведённого для неё места (место рассчитано на
    // полноразмерную монетку).
        coinScoreImage = animatorCoin60.getCurrentTile();
        float imgSide = coinScoreImage.getRegionWidth() * coin60Scale;
        float shift = 5f;
        scoreImagePinPoint = new Vector2 (shift,  graphics.getHeight() -imgSide -shift);

        lableCrore = new Lable ((int)(80 * coin60Scale),
                                (int)(scoreImagePinPoint.x + animatorCoin60.tileWidth * coin60Scale),
                                graphics.getHeight());
    }

    private void initBackground () {
        //landScape = new Texture ("");
        //sky = new Texture ("");
    }
//--------------------------------------------------------------------------------------render
    @Override public void render ()
    {
/* Принцип, используемый для удерживания героя и его тела в центре экрана: При движении персонажа изменяются
 координаты его тела. Вслед за ними мы двигаем камеру так, чтобы тело оставалось в центре поля зрения камеры
 (ЦПЗК). А персонажа мы просто рисуем в ЦПЗК. ЦПЗК совпадает с центром экрана. */

        ScreenUtils.clear (clearScreenColor);
        processUserInput();     //< считываем пользовательский ввод и вычисляем состояние персонажа.
        //drawBackground (batch);
        ortoMapRenderer.setView (ortoCamera);
        ortoMapRenderer.render (/*backgroundLayers*/); //< рисуем часть карты, попавшую в поле зрения камеры.
        drawBatch (batch);      //< рисуем персонажа, монетки и надписи.
        drawShaper (shaper);    //< рисуем отладочные прямоугольники вокруг персонажа и монеток.
        physics.debugDraw (ortoCamera); //< рисуем отладочные контуры тел.
    }

    private void processUserInput ()
    {
        Graphics graphics = Gdx.graphics;
        Input input = Gdx.input;

        if (input.isKeyPressed (Input.Keys.ESCAPE))
            Gdx.app.exit();

        if (input.isKeyJustPressed (Input.Keys.SPACE))
            recalc_world = !recalc_world;

        Vector2 delta = readDirectionsKeys (input);

        if (!hero.getState().isJumping)
        {
            heroBody.setLinearVelocity (0f, 0f); //< гасим импульс, полученный от наклонной поверхности
            Vector2 v = heroBody.getPosition();//new Vector2 (ortoCamera.position.x, ortoCamera.position.y);//
            delta.x = v.x - ortoCamera.position.x;
            delta.y = v.y - ortoCamera.position.y;
        }
        physics.recalcWorld();

        float deltaTime = graphics.getDeltaTime();
        animatorCoin60.updateTime (deltaTime);
        animatorPinkBomb_Waiting.updateTime (deltaTime);
        animatorPinkBomb_Jumping.updateTime (deltaTime);

        if (delta.x != 0.0f || delta.y != 0.0f)
        {
//if (DEBUG) System.out.printf("%.2f_%.2f | ", delta.x, delta.y);
            cameraMoving (delta);
            hero.updateTime (deltaTime);
            ortoCamera.update();
            //hero.setState (AS_RUNNING); << состояние здесь не обновляем, — это задача readDirectionsKeys().
        }
        else {
            hero.updateTime (-1.0f);
            if (hero.getState().canTurnToIdle)
                hero.setState (AS_STANDING);
        }
    }

    private Vector2 readDirectionsKeys (Input input)
    {
        boolean jumping = hero.getState().isJumping;

        Vector2 delta = new Vector2();
//-------------------------------continue jumping or falling
        if (jumping)
        {
            boolean right = hero.getDirection().equals (MD_RIGHT);
            boolean fall  = hero.getState().equals (AS_FALLING) == FALL;
            Vector2 jump  = hero.getJump();

            delta.x = (right) ? jump.x : -jump.x;
            delta.y = parabola.jump (delta.x, fall);

            if (hero.getState().equals (AS_JUMPING_UP)) {
                delta.x = 0f;
            }
heroBody.setTransform (heroBody.getPosition().add (delta), 0f);
            return delta;
        }
//-------------------------------reverse direction or continue running

        if (input.isKeyPressed (Input.Keys.D) || input.isKeyPressed (Input.Keys.RIGHT))
        {
            //Если смотрим в противоположную сторону, то разворачиваемся, а если смотрим в правильную сторону, то бежим.
            if (hero.getDirection().equals (MD_LEFT)) {
                hero.setDirection (MD_RIGHT);
                hero.setState (AS_STANDING);
            }
            else {
                hero.setState (AS_RUNNING);
                delta.x = hero.getStep().x;
                heroBody.setTransform (heroBody.getPosition().add (delta), 0f);
            }
        }
        else
        if (input.isKeyPressed (Input.Keys.A) || input.isKeyPressed (Input.Keys.LEFT))
        {
            //Если смотрим в противоположную сторону, то разворачиваемся, а если смотрим в правильную сторону, то бежим.
            if (hero.getDirection().equals(MD_RIGHT)) {
                hero.setDirection (MD_LEFT);
                hero.setState (AS_STANDING);
            }
            else {
                hero.setState (AS_RUNNING);
                delta.x = -hero.getStep().x;
                heroBody.setTransform (heroBody.getPosition().add (delta), 0f);
            }
        }
//-------------------------------start jumping or start climbing up
        if (input.isKeyJustPressed (Input.Keys.W) || input.isKeyJustPressed (Input.Keys.UP))
        {
            //Если мы на лестнице или перед ней, то — поднимаемся на 1 шаг вверх. (Снова помним о направлении,
            // которое стоящий персонаж должен иметь после завершения подъёма.)
            // Эту проверку нужно делать в первую очередь, а то до canClimb() очередь не дойдёт.
            if (canClimb (MD_UP)) {
                hero.setState (AS_CLIMBING);
                hero.setDirection (MD_UP);
                delta.y = hero.getStep().y;
            }
            else
            //Если мы стоим, то — прыжок вверх. (Важно сохранить направление, чтобы не приземлиться, повёрнутым
            // в противоположную сторону.)
            if (hero.getState().equals (AS_STANDING))
            {
                hero.setState (AS_JUMPING_UP);
                startJumping (delta, UP);
            }
            else
            //Если мы бежим, то — прыжок вперёд. (Также важно сохранить направление.)
            if (hero.getState().equals (AS_RUNNING))
            {
                hero.setState (AS_JUMPING_FORTH);
                startJumping (delta, FORTH);
            }
            if (delta.y != 0f)
                heroBody.setTransform (heroBody.getPosition().add (delta), 0f);
        }
//-------------------------------start climbing down
        else
        if (input.isKeyJustPressed (Input.Keys.S) || input.isKeyJustPressed (Input.Keys.DOWN)) {
            if (canClimb (MD_DOWN))
            {
                hero.setState (AS_CLIMBING);
                hero.setDirection (MD_DOWN);
                delta.y = -hero.getStep().y;
            }
            if (delta.y != 0f)
                heroBody.setTransform (heroBody.getPosition().add (delta), 0f);
        }
//-------------------------------climbing
        //Сейчас мы находимся в состоянии AS_CLIMBING. (Для состояний AS_JUMPING_xx нажатия клавиш направления не обрабатываются.)
        else
        if (input.isKeyPressed (Input.Keys.W) || input.isKeyPressed (Input.Keys.UP))
        {
            if (canClimb (MD_UP))
                delta.y = hero.getStep().y;
            if (delta.y != 0f)
                heroBody.setTransform (heroBody.getPosition().add (delta), 0f);
        }
        else
        if (input.isKeyPressed (Input.Keys.S) || input.isKeyPressed (Input.Keys.DOWN))
        {
            //Если мы на лестнице, то — опускаемся на 1 шаг вниз. (Помним о направлении,
            // которое стоящий персонаж должен иметь после завершения спуска.)
            if (canClimb (MD_DOWN))
                delta.y = -hero.getStep().y;
            if (delta.y != 0f)
                heroBody.setTransform (heroBody.getPosition().add (delta), 0f);
        }
        return delta;
    }

    public static boolean UP = true, FORTH = false, FALL = true;

    private void startJumping (Vector2 delta, boolean up)
    {
        delta.x = hero.getJump().x;

        boolean right = hero.getDirection().equals (MD_RIGHT);
        parabola.reset (right);
        if (!right) delta.x *= -1f;
        delta.y = parabola.jump (delta.x, !FALL);
        heroBody.setTransform (heroBody.getPosition().add (delta), 0f);

        if (up) delta.x = 0f;
    }

/** @param delta смещение камеры по осям.  */
    private void cameraMoving (Vector2 delta)
    {
        ortoCamera.position.x += delta.x;
        ortoCamera.position.y += delta.y;
        mapToScreenOriginOffset.x += delta.x;
        mapToScreenOriginOffset.y += delta.y;

        for (Coin60 coin : coins60)
            coin.shift (delta.x, delta.y);

        for (PinkBomb pinkBomb : pinkBombs)
            pinkBomb.shift (delta.x, delta.y);
    }

    /** Выясняем, можем ли мы карабкаться вверх, например, по лестнице. Персонаж <b>может</b> подниматься
     по лестнице, если он находится на лестнице или стоит у её подножья, и не находится не верхней ступеньке.
     Персонаж <b>не может</b> подниматься по лестнице во всех отсальных случаях. */
    private boolean canClimb (MoveDirections direction)  //TODO: заглушка.
    {
/*        if (direction != null)
            if (direction.equals (MD_UP))
                return Gdx.input.isKeyPressed (Input.Keys.CONTROL_RIGHT);
            else
            if (direction.equals (MD_DOWN))
                return Gdx.input.isKeyPressed (Input.Keys.CONTROL_RIGHT);*/
        return false;
    }

    private void drawBackground (SpriteBatch batch)
    {
/*  Блоков batch.begin() … batch.end() может быть несколько.
        batch.begin();
        batch.draw (landScape, 0,0, graphics.getWidth(), graphics.getHeight());
        batch.draw (sky, 0,0, graphics.getWidth(), graphics.getHeight());
        batch.end();*/
    }

    private void drawBatch (SpriteBatch batch) {
        batch.begin();
        hero.draw (batch);
        drawCoins (batch);
        drawPinkBombs (batch);
        lableCrore.draw (batch, format (" %d/%d", coins - coins60.size(), coins));
        batch.end();
    }

    private void drawCoins (SpriteBatch batch)
    {
        batch.draw (coinScoreImage, scoreImagePinPoint.x, scoreImagePinPoint.y, 0,0,
                    coinScoreImage.getRegionWidth(), coinScoreImage.getRegionHeight(),
                    coin60Scale, coin60Scale, 0f);
        for (Coin60 coin : coins60)
            if (coin.visible)
                coin.draw (batch);
    }

    private void drawPinkBombs (SpriteBatch batch)
    {
        Iterator<PinkBomb> iterator = pinkBombs.iterator();
        while (iterator.hasNext())
            iterator.next().draw (batch, iterator, mapToScreenOriginOffset);
    }

    private void drawShaper (ShapeRenderer shaper)
    {
        shaper.begin (ShapeRenderer.ShapeType.Line);
        hero.drawShape (shaper, onscreenTextColor);

        Iterator<Coin60> itc = coins60.iterator();
        while (itc.hasNext()) {
            Coin60 coin = itc.next();
            if (coin.isOverlapped (hero.shape()))
                itc.remove();
            else coin.drawShape (shaper, Color.WHITE);
        }
        for (PinkBomb pb : pinkBombs)
            pb.drawShape (shaper, Color.WHITE);
        shaper.end();
    }
//-------------------------------------------------------------------------------------dispose
    @Override public void dispose () {
        hero.dispose();
        animatorCoin60.dispose();
        animatorPinkBomb_Waiting.dispose();
        animatorPinkBomb_Jumping.dispose();
        lableCrore.dispose();
        shaper.dispose();
        physics.dispose();
        batch.dispose();
    }
}
