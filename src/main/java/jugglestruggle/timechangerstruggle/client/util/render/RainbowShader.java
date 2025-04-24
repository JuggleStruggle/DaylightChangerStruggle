package jugglestruggle.timechangerstruggle.client.util.render;


import jugglestruggle.timechangerstruggle.TimeChangerStruggle;
import jugglestruggle.timechangerstruggle.mixin.client.render.gl.GlBackendAccessor;
import jugglestruggle.timechangerstruggle.mixin.client.render.gl.GlResourceManagerAccessor;
import jugglestruggle.timechangerstruggle.mixin.client.render.gl.VertexFormatBuilderAccessor;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import org.joml.Matrix4f;

import net.minecraft.client.gl.CompiledShader;
import net.minecraft.client.gl.CompiledShaderPipeline;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.UniformDescription;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;


/**
 * A convoluted mess of a shader that hosts its own render layer, pipeline
 * and its own program for the 1.21.5 port. All for the sake of ensuring mod
 * compatibility.
 *
 * @author JuggleStruggle
 * @implNote Created on 20-Feb-2022, Sunday
 */
public class RainbowShader extends ShaderProgram
{
	public static final VertexFormatEx RAINBOW_SHADER_FORMAT;
	public static final VertexFormatElement VFE_OFFSET;
	public static final VertexFormatElement VFE_FLOAT_GENERIC;

	public static final MultiPhaseRainbow RAINBOW_RL;
	public static final RenderPipeline RAINBOW_PIPELINE;
	// public static final RenderPipeline.Snippet RAINBOW_PIPELINE_SNIPPET;
	
	public static float uStrokeWidth = 0.0f;
	public static float uStripeScale = 1.0f;
	public static float uTimeOffset = 0.0f;
	
	static
	{
		VFE_OFFSET = new VertexFormatElement(1, 0,
			VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
		VFE_FLOAT_GENERIC = new VertexFormatElement(2, 0, 
			VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);

		RAINBOW_SHADER_FORMAT = VertexFormatEx.builderEx()
			.add("aPosition", VertexFormatElement.POSITION)
			.add("aOffset", RainbowShader.VFE_OFFSET)
			.add("aProgress", RainbowShader.VFE_FLOAT_GENERIC)
			.build();
		
		Identifier rsLoc = Identifier.of(TimeChangerStruggle.MOD_ID, "core/rainbow_shader");
		
		RAINBOW_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_SNIPPET)
			.withLocation(rsLoc).withVertexShader(rsLoc).withFragmentShader(rsLoc)
			.withUniform("uStrokeWidth", UniformType.FLOAT)
			.withUniform("uDashCount", UniformType.FLOAT)
			.withUniform("uTimeOffset", UniformType.FLOAT)
			.withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO))
			.withVertexFormat(RAINBOW_SHADER_FORMAT, DrawMode.QUADS)
			.build();
		
		RAINBOW_RL = new MultiPhaseRainbow();
	}
	
	public GlUniform strokeWidth;
	public GlUniform stripeScale;
	public GlUniform timeOffset;

	public RainbowShader(int glRef) {
		super(glRef, "rainbow_shader");
	}

	@Override
	public void set(List<UniformDescription> uniforms, List<String> samplers)
	{
		super.set(uniforms, samplers);
		
		this.strokeWidth = super.getUniform("uStrokeWidth");
		this.stripeScale = super.getUniform("uDashCount");
		this.timeOffset = super.getUniform("uTimeOffset");
	}
	
	@Override
	public void initializeUniforms(DrawMode drawMode, Matrix4f viewMatrix, 
		Matrix4f projectionMatrix, float screenWidth, float screenHeight)
	{
		super.initializeUniforms(drawMode, viewMatrix, projectionMatrix, screenWidth, screenHeight);
		
		this.timeOffset.set(RainbowShader.uTimeOffset);
		this.strokeWidth.set(RainbowShader.uStrokeWidth);
		this.stripeScale.set(RainbowShader.uStripeScale);
	}
	
	
	static void compilePipeline()
	{
		GlResourceManagerAccessor grm = (GlResourceManagerAccessor)RenderSystem.getDevice().createCommandEncoder();
		
		GlBackend gb = grm.getBackend();
		((GlBackendAccessor)gb).getPipelineCompileCache().computeIfAbsent(RAINBOW_PIPELINE, p -> RainbowShader.compileRenderPipeline(gb));
	}
	
	// Here comes a full method copy all for the sake of getting access to creating the Rainbow Shader.
	static CompiledShaderPipeline compileRenderPipeline(GlBackend gb) 
	{
		RenderPipeline pipeline = RAINBOW_PIPELINE; GlBackendAccessor gba = (GlBackendAccessor)gb;
		BiFunction<Identifier, ShaderType, String> sourceRetriever = gba.getDefaultShaderSourceGetter();
		
		CompiledShader compiledShader = gba.dcsCompileShader(pipeline.getVertexShader(), ShaderType.VERTEX, pipeline.getShaderDefines(), sourceRetriever);
		CompiledShader compiledShader2 = gba.dcsCompileShader(pipeline.getFragmentShader(), ShaderType.FRAGMENT, pipeline.getShaderDefines(), sourceRetriever);
		
		if (compiledShader == CompiledShader.INVALID_SHADER) 
		{
			TimeChangerStruggle.LOGGER.error("Couldn't compile pipeline {}: vertex shader {} was invalid", pipeline.getLocation(), pipeline.getVertexShader());
			return new CompiledShaderPipeline(pipeline, ShaderProgram.INVALID);
		} 
		else if (compiledShader2 == CompiledShader.INVALID_SHADER) 
		{
			TimeChangerStruggle.LOGGER.error("Couldn't compile pipeline {}: fragment shader {} was invalid", pipeline.getLocation(), pipeline.getFragmentShader());
			return new CompiledShaderPipeline(pipeline, ShaderProgram.INVALID);
		} 
		else 
		{
			ShaderProgram shaderProgram;
			
			try 
			{
				shaderProgram = ShaderProgram.create(compiledShader, compiledShader2, pipeline.getVertexFormat(), pipeline.getLocation().toString());
				shaderProgram = new RainbowShader(shaderProgram.getGlRef());
			} 
			catch (ShaderLoader.LoadException var7) 
			{
				TimeChangerStruggle.LOGGER.error("Couldn't compile program for pipeline {}: {}", pipeline.getLocation(), var7);
				return new CompiledShaderPipeline(pipeline, ShaderProgram.INVALID);
			}

			shaderProgram.set(pipeline.getUniforms(), pipeline.getSamplers());
			gb.getDebugLabelManager().labelShaderProgram(shaderProgram);
			
			return new CompiledShaderPipeline(pipeline, shaderProgram);
		}
	}
	
	public static class MultiPhaseRainbow extends RenderLayer.MultiPhase
	{
		boolean cacheRainbowShader;
		
		public MultiPhaseRainbow()
		{
			super("rainbow_shader", 786432, false, true, 
				RAINBOW_PIPELINE, RenderLayer.MultiPhaseParameters.builder().build(false));
			
			this.cacheRainbowShader = true;
		}
		
		@Override
		public void draw(BuiltBuffer buffer)
		{
			// Cache and compile the shader before drawing it in order to load
			// the Rainbow Shader correctly. Not the best way but at least shall
			// help on times when it matters.
			if (this.cacheRainbowShader)
			{
				RainbowShader.compilePipeline();
				this.cacheRainbowShader = false;
			}
			
			super.draw(buffer);
		}
		
		public void resetCachedState() {
			this.cacheRainbowShader = true;
		}
	}
	
	static class VertexFormatEx extends VertexFormat
	{
		protected VertexFormatEx(List<VertexFormatElement> elements, 
			List<String> names, IntList offsets, int vertexSize)
		{
			super(elements, names, offsets, vertexSize);
			
			int minSize = Math.min(offsets.size(), this.offsetsByElement.length);
			
			if (minSize > 0)
				System.arraycopy(((IntArrayList)offsets).toIntArray(), 0, this.offsetsByElement, 0, minSize);
			if (minSize < this.offsetsByElement.length)
				Arrays.fill(this.offsetsByElement, minSize, this.offsetsByElement.length - 1, -1);
		}
		
		public static BuilderEx builderEx() {
			return new BuilderEx();
		}
		
		static class BuilderEx extends VertexFormat.Builder
		{
			@Override
			public BuilderEx add(String name, VertexFormatElement element) {
				return (BuilderEx)super.add(name, element);
			}

			@Override
			public BuilderEx padding(int padding) {
				return (BuilderEx)super.padding(padding);
			}
			
			@Override
			public VertexFormatEx build() 
			{
				VertexFormatBuilderAccessor vfb = (VertexFormatBuilderAccessor)this;
				
				ImmutableMap<String, VertexFormatElement> elems = vfb.getElements().buildOrThrow();
				return new VertexFormatEx(elems.values().asList(), elems.keySet().asList(), vfb.getOffsets(), vfb.getOffset());
			}
		}
	}
	
	
	/*
	static class RenderPassRainbow extends RenderPassImpl
	{
		public RenderPassRainbow(GlResourceManager rm, boolean hasDepth)
		{
			super(rm, hasDepth);
		}
		
	}
	
	static class ShaderResourceFactory implements ResourceFactory
	{
		static final String BASE_LOCATION = "/assets/" + TimeChangerStruggle.MOD_ID + "/";

		@Override
		public Optional<Resource> getResource(Identifier id)
		{

			if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && id.getPath().contains("shaders/core"))
			{
				return Optional.of(new Resource(TimeChangerStruggle.MOD_ID,
					() -> TimeChangerStruggleClient.class.getResourceAsStream(BASE_LOCATION + id.getPath())));
			}

			return Optional.empty();
		}
	}
	*/
}
