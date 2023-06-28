/**
 *  Copyright (c) 2018 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [generic editor] Default Code folding for generic editor should use IndentFoldingStrategy - Bug 520659
 */
package org.eclipse.ui.internal.genericeditor.folding;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.AbstractReconciler;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class DefaultFoldingReconciler extends AbstractReconciler {

	private final IndentFoldingStrategy foldingStrategy;

	public DefaultFoldingReconciler() {
		this.foldingStrategy = new IndentFoldingStrategy();
	}

	@Override public void install(ITextViewer textViewer) {
		super.install(textViewer);
		if (textViewer instanceof ProjectionViewer) {
			ProjectionViewer viewer = (ProjectionViewer) textViewer;
			foldingStrategy.setViewer(viewer);
		}
	}

	@Override public void uninstall() {
		super.uninstall();
		if (foldingStrategy != null) {
			foldingStrategy.uninstall();
		}
	}

	@Override protected void process(DirtyRegion dirtyRegion) {
		foldingStrategy.reconcile(dirtyRegion, null);
	}

	@Override protected void reconcilerDocumentChanged(IDocument newDocument) {
		foldingStrategy.setDocument(newDocument);
	}

	@Override public IReconcilingStrategy getReconcilingStrategy(String contentType) {
		return foldingStrategy;
	}

	@Override public void setProgressMonitor(IProgressMonitor monitor) {
		super.setProgressMonitor(monitor);
		foldingStrategy.setProgressMonitor(monitor);
	}

	@Override protected void initialProcess() {
		super.initialProcess();
		foldingStrategy.initialReconcile();
	}
}
