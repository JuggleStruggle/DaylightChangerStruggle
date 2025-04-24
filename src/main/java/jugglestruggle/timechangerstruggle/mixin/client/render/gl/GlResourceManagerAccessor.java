package jugglestruggle.timechangerstruggle.mixin.client.render.gl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.gl.GlResourceManager;

/**
 * Done to get access to the pipeline compile cache in order to create
 * the Rainbow shader which is even more convoluted than both the 
 * 1.18/1.19 port.
 *
 * @author JuggleStruggle
 * @implNote Exclusive to the 1.21.5 port
 */
@Mixin(GlResourceManager.class)
public interface GlResourceManagerAccessor
{
	@Accessor
	GlBackend getBackend();
}
