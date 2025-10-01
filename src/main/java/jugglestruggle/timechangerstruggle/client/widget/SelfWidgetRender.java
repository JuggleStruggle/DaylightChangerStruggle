package jugglestruggle.timechangerstruggle.client.widget;

import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.client.util.color.RainbowRGB;
import jugglestruggle.timechangerstruggle.client.util.render.RenderUtils;
import jugglestruggle.timechangerstruggle.util.EasingType;
import jugglestruggle.timechangerstruggle.util.Easings;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.OrderedText;

/**
 * @author JuggleStruggle
 * @implNote Created on 13-Feb-2022, Sunday
 */
public class SelfWidgetRender<W extends ClickableWidget>
{
	private final W widget;
	private TextRenderer textRenderer;

	private RainbowRGB selectedTextRGB;
	private RainbowRGB[] selectedRectRGB;
	
	public SelfWidgetRender(W widget, TextRenderer textRenderer)
	{
		this.widget = widget;
		this.textRenderer = textRenderer;

		this.selectedTextRGB = new RainbowRGB(0xFFFFFFFF);
		this.selectedRectRGB = new RainbowRGB[4];
		
		boolean isTopSide;
		
		for (int i = 0; i < 4; ++i)
		{
			isTopSide = i < 2;
			
			this.selectedRectRGB[i] = new RainbowRGB
			(
				switch (i) { 
					default -> 0xCCB0CD81; case 1 -> 0xCCB0E481; 
					case 2 -> 0xAA00153E; case 3 -> 0xAA001558;
				}, 
				Easings.QUINT, 
				isTopSide ? EasingType.IN : EasingType.OUT, 40, 
				isTopSide ? (byte)2 : 4
			);
		}
	}
	
	public void setTextRendering(TextRenderer renderer) {
		this.textRenderer = renderer;
	}
	
	public void tick()
	{
		if (this.widget.active && this.widget.isSelected())
		{
			this.selectedTextRGB.tick();
			
			for (int i = 0; i < 4; ++i)
				this.selectedRectRGB[i].tick();
		}
	}
	
	public void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta)
	{
		boolean renderNormalText = true;	
		int textColor;
		
		if (this.widget.active && this.widget.isSelected())
		{
			textColor = this.selectedTextRGB.getInterpolatedColor(delta);
			
			// v0.0.2+1.21.6 port change: Since Mojang is making changes to rendering side of things (as of June 2025), 
			// the Rainbow Shader that was formerly used was completely removed. It will still be used in the future, 
			// but not during minor ports to make porting quicker for newer versions of the game.
			RenderUtils.fillPointedGradient
			(
				ctx, this.widget.getX(), this.widget.getY(), 
				this.widget.getRight(), this.widget.getBottom(), 0, 
			
				this.selectedRectRGB[0].getInterpolatedColor(delta), 
				this.selectedRectRGB[1].getInterpolatedColor(delta), 
				this.selectedRectRGB[2].getInterpolatedColor(delta), 
				this.selectedRectRGB[3].getInterpolatedColor(delta)
			);
		}
		else
		{
			       textColor = this.widget.active ? 0xFFFFFFFF : 0xFFA0A0A0;
			int enabledColor = this.widget.active ? 0xCC888888 : 0xCC333333;
			
			ctx.fill(this.widget.getX(), this.widget.getY(), 
				this.widget.getRight(), this.widget.getBottom(), enabledColor);
		}
		
		
		if (renderNormalText)
		{
			OrderedText message = this.widget.getMessage().asOrderedText();
			int messageWidth = this.textRenderer.getWidth(message);
			
			final float x = this.widget.getX() + (this.widget.getWidth() / 2) - (messageWidth / 2);
			final float y = this.widget.getY() + ((this.widget.getHeight() - (this.textRenderer.fontHeight - 1)) / 2);

			TimeChangerScreen.renderTextD(ctx, this.textRenderer, message, x, y, textColor, true);
		}
	}
}