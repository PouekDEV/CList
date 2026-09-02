package one.pouekdev.coordinatelist;

import java.awt.*;
import java.util.Random;

public class CListElementColor{
    public float r;
    public float g;
    public float b;
    private final static Random RAND = new Random();

    CListElementColor(float red, float green, float blue){
        r = red;
        g = green;
        b = blue;
    }

    CListElementColor(){
        r = RAND.nextFloat();
        g = RAND.nextFloat();
        b = RAND.nextFloat();
    }

    public float[] getHSV(){
        float[] hsv = new float[3];
        int rgb = getHex();
        Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, hsv);
        return hsv;
    }

    public int getHex(){
        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);
        return (255 << 24) | (red << 16) | (green << 8) | blue;
    }

    public void set(String hex){
        Color color = Color.decode("#"+hex);
        r = color.getRed() / 255.0f;
        g = color.getGreen() / 255.0f;
        b = color.getBlue() / 255.0f;
    }
}
