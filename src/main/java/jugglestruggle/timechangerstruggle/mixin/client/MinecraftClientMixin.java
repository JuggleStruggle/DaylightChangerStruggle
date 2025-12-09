package jugglestruggle.timechangerstruggle.mixin.client;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.1
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
	// A semi-copy of Fabric's MinecraftClient's mixin due to that one requiring that
	// the world parameter not be null. This variant requires that to be in order to
	// save certain cycles to disk.
	@Inject(method = "setWorld", at = @At("TAIL"))
	private void daylightChangerStruggle_onAfterClientWorldChange(ClientWorld world, CallbackInfo ci) {
		TimeChangerStruggleClient.onWorldChanged((MinecraftClient)(Object)this, world);
	}
}
