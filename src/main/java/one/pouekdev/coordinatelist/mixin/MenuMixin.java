package one.pouekdev.coordinatelist.mixin;

import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.text.Text;
import one.pouekdev.coordinatelist.CListWaypointScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class MenuMixin extends Screen {
	protected MenuMixin(Text title) {
		super(title);
	}
	@Inject(at = @At("HEAD"), method = "initWidgets")
	private void initWidgets(CallbackInfo ci) {
		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().margin(4, 4, 4, 0);
		GridWidget.Adder adder = gridWidget.createAdder(2);
		adder.add(ButtonWidget.builder(Text.literal("CList"), button -> {
			this.client.setScreen(new CListWaypointScreen(Text.literal("Waypoints")));
		}).width(204).build(),2, gridWidget.copyPositioner().marginTop(180));
		gridWidget.recalculateDimensions();// 1.19.4 gridWidget.refreshPositions();
		SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 0.25f);
		addDrawableChild(gridWidget); // 1.19.4 gridWidget.forEachChild(this::addDrawableChild);
	}
}

