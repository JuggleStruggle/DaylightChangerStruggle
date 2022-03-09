package jugglestruggle.timechangerstruggle.daynight.type;

import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import net.minecraft.client.world.ClientWorld;

/**
 * ¯\_(ツ)_/¯
 *
 * @author JuggleStruggle
 * @implNote Created on 17-Feb-2022, Thursday
 */
public class ShrugTime implements DayNightCycleBasis
{

	@Override
	public long getModifiedTime(ClientWorld world, DayNightGetterType executor, boolean previous)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCachedTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Class<?> getBuilderClass() {
		return Builder.class;
	}


	public static class Builder implements DayNightCycleBuilder
	{
		@Override
		public DayNightCycleBasis create() {
			return new ShrugTime();
		}

		@Override
		public String getKeyName() {
			return "shrugtime";
		}
		
		@Override
		public Text getTranslatableName() {
			return new LiteralText("¯\\_(ツ)_/¯");
		}
		@Override
		public Text getTranslatableDescription() {
			return new TranslatableText("jugglestruggle.tcs.dnt.shrugtime.description");
		}
	}
}
