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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A PlaceHolder is a non-visible stand-in for a IVisualPart.
 */
public class PartPlaceholder extends LayoutPart {
	public PartPlaceholder(String id) {
		super(id);
	}
/**
 * Creates the SWT control
 */
public void createControl(Composite parent) {
}
/**
 * Get the part control.  This method may return null.
 */
public Control getControl() {
	return null;
}
}
