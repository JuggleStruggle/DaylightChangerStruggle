package jugglestruggle.timechangerstruggle.client.util.render;

import net.minecraft.util.math.Matrix4f;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 11-Feb-2022, Friday
 */
public final class RenderUtils
{
	public static RainbowShader rainbowAllTheWay;
	
	public static void fillPointedGradient(MatrixStack matrices, int startX, int startY, int endX, int endY,
		int z, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor)
	{
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		
		final Tessellator tess = Tessellator.getInstance();
		final BufferBuilder bb = tess.getBuffer();
		final Matrix4f mat = matrices.peek().getPositionMatrix();
		
		bb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		
		RenderUtils.fillPoint(mat, bb, endX, startY, z, topRightColor);
		RenderUtils.fillPoint(mat, bb, startX, startY, z, topLeftColor);
		RenderUtils.fillPoint(mat, bb, startX, endY, z, bottomLeftColor);
		RenderUtils.fillPoint(mat, bb, endX, endY, z, bottomRightColor);
		
		tess.draw();
		
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
	public static void fillPoint(Matrix4f mat, BufferBuilder bb, int x, int y, int z, int color) 
	{
		float a = (float)(color >> 24 & 0xFF) / 255.0f;
		float r = (float)(color >> 16 & 0xFF) / 255.0f;
		float g = (float)(color >> 8 & 0xFF) / 255.0f;
		float b = (float)(color & 0xFF) / 255.0f;
		
		bb.vertex(mat, x, y, z).color(r, g, b, a).next();
	}
	
	public static void fillRainbow
	(
		MatrixStack matrices, int startX, int startY, int endX, int endY, int z,
		float offsetX, float offsetY, float offsetZ, float progress, boolean adv
	)
	{
		if (!adv)
		{
			RenderSystem.enableBlend();
			RenderSystem.disableTexture();
			RenderSystem.defaultBlendFunc();
		}
		
		
		
		RenderSystem.setShader(() -> RenderUtils.rainbowAllTheWay);
		
		final Tessellator tess = Tessellator.getInstance();
		final BufferBuilder bb = tess.getBuffer();
		final Matrix4f mat = matrices.peek().getPositionMatrix();
		
		bb.begin(VertexFormat.DrawMode.QUADS, RainbowShader.RAINBOW_SHADER_FORMAT);
		
		float width  = endX - startX;
		float height = endY - startY;
		// Width over Height
		float ratioW = width / height;
		// Height over Width
		float ratioH = height / width;
		
		float topLeftProgress = 0.0f;
		float topRghtProgress = 0.5f;
		float btmLeftProgress = 0.5f;
		float btmRghtProgress = 1.0f;
		
		// Width is higher than height
		if (ratioW > 1.0f) 
		{
			topRghtProgress = 0.5f * ratioW;
			btmLeftProgress = 0.5f;
			btmRghtProgress = topRghtProgress + 0.5f;
		}
		// Height is higher than width
		else if (ratioW < 1.0f)
		{
			topRghtProgress = 0.5f;
			btmLeftProgress = 0.5f * ratioH;
			btmRghtProgress = btmLeftProgress + 0.5f;
		}
		
		RenderUtils.fillRainbowPoint(mat, bb,   endX, startY, z, offsetX, offsetY, offsetZ, progress + topRghtProgress);
		RenderUtils.fillRainbowPoint(mat, bb, startX, startY, z, offsetX, offsetY, offsetZ, progress + topLeftProgress);
		RenderUtils.fillRainbowPoint(mat, bb, startX,   endY, z, offsetX, offsetY, offsetZ, progress + btmLeftProgress);
		RenderUtils.fillRainbowPoint(mat, bb,   endX,   endY, z, offsetX, offsetY, offsetZ, progress + btmRghtProgress);
		
		tess.draw();
		
		
		
		if (!adv)
		{
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
		}
	}
	
	public static void fillRainbowPoint(Matrix4f mat, BufferBuilder bb, int x, int y, int z, 
		float offsetX, float offsetY, float offsetZ, float progress) 
	{
		bb.vertex(mat, x, y, z);
		bb.vertex(mat, offsetX, offsetY, offsetZ);
		
		bb.putFloat(0, progress); bb.nextElement();
		
		bb.next();
	}
}
