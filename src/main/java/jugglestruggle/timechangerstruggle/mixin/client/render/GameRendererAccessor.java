package jugglestruggle.timechangerstruggle.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;

/**
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.4
 */
@Mixin(GameRenderer.class)
public interface GameRendererAccessor
{
	@Accessor("lightmapTextureManager")
	LightmapTextureManager getLightmapTextureManager();
}
