package jugglestruggle.timechangerstruggle.client.widget;

import jugglestruggle.timechangerstruggle.mixin.client.widget.CyclingButtonWidgetBuilderAccessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.function.Function;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.SimpleOption.TooltipFactory;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import com.google.common.collect.ImmutableList;

/**
 * @author JuggleStruggle
 * @implNote Created on 13-Feb-2022, Sunday
 */
@Environment(EnvType.CLIENT)
public class CyclingButtonWidgetEx<T> extends CyclingButtonWidget<T> 
implements SelfWidgetRendererInheritor<CyclingButtonWidgetEx<T>>, WidgetOrderedTooltip
{
	private final SelfWidgetRender<CyclingButtonWidgetEx<T>> renderer;
	private final CyclingButtonWidgetEx.TooltipFactoryEx<T> tooltipFactoryEx;
	private List<OrderedText> cachedTooltipText;
	
	protected CyclingButtonWidgetEx(int width, int height, Text message, Text optionText, 
		int index, T value, Values<T> values, Function<T, Text> valueToText,
		Function<CyclingButtonWidget<T>, MutableText> narrationMessageFactory, 
		UpdateCallback<T> callback, TooltipFactoryEx<T> tooltipFactory, boolean optionTextOmitted)
	{
		super(0, 0, width, height, message, optionText, index, value, values, valueToText, 
			narrationMessageFactory, callback, null, optionTextOmitted);
		
		this.renderer = new SelfWidgetRender<>(this, null);
		this.tooltipFactoryEx = tooltipFactory;
		
		this.refreshTooltip();
	}
	
	@Override
	protected void refreshTooltip()
	{
		// Return if the factory is null as this will be called before we even get the chance to 
		// set the tooltip factory (due to the constructor's super call requirement and that calling
		// this method).
		if (this.tooltipFactoryEx == null)
			return;
		
		this.cachedTooltipText = this.tooltipFactoryEx.apply(this.getValue());
	}
	
	@Override
	public List<OrderedText> getOrderedTooltip() {
		return this.cachedTooltipText;
	}

	
	
	
	@Override
	public SelfWidgetRender<CyclingButtonWidgetEx<T>> getWidgetRenderer() {
		return this.renderer;
	}
	@Override
	public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderer.renderButton(ctx, mouseX, mouseY, delta);
	}
	
	
	
	

	
	public static WidgetBuilder<Boolean> booleanCycle(boolean initial, Text trueText, Text falseText)
	{
		Function<Boolean, Text> valueToText;
		
		final boolean trueTextIsNull = trueText == null;
		final boolean falseTextIsNull = falseText == null;
		
		if (trueTextIsNull && falseTextIsNull)
			valueToText = state -> Text.empty();
		else if (trueTextIsNull)
			valueToText = state -> falseText;
		else if (falseTextIsNull)
			valueToText = state -> trueText;
		else
			valueToText = state -> state ? trueText : falseText;
		
		WidgetBuilder<Boolean> wcbb = new WidgetBuilder<>(valueToText);
		
		wcbb.values(ImmutableList.of(true, false));
		wcbb.initially(initial);
		
		return wcbb;
	}
	
	
	
	

	public static abstract class WidgetBuilderAbstract<V> extends CyclingButtonWidget.Builder<V>
	{
		protected TooltipFactoryEx<V> tooltipFactoryEx = v -> null;
		
		public WidgetBuilderAbstract(Function<V, Text> valueToText) {
			super(valueToText);
		}
		
		@Override 
		@SuppressWarnings("unchecked")
		public Builder<V> initially(V value)
		{
			final CyclingButtonWidgetBuilderAccessor<V> accessor = 
				(CyclingButtonWidgetBuilderAccessor<V>)this;
			
			accessor.setValue(value);
			
			int valueIndex = accessor.getValues().getDefaults().indexOf(value);
			
			// means that it doesn't exist
			if (valueIndex != -1)
				accessor.setInitialIndex(valueIndex);
			
			return this;
		}

		/**
		 * Use {@link #tooltip(TooltipFactoryEx)} instead as this will 
		 * not apply the vanilla variant.
		 * 
		 * <p> The vanilla version only allows the mouse-oriented variant to be used and 
		 * is very limited as the tooltip itself is caught over in a class that doesn't 
		 * allow easy modification unless it was to be used over a mixin.
		 */
		@Override @Deprecated
		public Builder<V> tooltip(TooltipFactory<V> tooltipFactory) {
			return this;
		}
		@Override @Deprecated
		public CyclingButtonWidget<V> build(int x, int y, int width, int height, Text optionText) {
			return null;
		}
		@Override @Deprecated
		public CyclingButtonWidget<V> build(int x, int y, int width, int height, 
			Text optionText, UpdateCallback<V> callback) 
		{
			return null;
		}
		
		public WidgetBuilderAbstract<V> tooltip(TooltipFactoryEx<V> tooltipFactory) {
			this.tooltipFactoryEx = tooltipFactory; return this;
		}
	}

	public static class WidgetBuilder<V> extends WidgetBuilderAbstract<V>
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
			
			int valueIndex = accessor.getValues().getDefaults().indexOf(value);
			
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
			
			List<V> defaults = accessor.getValues().getDefaults();
			
			V startingValue = accessor.getValue();
			startingValue = startingValue == null ? defaults.get(accessor.getInitialIndex()) : startingValue;
			
			Text messageText = accessor.getValueToText().apply(startingValue);
			
			if (!accessor.omitOptionText())
				messageText = ScreenTexts.composeGenericOptionText(optionText, messageText);
			
			return new CyclingButtonWidgetEx<>(width, height, messageText, optionText, 
				accessor.getInitialIndex(), startingValue, accessor.getValues(), accessor.getValueToText(), 
				accessor.getNarrationMessageFactory(), callback, this.tooltipFactoryEx, accessor.omitOptionText());
		}
	}
	
	public interface TooltipFactoryEx<T> extends Function<T, List<OrderedText>> {}
}

