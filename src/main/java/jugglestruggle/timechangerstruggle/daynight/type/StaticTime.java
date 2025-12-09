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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

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
		
		prop.add(new FancySectionProperty("time", new TranslatableText(PROPERTIES_KEY+"time")));
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
	

	// Introduced in v0.0.1: Make both Static and Moving Time use the same quick-options 
	// as they're not anymore different to what they do with their core functionality.
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
		
		MutableText lShiftKey = StaticTime.createQuickOptElemKey("shift");
		MutableText lCtrlKey = StaticTime.createQuickOptElemKey(MinecraftClient.IS_SYSTEM_MAC ? "super" : "control");
		MutableText lAltKey = StaticTime.createQuickOptElemKey("alt");
		
		int i; 
		
		List<OrderedText> howToUseTooltipsPreset = new ArrayList<>(4);
		
		for (i = 1; i <= 4; ++i) 
		{
			howToUseTooltipsPreset.addAll(
				screen.getTextRenderer().wrapLines(TimeChangerScreen.translateTextAsGrayColor(
				setPropsTooltip + "tooltip." + i, lShiftKey, lCtrlKey, lAltKey), 200)
			);
		}
		
		i = 0;
		ButtonWidgetEx bw;
		
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
			tooltipsB.add(new TranslatableText(setPropsTooltip + cycleName).asOrderedText());
			
			// How to use tooltip lines 
			if (enableAdditionOptions)
				tooltipsB.addAll(howToUseTooltipsPreset);
			
			bw = new ButtonWidgetEx
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
			
			bw.setNarrationBuilder((bwx, b) -> bwx.appendNarrationTooltipLine(b, (byte)1, 1, 0));
			
			// add i + 1 before setting it as the index (e.g. we want to 
			// add the button widget to index 1 instead of 0 even if i is 0)
			itemsToAdd[++i] = bw;
		}
		
		itemsToAdd[0] = timeWidget;
		
		timeWidget.setWidth(148 - (20 * setTimesSize));
		timeWidget.setHeight(18);
		
		return itemsToAdd;
	}

	/*
	public Element[] createQuickOptionElements2(TimeChangerScreen screen)
	{
		final Iterator<BaseProperty<?, ?>> propsCreated = this.createProperties().iterator();

		final FancySectionProperty sectionProp = (FancySectionProperty)propsCreated.next();
		final String sectionRoughLang = ((TranslatableText)sectionProp.get()).getKey();

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
				new TranslatableText(sectionRoughLang+".worldtime."+cycleName), 
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
	*/
	
	// v0.0.2
	public static MutableText createQuickOptElemKey(String key)
	{
		MutableText keyText = new TranslatableText("jugglestruggle.tcs.keytype." + key);	
		keyText.styled(s -> s.withColor(0xBFFFBF).withBold(true));
		
		return keyText;
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
				case NOON -> new LiteralText("\u2600");
				case MIDNIGHT -> new LiteralText("\u263D");
				case SUNRISE -> new LiteralText("\u25D3");
				case SUNSET -> new LiteralText("\u25D2");
				
				default -> LiteralText.EMPTY;
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
			return new TranslatableText("jugglestruggle.tcs.dnt.statictime");
		}
		@Override
		public Text getTranslatableDescription() {
			return new TranslatableText("jugglestruggle.tcs.dnt.statictime.description");
		}
		
		@Override
		public boolean hasOptionsToEdit() {
			return true;
		}
	}
}
