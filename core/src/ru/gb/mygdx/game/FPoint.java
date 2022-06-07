package ru.gb.mygdx.game;

public class FPoint {
    public float x;
    public float y;

    public FPoint (){}
    public FPoint (float fx, float fy) {  x = fx;  y = fy;  }

    public FPoint scale (float factor) {
        x *= factor;
        y *= factor;
        return this;
    }
}
