package jugglestruggle.timechangerstruggle.client.util.render;

import jugglestruggle.timechangerstruggle.TimeChangerStruggle;
import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormatElement.DataType;
import net.minecraft.client.render.VertexFormatElement.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * 
 *
 * @author JuggleStruggle
 * @implNote Created on 20-Feb-2022, Sunday
 */
public class RainbowShader extends Shader
{
	public static final VertexFormat RAINBOW_SHADER_FORMAT;
	public static final VertexFormatElement FLOAT_GENERIC;
	
	static
	{
		FLOAT_GENERIC = new VertexFormatElement(0, DataType.FLOAT, Type.GENERIC, 1);
		
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
		super(new ShaderResourceManager(), "rainbow_shader", RainbowShader.RAINBOW_SHADER_FORMAT);
		
		this.timeOffset  = super.getUniform("uTimeOffset");
		this.strokeWidth = super.getUniform("uStrokeWidth");
		this.stripeScale = super.getUniform("uDashCount");
	}

	static class ShaderResourceManager implements ResourceManager
	{
		static final String BASE_LOCATION = "/assets/"+TimeChangerStruggle.MOD_ID+"/";
		
		@Override
		public Resource getResource(Identifier id) throws IOException
		{
			if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE))
			{
				if (id.getPath().contains("shaders/core"))
				{
					InputStream resource = TimeChangerStruggleClient.class
						.getResourceAsStream(BASE_LOCATION + id.getPath());
					
					if (resource != null) 
					{
						Identifier newId = new Identifier(TimeChangerStruggle.MOD_ID, id.getPath());
						return new ResourceImpl(TimeChangerStruggle.MOD_ID, newId, resource, null);
					}
				}
			}
			
			return null;
		}

		@Override
		public Set<String> getAllNamespaces() {
			return ImmutableSet.of(TimeChangerStruggle.MOD_ID);
		}

		@Override
		public boolean containsResource(Identifier id) {
			return true;
		}

		@Override
		public List<Resource> getAllResources(Identifier id) throws IOException {
			return null;
		}

		@Override
		public Collection<Identifier> findResources(String startingPath, Predicate<String> pathPredicate) {
			return null;
		}

		@Override
		public Stream<ResourcePack> streamResourcePacks() {
			return null;
		}
	}
}
