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

import org.eclipse.jface.examples.databinding.compositetable.CompositeTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class MonthCalendar extends Composite {

   private CompositeTable compositeTable = null;
   private WeekHeader weekHeader = null;
   private Week week = null;

   public MonthCalendar(Composite parent, int style) {
      super(parent, style);
      initialize();
   }

   private void initialize() {
      this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
      createCompositeTable();
      this.setLayout(new FillLayout());
      setSize(new org.eclipse.swt.graphics.Point(756,642));
   }

   /**
    * This method initializes compositeTable	
    *
    */
   private void createCompositeTable() {
      compositeTable = new CompositeTable(this, SWT.NONE);
      compositeTable.setNumRowsInCollection(5);
      compositeTable.setMaxRowsVisible(5);
      compositeTable.setFittingVertically(true);
      compositeTable.setRunTime(true);
      createWeekHeader();
      createWeek();
   }

   /**
    * This method initializes weekHeader	
    *
    */
   private void createWeekHeader() {
      weekHeader = new WeekHeader(compositeTable, SWT.NONE);
   }

   /**
    * This method initializes week	
    *
    */
   private void createWeek() {
      week = new Week(compositeTable, SWT.NONE);
   }

}  //  @jve:decl-index=0:visual-constraint="10,10"
