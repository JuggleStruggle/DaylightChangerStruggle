package jugglestruggle.timechangerstruggle.client.util.render;

import jugglestruggle.timechangerstruggle.TimeChangerStruggle;
import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormatElement.ComponentType;
import net.minecraft.client.render.VertexFormatElement.Type;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

import com.google.common.collect.ImmutableMap;

/**
 * @author JuggleStruggle
 * @implNote Created on 20-Feb-2022, Sunday
 */
public class RainbowShader extends ShaderProgram
{
	public static final VertexFormat RAINBOW_SHADER_FORMAT;
	public static final VertexFormatElement FLOAT_GENERIC;
	
	static
	{
		FLOAT_GENERIC = new VertexFormatElement(0, ComponentType.FLOAT, Type.GENERIC, 1);
		
		ImmutableMap.Builder<String, VertexFormatElement> builder = ImmutableMap.builderWithExpectedSize(2);
		
		builder.put("aPosition", VertexFormats.POSITION_ELEMENT);
		builder.put("aOffset", VertexFormats.POSITION_ELEMENT);
		builder.put("aProgress", RainbowShader.FLOAT_GENERIC);
		
		RAINBOW_SHADER_FORMAT = new VertexFormat(builder.build());
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
	
	static class DummyResourcePack implements ResourcePack
	{
		@Override
		public String getName() {
			return TimeChangerStruggle.MOD_ID;
		}
		
		@Override
		public boolean isAlwaysStable() {
			return true;
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
	}
}
