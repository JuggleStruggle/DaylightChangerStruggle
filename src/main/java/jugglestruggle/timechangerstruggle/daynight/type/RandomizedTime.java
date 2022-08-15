package jugglestruggle.timechangerstruggle.daynight.type;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.client.config.widget.WidgetConfigInterface;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;
import jugglestruggle.timechangerstruggle.config.property.BooleanValue;
import jugglestruggle.timechangerstruggle.config.property.EnumValue;
import jugglestruggle.timechangerstruggle.config.property.LongValue;
import jugglestruggle.timechangerstruggle.config.property.StringValue;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;
import jugglestruggle.timechangerstruggle.util.EasingType;
import jugglestruggle.timechangerstruggle.util.Easings;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.Map.Entry;

import net.minecraft.text.Text;

import com.google.common.collect.ImmutableSet;

/**
 * A time which it is randomized through crazy means. This can essentially
 * turn into seizures as the time is ran though the randomizer. The initial
 * seed can be put, but whatever.
 * 
 * @author JuggleStruggle
 * @implNote
 * Created on 26-Jan-2022, Wednesday
 */
@Environment(EnvType.CLIENT)
public class RandomizedTime extends MovingTimeBasis
{
	/** 
	 * The initial seed whenever this class is created, loaded or 
	 * property-loaded provided that the property-load was done without 
	 * any tick updates being called. 
	 * 
	 * <p> Can be left out {@code null} to start with the 
	 * {@linkplain System#currentTimeMillis() current millisecond}.
	 */
	public String startingSeed;
	
	/**
	 * Minimum value for daylight randomization.
	 */
	public long minimumRandomTime = 0L;
	/**
	 * Maximum value for daylight randomization.
	 */
	public long maximumRandomTime = 24000L;


	
	/**
	 * As the field name suggests, this randomizes {@link #ticksUntilNextRNG}
	 * to a user-defined value.
	 */
	public boolean randomizeTicksUntilNextRNG = false;
	/**
	 * Minimum value for randomization. The minimum value for this field 
	 * must be 1.
	 * 
	 * <p> Only used if {@link #randomizeTicksUntilNextRNG} is enabled.
	 */
	public long ticksUntilNextRNGMin = 1L;
	/**
	 * Maximum value for randomization. The maximum value for this field 
	 * must be {@link Long#MAX_VALUE}.
	 * 
	 * <p> Only used if {@link #randomizeTicksUntilNextRNG} is enabled.
	 */
	public long ticksUntilNextRNGMax = 24000L;
	
	
	/**
	 * As the field name suggests, this randomizes {@link #easingBetweenTicks}
	 * to whatever it can come up. This is only updated whenever {@link #updateRNG()}
	 * is called.
	 * 
	 * <p> <i>(Maybe in the future change the easing on-the-fly?)</i>
	 */
	public boolean randomizeEasingBetweenTicks = false;
	/**
	 * A similar goal to that of {@link #randomizeEasingBetweenTicks} but
	 * instead of targeting on {@link Easings}, it instead targets the
	 * smoothness transition like in or out, which is {@link EasingType}.
	 */
	public boolean randomizeEasingTypeBetweenTicks = false;
	
	/**
	 * This is the value that will be storedd and the value which will override
	 * {@link #ticksUntilNextRNG} whenever {@link #randomizeTicksUntilNextRNG}
	 * is off.	 
	 */
	public long ticksUntilNextRNGBasis = 40L;
	/**
	 * This is the value that will be stored and the value which will override
	 * {@link #easingBetweenTicks} whenever {@link #randomizeEasingBetweenTicks}
	 * is off.
	 */
	public Easings easingBetweenTicksBasis = Easings.LINEAR;
	/**
	 * This is the value that will be stored and the value which will override
	 * {@link #easingType} whenever {@link #randomizeEasingTypeBetweenTicks}
	 * is off.
	 */
	public EasingType easingTypeBetweenTicksBasis = EasingType.BETWEEN;
	
	
	/**
	 * The master randomizer. BEHOLD!
	 */
	public Random rng;
	
	/**
	 * Only used for helping in creation of the RNG seed.
	 */
	protected boolean hasTickUpdateOccured = false;
	
	
	public RandomizedTime(String startingSeed, boolean applyRNG) {
		this.createRNG(startingSeed, applyRNG);
	}
	private void createRNG(String startingSeed, boolean applyRNG)
	{
		if (startingSeed == null || startingSeed.length() == 0) {
			this.rng = new Random(System.currentTimeMillis());
		} 
		else 
		{
			int seed = 0;
			
			try {
				seed = Integer.parseInt(startingSeed);
			} catch (Exception e) {
				seed = startingSeed.hashCode();
			}
			
			this.rng = new Random(seed);
			this.startingSeed = startingSeed;
		}
		
		if (applyRNG) 
		{
			// Called twice to update the current and previous interpolation
			// times so that it doesn't create the sad immersion that you start
			// with nothing (well, now you know...)
			this.updateCall(); this.updateCall();
		}
	}

	@Override
	public void tick() 
	{
		super.tick();
		this.hasTickUpdateOccured = true;
	}
	
	@Override
	public void updateInterpolation()
	{
		this.genNextRandomTime();
		
		if (this.randomizeTicksUntilNextRNG)
			this.genTicksUntilNextRNG();
		
		if (this.randomizeEasingBetweenTicks)
			this.genRandomizedEasing();
		if (this.randomizeEasingTypeBetweenTicks)
			this.genRandomizedEasingType();
	}
	
	private long genNextLongRNG(Random rng, long minRand, long maxRand) 
	{
		if (minRand < 0)
			minRand = 0;
		
		if (maxRand < 0)
			maxRand = 1;
		// Force maxRand to be inclusive if not the maximum value Long can hold
		// as Java's Random implementation makes the maximum value exclusive
		else if (maxRand != Long.MAX_VALUE)
			maxRand += 1L;
		
		return rng.nextLong(minRand, maxRand);
	}
	
	private void shouldGenTicksUntilNextRNG() 
	{
		if (this.ticksUntilNextCall < this.ticksUntilNextRNGMin ||
			this.ticksUntilNextCall > this.ticksUntilNextRNGMax)
		{
			this.genTicksUntilNextRNG();
		}
	}
	private void genNextRandomTime() {
		this.nextInterpolatedTime = this.genNextLongRNG(this.rng, this.minimumRandomTime, this.maximumRandomTime);
	}
	private void genTicksUntilNextRNG() {
		this.ticksUntilNextCall = this.genNextLongRNG(this.rng, this.ticksUntilNextRNGMin, this.ticksUntilNextRNGMax);
	}
	
	private void genRandomizedEasing()
	{
		Easings easingToUse = this.genRandomizedEnum(Easings.values(), easing -> easing.canBeRandomlyUsed());
		
		if (easingToUse != null)
			this.easingBetweenTicks = easingToUse;
	}
	
	private void genRandomizedEasingType()
	{
		EasingType easingToUse = this.genRandomizedEnum(EasingType.values(), null);
		
		if (easingToUse != null)
			this.easingType = easingToUse;
	}
	private <E extends Enum<E>> E genRandomizedEnum(E[] types, Predicate<E> canBeUsedIfSelected)
	{
		E typeToUse = null;
		byte tries = 0;
		
		final int typesSize = types.length;
		
		while (true)
		{
			if (tries > 5) {
				break;
			}
			
			int index = this.rng.nextInt(typesSize);
			
			if (index < 0 || index >= typesSize) {
				++tries; continue;
			}
			
			typeToUse = types[index];
			
			if (canBeUsedIfSelected == null || canBeUsedIfSelected.test(typeToUse))
				break;
				
			++tries;
		}
		
		return typeToUse;
	}
	
	
	@Override
	public Class<?> getBuilderClass() {
		return Builder.class;
	}
	
	@Override
	public Set<BaseProperty<?, ?>> createProperties()
	{
		ImmutableSet.Builder<BaseProperty<?, ?>> prop = ImmutableSet.builderWithExpectedSize(12);
		
		final String sectLang = "jugglestruggle.tcs.dnt.randomizer.properties.";
		
		prop.add(new FancySectionProperty("seed", Text.translatable(sectLang+"seed")));
		prop.add(new StringValue("startingSeed", (this.startingSeed == null) ? "" : this.startingSeed).setEmptyTextAllowance(true));
		
		prop.add(new FancySectionProperty("daylightrandomtime", Text.translatable(sectLang+"daylightrandomtime")));
		prop.add(new LongValue("minimumRandomTime", this.minimumRandomTime, 0L, Long.MAX_VALUE));
		prop.add(new LongValue("maximumRandomTime", this.maximumRandomTime, 0L, Long.MAX_VALUE));
		
		prop.add(new FancySectionProperty("ticksuntilnextrng", Text.translatable(sectLang+"ticksuntilnextrng")));
		prop.add(new BooleanValue("randomizeTicksUntilNextRNG", this.randomizeTicksUntilNextRNG));
		prop.add(new LongValue("ticksUntilNextRNG", this.ticksUntilNextRNGBasis, 1L, Long.MAX_VALUE));
		prop.add(new LongValue("ticksUntilNextRNGMin", this.ticksUntilNextRNGMin, 1L, Long.MAX_VALUE));
		prop.add(new LongValue("ticksUntilNextRNGMax", this.ticksUntilNextRNGMax, 1L, Long.MAX_VALUE));
		
		prop.add(new FancySectionProperty("easings", Text.translatable(sectLang+"easings")));
		prop.add(new BooleanValue("randomizeEasingBetweenTicks", this.randomizeEasingBetweenTicks));
		prop.add(new EnumValue<>("easingBetweenTicks", this.easingBetweenTicksBasis, Easings.LINEAR, Easings.values())
			.setVTT(easing -> easing.getFormattedText()));
		prop.add(new BooleanValue("randomizeEasingTypeBetweenTicks", this.randomizeEasingTypeBetweenTicks));
		prop.add(new EnumValue<>("easingTypeBetweenTicks", this.easingTypeBetweenTicksBasis, EasingType.BETWEEN, EasingType.values())
			.setVTT(easing -> easing.getFormattedText()));
		
		return prop.build();
	}
	
	@Override
//	public <B extends BaseProperty<B, V>, V> void writePropertyValueToCycle(B property)
	public void writePropertyValueToCycle(BaseProperty<?, ?> property)
	{
		final String belongingKey = property.property();
		
		if (property instanceof StringValue)
		{
			if (belongingKey.equals("startingSeed"))
			{
				this.startingSeed = ((StringValue)property).get();
				
				if (!this.hasTickUpdateOccured)
					this.createRNG(this.startingSeed, true);
			}
		}
		else if (property instanceof LongValue)
		{
			LongValue prop = (LongValue)property;
			
			switch (belongingKey)
			{
				case "minimumRandomTime": 
					this.minimumRandomTime = prop.get(); break;
				case "maximumRandomTime": 
					this.maximumRandomTime = prop.get(); break;
					
				case "ticksUntilNextRNG": 
				{
					this.ticksUntilNextRNGBasis = prop.get();
					
					if (!this.randomizeTicksUntilNextRNG)
						this.ticksUntilNextCall = this.ticksUntilNextRNGBasis;
						
					break;
				}
				case "ticksUntilNextRNGMin": 
				case "ticksUntilNextRNGMax": 
				{
					if (belongingKey.equals("ticksUntilNextRNGMax"))
						this.ticksUntilNextRNGMax = prop.get();
					else
						this.ticksUntilNextRNGMin = prop.get();
						
					if (this.randomizeTicksUntilNextRNG)
						this.shouldGenTicksUntilNextRNG();
					else
						this.easingBetweenTicks = this.easingBetweenTicksBasis;
					
					break;
				}
			}
		}
		else if (property instanceof BooleanValue)
		{
			BooleanValue prop = (BooleanValue)property;
			
			switch (belongingKey)
			{
				case "randomizeTicksUntilNextRNG": 
				{
					this.randomizeTicksUntilNextRNG = prop.get(); 
					
					if (this.randomizeTicksUntilNextRNG)
						this.shouldGenTicksUntilNextRNG();
					else
						this.ticksUntilNextCall = this.ticksUntilNextRNGBasis;
					
					break;
				}
				case "randomizeEasingBetweenTicks": 
				{
					this.randomizeEasingBetweenTicks = prop.get(); 
					
					if (this.randomizeEasingBetweenTicks)
						this.genRandomizedEasing();
					else
						this.easingBetweenTicks = this.easingBetweenTicksBasis;
					
					break;
				}
				case "randomizeEasingTypeBetweenTicks": 
				{
					this.randomizeEasingTypeBetweenTicks = prop.get(); 
					
					if (this.randomizeEasingTypeBetweenTicks)
						this.genRandomizedEasingType();
					else
						this.easingType = this.easingTypeBetweenTicksBasis;
					
					break;
				}
			}
		}
		else if (property instanceof EnumValue<?>)
		{
			EnumValue<?> prop = (EnumValue<?>)property;
			
			if (prop.getDefaultValue() instanceof Easings)
			{
				switch (belongingKey)
				{
					case "easingBetweenTicks": 
					{
						this.easingBetweenTicksBasis = (Easings)prop.get(); 
						
						if (!this.randomizeEasingBetweenTicks)
							this.easingBetweenTicks = this.easingBetweenTicksBasis;
						
						break;
					}
				}
			}
			else if (prop.getDefaultValue() instanceof EasingType)
			{
				switch (belongingKey)
				{
					case "easingTypeBetweenTicks": 
					{
						this.easingTypeBetweenTicksBasis = (EasingType)prop.get(); 
						
						if (!this.randomizeEasingTypeBetweenTicks)
							this.easingType = this.easingTypeBetweenTicksBasis;
						
						break;
					}
				}
			}
		}
	}
	
	@Override
	public WidgetConfigInterface<?, ?>[][] rearrangeSectionElements
	(Entry<FancySectionProperty, List<WidgetConfigInterface<?, ?>>> entry, int elementsPerRow)
	{
		final List<WidgetConfigInterface<?, ?>> elements = entry.getValue();
		
		return switch (entry.getKey().property())
		{
//			default -> DayNightCycleBasis.super.rearrangeSectionElements(entry, elementsPerRow);
			default -> super.rearrangeSectionElements(entry, elementsPerRow);
				
			case "ticksuntilnextrng" -> new WidgetConfigInterface<?, ?>[][] 
			{
				// Randomize ticks until next RNG and non-RNG next ticks
				new WidgetConfigInterface<?, ?>[]{ elements.get(0), elements.get(1) },
				// Minimum and Maximum for RNG to touch
				new WidgetConfigInterface<?, ?>[]{ elements.get(2), elements.get(3) }
			};
		};
	}
	
	public static class Builder implements DayNightCycleBuilder
	{
		@Override
		public DayNightCycleBasis create() {
			return new RandomizedTime(null, false);
		}

		@Override
		public String getKeyName() {
			return "randomizers";
		}
		
		@Override
		public Text getTranslatableName() {
			return Text.translatable("jugglestruggle.tcs.dnt.randomizer");
		}
		@Override
		public Text getTranslatableDescription() {
			return Text.translatable("jugglestruggle.tcs.dnt.randomizer.description");
		}
		
		@Override
		public boolean hasOptionsToEdit() {
			return true;
		}
	}
}
