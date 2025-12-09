package jugglestruggle.timechangerstruggle.mixin.client.widget;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget.Values;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 03-Feb-2022, Thursday
 */
@Mixin(CyclingButtonWidget.Builder.class)
public interface CyclingButtonWidgetBuilderAccessor<T>
{
	@Accessor("valueSupplier")
	Supplier<T> getValueSupplier();
	
	@Accessor("values")
	Values<T> getValues();

	@Accessor("valueToText")
	Function<T, Text> getValueToText();

	@Accessor("labelType")
	CyclingButtonWidget.LabelType getDisplayState();

	@Accessor("narrationMessageFactory")
	Function<CyclingButtonWidget<T>, MutableText> getNarrationMessageFactory();
}
