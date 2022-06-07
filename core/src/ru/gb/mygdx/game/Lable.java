package ru.gb.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Lable
{
    private final BitmapFont bitmapFont;
    private final int offsetX, offsetY;

    public Lable (int siz, int lableOffsetX, int lableOffsetY) {
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("Gabriola.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = siz;
        fontParameter.characters = "0123456789ЙЦУКЕНГШЩЗХЪЖДЛОРПАВЫФЯЧСМИТЬБЮЁ йцукенгшщзхъждлорпавыфячсмитьбюё.,!:;?—-+«»()/*\\";
        bitmapFont = fontGenerator.generateFont (fontParameter);
        bitmapFont.setColor (0.7f, 1.0f, 0.9f, 1.0f);
        offsetX = lableOffsetX;
        offsetY = lableOffsetY;
    }

    public void draw (SpriteBatch batch, String text) {
        bitmapFont.draw (batch, text, offsetX, offsetY - bitmapFont.getAscent());
    }

    public void dispose() {    bitmapFont.dispose();    }
}
