#version 150

in vec3 Position;
in vec4 Color;      // red channel = sky index (see SkyBlockItemRenderer)

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

flat out int skyLayer;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    skyLayer = int(Color.r * 255.0 + 0.5);
}
