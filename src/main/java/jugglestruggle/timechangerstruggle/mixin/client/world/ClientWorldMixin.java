package jugglestruggle.timechangerstruggle.mixin.client.world;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

/**
 * Client world mixin; the main class that handles our glorious time which cannot
 * be ignored upon our hands
 *
 * @author JuggleStruggle
 * @implNote Created on 26-Jan-2022, Wednesday
 */
@Mixin(ClientWorld.class)
@Environment(EnvType.CLIENT)
public abstract class ClientWorldMixin extends World
{
	protected ClientWorldMixin() {
		super(null, null, null, null, null, true, false, 0, 0);
	}
	
	@Override
	public long getTimeOfDay() 
	{
		return TimeChangerStruggleClient.useWorldTime() ? 
			super.getTimeOfDay() : this.tcs_getModifiedTime(DayNightGetterType.DEFAULT, false);
	}
	@Override
	public long getLunarTime() 
	{
		return TimeChangerStruggleClient.useWorldTime() ? 
			super.getLunarTime() : this.tcs_getModifiedTime(DayNightGetterType.LUNAR, false);
	}
	@Unique
	public long getPreviousTimeOfDay() 
	{
		return TimeChangerStruggleClient.useWorldTime() ? 
			super.getTimeOfDay() : this.tcs_getModifiedTime(DayNightGetterType.DEFAULT, true);
	}
	@Unique
	public long getPreviousLunarTime() 
	{
		return TimeChangerStruggleClient.useWorldTime() ? 
			super.getLunarTime() : this.tcs_getModifiedTime(DayNightGetterType.LUNAR, true);
	}
	
	@Unique
	public long tcs_getModifiedTime(DayNightGetterType executor, boolean previous) {
		return TimeChangerStruggleClient.getTimeChanger().getModifiedTime((ClientWorld)(Object)this, executor, previous);
	}
	
	@Override
	public float getSkyAngle(float tickDelta)
	{
		final DimensionType dt = this.getDimension();
		
		final long lunarTime  = this.getLunarTime();
		float lunarAngle = dt.getSkyAngle(lunarTime);
		
		if (TimeChangerStruggleClient.smoothButterCycle)
		{
			final long lunarTimePrev = this.getPreviousLunarTime();
			float lunarAnglePrev = dt.getSkyAngle(lunarTimePrev);
			
			// This is important; not having it means that once either previous or current reaches higher 
			// than 1.0 in its sky angle it will be reset immediately back to 0 point whatever as a result 
			// of MathHelper.fractionalPart in DimensionType.getSkyAngle removing whole numbers and causing
			// it to transition backwards; so in the meantime this is the best that I could come up.
			if ((lunarAnglePrev > lunarAngle) && (lunarTime > lunarTimePrev)) {
				lunarAngle += 1f;
			} else if ((lunarAngle > lunarAnglePrev) && (lunarTimePrev > lunarTime)) {
				lunarAnglePrev += 1f;
			}
			
			return lunarAnglePrev + (lunarAngle - lunarAnglePrev) * tickDelta;
		}
		else
		{
			return lunarAngle;
		}
	}
}
