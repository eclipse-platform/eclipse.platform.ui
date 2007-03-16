/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/**
 * Minimal view, for the RCP tests. 
 */
public class EmptyView extends ViewPart {

    public static final String ID = "org.eclipse.ui.tests.rcp.util.EmptyView"; //$NON-NLS-1$
    
    private Label label;
    
	public EmptyView() {
	    // do nothing
	}

	public void createPartControl(Composite parent) {
	    label = new Label(parent, SWT.NONE);
	    label.setText("Empty view");
	}

	public void setFocus() {
		label.setFocus();
	}
}
