#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;

out vec3 worldDir;

void main() {
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;

    // Direction from the camera to this vertex, in world space.
    // It is the same no matter where the block is, so every face
    // shows the same sky, like looking through a window.
    worldDir = IViewRotMat * viewPos.xyz;
}
