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

package org.eclipse.examples.widgetsample.rcp;

import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.examples.chart.Chart;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.ViewPart;

public class View extends ViewPart {
	/**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
	    
	    Chart chart = new Chart(parent, SWT.NONE);
	    chart.addTrend(new Chart.Trend("Trend one", 20));
	    chart.addTrend(new Chart.Trend("Trend two", 40));
	    chart.addTrend(new Chart.Trend("Trend three", 10));
	    chart.addTrend(new Chart.Trend("Trend four", 5));
	    chart.addTrend(new Chart.Trend("Trend five", 15));
	    
	    IStylingEngine stylingEngine = getStylingEngine();
	    if (stylingEngine != null) {
	    	stylingEngine.style(chart);
	    }
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}
	
	private IStylingEngine getStylingEngine() {
		return (IStylingEngine) ((Workbench) PlatformUI.getWorkbench()).getService(IStylingEngine.class);
	}
}