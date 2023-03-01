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
package org.girod.jarrepackager.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Represents an input jar file and its sub-directories and jar entries.
 *
 * @since 0.1
 */
public class JarRootDirectory extends AbstractJarFileDirectory {
   private Manifest manifest = null;
   private final JarFile jarFile;
   private final JarCollectionModel model;

   public JarRootDirectory(JarCollectionModel model, JarFile jarFile, String name) {
      super(name);
      this.model = model;
      this.jarFile = jarFile;
      try {
         this.manifest = jarFile.getManifest();
         addMainManifestAttributes();
      } catch (IOException ex) {
      }
   }

   /**
    * Return the associated jar file.
    *
    * @return the jar file
    */   
   @Override
   public JarFile getJarFile() {
      return jarFile;
   }

   @Override
   public String getPath() {
      return name;
   }

   /**
    * Return this.
    *
    * @return this
    */
   @Override
   public JarRootDirectory getRootDirectory() {
      return this;
   }

   private void addMainManifestAttributes() {
      if (manifest != null) {
         Iterator<Entry<Object, Object>> it = manifest.getMainAttributes().entrySet().iterator();
         while (it.hasNext()) {
            Entry<Object, Object> entry = it.next();
            model.addMainManifestAttribute(entry.getKey().toString(), entry.getValue().toString());
         }
      }
   }

   public Manifest getManifest() {
      return manifest;
   }
}
