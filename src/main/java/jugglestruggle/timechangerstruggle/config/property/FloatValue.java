package jugglestruggle.timechangerstruggle.config.property;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 * This is directly taken from my own legit client (up to you to 
 * believe) and repurposed for this mod.
 * 
 * @author JuggleStruggle
 * @implNote 24-11-2021, Wednesday
 */
public class FloatValue extends BaseNumber<Float>
{
	public FloatValue(String propertyName, float defaultValue, Float min, Float max) {
		super(propertyName, defaultValue, min, max);
	}
	
	@Override
	public boolean isWithinRange() 
	{
		final Float value = this.get();
		return value != null && value >= this.getMin() && value <= this.getMax();
	}
	@Override
	public Float parseStringNumber(String toParse)
	{
		try {
			return Float.parseFloat(toParse);
		} catch (Throwable t) {}
		
		return null;
	}
	

	@Override
	public ArgumentType<Float> onCommandOptionGetArgType() 
	{
		if (this.min == null || this.max == null)
			return FloatArgumentType.floatArg();
		else
			return FloatArgumentType.floatArg(this.min, this.max);
	}

	@Override
	public int onCommandOptionWithValueExecute(CommandContext<FabricClientCommandSource> ctx) {
		this.set(FloatArgumentType.getFloat(ctx, "value")); return 3;
	}
	
	/*
	@Override
	public boolean readFromValue(String value) 
	{
		try 
		{
			this.value = Float.parseFloat(value);
			return true;
		} 
		catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		
		return false;
	}

	@Override
	public String getValueAsString() {
		return this.value.toString();
	}
	 */
}
