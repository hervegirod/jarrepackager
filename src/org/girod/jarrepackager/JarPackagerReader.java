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
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.girod.jarrepackager.model.JarCollectionModel;

/**
 * The Jar reader used for the repackaging.
 *
 * @since 0.1
 */
public class JarPackagerReader {
   private File[] inputFiles = null;
   private final JarCollectionModel jarModel = new JarCollectionModel();

   /**
    * Constructor.
    *
    * @param inputFiles the input files
    */
   public JarPackagerReader(File[] inputFiles) {
      this.inputFiles = inputFiles;
   }

   /**
    * Return the resulting model.
    *
    * @return the model
    */
   public JarCollectionModel getModel() {
      return jarModel;
   }

   /**
    * Analyse the content of the model.
    * @return the model
    * @throws IOException 
    */
   public JarCollectionModel analyze() throws IOException {
      for (int i = 0; i < inputFiles.length; i++) {
         File file = inputFiles[i];
         analyze(file);
      }
      return jarModel;
   }

   private void analyze(File file) throws IOException {
      JarFile jarFile = new JarFile(file);
      jarModel.setJarFile(jarFile);
      Enumeration<JarEntry> en = jarFile.entries();
      while (en.hasMoreElements()) {
         JarEntry entry = en.nextElement();
         jarModel.addJarEntry(entry);
      }
   }
}
