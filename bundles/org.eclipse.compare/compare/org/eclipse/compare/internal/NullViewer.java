/*
 * Copyright (c) 2000, 2003 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import org.eclipse.compare.CompareViewerSwitchingPane;

/**
 * Used whenever the input is null or no viewer can be found.
 */
public class NullViewer extends AbstractViewer {

	private Control fDummy;

	public NullViewer(Composite parent) {

		fDummy= new Tree(parent, SWT.NULL);

		CompareViewerSwitchingPane.clearToolBar(parent);
	}

	public Control getControl() {
		return fDummy;
	}
}
