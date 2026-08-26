package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Util;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.function.Function;

public class CListRenderLayers{
    private static final RenderPipeline POSITION_TEX_COLOR_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                    .withLocation("pipeline/position_tex_color")
                    .withCull(false)
                    .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
                    .build()
    );
    public static final Function<Identifier, RenderType> POSITION_TEX_COLOR = Util.memoize(
            texture -> RenderType.create(
                    "coordinatelist:pos_text_color",
                    RenderSetup.builder(POSITION_TEX_COLOR_PIPELINE)
//                        .bufferSize(1536)
                        .setLayeringTransform(LayeringTransform.NO_LAYERING)
                        .withTexture("Sampler0", texture)
                        .createRenderSetup()
            )
    );
}
