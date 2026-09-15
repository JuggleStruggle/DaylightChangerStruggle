package jugglestruggle.timechangerstruggle.client.timeline;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;
import jugglestruggle.timechangerstruggle.mixin.client.world.ClientWorldAccessor;
import jugglestruggle.timechangerstruggle.mixin.client.world.ClientWorldMixin;

import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientClocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ClockUpdate;
import net.minecraft.world.clock.WorldClockKey;

/**
 * Used instead of the base {@link ClientClocks} in {@link ClientWorld#getClocks()} 
 * but refers to the client network handler's variant when {@link #useDcsClock} is off
 * as it is the case with {@link ClientWorld}.
 * 
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.4
 */
public class ClientClocksDCS extends ClientClocks
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
	
	/**
	 * Only used when getting the clock's entry without a world so it wouldn't have
	 * to constantly be recreated.
	 * 
	 * @implNote Introduced v0.0.5+26.3
	 */
	public ClientClocks.Entry emptyCC;
	
	public ClientClocksDCS() 
	{
		this.defaults(); this.useDcsClock = false;
		this.emptyCC = new ClientClocks.Entry();
	}
	
	@Override
	public ClientClocks.Entry get(RegistryEntry<WorldClockKey> key)
	{
		ClientWorld w = MinecraftClient.getInstance().world;
		DayNightCycleBasis cycle = TimeChangerStruggleClient.getTimeChanger();
		
		ClientClocks.Entry cc;
		
		if (w == null)
		{
			cc = super.get(key);
			
			// Return what we got if the cycle itself is empty.
			if (cycle == null)
				return cc;
		}
		else
		{
			ClientClocks.Entry nh = ((ClientWorldAccessor)w)
				.getClientNetworkHandler().getClocks().get(key);
			
			// Return the original client clock if DCS clock is not in use or the active cycle is null.
			if (!this.useDcsClock || cycle == null)
				return nh;
			
			// Pass values from the network handler's clock to our entry.
			cc = super.get(key);
			
			cc.partialTick = nh.partialTick;
			cc.rate = nh.rate;
		}
		
		cc.ticks = (w == null) ? cycle.getCachedTime() : 
			cycle.getModifiedTime(w, this.forExecutor, this.forPreviousTime);
		
		return cc;
	}
	
	@Override
	public void setTicks(long ticks) 
	{
		ClientWorld w = MinecraftClient.getInstance().world;
		
		if (w != null)
			((ClientWorldAccessor)w).getClientNetworkHandler().getClocks().setTicks(ticks);
	}
	
	@Override
	public void update(long ticks, Map<RegistryEntry<WorldClockKey>, ClockUpdate> clockDataByKey) 
	{
		ClientWorld w = MinecraftClient.getInstance().world;
		
		if (w != null)
			((ClientWorldAccessor)w).getClientNetworkHandler().getClocks().update(ticks, clockDataByKey);
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
