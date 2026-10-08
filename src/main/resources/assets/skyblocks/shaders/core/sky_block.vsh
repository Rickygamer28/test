#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec3 worldDir;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    // Block entity vertices are camera-relative and in world axes (the view
    // rotation lives in ModelViewMat), so Position itself is the world-space
    // direction from the camera to this vertex. Using it directly keeps the
    // sky fixed in the world while the camera turns.
    worldDir = Position;
}
