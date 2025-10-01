package jugglestruggle.timechangerstruggle.daynight.type;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;
import jugglestruggle.timechangerstruggle.config.property.EnumValue;
import jugglestruggle.timechangerstruggle.config.property.LongValue;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;
import jugglestruggle.timechangerstruggle.util.EasingType;
import jugglestruggle.timechangerstruggle.util.Easings;

import java.util.Set;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;

import com.google.common.collect.ImmutableSet;

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
	protected final static String PROPERTIES_BASIS_KEY = "jugglestruggle.tcs.dnt.movingtimebasis.properties.";
	
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
	
	// From here on, all of the methods laid down were introduced in v0.0.1.
	
	protected Set<BaseProperty<?, ?>> createSnapshotProperties(boolean withEasings)
	{
		ImmutableSet.Builder<BaseProperty<?, ?>> props = 
			ImmutableSet.builderWithExpectedSize(withEasings ? 12 : 10);
		
		props.add(this.getMovingTimeBasisCategory());
		props.add(this.getCachedTimeProp());
		props.add(this.getPrevCachedTimeProp());
		props.add(this.getPrevInterpolatedTimeProp());
		props.add(this.getNextInterpolatedTimeProp());
		props.add(this.getTicksPassedProp());
		props.add(this.getTicksUntilNextCallProp());
		
		if (withEasings)
		{
			props.add(this.getEasingBetweenTicksProp());
			props.add(this.getEasingTypeBetweenTicksProp());
		}
		
		return props.build();
	}
	
	public FancySectionProperty getMovingTimeBasisCategory() {
		return new DefaultFilteredPropsSection("cached", Text.translatable(PROPERTIES_BASIS_KEY+"basis"));
	}
	protected LongValue getCachedTimeProp() {
		return new LongValue("cachedTime", this.cachedTime, null, null);
	}
	protected LongValue getPrevCachedTimeProp() {
		return new LongValue("previousCachedTime", this.previousCachedTime, null, null);
	}
	protected LongValue getPrevInterpolatedTimeProp() {
		return new LongValue("prevInterpolatedTime", this.previousInterpolatedTime, null, null);
	}
	protected LongValue getNextInterpolatedTimeProp() {
		return new LongValue("nextInterpolatedTime", this.nextInterpolatedTime, null, null);
	}
	protected LongValue getTicksPassedProp() {
		return new LongValue("ticksPassed", this.ticksPassed, 0L, Long.MAX_VALUE);
	}
	protected LongValue getTicksUntilNextCallProp() {
		return new LongValue("ticksUntilNextUpdate", this.ticksUntilNextCall, 0L, Long.MAX_VALUE);
	}
	protected EnumValue<Easings> getEasingBetweenTicksProp() 
	{
		return new EnumValue<>("easingBetweenTicks", this.easingBetweenTicks, 
			Easings.LINEAR, Easings.values()).setVTT(easing -> easing.getFormattedText());
	}
	protected EnumValue<EasingType> getEasingTypeBetweenTicksProp() 
	{
		return new EnumValue<>("easingTypeBetweenTicks", this.easingType, 
			EasingType.BETWEEN, EasingType.values()).setVTT(easing -> easing.getFormattedText());
	}

	public void writePropertyValueToCycleForBasis(BaseProperty<?, ?> property, PropertyWriterSource writer)
	{
		final String belongingKey = property.property();
		
		if (property instanceof LongValue prop)
		{
	        long v = prop.get();

	        switch (belongingKey)
	        {
	        	case "cachedTime" ->
	        	{
	        		if (writer == PropertyWriterSource.USER)
	        		{
	        			this.previousInterpolatedTime = this.nextInterpolatedTime =
	        			this.previousCachedTime = this.cachedTime = v;
	        		}
	        		else {
	        			this.cachedTime = v;
	        		}
	        	}
	        	case "previousCachedTime" -> {
	        		if (writer == PropertyWriterSource.FROM_JSON)
	        			this.previousCachedTime = v;
	        	}
	        	case "prevInterpolatedTime" -> {
	        		if (writer == PropertyWriterSource.FROM_JSON)
	        			this.previousInterpolatedTime = v;
	        	}
	        	case "nextInterpolatedTime" -> {
	        		if (writer == PropertyWriterSource.FROM_JSON)
	        			this.nextInterpolatedTime = v;
	        	}
	        	case "ticksPassed" -> 
	        		this.ticksPassed = v;
	        	case "ticksUntilNextUpdate" -> 
	        		this.ticksUntilNextCall = v;
	        }
		}
		else if (property instanceof EnumValue<?> prop)
		{
			if (prop.getDefaultValue() instanceof Easings)
			{
				if (belongingKey == "easingBetweenTicks")
					this.easingBetweenTicks = (Easings)prop.get(); 
			}
			else if (prop.getDefaultValue() instanceof EasingType)
			{
				if (belongingKey == "easingTypeBetweenTicks")
					this.easingType = (EasingType)prop.get(); 
			}
		}
	}
	
	
	
	protected class DefaultFilteredPropsSection extends FancySectionProperty
	{
		public DefaultFilteredPropsSection(String property, Text value) {
			super(property, value);
		}
		
		@Override
		public boolean shouldCreatePropertyConfigElem(int sectionPropIndex, BaseProperty<?, ?> prop) {
			return prop.property().equals("cachedTime") || prop.property().equals("previousCachedTime");
		}
	}
}
