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
package org.girod.jarrepackager.parser;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.girod.jarrepackager.JarRepackager;
import org.girod.jarrepackager.model.ManifestModel;
import org.mdiutil.swing.ExtensionFileFilter;
import org.mdiutil.xml.ResolverSAXHandler;
import org.mdiutil.xml.XMLSAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

/**
 * This class is used to parse a properties file.
 *
 * @since 0.1
 */
public class PropertiesParser extends ResolverSAXHandler {
   private static final ExtensionFileFilter JAR_FILEFILTER;

   static {
      String[] ext = { "jar" };
      JAR_FILEFILTER = new ExtensionFileFilter(ext, "Jar Files");
   }
   private static final Pattern WILCARD_PAT = Pattern.compile("([^*]*)\\*([^*/]+)");
   private final JarRepackager repackager;
   private File dir = null;
   private boolean debug = false;
   private final ManifestModel manifestModel = new ManifestModel();
   private final List<File> inputFiles = new ArrayList<>();
   private File outputFile = null;
   private boolean inManifest = false;

   public PropertiesParser(JarRepackager repackager) {
      this.repackager = repackager;
      this.debug = repackager.isDebugging();
   }

   public ManifestModel parse(File propertiesFile) {
      this.dir = propertiesFile.getParentFile();
      URL schemaURL = PropertiesParser.class.getResource("properties.xsd");
      XMLSAXParser parser = new XMLSAXParser("Properties Parser");
      parser.setValidating(true);
      parser.setSchema(schemaURL);
      parser.setHandler(this);
      parser.parse(propertiesFile);
      return manifestModel;
   }

   @Override
   public void startElement(String uri, String localname, String qname, Attributes attr) {
      switch (localname) {
         case "debug":
            parseDebug(attr);
            break;
         case "manifest":
            inManifest = true;
            parseManifest(attr);
            break;
         case "existingProperty":
            if (inManifest) {
               parseManifestProperty(attr);
            }
            break;
         case "newProperty":
            if (inManifest) {
               parseManifestNewProperty(attr);
            }
            break;
         case "file":
            parseInputFile(attr);
            break;
         case "output":
            parseOutputFile(attr);
            break;
      }
   }

   @Override
   public void endElement(String uri, String localname, String qname) {
      switch (localname) {
         case "manifest":
            inManifest = false;
            break;
         case "properties":
            applyGeneralParameters();
            break;
      }
   }

   private void addWarning(String message) {
      this.warning(new SAXParseException(message, locator));
   }

   private void parseInputFile(Attributes attr) {
      for (int i = 0; i < attr.getLength(); i++) {
         String key = attr.getLocalName(i);
         String value = attr.getValue(i);
         if (key.equals("url")) {
            if (!value.contains("*")) {
               File file = ParserUtils.parseFileValue(dir, value, "jar");
               if (file != null && file.exists() && file.isFile()) {
                  inputFiles.add(file);
               } else {
                  addWarning("Input File of URL " + value + " not found or is not a File");
               }
            } else {
               boolean isParsable = parseWilcardFiles(value);
               if (!isParsable) {
                  addWarning("Input File of URL " + value + " has an invalid pattern");
               }
            }
         }
      }
   }

   private boolean parseWilcardFiles(String value) {
      Matcher m = WILCARD_PAT.matcher(value);
      if (!m.matches()) {
         return false;
      }
      File currentDir = dir;
      String g1 = m.group(1);
      value = value.replaceAll("\\.", "\\\\.");
      String regex = value.replaceAll("\\*", ".*");
      if (g1.contains("/")) {
         String filePath = g1;
         int index = g1.lastIndexOf('/');
         String subdirpath = g1.substring(0, index);
         currentDir = new File(currentDir, subdirpath);
         if (!currentDir.exists() || !currentDir.isDirectory()) {
            addWarning("Directory for " + value + " does not exist or is not a directory");
            return false;
         }
         if (index > g1.length() + 1) {
            filePath = g1.substring(index + 1);
         }
         if (filePath != null) {
            regex = filePath + regex;
         }
      }
      File[] files = currentDir.listFiles((FileFilter) JAR_FILEFILTER);
      if (files != null && files.length > 0) {
         for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String name = file.getName();
            Pattern p = Pattern.compile(regex);
            m = p.matcher(name);
            if (m.matches()) {
               inputFiles.add(file);
               if (debug) {
                  System.out.println("File " + file.getAbsolutePath() + " added");
               }
            }
         }
      }
      return true;
   }

   private void parseOutputFile(Attributes attr) {
      for (int i = 0; i < attr.getLength(); i++) {
         String key = attr.getLocalName(i);
         String value = attr.getValue(i);
         if (key.equals("url")) {
            outputFile = ParserUtils.parseFileValue(dir, value, "jar");
            if (outputFile != null && outputFile.exists() && outputFile.isFile()) {
               repackager.setOutputFile(outputFile);
            } else {
               addWarning("OutputFile of URL " + value + " not found or is not a File");
            }
         }
      }
   }

   private void parseManifest(Attributes attr) {
      for (int i = 0; i < attr.getLength(); i++) {
         String key = attr.getLocalName(i);
         String value = attr.getValue(i);
         if (key.equals("keep")) {
            if (value.equals("skip")) {
               manifestModel.setDefaultType(ManifestModel.SKIP);
            }
         }
      }
   }

   private void parseManifestNewProperty(Attributes attr) {
      String key = null;
      String value = null;

      for (int i = 0; i < attr.getLength(); i++) {
         String _key = attr.getLocalName(i);
         String _value = attr.getValue(i);
         if (_key.equals("key")) {
            key = _value;
         } else if (_key.equals("value")) {
            value = _value;
         }
      }
      if (key != null && value != null) {
         manifestModel.addNewProperty(key, value);
      }
   }

   private void parseManifestProperty(Attributes attr) {
      String key = null;
      short type = -1;
      for (int i = 0; i < attr.getLength(); i++) {
         String _key = attr.getLocalName(i);
         String value = attr.getValue(i);
         if (_key.equals("key")) {
            key = value;
         } else if (_key.equals("keep")) {
            type = value.equals("keep") ? ManifestModel.KEEP : ManifestModel.SKIP;
         }
      }
      if (key != null && type != -1) {
         manifestModel.addExistingPropertyType(key, type);
      }
   }

   private void parseDebug(Attributes attr) {
      for (int i = 0; i < attr.getLength(); i++) {
         String key = attr.getLocalName(i);
         String value = attr.getValue(i);
         if (key.equals("value")) {
            debug = value.equals("true");
            repackager.setDebug(debug);
         }
      }
   }

   private void applyGeneralParameters() {
      if (!inputFiles.isEmpty()) {
         File[] files = inputFiles.toArray(new File[inputFiles.size()]);
         repackager.setInputFiles(files);
      }
      if (outputFile != null) {
         repackager.setOutputFile(outputFile);
      }
   }
}
