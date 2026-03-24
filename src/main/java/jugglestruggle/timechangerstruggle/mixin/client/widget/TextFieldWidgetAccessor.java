package jugglestruggle.timechangerstruggle.mixin.client.widget;

import jugglestruggle.timechangerstruggle.client.config.widget.NumericFieldWidgetConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.widget.TextFieldWidget;

/**
 * Introduced mostly for the {@link NumericFieldWidgetConfig#write(String)} not being 
 * given access to modify important parts of the text.
 *
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.4+26.1
 */
@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor
{
	@Accessor("selectionStart")
	int getSelectionStart();

	@Accessor("selectionEnd")
	int getSelectionEnd();

	@Accessor("maxLength")
	int getMaxLength();

	@Accessor("text")
	void setDirectText(String newTextRes);

	@Invoker("onChanged")
	void onDirectChanged(String newTextRes);
}
