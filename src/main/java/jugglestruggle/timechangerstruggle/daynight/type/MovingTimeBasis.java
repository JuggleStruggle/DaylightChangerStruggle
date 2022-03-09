package jugglestruggle.timechangerstruggle.daynight.type;

import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;
import jugglestruggle.timechangerstruggle.util.EasingType;
import jugglestruggle.timechangerstruggle.util.Easings;

import net.minecraft.client.world.ClientWorld;

/**
 * A class which handles smooth transitions and includes easing for
 * the viewer's convenience :)
 * 
 * <p> Most of the fields used to exclusively be in {@link RandomizedTime}
 * but were moved to here as it was thought that {@link MovingTime} would
 * need it along as well. 
 *
 * @author JuggleStruggle
 * @implNote Created on 22-Feb-2022, Tuesday
 */
public abstract class MovingTimeBasis implements DayNightCycleBasis
{
	protected long cachedTime = 0;
	protected long previousCachedTime = 0;
	
	/**
	 * Used as a transition for the current tick.
	 */
	protected long previousInterpolatedTime = 0;
	/**
	 * Used as a transition for the next tick.
	 */
	protected long nextInterpolatedTime = 0;
	
	/**
	 * The amount of ticks it has passed, it is also a way to create an 
	 * easing delta combining this field with {@link #ticksUntilNextCall}.
	 */
	protected long ticksPassed = 1L;
	/**
	 * The amount of ticks it takes until it calls the next call. You can use
	 * an easing type to help you decide how slow/fast the transition should
	 * be.
	 * 
	 * <p> Having a tick of 0 will only call updateCall everytime and force
	 * {@link #ticksPassed} to remain in zero.
	 * 
	 * <p> A tick less than 10 will cause seizures if having to cycle 
	 * constantly through day and night, so prepare yourselves for such thing.
	 */
	protected long ticksUntilNextCall = 40L;
	
	/**
	 * See {@link #ticksUntilNextCall} for some information.
	 * <p> This field can be left null if planning not to use any.
	 */
	protected Easings easingBetweenTicks = Easings.LINEAR;
	/**
	 * Helps in deciding the easing if going in, out or both.
	 */
	protected EasingType easingType = EasingType.BETWEEN;
	
	
	
	
	
	
	
	public abstract void updateInterpolation();
	
	
	
	
	
	
	
	@Override
	public void tick()
	{
		if (this.ticksPassed >= this.ticksUntilNextCall)
		{
			this.ticksPassed = 0;
			this.updateCall();
		}
		else
		{
			if (this.easingBetweenTicks != null)
			{
				double animationDelta = (double)this.ticksPassed / (double)this.ticksUntilNextCall;
				double result = this.easingBetweenTicks.value(this.easingType, animationDelta);
				
				this.previousCachedTime = this.cachedTime;
				
				this.cachedTime = (long)((double)this.previousInterpolatedTime + 
					((double)this.nextInterpolatedTime - (double)this.previousInterpolatedTime) * result);
			}
			
			++this.ticksPassed;
		}
	}
	/**
	 * Updates the call whenever {@link #ticksPassed} supercedes or 
	 * matches {@link #ticksUntilNextCall}.
	 */
	public void updateCall()
	{
		this.previousInterpolatedTime = this.nextInterpolatedTime;
		
		this.updateInterpolation();
		
		this.previousCachedTime = this.cachedTime;
		this.cachedTime = this.previousInterpolatedTime;
	}
	
	@Override
	public long getModifiedTime(ClientWorld world, DayNightGetterType executor, boolean previous) {
		return previous ? this.previousCachedTime : this.cachedTime;
	}
	@Override
	public long getCachedTime() {
		return this.cachedTime;
	}
}
