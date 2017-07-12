/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lucas Bullen (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.examples.dotproject;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class FoldingReconciler extends Reconciler {

	private FoldingStrategy fStrategy;

	public FoldingReconciler() {
        fStrategy = new FoldingStrategy();
        this.setReconcilingStrategy(fStrategy, IDocument.DEFAULT_CONTENT_TYPE);
    }

    @Override
    public void install(ITextViewer textViewer) {
    	super.install(textViewer);
    	ProjectionViewer pViewer =(ProjectionViewer)textViewer;
    	fStrategy.setProjectionViewer(pViewer);
    }
}