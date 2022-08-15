package jugglestruggle.timechangerstruggle.config.property;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 * 
 *
 * @author JuggleStruggle
 * @implNote Created on 31-Jan-2022, Monday
 */
public class LongValue extends BaseNumber<Long>
{
	public LongValue(String propertyName, long defaultValue, Long min, Long max) {
		super(propertyName, defaultValue, min, max);
	}
	
	@Override
	public boolean isWithinRange() 
	{
		final Long value = this.get();
		return value != null && value >= this.getMin() && value <= this.getMax();
	}
	@Override
	public Long parseStringNumber(String toParse)
	{
		try {
			return Long.parseLong(toParse);
		} catch (Throwable t) {}
		
		return null;
	}

	@Override
	public ArgumentType<Long> onCommandOptionGetArgType() 
	{
		if (this.min == null || this.max == null)
			return LongArgumentType.longArg();
		else
			return LongArgumentType.longArg(this.min, this.max);
	}

	@Override
	public int onCommandOptionWithValueExecute(CommandContext<FabricClientCommandSource> ctx) {
		this.set(LongArgumentType.getLong(ctx, "value")); return 3;
	}
}
