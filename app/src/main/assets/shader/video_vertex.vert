uniform  mat4 uMVPMatirx;
attribute  vec4 vPosition;
attribute  vec2  inputTextureCoordinate;
varying  vec2 textureCoordinate;
void main() {
    gl_Position = uMVPMatirx*vPosition;
    textureCoordinate=inputTextureCoordinate;
}
