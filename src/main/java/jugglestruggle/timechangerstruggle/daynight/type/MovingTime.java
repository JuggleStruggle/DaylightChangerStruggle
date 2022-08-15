package jugglestruggle.timechangerstruggle.daynight.type;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;
import jugglestruggle.timechangerstruggle.config.property.EnumValue;
import jugglestruggle.timechangerstruggle.config.property.IntValue;
import jugglestruggle.timechangerstruggle.config.property.LongValue;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;
import jugglestruggle.timechangerstruggle.util.EasingType;
import jugglestruggle.timechangerstruggle.util.Easings;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Set;

import net.minecraft.text.Text;

import com.google.common.collect.ImmutableSet;

/**
 * A time which the user can define the speed, the starting time (will be moved
 * as soon as the next tick occurs) and some variables.
 *
 * @author JuggleStruggle
 * @implNote Created on 26-Jan-2022, Wednesday
 */
@Environment(EnvType.CLIENT)
public class MovingTime extends MovingTimeBasis
{
	public int speedForImmediateCalls = 1;
	public int speedForLaterCalls = 80;

	@Override
	public void updateCall()
	{
		if (super.ticksUntilNextCall == 0)
		{
			super.previousInterpolatedTime =
			super.previousCachedTime = super.cachedTime;
			
			super.cachedTime += this.speedForImmediateCalls;
		}
		else 
		{
			super.updateCall();
		}
	}
	@Override
	public void updateInterpolation() {
		super.nextInterpolatedTime += this.speedForLaterCalls;
	}

	@Override
	public Class<?> getBuilderClass() {
		return Builder.class;
	}
	

	@Override
	public Set<BaseProperty<?, ?>> createProperties()
	{
		ImmutableSet.Builder<BaseProperty<?, ?>> prop = ImmutableSet.builderWithExpectedSize(9);
		
		final String sectLang = "jugglestruggle.tcs.dnt.movingtime.properties.";
		
		prop.add(new FancySectionProperty("updating", Text.translatable(sectLang+"updating")));
		prop.add(new LongValue("ticksUntilNextUpdate", super.ticksUntilNextCall, 0L, Long.MAX_VALUE));
		
		prop.add(new FancySectionProperty("speed", Text.translatable(sectLang+"speed")));
		prop.add(new IntValue("immediateSpeed", this.speedForImmediateCalls, Integer.MIN_VALUE, Integer.MAX_VALUE));
		prop.add(new IntValue("pausedSpeed", this.speedForLaterCalls, Integer.MIN_VALUE, Integer.MAX_VALUE));

		prop.add(new FancySectionProperty("easings", Text.translatable(sectLang+"easings")));
		prop.add(new EnumValue<>("easingBetweenTicks", this.easingBetweenTicks, Easings.LINEAR, Easings.values())
			.setVTT(easing -> easing.getFormattedText()));
		prop.add(new EnumValue<>("easingTypeBetweenTicks", this.easingType, EasingType.BETWEEN, EasingType.values())
			.setVTT(easing -> easing.getFormattedText()));
		
		return prop.build();
	}
	
	@Override
	public void writePropertyValueToCycle(BaseProperty<?, ?> property)
	{
		final String belongingKey = property.property();
		
		if (property instanceof LongValue)
		{
			if (belongingKey.equals("ticksUntilNextUpdate")) {
				this.ticksUntilNextCall = ((LongValue)property).get();
			}
		}
		else if (property instanceof IntValue)
		{
			IntValue prop = (IntValue)property;
			
			switch (belongingKey)
			{
				case "immediateSpeed": 
					this.speedForImmediateCalls = prop.get(); break;
				case "pausedSpeed": 
					this.speedForLaterCalls = prop.get(); break;
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
						this.easingBetweenTicks = (Easings)prop.get(); 
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
						this.easingType = (EasingType)prop.get(); 
						break;
					}
				}
			}
		}
	}

	public static class Builder implements DayNightCycleBuilder
	{
		@Override
		public DayNightCycleBasis create() {
			return new MovingTime();
		}

		@Override
		public String getKeyName() {
			return "movingtime";
		}
		
		@Override
		public Text getTranslatableName() {
			return Text.translatable("jugglestruggle.tcs.dnt.movingtime");
		}
		@Override
		public Text getTranslatableDescription() {
			return Text.translatable("jugglestruggle.tcs.dnt.movingtime.description");
		}
		
		@Override
		public boolean hasOptionsToEdit() {
			return true;
		}
	}
}
