package org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin;

import static org.objectweb.asm.ClassReader.SKIP_DEBUG;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.dyndns.fichtner.purgeannotationrefs.testcode.util.TestHelper;
import org.objectweb.asm.ClassReader;

public final class JasminUtil {

	private JasminUtil() {
		super();
	}

	public static String dump(String jasmin) {
		System.out.println(jasmin);
		return jasmin;
	}

	public static String classToJasmin(Class<?> clazz) throws IOException {
		return streamToJasmin(TestHelper.classAsStream(clazz));
	}

	public static String streamToJasmin(InputStream classAsStream)
			throws IOException {
		ClassReader cr = new ClassReader(classAsStream);
		int flags = SKIP_DEBUG;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(os);
		try {
			cr.accept(new JasminifierClassAdapter(pw, null), flags
					| ClassReader.EXPAND_FRAMES);
		} finally {
			pw.close();
		}
		return os.toString();
	}

}
