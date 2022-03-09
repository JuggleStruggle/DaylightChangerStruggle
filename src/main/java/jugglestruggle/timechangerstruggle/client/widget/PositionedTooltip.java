package jugglestruggle.timechangerstruggle.client.widget;

import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.OrderableTooltip;

import com.google.common.collect.ImmutableList;

/**
 * 
 *
 * @author JuggleStruggle
 * @implNote Created on 06-Feb-2022, Sunday
 */
@Environment(EnvType.CLIENT)
public interface PositionedTooltip extends OrderableTooltip
{
	int getTooltipWidth();
	int getTooltipHeight();
	
	void setTooltipWidth(int width);
	void setTooltipHeight(int height);
	
	void setOrderedTooltip(List<OrderedText> textToSet);
	
	default void updateTooltip(Text tooltipDescText, Text tooltipText, TextRenderer renderer)
	{
		final boolean descIsNull = tooltipDescText == null;
		final boolean tooltipIsNull = tooltipText == null;
		
		List<OrderedText> compiledTooltipText;
		
		if (descIsNull && tooltipIsNull) {
			compiledTooltipText = ImmutableList.of();
		} 
		else
		{
			byte useCase;
			
			if (descIsNull)
				useCase = 1;
			else if (tooltipIsNull)
				useCase = 3;
			else
				useCase = 2;
			
			
			compiledTooltipText = TimeChangerScreen.createOrderedTooltips(
				renderer, useCase, tooltipDescText, tooltipText
			);
		}
		
		final int[] offsetPos = TimeChangerScreen.getTooltipForWidgetWidthHeight(compiledTooltipText, renderer);
		this.setTooltipWidth(offsetPos[0]); this.setTooltipHeight(offsetPos[1]); 
		
		this.setOrderedTooltip(compiledTooltipText);
	}
}
