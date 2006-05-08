/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.compositetable.month;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class Day extends Composite {

   private static final int _SIZE_MULTIPLIER = 7;
   private Label dayNumber = null;
   private Point textBounds;
   private Label label1 = null;
   private Label notes = null;
   public Day(Composite parent, int style) {
      super(parent, style);
      initialize();
   }

   private void initialize() {
      GridData gridData1 = new org.eclipse.swt.layout.GridData();
      gridData1.horizontalSpan = 2;
      gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
      gridData1.grabExcessVerticalSpace = true;
      gridData1.grabExcessHorizontalSpace = true;
      gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
      GridData gridData = new org.eclipse.swt.layout.GridData();
      gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
      gridData.grabExcessHorizontalSpace = true;
      gridData.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
      label1 = new Label(this, SWT.NONE);
      label1.setLayoutData(gridData);
      label1.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      dayNumber = new Label(this, SWT.NONE);
      dayNumber.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
      dayNumber.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
      dayNumber.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION));
      dayNumber.setText("31");
      notes = new Label(this, SWT.WRAP);
      notes.setText("8:45a Stand-up meeting\n9:00a Pair with Andy");
      notes.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
      notes.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
      notes.setLayoutData(gridData1);
      textBounds = dayNumber.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
      this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
      this.setLayout(gridLayout);
      setSize(new org.eclipse.swt.graphics.Point(106,101));
   }
   
   public Point computeSize(int wHint, int hHint, boolean changed) {
      Point size = new Point(0, 0);
      size.x = textBounds.x * _SIZE_MULTIPLIER;
      size.y = textBounds.y * _SIZE_MULTIPLIER/2;
      return size;
   }
   
   public int getDayNumber() {
      return -1;
   }
   
   public void setDayNumber() {
      
   }

}  //  @jve:decl-index=0:visual-constraint="10,10"
