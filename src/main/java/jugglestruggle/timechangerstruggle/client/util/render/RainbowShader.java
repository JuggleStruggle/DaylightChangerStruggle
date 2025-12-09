package jugglestruggle.timechangerstruggle.client.util.render;

import jugglestruggle.timechangerstruggle.TimeChangerStruggle;
import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.mixin.client.render.gl.VertexFormatBuilderAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormatElement.ComponentType;
import net.minecraft.client.render.VertexFormatElement.Usage;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Mixed backport of 0.0.1+1.21.5's Rainbow Shader while accounting from 
 * 0.0.3+1.20.1's version as well.
 * 
 * @author JuggleStruggle
 * @implNote Created on 20-Feb-2022, Sunday
 */
public class RainbowShader extends ShaderProgram
{
	public static final RainbowShader RAINBOW_SHADER;
	public static final RenderPhase.ShaderProgram RAINBOW_SHADER_PROGRAM;
	
	public static final VertexFormat RAINBOW_SHADER_FORMAT;
	public static final VertexFormatElement VFE_OFFSET;
	public static final VertexFormatElement VFE_FLOAT_GENERIC;
	
	public static final RenderLayer.MultiPhase RAINBOW_RL;
	public static final RenderLayer.MultiPhaseParameters RAINBOW_RL_PARAMS;
	
	static
	{
		VFE_OFFSET = new VertexFormatElement(1, 0, ComponentType.FLOAT, Usage.POSITION, 3);
		VFE_FLOAT_GENERIC = new VertexFormatElement(2, 0, ComponentType.FLOAT, Usage.GENERIC, 1);
		
		RAINBOW_SHADER_FORMAT = VertexFormatEx.builderEx()
			.add("aPosition", VertexFormatElement.POSITION)
			.add("aOffset", RainbowShader.VFE_OFFSET)
			.add("aProgress", RainbowShader.VFE_FLOAT_GENERIC)
			.build();
		
		try {
			RAINBOW_SHADER = new RainbowShader();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		RAINBOW_SHADER_PROGRAM = new RenderPhase.ShaderProgram(() -> RAINBOW_SHADER);
		
		RAINBOW_RL_PARAMS = RenderLayer.MultiPhaseParameters.builder()
			.program(RAINBOW_SHADER_PROGRAM)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.build(false);
		
		RAINBOW_RL = RenderLayer.MultiPhase.of(
			"rainbow_shader", RAINBOW_SHADER_FORMAT, 
			VertexFormat.DrawMode.QUADS, 786432, 
			false, true, RAINBOW_RL_PARAMS
		);
	}
	
	// v0.0.3+1.21.1 port: Add empty static method to initialize this class
	public static void start() {
		// empty method to pre-load the shader on game start
	}
	
	
	public final GlUniform strokeWidth;
	public final GlUniform stripeScale;
	public final GlUniform timeOffset;

	public RainbowShader() throws IOException
	{
		super(new ShaderResourceFactory(), "rainbow_shader", RainbowShader.RAINBOW_SHADER_FORMAT);
		
		this.timeOffset  = super.getUniform("uTimeOffset");
		this.strokeWidth = super.getUniform("uStrokeWidth");
		this.stripeScale = super.getUniform("uDashCount");
	}

	static class ShaderResourceFactory implements ResourceFactory
	{
		static final String BASE_LOCATION = "/assets/"+TimeChangerStruggle.MOD_ID+"/";
		
		final DummyResourcePack pack = new DummyResourcePack();

		@Override
		public Optional<Resource> getResource(Identifier id)
		{
			if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && id.getPath().contains("shaders/core")) {
				return Optional.of(new Resource(this.pack, () -> TimeChangerStruggleClient.class.getResourceAsStream(BASE_LOCATION + id.getPath())));
			}
			
			return Optional.empty();
		}
	}

	static class VertexFormatEx extends VertexFormat
	{
		protected VertexFormatEx(List<VertexFormatElement> elements, 
			List<String> names, IntList offsets, int vertexSize)
		{
			super(elements, names, offsets, vertexSize);
			
			int minSize = Math.min(offsets.size(), this.offsetsByElementId.length);
			
			if (minSize > 0)
				System.arraycopy(((IntArrayList)offsets).toIntArray(), 0, this.offsetsByElementId, 0, minSize);
			if (minSize < this.offsetsByElementId.length)
				Arrays.fill(this.offsetsByElementId, minSize, this.offsetsByElementId.length - 1, -1);
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
			public BuilderEx skip(int padding) {
				return (BuilderEx)super.skip(padding);
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
	
	static class DummyResourcePack implements ResourcePack
	{
		@Override
		public String getId() {
			return TimeChangerStruggle.MOD_ID;
		}
		
		@Override
		public InputSupplier<InputStream> openRoot(String... segments) {
			return null;
		}

		@Override
		public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
			return null;
		}

		@Override
		public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
			// not implemented
		}

		@Override
		public Set<String> getNamespaces(ResourceType type) {
			return null;
		}

		@Override
		public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
			return null;
		}

		@Override
		public void close() {}

		@Override
		public ResourcePackInfo getInfo() {
			return null;
		}
	}
}
