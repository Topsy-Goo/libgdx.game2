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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Graphics;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ru.gb.mygdx.game.actors.Hero;
import ru.gb.mygdx.game.actors.MoveDirections;
import ru.gb.mygdx.game.buttons.Coin60;
import ru.gb.mygdx.game.buttons.Lable;

public class MyGdxGame extends ApplicationAdapter
{
    public static final int wndWidth = 800, wndHeight = 600;
    public static final Color onscreenTextColor = new Color (0.7f, 1.0f, 0.9f, 1.0f);
    public static final boolean DEBUG = true;
    public static final String heroTextureFileName = "mario02.png";

    private SpriteBatch batch;
    private Texture  landScape, sky;  //TODO: выбрать пейзаж и небо.
    private float    coin60Scale, zoom;
    private TiledMap                   map;
    private OrthogonalTiledMapRenderer ortoMapRenderer;
    private OrthographicCamera         ortoCamera;
    private Animator                   animatorCoin60;
    private List<Coin60>               coins60;
    private int                        coins;
    private TextureRegion coinScoreImage;
    private Point                      scoreImagePinPoint;
    private Lable                      lableCrore; //< Для каждого размера или начертания нужно создавать отдельный объект.
    private ShapeRenderer              shaper;
    private Hero                       hero;


    @Override public void create ()
    {
        Graphics graphics = Gdx.graphics;
        float viewportWidth = graphics.getWidth();
        float viewportHeight = graphics.getHeight();

        zoom = 1.0f;
        batch = new SpriteBatch();
        shaper = new ShapeRenderer();

        map = new TmxMapLoader().load ("maps/map2.tmx");
        ortoMapRenderer = new OrthogonalTiledMapRenderer (map);

        initHero (viewportWidth, viewportHeight);

        MapObjects mapObjects = map.getLayers()
                                   .get ("Слой объектов 1")
                                   .getObjects();
        Vector2 cameraPosition = initCamera (mapObjects, zoom, viewportWidth, viewportHeight);

/*  В редакторе карт координаты карты отсчитываются от ЛВУгла.
    RectangleMapObject.rectangle преобразует координаты карты так, чтобы они отсчитывались от ЛНУгла.
    На экране они отсчитываются от ЛНУгла.
    Камера.position, судя по всему, указывает на центр вьюпорта.
*/
        Vector2 mapToScreenOriginOffset = new Vector2 (cameraPosition.x - viewportWidth * zoom / 2.0f,
                                                       cameraPosition.y - viewportHeight * zoom / 2.0f);
        initCoins (mapToScreenOriginOffset, mapObjects);
        initScoreString (graphics);

        //landScape = new Texture ("");
        //sky = new Texture ("");
    }

    private void initHero (float viewportWidth, float viewportHeight)
    {
        hero = new Hero (heroTextureFileName,
                         new Vector2 (viewportWidth/2.0f, viewportHeight/2.0f),
                         new Vector2 (4, 4), AS_STANDING, MD_RIGHT);
        hero.setScale (1.0f / zoom);
    }

    private Vector2 initCamera (MapObjects mapObjects, float zoom, float viewportWidth, float viewportHeight)
    {
        ortoCamera = new OrthographicCamera (viewportWidth, viewportHeight); //< Значения можно менять.
        ortoCamera.zoom = zoom;

        //Никакой магии — объект 'точка' является RectangleMapObject с нулевыми размерами.
        RectangleMapObject rmo = (RectangleMapObject) mapObjects.get ("Старт камеры");
        Rectangle rect = rmo.getRectangle();
        Vector2 cameraPosition = new Vector2 (rect.x, rect.y);

        ortoCamera.position.x = cameraPosition.x;
        ortoCamera.position.y = cameraPosition.y;
        ortoCamera.update(); //< Вызываем каждый раз после изменения камеры.
        return cameraPosition;
    }

    /** Расставляем монетки оконным координатам, указанным в карте.
     @param mapToScreenOriginOffset смещение оконных координат относительно координат RectangleMapObject. */
    private void initCoins (Vector2 mapToScreenOriginOffset, MapObjects mapObjects)
    {
        coin60Scale = 1.0f / zoom;
        animatorCoin60 = new Animator ("coins.png", 6, 1, 10.0f, Animation.PlayMode.LOOP, 194, 24, 62*6, 60, 8);

        coins60 = new LinkedList<>();

        //Смещение монетки нужно для того, чтобы точка на карте соответствовала не ЛНуглу монетки,
        // а середине основания.
        Vector2 coinDrawShift = new Vector2 (-animatorCoin60.tileWidth/2.0f * coin60Scale,
                                             0.0f);
        for (MapObject mo : mapObjects)
        {
            String name = mo.getName();
            if (name != null && name.startsWith ("Монетка"))
            {
                Rectangle rm = ((RectangleMapObject) mo).getRectangle();
                Vector2 pinPoint = new Vector2 (rm.x - mapToScreenOriginOffset.x + coinDrawShift.x,
                                                rm.y - mapToScreenOriginOffset.y + coinDrawShift.y);
                coins60.add (new Coin60 (animatorCoin60, pinPoint, coin60Scale, mo.isVisible()));
            }
        }
        coins = coins60.size();
    }

    /** Рисуем строку счёта собранных монеток и изображение монетки рядом с ней. */
    private void initScoreString (Graphics graphics)
    {
        coinScoreImage = animatorCoin60.getCurrentTile();
        scoreImagePinPoint = new Point (0, (int)(graphics.getHeight() - animatorCoin60.tileHeight * coin60Scale));

        lableCrore = new Lable ((int)(64 / zoom)
                ,scoreImagePinPoint.x + animatorCoin60.tileWidth
                ,graphics.getHeight());
    }
//--------------------------------------------------------------------------------------------
    @Override public void render ()
    {
        Graphics graphics = Gdx.graphics;
        Input input = Gdx.input;
        if (input.isKeyPressed (Input.Keys.ESCAPE))
            Gdx.app.exit();

        ScreenUtils.clear (0.25f, 0.75f, 0.85f, 1.0f);

        float deltaTime = graphics.getDeltaTime();
        animatorCoin60.updateTime (deltaTime);

        Vector2 delta = readDirectionsKeys (input);
        if (delta.x != 0.0f || delta.y != 0.0f)
        {
            cameraMoving (delta);
            hero.updateTime (deltaTime);
            ortoCamera.update();
            //hero.setState (AS_RUNNING);
        }
        else {
            hero.updateTime (-1.0f);
            hero.setState (AS_STANDING);
        }
        ortoMapRenderer.setView (ortoCamera);

/*        batch.begin();
        batch.draw (landScape, 0,0, graphics.getWidth(), graphics.getHeight());
        batch.draw (sky, 0,0, graphics.getWidth(), graphics.getHeight());
        batch.end();*/

        ortoMapRenderer.render();

        batch.begin();
        hero.draw (batch);
        drawCoins();
        lableCrore.draw (batch, format (" %d/%d", coins - coins60.size(), coins));
        batch.end();

        shaper.begin (ShapeRenderer.ShapeType.Line);
        hero.drawShape (shaper, onscreenTextColor/*Color.WHITE*/);

/*        for (int i=0, n=coins60.size();  i < n;  i++) {
            Coin60 coin = coins60.get(i);

            if (coin.isOverlapped (hero.shape())) {
                coins60.remove(i);  //< В обычном цикле удалять можно без итератора.
                n--;
            }
            else coin.drawShape (shaper, onscreenTextColor);
        }*/
        Iterator<Coin60> it = coins60.iterator();
        while (it.hasNext())
        {
            Coin60 coin = it.next();
            if (coin.isOverlapped (hero.shape()))
                it.remove();
            else coin.drawShape (shaper, Color.WHITE);
        }
        shaper.end();
    }

/** @param delta смещение камеры по осям.  */
    private void cameraMoving (Vector2 delta)
    {
        ortoCamera.position.x += delta.x;
        ortoCamera.position.y += delta.y;
        for (Coin60 coin : coins60) {
            coin.shift (delta.x, delta.y);
        }
    }

    private Vector2 readDirectionsKeys (Input input)
    {
        Vector2 delta = new Vector2();
        if (input.isKeyPressed (Input.Keys.D) || input.isKeyPressed (Input.Keys.RIGHT))
        {
            //Если смотрим в противоположную сторону, то разворачиваемся, а если смотрим в правильную сторону, то бежим.
            if (hero.getDirection().equals(MD_LEFT)) {
                hero.setDirection (MD_RIGHT);
                hero.setState (AS_STANDING);
            }
            else {
                hero.setState (AS_RUNNING);
                delta.x = hero.step().x;
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
                delta.x = -hero.step().x;
            }
        }

        if (input.isKeyJustPressed (Input.Keys.W) || input.isKeyJustPressed (Input.Keys.UP))
        {
            //Если мы на лестнице или перед ней, то — поднимаемся на 1 шаг вверх. (Снова помним о направлении,
            // которое стоящий персонаж должен иметь после завершения подъёма.)
            // Эту проверку нужно делать в первую очередь, а то до canClimb() очередь не дойдёт.
            if (canClimb (MD_UP)) {
                hero.setState (AS_CLIMBING);
                delta.y = hero.step().y;
            }
            else
            //Если мы стоим, то — прыжок вверх. (Важно сохранить направление, чтобы не приземлиться, повёрнутым
            // в противоположную сторону.)
            if (hero.getState().equals (AS_STANDING)) {
                hero.setState (AS_JUMPING_UP);
                delta.y = hero.jump().y;
            }
            else
            //Если мы бежим, то — прыжок вперёд. (Также важно сохранить направление.)
            if (hero.getState().equals (AS_RUNNING)) {
                hero.setState (AS_JUMPING_FORTH);
                delta.x = hero.jump().x;
                delta.y = hero.jump().y;
            }
            //else if (DEBUG) throw new RuntimeException ("Неизвестное состояние персонажа при isKeyJustPressed (UP).");
        }
        else
        if (input.isKeyJustPressed (Input.Keys.S) || input.isKeyJustPressed (Input.Keys.DOWN))
        {
            //Если мы на лестнице или на её верхней ступеньке, то — опускаемся на 1 шаг вниз. (Помним о направлении,
            // которое стоящий персонаж должен иметь после завершения спуска.)
            if (canClimb (MD_DOWN)) {
                hero.setState (AS_CLIMBING);
                delta.y = -hero.step().y;
            }
        }
        else
        //(Сейчас мы находимся в одном из след.состояний: AS_JUMPING_UP, AS_JUMPING_FORTH, AS_CLIMBING.)
        if (input.isKeyPressed (Input.Keys.W) || input.isKeyPressed (Input.Keys.UP))
        {
            if (canClimb (MD_UP)/*hero.getState().equals(AS_CLIMBING)*/)
                delta.y = hero.step().y;
        }
        else
        if (input.isKeyPressed (Input.Keys.S) || input.isKeyPressed (Input.Keys.DOWN))
        {
            //Если мы на лестнице, то — опускаемся на 1 шаг вниз. (Помним о направлении,
            // которое стоящий персонаж должен иметь после завершения спуска.)
            if (canClimb (MD_DOWN)/*hero.getState().equals(AS_CLIMBING)*/)
                delta.y = -hero.step().y;
        }
        return delta;
    }

    /** Выясняем, можем ли мы карабкаться вверх, например, по лестнице. Персонаж <b>может</b> подниматься
     по лестнице, если он находится на лестнице или стоит у её подножья, и не находится не верхней ступеньке.
     Персонаж <b>не может</b> подниматься по лестнице во всех отсальных случаях. */
    private boolean canClimb (MoveDirections direction)  //TODO: заглушка.
    {
        if (direction != null)
            if (direction.equals (MD_UP))
                return Gdx.input.isKeyPressed (Input.Keys.CONTROL_RIGHT);
            else
            if (direction.equals (MD_DOWN))
                return Gdx.input.isKeyPressed (Input.Keys.CONTROL_RIGHT);
        return false;
    }

    private void drawCoins () {
        batch.draw (coinScoreImage, scoreImagePinPoint.x, scoreImagePinPoint.y);
        for (Coin60 coin : coins60)
            if (coin.visible)
                coin.draw (batch, zoom);
    }
//--------------------------------------------------------------------------------------------
    @Override public void dispose ()
    {
        batch.dispose();
        hero.dispose();
        animatorCoin60.dispose();
        lableCrore.dispose();
        shaper.dispose();
    }
}
