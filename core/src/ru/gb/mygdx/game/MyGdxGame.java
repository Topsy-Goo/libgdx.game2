package ru.gb.mygdx.game;

import static ru.gb.mygdx.game.HeroStates.HS_RUNNING;
import static ru.gb.mygdx.game.HeroStates.HS_STANDING;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Graphics;

import java.awt.Dimension;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

public class MyGdxGame extends ApplicationAdapter
{
    public static final int wndWidth = 800, wndHeight = 600;

    private SpriteBatch batch;
    private Animator    animatorHeroRunning, animatorHeroStanding, animatorCoin60;
    private float       marioScale, coin60Scale, zoom;
    private Lable       lable32; //< Для каждого размера или начертания нужно создавать отдельный объект.
    private TiledMap    map;
    private OrthogonalTiledMapRenderer ortoMapRenderer;
    private OrthographicCamera ortoCamera;
    private List<FPoint>       coinPoints;
    //private int   xStep, yStep;
    private FPoint heroCenter, heroStep;
    HeroStates heroState;


    @Override public void create ()
    {
        Graphics graphics = Gdx.graphics;
        heroStep = new FPoint (4, 4);
        batch = new SpriteBatch();

        animatorHeroRunning = new Animator ("mario02.png", 4, 2, 15, Animation.PlayMode.LOOP,
                                            137, 0, 256, 196, 8);
        animatorHeroStanding = new Animator ("mario02.png", 1, 1, 15, Animation.PlayMode.NORMAL,
                                             0, 0, 64, 98, 1);
        animatorCoin60 = new Animator ("coins.png", 6, 1, 10.0f, Animation.PlayMode.LOOP,
                                       194, 24, 62*6, 60, 8);

        map = new TmxMapLoader().load ("maps/map2.tmx");
        ortoMapRenderer = new OrthogonalTiledMapRenderer (map);
        float viewportWidth = graphics.getWidth();
        float viewportHeight = graphics.getHeight();
        ortoCamera = new OrthographicCamera (viewportWidth, viewportHeight); //< Значения можно менять.

        zoom = ortoCamera.zoom = 1.0f;
        marioScale = 1.0f / zoom;
        coin60Scale = 1.0f / zoom;
        heroStep.scale (marioScale);

        Dimension dimentionHero = animatorHeroRunning.getTileDimention();
        heroCenter = new FPoint (graphics.getWidth()/2.0f - dimentionHero.width / 2.0f,
                                 graphics.getHeight()/2.0f - dimentionHero.height / 2.0f);

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
                                           /*-animatorCoin60.tileHeight/2.0f * coin60Scale*/0);
        coinPoints = new LinkedList<>();
        for (MapObject mo : mapObjects)
        {
            String name = mo.getName();
            if (name != null && name.startsWith("Монетка"))
            {
                Rectangle rm = ((RectangleMapObject) mo).getRectangle();
                coinPoints.add (new FPoint (rm.x - screenOriginOffset.x + coinDrawShift.x,
                                            rm.y - screenOriginOffset.y + coinDrawShift.y));
            }
        }
        heroState = HS_STANDING;
        lable32 = new Lable ((int)(32 / zoom));
    }

    @Override public void render ()
    {
        if (Gdx.input.isKeyPressed (Input.Keys.ESCAPE))
            Gdx.app.exit();
        ScreenUtils.clear (0.25f, 0.75f, 0.85f, 1);

        float deltaTime = Gdx.graphics.getDeltaTime();
        animatorCoin60.updateTime (deltaTime);

        FPoint delta = readDirectionsKeys (Gdx.input);
        boolean cameraMoved = delta.x != 0 || delta.y != 0;
        if (cameraMoved)
        {
            cameraMoving (delta);
            animatorHeroRunning.updateTime(deltaTime);
            ortoCamera.update();
            heroState = HS_RUNNING;
        }
        else {
            //animatorHeroRunning.updateTime (-1.0f);
            heroState = HS_STANDING;
        }
        ortoMapRenderer.setView (ortoCamera);
        ortoMapRenderer.render();

        batch.begin();
        drawHero ();
        drawCoins();
        //lable32.draw (batch, "ЙЦУКЕНГШЩЗХЪЖДЛОРПАВЫФЯЧСМИТЬБЮЁ йцукенгшщзхъждлорпавыфячсмитьбюё.,!:?—-«»()/*\\");
        batch.end();
    }
    
    @Override public void dispose ()
    {
        batch.dispose();
        animatorHeroRunning.dispose();
        animatorHeroStanding.dispose();
        animatorCoin60.dispose();
        lable32.dispose();
    }

/** @param delta смещение камеры по осям.
 @return TRUE, если камера сместилась, или FALSE, если смещения камеры не было. */
    private boolean cameraMoving (FPoint delta)
    {
        ortoCamera.position.x += delta.x;
        ortoCamera.position.y += delta.y;
        for (FPoint p : coinPoints) {
            p.x -= delta.x;
            p.y -= delta.y;
        }
        return true;
    }

    private FPoint readDirectionsKeys(Input input)
    {
        FPoint delta = new FPoint();
        if (input.isKeyPressed (Input.Keys.D) || input.isKeyPressed (Input.Keys.RIGHT))  delta.x = heroStep.x;
        else
        if (input.isKeyPressed (Input.Keys.A) || input.isKeyPressed (Input.Keys.LEFT))   delta.x = -heroStep.x;

        if (input.isKeyPressed (Input.Keys.W) || input.isKeyPressed (Input.Keys.UP))     delta.y = heroStep.y;
        else
        if (input.isKeyPressed (Input.Keys.S) || input.isKeyPressed (Input.Keys.DOWN))   delta.y = -heroStep.y;
        return delta;
    }

    private void drawHero ()
    {
        Animator animator = heroState == HS_STANDING ? animatorHeroStanding : animatorHeroRunning;
        batch.draw (animator.getTile(),
                    heroCenter.x,
                    heroCenter.y,
                    0, 0, animator.tileWidth, animator.tileHeight,
                    marioScale, marioScale, 0);
    }

    private void drawCoins () {
        for (FPoint p : coinPoints)
            batch.draw (animatorCoin60.getTile(),
                        p.x / zoom, p.y / zoom,
                        0, 0, animatorCoin60.tileWidth, animatorCoin60.tileHeight,
                        coin60Scale, coin60Scale, 0);
    }
}
