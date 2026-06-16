package jugglestruggle.timechangerstruggle.client.timeline;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeInterpolator;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.WorldEnvironmentAttributeAccess;

/**
 * @author JuggleStruggle
 * @implNote Introduced in v0.0.4
 */
public class EnvironmentAttributeInterpolatorDCS extends EnvironmentAttributeInterpolator
{
	public final Camera camera;
	
	public EnvironmentAttributeInterpolatorDCS(Camera camera) {
		this.camera = camera;
	}
	
	@Override @SuppressWarnings("unchecked")
	public <V> V get(EnvironmentAttribute<V> a, float delta)
	{
		if (isExpectedEnvAttribute(a))
		{
			EnvironmentAttributeInterpolator.Entry<V> entry = (Entry<V>)this.entries.get(a);
			
			if (entry == null)
			{
				entry = new EntryDCS<>(this, a);
				this.entries.put(a, entry);
			}
			
			return entry.get(a, delta);	
		}
		
		// If all else fails, use the camera's environment interpolator data.
		return ((DCSCameraProvider)this.camera).getCameraAttributes().get(a, delta);
	}
	
	/**
	 * Invokes {@link #clear()} first then {@link #update()} while remembering 
	 * both the position and the world.
	 */
	public void refresh()
	{
		final World oldWorld = this.world;
		final Vec3d oldPos = this.pos;
		
		this.clear();
		this.update(oldWorld, oldPos);
	}

	/**
	 * Checks if the environment attribute passed through the parameter is one 
	 * being expected by DaylightChangerStruggle.
	 * 
	 * <p> While technically it could choose to modify and keep all environment 
	 * attributes to itself, it does not as the mod's goal is only to focus in 
	 * elements that affect the daylight alone.
	 * 
	 * @param a the environment attribute to check against
	 * @return {@code true} if supported
	 */
	public static boolean isExpectedEnvAttribute(EnvironmentAttribute<?> a)
	{
		return 
			a == EnvironmentAttributes.SUN_ANGLE_VISUAL || a == EnvironmentAttributes.STAR_ANGLE_VISUAL ||
			a == EnvironmentAttributes.MOON_ANGLE_VISUAL || a == EnvironmentAttributes.MOON_PHASE_VISUAL ||
			a == EnvironmentAttributes.STAR_BRIGHTNESS_VISUAL || a == EnvironmentAttributes.SKY_COLOR_VISUAL ||
			a == EnvironmentAttributes.FOG_COLOR_VISUAL || a == EnvironmentAttributes.SKY_LIGHT_COLOR_VISUAL ||
			a == EnvironmentAttributes.WATER_FOG_COLOR_VISUAL || a == EnvironmentAttributes.SKY_LIGHT_FACTOR_VISUAL ||
			a == EnvironmentAttributes.SUNRISE_SUNSET_COLOR_VISUAL || a == EnvironmentAttributes.CLOUD_COLOR_VISUAL ||
			a == EnvironmentAttributes.NIGHT_VISION_COLOR_VISUAL;
	}
	
	public class EntryDCS<V> extends Entry<V>
	{
		public final EnvironmentAttributeInterpolatorDCS instance;
		
		public EntryDCS(EnvironmentAttributeInterpolatorDCS instance, EnvironmentAttribute<V> a)
		{
			super(a); this.instance = instance;
			
			// Compute the values yet again due to it being instantiated from the superclass.
			this.updateCurrentAndLastVal(a);
		}

		/**
		 * Keeps the attribute entry's age as it is.
		 */
		@Override
		protected V compute(final EnvironmentAttribute<V> a)
		{
			if (this.instance != null && this.instance.world != null)
			{
				WorldEnvironmentAttributeAccess.Entry<V> e = this.instance.world.getEnvironmentAttributes().getEntry(a);
				
				if (e != null) 
				{
					TimeChangerStruggleClient.dcsClock.useDcsClock = true;
					TimeChangerStruggleClient.dcsClock.defaults();
					V res = (this.instance.pos == null) ? e.get() : e.getAt(this.instance.pos, this.instance.pool);
					TimeChangerStruggleClient.dcsClock.useDcsClock = false;
					
					return res;
				}
			}
			
			return a.getDefaultValue();
		}
		

		@Override
		public V get(final EnvironmentAttribute<V> a, float delta) 
		{
			if (this.current == null)
				this.updateCurrentAndLastVal(a);

			return a.getType().partialTickLerp().apply(delta, this.last, this.current);
		}

		// The only change in the update() method was to force the previous value to remain as
		// it is instead of trying to catch up to the current value as the cycle itself already
		// handles previous and current. Having this only makes it not be as smooth as it should be.
		@Override
		public boolean update() 
		{
			if (this.current == null)
				return true;

			this.current = null;
			return false;
		}
		
		public void updateCurrentAndLastVal(final EnvironmentAttribute<V> a)
		{
			if (this.instance != null && this.instance.world != null)
			{
				WorldEnvironmentAttributeAccess.Entry<V> e = this.instance.world.getEnvironmentAttributes().getEntry(a);
				
				if (e != null)
				{
					TimeChangerStruggleClient.dcsClock.useDcsClock = true;
					
					// Modifies the entry's age first, ticks to then clear the cached value, gets a representation and
					// tries again for 'current'. This is to ensure forced update through TrackAttributeModification and 
					// the entry from returning the 'last' value. The age is subtracted by 2 first for the previous, 
					// then added 1 again via the tick() method and does the same thing again so it's always back to its 
					// initial value.
					
					TimeChangerStruggleClient.dcsClock.forExecutor = DayNightGetterType.getCorrespondingFromEnvAttr(a);
					TimeChangerStruggleClient.dcsClock.forPreviousTime = true; e.age -= 3; 
					
					// Note: Exclusive to 26.1 port (attribute access's age being reduced by 3, invoking a tick and an 
					// empty get) due to causing a flickering bug essentially being caused as a result of the DCS clock 
					// either not being applied or only getting a cached element for the 'last' value which ends up being
					// the world's value time being applied. This does not happen normally in the development environment 
					// and was tested in a non-development environment with Fabric API and this is how that bug was 
					// presented through.
					e.tick(); e.get();
					
					// Tick forward to clear and get the real previous value.
					e.tick();
					this.last = (this.instance.pos == null) ? e.get() : e.getAt(this.instance.pos, this.instance.pool);
					
					TimeChangerStruggleClient.dcsClock.forPreviousTime = false; e.tick();
					this.current = (this.instance.pos == null) ? e.get() : e.getAt(this.instance.pos, this.instance.pool);
					
					TimeChangerStruggleClient.dcsClock.useDcsClock = false;
					return;
				}
				
			}
			
			this.last = this.current = a.getDefaultValue();
		}
	}
}
