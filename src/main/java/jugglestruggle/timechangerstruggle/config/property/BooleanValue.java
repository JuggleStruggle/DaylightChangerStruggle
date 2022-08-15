package jugglestruggle.timechangerstruggle.config.property;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.client.config.widget.CyclingWidgetConfig;
import jugglestruggle.timechangerstruggle.client.config.widget.WidgetConfigInterface;
import jugglestruggle.timechangerstruggle.client.config.widget.CyclingWidgetConfig.WidgetConfigBuilderBoolean;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Locale;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.client.gui.widget.CyclingButtonWidget;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 31-Jan-2022, Monday
 */
@Environment(EnvType.CLIENT)
public class BooleanValue extends BaseProperty<BooleanValue, Boolean>
{
	private Text trueText = ScreenTexts.ON;
	private Text falseText = ScreenTexts.OFF;
	private Text propertyText;
	
	private final CyclingButtonWidget.UpdateCallback<Boolean> callback = (button, value) -> { 
		if (super.consumer != null)
			super.consumer.consume(this, value);
	};
	
	public BooleanValue(String property, boolean value) {
		super(property, value);
	}

	@Override
	public void set(Boolean value) {
		super.value = (value == null) ? false : value;
	}
	
	public BooleanValue setTrueText(Text text) {
		this.trueText = text; return this;
	}
	public BooleanValue setFalseText(Text text) {
		this.falseText = text; return this;
	}
	public BooleanValue setText(Text text) {
		this.propertyText = text; return this;
	}
	
	@Override
	public WidgetConfigInterface<BooleanValue, Boolean> createConfigElement
	(TimeChangerScreen screen, FancySectionProperty owningSection)
	{
		WidgetConfigBuilderBoolean builder = 
			CyclingWidgetConfig.booleanCycle(this, this.trueText, this.falseText);
		
		Text optionText;
		
		if (this.propertyText == null)
		{
			optionText = null;
			
			if (owningSection != null)
			{
				Text sectionText = owningSection.get();
				
				if (sectionText != null && sectionText.getContent() instanceof TranslatableTextContent)
				{
					optionText = Text.translatable(String.format("%1$s.%2$s",
						((TranslatableTextContent)sectionText.getContent()).getKey(), this.property().toLowerCase(Locale.ROOT)));
				}
			}
			
			if (optionText == null)
				optionText = Text.of(this.property());
		}
		else
			optionText = this.propertyText;
		
		return builder.build(20, 20, optionText, this.callback);
	}
	@Override
	public ArgumentType<Boolean> onCommandOptionGetArgType() {
		return BoolArgumentType.bool();
	}
	@Override
	public int onCommandOptionWithValueExecute(CommandContext<FabricClientCommandSource> ctx) {
		this.set(BoolArgumentType.getBool(ctx, "value")); return 3;
	}
	@Override
	public boolean onCommandOptionNoValueShouldBeExecuted() {
		return true;
	}
	@Override
	public int onCommandOptionNoValueExecute(CommandContext<FabricClientCommandSource> ctx) {
		this.set(!this.get()); return 3;
	}
	
	

	@Override
	public void readFromJson(JsonElement elem) 
	{
		if (elem.isJsonPrimitive()) 
		{
			JsonPrimitive prim = elem.getAsJsonPrimitive();
			
			if (prim.isBoolean())
				this.set(prim.getAsBoolean());
		}
	}

	@Override
	public JsonElement writeToJson() {
		return new JsonPrimitive(this.get());
	}
}
