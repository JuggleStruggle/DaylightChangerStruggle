package jugglestruggle.timechangerstruggle.client.config.property;

import jugglestruggle.timechangerstruggle.client.config.widget.WidgetConfigInterface;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 * A property whose goal is to be used as a widget to perform other actions that
 * otherwise would require the user to go into the configuration file/etc. to
 * change certain settings. 
 * 
 * <p> At the time of writing, this has no functionality regarding saving or 
 * loading and command-like arguments like a usual property would have unless 
 * there is a need for in the future.
 *
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.1
 */
public class WidgetOnlyProperty extends BaseProperty<WidgetOnlyProperty, Object>
{
	final CreateConfigElem onCreateConfigElement;
	
	public WidgetOnlyProperty(String property, CreateConfigElem onCreateConfigElement)
	{
		super(property, new Object()); 
		this.onCreateConfigElement = onCreateConfigElement;
	}

	@Override
	public void set(Object value) {}

	@Override
	public WidgetInterface createConfigElement(TimeChangerScreen screen, FancySectionProperty owningSection) {
		return this.onCreateConfigElement.apply(screen, owningSection, this);
	}

	@Override
	public ArgumentType<Object> onCommandOptionGetArgType() {
		return null;
	}
	@Override
	public int onCommandOptionWithValueExecute(CommandContext<FabricClientCommandSource> ctx) {
		return 0;
	}

	@Override
	public void readFromJson(JsonElement elem) {}

	@Override
	public JsonElement writeToJson() {
		return null;
	}

	public interface WidgetInterface extends WidgetConfigInterface<WidgetOnlyProperty, Object>
	{}
	
	public interface CreateConfigElem
	{
		WidgetInterface apply(TimeChangerScreen screen, FancySectionProperty section, WidgetOnlyProperty prop);
	}
}
