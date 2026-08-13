package one.pouekdev.coordinatelist;

import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

public abstract class CListElement{
    public String name;
    public String dimension;
    public CListElementColor color;
    public transient CListFolder parent;
    public boolean render;

    public void toggleVisibility(){
        this.render = !this.render;
        CListVariables.savedSinceLastUpdate = false;
    }

    public Component getDimensionText(){
        return Component.literal(this.getDimensionString());
    }

    public String getDimensionString(){
        String s = this.dimension;
        s = s.replace("minecraft:", "");
        s = s.replace("_", " ");
        s = s.replace(":", " ");
        s = StringUtils.capitalize(s);
        return s;
    }
}
