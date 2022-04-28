package org.dyndns.fichtner.purgeannotationrefs.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtilities {

  private FileUtilities() {
    super();
  }

  public static void copy(File source, File destination)
      throws IOException {
    if (source.isDirectory()) {
      copyDirectory(source, destination);
    } else {
      copyFile(source, destination);
    }
  }

  public static void copyDirectory(File source, File destination)
      throws IOException {
    if (!source.isDirectory()) {
      throw new IllegalArgumentException("Source (" + source.getPath()
          + ") must be a directory.");
    }

    if (!destination.isDirectory()) {
      throw new IllegalArgumentException("Destination (" + destination.getPath()
          + ") must be a directory.");
    }

    if (!source.exists()) {
      throw new IllegalArgumentException("Source directory ("
          + source.getPath() + ") doesn't exist.");
    }

    if (destination.exists()) {
      throw new IllegalArgumentException("Destination ("
          + destination.getPath() + ") exists.");
    }

    destination.mkdirs();
    File[] files = source.listFiles();

    for (File file : files) {
      if (file.isDirectory()) {
        copyDirectory(file, new File(destination, file.getName()));
      } else {
        copyFile(file, new File(destination, file.getName()));
      }
    }
  }

  public static void copyFile(File source, File destination)
      throws IOException {
    try (FileChannel sourceChannel = new FileInputStream(source).getChannel()) {
      try (FileChannel targetChannel = new FileOutputStream(destination)
          .getChannel()) {
        sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
      }
    }
  }

}