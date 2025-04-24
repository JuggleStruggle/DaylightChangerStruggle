package jugglestruggle.timechangerstruggle.client.widget;

import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;

import java.util.List;

import net.minecraft.text.OrderedText;

/**
 * An ordered tooltip that is used for the {@link TimeChangerScreen}.
 * It is also used to avoid using the Vanilla 1.21.5's {@code Tooltip}
 * class as it is restrictive for what one can do.
 *
 * @author JuggleStruggle
 * @implNote Exclusive for the 1.21.5 port
 */
public interface WidgetOrderedTooltip
{
	List<OrderedText> getOrderedTooltip();
	default void setOrderedTooltip(List<OrderedText> textToSet) {}
}
