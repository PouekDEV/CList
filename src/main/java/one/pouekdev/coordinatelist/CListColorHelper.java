package one.pouekdev.coordinatelist;

public class CListColorHelper{
    public static float[] HSVtoRGB(float[] hsv){
        float h = hsv[0];
        float s = hsv[1];
        float v = hsv[2];
        s /= 100;
        v /= 100;
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = v - c;
        float rPrime, gPrime, bPrime;
        if(h < 60){
            rPrime = c;
            gPrime = x;
            bPrime = 0;
        }
        else if(h < 120){
            rPrime = x;
            gPrime = c;
            bPrime = 0;
        }
        else if(h < 180){
            rPrime = 0;
            gPrime = c;
            bPrime = x;
        }
        else if(h < 240){
            rPrime = 0;
            gPrime = x;
            bPrime = c;
        }
        else if(h < 300){
            rPrime = x;
            gPrime = 0;
            bPrime = c;
        }
        else{
            rPrime = c;
            gPrime = 0;
            bPrime = x;
        }
        return new float[]{rPrime + m, gPrime + m, bPrime + m};
    }

    public static int HSVtoRGB(float h, float s, float v){
        float[] rgb = HSVtoRGB(new float[]{h, s, v});
        return (255 << 24) | ((int) (rgb[0] * 255) << 16) | ((int) (rgb[1] * 255) << 8) | (int) (rgb[2] * 255);
    }

    public static String HexNoAlpha(float rgb[]){
        int red = (int) (rgb[0] * 255);
        int green = (int) (rgb[1] * 255);
        int blue = (int) (rgb[2] * 255);
        return String.format("%02X%02X%02X", red, green, blue);
    }
}
