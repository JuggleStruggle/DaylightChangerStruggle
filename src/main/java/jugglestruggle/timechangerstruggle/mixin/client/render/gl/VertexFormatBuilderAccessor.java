package jugglestruggle.timechangerstruggle.mixin.client.render.gl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import it.unimi.dsi.fastutil.ints.IntList;

/**
 * 
 *
 * @author JuggleStruggle
 * @implNote Exclusive for the 1.21.5 port
 */
@Mixin(VertexFormat.Builder.class)
public interface VertexFormatBuilderAccessor
{
	@Accessor("elements")
	ImmutableMap.Builder<String, VertexFormatElement> getElements();
	
	@Accessor("offsets")
	IntList getOffsets();
	
	@Accessor("offset")
	int getOffset();
}
