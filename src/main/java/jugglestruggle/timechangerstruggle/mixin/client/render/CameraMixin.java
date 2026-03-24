package jugglestruggle.timechangerstruggle.mixin.client.render;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.client.timeline.DCSCameraProvider;
import jugglestruggle.timechangerstruggle.client.timeline.EnvironmentAttributeInterpolatorDCS;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributeInterpolator;

/**
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.4
 */
@Mixin(Camera.class)
public abstract class CameraMixin implements DCSCameraProvider
{
	@Shadow
	private World area;
	@Shadow
	private Vec3d pos;
	
	@Shadow @Final
	private EnvironmentAttributeInterpolator environmentAttributeInterpolator;
	
	@Unique
	private final EnvironmentAttributeInterpolatorDCS dcsAttributeInterpolator = 
		new EnvironmentAttributeInterpolatorDCS((Camera)(Object)this);
	
	@Override
	public EnvironmentAttributeInterpolator getCameraAttributes() {
		return this.environmentAttributeInterpolator;
	}
	
	@Override
	public EnvironmentAttributeInterpolatorDCS getDcsAttributes() {
		return this.dcsAttributeInterpolator;
	}
	
	
	@Inject(method = "updateEyeHeight()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/"
		+ "EnvironmentAttributeInterpolator;update(Lnet/minecraft/world/World;Lnet/minecraft/util/math/Vec3d;)V"))
	private void dcsOnEnvAttributeUpdate(CallbackInfo ci) {
		this.dcsAttributeInterpolator.update(this.area, this.pos);
	}

	@Inject(method = "reset()V", at = @At(value = "TAIL"))
	private void dcsOnCameraReset(CallbackInfo ci) {
		this.dcsAttributeInterpolator.clear();
	}
	

	/**
	 * Note: Originally when developing the mod port during pre-releases, it was decided not to 
	 * modify the environment attribute of the camera to maintain its integrity, including this method. 
	 * 
	 * <p> But since this method is very prevalent and is used in a lot of areas, including with 
	 * certain mods such as Iris, it was decided that the best way to go on about was to replace 
	 * this method to contain the DCS environment attribute only if not using the world time.
	 * 
	 * <p> However, the DCS environment attribute will still use the camera's environment attribute
	 * data in attribute types it does not support.
	 * 
	 * @return either the base game's attribute interpolator or DCS's variant
	 */
	@Overwrite
	public EnvironmentAttributeInterpolator getEnvironmentAttributeInterpolator() 
	{
		return TimeChangerStruggleClient.useWorldTime() ? 
			this.environmentAttributeInterpolator : this.dcsAttributeInterpolator;
	}
}
