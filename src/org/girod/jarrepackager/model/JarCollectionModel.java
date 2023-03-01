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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The model representing the input jar files and their Jar entries.
 *
 * @since 0.1
 */
public class JarCollectionModel {
   private final static String MANIFEST = "META-INF/MANIFEST.MF";
   private final static String MANIFEST_DIR = "META-INF/";
   private final List<JarRootDirectory> jarRootDirectories = new ArrayList<>();
   private final List<AbstractJarFileDirectory> jarDirectories = new ArrayList<>();
   private final Map<String, AbstractJarFileDirectory> paths = new HashMap<>();
   private final Map<String, ManifestJarEntry> manifestContent = new HashMap<>();
   private final Map<String, String> manifestMainAttrs = new HashMap<>();
   private JarFile jarFile = null;
   private JarRootDirectory rootDirectory = null;

   public JarCollectionModel() {
   }

   /**
    * Return the list of root directories.
    *
    * @return the list of root directories
    */
   public List<JarRootDirectory> getRootJarDirectories() {
      return jarRootDirectories;
   }

   /**
    * Return the list of all directories.
    *
    * @return the list of all directories
    */
   public List<AbstractJarFileDirectory> getJarDirectories() {
      return jarDirectories;
   }

   private boolean isManifest(String path) {
      return path.equals(MANIFEST);
   }

   private boolean isInManifest(String path) {
      return path.startsWith(MANIFEST_DIR);
   }

   /**
    * Add an attribute to be put in the Manifest.
    *
    * @param key the attribute key
    * @param value the attribute value
    */
   public void addMainManifestAttribute(String key, String value) {
      if (!manifestMainAttrs.containsKey(key)) {
         manifestMainAttrs.put(key, value);
      }
   }

   /**
    * Return the map of attributes to be put in the Manifest.
    *
    * @return the attributes
    */
   public Map<String, String> getManifestAttributes() {
      return manifestMainAttrs;
   }

   /**
    * Return the content of the "MANIFEST/" directory.
    *
    * @return the content
    */
   public Map<String, ManifestJarEntry> getManifestContent() {
      return manifestContent;
   }

   /**
    * Return true if the "MANIFEST/" directory has content files.
    *
    * @return true if the "MANIFEST/" directory has content files
    */
   public boolean hasManifestContent() {
      return !manifestContent.isEmpty();
   }

   /**
    * Add an entry in the "MANIFEST/" directory.
    *
    * @param fileEntry the entry
    */
   public void addManifestContent(ManifestJarEntry fileEntry) {
      String path = fileEntry.getPath();
      if (!manifestContent.containsKey(path)) {
         manifestContent.put(path, fileEntry);
      }
   }

   /**
    * Set the current Jar file during the input files reading.
    *
    * @param jarFile the Jar file
    */
   public void setJarFile(JarFile jarFile) {
      this.jarFile = jarFile;
      this.rootDirectory = null;
      this.paths.clear();
   }

   /**
    * Add a Jar entry found in the input files.
    *
    * @param entry the entry
    */
   public void addJarEntry(JarEntry entry) {
      String path = entry.getName();
      if (path.endsWith("/")) {
         return;
      } else if (isManifest(path)) {
         return;
      }
      boolean isInManifest = false;
      AbstractJarEntry fileEntry;
      if (isInManifest(path)) {
         isInManifest = true;
         fileEntry = new ManifestJarEntry(entry);
      } else {
         fileEntry = new JarFileEntry(entry);
      }
      List<String> decodedPath = decodePath(path);
      AbstractJarFileDirectory dir = null;
      for (int i = 0; i < decodedPath.size() - 1; i++) {
         String subpath = decodedPath.get(i);
         if (dir == null) {
            rootDirectory = new JarRootDirectory(this, jarFile, subpath);
            dir = rootDirectory;
            jarRootDirectories.add((JarRootDirectory) dir);
         } else if (!isInManifest) {
            dir = new JarFileDirectory(dir, subpath);
         }
         if (isInManifest) {
            ManifestJarEntry manifestEntry = (ManifestJarEntry) fileEntry;
            manifestEntry.setParent(rootDirectory);
            addManifestContent(manifestEntry);
         } else {
            String completePath = dir.getPath();
            if (paths.containsKey(completePath)) {
               dir = paths.get(completePath);
            } else {
               jarDirectories.add(dir);
               paths.put(completePath, dir);
            }
            if (i == decodedPath.size() - 2) {
               JarFileEntry _jarfileEntry = (JarFileEntry) fileEntry;
               _jarfileEntry.setName(decodedPath.get(decodedPath.size() - 1));
               dir.addChildEntry(_jarfileEntry);
            }
         }
      }
   }

   private List<String> decodePath(String path) {
      List<String> list = new ArrayList<>();
      StringTokenizer tok = new StringTokenizer(path, "/");
      while (tok.hasMoreTokens()) {
         String tk = tok.nextToken();
         list.add(tk);
      }
      return list;
   }
}
