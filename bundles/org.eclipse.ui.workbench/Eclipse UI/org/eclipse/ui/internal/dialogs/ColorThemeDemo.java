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

import org.eclipse.jface.window.ColorSchemeService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder2;
import org.eclipse.swt.custom.CTabItem2;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Class to encapsulate the set of widgets that will be used to indicate 
 * the current color theme of the workbench and give feedback on 
 * changes made.
 */
public class ColorThemeDemo {

	Composite sampleComposite;
	CTabFolder2 sampleTabFolder; 
	CLabel sampleClabel;
		
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
		marginComposite.setBackground(new Color(parent.getDisplay(), 0,0,0));
		marginComposite.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_BOTH);
		
		sampleComposite = new Composite(marginComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		GridLayout gl2 = new GridLayout();
		gl2.marginHeight = 0;
		gl2.marginWidth = 0;
		sampleComposite.setLayout(gl2);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		sampleComposite.setData(gridData);
			
		sampleTabFolder = new CTabFolder2(sampleComposite, SWT.BORDER);
		sampleTabFolder.setData(new GridData(GridData.FILL_BOTH));
		CTabItem2 temp = new CTabItem2(sampleTabFolder, SWT.NONE);
		temp.setText("Console");
		Text text = new Text(sampleTabFolder, SWT.MULTI);
		text.setText("Lorem ipsum dolor sit amet\n");
		temp.setControl(text);
		sampleTabFolder.setSelection(0);
		temp = new CTabItem2(sampleTabFolder, SWT.NONE);
		temp.setText("Search");

		CLabel clabel = new CLabel(sampleComposite, SWT.NONE);
		clabel.setText("Status Text");
		clabel.setData(new GridData(GridData.FILL_HORIZONTAL));
		
		resetColors();
		
		marginComposite.setLayoutData(new GridData());
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
		sampleTabFolder.setSelectionBackground(color);
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
		sampleTabFolder.setBackground(color);	
	}

	/**
	 * Set the Selected Tab foreground 
	 * @param color
	 */
	public void setTabFGColor(Color color) {
		sampleTabFolder.setForeground(color);	
	}
	

}
