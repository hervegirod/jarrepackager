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

import java.util.jar.JarEntry;

/**
 * Represents a jar entry used in a jar file content.
 *
 * @since 0.1
 */
public class JarFileEntry extends AbstractJarEntry {
   private String name;
   private AbstractJarFileDirectory parent;

   public JarFileEntry(JarEntry entry) {
      super(entry);
   }

   /**
    * Set the parent directory.
    *
    * @param parent the directory
    */
   public void setParent(AbstractJarFileDirectory parent) {
      this.parent = parent;
   }

   /**
    * Return the parent directory.
    *
    * @return the directory
    */
   public AbstractJarFileDirectory getParent() {
      return parent;
   }

   /**
    * Set the entry name (excluding the path).
    *
    * @param name the name
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * Return the entry name (excluding the path).
    *
    * @return the name
    */   
   public String getName() {
      return name;
   }

   /**
    * Return the entry path.
    *
    * @return the path
    */   
   @Override
   public String getPath() {
      return parent.getPath() + "/" + name;
   }

}
