package jugglestruggle.timechangerstruggle.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;

/**
 * @author JuggleStruggle
 * @implNote Modified for the 1.21.6 port due to non-access with certain fields
 */
@Mixin(DrawContext.class)
public interface DrawContextAccessor
{
	@Accessor("state")
	GuiRenderState getRenderState();

	@Accessor("scissorStack")
	DrawContext.ScissorStack getScissorStack();
}
