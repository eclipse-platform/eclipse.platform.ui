/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

/**
 * The PerspectiveBarManager is the tool bar manager used for the perspective
 * bar.
 */
public class PerspectiveBarManager extends ToolBarManager {

	
	/**
	 * The symbolic font name for the small font (value <code>"org.eclipse.jface.smallfont"</code>).
	 */

	public static final String SMALL_FONT = "org.eclipse.ui.smallFont"; //$NON-NLS-1$
	
	
	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param style
	 */
	public PerspectiveBarManager(int style) {
		super(style);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ToolBarManager#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public ToolBar createControl(Composite parent) {
		ToolBar control =  super.createControl(parent);
		control.setFont(getFont());
		return control;
	}

	/**
	 * Create a new instance of the receiver with the supplied 
	 * tool bar.
	 * @param toolbar
	 */
	public PerspectiveBarManager(ToolBar toolbar) {
		super(toolbar);
		toolbar.setFont(getFont());
	}
	
	/**
	 * Update the font value to the current setting of SMALL_FONT.
	 */
	public void updateFont(){
		getControl().setFont(getFont());
	}
	
	/**
	 * Return the Font we use for the toolbat.
	 * @return Font
	 */
	private Font getFont(){
		return JFaceResources.getFont(SMALL_FONT);
	}

}
