package ru.gb.mygdx.game.screens;

import static java.lang.String.format;
import static ru.gb.mygdx.game.Constants.*;
import static ru.gb.mygdx.game.actors.ActorStates.*;
import static ru.gb.mygdx.game.actors.MoveDirections.*;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import ru.gb.mygdx.game.Animator;
import ru.gb.mygdx.game.actors.Hero;
import ru.gb.mygdx.game.actors.MoveDirections;
import ru.gb.mygdx.game.buttons.Coin60;
import ru.gb.mygdx.game.buttons.Lable;
import ru.gb.mygdx.game.buttons.PinkBomb;
import ru.gb.mygdx.game.physics.Parabola;
import ru.gb.mygdx.game.physics.Physics;

public class GameScreen implements Screen
{
    private final Game game;
    private SpriteBatch batch;
    private TiledMap map;
    private OrthogonalTiledMapRenderer ortoMapRenderer;
    private OrthographicCamera         ortoCamera;
    private Animator animatorCoin60, animatorPinkBomb_Waiting, animatorPinkBomb_Jumping;
    private List<Coin60>  coins60;
    private List<PinkBomb> pinkBombs;
    private int           coins;
    private TextureRegion coinScoreImage;
    private Vector2       scoreImagePinPoint;
    private Lable         lableCrore; //< ?????? ?????????????? ?????????????? ?????? ???????????????????? ?????????? ?????????????????? ?????????????????? ????????????.
    private ShapeRenderer shaper;
    private Hero          hero;

    private Physics physics;
    private Body  heroBody;
    private boolean recalc_world;
    private Parabola parabola;
    private Vector2 mapToScreenOriginOffset;

    private Music music, coinSound;
    private Texture landScape;
    private boolean gameOver;

    public GameScreen (Game g) {
        game = g;
        create();
    }
//--------------------------------------------------------------------------------------create

    public void create ()
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
    ?? ?????????????????? ???????? ???????????????????? ?????????? ?????????????????????????? ???? ????????????.
    RectangleMapObject.rectangle ?????????????????????? ???????????????????? ?????????? ??????, ?????????? ?????? ?????????????????????????? ???? ????????????.
    ???? ???????????? ?????? ?????????????????????????? ???? ????????????.
    ????????????.position, ???????? ???? ??????????, ?????????????????? ???? ?????????? ????????????????.
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

        createWorld (cameraPosition, DRAW_AABBS, DRAW_VELOCITIES);
        initMusic();
    }

    private void initMusic() {
        music = Gdx.audio.newMusic (Gdx.files.internal (FILENAME_MUSIC));
        music.setLooping (false);
        music.setVolume (0.05f);
        music.play();
        coinSound = Gdx.audio.newMusic (Gdx.files.internal (FILENAME_SOUND_COIN));
        coinSound.setLooping (false);
        coinSound.setVolume (0.15f);
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
        //???????????????? ?????????????? ?????????? ?????? ????????, ?????????? ?????????? ???? ?????????? ?????????????????????????????? ???? ???????????? ????????????????,
        // ?? ???????????????? ??????????????????.
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
                physics.addBodyToPinkBombDot ((RectangleMapObject) dot, pink); //< ???? ?????????? ?????????? ??? ?????? ???????????????????????????? ?????? ????????????????.
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

/** @param cameraPosition ??????????, ?? ?????????????? ?????????? ???????????????????? ???????? ?????????????????? ?????????? ????????????????. */
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
        ortoCamera = new OrthographicCamera (viewportWidth, viewportHeight); //< ???????????????? ?????????? ????????????.
        ortoCamera.zoom = ZOOM;

        ortoCamera.position.x = cameraPosition.x;
        ortoCamera.position.y = cameraPosition.y;
        ortoCamera.update(); //< ???????????????? ???????????? ?????? ?????????? ?????????????????? ????????????.
    }

/** ?????????????????????? ?????????????? ?????????????? ??????????????????????, ?????????????????? ?? ??????????. */
    private void initCoins (MapObjects mapObjects)
    {
        animatorCoin60 = new Animator (FILENAME_TEXTURE_COINS, 6, 1, COIN_FPS,
                                       Animation.PlayMode.LOOP, 194, 24, COIN_W *6, COIN_H, 6);
        coins60 = new LinkedList<>();

        //???????????????? ?????????????? ?????????? ?????? ????????, ?????????? ?????????? ???? ?????????? ?????????????????????????????? ???? ???????????? ??????????????,
        // ?? ???????????????? ??????????????????.
        Vector2 shift = new Vector2 (-COIN_W / 2.0f * coin60Scale,   0.0f);
        for (MapObject mo : mapObjects)
        {
            String name = mo.getName();
            if (name != null && name.startsWith (PREFIX_COIN_NAME))
            {
                Rectangle rm = ((RectangleMapObject) mo).getRectangle();
                Vector2 pinPoint = new Vector2 (rm.x - mapToScreenOriginOffset.x + shift.x,
                                                rm.y - mapToScreenOriginOffset.y + shift.y);
                coins60.add (new Coin60 (name, animatorCoin60, pinPoint, coin60Scale, mo.isVisible(), rm));
            }
        }
        coins = coins60.size();
    }

    /** ???????????? ???????????? ?????????? ?????????????????? ?????????????? ?? ?????????????????????? ?????????????? ?????????? ?? ??????. */
    private void initScoreString (Graphics graphics)
    {
    //?????? ???????????????? ?????????????? ??????????, ?????????????? ???? ?????????????????? ?????? ?????????? ?????????????? ??????????????, ?? ?????????????????? ?? ???????? ????????????
    // ?????????????????????? ?????????????? ??? ???????????? ?????????????? ?? ???????????? ?????????????????????? ?????? ?????? ?????????? (?????????? ???????????????????? ????
    // ???????????????????????????? ??????????????).
        coinScoreImage = animatorCoin60.getCurrentTile();
        float imgSide = coinScoreImage.getRegionWidth() * coin60Scale;
        float shift = 5f;
        scoreImagePinPoint = new Vector2 (shift,  graphics.getHeight() -imgSide -shift);

        lableCrore = new Lable ((int)(80 * coin60Scale),
                                (int)(scoreImagePinPoint.x + animatorCoin60.tileWidth * coin60Scale),
                                graphics.getHeight());
    }

    private void initBackground () {
        landScape = new Texture (FILENAME_LANDSCAPE);
        //sky = new Texture ("");
    }
//--------------------------------------------------------------------------------------render

    @Override public void render (float delta)
    {
        checkGameOver();
        if (!gameOver)
        {
            System.out.print("\rZOOM: "+ Float.valueOf(ZOOM).toString());
    /*
     ??????????????, ???????????????????????? ?????? ?????????????????????? ?????????? ?? ?????? ???????? ?? ???????????? ????????????: ?????? ???????????????? ?????????????????? ????????????????????
     ???????????????????? ?????? ????????. ?????????? ???? ???????? ???? ?????????????? ???????????? ??????, ?????????? ???????? ???????????????????? ?? ???????????? ???????? ???????????? ????????????
     (????????). ?? ?????????????????? ???? ???????????? ???????????? ?? ????????. ???????? ?????????????????? ?? ?????????????? ????????????.
     */
            ScreenUtils.clear (clearScreenColor);
            processUserInput();     //< ?????????????????? ???????????????????????????????? ???????? ?? ?????????????????? ?????????????????? ??????????????????.
            drawBackground (batch);
            ortoMapRenderer.setView (ortoCamera);
            ortoMapRenderer.render(); //< ???????????? ?????????? ??????????, ???????????????? ?? ???????? ???????????? ????????????.
            drawBatch (batch);      //< ???????????? ??????????????????, ?????????????? ?? ??????????????.
            drawShaper (shaper);    //< ???????????? ???????????????????? ???????????????????????????? ???????????? ?????????????????? ?? ??????????????.
            if (DEBUG) physics.debugDraw (ortoCamera); //< ???????????? ???????????????????? ?????????????? ??????.
        }
        else if (!coinSound.isPlaying()) //< ?????????? ?????????????????? ?????????????? ???????? ????????????????????.
            exitThisScreen();
    }

    private void checkGameOver () {
        gameOver = gameOver || coins60.isEmpty();
    }

    private void exitThisScreen () {
        dispose();
        try {
            Thread.sleep (1000);
        }
        catch (InterruptedException e) {e.printStackTrace();}
        game.setScreen (new GameOverScreen (game));
    }

    private void processUserInput ()
    {
        Graphics graphics = Gdx.graphics;
        Input input = Gdx.input;

        if (Gdx.input.isKeyJustPressed (Input.Keys.ESCAPE))
            gameOver = true;

        if (input.isKeyPressed (Input.Keys.SPACE))
            recalc_world = !recalc_world;

        if (input.isKeyJustPressed (Input.Keys.NUMPAD_ADD))
            updateZoom (-0.2f);
        else
        if (input.isKeyJustPressed (Input.Keys.NUMPAD_SUBTRACT))
            updateZoom ( 0.2f);

        Vector2 delta = readDirectionsKeys (input);

        if (!hero.getState().isJumping)
        {
            heroBody.setLinearVelocity (0f, 0f); //< ?????????? ??????????????, ???????????????????? ???? ?????????????????? ??????????????????????
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
            //hero.setState (AS_RUNNING); << ?????????????????? ?????????? ???? ??????????????????, ??? ?????? ???????????? readDirectionsKeys().
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
            //???????? ?????????????? ?? ?????????????????????????????? ??????????????, ???? ??????????????????????????????, ?? ???????? ?????????????? ?? ???????????????????? ??????????????, ???? ??????????.
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
            //???????? ?????????????? ?? ?????????????????????????????? ??????????????, ???? ??????????????????????????????, ?? ???????? ?????????????? ?? ???????????????????? ??????????????, ???? ??????????.
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
            //???????? ???? ???? ???????????????? ?????? ?????????? ??????, ???? ??? ?????????????????????? ???? 1 ?????? ??????????. (?????????? ???????????? ?? ??????????????????????,
            // ?????????????? ?????????????? ???????????????? ???????????? ?????????? ?????????? ???????????????????? ??????????????.)
            // ?????? ???????????????? ?????????? ???????????? ?? ???????????? ??????????????, ?? ???? ???? canClimb() ?????????????? ???? ????????????.
            if (canClimb (MD_UP)) {
                hero.setState (AS_CLIMBING);
                hero.setDirection (MD_UP);
                delta.y = hero.getStep().y;
            }
            else
            //???????? ???? ??????????, ???? ??? ???????????? ??????????. (?????????? ?????????????????? ??????????????????????, ?????????? ???? ????????????????????????, ????????????????????
            // ?? ?????????????????????????????? ??????????????.)
            if (hero.getState().equals (AS_STANDING))
            {
                hero.setState (AS_JUMPING_UP);
                startJumping (delta, UP);
            }
            else
            //???????? ???? ??????????, ???? ??? ???????????? ????????????. (?????????? ?????????? ?????????????????? ??????????????????????.)
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
        //???????????? ???? ?????????????????? ?? ?????????????????? AS_CLIMBING. (?????? ?????????????????? AS_JUMPING_xx ?????????????? ???????????? ?????????????????????? ???? ????????????????????????????.)
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
            //???????? ???? ???? ????????????????, ???? ??? ???????????????????? ???? 1 ?????? ????????. (???????????? ?? ??????????????????????,
            // ?????????????? ?????????????? ???????????????? ???????????? ?????????? ?????????? ???????????????????? ????????????.)
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

/** @param delta ???????????????? ???????????? ???? ????????.  */
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

    /** ????????????????, ?????????? ???? ???? ?????????????????????? ??????????, ????????????????, ???? ????????????????. ???????????????? <b>??????????</b> ??????????????????????
     ???? ????????????????, ???????? ???? ?????????????????? ???? ???????????????? ?????? ?????????? ?? ???? ????????????????, ?? ???? ?????????????????? ???? ?????????????? ??????????????????.
     ???????????????? <b>???? ??????????</b> ?????????????????????? ???? ???????????????? ???? ???????? ?????????????????? ??????????????. */
    private boolean canClimb (MoveDirections direction)  //TODO: ????????????????.
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
/*  ???????????? batch.begin() ??? batch.end() ?????????? ???????? ??????????????????.*/
        batch.begin();
        batch.draw (landScape, 0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //batch.draw (sky, 0,0, graphics.getWidth(), graphics.getHeight());
        batch.end();
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
        if (DEBUG) {
            hero.drawShape (shaper, onscreenTextColor);
            for (PinkBomb pb : pinkBombs)
                pb.drawShape (shaper, Color.WHITE);
        }
        Iterator<Coin60> itc = coins60.iterator();
        while (itc.hasNext()) {
            Coin60 coin = itc.next();
            if (coin.isOverlapped (hero.shape())) {
                coinSound.stop(); //< ?????????? ???????????????????? ???????? ???? ?????????? ???????????????? ????????????.
                coinSound.play();
                itc.remove();
            }
            else if (DEBUG)
                coin.drawShape (shaper, Color.WHITE);
        }
        shaper.end();
    }

    private void updateZoom (float zdelta)
    {
        ZOOM += zdelta;
        ortoCamera.zoom = ZOOM;
        ortoCamera.update();
        float viewportWidth = Gdx.graphics.getWidth();
        float viewportHeight = Gdx.graphics.getHeight();
        mapToScreenOriginOffset = new Vector2 (ortoCamera.position.x - viewportWidth / 2.0f * ZOOM,
                                               ortoCamera.position.y - viewportHeight / 2.0f * ZOOM);
        hero.setScale (1f/ZOOM, mapToScreenOriginOffset);
        for (PinkBomb pb : pinkBombs)
            pb.setScale (pinkBombScale / ZOOM, mapToScreenOriginOffset);
        for (Coin60 c : coins60)
            c.setScale (coin60Scale / ZOOM, mapToScreenOriginOffset);
    }

//-------------------------------------------------------------------------------------dispose
    @Override public void dispose () {
        music.stop();
        music.dispose();
        coinSound.stop();
        coinSound.dispose();
        hero.dispose();
        animatorCoin60.dispose();
        animatorPinkBomb_Waiting.dispose();
        animatorPinkBomb_Jumping.dispose();
        lableCrore.dispose();
        landScape.dispose();
        shaper.dispose();
        physics.dispose();
        batch.dispose();
    }
//-------------------------------------------------------------------------------------
    @Override public void show () {}
    @Override public void resize (int width, int height) {}
    @Override public void pause () {}
    @Override public void resume () {}
    @Override public void hide () {}
}
