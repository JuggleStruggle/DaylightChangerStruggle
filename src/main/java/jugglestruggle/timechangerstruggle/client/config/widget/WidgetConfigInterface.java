package jugglestruggle.timechangerstruggle.client.config.widget;

import jugglestruggle.timechangerstruggle.config.property.BaseProperty;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.util.OrderableTooltip;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 30-Jan-2022, Sunday
 */
public interface WidgetConfigInterface<B extends BaseProperty<B, V>, V> 
extends Element, Drawable, Selectable, OrderableTooltip
{
	/**
	 * Gets whether the property set in the widget is valid 
	 * to use or save. 
	 * 
	 * @return a boolean value
	 */
	boolean isValid();
	
	/**
	 * Gets the modifying property.
	 * @return the property
	 */
	B getProperty();
	
	/**
	 * Gets the starting value when this config widget was 
	 * either created or replaced with a new value.
	 * 
	 * @return a value which represents the property's type
	 */
	V getInitialValue();
	/**
	 * Sets the starting value; usually this is done during
	 * a write (like a save to the cycle type, not part if
	 * there is there is an automatic apply).
	 * 
	 * @param value the new value to apply into the widget
	 */
	void setInitialValue(V value);
	
	/**
	 * Forces the current value of the widget to be reset back
	 * to either initial or default of the property.
	 * 
	 * @param justInitial if set to {@code true}, use the
	 * initial value cached by the widget; otherwise use
	 * the property's default value
	 */
	void forceSetWidgetValueToDefault(boolean justInitial);
	/**
	 * Sets the current value of the property to be reset back
	 * to either initial or property default.
	 * 
	 * @param justInitial if set to {@code true}, use the
	 * initial value cached by the widget; otherwise use
	 * the property's default value
	 */
	void setPropertyValueToDefault(boolean justInitial);
	
	/**
	 * Returns if the value within the {@link #getProperty()}'s
	 * {@linkplain BaseProperty#getDefaultValue() getDefaultValue()} 
	 * is the same as the current value.
	 * 
	 * @return {@code true} if it is; otherwise it is 
	 * considered modified
	 */
	boolean isDefaultValue();
	
	/**
	 * Used to keep track of the JSON read/write.
	 * @return a string
	 */
	default String getPropertyName() {
		return this.getProperty().property();
	}
}
