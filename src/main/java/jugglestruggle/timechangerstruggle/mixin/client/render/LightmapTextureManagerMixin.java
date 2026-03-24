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
	@ModifyVariable(method = "update(F)V", at = @At(value = "HEAD"), ordinal = 0)
	private float dcs_smoothOrChopRenderState(float delta) {
		return TimeChangerStruggleClient.smoothButterCycle ? delta : 1.0f;
	}
}
