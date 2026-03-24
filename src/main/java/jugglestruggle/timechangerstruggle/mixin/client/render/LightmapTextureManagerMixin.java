package jugglestruggle.timechangerstruggle.mixin.client.render;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.render.LightmapTextureManager;

/**
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.4
 * 
 * @see SkyRenderingMixin
 */
@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin
{
	@ModifyVariable(
		at = @At(value = "HEAD"), ordinal = 0,
		method = "prepareRender(Lnet/minecraft/client/render/state/LightmapRenderState;F)V"
	)
	private float dcs_smoothOrChopRenderState(float delta) {
		return TimeChangerStruggleClient.smoothButterCycle ? delta : 1.0f;
	}
}
