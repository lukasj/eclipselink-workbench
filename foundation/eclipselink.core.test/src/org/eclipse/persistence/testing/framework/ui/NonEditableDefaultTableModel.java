/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.framework.ui;

public class NonEditableDefaultTableModel extends javax.swing.table.DefaultTableModel {

    public NonEditableDefaultTableModel() {
        super();
    }

    public NonEditableDefaultTableModel(java.lang.Object[][] data, java.lang.Object[] columnNames) {
        super(data, columnNames);
    }

    public NonEditableDefaultTableModel(java.lang.Object[] columnNames, int numRows) {
        super(columnNames, numRows);
    }

    public NonEditableDefaultTableModel(int numRows, int numColumns) {
        super(numRows, numColumns);
    }

    public NonEditableDefaultTableModel(java.util.Vector columnNames, int numRows) {
        super(columnNames, numRows);
    }

    public NonEditableDefaultTableModel(java.util.Vector data, java.util.Vector columnNames) {
        super(data, columnNames);
    }

    /**
     * Always return false
     */
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}