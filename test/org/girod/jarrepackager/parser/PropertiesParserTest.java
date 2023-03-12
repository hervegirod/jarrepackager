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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.net.URL;
import org.girod.jarrepackager.JarRepackager;
import org.girod.jarrepackager.model.ManifestModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @since 0.1
 */
public class PropertiesParserTest {

   public PropertiesParserTest() {
   }

   @BeforeClass
   public static void setUpClass() {
   }

   @AfterClass
   public static void tearDownClass() {
   }

   @Before
   public void setUp() {
   }

   @After
   public void tearDown() {
   }

   /**
    * Test of parse method, of class PropertiesParser.
    */
   @Test
   public void testParse() {
      System.out.println("PropertiesParserTest: testParse");
      File dir = new File(System.getProperty("user.dir"));
      dir = new File(dir, "samples/netty");
      URL url = PropertiesParserTest.class.getResource("configNetty.xml");
      JarRepackager repackager = new JarRepackager();
      PropertiesParser parser = new PropertiesParser(repackager);
      ManifestModel model = parser.parse(dir, new File(url.getFile()));
      assertNotNull("ManifestModel should not be null", model);
      assertFalse("PropertiesParser should not have any exception", parser.hasParserExceptions());
      assertTrue("PropertiesParser should not have any exception", repackager.getErrors().isEmpty());

      File[] files = repackager.getInputFiles();
      assertNotNull("input files should not be null", files);
      assertEquals("input files", 33, files.length);

      File file = repackager.getOutputFile();
      assertNull("output file should be null", file);

      assertEquals("existing manifest property types", 4, model.getExistingPropertyTypes().size());
      assertEquals("new manifest property types", 2, model.getNewProperties().size());
   }
}
