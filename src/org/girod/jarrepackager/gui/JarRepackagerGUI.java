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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import org.girod.jarrepackager.JarRepackager;
import org.girod.jarrepackager.parser.PackagerError;
import org.mdiutil.swing.ExtensionFileFilter;
import org.mdiutil.swing.JFileSelector;

/**
 * The GUI used for the Jar repackager.
 *
 * @since 0.1
 */
public class JarRepackagerGUI extends JFrame {
   private File[] inputFiles = null;
   private File outputFile = null;
   private File propertiesFile = null;
   private JFileSelector outputSelector = null;
   private JFileSelector inputSelector = null;
   private JFileSelector propertiesSelector = null;
   private JCheckBox debugCb = null;
   private AbstractAction applyAction = null;
   private JProgressBar progress = null;
   private File dir = null;
   private final JarRepackager repackager;
   private ExtensionFileFilter jarFileFilter;
   private ExtensionFileFilter xmlFileFilter;

   public JarRepackagerGUI(JarRepackager repackager) {
      super("Jar Repackager");
      this.repackager = repackager;
      this.dir = new File(System.getProperty("user.dir"));
      setup();
      this.setSize(500, 300);
   }

   private void setup() {
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      String[] ext = { "xml" };
      xmlFileFilter = new ExtensionFileFilter(ext, "XML Files");

      String[] ext2 = { "jar" };
      jarFileFilter = new ExtensionFileFilter(ext2, "Jar Files");

      JMenuBar mbar = new JMenuBar();
      this.setJMenuBar(mbar);

      JMenu fileMenu = new JMenu("File");
      mbar.add(fileMenu);
      AbstractAction exitAction = new AbstractAction("Exit") {
         @Override
         public void actionPerformed(ActionEvent e) {
            System.exit(0);
         }
      };
      fileMenu.add(exitAction);

      inputSelector = new JFileSelector("Select input jar files");
      inputSelector.setMultiSelectionEnabled(true);
      inputSelector.setCurrentDirectory(dir);
      inputSelector.setFileSelectionMode(JFileChooser.FILES_ONLY);
      inputSelector.setDialogType(JFileChooser.OPEN_DIALOG);

      inputSelector.setFileFilter(jarFileFilter);
      inputSelector.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            inputFiles = inputSelector.getSelectedFiles();
            checkApplyState();
         }
      });

      outputSelector = new JFileSelector("Select output jar file");
      outputSelector.setCurrentDirectory(dir);
      outputSelector.setFileSelectionMode(JFileChooser.FILES_ONLY);
      outputSelector.setDialogType(JFileChooser.SAVE_DIALOG);

      outputSelector.setFileFilter(jarFileFilter);
      outputSelector.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            outputFile = outputSelector.getSelectedFile();
            checkApplyState();
         }
      });

      propertiesSelector = new JFileSelector("Select properties file");
      propertiesSelector.setCurrentDirectory(dir);
      propertiesSelector.setFileSelectionMode(JFileChooser.FILES_ONLY);
      propertiesSelector.setDialogType(JFileChooser.OPEN_DIALOG);

      propertiesSelector.setFileFilter(xmlFileFilter);
      propertiesSelector.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            propertiesFile = propertiesSelector.getSelectedFile();
            checkApplyState();
         }
      });

      debugCb = new JCheckBox("Debug");
      debugCb.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            repackager.setDebug(debugCb.isSelected());
         }
      });

      JMenu actionsMenu = new JMenu("Actions");
      mbar.add(actionsMenu);

      applyAction = new AbstractAction("Apply") {
         @Override
         public void actionPerformed(ActionEvent e) {
            apply();
         }
      };
      applyAction.setEnabled(false);
      actionsMenu.add(applyAction);

      Container pane = this.getContentPane();
      progress = new JProgressBar();
      JPanel contentPanel = new JPanel();
      pane.add(contentPanel, BorderLayout.CENTER);
      pane.add(progress, BorderLayout.SOUTH);
      contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
      contentPanel.add(Box.createVerticalStrut(5));

      JPanel inputPanel = new JPanel();
      inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
      inputPanel.add(Box.createHorizontalStrut(5));
      inputPanel.add(new JLabel("Input jar files"));
      inputPanel.add(Box.createHorizontalStrut(5));
      inputPanel.add(inputSelector);
      inputPanel.add(Box.createHorizontalStrut(5));
      inputPanel.add(Box.createHorizontalGlue());

      contentPanel.add(inputPanel);
      contentPanel.add(Box.createVerticalStrut(5));

      JPanel outputPanel = new JPanel();
      outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.X_AXIS));
      outputPanel.add(Box.createHorizontalStrut(5));
      outputPanel.add(new JLabel("Output jar file"));
      outputPanel.add(Box.createHorizontalStrut(5));
      outputPanel.add(outputSelector);
      outputPanel.add(Box.createHorizontalStrut(5));
      outputPanel.add(Box.createHorizontalGlue());

      contentPanel.add(outputPanel);
      contentPanel.add(Box.createVerticalStrut(5));

      JPanel propertiesPanel = new JPanel();
      propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.X_AXIS));
      propertiesPanel.add(Box.createHorizontalStrut(5));
      propertiesPanel.add(new JLabel("Properties File"));
      propertiesPanel.add(Box.createHorizontalStrut(5));
      propertiesPanel.add(propertiesSelector);
      propertiesPanel.add(Box.createHorizontalStrut(5));
      propertiesPanel.add(Box.createHorizontalGlue());

      contentPanel.add(propertiesPanel);
      contentPanel.add(Box.createVerticalStrut(5));

      JPanel debugPanel = new JPanel();
      debugPanel.setLayout(new BoxLayout(debugPanel, BoxLayout.X_AXIS));
      debugPanel.add(Box.createHorizontalStrut(5));
      debugPanel.add(debugCb);
      debugPanel.add(Box.createHorizontalStrut(5));
      debugPanel.add(Box.createHorizontalGlue());

      contentPanel.add(debugPanel);
      contentPanel.add(Box.createVerticalStrut(5));
      contentPanel.add((Box.createVerticalGlue()));
   }

   /**
    * Set the input jar files.
    *
    * @param inputFiles the input files
    */
   public void setInputFiles(File[] inputFiles) {
      this.inputFiles = inputFiles;
      inputSelector.setSelectedFiles(inputFiles);
   }

   /**
    * Set the output jar file.
    *
    * @param outputFile the output file
    */
   public void setOutputFile(File outputFile) {
      this.outputFile = outputFile;
      outputSelector.setSelectedFile(outputFile);
   }

   private void checkApplyState() {
      if (((inputFiles != null && inputFiles.length != 0) || propertiesFile != null) && outputFile != null) {
         applyAction.setEnabled(true);
      } else {
         applyAction.setEnabled(false);
      }
   }

   private void apply() {
      applyAction.setEnabled(false);
      progress.setStringPainted(true);
      progress.setString("Repackaging Started");
      progress.setIndeterminate(true);
      repackager.setInputFiles(inputFiles);
      repackager.setOutputFile(outputFile);
      repackager.setPropertiesFile(propertiesFile);
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<Boolean> future = executor.submit(new Callable<Boolean>() {
         @Override
         public Boolean call() {
            try {
               boolean result = repackager.repackage();
               if (result) {
                  setProgressFinishedText("Repackaging Finished");
               } else {
                  setProgressFinishedText("Repackaging Failed");
                  List<PackagerError> packagerErrors = repackager.getErrors();
                  if (!packagerErrors.isEmpty()) {
                     ErrorLogger logger = new ErrorLogger();
                     logger.showErrors(packagerErrors);
                  }
               }
               return result;
            } catch (IOException ex) {
               setProgressFinishedText("Repackaging Failed");
               ex.printStackTrace();
               return false;
            }
         }
      });
   }

   private void setProgressFinishedText(String message) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            applyAction.setEnabled(true);
            progress.setIndeterminate(false);
            progress.setString(message);
         }
      });
   }
}
