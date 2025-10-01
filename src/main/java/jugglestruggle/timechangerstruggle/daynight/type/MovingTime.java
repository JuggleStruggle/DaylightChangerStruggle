package jugglestruggle.timechangerstruggle.daynight.type;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;
import jugglestruggle.timechangerstruggle.config.property.IntValue;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Set;

import net.minecraft.client.gui.Element;
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
	final static String PROPERTIES_KEY = "jugglestruggle.tcs.dnt.movingtime.properties.";
	
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
		else {
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

	@Override // Introduced in v0.0.1
	public boolean saveOnWorldChange() {
		return true;
	}

	@Override // Introduced in v0.0.1
	public Element[] createQuickOptionElements(TimeChangerScreen screen)
	{
		return StaticTime.createQuickOptionElementsShared(screen, 
			this.getMovingTimeBasisCategory(), this.getCachedTimeProp()
		);
	}

	@Override
	public Set<BaseProperty<?, ?>> createProperties()
	{
		ImmutableSet.Builder<BaseProperty<?, ?>> props = ImmutableSet.builderWithExpectedSize(14);
		
		props.add(new FancySectionProperty("updating", Text.translatable(PROPERTIES_KEY+"updating")));
		props.add(this.getTicksUntilNextCallProp());
		
		props.add(new FancySectionProperty("speed", Text.translatable(PROPERTIES_KEY+"speed")));
		props.add(new IntValue("immediateSpeed", this.speedForImmediateCalls, Integer.MIN_VALUE, Integer.MAX_VALUE));
		props.add(new IntValue("pausedSpeed", this.speedForLaterCalls, Integer.MIN_VALUE, Integer.MAX_VALUE));

		props.add(new FancySectionProperty("easings", Text.translatable(PROPERTIES_KEY+"easings")));
		props.add(this.getEasingBetweenTicksProp());
		props.add(this.getEasingTypeBetweenTicksProp());
		
		props.add(this.getMovingTimeBasisCategory());
		props.add(this.getCachedTimeProp());
		props.add(this.getPrevCachedTimeProp());
		props.add(this.getPrevInterpolatedTimeProp());
		props.add(this.getNextInterpolatedTimeProp());
		props.add(this.getTicksPassedProp());
		
		
		return props.build();
	}
	
	@Override
	public void writePropertyValueToCycle(BaseProperty<?, ?> property, PropertyWriterSource writer)
	{
		final String belongingKey = property.property();
		
		if (property instanceof IntValue prop)
		{
			switch (belongingKey)
			{
				case "immediateSpeed" -> {
					this.speedForImmediateCalls = prop.get(); return;
				}
				case "pausedSpeed" -> {
					this.speedForLaterCalls = prop.get(); return;
				}
			}
		}
		
		this.writePropertyValueToCycleForBasis(property, writer);
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
