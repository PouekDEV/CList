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
    public int rgbToHex(){
        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);
        return (255 << 24) | (red << 16) | (green << 8) | blue;
    }
    public String rgbToHexNoAlpha(){
        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);
        return String.format("%02X%02X%02X", red, green, blue);
    }
    public void hexToRGB(String hex) {
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
