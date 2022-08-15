package jugglestruggle.timechangerstruggle.client.config.property;

import jugglestruggle.timechangerstruggle.client.config.widget.WidgetConfigInterface;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.text.Text;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 * A section that cannot and should not be used to save as
 * its intention is that it is used to help let the user
 * know that the properties provided are on their own section.
 *
 * <p> If you see Fancy at the start of the name, assume that
 * these are only there to make things readable.
 *
 * @author JuggleStruggle
 * @implNote Created on 01-Feb-2022, Tuesday
 */
@Environment(EnvType.CLIENT)
public class FancySectionProperty extends BaseProperty<FancySectionProperty, Text>
{
	public static final FancySectionProperty EMPTY = new FancySectionProperty("", null);
	
	public FancySectionProperty(String property, Text value) {
		super(property, value);
	}

	@Override
	public void set(Text value) {}

	@Override
	public WidgetConfigInterface<FancySectionProperty, Text> createConfigElement
	(TimeChangerScreen screen, FancySectionProperty owningSection)
	{
		return null;
	}

	@Override
	public void readFromJson(JsonElement elem) {}

	@Override
	public JsonElement writeToJson() {
		return null;
	}

	@Override
	public ArgumentType<Text> onCommandOptionGetArgType() {
		return null;
	}

	@Override
	public int onCommandOptionWithValueExecute(CommandContext<FabricClientCommandSource> ctx) {
		return 0;
	}
}
