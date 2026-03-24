package jugglestruggle.timechangerstruggle.daynight;

import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;

/**
 * @author JuggleStruggle
 * @implNote Created on 26-Jan-2022, Wednesday
 */
public enum DayNightGetterType
{
	DEFAULT,
	LUNAR;

	/**
	 * Gets the corresponding DCS clock from the cycle based on the environment attribute.
	 * Everything by default sticks with {@link #DEFAULT} besides Lunar-based variants.
	 * 
	 * @param a the attribute to check against
	 * @return either of the two daylight cycle target type
	 * 
	 * @implNote Introduced in v0.0.4
	 */
	public static DayNightGetterType getCorrespondingFromEnvAttr(EnvironmentAttribute<?> a)
	{
		return (a == EnvironmentAttributes.MOON_ANGLE_VISUAL || a == EnvironmentAttributes.MOON_PHASE_VISUAL) ?
			DayNightGetterType.LUNAR : DayNightGetterType.DEFAULT;
	}
}
