package jugglestruggle.timechangerstruggle.client.widget;

import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.util.SimpleCharacterVisitor;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import java.util.List;

/**
 * An ordered tooltip that is used for the {@link TimeChangerScreen}.
 * It is also used to avoid using the Vanilla 1.21.5's {@code Tooltip}
 * class as it is restrictive for what one can do.
 *
 * @author JuggleStruggle
 * @implNote Introduced from the 1.21.5 port
 */
public interface WidgetOrderedTooltip
{
	List<OrderedText> getOrderedTooltip();
	default void setOrderedTooltip(List<OrderedText> textToSet) {}
	
	static void narrateTooltip(NarrationMessageBuilder builder, List<OrderedText> compiledTooltipText, int skipAmountOfLines)
	{
		if (compiledTooltipText == null)
			return;
		
		MutableText mt = SimpleCharacterVisitor.asMutableText(skipAmountOfLines, -1, compiledTooltipText);
		
		if (mt.getSiblings().size() > 0)
			builder.put(NarrationPart.HINT, mt);
	}
}
