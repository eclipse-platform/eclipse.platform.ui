/************************************************************************
Copyright (c) 2002, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

public class EditorView extends ViewPart {
	private EditorList editorList;
	
/**
 * Constructs a new editorList view.
 */
public EditorView() {
}

/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void createPartControl(Composite parent) {
	IWorkbenchWindow window = getSite().getPage().getWorkbenchWindow();
	editorList = new EditorList(window, null);
	editorList.createControl(parent);
}

/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void dispose() {
	editorList.dispose();
	editorList = null;
	super.dispose();
}

/**
 * @see IWorkbenchPart#setFocus()
 */
public void setFocus() {
	editorList.getControl().setFocus();
}
}