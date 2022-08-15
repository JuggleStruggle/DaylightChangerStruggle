package jugglestruggle.timechangerstruggle.util;

import java.util.Locale;

import net.minecraft.text.Text;

/**
 * An easings used for times that move; allows further customization in
 * how the daylight cycle should interact. This is not exclusive for one
 * thing, as it is used by the Chroma RNG and might be expanded in other
 * areas.
 * 
 * <p> Easings code repurposed and taken from https://easings.net/ with
 * some that are original from JuggleStruggle.
 *
 * @author JuggleStruggle
 * @implNote Created on 28-Jan-2022, Friday
 */
public enum Easings implements InterchangeableFunction<Easings, String>
{
	LINEAR { // JuggleStruggle
		@Override
		public double value(EasingType type, double delta) {
			return delta;
		}
	},
	FLOOR { // JuggleStruggle
		@Override
		public double value(EasingType type, double delta) {
			return Math.floor(delta);
		}
	},
	CEILING { // JuggleStruggle
		@Override
		public double value(EasingType type, double delta) {
			return Math.ceil(delta);
		}
	},
	
	// 
	SINE
	{
		@Override
		public double value(EasingType type, double delta)
		{
//			delta *= Math.PI;
			delta = delta * Math.PI;
			
			return switch (type)
			{
				case IN -> 
					1.0 - Math.cos(delta / 2.0);
				case OUT -> 
					Math.sin(delta / 2.0);
				case BETWEEN -> 
					-(Math.cos(delta / 2.0) - 1.0) / 2.0;
			};
		}
	},
	QUAD 
	{
		@Override
		public double value(EasingType type, double delta)
		{
			return switch (type)
			{
				case IN -> 
					delta * delta;
				case OUT -> 
					1.0 - (1.0 - delta) * (1.0 - delta);
				case BETWEEN -> (delta < 0.5) ? 
					(2.0 * delta * delta) : 
					(1.0 - Math.pow(-2.0 * delta + 2.0, 2.0) / 2.0);
			};
		}
	},
	CUBIC 
	{
		@Override
		public double value(EasingType type, double delta)
		{
			return switch (type)
			{
				case IN -> 
					delta * delta * delta;
				case OUT -> 
					1.0 - Math.pow(1.0 - delta, 3.0);
				case BETWEEN -> (delta < 0.5) ? 
					(4.0 * delta * delta * delta) : 
					(1.0 - Math.pow(-2.0 * delta + 2.0, 3.0) / 2.0);
			};
		}
	},
	QUART
	{
		@Override
		public double value(EasingType type, double delta)
		{
			return switch (type)
			{
				case IN -> 
					delta * delta * delta * delta;
				case OUT -> 
					1.0 - Math.pow(1.0 - delta, 4.0);
				case BETWEEN -> (delta < 0.5) ? 
					(8.0 * delta * delta * delta * delta) : 
					(1.0 - Math.pow(-2.0 * delta + 2.0, 4.0) / 2.0);
			};
		}
	},
	QUINT 
	{
		@Override
		public double value(EasingType type, double delta)
		{
			return switch (type)
			{
				case IN -> 
					delta * delta * delta * delta * delta;
				case OUT -> 
					1.0 - Math.pow(1.0 - delta, 5.0);
				case BETWEEN -> (delta < 0.5) ? 
					(16.0 * delta * delta * delta * delta * delta) : 
					(1.0 - Math.pow(-2.0 * delta + 2.0, 5.0) / 2.0);
			};
		}
	},
	EXPO 
	{
		@Override
		public double value(EasingType type, double delta)
		{
			return switch (type)
			{
				case IN -> (delta == 0.0) ? 
					0.0 : Math.pow(2.0, 10.0 * delta - 10.0);
				case OUT -> (delta == 1.0) ?
					1.0 : 1.0 - Math.pow(2.0, -10.0 * delta);
				case BETWEEN -> (delta == 0.0) ? 0.0 :
					(
						(delta == 1.0) ? 1.0 : 
						(
							(delta < 0.5) ? 
							(Math.pow(2.0, 20.0 * delta - 10.0) / 2.0) :
							((2.0 - Math.pow(2.0, -20.0 * delta + 10.0)) / 2.0)
						)
					);
			};
		}
	},
	CIRCLE
	{
		@Override
		public double value(EasingType type, double delta)
		{
			return switch (type)
			{
				case IN -> 
					1.0 - Math.sqrt(1.0 - Math.pow(delta, 2.0));
				case OUT -> 
					Math.sqrt(1.0 - Math.pow(delta - 1.0, 2.0));
				case BETWEEN -> (delta < 0.5) ?
					(1.0 - Math.sqrt(1.0 - Math.pow(2.0 * delta, 2.0)) / 2.0) :
					((Math.sqrt(1.0 - Math.pow(-2.0 * delta + 2.0, 2.0)) + 1.0) / 2.0);
			};
		}
	},
	BACK
	{
		@Override
		public double value(EasingType type, double delta)
		{
			final double ebb = 1.70158;
			final double ebbP1 = ebb + 1.0;
			final double ebbP2 = ebb * 1.525;
			
			return switch (type)
			{
				case IN -> 
					ebbP1 * delta * delta * delta - ebb * delta * delta;
				case OUT -> 
					1.0 + ebbP1 * Math.pow(delta - 1.0, 3.0) + ebb * Math.pow(delta - 1.0, 2.0);
				case BETWEEN -> (delta < 0.5) ?
					((Math.pow(2.0 * delta, 2.0) * ((ebbP2 + 1.0) * 2.0 * delta - ebbP2)) / 2.0) :
					((Math.pow(2.0 * delta - 2.0, 2.0) * ((ebbP2 + 1.0) * (delta * 2.0 - 2.0) + ebbP2) + 2.0) / 2.0);
			};
		}
	},
	ELASTIC 
	{
		@Override
		public double value(EasingType type, double delta)
		{
			final double ebb  = (2.0 * Math.PI) / 3.0;
			final double ebbT = (2.0 * Math.PI) / 4.5;
			
			return switch (type)
			{
				case IN -> (delta == 0) ? 0.0 :
					(
						(delta == 1) ? 1.0 :
						(-Math.pow(2.0, 10.0 * delta - 10.0) * Math.sin((delta * 10.0 - 10.75) * ebb))
					);
				case OUT -> (delta == 0) ? 0.0 :
					(
						(delta == 1) ? 1.0 :
						(Math.pow(2.0, -10.0 * delta) * Math.sin((delta * 10.0 - 0.75) * ebb) + 1.0)
					);
				case BETWEEN -> (delta == 0) ? 0.0 :
					(
						(delta == 1) ? 1.0 :
						(
							(delta < 0.5) ?
							(-(Math.pow(2.0,  20.0 * delta - 10.0) * Math.sin((20.0 * delta - 11.125) * ebbT)) / 2.0) :
							( (Math.pow(2.0, -20.0 * delta + 10.0) * Math.sin((20.0 * delta - 11.125) * ebbT)) / 2.0 + 1.0)
						)
					);
			};
		}
	},
	BOUNCE 
	{
		@Override
		public double value(EasingType type, double delta)
		{
			return switch (type)
			{
				case IN -> 
					1.0 - this.easeOutBounce(1.0 - delta);
				case OUT -> 
					this.easeOutBounce(delta);
				case BETWEEN -> (delta < 0.5) ?
					((1.0 - this.easeOutBounce(1.0 - 2.0 * delta)) / 2.0) :
					((1.0 + this.easeOutBounce(2.0 * delta - 1.0)) / 2.0);
			};
		}
		
		private double easeOutBounce(double delta)
		{
			final double ebbN = 7.5625;
			final double ebbD = 2.75;
			
			if (delta < 1.0 / ebbD)
				return ebbN * delta * delta;
			else if (delta < 2.0 / ebbD)
				return ebbN * (delta -= 1.5 / ebbD) * delta + 0.75;
			else if (delta < 2.5 / ebbD)
				return ebbN * (delta -= 2.25 / ebbD) * delta + 0.9375;
			else
				return ebbN * (delta -= 2.625 / ebbD) * delta + 0.984375;
		}
	}
	;
	
	/**
	 * Gets the value ranging from 0.0 or 1.0 (depends on the easing 
	 * type as some can return lower or higher than presribed) depending 
	 * on the parameter {@code animationDelta} value.
	 * 
	 * @param type the easing type to get the return value from
	 * @param animationDelta the delta ranging from 0.0 to 1.0
	 * 
	 * @return a value from the easing type
	 */
	public abstract double value(EasingType type, double animationDelta);
	
	public final double easingInValue(double delta) {
		return this.value(EasingType.IN, delta);
	}
	public final double easingOutValue(double delta) {
		return this.value(EasingType.OUT, delta);
	}
	public final double easingBetweenValue(double delta) {
		return this.value(EasingType.BETWEEN, delta);
	}

	public final String toValueString() {
		return this.toString().toLowerCase(Locale.ROOT);
	}
	public final Text getFormattedText() {
		return Text.translatable(String.format("jugglestruggle.tcs.easings.%1$s", this.toValueString()), new Object[0]);
	}
	
	/**
	 * This is used whenever there is a random function that would 
	 * like to know if this easing is OK to be used in randoms.
	 * 
	 * <p> In most cases, this returns {@code true} by default.
	 * 
	 * @return a boolean value
	 */
	public boolean canBeRandomlyUsed() {
		return true;
	}
	
	@Override
	public Easings applyLeft(String value) {
		return Easings.parseFromString(value);
	}
	@Override
	public String applyRight(Easings value) {
		return value.toValueString();
	}
	
	
	
	public static final Easings parseFromString(String name)
	{
		if (name == null || name.isEmpty() || name.isBlank())
			return null;
		
		return switch (name.toLowerCase(Locale.ROOT))
		{
			case "linear" -> Easings.LINEAR;
			case "floor" -> Easings.FLOOR;
			case "ceiling" -> Easings.CEILING;
			case "sine" -> Easings.SINE;
			case "quad" -> Easings.QUAD;
			case "cubic" -> Easings.CUBIC;
			case "quart" -> Easings.QUART;
			case "quint" -> Easings.QUINT;
			case "expo" -> Easings.EXPO;
			case "circle" -> Easings.CIRCLE;
			case "back" -> Easings.BACK;
			case "elastic" -> Easings.ELASTIC;
			case "bounce" -> Easings.BOUNCE;
			
			default -> null;
		};
	}
}
