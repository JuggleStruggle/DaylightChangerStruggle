package jugglestruggle.timechangerstruggle.client.timeline;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;
import jugglestruggle.timechangerstruggle.mixin.client.world.ClientWorldMixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

/**
 * Backport version for 1.21.11 from 26.1.
 * 
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.4
 */
public class ClientClocksDCS
{
	public DayNightGetterType forExecutor;
	public boolean forPreviousTime;

	/**
	 * Determine if DCS's World Clock is used for the client world.
	 * 
	 * <p> Mostly used as a temporary method when performing an operation (most notably
	 * in {@link EnvironmentAttributeInterpolatorDCS}) to retrieve a value depending on
	 * the environment and needs the cycle to determine what shall be done.
	 * 
	 * @see ClientWorldMixin#getClocks()
	 */
	public boolean useDcsClock;
	
	public ClientClocksDCS() {
		this.defaults(); this.useDcsClock = false;
	}
	
	public long getTime()
	{
		ClientWorld w = MinecraftClient.getInstance().world;
		DayNightCycleBasis cycle = TimeChangerStruggleClient.getTimeChanger();
		
		return (w == null) ? cycle.getCachedTime() : 
			cycle.getModifiedTime(w, this.forExecutor, this.forPreviousTime);
	}

	/**
	 * Resets back two of the most used variables to their defaults.
	 * 
	 * This sets:
	 * <ul> 
	 * <li> {@link #forExecutor} to {@link DayNightGetterType#DEFAULT} </li>
	 * <li> {@link #forPreviousTime} to {@code false} </li>
	 * </ul>
	 * 
	 * @see #forExecutor
	 * @see #forPreviousTime
	 */
	public void defaults()
	{
		this.forExecutor = DayNightGetterType.DEFAULT;
		this.forPreviousTime = false;
	}
}
