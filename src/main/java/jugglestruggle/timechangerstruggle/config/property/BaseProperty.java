package jugglestruggle.timechangerstruggle.config.property;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.client.config.widget.WidgetConfigInterface;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 31-Jan-2022, Monday
 */
@Environment(EnvType.CLIENT)
public abstract class BaseProperty<B extends BaseProperty<B, V>, V>
{
	protected final String propertyKey;
	
	protected V value;
	protected V defaultValue;
	
	protected ValueConsumer<B, V> consumer;
	
	public BaseProperty(String property, V value) {
		this(property, value, value);
	}
	
	public BaseProperty(String property, V value, V defaultValue)
	{
		this.propertyKey = property;
		this.value = value;
		this.defaultValue = defaultValue;
	}
	
	public final String property() {
		return this.propertyKey;
	}
	
	public V get() {
		return this.value;
	}
	public V getDefaultValue() {
		return this.defaultValue;
	}
	
	public abstract void set(V value);

	/**
	 * Creates a configuration element used in {@link TimeChangerScreen} 
	 * alone  <i>(although other classes can make use of it anyways)</i>.
	 * 
	 * @param screen the screen that is creating the element
	 * @param owningSection the section in which this property resides on
	 * 
	 * @return a widget which contains this property for reading/writing
	 * purposes
	 */
	public abstract WidgetConfigInterface<B, V> 
	createConfigElement(TimeChangerScreen screen, FancySectionProperty owningSection);
	
	/**
	 * Gets the argument type that this property associates with for the 
	 * value to be obtained from.
	 * 
	 * <p> If the value is integer, you'd use IntegerArgumentType from 
	 * the brigadier class. Or if it is a custom one that the Brigadier 
	 * does not contain you'd want to go for a custom one that supports
	 * the value type from the property.
	 * 
	 * @return an argument type to use for the option's value
	 */
	public abstract ArgumentType<V> onCommandOptionGetArgType();
	/**
	 * Executes the option's value. This should only be used to set the
	 * property's value to the one provided from the context itself (ctx).
	 * 
	 * <p> To get the value for your {@linkplain #onCommandOptionGetArgType() 
	 * argument type}, use {@code "value"} as the name to search for your value.
	 * For example, to get a Boolean value from the context, use
	 * {@code BoolArgumentType.getBool(context, "value")}
	 * 
	 * <p> See the return to see how each returned integer is used for.
	 * 
	 * @param ctx the context used to get the value from
	 * @return 
	 * <ul>
	 * <li> 0 = fail; 
	 * <li> 1 = success (no saves);
	 * <li> 2 = success (writes to the daylight cycle's property); 
	 * <li> 3+ = success (uses 2 and also saves it to disk)
	 * </ul>
	 */
	public abstract int onCommandOptionWithValueExecute(CommandContext<FabricClientCommandSource> ctx);
	
	/**
	 * By default, leaving this alone will avoid adding execution options to
	 * this property when attempting to pass the argument without any values.
	 * 
	 * @return a boolean value; default is {@code false}
	 */
	public boolean onCommandOptionNoValueShouldBeExecuted() { return false; }
	/**
	 * See {@link #onCommandOptionWithValueExecute} to get a basic idea
	 * in order to understand how both function. This will not be executed if
	 * {@link #onCommandOptionNoValueShouldBeExecuted()} is not set to {@code true}.
	 * 
	 * @param ctx the context used for a variety of reasons
	 * @return see {@link #onCommandOptionWithValueExecute}
	 */
	public int onCommandOptionNoValueExecute(CommandContext<FabricClientCommandSource> ctx) { return 0; }
	
	/**
	 * Reads what the primitive has provided to the config.
	 * @param elem the element used as the base JSON format
	 */
	public abstract void readFromJson(JsonElement elem);
	/**
	 * Writes to the config.
	 * @return an element to write.
	 */
	public abstract JsonElement writeToJson();
	
	public ValueConsumer<B, V> getConsumer() {
		return this.consumer;
	}
	
	/**
	 * Sets the consumer for when property values are updated.
	 * 
	 * @param consumer the listening consumer
	 * @return the same class but with an updated consumer
	 * 
	 * @see #consumer(DayNightCycleBasis)
	 * @see #consumerOnlyIfNotExists(DayNightCycleBasis)
	 */
	public BaseProperty<B, V> consumer(ValueConsumer<B, V> consumer) {
		this.consumer = consumer; return this;
	}
	/**
	 * Creates a consumer which uses the type of daylight cycle and calls
	 * {@link DayNightCycleBasis#writePropertyValueToCycle(BaseProperty)}.
	 * 
	 * @param cycle the base cycle which controls or generates this property
	 * @return the same class but with an updated consumer
	 * 
	 * @see #consumer(ValueConsumer)
	 * @see #consumerOnlyIfNotExists(DayNightCycleBasis)
	 */
	public BaseProperty<B, V> consumer(DayNightCycleBasis cycle) 
	{
		if (cycle != null)
			this.consumer = (prop, val) -> {cycle.writePropertyValueToCycle(prop);}; 
		
		return this;
	}
	/**
	 * As the name suggests, create a consumer only if the consumer in the 
	 * property does not exist.
	 * 
	 * @param cycle the base cycle which controls or generates this property
	 * @return the same class but with an updated consumer, if it exists
	 * 
	 * @see #consumer(ValueConsumer)
	 * @see #consumer(DayNightCycleBasis)
	 * @see #consumerOnlyIfNotExists(TimeChangerScreen)
	 */
	public final BaseProperty<B, V> consumerOnlyIfNotExists(DayNightCycleBasis cycle) 
	{
		if (cycle != null && this.consumer == null)
			this.consumer((prop, val) -> {cycle.writePropertyValueToCycle(prop);}); 
		
		return this;
	}
	/**
	 * Same as {@link #consumerOnlyIfNotExists(DayNightCycleBasis)} except
	 * that it is on the screen to do the job instead.
	 * 
	 * @param cycle the screen which controls everything about the mod
	 * @return the same class but with an updated consumer, if it exists
	 * 
	 * @see #consumer(ValueConsumer)
	 * @see #consumer(DayNightCycleBasis)
	 * @see #consumerOnlyIfNotExists(DayNightCycleBasis)
	 */
	public final BaseProperty<B, V> consumerOnlyIfNotExists(TimeChangerScreen cycle) 
	{
		if (cycle != null && this.consumer == null)
			this.consumer(cycle::consumeChangedProperty); 
		
		return this;
	}
	
	public static interface ValueConsumer<B extends BaseProperty<B, V>, V> 
	{
		void consume(B owningProperty, V newValue);
	}
}
