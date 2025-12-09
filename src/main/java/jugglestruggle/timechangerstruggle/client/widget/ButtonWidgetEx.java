package jugglestruggle.timechangerstruggle.client.widget;

import jugglestruggle.timechangerstruggle.util.SimpleCharacterVisitor;

import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

/**
 * @author JuggleStruggle
 * @implNote Created on 13-Feb-2022, Sunday
 */
public class ButtonWidgetEx extends ButtonWidget 
implements WidgetPositionedTooltip, SelfWidgetRendererInheritor<ButtonWidgetEx>
{
	private int tooltipWidth;
	private int tooltipHeight;

	private List<OrderedText> compiledTooltipText;
	private BiConsumer<ButtonWidgetEx, NarrationMessageBuilder> narrationBuilder;
	
	private final SelfWidgetRender<ButtonWidgetEx> renderer;
	
	public ButtonWidgetEx(int width, int height, Text message, Text tooltipDescText, Text tooltipText, 
		TextRenderer renderer, PressAction onPress)
	{
		super(0, 0, width, height, message, onPress, ButtonWidget.EMPTY);
		this.updateTooltip(tooltipDescText, tooltipText, renderer);
		this.renderer = new SelfWidgetRender<>(this, renderer);
	}

	public ButtonWidgetEx(int width, int height, Text message, 
		List<OrderedText> compiledTooltip, TextRenderer renderer, PressAction onPress)
	{
		super(0, 0, width, height, message, onPress, ButtonWidget.EMPTY);
		this.compiledTooltipText = compiledTooltip;
		this.renderer = new SelfWidgetRender<>(this, renderer);
		this.narrationBuilder = (bw, b) -> this.appendNarrationMessage(b, (byte)1, 0);
	}

	public ButtonWidgetEx(int width, int height, Text message, TextRenderer renderer, PressAction onPress) {
		this(width, height, message, null, renderer, onPress);
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
	public SelfWidgetRender<ButtonWidgetEx> getWidgetRenderer() {
		return this.renderer;
	}
	
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderer.renderButton(matrices, mouseX, mouseY, delta);
	}

	public void appendClickableNarrations(NarrationMessageBuilder builder) {
		this.narrationBuilder.accept(this, builder);
	}

	/**
	 * Sets the narration consumer to be used.
	 * 
	 * @param c a consumer; must not be null
	 * @return the current widget instance
	 */
	public ButtonWidgetEx setNarrationBuilder(BiConsumer<ButtonWidgetEx, NarrationMessageBuilder> c) {
		this.narrationBuilder = c; return this;
	}
	
	/**
	 * Narrates the contents of this button widget via {@link #getNarrationMessage()}.
	 * @see #appendNarration(NarrationMessageBuilder, byte, boolean, Text)
	 */
	public void appendNarrationMessage(NarrationMessageBuilder builder, byte messageOrder, int skipTooltipLines) {
		this.appendNarration(builder, messageOrder, skipTooltipLines, this.getNarrationMessage());
	}
	/**
	 * Narrates the contents of this button widget by using a {@link #compiledTooltipText}'s line.
	 * 
	 * @param targetTooltipLine targets the index from {@link #compiledTooltipText}
	 * @see #appendNarration(NarrationMessageBuilder, byte, boolean, Text)
	 */
	public void appendNarrationTooltipLine(NarrationMessageBuilder builder, byte messageOrder, int skipTooltipLines, int targetTooltipLine) 
	{
		this.appendNarration(builder, messageOrder, skipTooltipLines, 
			SimpleCharacterVisitor.asMutableText(targetTooltipLine, targetTooltipLine + 1, this.compiledTooltipText));
	}
	/**
	 * Narrates the contents of this button widget by using its title as {@code titleText}.
	 * 
	 * @param builder important to have the narrator be able to speak on the parts it shall be narrated
	 * @param messageOrder 0 = no message, 1 = before description, 2 = after description
	 * @param skipTooltipLines whether to skip the tooltip message's lines, starting from index 0
	 * @param titleText the text for the narrator to speak out as the title; if {@code messageOrder} is not 1 or 2, 
	 *        this must be non-null
	 */
	public void appendNarration(NarrationMessageBuilder builder, byte messageOrder, int skipTooltipLines, Text titleText)
	{
		if (messageOrder == 1)
			builder.put(NarrationPart.TITLE, titleText);
		
		if (this.active)
		{
			builder.put(messageOrder == 2 ? NarrationPart.TITLE : NarrationPart.USAGE, 
				new TranslatableText("narration.button.usage." + (this.isFocused() ? "focused" : "hovered")));
				WidgetOrderedTooltip.narrateTooltip(builder, this.compiledTooltipText, skipTooltipLines);
		}
		
		if (messageOrder == 2)
			builder.put(NarrationPart.USAGE, titleText);
	}
}