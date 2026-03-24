package jugglestruggle.timechangerstruggle.mixin.client.world;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;

/**
 * Client world mixin; the main class that handles our glorious time which cannot
 * be ignored upon our hands
 *
 * @author JuggleStruggle
 * @implNote Created on 26-Jan-2022, Wednesday
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World
{
    protected ClientWorldMixin() {
		super(null, null, null, null, false, false, 0, 0);
    }
	
	@Override
	public long getTimeOfDay() 
	{
		return TimeChangerStruggleClient.useWorldTime() ? 
			super.getTimeOfDay() : TimeChangerStruggleClient.dcsClock.getTime();
	}
}
