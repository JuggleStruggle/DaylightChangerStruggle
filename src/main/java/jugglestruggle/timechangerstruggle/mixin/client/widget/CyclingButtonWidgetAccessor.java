package jugglestruggle.timechangerstruggle.mixin.client.widget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

/**
 * @author JuggleStruggle
 * @implNote Implemented in v0.0.2
 */
@Mixin(CyclingButtonWidget.class)
public interface CyclingButtonWidgetAccessor<T>
{
	@Invoker(value = "getValue")
	T dcs_getValue(int offset);
	
	@Invoker(value = "composeText")
	Text dcs_composeText(T value); 
}
