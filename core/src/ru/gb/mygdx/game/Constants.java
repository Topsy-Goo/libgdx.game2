package ru.gb.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public final class Constants
{
    public static final int
         wndWidth = 1500
        ,wndHeight = 900
        ,FPS_MAX = 60
        ,fps = Math.min (60, FPS_MAX)
        ,HERO_W = 64    //< размер одной плитки
        ,HERO_H = 98    //< размер одной плитки
        ,HERO_COLS = 4
        ,HERO_ROWS = 2
        ,TEX_OFFS_X = 137
        ,TEX_OFFS_Y = 0
        ,COIN_W = 62    //< размер одной плитки
        ,COIN_H = 60    //< размер одной плитки
        ,PINKB_W = 132  //< размер одной плитки
        ,PINKB_H = 199  //< размер одной плитки
        ;
    public static final float
         STEP = 4f
        ,ZOOM = 0.6f//1//2.8f//1.8f//0.2f//
        ,G = 9.81f
        ,COIN_FPS = 10.0f
        ,PINKBOMB_FPS = 10.0f
        ,MARK_RADIUS = 5f//0f//
        ,MARK_RADIUS_MIN = 2f

        ,HERO_FOOTSENSOR_HH = 2f /* * STEP*/
        ,HERO_FOOTSENSOR_HW = 20f

        ,JUMP_DISTANCE = 100f   //< Длина прыжка на ровной поверхности (на параболе будет выбрана точка, плечо которой рано половине длины прыжка; из этой точки персонаж начинает прыжок или падение).
        ,STEP_TO_JUMP_FACTOR = 0.5f //< Во столько раз шаг прыжка длиннее шага ходьбы. Влияет только на скорость прыжка.
        ,PARABOLA_A_FACTOR = -0.04f//-0.03444f  //< Экспериментально установленное значение для крутизны параболы: y = ax²+c (с == высота карты).
        ,coin60Scale = 0.5f
        ,pinkBombScale = 0.5f;
        ;

    public static final Color
        onscreenTextColor = new Color (0.7f, 1.0f, 0.9f, 1.0f),
        clearScreenColor = new Color (0.25f, 0.75f, 0.85f, 1.0f);

    public static final Vector2 heroStep = new Vector2 (STEP *FPS_MAX /fps, STEP *FPS_MAX /fps); //< скорость персонажа не должна зависеть от fps.
    //public static final float FALLING_STEP_MAX = heroStep.y;

    public static final boolean
         DEBUG = true
        ,SLEEPABLE_WORLD    = !true
        ,WAKE        = true
        ,DRAW_BODIES          = true
        ,DRAW_JOINTS          = true
        ,DRAW_AABBS           = true
        ,DRAW_INACTIVE_BODIES = true
        ,DRAW_VELOCITIES      = true
        ,DRAW_CONTACTS        = true
        ;
    public static final String
         FILENAME_MAP           = "maps/map2.tmx"
        ,FILENAME_TEXTURE_COINS = "coins.png"
        ,FILENAME_TEXTURE_PINKBOMB = "pinkbomb.png"
        ,FILENAME_TEXTURE_HERO  = "mario02.png"
        ,FILENAME_FONT_ONSCREEN = "Gabriola.ttf"
        ,LAYERNAME_COINS  = "coins"
        ,LAYERNAME_GROUND = "surfaces"
        ,LAYERNAME_TRAPS = "ловушки"
        ,POINTNAME_START_CAMERA = "CameraStart"
        ,PREFIX_COIN_NAME = "Coin"
        ,PREFIX_PINKBOMB_NAME = "PinkBomb"
        ,NAME_MAPOBJECT_PERSONAGE = "Hero"

        ,PROPNAME_AWAKE         = "awake"
        //,PROPNAME_SHAPESENSOR   = "shapesensor"
        ,PROPNAME_TRIGGEROBJECT = "triggerobject"
        ,PROPNAME_GRAVITYSCALE  = "gravityscale"
        ,PROPNAME_RESTITUTION   = "restitution"
        ,PROPNAME_FRICTION      = "friction"
        ,PROPNAME_DENSITY       = "density"
        ,PROPNAME_BODYTYPE      = "bodytype"
        ,PROPNAME_FOOTSENSOR    = "footsensor"

        //,SENSORNAME_HERO_FOOTSENSOR = "herofootsensor"

        ,CHAR_SET = "0123456789ЙЦУКЕНГШЩЗХЪЖДЛОРПАВЫФЯЧСМИТЬБЮЁ йцукенгшщзхъждлорпавыфячсмитьбюё.,!:;?—-+«»()/*\\"
        ;
}
