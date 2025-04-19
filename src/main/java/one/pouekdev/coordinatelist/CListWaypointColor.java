package one.pouekdev.coordinatelist;

public class CListWaypointColor {
    public float r;
    public float g;
    public float b;
    CListWaypointColor(float red, float green, float blue){
        r = red;
        g = green;
        b = blue;
    }
    public void set(float[] hsv) {
        float h = hsv[0];
        float s = hsv[1];
        float v = hsv[2];
        s /= 100;
        v /= 100;
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = v - c;
        float rPrime, gPrime, bPrime;
        if (h < 60) {
            rPrime = c;
            gPrime = x;
            bPrime = 0;
        } else if (h < 120) {
            rPrime = x;
            gPrime = c;
            bPrime = 0;
        } else if (h < 180) {
            rPrime = 0;
            gPrime = c;
            bPrime = x;
        } else if (h < 240) {
            rPrime = 0;
            gPrime = x;
            bPrime = c;
        } else if (h < 300) {
            rPrime = x;
            gPrime = 0;
            bPrime = c;
        } else {
            rPrime = c;
            gPrime = 0;
            bPrime = x;
        }
        this.r = (rPrime + m);
        this.g = (gPrime + m);
        this.b = (bPrime + m);
    }
    public float[] getHSV() {
        float min = Math.min(Math.min(r, g), b);
        float max = Math.max(Math.max(r, g), b);
        float delta = max - min;
        float h, s;
        float v = max * 100;
        if (max != 0)
            s = (delta / max) * 100;
        else {
            s = 0;
            h = 0;
            return new float[]{h, s, v};
        }
        if (r == max)
            h = (g - b) / delta;
        else if (g == max)
            h = 2 + (b - r) / delta;
        else
            h = 4 + (r - g) / delta;
        h *= 60;
        if (h < 0)
            h += 360;
        if (Float.isNaN(h))
            h = 0;
        if (Float.isNaN(s))
            s = 0;
        if (Float.isNaN(v))
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
        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);
        return String.format("%02X%02X%02X", red, green, blue);
    }
    public void set(String hex) {
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
            catch (NumberFormatException ignored){}
            if(red != -1 && green != -1 && blue != -1){
                this.r = red / 255.0f;
                this.g = green / 255.0f;
                this.b = blue / 255.0f;
            }
        }
    }
}
