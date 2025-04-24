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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import com.google.common.collect.ImmutableList;
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
	final static String PROPERTIES_KEY = "jugglestruggle.tcs.dnt.statictime.properties.";
	
	
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

		return StaticTime.createQuickOptionElementsShared(screen, 
			(FancySectionProperty)propsCreated.next(), (LongValue)propsCreated.next()
		);
	}
	
	@Override
	public Set<BaseProperty<?, ?>> createProperties()
	{
		ImmutableSet.Builder<BaseProperty<?, ?>> prop = ImmutableSet.builderWithExpectedSize(2);

		prop.add(new FancySectionProperty("time", Text.translatable(PROPERTIES_KEY+"time")));
		prop.add(new LongValue("worldtime", this.timeSet, null, null));

		return prop.build();
	}
	
	@Override
	public void writePropertyValueToCycle(BaseProperty<?, ?> property, PropertyWriterSource writer)
	{
		final String belongingKey = property.property();
		
		if (belongingKey.equals("worldtime") && property instanceof LongValue lv)
			this.timeSet = lv.get();
	}
	
	
	// v0.0.1+1.21.5 port exclusive: make both Static and Moving Time use the same quick-options as
	// they're not anymore different to what they do in their core functionality
	public static Element[] createQuickOptionElementsShared(TimeChangerScreen screen, 
		FancySectionProperty sectionProp, LongValue timeValue)
	{
		final NumericFieldWidgetConfig<Long> timeWidget = (NumericFieldWidgetConfig<Long>)
			timeValue.createConfigElement(screen, sectionProp);
		
		final List<PresetSetTimes> setTimes = 
			Lists.newArrayList(PresetSetTimes.values()).stream()
			.filter(presetTime -> presetTime.shouldShowInQuickOptions()).toList();
		
		final int setTimesSize = setTimes.size();
		final Element[] itemsToAdd = new Element[1 + setTimesSize];
		
		final String setPropsTooltip = PROPERTIES_KEY + "time.worldtime.";
		final Iterator<PresetSetTimes> setTimesIterator = setTimes.iterator();
		
		int i; 
		
		List<OrderedText> howToUseTooltipsPreset = new ArrayList<>(4);
		
		for (i = 1; i <= 4; ++i) 
		{
			howToUseTooltipsPreset.addAll(screen.getTextRenderer()
				.wrapLines(Text.translatable(setPropsTooltip + "tooltip." + i), 200));
		}
		
		i = 0;
		
		while (setTimesIterator.hasNext())
		{
			final PresetSetTimes entry = setTimesIterator.next();
			final Text displayText = entry.getQuickOptionsText();
			final String cycleName = entry.name().toLowerCase(Locale.ROOT);
			
			// not used if enableAdditionOptions is SUNRISE due to its value being 0
			final boolean enableAdditionOptions = entry != PresetSetTimes.SUNRISE;
			
			ImmutableList.Builder<OrderedText> tooltipsB = 
				ImmutableList.builderWithExpectedSize(enableAdditionOptions ? 5 : 1);
			
			// Name of the tooltip text that belongs on the first line
			tooltipsB.add(Text.translatable(setPropsTooltip + cycleName).asOrderedText());
			
			// How to use tooltip lines: 
			if (enableAdditionOptions)
				tooltipsB.addAll(howToUseTooltipsPreset);
			
			// add i + 1 before setting it as the index (e.g. we want to 
			// add the button widget to index 1 instead of 0 even if i is 0)
			itemsToAdd[++i] = new ButtonWidgetEx
			(
				20, 20, displayText, tooltipsB.build(),
				screen.getTextRenderer(), b -> 
				{
					Long addTime = entry.getTime();
					Long baseTime = 0L;
					
					if (enableAdditionOptions)
					{
						final boolean shiftHeld = Screen.hasShiftDown();
						final boolean controlHeld = Screen.hasControlDown();
						final boolean altHeld = Screen.hasAltDown();
						
						if (shiftHeld || controlHeld || altHeld) 
						{
							baseTime = timeWidget.getProperty().get();
							
							// Shift just adds so no reason to do anything
							// Control sets the value negative
							if (controlHeld)
								addTime = -addTime;
							// Alt divides the value to half
							if (altHeld)
								addTime /= 2L;
						}
					}
					
					final Long finalValue = baseTime + addTime;
					timeWidget.setText(finalValue.toString());
				}
			);
		}
		
		itemsToAdd[0] = timeWidget;
		
		timeWidget.setWidth(150 - (20 * setTimesSize));
		timeWidget.setHeight(20);
		
		return itemsToAdd;
	}
	
	public static enum PresetSetTimes
	{
		NOON(6000L), MIDNIGHT(18000L), SUNRISE(0L), SUNSET(12000L),
		DAY(1000L, false, true), NIGHT(13000L, false, true);
		
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
			return this.showInQuickOptions;
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
