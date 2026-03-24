package jugglestruggle.timechangerstruggle.mixin.client.render;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.render.SkyRendering;

/**
 * Created in response to how 1.21.11 now smooths out the daylight cycle and was
 * made data-driven with its timeline feature. It attempts to bring back the chopped 
 * daylight cycle used in older versions (most notably in 1.21.10 and below) and 
 * because the DCS mod itself utilizes this feature.
 *
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.3+1.21.11
 */
@Mixin(SkyRendering.class)
public class SkyRenderingMixin
{
	@ModifyVariable(
		at = @At(value = "HEAD"), ordinal = 0,
		method = "updateRenderState(Lnet/minecraft/client/world/ClientWorld;F"
			+ "Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/state/SkyRenderState;)V"
	)
	private float dcs_smoothOrChopRenderState(float delta) {
		return TimeChangerStruggleClient.smoothButterCycle ? delta : 1.0f;
	}
}
