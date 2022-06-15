package ru.gb.mygdx.game.buttons;

import static ru.gb.mygdx.game.Constants.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;import com.badlogic.gdx.utils.Disposable;

public class Lable implements Disposable
{
    private final BitmapFont bitmapFont;
    private final int offsetX, offsetY;

    public Lable (int siz, int lableOffsetX, int lableOffsetY)
    {
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator (Gdx.files.internal (FILENAME_FONT_ONSCREEN));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = siz;
        fontParameter.characters = CHAR_SET;

        bitmapFont = fontGenerator.generateFont (fontParameter);
        bitmapFont.setColor (onscreenTextColor);
        offsetX = lableOffsetX;
        offsetY = lableOffsetY;
    }

    public void draw (SpriteBatch batch, String text) {
        bitmapFont.draw (batch, text, offsetX, offsetY - bitmapFont.getAscent());
    }

    public void dispose() {    bitmapFont.dispose();    }
}
