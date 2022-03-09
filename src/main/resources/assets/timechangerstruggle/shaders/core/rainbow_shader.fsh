#version 150

// THIS IS NOT JUGGLESTRUGGLE'S SHADER; it is from riggaroo and was modified to avoid OpenGL 
// ES elements and make it compatible with Minecraft shader formatting, my comments are 
// mentioned by my username (JuggleStruggle)
// https://github.com/riggaroo/android-rainbow-opengl/blob/main/app/src/main/java/dev/riggaroo/rainbowbox/Shaders.kt

// uniform float uAspectRatio;
uniform float uTimeOffset;
uniform float uDashCount;

in float vProgress;

out vec4 fragColor;

const vec4 COLORS[13] = vec4[]
(
    vec4(1.0000, 0.0000, 0.0000, 1.0), // JuggleStruggle: Red
    vec4(1.0000, 0.5000, 0.1882, 1.0), // JuggleStruggle: Orange
    vec4(1.0000, 1.0000, 0.0000, 1.0), // JuggleStruggle: Yellow
    vec4(0.5000, 1.0000, 0.1882, 1.0), // JuggleStruggle: Lime
    vec4(0.0000, 1.0000, 0.0000, 1.0), // JuggleStruggle: Green
    vec4(0.1882, 1.0000, 0.5686, 1.0), // JuggleStruggle: Turquiose
    vec4(0.0000, 1.0000, 1.0000, 1.0), // JuggleStruggle: Light Blue
    vec4(0.1882, 0.5020, 1.0000, 1.0), // JuggleStruggle: Sky Blue?? 
    vec4(0.0000, 0.0000, 1.0000, 1.0), // JuggleStruggle: Blue
    vec4(0.5324, 0.1882, 1.0000, 1.0), // JuggleStruggle: Purple
    vec4(1.0000, 0.0000, 1.0000, 1.0), // JuggleStruggle: Magenta
    vec4(1.0000, 0.1882, 0.5020, 1.0), // JuggleStruggle: Reddish Magenta
    vec4(1.0000, 0.0000, 0.0000, 1.0)  // Re-adding the first color to avoid mod() operation after 'colorIndex + 1'
);

float isInRange(float x, float start, float end) {
    return step(start, x) * (1.0 - step(end, x));
}

void main() 
{
    // JuggleStruggle's Comment:
    // Since the original author left a good amount of shader functions unused as in the caculations, uAspectRatio 
    // as a uniform is not used by GL Shader since it does not influence the fragment color. Also, thsee functions were
    // removed to save space so if you want to see them, refer to the original fragment shader source code. Kids, don't
    // forget to read the docs about GL Shaders and how it removes uniforms if not actually in use! :D
    
    // vProgress is interpolated between 0 - 1 by the vertex shader. 
    // We multiply by uTimeOffset to give the animation over time.
    // We multiply uTimeOffset by 16 to make the speed of the animation a bit faster, and 0.125 to stretch out the gradient a bit more.
    // Now bringing it all together into the final progress value that should give a nice smooth gradient along the perimeter.
    float progress = (vProgress + uTimeOffset * 16.0f) * 0.125;
    // There are actually 6 colors, not 7 (JuggleStruggle: No, there are actually 12 colors, not 6!)
    float colorIndex = mod(uDashCount * progress / 4.0, 12.0); 
    vec4 currentColor = COLORS[int(floor(colorIndex))];
    vec4 nextColor = COLORS[int(floor(colorIndex)) + 1];
    // The output colour of the pixel is a mix between the two colors, producing the gradient effect
    fragColor = mix(currentColor, nextColor, fract(colorIndex));
}
