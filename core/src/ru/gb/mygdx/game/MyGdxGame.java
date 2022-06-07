package ru.gb.mygdx.game;

import static java.lang.String.format;
import static ru.gb.mygdx.game.HeroStates.HS_RUNNING;
import static ru.gb.mygdx.game.HeroStates.HS_STANDING;

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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Graphics;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MyGdxGame extends ApplicationAdapter
{
    public static final int wndWidth = 800, wndHeight = 600;

    private SpriteBatch batch;
    private Texture  landScape, sky;  //TODO: выбрать пейзаж и небо.
    private float    coin60Scale, zoom;
    private TiledMap map;
    private OrthogonalTiledMapRenderer ortoMapRenderer;
    private OrthographicCamera         ortoCamera;
    private Animator     animatorCoin60;
    private List<Coin60> coins60;
    private int          coins;
    private TextureRegion coinScoreImage;
    private Point         scoreImagePinPoint;
    private Lable         lableCrore; //< Для каждого размера или начертания нужно создавать отдельный объект.
    private ShapeRenderer shaper;
    private Hero hero;


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
        FPoint cameraPosition = initCamera (mapObjects, zoom, viewportWidth, viewportHeight);

/*  В редакторе карт координаты карты отсчитываются от ЛВУгла.
    RectangleMapObject.rectangle преобразует координаты карты так, чтобы они отсчитывались от ЛНУгла.
    На экране они отсчитываются от ЛНУгла.
    Камера.position, судя по всему, указывает на центр вьюпорта.
*/
        FPoint mapToScreenOriginOffset = new FPoint (cameraPosition.x - viewportWidth * zoom / 2.0f,
                                                     cameraPosition.y - viewportHeight * zoom / 2.0f);
        initCoins (mapToScreenOriginOffset, mapObjects);
        initScoreString (graphics);

        //landScape = new Texture ("");
        //sky = new Texture ("");
    }

    private void initHero (float viewportWidth, float viewportHeight)
    {
        hero = new Hero (new FPoint (viewportWidth/2.0f, viewportHeight/2.0f),
                         new FPoint (4, 4), HS_STANDING);
        hero.setScale (1.0f / zoom);
    }

    private FPoint initCamera (MapObjects mapObjects, float zoom, float viewportWidth, float viewportHeight)
    {
        ortoCamera = new OrthographicCamera (viewportWidth, viewportHeight); //< Значения можно менять.
        ortoCamera.zoom = zoom;

        //Никакой магии — объект 'точка' является RectangleMapObject с нулевыми размерами.
        RectangleMapObject rmo = (RectangleMapObject) mapObjects.get ("Старт камеры");
        Rectangle rect = rmo.getRectangle();
        FPoint cameraPosition = new FPoint (rect.x, rect.y);

        ortoCamera.position.x = cameraPosition.x;
        ortoCamera.position.y = cameraPosition.y;
        ortoCamera.update(); //< Вызываем каждый раз после изменения камеры.
        return cameraPosition;
    }

    /** Расставляем монетки оконным координатам, указанным в карте.
     @param mapToScreenOriginOffset смещение оконных координат относительно координат RectangleMapObject. */
    private void initCoins (FPoint mapToScreenOriginOffset, MapObjects mapObjects)
    {
        coin60Scale = 1.0f / zoom;
        animatorCoin60 = new Animator ("coins.png", 6, 1, 10.0f, Animation.PlayMode.LOOP, 194, 24, 62*6, 60, 8);

        coins60 = new LinkedList<>();

        //Смещение монетки нужно для того, чтобы точка на карте соответствовала не ЛНуглу монетки,
        // а середине основания.
        FPoint coinDrawShift = new FPoint (-animatorCoin60.tileWidth/2.0f * coin60Scale,
                                           0.0f);
        for (MapObject mo : mapObjects)
        {
            String name = mo.getName();
            if (name != null && name.startsWith ("Монетка"))
            {
                Rectangle rm = ((RectangleMapObject) mo).getRectangle();
                FPoint pinPoint = new FPoint (rm.x - mapToScreenOriginOffset.x + coinDrawShift.x,
                                              rm.y - mapToScreenOriginOffset.y + coinDrawShift.y);
                coins60.add (new Coin60 (animatorCoin60, pinPoint, coin60Scale));
            }
        }
        coins = coins60.size();
    }

    /** Рисуем строку счёта собранных монеток и изображение монетки рядом с ней. */
    private void initScoreString (Graphics graphics)
    {
        coinScoreImage = animatorCoin60.getTile();
        scoreImagePinPoint = new Point (0, (int)(graphics.getHeight() - animatorCoin60.tileHeight * coin60Scale));

        lableCrore = new Lable ((int)(64 / zoom)
                ,scoreImagePinPoint.x + animatorCoin60.tileWidth
                ,graphics.getHeight());
    }

    @Override public void render ()
    {
        Graphics graphics = Gdx.graphics;
        Input input = Gdx.input;
        if (input.isKeyPressed (Input.Keys.ESCAPE))
            Gdx.app.exit();

        ScreenUtils.clear (0.25f, 0.75f, 0.85f, 1.0f);

        float deltaTime = graphics.getDeltaTime();
        animatorCoin60.updateTime (deltaTime);

        FPoint delta = readDirectionsKeys (input);
        if (delta.x != 0.0f || delta.y != 0.0f)
        {
            cameraMoving (delta);
            hero.updateTime (deltaTime);
            ortoCamera.update();
            hero.setState (HS_RUNNING);
        }
        else {
            hero.updateTime (-1.0f);
            hero.setState (HS_STANDING);
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
        hero.drawShape (shaper, Color.WHITE);

        for (int i=0, n=coins60.size();  i < n;  i++) {
            Coin60 coin = coins60.get(i);

            if (coin.isOverlapped (hero.shape())) {
                coins60.remove(i);  //< В обычном цикле удалять можно без итератора.
                n--;
            }
        }
/*        Iterator<Coin60> it = coins60.iterator();
        while (it.hasNext())
        {
            Coin60 coin = it.next();
            if (coin.isOverlapped (hero.shape()))
                it.remove();
            else coin.drawShape (shaper, Color.WHITE);
        }*/
        shaper.end();
    }

/** @param delta смещение камеры по осям.  */
    private void cameraMoving (FPoint delta)
    {
        ortoCamera.position.x += delta.x;
        ortoCamera.position.y += delta.y;
        for (Coin60 coin : coins60) {
            coin.shift (delta.x, delta.y);
        }
    }

    private FPoint readDirectionsKeys (Input input)
    {
        FPoint delta = new FPoint();
        if (input.isKeyPressed (Input.Keys.D) || input.isKeyPressed (Input.Keys.RIGHT))  delta.x =  hero.step().x;
        else
        if (input.isKeyPressed (Input.Keys.A) || input.isKeyPressed (Input.Keys.LEFT))   delta.x = -hero.step().x;

        if (input.isKeyPressed (Input.Keys.W) || input.isKeyPressed (Input.Keys.UP))     delta.y =  hero.step().y;
        else
        if (input.isKeyPressed (Input.Keys.S) || input.isKeyPressed (Input.Keys.DOWN))   delta.y = -hero.step().y;
        return delta;
    }

    private void drawCoins () {
        //scoreCoin.draw (batch, zoom);
        batch.draw (coinScoreImage, scoreImagePinPoint.x, scoreImagePinPoint.y);
        for (Coin60 coin : coins60)
            coin.draw (batch, zoom);
    }

    @Override public void dispose ()
    {
        batch.dispose();
        hero.dispose();
        animatorCoin60.dispose();
        //animatorCoin60score.dispose();
        lableCrore.dispose();
        shaper.dispose();
    }
}
