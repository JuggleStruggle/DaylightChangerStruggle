package jugglestruggle.timechangerstruggle.config.property;

import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.client.config.widget.CyclingWidgetConfig;
import jugglestruggle.timechangerstruggle.client.config.widget.WidgetConfigInterface;
import jugglestruggle.timechangerstruggle.client.config.widget.CyclingWidgetConfig.WidgetConfigBuilderEnum;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.util.InterchangeableFunction;

import net.fabricmc.api.EnvType;

import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.client.gui.widget.CyclingButtonWidget;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 31-Jan-2022, Monday
 */
@Environment(EnvType.CLIENT)
public class EnumValue<EV extends Enum<EV>> extends BaseProperty<EnumValue<EV>, EV>
{
	protected final EV[] enumValues;
	
	protected Function<EV, Text> valueToTextFunc;
	protected Predicate<EV> validatePredicate;
	protected InterchangeableFunction<EV, String> readableFunc;
	
	private final CyclingButtonWidget.UpdateCallback<EV> callback = (button, value) -> { 
		if (super.consumer != null)
			super.consumer.consume(this, value);
	};
	
	@SuppressWarnings("unchecked")
	public EnumValue(String property, EV value, EV defaultValue, EV[] values) 
	{
		super(property, value, defaultValue);
		
		assert values != null && values.length > 0;
		
		this.enumValues = values;
		
		if (defaultValue instanceof InterchangeableFunction) {
			this.readableFunc = (InterchangeableFunction<EV, String>)defaultValue;
		} else {
			this.readableFunc = null;
		}
	}

	@Override
	public void set(EV value) 
	{
		if (this.validatePredicate == null || this.validatePredicate.test(value))
			super.value = value;
	}
	
	public EV[] getEnumValues() {
		return this.enumValues;
	}
	public Function<EV, Text> getVTT() {
		return this.valueToTextFunc;
	}
	public Predicate<EV> getEnumValidation() {
		return this.validatePredicate;
	}
	/**
	 * Used to translate enumerator text to a readable text. Used whenever
	 * one wants to switch to a different enumerator.
	 * 
	 * @param func the function used to translate enumerators to text
	 * @return the same class but with an updated field
	 */
	public EnumValue<EV> setVTT(Function<EV, Text> func) {
		this.valueToTextFunc = func; return this;
	}
	/**
	 * Used to translate back and forth about the enumerator being saved 
	 * as a proper string. This might be replaced with enumerators to 
	 * provide it for themselves.
	 * 
	 * <p> If not, then the Deprecated tag will be removed!
	 * 
	 * @param func the function used to translate the enumerator to string 
	 *              and vice versa
	 * @return the same class but with an updated field
	 */
	@Deprecated
	public EnumValue<EV> setSaveLoadEnumFunction(InterchangeableFunction<EV, String> func) {
		this.readableFunc = func; return this;
	}
	/**
	 * Used to perform checks as to whether the enumerator provided is valid.
	 * 
	 * @param predicate the predicate which is going to be used to check
	 * for enumerator validation.
	 * @return the same class but with an updated field
	 */
	public EnumValue<EV> setEnumValidation(Predicate<EV> predicate) {
		this.validatePredicate = predicate; return this;
	}
	
	@Override
	public WidgetConfigInterface<EnumValue<EV>, EV> createConfigElement
	(TimeChangerScreen screen, FancySectionProperty owningSection)
	{
		WidgetConfigBuilderEnum<EV> builder = CyclingWidgetConfig.enumCycle(this);

		Text optionText = null;
		
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
		
		return builder.build(20, 20, optionText, this.callback);
	}

	@Override
	public void readFromJson(JsonElement elem)
	{
		if (!elem.isJsonPrimitive())
			return;
		
		JsonPrimitive prim = elem.getAsJsonPrimitive();
		
		if (!prim.isString())
			return;
		
		String valueName = prim.getAsString();
		EV obtainedValue = null;
		
		if (this.readableFunc == null)
		{
			// At the moment, it is not known how to retrieve
			// enum values using Java API, so we will resort
			// to checking the list of values that enumValues
			// has for us.
			for (EV value : this.enumValues) {
				if (valueName.equals(value.name())) {
					obtainedValue = value; break;
				}
			}
		}
		else {
			obtainedValue = this.readableFunc.applyLeft(valueName);
		}
		
		if (obtainedValue != null)
			this.set(obtainedValue);
	}

	@Override
	public JsonElement writeToJson()
	{
		EV enumToWriteTo;
		
		if (this.validatePredicate == null || this.validatePredicate.test(this.get()))
			enumToWriteTo = this.get();
		else
			enumToWriteTo = this.getDefaultValue();
			
		if (this.readableFunc != null)	
		{
			String namedProp = this.readableFunc.applyRight(enumToWriteTo);
			
			if (namedProp != null && !(namedProp.isEmpty() || namedProp.isBlank()))
				return new JsonPrimitive(namedProp);
		}
		
		return new JsonPrimitive(enumToWriteTo.name());
	}

	@Override
	public ArgumentType<EV> onCommandOptionGetArgType() {
		return new EnumValueArgType<>(this);
	}
	@Override
	public int onCommandOptionWithValueExecute(CommandContext<FabricClientCommandSource> ctx) {
		return 0;
	}
	
	static class EnumValueArgType<EV extends Enum<EV>> implements ArgumentType<EV>
	{
		private final Set<EV> enums;
		private final InterchangeableFunction<EV, String> readableFunc;
		
		private EnumValueArgType(EnumValue<EV> prop) 
		{
			this.readableFunc = prop.readableFunc;
			this.enums = ImmutableSet.copyOf(prop.getEnumValues());
		}

		@Override
		public EV parse(StringReader reader) throws CommandSyntaxException
		{
			int beginningArg = reader.getCursor();
			
			if (!reader.canRead()) {
				reader.skip();
			}
			
			while (reader.canRead() && reader.peek() != ' ') {
				reader.skip();
			}
			
			String val = reader.getString().substring(beginningArg, reader.getCursor());
			String valLow = val.toLowerCase(Locale.ROOT);
			
			Optional<EV> enumFound;
			
			if (this.readableFunc == null)
			{
				enumFound = this.enums.stream()
					.filter(enumerator -> enumerator.name().toLowerCase(Locale.ROOT).equals(valLow))
					.findFirst();
			}
			else
			{
				enumFound = this.enums.stream()
					.filter(enumerator -> this.readableFunc.applyRight(enumerator).equals(val))
					.findFirst();
			}
			
			if (enumFound.isPresent())
				return enumFound.get();
			else
				return null;
		}
		
		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
		{
			final String startingText = builder.getRemainingLowerCase();
			
			if (this.readableFunc == null)
			{
				this.enums.stream()
					.filter(enumerator -> enumerator.name().toLowerCase(Locale.ROOT).startsWith(startingText) )
					.forEach(enumerator -> { builder.suggest(enumerator.name().toLowerCase(Locale.ROOT)); });
			}
			else
			{
				this.enums.stream()
					.filter(enumerator -> this.readableFunc.applyRight(enumerator).startsWith(startingText) )
					.forEach(enumerator -> { builder.suggest(this.readableFunc.applyRight(enumerator)); });
			}
			
			return builder.buildFuture();
		}
	}
}
