/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.TreeViewer;

class RetrieverTreeViewer extends TreeViewer {
	private boolean fPreservingSelection= false;

	public RetrieverTreeViewer(Composite parent, int options) {
		super(parent, options);
	}

	public void preservingSelection(Runnable updateCode) {
		if (!fPreservingSelection) {
			fPreservingSelection= true;
			try {
				super.preservingSelection(updateCode);
			} finally {
				fPreservingSelection= false;
			}
		} else {
			updateCode.run();
		}
	}
}
