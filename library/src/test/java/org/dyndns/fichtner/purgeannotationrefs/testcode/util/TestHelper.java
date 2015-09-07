package org.dyndns.fichtner.purgeannotationrefs.testcode.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;

public final class TestHelper {

	private TestHelper() {
		super();
	}

	public static byte[] writeClass(String filename, byte[] bytes)
			throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		try {
			fos.write(bytes);
		} finally {
			fos.close();
		}
		return bytes;
	}

	public static byte[] removeAnno(Class<?> clazz,
			AnnotationReferenceRemover remover) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			remover.optimize(classAsStream(clazz), os);
			return os.toByteArray();
		} finally {
			os.close();
		}
	}

	public static InputStream classAsStream(Class<?> clazz) throws IOException {
		URL resource = loadClass(clazz);
		InputStream openStream = resource.openStream();
		return openStream;
	}

	public static URL loadClass(Class<?> clazz) {
		return clazz
				.getResource("/"
						+ (clazz.getName().replace('.', File.separatorChar) + ".class"));
	}

}
