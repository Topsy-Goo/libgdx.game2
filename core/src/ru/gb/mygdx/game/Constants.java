package ru.gb.mygdx.game;

import com.badlogic.gdx.graphics.Color;import com.badlogic.gdx.math.Vector2;public final class Constants {

    public static final int wndWidth = 800, wndHeight = 600, FPS_MAX = 60, fps = Math.min (30, FPS_MAX);
    public static final float   G = 9.8f, COIN_FPS = 10.0f;
    public static final Color onscreenTextColor = new Color (0.7f, 1.0f, 0.9f, 1.0f);
    public static final boolean DEBUG = true;
    public static final Vector2 heroStep = new Vector2 (4f *FPS_MAX /fps, 4f *FPS_MAX /fps); //< скорость персонажа не должна зависеть от fps.
    public static final boolean
            DO_SLEEP = true,
            WAKE = true,
            MARK_BODIES = true,
            DRAW_BODIES          = true,
            DRAW_JOINTS          = true,
            DRAW_AABBS           = true,
            DRAW_INACTIVE_BODIES = true,
            DRAW_VELOCITIES      = true,
            DRAW_CONTACTS        = true;
    public static final String
        FILENAME_MAP = "maps/map2.tmx",
        FILENAME_TEXTURE_COINS = "coins.png",
        FILENAME_TEXTURE_HERO = "mario02.png",
        FILENAME_FONT = "Gabriola.ttf",
        LAYERNAME_COINS = "Монетки",
        LAYERNAME_GROUND = "поверхности",
        POINTNAME_TEST = "Точка0",
        POINTNAME_START_CAMERA = "Старт камеры",
        PREFIX_COIN_NAME = "Монетка",
        CHAR_SET = "0123456789ЙЦУКЕНГШЩЗХЪЖДЛОРПАВЫФЯЧСМИТЬБЮЁ йцукенгшщзхъждлорпавыфячсмитьбюё.,!:;?—-+«»()/*\\";

}
