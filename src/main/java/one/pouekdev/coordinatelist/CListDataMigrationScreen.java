package one.pouekdev.coordinatelist;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.NonNull;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

public class CListDataMigrationScreen extends Screen{
    private enum Status{
        NONE, OK, DUPLICATE, ERROR
    }

    private Button acknowledgeButton;
    private Button copyButton;
    private boolean acknowledged = false;
    private Status status = Status.NONE;
    private List<String> messages = Lists.newArrayList();
    private final List<String> saves = Lists.newArrayList();
    private final List<String> CListSaves = Lists.newArrayList();

    public CListDataMigrationScreen(Component title){
        super(title);
        for(LevelStorageSource.LevelDirectory level : CListVariables.minecraftClient.getLevelSource().findLevelCandidates().levels()){
            saves.add(level.directoryName());
        }
        ServerList serverList = new ServerList(CListVariables.minecraftClient);
        serverList.load();
        for(int i = 0; i < serverList.size(); i++){
            saves.add(serverList.get(i).ip);
        }
        for(String save : CListData.getSavedData()){
            CListSaves.add(save.replace("clist_", "").replace(".json", ""));
        }
    }

    @Override
    protected void init(){
        CListDropdown from = new CListDropdown(this.width / 2 - 75 - 100, this.height / 2 - 20, 160, this.height / 3, 20, Component.literal(!CListSaves.isEmpty() ? CListSaves.getFirst() : "---"), CListSaves, null, false);
        CListDropdown to = new CListDropdown(this.width / 2 - 75 + 100, this.height / 2 - 20, 160, this.height / 3, 20, Component.literal(!saves.isEmpty() ? saves.getFirst() : "---"), saves, null, false);
        copyButton = Button.builder(Component.translatable("data.migration.copy"), _ -> {
            status = Status.NONE;
            if(!from.getMessage().getString().equals(to.getMessage().getString())){
                try{
                    CListElementsContainer f = CListData.loadListFromFile("clist_" + from.getMessage().getString() + ".json");
                    CListElementsContainer t = CListData.loadListFromFile("clist_" + to.getMessage().getString().replace(":", "P") + ".json");
                    if(t == null){
                        t = new CListElementsContainer();
                    }
                    t.folders.addAll(f.folders);
                    t.waypoints.addAll(f.waypoints);
                    CListData.saveListToFile("clist_" + to.getMessage().getString().replace(":", "P") + ".json", t);
                    status = Status.OK;
                }
                catch(Exception e){
                    status = Status.ERROR;
                }
            }
            else{
                status = Status.DUPLICATE;
            }
        }).bounds(this.width / 2 - 155, this.height - 30, 150, 20).build();
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), _ -> {
            CListVariables.minecraftClient.setScreen(MidnightConfig.getScreen(null, CList.MOD_ID));
        }).bounds(this.width / 2 + 5, this.height - 30, 150, 20).build());
        if(!acknowledged){
            acknowledgeButton = addRenderableWidget(Button.builder(Component.translatable("data.migration.acknowledge"), _ -> {
                acknowledged = true;
                removeWidget(acknowledgeButton);
                addRenderableWidget(copyButton);
                addRenderableWidget(from);
                addRenderableWidget(to);
            }).bounds(this.width / 2 - 155, this.height - 30, 150, 20).build());
            String source = Component.translatable("data.migration.message").getString();
            // This should allow us to have a perfectly working version if Chinese or Japanese comes around
            BreakIterator boundary = BreakIterator.getWordInstance(Locale.ROOT);
            boundary.setText(source);
            List<String> messages = Lists.newArrayList();
            String lastMessage = "";
            float maxWidth = this.width / 1.5f;
            int currentWidth = 0;
            int start = boundary.first();
            for(int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()){
                String text = source.substring(start, end);
                int width = CListVariables.minecraftClient.font.width(text);
                if(currentWidth + width >= maxWidth){
                    messages.add(lastMessage);
                    lastMessage = text;
                    currentWidth = width;
                }
                else{
                    currentWidth += width;
                    lastMessage = lastMessage.concat(text);
                }
            }
            messages.add(lastMessage);
            this.messages = messages;
        }
        else{
            addRenderableWidget(from);
            addRenderableWidget(to);
            addRenderableWidget(copyButton);
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if(!acknowledged){
            graphics.centeredText(CListVariables.minecraftClient.font, Component.translatable("data.migration.warning"), this.width / 2, this.height / 2 - 60, 0xFFFF0000);
            for(int i = 0; i < messages.size(); i++){
                graphics.centeredText(CListVariables.minecraftClient.font, messages.get(i), this.width / 2, this.height / 2 - 40 + (10 * i), 0xFFFFFFFF);
            }
        }
        else{
            graphics.centeredText(CListVariables.minecraftClient.font, Component.translatable("data.migration.copy.from"), this.width / 2 - 100, this.height / 2 - 30, 0xFFFFFFFF);
            graphics.centeredText(CListVariables.minecraftClient.font, Component.translatable("data.migration.copy.to"), this.width / 2 + 100, this.height / 2 - 30, 0xFFFFFFFF);
            int color = 0xFF00FF00;
            Component message = null;
            switch(status){
                case OK -> message = Component.translatable("data.migration.success");
                case DUPLICATE -> {
                    message = Component.translatable("data.migration.duplicate");
                    color = 0xFFFFBF00;
                }
                case ERROR -> {
                    message = Component.translatable("data.migration.error");
                    color = 0xFF0000FF;
                }
            }
            if(status != Status.NONE){
                graphics.centeredText(CListVariables.minecraftClient.font, message, this.width / 2, this.height / 2 + 30, color);
            }
        }
    }
}
