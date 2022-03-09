package jugglestruggle.timechangerstruggle.mixin.client.render;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;

import net.minecraft.client.render.GameRenderer;

/**
 * 
 *
 * @author JuggleStruggle
 * @implNote Created on 08-Mar-2022, Tuesday
 */
@Mixin(GameRenderer.class)
@Environment(EnvType.CLIENT)
public class GameRendererMixin
{
	@Inject(method = "getNightVisionStrength", at = @At(value = "HEAD"), cancellable = true)
	private static void nightVisionStrengthCheck(LivingEntity entity, float delta, CallbackInfoReturnable<Float> info)
	{
		if (TimeChangerStruggleClient.disableNightVisionEffect) {
			info.setReturnValue(0.0f); info.cancel();
		}
	}
}
