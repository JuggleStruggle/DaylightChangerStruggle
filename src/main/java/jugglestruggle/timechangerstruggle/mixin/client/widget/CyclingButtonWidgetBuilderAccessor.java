package jugglestruggle.timechangerstruggle.mixin.client.widget;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget.Values;

/**
 *
 * @author JuggleStruggle
 * @implNote
 * Created on 03-Feb-2022, Thursday
 */
@Mixin(CyclingButtonWidget.Builder.class)
public interface CyclingButtonWidgetBuilderAccessor<T>
{
	@Accessor("value")
	T getValue();
	@Accessor("value")
	void setValue(T value);
	
	@Accessor("values")
	Values<T> values();
	
	@Accessor("initialIndex")
	int getInitialIndex();
	@Accessor("initialIndex")
	void setInitialIndex(int initialIndex);

	@Accessor("valueToText")
	Function<T, Text> getValueToText();

	@Accessor("optionTextOmitted")
	boolean omitOptionText();

	@Accessor("narrationMessageFactory")
	Function<CyclingButtonWidget<T>, MutableText> getNarrationMessageFactory();

	@Accessor("tooltipFactory")
	CyclingButtonWidget.TooltipFactory<T> getTooltipFactory();
}
