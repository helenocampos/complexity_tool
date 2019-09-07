package com.mycompany.complexity.tool.gui;

import java.io.File;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Heleno
 */
class MyCustomFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public boolean accept(File file) {
        // Allow only directories, or files with ".txt" extension
        return file.isDirectory() || file.getAbsolutePath().endsWith(".java");
    }

    @Override
    public String getDescription() {
            // This description will be displayed in the dialog,
        // hard-coded = ugly, should be done via I18N
        return "Java Files (*.java)";
    }
}
