/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.girod.jarrepackager.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * Represents a jar file containing jar entries.
 *
 * @since 0.1
 */
public abstract class AbstractJarFileDirectory {
   /**
    * The directory name.
    */
   protected final String name;
   private final List<JarFileDirectory> childrenDirs = new ArrayList<>();
   private final List<JarFileEntry> childrenEntries = new ArrayList<>();
   private final Map<String, JarFileDirectory> childrenDirsMap = new HashMap<>();

   public AbstractJarFileDirectory(String name) {
      this.name = name;
   }

   /**
    * Return the root directory.
    *
    * @return the root directory
    */
   public abstract JarRootDirectory getRootDirectory();

   /**
    * Return the associated jar file.
    *
    * @return the jar file
    */
   public abstract JarFile getJarFile();

   /**
    * Return the directory name.
    *
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * Return the path of the directory.
    *
    * @return the path
    */
   public abstract String getPath();

   /**
    * Return the Map of children directories.
    *
    * @return the map
    */
   public Map<String, JarFileDirectory> getChildrenDirectoriesMap() {
      return childrenDirsMap;
   }

   /**
    * Return the list of children directories.
    *
    * @return the list
    */   
   public List<JarFileDirectory> getChildrenDirectories() {
      return childrenDirs;
   }

   /**
    * Return the direct entries.
    *
    * @return the direct entries
    */
   public List<JarFileEntry> getEntries() {
      return childrenEntries;
   }

   /**
    * Add a direct entry.
    *
    * @param entry the entry
    */
   public void addChildEntry(JarFileEntry entry) {
      entry.setParent(this);
      childrenEntries.add(entry);
   }

   /**
    * Add a child directory.
    *
    * @param name the child directory name
    * @return the child directory
    */
   public JarFileDirectory addChildDirectory(String name) {
      JarFileDirectory childDir = new JarFileDirectory(this, name);
      String thePath = childDir.getPath();
      if (childrenDirsMap.containsKey(thePath)) {
         return childrenDirsMap.get(thePath);
      } else {
         childrenDirs.add(childDir);
         childrenDirsMap.put(thePath, childDir);
         return childDir;
      }
   }
}
