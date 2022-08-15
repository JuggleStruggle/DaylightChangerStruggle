package jugglestruggle.timechangerstruggle.config.property;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 * This is directly taken from my own legit client (up to you to 
 * believe) and repurposed for this mod.
 * 
 * @author JuggleStruggle
 * @implNote 24-11-2021, Wednesday
 */
public class DoubleValue extends BaseNumber<Double>
{
	public DoubleValue(String propertyName, double defaultValue, Double min, Double max) {
		super(propertyName, defaultValue, min, max);
	}

	@Override
	public boolean isWithinRange() 
	{
		final Double value = this.get();
		return value != null && value >= this.getMin() && value <= this.getMax();
	}
	@Override
	public Double parseStringNumber(String toParse)
	{
		try {
			return Double.parseDouble(toParse);
		} catch (Throwable t) {}
		
		return null;
	}

	@Override
	public ArgumentType<Double> onCommandOptionGetArgType() 
	{
		if (this.min == null || this.max == null)
			return DoubleArgumentType.doubleArg();
		else
			return DoubleArgumentType.doubleArg(this.min, this.max);
	}

	@Override
	public int onCommandOptionWithValueExecute(CommandContext<FabricClientCommandSource> ctx) {
		this.set(DoubleArgumentType.getDouble(ctx, "value")); return 3;
	}
}
