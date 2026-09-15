package jugglestruggle.timechangerstruggle.client.widget;

import jugglestruggle.timechangerstruggle.util.SimpleCharacterVisitor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.OrderedText;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author JuggleStruggle
 * @implNote Created on 13-Feb-2022, Sunday
 */
public class ButtonWidgetEx extends ButtonWidget
	implements WidgetPositionedTooltip, SelfWidgetRendererInheritor<ButtonWidgetEx>
{
	// Both fields introduced in v0.0.5
	public static final Predicate<MouseInput> LEFT_CLICK_BUTTON = i -> 
		i.button() == 1;
	public static final Predicate<MouseInput> LEFT_AND_RIGHT_CLICK_BUTTONS = i -> 
		i.button() == 1 || i.button() == 3;
	
	private int tooltipWidth;
	private int tooltipHeight;
	
	private List<OrderedText> compiledTooltipText;
	private BiConsumer<ButtonWidgetEx, NarrationMessageBuilder> narrationBuilder;
	private Predicate<MouseInput> validClickButtons;
	
	private final SelfWidgetRender<ButtonWidgetEx> renderer;
	private final Consumer<AbstractInput> onPress;
	
	// v0.0.5 change: Cut down on initialization repeats and make changes to onPress
	public ButtonWidgetEx(int width, int height, net.minecraft.text.Text message, 
		List<OrderedText> compiledTooltip, TextRenderer renderer, Consumer<AbstractInput> onPress)
	{
		super(0, 0, width, height, message, null, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
		this.compiledTooltipText = compiledTooltip;
		this.renderer = new SelfWidgetRender<>(this, renderer);
		this.narrationBuilder = (bw, b) -> this.appendNarrationMessage(b, (byte)1, 0);
		this.validClickButtons = ButtonWidgetEx.LEFT_CLICK_BUTTON;
		this.onPress = onPress;
	}
	
	public ButtonWidgetEx(int width, int height, net.minecraft.text.Text message, net.minecraft.text.Text tooltipDescText, 
		net.minecraft.text.Text tooltipText, TextRenderer renderer, Consumer<AbstractInput> onPress)
	{
		this(width, height, message, null, renderer, onPress);
		this.updateTooltip(tooltipDescText, tooltipText, renderer);
	}
	
	public ButtonWidgetEx(int width, int height, net.minecraft.text.Text message, TextRenderer renderer, Consumer<AbstractInput> onPress) {
		this(width, height, message, null, renderer, onPress);
	}
	
	// v0.0.5 change: To allow input presses to go through due to some button presses 
	// requiring certain input actions such as right-clicking. Completely replaces the 
	// old approach where the value being passed was this button widget itself.
	@Override
	public void onPress(AbstractInput input) {
		this.onPress.accept(input);
	}
	// Introduced in v0.0.5: Allows other mouse inputs (most specifically buttons) to
	// be valid based on a predicate
	@Override
	protected boolean isValidClickButton(MouseInput input) {
		return this.validClickButtons.test(input);
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
	public void drawIcon(DrawContext ctx, int mouseX, int mouseY, float delta) {
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
	public ButtonWidgetEx setNarrationBuilder(BiConsumer<ButtonWidgetEx, NarrationMessageBuilder> c) {
		this.narrationBuilder = c; return this;
	}
	
	/**
	 * Narrates the contents of this button widget via {@link #getNarrationMessage()}.
	 * @see #appendNarration(NarrationMessageBuilder, byte, boolean, net.minecraft.text.Text)
	 */
	public void appendNarrationMessage(NarrationMessageBuilder builder, byte messageOrder, int skipTooltipLines) {
		this.appendNarration(builder, messageOrder, skipTooltipLines, this.getNarrationMessage());
	}
	/**
	 * Narrates the contents of this button widget by using a {@link #compiledTooltipText}'s line.
	 * 
	 * @param targetTooltipLine targets the index from {@link #compiledTooltipText}
	 * @see #appendNarration(NarrationMessageBuilder, byte, boolean, net.minecraft.text.Text)
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
	public void appendNarration(NarrationMessageBuilder builder, byte messageOrder, int skipTooltipLines, net.minecraft.text.Text titleText)
	{
		if (messageOrder == 1)
			builder.put(NarrationPart.TITLE, titleText);
		
		if (this.active)
		{
			builder.put(messageOrder == 2 ? NarrationPart.TITLE : NarrationPart.USAGE, 
				net.minecraft.text.Text.translatable("narration.button.usage." + (this.isFocused() ? "focused" : "hovered")));
				WidgetOrderedTooltip.narrateTooltip(builder, this.compiledTooltipText, skipTooltipLines);
		}
		
		if (messageOrder == 2)
			builder.put(NarrationPart.USAGE, titleText);
	}
	
	/**
	 * Determines whether the mouse click is considered valid for this button.
	 * 
	 * @param p the predicate to set
	 * @return the current widget instance
	 * 
	 * @implNote Introduced in v0.0.5
	 */
	public ButtonWidgetEx setValidClickButtons(Predicate<MouseInput> p) {
		this.validClickButtons = p; return this;
	}
}