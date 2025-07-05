
#ifdef VERTEX

void main() {
	gl_Vertex = vec3(1, 1, 1);
}

#else

void main() {
	gl_Color = vec4(1, 1, 1, 1);
}

#endif