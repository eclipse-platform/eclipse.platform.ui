/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * The NewWorkbenchWindowLayout is the layout for the new look
 * user interface.
 */
public class NewWorkbenchWindowLayout extends Layout {
	
	WorkbenchWindow window;

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
	 */
	protected Point computeSize(
		Composite composite,
		int wHint,
		int hHint,
		boolean flushCache) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
	 */
	protected void layout(Composite composite, boolean flushCache) {
		
		FormLayout layout = new FormLayout();
		composite.setLayout(layout);
		
		FormData menuBarData = new FormData();
		menuBarData.top = new FormAttachment(0);
		menuBarData.left = new FormAttachment(0);
		menuBarData.right = new FormAttachment(100);
		
		//window.getMenuManager().getsetLayoutData(menuBarData);
	
		

	}

}
