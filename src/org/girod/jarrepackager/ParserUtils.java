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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.mdiutil.io.FileUtilities;

/**
 * An utility class for parsing the launcher arguments.
 *
 * @since 0.1
 */
public class ParserUtils {
   private ParserUtils() {
   }

   /**
    * Parse one URL declaration.
    *
    * @param dir the parent directory
    * @param propValue the property value
    * @param extension the file extension
    * @return the URL
    */
   public static File parseFileValue(File dir, String propValue, String extension) {
      File file;
      try {
         URL url = FileUtilities.getChildURL(dir.toURI().toURL(), propValue);
         file = new File(url.getFile());
      } catch (MalformedURLException ex) {
         file = FileUtilities.getFile(dir, propValue);
      }
      if ((!file.exists() && file.isDirectory()) && FileUtilities.getFileExtension(file).equals(extension)) {
         return file;
      } else {
         return null;
      }
   }

   /**
    * Parse a list of URLs declaration.
    *
    * @param dir the parent directory
    * @param propValue the property value
    * @return the URLs array
    */
   public static File[] parseFilesValue(File dir, String propValue) {
      if (propValue.indexOf(';') == -1) {
         File[] files = null;
         try {
            URL url = FileUtilities.getChildURL(dir.toURI().toURL(), propValue);
            files = new File[1];
            File file = new File(url.getFile());
            if (file.exists() && file.isFile() && FileUtilities.getFileExtension(file).equals("jar")) {
               files[0] = file;
            }
         } catch (MalformedURLException e) {
         }
         if (files != null && files.length == 0) {
            files = null;
         }
         return files;
      } else {
         List<File> files = new ArrayList<>();
         String[] urlS = propValue.split(";");
         for (int i = 0; i < urlS.length; i++) {
            try {
               URL url = FileUtilities.getChildURL(dir.toURI().toURL(), urlS[i]);
               File file = new File(url.getFile());
               if (file.exists() && file.isFile()) {
                  files.add(file);
               }
            } catch (MalformedURLException e) {
            }
         }

         if (files.isEmpty()) {
            return null;
         } else {
            return files.toArray(new File[files.size()]);
         }
      }
   }
}
