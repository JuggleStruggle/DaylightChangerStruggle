package jugglestruggle.timechangerstruggle.daynight;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.client.config.widget.WidgetConfigInterface;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;
import jugglestruggle.timechangerstruggle.config.property.IntValue;
import jugglestruggle.timechangerstruggle.config.property.StringValue;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

import com.google.common.collect.ImmutableSet;

/**
 * @author JuggleStruggle
 * @implNote Created on 26-Jan-2022, Wednesday
 */
@Environment(EnvType.CLIENT)
public interface DayNightCycleBasis
{
	/**
	 * Gets the time that the client world should be using.
	 * 
	 * <p> Note: This does not affect the server or the world
	 * property in any capacity as it just overrides a getter
	 * to apply its own.
	 * 
	 * @param world the client world getting the modified time
	 * @param executor the part of the code that called the time
	 * @param previous the last modified time, primarily used 
	 *                  for ticking
	 * 
	 * @return a {@code long} value representing the game time
	 */
	long getModifiedTime(ClientWorld world, DayNightGetterType executor, boolean previous);
	
	/**
	 * Returns the cached time provided by the cycle itself. 
	 * 
	 * <p> It is used on {@link TimeChangerScreen} to get the time 
	 * without having to constantly call {@link #getModifiedTime}.
	 * 
	 * @return a {@code long} value representing the game time
	 */
	long getCachedTime();
	
	/**
	 * Returns the class which builds this day night cycle.
	 * 
	 * <p> Required for checking if the builder in question is
	 * the same as what is going to be checked.
	 * 
	 * @return a builder class
	 */
	Class<?> getBuilderClass();
	
	/**
	 * Called whenever a tick occurs.
	 */
	default void tick() {}
	
	/**
	 * By default when rendering the "date over ticks" option,
	 * add a day to represent the next day. This is mostly buggy
	 * if used on day 0 without adding a day. It's just Java
	 * things™...
	 * 
	 * @return a boolean value
	 */
	default boolean shouldAddDayForDateDisplay() {
		return true;
	}
	
	/**
	 * Shows an element if this cycle has an option that the user 
	 * should have quick access to.
	 * 
	 * <p> You should at least be sure that all of the elements
	 * combined do fit on width of 150 and the height of 20.
	 * 
	 * @return a list of elements to create.
	 */
	default Element[] createQuickOptionElements(TimeChangerScreen screen) {
		return null;
	}
	
	/**
	 * Creates properties for use in reads/writes, saves, commands 
	 * and in graphical interfaces such as the Property List.
	 * 
	 * @return a set; should use {@link ImmutableSet} to avoid
	 * writes although none of it really matters as it is not 
	 * written to, but read from
	 */
	default Set<BaseProperty<?, ?>> createProperties() {
		return null;
	}
	
	/**
	 * Writes the current property back to the current cycle that 
	 * handles the current time.
	 * 
	 * @param property the property which was overriden to provide 
	 * the new value. 
	 * 
	 * <p> The property string is obtained by using 
	 * {@link BaseProperty#property()} and is instead used to allow
	 * switch statements to be used for quicker value setting. To
	 * verify that the type (like the property type obtained) is 
	 * correct, perform an {@code instanceof} check on the property 
	 * and then get its property name and apply the value to avoid 
	 * crash problems and it is also quicker that way.
	 * 
	 * @param <B> the base property, usually represents a type, 
	 *        like {@link StringValue} or {@link IntValue}
	 * @param <V> the type used from the property itself, like
	 *        {@link String} or {@link Integer}
	 */
	default void writePropertyValueToCycle(BaseProperty<?, ?> property, PropertyWriterSource writer) { }

	/**
	 * Whenever a world change happens, save the cycle's preferences
	 * to disk so that certain values that constantly update aren't lost
	 * on the next session. However, this only triggers if the world 
	 * previously was not empty as to avoid saving when not needed.
	 * 
	 * @return a boolean value; by default it's set to {@code false}
	 * on most cycles
	 * @implNote Introduced in v0.0.1
	 */
	default boolean saveOnWorldChange() {
		return false;
	}
	
	/**
	 * Rearranges {@link #createProperties()}'s given elements to 
	 * a daylight cycle's own version of the rearrangement. This
	 * helps in not having to do all of the elements on itself.
	 * 
	 * @param entry the entry to which is used to help make the 
	 *        rows of the list
	 * @param elementsPerRow how many elements will there be for 
	 *        each row
	 * 
	 * @return a doubled array of {@link Element} which represents
	 * the following:
	 * <ul>
	 * <li> First array:  the row for the list when creating the row
	 * <li> Second array: the elements to which the row will utilize
	 * </ul>
	 */
	default WidgetConfigInterface<?, ?>[][] rearrangeSectionElements
	(Map.Entry<FancySectionProperty, List<WidgetConfigInterface<?, ?>>> entry, int elementsPerRow) 
	{
		final List<WidgetConfigInterface<?, ?>> elements = entry.getValue();

		final int sectionElements = elements.size();
		final int sectionElementsHalf = MathHelper.ceil((float)sectionElements / (float)elementsPerRow);
		
		WidgetConfigInterface<?, ?>[][] sectionPartsToCreate = 
			new WidgetConfigInterface<?, ?>[sectionElementsHalf][elementsPerRow];
		
		for (int i = 0; i < sectionElements; ++i)
		{
			WidgetConfigInterface<?, ?> entryElement = elements.get(i);
			
			final int rowToPut = i / elementsPerRow;
			final int elementOrder = i % elementsPerRow;
			
			sectionPartsToCreate[rowToPut][elementOrder] = entryElement;
		}
		
		return sectionPartsToCreate;
	}
	
	default void rearrangeCreatedOptionElements(int x, int y, int entryWidth, 
		int entryHeight, List<WidgetConfigInterface<?, ?>> myConfigElements) 
	{
		final int halfWidth = (entryWidth / 2);
		final int xCentered = x + halfWidth;
		final int myElemsSize = myConfigElements.size();
		
		switch (myElemsSize)
		{
			case 1: 
			{
				WidgetConfigInterface<?, ?> elem = myConfigElements.get(0);
				
				if (elem instanceof ClickableWidget) 
				{
					ClickableWidget elemClickable = (ClickableWidget)elem;

					elemClickable.setX(xCentered - (halfWidth - 4)); 
					elemClickable.setY(y + 2);
					elemClickable.setWidth(entryWidth - 10);
					
					if (elemClickable instanceof TextFieldWidget)
					{
						elemClickable.setX(elemClickable.getX() + 1);
						elemClickable.setY(elemClickable.getY() + 1);
						elemClickable.setWidth(entryWidth - 12);
					}
					else
						elemClickable.setWidth(entryWidth - 10);
				}
				
				break;
			}
			case 2: 
			{
				final int entryWidthDiv = halfWidth - 8;
				
				for (int i = 1; i >= 0; --i)
				{
					WidgetConfigInterface<?, ?> elem = myConfigElements.get(i);
					
					if (elem instanceof ClickableWidget) 
					{
						ClickableWidget elemClickable = (ClickableWidget)elem;
						elemClickable.setX(xCentered + 1);
						elemClickable.setY(y + 2);
						
						if (i % 2 == 0)
							elemClickable.setX(elemClickable.getX() - (halfWidth - 4));
						else // 1
							elemClickable.setX(elemClickable.getX() + 2);
						
						if (elemClickable instanceof TextFieldWidget)
						{
							elemClickable.setX(elemClickable.getX() + 1);
							elemClickable.setY(elemClickable.getY() + 1);
							elemClickable.setWidth(entryWidthDiv - 2);
						}
						else
							elemClickable.setWidth(entryWidthDiv);
					}
					
				}
				
				break;
			}
			case 3: 
			{
				final int entryWidthDiv = (entryWidth / myElemsSize) - 8;
				final int entryWidthDivSeparator = entryWidthDiv + 4;
				
				for (int i = myElemsSize - 1; i >= 0; --i)
				{
					WidgetConfigInterface<?, ?> elem = myConfigElements.get(i);
					
					if (elem instanceof ClickableWidget) 
					{
						ClickableWidget elemClickable = (ClickableWidget)elem;
						
						int xOffset = entryWidthDivSeparator * i;
						
						elemClickable.setX(xCentered + xOffset - (int)((float)entryWidthDivSeparator * 1.5f));
						elemClickable.setY(y + 2);
						
						if (elemClickable instanceof TextFieldWidget)
						{
							elemClickable.setX(elemClickable.getX() + 1);
							elemClickable.setY(elemClickable.getY() + 1);
							elemClickable.setWidth(entryWidthDiv - 2);
						}
						else
							elemClickable.setWidth(entryWidthDiv);
					}
					
				}
				
				break;
			}
		}
	}
	
	/**
	 * Used to know which source requested for a write. It is there to ensure that certain 
	 * properties behave the way they should when it comes to user writing the property or 
	 * if it was loaded from disk, which shouldn't write much else on certain cycles.
	 *
	 * @author JuggleStruggle
	 * @implNote Implemented in v0.0.1
	 */
	public enum PropertyWriterSource
	{
		FROM_JSON,
		USER
	}
}
