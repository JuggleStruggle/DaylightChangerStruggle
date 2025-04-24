package jugglestruggle.timechangerstruggle.mixin.client.render.gl;

import jugglestruggle.timechangerstruggle.client.util.render.RainbowShader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gl.GlBackend;

/**
 * Done in order to reset the shader compilation state when reloading
 * resources as they tend to get cleared by the {@link GlBackend}.
 *
 * @author JuggleStruggle
 * @implNote Exclusive to the 1.21.5 port
 */
@Mixin(GlBackend.class)
public class GlBackendMixin
{
	@Inject(method = "clearPipelineCache", at = @At("TAIL"))
	private void clearPipelineCacheTail(CallbackInfo ci) {
		RainbowShader.RAINBOW_RL.resetCachedState();
	}
}
