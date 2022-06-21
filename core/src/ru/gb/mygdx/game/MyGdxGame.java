package ru.gb.mygdx.game;

import com.badlogic.gdx.Game;
import ru.gb.mygdx.game.screens.StartScreen;

public class MyGdxGame extends Game// ApplicationAdapter//
{
    @Override public void create () {    this.setScreen (new StartScreen (this));    }

    @Override public void render () {    super.render();    }

    @Override public void resize (int width, int height) {    super.resize (width, height);    }

    @Override public void dispose () {}
}
