precision mediump float;
varying vec2 textureCoordinate;
uniform  sampler2D vTexture;
uniform  sampler2D bTexture;
//out vec4 fragColor;
void main() {
    vec4 bColor = texture2D(bTexture, textureCoordinate);
    vec4 vColor = texture2D(vTexture, textureCoordinate);
    gl_FragColor =  mix(bColor, vColor, 1);
}
