package jugglestruggle.timechangerstruggle.mixin.client.render;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;

/**
 * Mixin moved to its own variant so other mods which focus or use night vision, can now disable
 * this individually without having to disable other important parts as of v0.0.4+26.1.
 * 
 * @author JuggleStruggle
 * @implNote Created on 08-Mar-2022, Tuesday
 */
@Mixin(GameRenderer.class)
public class GameRendererMixinNightVision
{
	@Inject(method = "getNightVisionStrength", at = @At(value = "HEAD"), cancellable = true)
	private static void dcs_nightVisionStrengthCheck(LivingEntity entity, float delta, CallbackInfoReturnable<Float> info)
	{
		if (TimeChangerStruggleClient.disableNightVisionEffect) {
			info.setReturnValue(0.0f); info.cancel();
		}
	}
}
