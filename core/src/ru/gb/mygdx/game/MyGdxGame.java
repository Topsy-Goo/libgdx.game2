package ru.gb.mygdx.game;

import static ru.gb.mygdx.game.HeroStates.HS_RUNNING;
import static ru.gb.mygdx.game.HeroStates.HS_STANDING;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

import java.util.LinkedList;
import java.util.List;

public class MyGdxGame extends ApplicationAdapter
{
    public static final int wndWidth = 800, wndHeight = 600;

    private SpriteBatch batch;
    private Texture  landScape, sky;  //TODO: выбрать пейзаж и небо.
    private Animator animatorCoin60;
    private float    coin60Scale, zoom;
    private Lable    lable32; //< Для каждого размера или начертания нужно создавать отдельный объект.
    private TiledMap map;
    private OrthogonalTiledMapRenderer ortoMapRenderer;
    private OrthographicCamera ortoCamera;
    private List<Coin60> coins60;
    private ShapeRenderer shaper;
    private Hero hero;


    @Override public void create ()
    {
        Graphics graphics = Gdx.graphics;
        batch = new SpriteBatch();
        shaper = new ShapeRenderer();

        hero = new Hero (new FPoint (graphics.getWidth()/2.0f, graphics.getHeight()/2.0f),
                         new FPoint (4, 4));

        animatorCoin60 = new Animator ("coins.png", 6, 1, 10.0f, Animation.PlayMode.LOOP,
                                       194, 24, 62*6, 60, 8);

        map = new TmxMapLoader().load ("maps/map2.tmx");
        ortoMapRenderer = new OrthogonalTiledMapRenderer (map);
        float viewportWidth = graphics.getWidth();
        float viewportHeight = graphics.getHeight();
        ortoCamera = new OrthographicCamera (viewportWidth, viewportHeight); //< Значения можно менять.

        zoom = ortoCamera.zoom = 1.0f;
        hero.setScale (1.0f / zoom);
        coin60Scale = 1.0f / zoom;

        //Никакой магии — объект 'точка' является RectangleMapObject с нулевыми размерами.
        MapObjects mapObjects = map.getLayers()
                                   .get ("Слой объектов 1")
                                   .getObjects();
        RectangleMapObject rmo = (RectangleMapObject) mapObjects.get ("Старт камеры");
        Rectangle rect = rmo.getRectangle();
        ortoCamera.position.x = rect.x;
        ortoCamera.position.y = rect.y;
        ortoCamera.update(); //< Вызываем каждый раз после изменения камеры.

/*  В редакторе карт координаты карты отсчитываются от ЛВУгла.
    RectangleMapObject.rectangle преобразует координаты карты так, чтобы они отсчитывались от ЛНУгла.
    На экране они отсчитываются от ЛНУгла.
    Камера.position, судя по всему, указывает на центр вьюпорта.
*/
        FPoint screenOriginOffset = new FPoint (rect.x - viewportWidth * zoom / 2.0f,
                                                rect.y - viewportHeight * zoom / 2.0f);
        //Расставляем монетки оконным координатам.
        FPoint coinDrawShift = new FPoint (-animatorCoin60.tileWidth/2.0f  * coin60Scale,
                                           /*-animatorCoin60.tileHeight/2.0f * coin60Scale*/0.0f);
        coins60 = new LinkedList<>();
        for (MapObject mo : mapObjects)
        {
            String name = mo.getName();
            if (name != null && name.startsWith("Монетка"))
            {
                Rectangle rm = ((RectangleMapObject) mo).getRectangle();
                FPoint pinPoint = new FPoint (rm.x - screenOriginOffset.x + coinDrawShift.x,
                                              rm.y - screenOriginOffset.y + coinDrawShift.y);
                coins60.add (new Coin60 (animatorCoin60, pinPoint, coin60Scale));
            }
        }
        hero.setState (HS_STANDING);
        lable32 = new Lable ((int)(32 / zoom));
        //landScape = new Texture ("");
        //sky = new Texture ("");
    }

    @Override public void render ()
    {
        Graphics graphics = Gdx.graphics;
        Input input = Gdx.input;
        if (input.isKeyPressed (Input.Keys.ESCAPE))
            Gdx.app.exit();

        ScreenUtils.clear (0.25f, 0.75f, 0.85f, 1);

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
        //lable32.draw (batch, "ЙЦУКЕНГШЩЗХЪЖДЛОРПАВЫФЯЧСМИТЬБЮЁ йцукенгшщзхъждлорпавыфячсмитьбюё.,!:?—-«»()/*\\");
        batch.end();

        shaper.begin (ShapeRenderer.ShapeType.Line);
        shaper.end();
    }
    
    @Override public void dispose ()
    {
        batch.dispose();
        hero.dispose();
        animatorCoin60.dispose();
        lable32.dispose();
    }

/** @param delta смещение камеры по осям.
 @return TRUE, если камера сместилась, или FALSE, если смещения камеры не было. */
    private boolean cameraMoving (FPoint delta)
    {
        ortoCamera.position.x += delta.x;
        ortoCamera.position.y += delta.y;
        for (Coin60 coin : coins60)
            coin.shift (delta.x, delta.y);
        return true;
    }

    private FPoint readDirectionsKeys(Input input)
    {
        FPoint delta = new FPoint();
        if (input.isKeyPressed (Input.Keys.D) || input.isKeyPressed (Input.Keys.RIGHT))  delta.x = hero.step().x;
        else
        if (input.isKeyPressed (Input.Keys.A) || input.isKeyPressed (Input.Keys.LEFT))   delta.x = -hero.step().x;

        if (input.isKeyPressed (Input.Keys.W) || input.isKeyPressed (Input.Keys.UP))     delta.y = hero.step().y;
        else
        if (input.isKeyPressed (Input.Keys.S) || input.isKeyPressed (Input.Keys.DOWN))   delta.y = -hero.step().y;
        return delta;
    }

    private void drawCoins () {
        for (Coin60 coin : coins60)
            coin.draw (batch, zoom);
    }
}
