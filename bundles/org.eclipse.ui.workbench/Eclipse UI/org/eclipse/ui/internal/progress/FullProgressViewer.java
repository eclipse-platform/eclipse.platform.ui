/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.progress;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * The FullProgressViewer is the viewer that displays jobs with full progress.
 * 
 */
public class FullProgressViewer extends AbstractProgressViewer {

	Composite control;

	/**
	 * Creates a table viewer on a newly-created table control under the given
	 * parent. The table control is created using the given style bits. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters. The table has no columns.
	 * 
	 * @param parent
	 *            the parent control
	 * @param style
	 *            SWT style bits
	 */
	public FullProgressViewer(Composite parent, int style) {
		control = new Composite(parent, style);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindInputItem(java.lang.Object)
	 */
	protected Widget doFindInputItem(Object element) {
		if (element == ProgressManager.getInstance())
			return control;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindItem(java.lang.Object)
	 */
	protected Widget doFindItem(Object element) {
		Control[] children = control.getChildren();
		for (int i = 0; i < children.length; i++) {
			ProgressItem control = (ProgressItem) children[i];
			if(control.getElement().equals(element))
				return control;
		}
		return null;
	}

	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		// TODO Auto-generated method stub

	}

	protected List getSelectionFromWidget() {
		// TODO Auto-generated method stub
		return null;
	}

	protected void internalRefresh(Object element) {
		// TODO Auto-generated method stub

	}

	public void reveal(Object element) {
		// TODO Auto-generated method stub

	}

	protected void setSelectionToWidget(List l, boolean reveal) {
		// TODO Auto-generated method stub

	}

	public Control getControl() {
		// TODO Auto-generated method stub
		return null;
	}

}
