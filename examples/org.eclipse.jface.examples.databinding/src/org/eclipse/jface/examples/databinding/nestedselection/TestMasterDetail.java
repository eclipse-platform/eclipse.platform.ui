/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.nestedselection;

import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.2
 * 
 */
public class TestMasterDetail {
   /**
	 * @param args
	 */
	public static void main(String[] args) {
		new TestMasterDetail().run();
	}

   private Shell shell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
   private Table table = null;
   private Label label = null;
   private Label label1 = null;
   private Text text = null;
   private Label label2 = null;
   private Text text1 = null;
   private Label label3 = null;
   private Text text2 = null;
   private Label label4 = null;
   private Text text3 = null;
   private Table table1 = null;
   
   /**
    * This method initializes table 
    *
    */
   private void createTable() {
      GridData gridData = new org.eclipse.swt.layout.GridData();
      gridData.grabExcessHorizontalSpace = true;
      gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
      gridData.horizontalSpan = 2;
      gridData.grabExcessVerticalSpace = true;
      gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
      table = new Table(shell, SWT.NONE);
      table.setHeaderVisible(true);
      table.setLayoutData(gridData);
      table.setLinesVisible(true);
      TableColumn tableColumn = new TableColumn(table, SWT.NONE);
      tableColumn.setWidth(60);
      tableColumn.setText("Name");
      TableColumn tableColumn1 = new TableColumn(table, SWT.NONE);
      tableColumn1.setWidth(60);
      tableColumn1.setText("State");
   }

   /**
    * This method initializes table1   
    *
    */
   private void createTable1() {
      GridData gridData5 = new org.eclipse.swt.layout.GridData();
      gridData5.horizontalSpan = 2;
      gridData5.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
      gridData5.grabExcessHorizontalSpace = true;
      gridData5.grabExcessVerticalSpace = true;
      gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
      table1 = new Table(shell, SWT.NONE);
      table1.setHeaderVisible(true);
      table1.setLayoutData(gridData5);
      table1.setLinesVisible(true);
      TableColumn tableColumn2 = new TableColumn(table1, SWT.NONE);
      tableColumn2.setWidth(60);
      tableColumn2.setText("Order No");
      TableColumn tableColumn3 = new TableColumn(table1, SWT.NONE);
      tableColumn3.setWidth(60);
      tableColumn3.setText("Date");
   }

   /**
    * This method initializes sShell
    */
   private void createShell() {
      GridData gridData4 = new org.eclipse.swt.layout.GridData();
      gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
      gridData4.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
      GridData gridData3 = new org.eclipse.swt.layout.GridData();
      gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
      gridData3.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
      GridData gridData2 = new org.eclipse.swt.layout.GridData();
      gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
      gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
      GridData gridData1 = new org.eclipse.swt.layout.GridData();
      gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
      gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      shell = new Shell();
      shell.setText("Shell");
      createTable();
      shell.setLayout(gridLayout);
      shell.setSize(new org.eclipse.swt.graphics.Point(495,357));
      label1 = new Label(shell, SWT.NONE);
      label1.setText("Name");
      text = new Text(shell, SWT.BORDER);
      text.setLayoutData(gridData1);
      label2 = new Label(shell, SWT.NONE);
      label2.setText("Address");
      text1 = new Text(shell, SWT.BORDER);
      text1.setLayoutData(gridData2);
      label3 = new Label(shell, SWT.NONE);
      label3.setText("City");
      text2 = new Text(shell, SWT.BORDER);
      text2.setLayoutData(gridData4);
      label4 = new Label(shell, SWT.NONE);
      label4.setText("State");
      text3 = new Text(shell, SWT.BORDER);
      text3.setLayoutData(gridData3);
      createTable1();
   }
   
   
   private void run() {
      Display display = new Display();

      createShell();
      bind(shell);

      shell.setSize(600, 600);
      shell.open();
      while (!shell.isDisposed()) {
         if (!display.readAndDispatch())
            display.sleep();
      }
      display.dispose();
   }

   private void bind(Control parent) {
      IDataBindingContext dbc = ExampleBinding.createContext(parent);
   }
}
