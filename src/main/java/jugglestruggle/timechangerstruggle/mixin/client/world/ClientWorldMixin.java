package jugglestruggle.timechangerstruggle.mixin.client.world;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Client world mixin; the main class that handles our glorious time which cannot
 * be ignored upon our hands
 *
 * @author JuggleStruggle
 * @implNote Created on 26-Jan-2022, Wednesday
 */
@Mixin(ClientWorld.class) @Environment(EnvType.CLIENT)
public abstract class ClientWorldMixin extends World
{
    protected ClientWorldMixin() {
		super(null, null, null, null, false, false, 0, 0);
    }
	
	@Override
	public long getTimeOfDay() 
	{
		return TimeChangerStruggleClient.useWorldTime() ? 
			super.getTimeOfDay() : this.tcs_getModifiedTime(DayNightGetterType.DEFAULT, false);
	}
	@Unique
	public long getPreviousTimeOfDay() 
	{
		return TimeChangerStruggleClient.useWorldTime() ? 
			super.getTimeOfDay() : this.tcs_getModifiedTime(DayNightGetterType.DEFAULT, true);
	}
	
	@Unique
	public long tcs_getModifiedTime(DayNightGetterType executor, boolean previous) {
		return TimeChangerStruggleClient.getTimeChanger().getModifiedTime((ClientWorld)(Object)this, executor, previous);
	}
}
