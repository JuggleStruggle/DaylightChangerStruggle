package jugglestruggle.timechangerstruggle.client.util.render;

import jugglestruggle.timechangerstruggle.TimeChangerStruggle;
import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;

import java.io.IOException;
import java.util.Optional;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormatElement.ComponentType;
import net.minecraft.client.render.VertexFormatElement.Type;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;

import com.google.common.collect.ImmutableMap;

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
		
		@Override
		public Optional<Resource> getResource(Identifier id)
		{
			if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE))
			{
				if (id.getPath().contains("shaders/core")) 
				{
				    return Optional.of(new Resource(TimeChangerStruggle.MOD_ID, 
				        () -> TimeChangerStruggleClient.class.getResourceAsStream(BASE_LOCATION + id.getPath())));
				}
			}
			
			return Optional.empty();
		}
	}
}
