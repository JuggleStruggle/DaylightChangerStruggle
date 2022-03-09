package jugglestruggle.timechangerstruggle.daynight;

import jugglestruggle.timechangerstruggle.client.Commands;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;

import net.minecraft.text.Text;

/**
 * A cycle builder; this name is rather misleading as it is
 * only primarily used on {@link #create()} and the rest
 * is mostly metadata information.
 *
 * @author JuggleStruggle
 * @implNote Created on 27-Jan-2022, Thursday
 */
public interface DayNightCycleBuilder
{
	/**
	 * Creates the controller for the daylight cycle.
	 * @return a daylight cycle type.
	 */
	DayNightCycleBasis create();

	/**
	 * Gets the key name which is used on the configuration,
	 * commands and on other places.
	 * 
	 * @return a String value
	 */
	String getKeyName();
	
	/**
	 * Gets the name of the cycle which is translatable; 
	 * this is not necessary as some cycles do not implement 
	 * this.
	 * 
	 * @return the name of the cycle
	 */
	Text getTranslatableName();

	/**
	 * Gets a short description of the cycle. Used in both
	 * the {@linkplain TimeChangerScreen screen} and in
	 * {@linkplain Commands commands} when typing 
	 * {@code cycle}.
	 * 
	 * @return a description of the cycle
	 */
	Text getTranslatableDescription();
	
	/**
	 * Used to determine if certain option-related elements 
	 * should be enabled; not really representative of the 
	 * actual properties the cycle might have as it might 
	 * not have it.
	 * 
	 * @return a boolean value
	 */
	default boolean hasOptionsToEdit() {
		return false;
	}
	/**
	 * Used to determine if the cycle in question uses 
	 * dynamic properties meaning that while the user
	 * makes X change results in either adding or removing
	 * a certain property.
	 * 
	 * @return a boolean value
	 */
	default boolean hasDynamicOptions() {
		return false;
	}
}
