package jugglestruggle.timechangerstruggle.util;

import java.util.Locale;

import net.minecraft.text.Text;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 28-Jan-2022, Friday
 */
public enum EasingType implements InterchangeableFunction<EasingType, String>
{
	IN,
	OUT,
	BETWEEN;
	
	@Override
	public EasingType applyLeft(String value) {
		return EasingType.parseFromString(value);
	}
	@Override
	public String applyRight(EasingType value) {
		return value.toValueString();
	}
	

	public final String toValueString() {
		return this.toString().toLowerCase(Locale.ROOT);
	}
	public final Text getFormattedText() {
		return Text.translatable(String.format("jugglestruggle.tcs.easingtype.%1$s", this.toValueString()), new Object[0]);
	}
	
	
	public static final EasingType parseFromString(String name)
	{
		if (name == null || name.isEmpty() || name.isBlank())
			return null;
		
		return switch (name.toLowerCase(Locale.ROOT))
		{
			case "in" -> EasingType.IN;
			case "out" -> EasingType.OUT;
			case "between" -> EasingType.BETWEEN;
			
			default -> null;
		};
	}
}
