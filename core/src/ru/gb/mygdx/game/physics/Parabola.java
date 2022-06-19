package ru.gb.mygdx.game.physics;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

import static ru.gb.mygdx.game.Constants.*;

public class Parabola
{
    /* y = ax² + bx + c.
       a — задаёт форму параболы и определяется опытным путём (a < 0, чтобы ветви смотрели вниз);
       b = 0, поскольку для удобства расчётов вершина помещена на OY;
       с — высота карты;
       Т.о. персонаж будет прыгать по параболе y = ax²+c. */
    private final float a = PARABOLA_A_FACTOR, b = 0f, c;
    private final Vector2 point;


    public Parabola (TiledMap map)
    {
        point = new Vector2();
        MapProperties mprops = map.getProperties();
        c = (float)((int) mprops.get("height") * (int) mprops.get("tileheight"));
    }

/** Готовим точку this.point к выполнению прыжка — помещаем её в точку с ординатой JUMP_DISTANCE/2.
 Знак зависит от направления предстоящего прыжка.
 @param forRightJump указывает направление предстоящего прыжка. */
    public void reset (boolean forRightJump)
    {
        point.x = (forRightJump ? -JUMP_DISTANCE : JUMP_DISTANCE) / 2f;
        point.y = calcY (point.x);
    }

    public void setPoint (/*…*/) {    }

/** Перемещаем точку this.point по параболе так, чтобы её ордината получила приращение deltaX в направлении
 прыжка.<p>
    Если на пути падающего персонажа встретится блок, толщина которого окажется меньше deltaY, то персонаж
 может пролететь сквозь этот блок. На данный момент толщины в 32 точек хватает.
 @param deltaX приращение по длине.
 @return Приращение по высоте.
*/
    public float jump (float deltaX, boolean fall)
    {
        float x = point.x += deltaX;
        float y = calcY (x);
        float deltaY = y - point.y;
        point.y = y;
        return deltaY;
    }

    public float calcY (float x) {    return a * x * x + b * x + c;    }
}
