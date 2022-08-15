package jugglestruggle.timechangerstruggle.config.property;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.client.config.widget.TextFieldWidgetConfig;
import jugglestruggle.timechangerstruggle.client.config.widget.WidgetConfigInterface;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.client.widget.PositionedTooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Locale;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 * This is directly taken from my own legit client (up to you to 
 * believe) and repurposed for this mod.
 * 
 * @author JuggleStruggle
 * @implNote 23-11-2021, Tuesday
 */
@Environment(EnvType.CLIENT)
public class StringValue extends BaseProperty<StringValue, String>
{
	protected boolean allowEmptyText;
	
	public StringValue(String propertyName, String value) {
		super(propertyName, value);
	}
	public StringValue(String propertyName, String value, String defaultValue) {
		super(propertyName, value, defaultValue);
	}
	
	@Override
	public void set(String value) 
	{
		if (this.isEmptyTextAllowed())
		{
			if (value == null) {
				this.value = ""; return;
			}
		}
		else if (value == null || !(value.isEmpty() || value.isBlank())) {
			return;
		}
			
		this.value = value;
	}
	
	public boolean isEmptyTextAllowed() {
		return this.allowEmptyText;
	}
	public StringValue setEmptyTextAllowance(boolean allow) {
		this.allowEmptyText = allow; return this;
	}
	
	@Override
	public void readFromJson(JsonElement elem) 
	{
		if (!elem.isJsonPrimitive())
			return;
		
		JsonPrimitive prim = elem.getAsJsonPrimitive();
		
		if (prim.isString())
		{
			String s = prim.getAsString();
			
			if (this.isEmptyTextAllowed() || !(s.isEmpty() || s.isBlank())) {
				this.set(s);
			}
		}
	}
	@Override
	public JsonElement writeToJson()
	{
		String s = this.get();
		
		if (this.isEmptyTextAllowed()) {
			return new JsonPrimitive(s == null ? "" : s);
		} else if (!(s.isEmpty() || s.isBlank())) {
			return new JsonPrimitive(s);
		} else {
			return new JsonPrimitive(this.getDefaultValue());
		}
	}
	

	@Override
	public WidgetConfigInterface<StringValue, String> createConfigElement
	(TimeChangerScreen screen, FancySectionProperty owningSection)
	{
		TextFieldWidgetConfig s = new TextFieldWidgetConfig
			(screen.getTextRenderer(), 18, 18, this, this.allowEmptyText);
		
		StringValue.onCreateConfigElementAddTooltips(this, s, screen, owningSection);
		
		return s;
	}
	
	public static <B extends BaseProperty<B, V>, V> void onCreateConfigElementAddTooltips
	(B property, WidgetConfigInterface<B, V> widget, TimeChangerScreen screen, FancySectionProperty owningSection)
	{
		if (widget instanceof PositionedTooltip && owningSection != null && owningSection.get() != null)
		{
			if (owningSection.get().getContent() instanceof TranslatableTextContent)
			{
				Text tooltipDescText = Text.translatable(String.format("%1$s.%2$s",
					((TranslatableTextContent)owningSection.get().getContent()).getKey(), property.property().toLowerCase(Locale.ROOT)));
				
				((PositionedTooltip)widget).updateTooltip(tooltipDescText, null, screen.getTextRenderer());
			}
		}
	}
	
	@Override
	public ArgumentType<String> onCommandOptionGetArgType() {
		return StringArgumentType.string();
	}
	@Override
	public int onCommandOptionWithValueExecute(CommandContext<FabricClientCommandSource> ctx) {
		this.set(StringArgumentType.getString(ctx, "value")); return 3;
	}
}
