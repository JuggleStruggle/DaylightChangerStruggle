package jugglestruggle.timechangerstruggle.daynight.type;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;
import jugglestruggle.timechangerstruggle.config.property.DoubleValue;
import jugglestruggle.timechangerstruggle.config.property.LongValue;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;

import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import com.google.common.collect.ImmutableSet;

/**
 * A daylight cycle which uses the current viewing entity's height
 * to determine as to whether it should shine or night-ify.
 *
 * @author JuggleStruggle
 * @implNote Created on 21-Feb-2022, Monday
 */
public class LowToHighHeightTime implements DayNightCycleBasis
{
	protected long cachedTime = 0;
	protected long cachedTimePrev = 0;

	public double minHeight = 0.0;
	public double maxHeight = 64.0;
	
	public long minHeightTime = 18000L;
	public long maxHeightTime = 30000L;
	
	@Override
	public void tick()
	{
		Entity camEntity = MinecraftClient.getInstance().getCameraEntity();
		
		if (camEntity == null)
		{
			this.cachedTime = this.cachedTimePrev = 0L;
			return;
		}
		
		double myY = camEntity.getPos().getY();
		
		this.cachedTimePrev = this.cachedTime;
		
		if (myY < this.minHeight)
			this.cachedTime = this.minHeightTime;
		else if (myY > this.maxHeight)
			this.cachedTime = this.maxHeightTime;
		else
		{
			double heightLength = this.maxHeight - this.minHeight;
			double res = myY - this.minHeight;
			res = res / heightLength;
			
			this.cachedTime = (long)((double)this.minHeightTime + (double)(this.maxHeightTime - this.minHeightTime) * res);
		}
		
//		if (Keybindings.toggleWorldTimeKey.isPressed())
//		{
//			jugglestruggle.timechangerstruggle.TimeChangerStruggle.LOGGER
//			.info("cached time: {} | prev: {}", this.cachedTime, this.cachedTimePrev);
//		}
	}
	
	@Override
	public long getModifiedTime(ClientWorld world, DayNightGetterType executor, boolean previous) {
		return previous ? this.cachedTimePrev : this.cachedTime;
	}

	@Override
	public long getCachedTime() {
		return this.cachedTime;
	}

	@Override
	public Class<?> getBuilderClass() {
		return Builder.class;
	}
	
	@Override
	public Set<BaseProperty<?, ?>> createProperties()
	{
		ImmutableSet.Builder<BaseProperty<?, ?>> prop = ImmutableSet.builderWithExpectedSize(5);
		
		final String sectLang = "jugglestruggle.tcs.dnt.lowtohighheighttime.properties.";
		
		prop.add(new FancySectionProperty("minmaxheight", Text.translatable(sectLang+"minmaxheight")));
		prop.add(new DoubleValue("minHeight",     this.minHeight, (double)Long.MIN_VALUE, Double.MAX_VALUE));
		prop.add(new LongValue  ("minHeightTime", this.minHeightTime, Long.MIN_VALUE, Long.MAX_VALUE));
		prop.add(new DoubleValue("maxHeight",     this.maxHeight, (double)Long.MIN_VALUE, Double.MAX_VALUE));
		prop.add(new LongValue  ("maxHeightTime", this.maxHeightTime, Long.MIN_VALUE, Long.MAX_VALUE));
		
		return prop.build();
	}
	
	@Override
	public void writePropertyValueToCycle(BaseProperty<?, ?> property)
	{
		final String belongingKey = property.property();
		
		if (property instanceof LongValue)
		{
			LongValue prop = (LongValue)property;
			
			switch (belongingKey)
			{
				case "minHeightTime": 
					this.minHeightTime = prop.get(); break;
				case "maxHeightTime": 
					this.maxHeightTime = prop.get(); break;
			}
		}
		else if (property instanceof DoubleValue)
		{
			DoubleValue prop = (DoubleValue)property;
			
			switch (belongingKey)
			{
				case "minHeight": 
					this.minHeight = prop.get(); break;
				case "maxHeight": 
					this.maxHeight = prop.get(); break;
			}
		}
	}

	public static class Builder implements DayNightCycleBuilder
	{
		@Override
		public DayNightCycleBasis create() {
			return new LowToHighHeightTime();
		}

		@Override
		public String getKeyName() {
			return "lowtohighheighttime";
		}
		
		@Override
		public Text getTranslatableName() {
			return Text.translatable("jugglestruggle.tcs.dnt.lowtohighheighttime");
		}
		@Override
		public Text getTranslatableDescription() {
			return Text.translatable("jugglestruggle.tcs.dnt.lowtohighheighttime.description");
		}
		
		@Override
		public boolean hasOptionsToEdit() {
			return true;
		}
	}
}
