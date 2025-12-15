package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

import java.util.function.Function;

public class CListRenderLayers{
    private static final RenderPipeline POSITION_TEX_COLOR_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                    .withLocation("pipeline/position_tex_color")
                    .withCull(false)
                    .withoutBlend()
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(true)
                    .build()
    );
    public static final Function<ResourceLocation, RenderType> POSITION_TEX_COLOR = Util.memoize(
            texture -> RenderType.create(
                    "pos_tex_color",
                    1536,
                    false,
                    true,
                    POSITION_TEX_COLOR_PIPELINE,
                    RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(texture, false)).createCompositeState(false)
            )
    );
}
