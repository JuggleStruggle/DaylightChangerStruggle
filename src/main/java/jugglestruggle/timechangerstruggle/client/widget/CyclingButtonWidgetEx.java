package jugglestruggle.timechangerstruggle.client.widget;

import jugglestruggle.timechangerstruggle.mixin.client.widget.CyclingButtonWidgetAccessor;
import jugglestruggle.timechangerstruggle.mixin.client.widget.CyclingButtonWidgetBuilderAccessor;
import jugglestruggle.timechangerstruggle.util.SimpleCharacterVisitor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
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
	private BiConsumer<CyclingButtonWidgetEx<T>, NarrationMessageBuilder> narrationBuilder;
	
	protected CyclingButtonWidgetEx(int width, int height, Text message, Text optionText, 
		int index, T value, Values<T> values, Function<T, Text> valueToText,
		Function<CyclingButtonWidget<T>, MutableText> narrationMessageFactory, 
		UpdateCallback<T> callback, TooltipFactoryEx<T> tooltipFactory, boolean optionTextOmitted)
	{
		super(0, 0, width, height, message, optionText, index, value, values, valueToText, 
			narrationMessageFactory, callback, null, optionTextOmitted);
		
		this.renderer = new SelfWidgetRender<>(this, null);
		this.tooltipFactoryEx = tooltipFactory;
		this.narrationBuilder = (bw, b) -> this.appendNarrationMessage(b, false, 0);
		
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

	@Override
	public void appendClickableNarrations(NarrationMessageBuilder builder) {
		this.narrationBuilder.accept(this, builder);
	}

	/**
	 * Sets the narration consumer to be used.
	 * 
	 * @param c a consumer; must be non-null
	 * @return the current widget instance
	 */
	public CyclingButtonWidgetEx<T> setNarrationBuilder(BiConsumer<CyclingButtonWidgetEx<T>, NarrationMessageBuilder> c) {
		this.narrationBuilder = c; return this;
	}


	/**
	 * Narrates the contents of this button widget via {@link #getNarrationMessage()}.
	 * @see #appendNarration(NarrationMessageBuilder, byte, boolean, Text)
	 */
	public void appendNarrationMessage(NarrationMessageBuilder builder, boolean skipMessage, int skipTooltipLines) {
		this.appendNarration(builder, skipTooltipLines, skipMessage ? null : this.getNarrationMessage());
	}
	/**
	 * Narrates the contents of this button widget by using a {@link #compiledTooltipText}'s line.
	 * 
	 * @param targetTooltipLine targets the index from {@link #compiledTooltipText}
	 * @see #appendNarration(NarrationMessageBuilder, byte, boolean, Text)
	 */
	public void appendNarrationTooltipLine(NarrationMessageBuilder builder, int skipTooltipLines, int targetTooltipLine) 
	{
		this.appendNarration(builder, skipTooltipLines, SimpleCharacterVisitor.asMutableText
			(targetTooltipLine, targetTooltipLine + 1, this.cachedTooltipText));
	}
	/**
	 * Narrates the contents of this button widget by using its title as {@code titleText}.
	 * 
	 * @param builder important to have the narrator be able to speak on the parts it shall be narrated
	 * @param skipTooltipLines whether to skip the tooltip message's lines, starting from index 0
	 * @param titleText the text for the narrator to speak out as the title; can be null
	 */
	public void appendNarration(NarrationMessageBuilder builder, int skipTooltipLines, Text titleText)
	{
		if (titleText != null)
			builder.put(NarrationPart.TITLE, titleText);
		
		if (this.active)
		{
			@SuppressWarnings("unchecked")
			CyclingButtonWidgetAccessor<T> bwa = (CyclingButtonWidgetAccessor<T>)this;
			
			builder.put(NarrationPart.USAGE, Text.translatable("narration.button.usage." + 
				(this.isFocused() ? "focused" : "hovered"), bwa.dcs_composeText(bwa.dcs_getValue(1))));
			WidgetOrderedTooltip.narrateTooltip(builder, this.cachedTooltipText, skipTooltipLines);
		}
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
		 * <p> The vanilla version, while allowing both mouse and key variants to be used,
		 * is very limited as the tooltip itself is caught over in a class that doesn't 
		 * allow easy modification unless a mixin were to be used to give access to those
		 * functions.
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
			
			// means that it doesn't exist if -1
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
			final CyclingButtonWidgetBuilderAccessor<V> accessor = (CyclingButtonWidgetBuilderAccessor<V>)this;
			
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

