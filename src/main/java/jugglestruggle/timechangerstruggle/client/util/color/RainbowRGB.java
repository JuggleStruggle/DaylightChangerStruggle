package jugglestruggle.timechangerstruggle.client.util.color;

import jugglestruggle.timechangerstruggle.util.EasingType;
import jugglestruggle.timechangerstruggle.util.Easings;

/**
 * 
 *
 * @author JuggleStruggle
 * @implNote Created on 11-Feb-2022, Friday
 */
public class RainbowRGB extends AbstractRGB
{
	public static RainbowRGB[] createColors(int... startingColors) 
	{
		if (startingColors == null || startingColors.length <= 0) {
			throw new NullPointerException("Cannot create colors with an empty parameter");
		} 
		else 
		{
			RainbowRGB[] chromaColors = new RainbowRGB[startingColors.length];
			for (int i = 0; i < startingColors.length; ++i) {
				chromaColors[i] = new RainbowRGB(startingColors[i]);
			}
			return chromaColors;
		}
	}
	
	public Easings interpolation;
	public EasingType easingType;
	
	public int ticks;
	public int ticksForNextUpdate;
	
	/**
	 * Target color is defined as:
	 * 
	 * <ul>
	 * <li> 0 = Rising Red 
	 * <li> 1 = Falling Blue
	 * <li> 2 = Rising Green
	 * <li> 3 = Falling Red 
	 * <li> 4 = Rising Blue
	 * <li> 5 = Falling Green
	 * </ul>
	 */
	private byte targetColor = 0;
	public boolean reverseTargetColor;
	
	private int previousChromaColor;
	private int currentChromaColor;
	
	public RainbowRGB(int startingColor)
	{
		this.previousChromaColor = this.currentChromaColor =
		this.previousColor = this.color = startingColor;
		
		this.interpolation = Easings.QUAD;
		this.easingType = EasingType.BETWEEN;
		
		// If this field is set to ticksForNextUpdate, it is assumed 
		// that it updates the colors then resets it back to 0
		// this.ticks = 20;
		// After the chroma colors are updated, ticks is set to 0.
		// Helps with interpolation combined with easings
		this.ticksForNextUpdate = 20;
		
		this.tickSelf(true);
	}
	
	public RainbowRGB setTicks(int ticks) {
		this.ticks = ticks; return this;
	}
	public RainbowRGB setTicksForNextUpdate(int ticks) {
		this.ticksForNextUpdate = ticks; return this;
	}
	
	@Override
	public void setColor(int color)
	{
		super.setColor(color);
	}
	@Override
	public void setPrevColor(int color)
	{
		super.setPrevColor(color);
	}
	
	@Override
	public void tick() {
		this.tickSelf(false);
	}
	
	public void tickSelf(boolean forceNextUpdate)
	{
		if (this.ticks >= this.ticksForNextUpdate || forceNextUpdate)
		{
			this.ticks = 0;
			
			this.previousChromaColor = this.currentChromaColor;
			
			int a = (this.currentChromaColor >> 24 & 0xFF);
			int r = (this.currentChromaColor >> 16 & 0xFF);
			int g = (this.currentChromaColor >>  8 & 0xFF);
			int b = (this.currentChromaColor       & 0xFF);
			
			// Used to keep track of how many tries it took in attempting
			// to switch the colors where there is a chance that it might
			// softlock the client over something meaningless
			int tries = 0; 
			
			while (true)
			{
				boolean exitLoop = true;
				final byte previousTargetColor = this.targetColor;
				
				switch (this.targetColor)
				{
					case 0: // Rising Red
					{
						if (r >= 0xFF) 
						{
							// Check if there are other colors with at least
							// a higher than or equal to 128
							if (g >= 128 || b >= 128)
								this.targetColor = 3;
							
							++tries;
							exitLoop = false;
						}
						else
						{
							r = 0xFF;
						}
						
						break;
					}
					case 1: // Falling Blue
					{
						if (b <= 0x00) 
						{
							if (g < 128 || r < 128)
								this.targetColor = 4;
							
							++tries;
							exitLoop = false;
						}
						else
						{
							b = 0x00;
						}
						
						break;
					}
					case 2: // Rising Green
					{
						if (g >= 0xFF) 
						{
							if (r >= 128 || b >= 128)
								this.targetColor = 5;
							
							++tries;
							exitLoop = false;
						}
						else
						{
							g = 0xFF;
						}
						
						break;
					}
					case 3: // Falling Red
					{
						if (r <= 0x00) 
						{
							if (g < 128 || b < 128)
								this.targetColor = 0;
							
							++tries;
							exitLoop = false;
						}
						else
						{
							r = 0x00;
						}
						
						break;
					}
					case 4: // Rising Blue
					{
						if (b >= 0xFF) 
						{
							if (r >= 128 || g >= 128)
								this.targetColor = 1;
							
							++tries;
							exitLoop = false;
						}
						else
						{
							b = 0xFF;
						}
						
						break;
					}
					case 5: // Falling Green
					{
						if (g <= 0x00) 
						{
							if (r < 128 || b < 128)
								this.targetColor = 2;
						
							++tries;
							exitLoop = false;
						}
						else
						{
							g = 0x00;
						}
						
						break;
					}
				}
				

				// Use the next target color if it wasn't changed
				if (this.targetColor == previousTargetColor)
				{
					if (this.reverseTargetColor)
					{
						if (this.targetColor <= 0)
							this.targetColor = 5;
						else
							--this.targetColor;
					}
					else
					{
						if (this.targetColor >= 5)
							this.targetColor = 0;
						else
							++this.targetColor;
					}
				}
				
				if (exitLoop || tries > 3)
					break;
			}
			
			
			this.currentChromaColor = (a << 24) | (r << 16) | (g << 8) | b;
			
			this.previousColor = this.color;
			this.color = this.previousChromaColor;
		}
		else
		{
			this.previousColor = this.color;
			
			float delta = (float)this.ticks / (float)this.ticksForNextUpdate;
			float result = (float)this.interpolation.value(this.easingType, delta);
			
			this.color = AbstractRGB.getInterpolatedColor(this.previousChromaColor, this.currentChromaColor, result);
			
			++this.ticks;
		}
	}
}

