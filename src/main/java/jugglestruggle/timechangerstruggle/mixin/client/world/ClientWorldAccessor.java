package jugglestruggle.timechangerstruggle.mixin.client.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;

/**
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.4+26.1
 */
@Mixin(ClientWorld.class)
public interface ClientWorldAccessor
{
	@Accessor("networkHandler")
	ClientPlayNetworkHandler getClientNetworkHandler();
}
