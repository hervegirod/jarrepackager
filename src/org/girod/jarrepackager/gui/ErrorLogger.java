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
package org.girod.jarrepackager.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.girod.jarrepackager.parser.PackagerError;
import org.mdiutil.io.FileUtilities;
import org.mdiutil.swing.ExtensionFileFilter;
import org.mdiutil.xml.swing.BasicSAXHandler;

/**
 * This class is used to show errors detected during the execution.
 *
 * @since 0.1
 */
public class ErrorLogger {
   private List<PackagerError> errors = null;
   private JButton OKButton = null;
   private JButton printButton = null;
   private JDialog dialog = null;

   /**
    * Print on System.err the errors encountered during the execution.
    *
    * @param errors the errors
    */
   public void printErrors(List<PackagerError> errors) {
      for (PackagerError error : errors) {
         if (error.isParsingError()) {
            int lineNumber = error.getLineNumber();
            if (lineNumber != -1) {
               System.err.println("Parsing error line " + lineNumber + ": " + error.getMessage());
            } else {
               System.err.println("Parsing error: " + error.getMessage());
            }
         } else {
            System.err.println("Error: " + error.getMessage());
         }
      }
   }

   /**
    * Show errors encountered during the execution.
    *
    * @param errors the errors
    */
   public void showErrors(List<PackagerError> errors) {
      this.errors = errors;
      JTextPane area = constructErrorsPane();

      OKButton = new JButton("OK");
      printButton = new JButton("Print");

      OKButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            dialog.dispose();
         }
      });

      printButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            doPrint();
         }
      });

      JScrollPane scroll = new JScrollPane(area);
      JOptionPane pane = new JOptionPane(scroll, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] { OKButton, printButton });
      Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, null);
      dialog = pane.createDialog(frame, "Errors");
      dialog.setResizable(true);
      dialog.setAlwaysOnTop(true);
      dialog.setVisible(true);
   }

   private void doPrint() {
      String[] ext = { "html" };
      ExtensionFileFilter htmlfilter = new ExtensionFileFilter(ext, "HTML files");
      JFileChooser chooser = new JFileChooser();
      chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
      chooser.setDialogTitle("Save Errors File");
      chooser.setDialogType(JFileChooser.SAVE_DIALOG);
      chooser.setFileFilter(htmlfilter);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int ret = chooser.showOpenDialog(dialog);
      if (ret == JFileChooser.APPROVE_OPTION) {
         File file = FileUtilities.getCompatibleFile(chooser.getSelectedFile(), "html");
         try {
            doPrintImpl(file);
         } catch (IOException e) {
         }
      }
   }

   private void writeBR(BufferedWriter writer) throws IOException {
      writer.newLine();
      writer.append("</br>");
      writer.newLine();
   }

   private void writeError(BufferedWriter writer, String message) throws IOException {
      writer.append("<font color=\"red\">" + message + "</font>\n");
      writeBR(writer);
   }

   private void doPrintImpl(File file) throws IOException {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
         writer.append("<html>");
         writeBR(writer);
         writer.append("<H1>JarRepackager errors</H1>");
         writer.newLine();
         for (PackagerError error : errors) {
            if (error.isParsingError()) {
               int lineNumber = error.getLineNumber();
               if (lineNumber != -1) {
                  writeError(writer, "Parsing error line " + lineNumber + ": " + error.getMessage());
               } else {
                  writeError(writer, "Parsing error: " + error.getMessage());
               }
            } else {
               writeError(writer, "Error: " + error.getMessage());
            }
            writer.append("</html>");
            writer.flush();
         }
      }
   }

   private JTextPane constructErrorsPane() {
      SizedTextPane area = new SizedTextPane(10, 40);
      for (PackagerError error : errors) {
         if (error.isParsingError()) {
            int lineNumber = error.getLineNumber();
            if (lineNumber != -1) {
               area.append("Parsing error line " + lineNumber + ": " + error.getMessage() + "\n", "red");
            } else {
               area.append("Parsing error: " + error.getMessage() + "\n", "red");
            }
         } else {
            area.append("Error: " + error.getMessage() + "\n", "red");
         }
      }
      return area;
   }

   /**
    * A JTextPane with a specified preferred scrollable size.
    */
   private class SizedTextPane extends JTextPane {
      private Dimension d;
      private int _rows = 1;
      private int _columns = 1;
      private HTMLDocument doc;
      private final HTMLEditorKit kit = new HTMLEditorKit();
      private String fontFace = null;
      private static final int FONT_SIZE = BasicSAXHandler.DEFAULT_SIZE;

      /**
       * Create a new SizedTextPane.
       *
       * @param rows the number of rows of the area
       * @param columns the number of columns of the area
       */
      public SizedTextPane(int rows, int columns) {
         super();
         this._rows = rows;
         this._columns = columns;

         setSize();
         createDocument();
      }

      private void setSize() {
         JTextArea textArea = new JTextArea(_rows, _columns);
         Font font = textArea.getFont();
         fontFace = font.getFamily();
         d = textArea.getPreferredSize();
      }

      private void createDocument() {
         this.setEditorKit(kit);
         doc = (HTMLDocument) kit.createDefaultDocument();
         this.setDocument(doc);
         this.setEditable(false);
      }

      public void append(String text) {
         appendImpl("<html><font face=\"" + fontFace + "\" size=\"" + FONT_SIZE + "\">" + text + "</font></html>\n");
      }

      public void append(String text, String htmlColor) {
         appendImpl("<html><font face=\"" + fontFace + "\" size=\"" + FONT_SIZE + "\" color=" + htmlColor + "\">" + text + "</font></html>\n");
      }

      private void appendImpl(String text) {
         try {
            Reader r = new StringReader(text);
            kit.read(r, doc, doc.getLength());
            this.setCaretPosition(doc.getLength());
         } catch (BadLocationException | IOException e) {
         }
      }

      @Override
      public Dimension getPreferredScrollableViewportSize() {
         return d;
      }
   }
}
