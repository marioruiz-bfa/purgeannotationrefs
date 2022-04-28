package org.dyndns.fichtner.purgeannotationrefs.testcode.util;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;

import java.io.*;
import java.net.URL;

public final class TestHelper {

  private TestHelper() {
    super();
  }

  public static byte[] writeClass(String filename, byte[] bytes)
      throws IOException {
    try (FileOutputStream fos = new FileOutputStream(filename)) {
      fos.write(bytes);
    }
    return bytes;
  }

  public static byte[] removeAnno(Class<?> clazz,
                                  AnnotationReferenceRemover remover) throws IOException {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      remover.optimize(classAsStream(clazz), os);
      return os.toByteArray();
    }
  }

  public static InputStream classAsStream(Class<?> clazz) throws IOException {
    URL resource = loadClass(clazz);
    return resource.openStream();
  }

  public static URL loadClass(Class<?> clazz) {
    return clazz
        .getResource("/"
            + (clazz.getName().replace('.', File.separatorChar) + ".class"));
  }

}
