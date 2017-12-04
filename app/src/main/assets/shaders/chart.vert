attribute vec2 vPosition;

uniform float range;
uniform float offset;
varying float magnitude;

void main() {
    magnitude = vPosition.y / max(1.0, range);
    float renderX = vPosition.x - offset;
    if (renderX < -1.0) {
        renderX += 2.0;
    }
    gl_Position = vec4(renderX, magnitude, 0.0, 1.0);
}