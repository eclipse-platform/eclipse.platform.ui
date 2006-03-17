/*
 * Copyright (C) 2005 David Orme <djo@coconut-palm-software.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme     - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.compositetable.month;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class Week extends Composite {

	private Day day = null;
   private Day day1 = null;
   private Day day2 = null;
   private Day day3 = null;
   private Day day4 = null;
   private Day day5 = null;
   private Day day6 = null;

   public Week(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	private void initialize() {
		createDay();
		createDay1();
		createDay2();
		createDay3();
		createDay4();
		createDay5();
		createDay6();
		this.setSize(new org.eclipse.swt.graphics.Point(797,238));
	}

   /**
    * This method initializes day	
    *
    */
   private void createDay() {
      day = new Day(this, SWT.NONE);
      day.setBounds(new org.eclipse.swt.graphics.Rectangle(8,14,91,89));
   }

   /**
    * This method initializes day1	
    *
    */
   private void createDay1() {
      day1 = new Day(this, SWT.NONE);
      day1.setBounds(new org.eclipse.swt.graphics.Rectangle(112,14,92,93));
   }

   /**
    * This method initializes day2	
    *
    */
   private void createDay2() {
      day2 = new Day(this, SWT.NONE);
      day2.setBounds(new org.eclipse.swt.graphics.Rectangle(227,14,97,93));
   }

   /**
    * This method initializes day3	
    *
    */
   private void createDay3() {
      day3 = new Day(this, SWT.NONE);
      day3.setBounds(new org.eclipse.swt.graphics.Rectangle(347,13,101,94));
   }

   /**
    * This method initializes day4	
    *
    */
   private void createDay4() {
      day4 = new Day(this, SWT.NONE);
      day4.setBounds(new org.eclipse.swt.graphics.Rectangle(467,13,100,96));
   }

   /**
    * This method initializes day5	
    *
    */
   private void createDay5() {
      day5 = new Day(this, SWT.NONE);
      day5.setBounds(new org.eclipse.swt.graphics.Rectangle(582,16,89,92));
   }

   /**
    * This method initializes day6	
    *
    */
   private void createDay6() {
      day6 = new Day(this, SWT.NONE);
      day6.setBounds(new org.eclipse.swt.graphics.Rectangle(686,19,88,90));
   }

}  //  @jve:decl-index=0:visual-constraint="8,13"
