package jugglestruggle.timechangerstruggle.client.widget;

import net.minecraft.client.gui.widget.ClickableWidget;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 13-Feb-2022, Sunday
 */
public interface SelfWidgetRendererInheritor<W extends ClickableWidget>
{
	public SelfWidgetRender<W> getWidgetRenderer();
}
