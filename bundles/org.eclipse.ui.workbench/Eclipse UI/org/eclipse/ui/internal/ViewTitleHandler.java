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

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewReference;

/**
 * ViewTitleHandler is a placeholder for no oping the creation of view titles.
 */
public class ViewTitleHandler {

	ViewPane pane;

	/**
	 * Create a new instance of the receiver.
	 */
	public ViewTitleHandler(ViewPane parentPane) {
		super();
		pane = parentPane;
	}

	/**
	 * Return true if <code>x</code> is over the label image.
	 */
	public boolean overImage(int x) {
		return false;
	}

	/**
	 * Return the label created by the receiver. Return null if we don't create
	 * one.
	 * 
	 * @param parent
	 * @return
	 */
	public CLabel createLabel(Composite parent) {
		return null;
	}

	/**
	 * Returns the drag control.
	 */
	public Control getDragHandle() {
		return pane.isvToolBar;
	}
	/**
	 * Return whether or not there is a pane menu.
	 */
	public boolean hasLabel() {
		return getLabel() != null;
	}

	/**
	 * Return the title label or null for this handler/
	 * 
	 * @return
	 */
	public CLabel getLabel() {
		return null;
	}

	/**
	 * Return the label text.
	 * 
	 * @return
	 */
	public String getText() {
		return "No Label";
	}

	/**
	 * Update the label for the suppleid reference.
	 * 
	 * @param ref
	 */
	public void updateLabel(IViewReference ref) {
	}

}
