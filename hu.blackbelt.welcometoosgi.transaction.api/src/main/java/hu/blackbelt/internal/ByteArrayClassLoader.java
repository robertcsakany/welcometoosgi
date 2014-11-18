package hu.blackbelt.internal;

public class ByteArrayClassLoader extends ClassLoader {

	public ByteArrayClassLoader(ClassLoader parent) {
		super(parent);
	}
	public Class findClass(String name, byte[] ba) {
		return defineClass(name,ba,0,ba.length);
	}

}