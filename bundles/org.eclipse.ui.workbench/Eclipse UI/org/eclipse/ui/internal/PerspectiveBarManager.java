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
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

public class PerspectiveBarManager extends ToolBarManager {
	
	/**
	 * The symbolic font name for the small font (value <code>"org.eclipse.jface.smallfont"</code>).
	 */
	public static final String SMALL_FONT = "org.eclipse.ui.smallFont"; //$NON-NLS-1$

	public PerspectiveBarManager(int style) {
		super(style);	
	}

	public ToolBar createControl(Composite parent) {
		ToolBar control =  super.createControl(parent);
		control.setFont(getFont());
		return control;
	}
	
	public PerspectiveBarManager(ToolBar toolbar) {
		super(toolbar);
		toolbar.setFont(getFont());
	}
	
	// TODO begin refactor this out? 
	private CBanner banner;
	
	void layout(boolean b) {
		if (banner != null)
			banner.layout(b);
	}
	
	void setBanner(CBanner banner) {
		this.banner = banner;
	}
	// TODO end refactor this out? 

	public void updateFont(){
		getControl().setFont(getFont());
	}
	
	private Font getFont(){
		return JFaceResources.getFont(SMALL_FONT);
	}
}
