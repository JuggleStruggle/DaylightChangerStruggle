package jugglestruggle.timechangerstruggle.client.timeline;

import net.minecraft.world.attribute.EnvironmentAttributeInterpolator;

/**
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.4
 */
public interface DCSCameraProvider
{
	EnvironmentAttributeInterpolator getCameraAttributes();
	EnvironmentAttributeInterpolatorDCS getDcsAttributes();
}
