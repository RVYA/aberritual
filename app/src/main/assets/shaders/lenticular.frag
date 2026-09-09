precision mediump float;

varying vec2 v_TexCoord;

uniform sampler2D u_Texture0;
uniform sampler2D u_Texture1;
uniform sampler2D u_Texture2;
uniform sampler2D u_Texture3;
uniform sampler2D u_Texture4;

uniform int u_LayerCount;
uniform float u_TiltAngle;
uniform int u_Mode;
uniform float u_LPI;
uniform int u_ChromaticAberration;
uniform vec2 u_Resolution;

vec4 sampleLayer(int idx, vec2 uv) {
    if (idx == 0) return texture2D(u_Texture0, uv);
    if (idx == 1) return texture2D(u_Texture1, uv);
    if (idx == 2) return texture2D(u_Texture2, uv);
    if (idx == 3) return texture2D(u_Texture3, uv);
    return texture2D(u_Texture4, uv);
}

vec4 sampleWithAberration(int idx, vec2 uv, float strength) {
    if (strength <= 0.0001) {
        return sampleLayer(idx, uv);
    }
    vec2 offset = vec2(strength * 0.008, 0.0);
    float r = sampleLayer(idx, clamp(uv + offset, 0.0, 1.0)).r;
    float g = sampleLayer(idx, uv).g;
    float b = sampleLayer(idx, clamp(uv - offset, 0.0, 1.0)).b;
    float a = sampleLayer(idx, uv).a;
    return vec4(r, g, b, a);
}

void main() {
    if (u_LayerCount <= 0) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    if (u_LayerCount == 1) {
        gl_FragColor = sampleLayer(0, v_TexCoord);
        return;
    }

    float normTilt = clamp((u_TiltAngle + 1.0) * 0.5, 0.0, 1.0);
    float maxIdx = float(u_LayerCount - 1);
    float continuousIdx = normTilt * maxIdx;
    float aberrationStrength = (u_ChromaticAberration == 1) ? 1.0 : 0.0;

    if (u_Mode == 0) {
        float lensPeriod = max(u_Resolution.x / max(u_LPI * 6.0, 1.0), 2.0);
        float lensPhase = mod(gl_FragCoord.x, lensPeriod) / lensPeriod;
        float shiftedIdx = continuousIdx + (lensPhase - 0.5) * 1.5;
        int activeIdx = int(clamp(floor(shiftedIdx + 0.5), 0.0, maxIdx));
        gl_FragColor = sampleWithAberration(activeIdx, v_TexCoord, aberrationStrength * 0.5);
    } else if (u_Mode == 1) {
        int baseIdx = int(clamp(floor(continuousIdx), 0.0, maxIdx));
        int nextIdx = int(clamp(floor(continuousIdx) + 1.0, 0.0, maxIdx));
        float frac = fract(continuousIdx);

        vec4 colA = sampleWithAberration(baseIdx, v_TexCoord, aberrationStrength * (1.0 - frac));
        vec4 colB = sampleWithAberration(nextIdx, v_TexCoord, aberrationStrength * frac);
        gl_FragColor = mix(colA, colB, frac);
    } else {
        int activeIdx = int(clamp(floor(continuousIdx + 0.5), 0.0, maxIdx));
        gl_FragColor = sampleLayer(activeIdx, v_TexCoord);
    }
}
