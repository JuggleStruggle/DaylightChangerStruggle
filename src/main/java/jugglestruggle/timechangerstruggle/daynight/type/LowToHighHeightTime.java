package jugglestruggle.timechangerstruggle.daynight.type;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;
import jugglestruggle.timechangerstruggle.config.property.DoubleValue;
import jugglestruggle.timechangerstruggle.config.property.LongValue;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;

import java.util.Set;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

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
		
		double myY = camEntity.getEntityPos().getY();
		
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
		ImmutableSet.Builder<BaseProperty<?, ?>> props = ImmutableSet.builderWithExpectedSize(5);
		
		final String sectLang = "jugglestruggle.tcs.dnt.lowtohighheighttime.properties.";
		
		props.add(new FancySectionProperty("minmaxheight", Text.translatable(sectLang+"minmaxheight")));
		props.add(new DoubleValue("minHeight",     this.minHeight, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
		props.add(new LongValue  ("minHeightTime", this.minHeightTime, Long.MIN_VALUE, Long.MAX_VALUE));
		props.add(new DoubleValue("maxHeight",     this.maxHeight, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
		props.add(new LongValue  ("maxHeightTime", this.maxHeightTime, Long.MIN_VALUE, Long.MAX_VALUE));
		
		return props.build();
	}
	
	@Override
	public void writePropertyValueToCycle(BaseProperty<?, ?> property, PropertyWriterSource writer)
	{
		final String belongingKey = property.property();
		
		if (property instanceof LongValue prop)
		{
			switch (belongingKey)
			{
				case "minHeightTime" -> this.minHeightTime = prop.get();
				case "maxHeightTime" -> this.maxHeightTime = prop.get(); 
			}
		}
		else if (property instanceof DoubleValue prop)
		{
			switch (belongingKey)
			{
				case "minHeight" -> this.minHeight = prop.get();
				case "maxHeight" -> this.maxHeight = prop.get();
			}
		}
		
		if (writer == PropertyWriterSource.USER)
			TimeChangerStruggleClient.updateWorldDaylightCycle(true);
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
