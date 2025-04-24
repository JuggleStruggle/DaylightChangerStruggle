package jugglestruggle.timechangerstruggle.mixin.client.render.gl;

import java.util.Map;
import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gl.CompiledShader;
import net.minecraft.client.gl.CompiledShaderPipeline;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.util.Identifier;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;

/**
 * Done to get access to certain fields and methods in order to create
 * the Rainbow shader which is even more convoluted than both the 
 * 1.18/1.19 port.
 *
 * @author JuggleStruggle
 * @implNote Exclusive to the 1.21.5 port
 */
@Mixin(GlBackend.class)
public interface GlBackendAccessor
{
	@Accessor("pipelineCompileCache")
	Map<RenderPipeline, CompiledShaderPipeline> getPipelineCompileCache();

	@Accessor("defaultShaderSourceGetter")
	BiFunction<Identifier, ShaderType, String> getDefaultShaderSourceGetter();
	
	@Invoker("compileShader")
	CompiledShader dcsCompileShader(Identifier id, ShaderType type, Defines defines, BiFunction<Identifier, ShaderType, String> sourceRetriever);
}
