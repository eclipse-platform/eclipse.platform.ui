/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide extension point for CodeMining - Bug 528419
 */
package org.eclipse.jface.text.codemining;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.ISourceViewerExtension5;

/**
 * A reconciling strategy which updates code minings.
 *
 * @since 3.13
 */
class CodeMiningStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private ISourceViewerExtension5 fViewer;

	public void install(ITextViewer viewer) {
		if (viewer instanceof ISourceViewerExtension5) {
			fViewer= (ISourceViewerExtension5) viewer;
		}
	}

	@Override
	public void initialReconcile() {
		if (fViewer != null) {
			// FIXME: this update is done because minings is not updated on focus by AbstractTextEditor#setFocus
			// But I'm a little afraid to update minings each time editor will have focus
			// @Mickael what do you think about doing update minings on AbstractTextEditor#setFocus ?
			fViewer.updateCodeMinings();
		}
	}

	@Override
	public void reconcile(IRegion partition) {
		if (fViewer != null) {
			fViewer.updateCodeMinings();
		}
	}

	public void uninstall() {
		fViewer= null;
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// Do nothing
	}

	@Override
	public void setDocument(IDocument document) {
		// Do nothing
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// Do nothing
	}

}
