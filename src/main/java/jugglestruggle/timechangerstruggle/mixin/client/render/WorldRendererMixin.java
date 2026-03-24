package jugglestruggle.timechangerstruggle.mixin.client.render;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.render.WorldRenderer;

/**
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.4
 * 
 * @see SkyRenderingMixin
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin
{
	@ModifyArg(
		method = "fillRenderStatesFromWorld(Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/client/render/Camera;F)V",
		at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/attribute/EnvironmentAttributeInterpolator;get"
			+ "(Lnet/minecraft/world/attribute/EnvironmentAttribute;F)Ljava/lang/Object;"), 
		require = 1, allow = 1, index = 1
	)
	private float dcs_smoothOrChopCloudColor(float delta) {
		return TimeChangerStruggleClient.smoothButterCycle ? delta : 1.0f;
	}
}
