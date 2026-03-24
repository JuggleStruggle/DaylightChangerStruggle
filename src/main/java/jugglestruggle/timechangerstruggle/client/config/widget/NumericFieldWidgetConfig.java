package jugglestruggle.timechangerstruggle.client.config.widget;

import jugglestruggle.timechangerstruggle.client.widget.WidgetPositionedTooltip;
import jugglestruggle.timechangerstruggle.config.property.BaseNumber;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty.ValueConsumer;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis.PropertyWriterSource;
import jugglestruggle.timechangerstruggle.mixin.client.widget.TextFieldWidgetAccessor;
import jugglestruggle.timechangerstruggle.util.SimpleCharacterVisitor;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;

/**
 * @author JuggleStruggle
 * @implNote Created on 30-Jan-2022, Sunday
 */
public class NumericFieldWidgetConfig<N extends Number> extends TextFieldWidget 
implements WidgetConfigInterface<BaseNumber<N>, N>, WidgetPositionedTooltip
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
		super(textRenderer, 0, 0, width, height, Text.empty());
		
		this.property = property;
		this.isNewTextValid = true;
		
		this.setMaxLength(32);
		this.setChangedListener(null);
		
		this.setText(this.property.get().toString());
		this.initialNumber = this.property.get();
		
		this.setCursorToStart(false);
	}

	@Override
	public void setChangedListener(Consumer<String> changedListener)
	{
		this.textChangedListener = changedListener;
		super.setChangedListener(this::onTextChanged);
	}

	/**
	 * Verifies if this text, based on a set of conditions, passes the requirements
	 * for it to be displayed as a number in this property.
	 * 
	 * @implNote Introduced in v0.0.4
	 */
	protected boolean verifyTextPredicate(String text)
	{
		return text == null || text.isEmpty() || NumericFieldWidgetConfig.isDashOnly(text) || 
			NumericFieldWidgetConfig.canParseString(this.property.getDefaultValue(), text);
	}
	
	@Override
	public void setText(String text)
	{
		if (this.verifyTextPredicate(text))
			super.setText(text);
	}

	// v0.0.4+26.1: Due to removal of predicates dictating 
	// whether the new text shall get through. 
	@Override
	public void write(final String newText)
	{
		final TextFieldWidgetAccessor acc = (TextFieldWidgetAccessor)this;
		final String oldText = this.getText();
		
		int start = Math.min(acc.getSelectionStart(), acc.getSelectionEnd());
		int end = Math.max(acc.getSelectionStart(), acc.getSelectionEnd());
		int maxInsertionLength = acc.getMaxLength() - oldText.length() - (start - end);
		
		if (maxInsertionLength <= 0)
			return;

		String newTextM = StringHelper.stripInvalidChars(newText);
		int insertionLength = newTextM.length();
		
		if (maxInsertionLength < insertionLength) 
		{
			if (Character.isHighSurrogate(newTextM.charAt(maxInsertionLength - 1)))
				maxInsertionLength--;

			newTextM = newTextM.substring(0, maxInsertionLength);
			insertionLength = maxInsertionLength;
		}

		// Reuse newTextM as the new text which now accounts for the previous text 
		// alongside parts that need to be replaced by the new text.
		newTextM = new StringBuilder(oldText).replace(start, end, newTextM).toString();
		
		// If verification fails, return and do nothing.
		if (!this.verifyTextPredicate(newTextM))
			return;
		
		acc.setDirectText(newTextM);
		
		this.setSelectionStart(start + insertionLength);
		this.setSelectionEnd(acc.getSelectionStart());
		acc.onDirectChanged(newTextM);
	}

	// Introduced in v0.0.4+26.1, see #write() for the reason.
	@Override
	public void eraseCharactersTo(final int position) 
	{
		final TextFieldWidgetAccessor acc = (TextFieldWidgetAccessor)this;
		final String oldText = this.getText();
		
		if (oldText.isEmpty())
			return;
		
		if (acc.getSelectionEnd() != acc.getSelectionStart()) {
			this.write(""); return;
		}
		
		int start = Math.min(position, acc.getSelectionStart());
		int end = Math.max(position, acc.getSelectionStart());
		
		if (start == end) 
			return;
		
		String newTextM = new StringBuilder(oldText).delete(start, end).toString();
		
		// If verification fails, return and do nothing.
		if (!this.verifyTextPredicate(newTextM))
			return;
		
		acc.setDirectText(newTextM);
		this.setSelectionStart(start);
		acc.onDirectChanged(newTextM);
		this.setCursor(start, false);
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
		
		return !this.hasMinAndMaxValues() || this.property.isWithinRange();
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
		boolean valid = !(newText == null || newText.isEmpty() || newText.isBlank());
		
		if (valid)
		{
			N parsedNumber = NumericFieldWidgetConfig.isDashOnly(newText) ? this.getZero() : 
				NumericFieldWidgetConfig.parseString(this.property.getDefaultValue(), newText);
			
			if (parsedNumber == null) {
				valid = false;
			}
			else
			{
				final boolean prevNewTextValid = this.isNewTextValid;
				final N previousNumber = this.property.get();
				
				// to avoid the numbers from not being valid despite them being it
				this.isNewTextValid = true; 
				this.property.set(parsedNumber);

				valid = this.isValid();
				
				// just set it back after we are done :)
				this.property.set(previousNumber);
				this.isNewTextValid = prevNewTextValid;
			}
			
			// If valid, actually set the new value into the property as it is
			if (valid)
			{
				ValueConsumer<BaseNumber<N>, N> consumer = this.property.getConsumer();
				
				if (consumer != null)
					consumer.consume(this.property, parsedNumber, PropertyWriterSource.USER);
				
				this.property.set(parsedNumber);
			}
		}
		
		this.isNewTextValid = valid;
		this.setEditableColor(valid ? DEFAULT_EDITABLE_COLOR : 0xFFE06060);
		
		if (this.textChangedListener != null)
			this.textChangedListener.accept(newText);
	}

	/**
	 * Extracted function for the sake of knowing if the {@link #property} has both a
	 * minimum and a maximum applied.
	 * 
	 * @implNote Introduced in v0.0.4
	 */
	public boolean hasMinAndMaxValues() {
		return this.property.getMin() != null && this.property.getMax() != null;
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

	@Override
	protected MutableText getNarrationMessage()
	{
		MutableText mt = SimpleCharacterVisitor.asMutableText(0, -1, this.compiledTooltipText);
		return Text.translatable("gui.narrate.editBox", mt, super.getText());
	}
	
	
	
	
	
	
	protected static final boolean isDashOnly(String val) {
		return val != null && val.length() == 1 && val.equals("-");
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
			if (n instanceof Integer)
				return (N)(Integer)Integer.parseInt(val);
			else if (n instanceof Long)
				return (N)(Long)Long.parseLong(val);
			else if (n instanceof Double)
				return (N)(Double)Double.parseDouble(val);
			else if (n instanceof Float)
				return (N)(Float)Float.parseFloat(val);
			else if (n instanceof Byte)
				return (N)(Byte)Byte.parseByte(val);
		}
		catch (NumberFormatException nfe) {}
		
		return null;
	}
}
