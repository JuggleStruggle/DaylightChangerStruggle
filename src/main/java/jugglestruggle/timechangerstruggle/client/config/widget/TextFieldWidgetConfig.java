package jugglestruggle.timechangerstruggle.client.config.widget;

import jugglestruggle.timechangerstruggle.client.widget.PositionedTooltip;
import jugglestruggle.timechangerstruggle.config.property.StringValue;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty.ValueConsumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 30-Jan-2022, Sunday
 */
@Environment(EnvType.CLIENT)
public class TextFieldWidgetConfig extends TextFieldWidget 
implements WidgetConfigInterface<StringValue, String>, PositionedTooltip
{
	String initialText;
	final StringValue property;
	protected boolean allowEmptyText;

	private int tooltipWidth;
	private int tooltipHeight;
	private List<OrderedText> compiledTooltipText;
	private Consumer<String> textChangedListener;
	
	public TextFieldWidgetConfig(TextRenderer textRenderer, int width, int height, 
		StringValue property, boolean allowEmptyText) 
	{
		super(textRenderer, 0, 0, width, height, Text.empty());
		
		this.property = property;
		this.allowEmptyText = allowEmptyText;
		this.initialText = property.get();
		
		this.setText(this.initialText);
		this.setChangedListener(null);
		
		this.setCursorToStart();
	}

	@Override
	public boolean isValid() {
		return this.allowEmptyText ? true : !this.getText().isBlank();
	}
	
	@Override
	public StringValue getProperty() {
		return this.property;
	}
	@Override
	public String getInitialValue() {
		return this.initialText;
	}
	@Override
	public void setInitialValue(String value) {
		this.initialText = value;
	}
	@Override
	public boolean isDefaultValue() {
		return this.property.getDefaultValue().equals(this.property.get());
	}
	@Override
	public void forceSetWidgetValueToDefault(boolean justInitial)
	{
		if (justInitial) {
			super.setText((this.initialText == null) ? "" : this.initialText);
		} 
		else 
		{
			String def = this.property.getDefaultValue();
			super.setText((def == null) ? "" : def);
		}
	}
	@Override
	public void setPropertyValueToDefault(boolean justInitial)
	{
		if (justInitial) {
			this.property.set((this.initialText == null) ? "" : this.initialText);
		} 
		else 
		{
			String def = this.property.getDefaultValue();
			this.property.set((def == null) ? "" : def);
		}
	}

	@Override
	public void setChangedListener(Consumer<String> changedListener)
	{
		this.textChangedListener = changedListener;
		super.setChangedListener(this::onTextChanged);
	}
	private void onTextChanged(String newText)
	{
		ValueConsumer<StringValue, String> consumer = this.property.getConsumer();
		
		if (consumer != null) {
			consumer.consume(this.property, newText);
		}
		
		this.property.set(newText);
		
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
}
