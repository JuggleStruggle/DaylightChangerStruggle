package jugglestruggle.timechangerstruggle.daynight.type;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.client.config.widget.NumericFieldWidgetConfig;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.client.widget.ButtonWidgetEx;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;
import jugglestruggle.timechangerstruggle.config.property.LongValue;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * A time which the user defines and remains as it is.
 * 
 * <p> Essentially, this should be assumed as if the daylight cycle
 * is off and the time is set through the time command.
 *
 * @author JuggleStruggle
 * @implNote Created on 26-Jan-2022, Wednesday
 */
@Environment(EnvType.CLIENT)
public class StaticTime implements DayNightCycleBasis
{
	public long timeSet = 0;

	@Override
	public long getModifiedTime(ClientWorld world, DayNightGetterType executor, boolean previous) {
		return this.timeSet;
	}
	@Override
	public long getCachedTime() {
		return this.timeSet;
	}

	@Override
	public Class<?> getBuilderClass() {
		return Builder.class;
	}
	
	@Override
	public Element[] createQuickOptionElements(TimeChangerScreen screen)
	{
		final Iterator<BaseProperty<?, ?>> propsCreated = this.createProperties().iterator();

		final FancySectionProperty sectionProp = (FancySectionProperty)propsCreated.next();
		final String sectionRoughLang = ((TranslatableTextContent)sectionProp.get().getContent()).getKey();

		final NumericFieldWidgetConfig<Long> worldTimeProp = (NumericFieldWidgetConfig<Long>)
			((LongValue)propsCreated.next()).createConfigElement(screen, sectionProp);
		
		List<PresetSetTimes> setTimes = 
			Lists.newArrayList(PresetSetTimes.values()).stream()
			.filter(presetTime -> presetTime.shouldShowInQuickOptions()).toList();
		
		
		final int setTimesSize = setTimes.size();
		final ButtonWidgetEx[] dayCycles = new ButtonWidgetEx[setTimesSize];
		
		Iterator<PresetSetTimes> setTimesIterator = setTimes.iterator();
		
		int i = 0;
		while (setTimesIterator.hasNext())
		{
			final PresetSetTimes entry = setTimesIterator.next();
			final Text displayText = entry.getQuickOptionsText();
			final String cycleName = entry.name().toLowerCase(Locale.ROOT);
			
			dayCycles[i] = new ButtonWidgetEx
			(
				20, 20, displayText, 
				Text.translatable(sectionRoughLang+".worldtime."+cycleName), 
				null, screen.getTextRenderer(), b -> 
				{
					Long value = entry.getTime();
					Long baseTime = 0L;
					
					final boolean shiftHeld = Screen.hasShiftDown();
					final boolean controlHeld = Screen.hasControlDown();
					final boolean altHeld = Screen.hasAltDown();
					
					if (shiftHeld || controlHeld || altHeld) 
					{
						baseTime = worldTimeProp.getProperty().get();
						
						// Shift just adds so no reason to do anything
						// Control sets the value negative
						if (controlHeld)
							value = -value;
						// Alt divides the value to half
						if (altHeld)
							value /= 2L;
					}
					
					final Long finalValue = baseTime + value;
					worldTimeProp.setText(finalValue.toString()); 
				}
			);
			
			++i;
		}
		worldTimeProp.setWidth(148 - (20 * setTimesSize));
		
		Element[] itemsToAdd = new Element[1 + setTimesSize];
		itemsToAdd[0] = worldTimeProp;
		
		return ArrayUtils.insert(1, itemsToAdd, dayCycles);
	}
	
	@Override
	public Set<BaseProperty<?, ?>> createProperties()
	{
		ImmutableSet.Builder<BaseProperty<?, ?>> prop = ImmutableSet.builderWithExpectedSize(2);
		
		final String sectLang = "jugglestruggle.tcs.dnt.statictime.properties.";

		prop.add(new FancySectionProperty("time", Text.translatable(sectLang+"time")));
		prop.add(new LongValue("worldtime", this.timeSet, null, null));

		return prop.build();
	}
	
	@Override
	public void writePropertyValueToCycle(BaseProperty<?, ?> property)
	{
		final String belongingKey = property.property();
		
		if (property instanceof LongValue && belongingKey.equals("worldtime")) {
			this.timeSet = ((LongValue)property).get();
		}
	}
	
	public static enum PresetSetTimes
	{
		NOON(6000L), MIDNIGHT(18000L), SUNRISE(0L), SUNSET(12000L),
		DAY(1000L, false, true), NIGHT(13000L, false, true)
		;
		
		private final long time;
		private final boolean showInQuickOptions;
		private final boolean showInCommand;
		
		private PresetSetTimes(long time) {
			this(time, true, true);
		}
		private PresetSetTimes(long time, boolean showInQuickOptions, boolean showInCommand)
		{
			this.time = time; 
			this.showInQuickOptions = showInQuickOptions;
			this.showInCommand = showInCommand;
		}
		
		public long getTime() {
			return this.time;
		}
		public boolean shouldShowInCommand() {
			return this.showInCommand;
		}
		public boolean shouldShowInQuickOptions() {
			return showInQuickOptions;
		}
		
		public Text getQuickOptionsText()
		{
			return switch (this)
			{
				case NOON -> Text.of("\u2600");
				case MIDNIGHT -> Text.of("\u263D");
				case SUNRISE -> Text.of("\u25D3");
				case SUNSET -> Text.of("\u25D2");
				
				default -> Text.empty();
			};
		}
	}

	public static class Builder implements DayNightCycleBuilder
	{
		@Override
		public DayNightCycleBasis create() {
			return new StaticTime();
		}
		
		@Override
		public String getKeyName() {
			return "statictime";
		}

		@Override
		public Text getTranslatableName() {
			return Text.translatable("jugglestruggle.tcs.dnt.statictime");
		}
		@Override
		public Text getTranslatableDescription() {
			return Text.translatable("jugglestruggle.tcs.dnt.statictime.description");
		}
		
		@Override
		public boolean hasOptionsToEdit() {
			return true;
		}
	}
}
