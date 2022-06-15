package ru.gb.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public final class Constants
{
    public static final int
        wndWidth = 800,
        wndHeight = 600,
        FPS_MAX = 60,
        fps = Math.min (60, FPS_MAX)
        ;
    public static final float
        ZOOM = 1f,
        G = 9.81f,
        COIN_FPS = 10.0f,
        MARK_RADIUS = 0f,
        MARK_RADIUS_MIN = 1f
        ;
    public static final Color
        onscreenTextColor = new Color (0.7f, 1.0f, 0.9f, 1.0f),
        clearScreenColor = new Color (0.25f, 0.75f, 0.85f, 1.0f);

    public static final Vector2 heroStep = new Vector2 (4f *FPS_MAX /fps, 4f *FPS_MAX /fps); //< скорость персонажа не должна зависеть от fps.

    public static final boolean
        DEBUG = true,
        SLEEPABLE_WORLD    = true,
        WAKE        = true,
        DRAW_BODIES          = true,
        DRAW_JOINTS          = true,
        DRAW_AABBS           = true,
        DRAW_INACTIVE_BODIES = true,
        DRAW_VELOCITIES      = true,
        DRAW_CONTACTS        = true
        ;
    public static final String
        FILENAME_MAP           = "maps/map2.tmx",
        FILENAME_TEXTURE_COINS = "coins.png",
        FILENAME_TEXTURE_HERO  = "mario02.png",
        FILENAME_FONT_ONSCREEN = "Gabriola.ttf",
        LAYERNAME_COINS  = "coins",
        LAYERNAME_GROUND = "surfaces",
        POINTNAME_START_CAMERA = "CameraStart",
        PREFIX_COIN_NAME = "Coin",
        NAME_MAPOBJECT_PERSONAGE = "Hero",

        PROPNAME_GRAVITYSCALE = "gravityscale",
        PROPNAME_RESTITUTION  = "restitution",
        PROPNAME_FRICTION     = "friction",
        PROPNAME_DENSITY      = "density",
        PROPNAME_BODYTYPE     = "bodytype",

        CHAR_SET = "0123456789ЙЦУКЕНГШЩЗХЪЖДЛОРПАВЫФЯЧСМИТЬБЮЁ йцукенгшщзхъждлорпавыфячсмитьбюё.,!:;?—-+«»()/*\\"
        ;
}
