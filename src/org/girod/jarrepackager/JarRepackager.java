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
import java.net.URL;
import java.util.Map;
import java.util.PropertyResourceBundle;
import org.girod.jarrepackager.model.JarCollectionModel;
import org.mdiutil.util.LauncherUtils;

/**
 * The Jar repackager main class.
 *
 * @since 0.1
 */
public class JarRepackager {
   private static final String EXT_JAR = "jar";
   private static final String EXT_XML = "xml";
   private File[] inputFiles = null;
   private File outputFile = null;
   private File properties = null;
   private boolean debug = false;

   public JarRepackager() {
   }

   public static void main(String[] args) {
      JarRepackager repackager = new JarRepackager();
      repackager.launch(args);
   }

   private void printVersion() {
      URL url = JarRepackager.class.getResource("jarrepackager.properties");
      try {
         PropertyResourceBundle prb = new PropertyResourceBundle(url.openStream());
         String version = prb.getString("version");
         System.out.println("JarRepackager version " + version);
      } catch (IOException ex) {
      }
   }

   /**
    * Start the application. See {@link #main(java.lang.String[])} for the arguments list.
    *
    * @param args the arguments
    * @see #main(java.lang.String[])
    */
   public void launch(String[] args) {
      Map<String, String> props = LauncherUtils.getLaunchProperties(args);
      if (props.size() == 1 && props.containsKey("version")) {
         printVersion();
         return;
      }
      File dir = new File(System.getProperty("user.dir"));
      for (Map.Entry<String, String> entry : props.entrySet()) {
         String propKey = entry.getKey();
         String propValue = entry.getValue();
         switch (propKey) {
            case "inputFiles":
            case "input":
               inputFiles = ParserUtils.parseFilesValue(dir, propValue);
               break;
            case "outputFile":
            case "output":
               outputFile = ParserUtils.parseFileValue(dir, propValue, EXT_JAR);
               break;
            case "properties":
               properties = ParserUtils.parseFileValue(dir, propValue, EXT_XML);
               break;
            case "debug":
               debug = propValue.equals("true");
               break;
         }
      }
      if (inputFiles != null && outputFile != null) {
         try {
            repackage();
         } catch (IOException ex) {
            ex.printStackTrace();
         }
      } else {
         JarRepackagerGUI gui = new JarRepackagerGUI(this);
         gui.setVisible(true);
      }
   }

   /**
    * Set the input jar files.
    *
    * @param inputFiles the input files
    */
   public void setInputFiles(File[] inputFiles) {
      this.inputFiles = inputFiles;
   }

   /**
    * Set the output jar file.
    *
    * @param outputFile the output file
    */
   public void setOutputFile(File outputFile) {
      this.outputFile = outputFile;
   }

   /**
    * Set the debug property.
    *
    * @param debug the debug property
    */
   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   /**
    * Perform the repackaging.
    *
    * @return true if it could be performed
    * @throws IOException
    */
   public boolean repackage() throws IOException {
      if (inputFiles != null && inputFiles.length != 0 && outputFile != null) {
         JarPackagerReader reader = new JarPackagerReader(inputFiles);
         JarCollectionModel jarModel = reader.analyze();
         JarPackagerWriter writer = new JarPackagerWriter(jarModel, outputFile);
         writer.setDebug(debug);
         writer.write();
         System.out.println("Repackaging Finished");
         return true;
      } else {
         System.out.println("Repackaging Failed");
         return false;
      }
   }
}
