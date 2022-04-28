package org.dyndns.fichtner.purgeannotationrefs.optimizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Optimizer so some black magic on java class files.
 *
 * @author Peter Fichtner
 */
public interface ClassOptimizer {

  /**
   * Writes an optimized version to the output stream
   *
   * @param inputStream  stream to read
   * @param outputStream stream to write
   * @throws IOException IO errors
   */
  void optimize(final InputStream inputStream, final OutputStream outputStream)
      throws IOException;

}
