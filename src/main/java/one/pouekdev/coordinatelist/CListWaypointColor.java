package one.pouekdev.coordinatelist;

import java.util.Random;

public class CListWaypointColor{
    public float r;
    public float g;
    public float b;
    private final static Random rand = new Random();

    CListWaypointColor(float red, float green, float blue){
        r = red;
        g = green;
        b = blue;
    }

    CListWaypointColor(){
        r = rand.nextFloat();
        g = rand.nextFloat();
        b = rand.nextFloat();
    }

    public float[] getHSV(){
        float min = Math.min(Math.min(r, g), b);
        float max = Math.max(Math.max(r, g), b);
        float delta = max - min;
        float h, s;
        float v = max * 100;
        if(max != 0)
            s = (delta / max) * 100;
        else{
            s = 0;
            h = 0;
            return new float[]{h, s, v};
        }
        if(r == max)
            h = (g - b) / delta;
        else if(g == max)
            h = 2 + (b - r) / delta;
        else
            h = 4 + (r - g) / delta;
        h *= 60;
        if(h < 0)
            h += 360;
        if(Float.isNaN(h))
            h = 0;
        if(Float.isNaN(s))
            s = 0;
        if(Float.isNaN(v))
            v = 0;
        h = Float.parseFloat(String.format("%.1f", h).replace(",", "."));
        s = Float.parseFloat(String.format("%.1f", s).replace(",", "."));
        v = Float.parseFloat(String.format("%.1f", v).replace(",", "."));
        return new float[]{h, s, v};
    }

    public int getHex(){
        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);
        return (255 << 24) | (red << 16) | (green << 8) | blue;
    }

    public String getHexNoAlpha(){
        return CListColorHelper.HexNoAlpha(new float[]{r, g, b});
    }

    public void set(String hex){
        if(hex.length() == 6 && hex.matches("[a-zA-Z0-9]+")){
            hex = hex.replace("#", "");
            String redHex = hex.substring(0, 2);
            String greenHex = hex.substring(2, 4);
            String blueHex = hex.substring(4, 6);
            int red = -1;
            int green = -1;
            int blue = -1;
            try{
                red = Integer.parseInt(redHex, 16);
                green = Integer.parseInt(greenHex, 16);
                blue = Integer.parseInt(blueHex, 16);
            }
            catch(NumberFormatException ignored){}
            if(red != -1 && green != -1 && blue != -1){
                this.r = red / 255.0f;
                this.g = green / 255.0f;
                this.b = blue / 255.0f;
            }
        }
    }
}
