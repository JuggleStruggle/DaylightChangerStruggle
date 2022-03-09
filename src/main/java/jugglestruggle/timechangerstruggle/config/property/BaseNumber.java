package jugglestruggle.timechangerstruggle.config.property;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.client.config.widget.NumericFieldWidgetConfig;
import jugglestruggle.timechangerstruggle.client.config.widget.WidgetConfigInterface;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;

/**
 * A base number type is a type that stores numbers alone as its value
 * decider.
 * 
 * <p> This is directly taken from my own legit client (up to you to 
 * believe) and repurposed for this mod.
 * 
 * @author JuggleStruggle
 * @implNote 23-11-2021, Tuesday
 */
@Environment(EnvType.CLIENT)
public abstract class BaseNumber<N extends Number> extends BaseProperty<BaseNumber<N>, N>
{
	/** Hard minimum; {@code null} to avoid checking. */
	protected N min;
	/** Hard maximum; {@code null} to avoid checking. */
	protected N max;
	
	/**
	 * Soft Min means that while this number gets shown it will
	 * be used instead of hard {@link #min} if there is any. However, 
	 * if this is {@code null} then that will be used instead.
	 */
	protected N softMin;
	/**
	 * Soft Max means that while this number gets shown it will
	 * be used instead of hard {@link #max} if there is any. However, 
	 * if this is {@code null} then that will be used instead.
	 */
	protected N softMax;
	
	/**
	 * Initializes a constructor with the default values, minimum, 
	 * maximum and soft minimum / maximum initialized.
	 * 
	 * @param propertyName the name of the property when attempting 
	 *                      to retrieve or write
	 * @param defaultValue the default value and value to apply
	 * @param min the minimum value to set (also applies softMin)
	 * @param max the maximum value to set (also applies softMax) 
	 */
	protected BaseNumber(String propertyName, N defaultValue, N min, N max)
	{
		super(propertyName, defaultValue);
		
		this.min = min;
		this.max = max;
		
		this.softMin = max;
		this.softMax = max;
	}

	@Override
	public N get() {
		return super.value;
	}
	@Override
	public void set(N value) {
		super.value = value;
	}
	
//	public boolean isWithinRange() {
//		return this.get() >= this.getMin() && this.get() <= this.getMax();
//	}
	public abstract boolean isWithinRange();
	public abstract N parseStringNumber(String toParse);
	
	public N getMin() {
		return this.min;
	}
	public N getMax() {
		return this.max;
	}
	public N getSoftMin() {
		return this.softMin;
	}
	public N getSoftMax() {
		return this.softMax;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void readFromJson(JsonElement elem)
	{
		if (!elem.isJsonPrimitive())
			return;
		
		JsonPrimitive prim = elem.getAsJsonPrimitive();
		
		if (prim.isNumber())
		{
			Number n = prim.getAsNumber();
			
			boolean expectedNumber = n.getClass().equals(this.getDefaultValue().getClass());
			
			if (!expectedNumber && (prim.isString() || n instanceof LazilyParsedNumber)) 
			{
				if (n instanceof LazilyParsedNumber)
					n = this.parseStringNumber(n.toString());
				else
					n = this.parseStringNumber(prim.getAsString());
				
				expectedNumber = true;
			}
			
			if (expectedNumber) {
				this.set((N)n);
			}
		}
	}
	@Override
	public JsonElement writeToJson()
	{
		N value = this.get();
		
		return new JsonPrimitive(value == null ? 
			this.getDefaultValue() : value);
	}
	
	@Override
	public WidgetConfigInterface<BaseNumber<N>, N> createConfigElement
	(TimeChangerScreen screen, FancySectionProperty owningSection)
	{
		NumericFieldWidgetConfig<N> n = new NumericFieldWidgetConfig<>
		(screen.getTextRenderer(), 18, 18, this);
		
		StringValue.onCreateConfigElementAddTooltips(this, n, screen, owningSection);		

		return n;
	}
}
