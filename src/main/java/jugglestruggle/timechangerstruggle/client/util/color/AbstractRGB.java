package jugglestruggle.timechangerstruggle.client.util.color;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * A base class used exactly to help with RGB colorization rather
 * than create hacky elements when it can be combined all at once!
 *
 * @author JuggleStruggle
 * @implNote Created on 11-Feb-2022, Friday
 */
@Environment(EnvType.CLIENT)
public abstract class AbstractRGB
{
	protected int color;
	protected int previousColor;
	
	public void tick() {}
	
	public int getInterpolatedColor(float delta) {
		return AbstractRGB.getInterpolatedColor(this.previousColor, this.color, delta);
	}
	
	public int getColor() {
		return this.color;
	}
	public int getPrevColor() {
		return this.previousColor;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	public void setPrevColor(int color) {
		this.previousColor = color;
	}
	
	public static int getInterpolatedColor(int prevColor, int color, float delta)
	{
		int currA = (color >> 24 & 0xFF);
		int currR = (color >> 16 & 0xFF);
		int currG = (color >>  8 & 0xFF);
		int currB = (color       & 0xFF);
		
		int prevA = (prevColor >> 24 & 0xFF);
		int prevR = (prevColor >> 16 & 0xFF);
		int prevG = (prevColor >>  8 & 0xFF);
		int prevB = (prevColor       & 0xFF);
		
		int a = (int)((float)prevA + ((float)currA - (float)prevA) * delta);
		int r = (int)((float)prevR + ((float)currR - (float)prevR) * delta);
		int g = (int)((float)prevG + ((float)currG - (float)prevG) * delta);
		int b = (int)((float)prevB + ((float)currB - (float)prevB) * delta);
		
		if (a < 0x00) 
			a = 0x00;
		else if (a > 0xFF) 
			a = 0xFF;
		
		if (r < 0x00) 
			r = 0x00;
		else if (r > 0xFF) 
			r = 0xFF;
		
		if (g < 0x00) 
			g = 0x00;
		else if (g > 0xFF) 
			g = 0xFF;
		
		if (b < 0x00) 
			b = 0x00;
		else if (b > 0xFF) 
			b = 0xFF;
		
		return (a << 24) | (r << 16) | (g << 8) | b;
		
	}
}
