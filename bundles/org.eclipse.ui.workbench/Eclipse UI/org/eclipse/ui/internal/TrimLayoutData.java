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
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;

/**
 */
public class TrimLayoutData {
	int widthHint = SWT.DEFAULT;
	int heightHint = SWT.DEFAULT;
	boolean resizable = true;
	
	public TrimLayoutData() {
		this(true, SWT.DEFAULT, SWT.DEFAULT);
	}
	
	public TrimLayoutData(boolean resizable, int widthHint, int heightHint) {
		this.widthHint = widthHint;
		this.heightHint = heightHint;
		this.resizable = resizable;
	}
	
}
