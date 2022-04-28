package org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin;

import org.dyndns.fichtner.purgeannotationrefs.testcode.util.TestHelper;
import org.objectweb.asm.ClassReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;


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
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (PrintWriter pw = new PrintWriter(os)) {
      cr.accept(new JasminifierClassAdapter(pw, null), ClassReader.SKIP_DEBUG
          | ClassReader.EXPAND_FRAMES);
    }
    return os.toString();
  }

}
