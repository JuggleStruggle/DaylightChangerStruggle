package jugglestruggle.timechangerstruggle.client.widget;

import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.client.util.color.AbstractRGB;
import jugglestruggle.timechangerstruggle.client.util.color.RainbowRGB;
import jugglestruggle.timechangerstruggle.client.util.render.RainbowShader;
import jugglestruggle.timechangerstruggle.client.util.render.RenderUtils;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.OrderedText;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 13-Feb-2022, Sunday
 */
public class SelfWidgetRender<W extends ClickableWidget>
{
	private final W widget;
	private TextRenderer textRenderer;

	private AbstractRGB textColoring;
	
	private float stripeScale;
	private float rainbowSpeed;
	
	private float rainbowOffset;
	private float previousRainbowOffset;
	
	public boolean swapTextColoringWithRainbow;
//	private AbstractRGB[] hoveredColor;
	
	public SelfWidgetRender(W widget, TextRenderer textRenderer)
	{
		this.widget = widget;
		this.textRenderer = textRenderer;

		/*
		 this.hoveredColor = ChromaRGB.createColors
		 	(0xFFFFFFFF, 0xFFFFFF00, 0xFF00FFFF, 0xFF00FFFF, 0xFFFF00FF);
		 */
		this.textColoring = RainbowRGB.createColors(0xFFFFFFFF)[0];
		
		this.stripeScale = 2.0f;
		this.rainbowSpeed = 1.0f;
		this.rainbowOffset = 0.0f;
		
		// Non-functional; if you have an idea as to how we can render text
		// using the rainbow shader then we can make this useful :D
		this.swapTextColoringWithRainbow = false;
	}
	
	public void setTextRendering(TextRenderer renderer) {
		this.textRenderer = renderer;
	}
	
	public void tick()
	{
		if (this.widget.active && this.widget.isSelected())
		{
			this.textColoring.tick();
			
//			float maxOffset = 76.0f;  // for scale 2 without any changes ( 6 colors)
//			float maxOffset = 152.0f; // for scale 1 without any changes ( 6 colors)
			float maxOffset = 302.0f; // for scale 1 without any changes (12 colors)
			
			if (this.stripeScale != 1.0f)
				maxOffset /= this.stripeScale;
			
			if (this.rainbowOffset > maxOffset) 
			{
				this.previousRainbowOffset = 0f;
				this.rainbowOffset = this.rainbowOffset - (maxOffset + 1.0f);
			} 
			else 
			{
				this.previousRainbowOffset = this.rainbowOffset;
				this.rainbowOffset += this.rainbowSpeed;
			}
		}
	}
	
	public void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta)
	{
		boolean stcwr = false;	
		int textColor;
		
		if (this.widget.active && this.widget.isSelected())
		{
			if (this.swapTextColoringWithRainbow)
			{
				stcwr = true; textColor = 0xFF000000;
				
				ctx.fill(this.widget.getX(), this.widget.getY(), 
					this.widget.getRight(), this.widget.getBottom(), textColor);
			}
			else
			{
				textColor = this.textColoring.getInterpolatedColor(delta);
				this.fillMyRainbow(ctx, delta, false);
			}
		}
		else
		{
			       textColor = this.widget.active ? 0xFFFFFF : 0xA0A0A0;
			int enabledColor = this.widget.active ? 0xCC888888 : 0xCC333333;
			
			ctx.fill(this.widget.getX(), this.widget.getY(), 
				this.widget.getRight(), this.widget.getBottom(), enabledColor);
		}
		
		OrderedText message = this.widget.getMessage().asOrderedText();
		int messageWidth = this.textRenderer.getWidth(message);
		
		
		if (!stcwr)
		{
			final float x = this.widget.getX() + (this.widget.getWidth() / 2) - (messageWidth / 2);
			final float y = this.widget.getY() + ((this.widget.getHeight() - (this.textRenderer.fontHeight - 1)) / 2);

			TimeChangerScreen.renderTextD(ctx, this.textRenderer, message, x, y, textColor, true);
		}
	}
	
	private void fillMyRainbow(DrawContext ctx, float delta, boolean adv)
	{
		RainbowShader.uStripeScale = 50.0f / 2.0f * this.stripeScale;
		RainbowShader.uStrokeWidth = 0.0f;
		RainbowShader.uTimeOffset = 0.0f;
		
		RenderUtils.fillRainbow
		(
			ctx, 
			
			this.widget.getX(), this.widget.getY(), 
			this.widget.getRight(), this.widget.getBottom(),
			
			// z offset does not exist anymore in 1.21.5; skip it
			0, // this.widget.getZOffset(),
			
			0.0f, 0.0f, 0.0f, 
			
			(this.previousRainbowOffset + (this.rainbowOffset - this.previousRainbowOffset) * delta) / 20.0f,
			
			adv
		);
	}
}