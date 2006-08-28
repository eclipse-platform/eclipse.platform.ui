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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MonthCalendarTest {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	
	/**
    * This method initializes monthCalendar	
    *
    */
   private void createMonthCalendar() {
      new MonthCalendar(sShell, SWT.NONE);
   }

   /**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		MonthCalendarTest thisClass = new MonthCalendarTest();
		thisClass.createSShell();
		thisClass.sShell.open();

		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("Shell");
		sShell.setLayout(new FillLayout());
		createMonthCalendar();
		sShell.setSize(new org.eclipse.swt.graphics.Point(624,578));
	}

}
