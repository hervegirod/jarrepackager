/*
Copyright (c) 2023 Herve Girod
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies,
either expressed or implied, of the FreeBSD Project.

Alternatively if you have any questions about this project, you can visit
the project website at the project page on https://github.com/hervegirod/jarrepackager
 */
package org.girod.jarrepackager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.girod.jarrepackager.model.AbstractJarFileDirectory;
import org.girod.jarrepackager.model.JarCollectionModel;
import org.girod.jarrepackager.model.JarFileEntry;
import org.girod.jarrepackager.model.ManifestJarEntry;

/**
 * The Jar writer used for the repackaging.
 *
 * @since 0.1
 */
public class JarPackagerWriter {
   private final JarCollectionModel inputModel;
   private final File outputFile;
   private boolean debug = false;

   /**
    * Constructor.
    *
    * @param inputModel the input Jar files model
    * @param outputFile the output Jar file
    */
   public JarPackagerWriter(JarCollectionModel inputModel, File outputFile) {
      this.inputModel = inputModel;
      this.outputFile = outputFile;
   }

   /**
    * Set the Debug mode.
    *
    * @param debug true for the Debug mode
    */
   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   /**
    * Perform the writing on the output file.
    *
    * @throws IOException
    */
   public void write() throws IOException {
      // Create a buffer for reading the files
      byte[] buf = new byte[1024];
      Manifest manifest = createManifest();
      try (JarOutputStream out = new JarOutputStream(new FileOutputStream(outputFile), manifest)) {
         zipFile(buf, out);
      }
   }

   private Manifest createManifest() {
      Manifest manifest = new Manifest();
      Attributes global = manifest.getMainAttributes();
      global.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
      Iterator<Entry<String, String>> it = inputModel.getManifestAttributes().entrySet().iterator();
      while (it.hasNext()) {
         Entry<String, String> property = it.next();
         global.put(new Attributes.Name(property.getKey()), property.getValue());
      }
      return manifest;
   }

   private void zipFile(byte[] buf, JarOutputStream out) throws IOException {
      Iterator<AbstractJarFileDirectory> it = inputModel.getJarDirectories().iterator();
      while (it.hasNext()) {
         AbstractJarFileDirectory jarDir = it.next();
         write(buf, out, jarDir);
      }
      if (inputModel.hasManifestContent()) {
         writeManifestContent(buf, out);
      }
   }

   private void writeManifestContent(byte[] buf, JarOutputStream out) throws IOException {
      Iterator<ManifestJarEntry> it = inputModel.getManifestContent().values().iterator();
      while (it.hasNext()) {
         ManifestJarEntry jarEntry = it.next();
         String path = jarEntry.getPath();
         if (debug) {
            System.out.println("Manifest entry: " + path);
         }
         JarEntry inEntry = jarEntry.getEntry();
         JarFile jarFile = jarEntry.getParent().getJarFile();
         try (JarInputStream in = new JarInputStream(jarFile.getInputStream(inEntry))) {
            out.putNextEntry(new JarEntry(path));
            int len;
            while ((len = in.read(buf)) > 0) {
               out.write(buf, 0, len);
            }
            // complete the entry
            out.closeEntry();
         }
      }
   }

   private void write(byte[] buf, JarOutputStream out, AbstractJarFileDirectory jarDir) throws IOException {
      Iterator<JarFileEntry> it = jarDir.getEntries().iterator();
      while (it.hasNext()) {
         JarFileEntry fileEntry = it.next();
         String path = fileEntry.getPath();
         if (debug) {
            System.out.println("jarDir: " + jarDir.getPath() + " path: " + path);
         }
         JarEntry inEntry = fileEntry.getEntry();
         JarFile jarFile = jarDir.getJarFile();
         try (JarInputStream in = new JarInputStream(jarFile.getInputStream(inEntry))) {
            out.putNextEntry(new JarEntry(path));
            int len;
            while ((len = in.read(buf)) > 0) {
               out.write(buf, 0, len);
            }
            // complete the entry
            out.closeEntry();
         }
      }
   }
}
