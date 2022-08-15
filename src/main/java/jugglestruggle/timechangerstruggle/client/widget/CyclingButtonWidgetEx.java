package jugglestruggle.timechangerstruggle.client.widget;

import jugglestruggle.timechangerstruggle.client.config.widget.CyclingWidgetConfig;
import jugglestruggle.timechangerstruggle.mixin.client.widget.CyclingButtonWidgetBuilderAccessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.function.Function;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import net.minecraft.screen.ScreenTexts;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.SimpleOption.TooltipFactory;
import net.minecraft.client.util.math.MatrixStack;

import com.google.common.collect.ImmutableList;

/**
 * See {@link CyclingWidgetConfig}'s description.
 * 
 * <p> This is a copy of CyclingWidgetConfig which has removed certain
 * config/property-related settings for general use
 *
 * @author JuggleStruggle
 * @implNote Created on 13-Feb-2022, Sunday
 */
@Environment(EnvType.CLIENT)
public class CyclingButtonWidgetEx<T> extends CyclingButtonWidget<T> 
implements SelfWidgetRendererInheritor<CyclingButtonWidgetEx<T>>
{
	private final SelfWidgetRender<CyclingButtonWidgetEx<T>> renderer;
//	private T initial;
	
	protected CyclingButtonWidgetEx(int width, int height, Text message, Text optionText, 
		int index, T value, Values<T> values, Function<T, Text> valueToText,
		Function<CyclingButtonWidget<T>, MutableText> narrationMessageFactory, 
		UpdateCallback<T> callback, TooltipFactory<T> tooltipFactory, boolean optionTextOmitted)
	{
		super(0, 0, width, height, message, optionText, 
			index, value, values, valueToText, 
			narrationMessageFactory, callback,
			tooltipFactory, optionTextOmitted);
		
//		this.initial = value;
		this.renderer = new SelfWidgetRender<>(this, null);
	}

	
	
	
	@Override
	public SelfWidgetRender<CyclingButtonWidgetEx<T>> getWidgetRenderer() {
		return this.renderer;
	}
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderer.renderButton(matrices, mouseX, mouseY, delta);
	}
	
	
	
	
	
	
	public static WidgetBuilder<Boolean> booleanCycle(boolean initial, Text trueText, Text falseText)
	{
		Function<Boolean, Text> valueToText;
		
		final boolean trueTextIsNull = trueText == null;
		final boolean falseTextIsNull = falseText == null;
		
		if (trueTextIsNull && falseTextIsNull)
			valueToText = state -> { return Text.empty(); };
		else if (trueTextIsNull)
			valueToText = state -> { return falseText; };
		else if (falseTextIsNull)
			valueToText = state -> { return trueText; };
		else
			valueToText = state -> { return state ? trueText : falseText; };
		
		WidgetBuilder<Boolean> wcbb = new WidgetBuilder<>(valueToText);
		
		wcbb.values(ImmutableList.of(true, false));
		wcbb.initially(initial);
		
		return wcbb;
	}
	
	
	
	
	

	public static class WidgetBuilder<V> extends CyclingButtonWidget.Builder<V>
	{
		public WidgetBuilder(Function<V, Text> valueToText) {
			super(valueToText); 
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Builder<V> initially(V value)
		{
			final CyclingButtonWidgetBuilderAccessor<V> accessor = 
				(CyclingButtonWidgetBuilderAccessor<V>)this;
			
			accessor.setValue(value);
			
			int valueIndex = accessor.values().getDefaults().indexOf(value);
			
			// means that it doesn't exist
			if (valueIndex != -1)
				accessor.setInitialIndex(valueIndex);
				
			return this;
		}
		
		public CyclingButtonWidgetEx<V> build(int width, int height, Text optionText) {
			return this.build(width, height, optionText, (b, v) -> {});
		}
		public CyclingButtonWidgetEx<V> build(int width, int height, Text optionText, UpdateCallback<V> callback)
		{
			@SuppressWarnings("unchecked")
			final CyclingButtonWidgetBuilderAccessor<V> accessor = 
			(CyclingButtonWidgetBuilderAccessor<V>)this;
			
			List<V> defaults = accessor.values().getDefaults();
			
			V startingValue = accessor.getValue();
			startingValue = startingValue == null ? defaults.get(accessor.getInitialIndex()) : startingValue;
			
			Text messageText = accessor.getValueToText().apply(startingValue);
			
			if (!accessor.omitOptionText())
				messageText = ScreenTexts.composeGenericOptionText(optionText, messageText);
			
			return new CyclingButtonWidgetEx<>(width, height, messageText, optionText, 
				accessor.getInitialIndex(), startingValue, accessor.values(), accessor.getValueToText(), 
				accessor.getNarrationMessageFactory(), callback, accessor.getTooltipFactory(), accessor.omitOptionText());
		}
		
		@Override
		@Deprecated
		public CyclingButtonWidget<V> build(int x, int y, int width, int height, Text optionText) {
			return null;
		}
		@Override
		@Deprecated
		public CyclingButtonWidget<V> build(int x, int y, int width, int height, Text optionText, UpdateCallback<V> callback) {
			return null;
		}
	}
}

