package jugglestruggle.timechangerstruggle.config.property;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 * This is directly taken from my own legit client (up to you to 
 * believe) and repurposed for this mod.
 * 
 * @author JuggleStruggle
 * @implNote 23-11-2021, Tuesday
 */
public class IntValue extends BaseNumber<Integer>
{
	public IntValue(String propertyName, int defaultValue, Integer min, Integer max) {
		super(propertyName, defaultValue, min, max);
	}

	@Override
	public boolean isWithinRange() 
	{
		final Integer value = this.get();
		return value != null && value >= this.getMin() && value <= this.getMax();
	}
	@Override
	public Integer parseStringNumber(String toParse)
	{
		try {
			return Integer.parseInt(toParse);
		} catch (Throwable t) {}
		
		return null;
	}

	@Override
	public ArgumentType<Integer> onCommandOptionGetArgType() 
	{
		if (this.min == null || this.max == null)
			return IntegerArgumentType.integer();
		else
			return IntegerArgumentType.integer(this.min, this.max);
	}

	@Override
	public int onCommandOptionWithValueExecute(CommandContext<FabricClientCommandSource> ctx) {
		this.set(IntegerArgumentType.getInteger(ctx, "value")); return 3;
	}
}
