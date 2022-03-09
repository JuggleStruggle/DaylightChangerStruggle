#version 150

// THIS IS NOT JUGGLESTRUGGLE'S SHADER; it is from riggaroo and was modified to avoid OpenGL 
// ES elements and make it compatible with Minecraft shader formatting
// https://github.com/riggaroo/android-rainbow-opengl/blob/main/app/src/main/java/dev/riggaroo/rainbowbox/Shaders.kt

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float uStrokeWidth;

in vec3 aPosition;

// Offset represents the direction in which this point should be shifted to form the border
in vec3 aOffset;

// Progress changes from 0.0 to 1.0 along the perimeter (does not account for scaling, not yet).
in float aProgress;

out float vProgress;

// This version of normalize() 'correctly' handles zero-length vectors
vec2 safeNormalize(vec2 v) {
    if (length(v) == 0.0) return v;
    return normalize(v);
}

void main() 
{
    vProgress = aProgress;
    vec4 worldPosition = ModelViewMat * vec4(aPosition, 1.0);

    // We need to get the correct direction for the offset that forms the border (the thickness of the bounding box).
    // For that we see where the point ends up in the 'world' coordinates, then correct by aspect ratio to account for scaling,
    // and then normalize. Ta-da, offset direction!
    vec4 offsetPosition = ModelViewMat * vec4(aPosition + aOffset, 1.0);
    vec2 difference = offsetPosition.xy - worldPosition.xy;
    vec4 offset = vec4(safeNormalize(difference) * uStrokeWidth, 0.0, 0.0);
    
    gl_Position = ProjMat * (worldPosition + offset);
}