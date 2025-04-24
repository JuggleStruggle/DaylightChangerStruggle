package jugglestruggle.timechangerstruggle.client.widget;

import java.util.List;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

/**
 * 
 *
 * @author JuggleStruggle
 * @implNote Created on 13-Feb-2022, Sunday
 */
public class ButtonWidgetEx extends ButtonWidget implements WidgetPositionedTooltip, 
	SelfWidgetRendererInheritor<ButtonWidgetEx>
{
	private int tooltipWidth;
	private int tooltipHeight;
	private List<OrderedText> compiledTooltipText;
	private final SelfWidgetRender<ButtonWidgetEx> renderer;
	
	public ButtonWidgetEx(int width, int height, Text message, Text tooltipDescText, 
		Text tooltipText, TextRenderer renderer, PressAction onPress)
	{
		super(0, 0, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
		this.updateTooltip(tooltipDescText, tooltipText, renderer);
		this.renderer = new SelfWidgetRender<>(this, renderer);
	}
	
	public ButtonWidgetEx(int width, int height, Text message, 
		List<OrderedText> compiledTooltip, TextRenderer renderer, PressAction onPress)
	{
		super(0, 0, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
		this.compiledTooltipText = compiledTooltip;
		this.renderer = new SelfWidgetRender<>(this, renderer);
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
	public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderer.renderButton(ctx, mouseX, mouseY, delta);
	}
}