package one.pouekdev.coordinatelist;

public class CListDelayedEvent {
    float ticks;
    Runnable function;
    public CListDelayedEvent(float seconds, Runnable function){
        this.ticks = seconds*20;
        this.function = function;
    }
    public boolean update(){
        this.ticks -= 1;
        if(ticks<=0){
            this.function.run();
            return true;
        }
        return false;
    }
}
