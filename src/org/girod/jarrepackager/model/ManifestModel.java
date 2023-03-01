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

import java.util.HashMap;
import java.util.Map;

/**
 * A model for the manifest.
 *
 * @since 0.1
 */
public class ManifestModel {
   /**
    * The type for entries in the manifest which must be skipped.
    */
   public static final short SKIP = 0;
   /**
    * The type for entries in the manifest which must be kept.
    */
   public static final short KEEP = 1;
   private short defaultType = KEEP;
   private final Map<String, String> newProperties = new HashMap<>();
   private final Map<String, Short> existingPropertiesTypes = new HashMap<>();

   public ManifestModel() {
   }

   /**
    * Set the default keep or skip type for the main manifest entries.
    *
    * @param defaultType the default type
    */
   public void setDefaultType(short defaultType) {
      this.defaultType = defaultType;
   }

   /**
    * Return the default keep or skip type for the main manifest entries.
    *
    * @return the default type
    */
   public short getDefaultType() {
      return defaultType;
   }

   /**
    * Add a new property which must be added to the manifest.
    *
    * @param key the property key
    * @param value the property value
    */
   public void addNewProperty(String key, String value) {
      newProperties.put(key, value);
   }

   /**
    * Return the new properties which must be added to the manifest.
    *
    * @return the properties
    */
   public Map<String, String> getNewProperties() {
      return newProperties;
   }

   /**
    * Specify for an existing property if the property must be kept.
    *
    * <h1>Algorithm</h1>
    * If {@link #getDefaultType()} is {@link #KEEP}:
    * <ul>
    * <li>If the property key is not found in the {@link #getExistingPropertyTypes()}, then the method return true</li>
    * <li>If the property key is found in the {@link #getExistingPropertyTypes()}, then the method return true if the value in the
    * {@link #getExistingPropertyTypes()} for this property key is {@link #KEEP}</li>
    * </ul>
    *
    * If {@link #getDefaultType()} is {@link #SKIP}:
    * <ul>
    * <li>If the property key is not found in the {@link #getExistingPropertyTypes()}, then the method return false</li>
    * <li>If the property key is found in the {@link #getExistingPropertyTypes()}, then the method return true if the value in the
    * {@link #getExistingPropertyTypes()} for this property key is {@link #KEEP}</li>
    * </ul>
    *
    * @param key the property key
    * @return true if the property must be kept
    */
   public boolean allowExistingProperty(String key) {
      if (defaultType == KEEP) {
         if (!existingPropertiesTypes.containsKey(key)) {
            return true;
         } else {
            return existingPropertiesTypes.get(key) == KEEP;
         }
      } else {
         if (!existingPropertiesTypes.containsKey(key)) {
            return false;
         } else {
            return existingPropertiesTypes.get(key) == KEEP;
         }
      }
   }

   /**
    * Specify for a specific existing property if this property must be skipped or kept. THis will override the
    * {@link #getDefaultType()} behavior.
    *
    * @param key the property key
    * @param type the keep or skip type
    */
   public void addExistingPropertyType(String key, short type) {
      existingPropertiesTypes.put(key, type);
   }

   /**
    * Return the specific existing properties skipped or kept types.
    *
    * @return the map
    */
   public Map<String, Short> getExistingPropertyTypes() {
      return existingPropertiesTypes;
   }
}
