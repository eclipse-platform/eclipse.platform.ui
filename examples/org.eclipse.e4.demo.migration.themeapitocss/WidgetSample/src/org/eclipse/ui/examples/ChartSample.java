/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.examples;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.examples.chart.Chart;

public class ChartSample {
	public static void main(String... args) {
		 Display display = new Display();
		    Shell shell = new Shell(display);
		    shell.setText("Example of Chart widget");
		    shell.setLayout(new FillLayout());
		    
		    Chart chart = new Chart(shell, SWT.NONE);
		    chart.addTrend(new Chart.Trend("Trend one", 20));
		    chart.addTrend(new Chart.Trend("Trend two", 40));
		    chart.addTrend(new Chart.Trend("Trend three", 20));
		    
		    shell.open();
		    while (!shell.isDisposed()) {
		      if (!display.readAndDispatch()) {
		        display.sleep();
		      }
		    }
		    display.dispose();
	}
}
