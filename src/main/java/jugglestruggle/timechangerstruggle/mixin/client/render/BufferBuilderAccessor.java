package jugglestruggle.timechangerstruggle.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormatElement;

/**
 * Expands the original {@link BufferBuilder} by accessing
 * private fields and methods that aren't publicly accessible
 * primarily due to {@link VertexConsumer} not having putFloat,
 * which is required in order to upload a shader's data 
 * alongside with it, specifically the Rainbow Shader.
 *
 * @author JuggleStruggle
 * @implNote Exclusive for both 1.21.1 and the initial 1.21.5 port
 */
@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor
{
	@Invoker("beginElement")
	long getBeginElement(VertexFormatElement element);
}