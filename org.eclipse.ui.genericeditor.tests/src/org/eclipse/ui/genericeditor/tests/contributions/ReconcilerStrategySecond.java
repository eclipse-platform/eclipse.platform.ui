/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Lucas Bullen (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

public class ReconcilerStrategySecond implements IReconcilingStrategy, IReconcilingStrategyExtension{

	IDocument document;
	public static final String SEARCH_TERM = "BAR";
	public static final String REPLACEMENT = "second";

	@Override
	public void setDocument(IDocument document) {
		this.document = document;
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		initialReconcile();
	}

	@Override
	public void reconcile(IRegion partition) {
		initialReconcile();
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// no progress monitor in use
	}

	@Override
	public void initialReconcile() {
		String doc = document.get();
		if(doc.contains(SEARCH_TERM)) {
			Display.getDefault().asyncExec(() -> {
				document.set(document.get().replaceAll(SEARCH_TERM, REPLACEMENT));
			});
		}
	}

}
