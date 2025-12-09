package jugglestruggle.timechangerstruggle.mixin.client.render.gl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.ints.IntList;

/**
 * @author JuggleStruggle
 * @implNote Used in 1.21.1 and 1.21.5 ports
 */
@Mixin(VertexFormat.Builder.class)
public interface VertexFormatBuilderAccessor
{
	@Accessor("elements")
	ImmutableMap.Builder<String, VertexFormatElement> getElements();
	
	@Accessor("offsets")
	IntList getOffsets();
	
	@Accessor("currentOffset")
	int getOffset();
}
