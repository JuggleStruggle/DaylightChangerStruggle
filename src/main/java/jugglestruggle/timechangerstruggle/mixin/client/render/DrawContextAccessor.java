package jugglestruggle.timechangerstruggle.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;

/**
 *
 * @author JuggleStruggle
 * @implNote Exclusive to the 1.21.5 port due to non-access with certain fields
 */
@Mixin(DrawContext.class)
public interface DrawContextAccessor
{
	@Accessor("vertexConsumers")
	VertexConsumerProvider.Immediate getVertexConsumerImmediate();
}
