package jugglestruggle.timechangerstruggle.client.util.render;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.GlyphDrawable;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.ColoredQuadGuiElementRenderState;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.gui.render.state.TextGuiElementRenderState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.text.OrderedText;

/**
 * @author JuggleStruggle
 * @implNote Created on 11-Feb-2022, Friday
 */
public final class RenderUtils
{
	public static void fillPointedGradient(DrawContext ctx, int startX, int startY, int endX, int endY,
		int z, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor)
	{
		ctx.state.addSimpleElement(new ColoredBordersGradientGuiElementRenderState(
			new Matrix3x2f(ctx.getMatrices()), startX, startY, endX, endY, z, 
			topLeftColor, topRightColor, bottomLeftColor, bottomRightColor, 
			ctx.scissorStack.peekLast())
		);
	}
	public static void fillPoint(Matrix4f mat, BufferBuilder bb, int x, int y, int z, int color) 
	{
		float a = (float)(color >> 24 & 0xFF) / 255.0f;
		float r = (float)(color >> 16 & 0xFF) / 255.0f;
		float g = (float)(color >> 8 & 0xFF) / 255.0f;
		float b = (float)(color & 0xFF) / 255.0f;
		
		bb.vertex(mat, x, y, z).color(r, g, b, a);
	}
	

	// Introduced in v0.0.2+1.21.6 port as the previous method of doing the vertices isn't there anymore.
	// Based from the Vanilla's Colored Quad that doesn't allow the ability to color all edges individually.
	public record ColoredBordersGradientGuiElementRenderState
	(
		RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose,
		int x1, int y1, int x2, int y2, int z, int tlCol, int trCol, int blCol, int brCol,
		@Nullable ScreenRect scissorArea, @Nullable ScreenRect bounds
	)
	implements SimpleGuiElementRenderState 
	{
		public ColoredBordersGradientGuiElementRenderState(
			Matrix3x2f pose, int x1, int y1, int x2, int y2, int z, 
			int tlCol, int trCol, int blCol, int brCol, @Nullable ScreenRect scissorArea
		)
		{
			this(RenderPipelines.GUI, TextureSetup.empty(), pose, x1, y1, x2, y2, z, tlCol, trCol, blCol, brCol, 
				scissorArea, ColoredQuadGuiElementRenderState.createBounds(x1, y1, x2, y2, pose, scissorArea));
		}
		
		@Override
		public void setupVertices(VertexConsumer v)
		{
			this.vertex(v, this.x2, this.y1, this.z).color(this.trCol);
			this.vertex(v, this.x1, this.y1, this.z).color(this.tlCol);
			this.vertex(v, this.x1, this.y2, this.z).color(this.blCol);
			this.vertex(v, this.x2, this.y2, this.z).color(this.brCol);
		}
		
		// Introduced in v0.0.3+1.21.9 as the transformation for the Z axis was removed.
		VertexConsumer vertex(VertexConsumer v, float x, float y, float z)
		{
			Vector2f vector2f = this.pose.transformPosition(x, y, new Vector2f());
			return v.vertex(vector2f.x(), vector2f.y(), z);
		}
	}

	// Introduced in v0.0.2+1.21.6 port as the previous method of doing the text rendering isn't there anymore.
	// Force floats instead of integers to be used; this version couldn't be separated into its own class due 
	// to certain draw call functions from the vanilla side of things requiring the superclass.
	public static class TextGuiElementRenderStateDCS extends TextGuiElementRenderState
	{
		public final float fX;
		public final float fY;
		
		public TextGuiElementRenderStateDCS(TextRenderer tr, OrderedText text, Matrix3x2fc mtx,
			float x, float y, int color, int backgroundColor, boolean shadow, boolean includeEmpty, ScreenRect clipBounds)
		{
			super(tr, text, mtx, 0, 0, color, backgroundColor, shadow, includeEmpty, clipBounds);
			this.fX = x; this.fY = y;
		}

		@Override
		public GlyphDrawable prepare()
		{
			if (this.preparation == null) 
			{
				this.preparation = this.textRenderer.prepare(this.orderedText, this.fX, this.fY, 
					this.color, this.shadow, this.trackEmpty, this.backgroundColor);
				ScreenRect rect = this.preparation.getScreenRect();
			
				if (rect != null) 
				{
					rect = rect.transformEachVertex(this.matrix);
					this.bounds = this.clipBounds == null ? rect : this.clipBounds.intersection(rect);
				}
			}

			return this.preparation;
		}
	}
}
