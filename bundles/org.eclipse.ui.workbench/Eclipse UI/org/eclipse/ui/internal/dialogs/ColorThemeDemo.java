/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.ColorSchemeService;
import org.eclipse.ui.internal.WorkbenchColors;


/**
 * Class to encapsulate the set of widgets that will be used to indicate 
 * the current color theme of the workbench and give feedback on 
 * changes made.
 */
public class ColorThemeDemo {

	Composite sampleComposite;
	CTabFolder sampleTabFolder; 

	/**
	 * Creates an instance of the <code>ColorThemeDemo</code> class.  
	 * 
	 * @param Composite parent   The parent containing the ColorThemeDemo widgets
	 */
	public ColorThemeDemo(Composite parent) {
		createControl(parent);		
	}
	
	/**
	 * Create the set of widgets to display in the receiver.
	 * 
	 * @param Composite parent   The parent containing the ColorThemeDemo widgets
	 */
	private void createControl(Composite parent) {
		Composite marginComposite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginHeight = 1;
		gl.marginWidth = 1;
		
		marginComposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		marginComposite.setLayout(gl);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		marginComposite.setLayoutData(gd);
		
		sampleComposite = new Composite(marginComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		sampleComposite.setLayout(new GridLayout());
		GridData gridData = new GridData(GridData.FILL_BOTH);
		sampleComposite.setData(gridData);
			
		sampleTabFolder = new CTabFolder(sampleComposite, SWT.BORDER);
		sampleTabFolder.setSimpleTab(false);
		sampleTabFolder.setData(new GridData(GridData.FILL_BOTH));
		CTabItem temp = new CTabItem(sampleTabFolder, SWT.NONE);
		temp.setText("Console");
		Text text = new Text(sampleTabFolder, SWT.MULTI);
		text.setText("Lorem ipsum dolor sit amet\n"); //$NON-NLS-1$
		temp.setControl(text);
		sampleTabFolder.setSelection(0);
		temp = new CTabItem(sampleTabFolder, SWT.NONE);
		temp.setText("Search");
		
		resetColors();
	}

	/**
	 * Reset the colors in the receiver.
	 */
	public void resetColors() {
		ColorSchemeService.setTabColors(sampleTabFolder);
	}

	/**
	 * Redraw the receiver.
	 */
	void redraw() {
		sampleTabFolder.redraw();
	}
	
	/**
	 * Set the Selected Tab background 
	 * @param color
	 */
	public void setTabSelectionBGColor(Color color) {
		sampleTabFolder.setSelectionBackground(WorkbenchColors.createGradientArray(sampleTabFolder.getDisplay(), color), WorkbenchColors.getActiveViewGradientPercents(), true); 
	}
	
	/**
	 * Set the Selected Tab foreground 
	 * @param color
	 */
	public void setTabSelectionFGColor(Color color) {
		sampleTabFolder.setSelectionForeground(color);
	}
	
	/**
	 * Set the Tab background 
	 * @param color
	 */
	public void setTabBGColor(Color color) {
		sampleTabFolder.setBackground(WorkbenchColors.createGradientArray(sampleTabFolder.getDisplay(), color), WorkbenchColors.getActiveViewGradientPercents(), true);	
	}

	/**
	 * Set the Selected Tab foreground 
	 * @param color
	 */
	public void setTabFGColor(Color color) {
		sampleTabFolder.setForeground(color);	
	}
	

}
