package jugglestruggle.timechangerstruggle.client.config.widget;

import jugglestruggle.timechangerstruggle.client.widget.PositionedTooltip;
import jugglestruggle.timechangerstruggle.config.property.BaseNumber;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty.ValueConsumer;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 30-Jan-2022, Sunday
 */
public class NumericFieldWidgetConfig<N extends Number> extends TextFieldWidget 
implements WidgetConfigInterface<BaseNumber<N>, N>, PositionedTooltip
{
	protected final BaseNumber<N> property;
	protected N initialNumber;

	private int tooltipWidth;
	private int tooltipHeight;
	private List<OrderedText> compiledTooltipText;
	
	protected Consumer<String> textChangedListener;
	
	private boolean isNewTextValid;
	
	public NumericFieldWidgetConfig(TextRenderer textRenderer, int width, int height, BaseNumber<N> property) 
	{
		super(textRenderer, 18, 18, width, height, Text.empty());
		
		this.property = property;
		this.isNewTextValid = true;
		
		this.setChangedListener(null);
		this.setTextPredicate(null);
		
		this.setText(this.property.get().toString());
		this.initialNumber = this.property.get();
		
		this.setCursorToStart();
	}

	@Override
	public void setChangedListener(Consumer<String> changedListener)
	{
		this.textChangedListener = changedListener;
		super.setChangedListener(this::onTextChanged);
	}
	/**
	 * Note: Numeric Field Widget snatches the setTextPredicate as 
	 * the class itself only uses numbers as predicate.
	 *
	 * @param textPredicate useless if provided; this method is used
	 * to create the predicates for an specific number type and to
	 * avoid problems.
	 */
	@Override
	public void setTextPredicate(Predicate<String> textPredicate)
	{
		Predicate<String> theNextPredicate = (text) -> 
		{
			if (!text.isBlank()) {
				return NumericFieldWidgetConfig.canParseString(this.property.getDefaultValue(), text);
			}
			
			return true;
		};
		
		super.setTextPredicate(theNextPredicate);
	}
	
	
	
	

	@Override
	public BaseNumber<N> getProperty() {
		return this.property;
	}
	
	@Override
	public boolean isValid()
	{
		if (!this.isNewTextValid || this.property.get() == null)
			return false;
		
		if (this.property.getMin() == null || this.property.getMax() == null)
			return true;
		
		return this.property.isWithinRange();
	}
	@Override
	public N getInitialValue() {
		return this.initialNumber;
	}
	@Override
	public void setInitialValue(N value) {
		this.initialNumber = value;
	}
	@Override
	public void forceSetWidgetValueToDefault(boolean justInitial)
	{
		if (justInitial) {
			this.setText("" + ((this.initialNumber == null) ? "0" : this.initialNumber));
		} 
		else
		{
			final N defaultNumber = this.property.getDefaultValue();
			this.setText("" + ((defaultNumber == null) ? "0" : defaultNumber));
		}
	}
	@Override
	public void setPropertyValueToDefault(boolean justInitial)
	{
		if (justInitial) {
			this.property.set((this.initialNumber == null) ? this.getZero() : this.initialNumber);
		} 
		else
		{
			final N defaultNumber = this.property.getDefaultValue();
			this.property.set((defaultNumber == null) ? this.getZero() : defaultNumber);
		}
	}
	@Override
	public boolean isDefaultValue() {
		return this.property.get().equals(this.property.getDefaultValue());
	}
	
	
	
	
	
	
	private N getZero() {
		return NumericFieldWidgetConfig.parseString(this.property.getDefaultValue(), "0");
	}
	private void onTextChanged(String newText)
	{
		boolean valid = !(newText.isEmpty() || newText.isBlank());
		
		if (valid)
		{
			N parsedNumber = NumericFieldWidgetConfig.parseString(this.property.getDefaultValue(), newText);
			
			if (parsedNumber == null) {
				valid = false;
			}
			else
			{
				boolean tempNewTextValid = this.isNewTextValid;
				N previousNumber = this.property.get();
				
				// to avoid the numbers from not being valid despite them being it
				this.isNewTextValid = true; 
				this.property.set(parsedNumber);

				valid = this.isValid();
				
				// just set it back after we are done :)
				this.property.set(previousNumber);
				this.isNewTextValid = tempNewTextValid;
			}
			
			if (valid)
			{
				ValueConsumer<BaseNumber<N>, N> consumer = this.property.getConsumer();
				
				if (consumer != null) {
					consumer.consume(this.property, parsedNumber);
				}
				
				this.property.set(parsedNumber);
			}
		}
		
		this.isNewTextValid = valid;
		this.setEditableColor(valid ? DEFAULT_EDITABLE_COLOR : 0xE06060);
		
		if (this.textChangedListener != null) {
			this.textChangedListener.accept(newText);
		}
	}
	
	
	
	
	

	@Override
	public int getTooltipWidth() {
		return this.tooltipWidth;
	}
	@Override
	public int getTooltipHeight() {
		return this.tooltipHeight;
	}
	@Override
	public void setTooltipWidth(int width) {
		this.tooltipWidth = width;
	}
	@Override
	public void setTooltipHeight(int height) {
		this.tooltipHeight = height;
	}
	
	@Override
	public List<OrderedText> getOrderedTooltip() {
		return this.compiledTooltipText;
	}
	@Override
	public void setOrderedTooltip(List<OrderedText> textToSet) {
		this.compiledTooltipText = textToSet;
	}
	
	
	
	
	
	
	protected static final boolean canParseString(Number n, String val) {
		return NumericFieldWidgetConfig.parseString(n, val) != null;
	}
	
	@SuppressWarnings("unchecked")
	protected static final <N> N parseString(N n, String val) 
	{
		if (n == null || val == null)
			return null;
		
		try
		{
			if (n instanceof Integer) {
				return (N)(Integer)Integer.parseInt(val);
			} else if (n instanceof Long) {
				return (N)(Long)Long.parseLong(val);
			} else if (n instanceof Double) {
				return (N)(Double)Double.parseDouble(val);
			} else if (n instanceof Float) {
				return (N)(Float)Float.parseFloat(val);
			} else if (n instanceof Byte) {
				return (N)(Byte)Byte.parseByte(val);
			}
		}
		catch (NumberFormatException nfe) {}
		
		return null;
	}
}
