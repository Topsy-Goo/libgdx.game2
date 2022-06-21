package ru.gb.mygdx.game.screens;

import static ru.gb.mygdx.game.Constants.*;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StartScreen implements Screen
{
    private final Texture texture, textureGame;
    private final Game game;
    private final Music music;
    private final SpriteBatch batch;


    public StartScreen (Game g) {
        game = g;
        batch = new SpriteBatch();
        texture = new Texture (FILENAME_STARTSCREEN_TEXTURE);
        textureGame = new Texture (FAILENAME_IMAGE_GAME);
        music = Gdx.audio.newMusic (Gdx.files.internal (FILENAME_MUSIC));
        music.setLooping (false);
        music.setVolume (0.15f);
        music.play();
    }

//delta приходит из Game.render().
    @Override public void render (float delta) {
        batch.begin();
        batch.draw (texture, 0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw (textureGame, 0,0, 780, 364);
        batch.end();

        if (Gdx.input.isKeyPressed (Input.Keys.ANY_KEY) || Gdx.input.isTouched()) {
            dispose();
            game.setScreen (new GameScreen (game));
        }
    }

    @Override public void resize (int width, int height) {}
    @Override public void pause ()  {}
    @Override public void resume () {}
    @Override public void hide ()   {}
    @Override public void show ()   {}

    @Override public void dispose () {
        music.stop();
        music.dispose();
        textureGame.dispose();
        texture.dispose();
        batch.dispose();
    }
}
