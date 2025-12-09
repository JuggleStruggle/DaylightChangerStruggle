package jugglestruggle.timechangerstruggle.client.util.render;

import jugglestruggle.timechangerstruggle.mixin.client.render.BufferBuilderAccessor;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * @author JuggleStruggle
 * @implNote Created on 11-Feb-2022, Friday
 */
public final class RenderUtils
{
	public static void fillPointedGradient(DrawContext ctx, int startX, int startY, int endX, int endY,
		int z, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor)
	{
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
//		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		
		final BufferBuilder bb = (BufferBuilder)ctx.getVertexConsumers().getBuffer(RenderLayer.getGui());
		final Matrix4f mat = ctx.getMatrices().peek().getPositionMatrix();
		
		RenderUtils.fillPoint(mat, bb, endX, startY, z, topRightColor);
		RenderUtils.fillPoint(mat, bb, startX, startY, z, topLeftColor);
		RenderUtils.fillPoint(mat, bb, startX, endY, z, bottomLeftColor);
		RenderUtils.fillPoint(mat, bb, endX, endY, z, bottomRightColor);
		
		ctx.draw();
		
		RenderSystem.disableBlend();
	}
	public static void fillPoint(Matrix4f mat, BufferBuilder bb, int x, int y, int z, int color) 
	{
		float a = (float)(color >> 24 & 0xFF) / 255.0f;
		float r = (float)(color >> 16 & 0xFF) / 255.0f;
		float g = (float)(color >> 8 & 0xFF) / 255.0f;
		float b = (float)(color & 0xFF) / 255.0f;
		
		bb.vertex(mat, x, y, z).color(r, g, b, a);
	}
	
	public static void fillRainbow
	(
		DrawContext ctx, int startX, int startY, int endX, int endY, int z,
		float offsetX, float offsetY, float offsetZ, float progress, boolean adv
	)
	{
		if (!adv) {
			RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
		}
		
		final BufferBuilder bb = (BufferBuilder)ctx.getVertexConsumers().getBuffer(RainbowShader.RAINBOW_RL);
		final Matrix4f mat = ctx.getMatrices().peek().getPositionMatrix();
		
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
		
		ctx.draw();
		
		if (!adv)
			RenderSystem.disableBlend();
	}
	
	public static void fillRainbowPoint(Matrix4f mat, BufferBuilder bb, int x, int y, int z, 
		float offsetX, float offsetY, float offsetZ, float progress) 
	{
		bb.vertex(mat, x, y, z);
		RenderUtils.rainbowVertexPos(bb, mat, offsetX, offsetY, offsetZ);
		RenderUtils.rainbowFloatGeneric(bb, progress);
	}

	static void rainbowVertexPos(BufferBuilder bb, Matrix4f mat, float x, float y, float z)
	{
		long p = ((BufferBuilderAccessor)bb).getBeginElement(RainbowShader.VFE_OFFSET);
		
		if (p == -1L)
			return;
		
		Vector3f v = mat.transformPosition(x, y, z, new Vector3f());
		MemoryUtil.memPutFloat(p     , v.x());
		MemoryUtil.memPutFloat(p + 4L, v.y());
		MemoryUtil.memPutFloat(p + 8L, v.z());
	}

	static void rainbowFloatGeneric(BufferBuilder bb, float v)
	{
		long p = ((BufferBuilderAccessor)bb).getBeginElement(RainbowShader.VFE_FLOAT_GENERIC);
		
		if (p != -1L)
			MemoryUtil.memPutFloat(p, v);
	}
}
