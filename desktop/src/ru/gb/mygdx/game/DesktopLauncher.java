package ru.gb.mygdx.game;

import static ru.gb.mygdx.game.MyGdxGame.fps;
import static ru.gb.mygdx.game.MyGdxGame.wndHeight;
import static ru.gb.mygdx.game.MyGdxGame.wndWidth;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ru.gb.mygdx.game.MyGdxGame;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
    public static void main (String[] arg)
    {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode (wndWidth, wndHeight);
        config.setResizable (false);
        config.setForegroundFPS (fps);
        config.setTitle ("My GDX Game");
        new Lwjgl3Application (new MyGdxGame(), config);
    }
}
