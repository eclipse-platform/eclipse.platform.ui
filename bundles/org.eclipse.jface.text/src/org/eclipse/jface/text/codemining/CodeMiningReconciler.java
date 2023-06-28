/**
 *  Copyright (c) 2017, 2021 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide extension point for CodeMining - Bug 528419
 *  Christoph Läubrich - Bug 570727 - [codemining] Codeminings computed multiple times
 */
package org.eclipse.jface.text.codemining;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.source.ISourceViewerExtension5;

/**
 * A reconciler which update code minings.
 *
 * @since 3.13
 */
public class CodeMiningReconciler extends Reconciler {

	private static final String KEY= CodeMiningReconciler.class.getName();

	public CodeMiningReconciler() {
		super.setIsIncrementalReconciler(false);
		this.setReconcilingStrategy(new CodeMiningStrategy(() -> getTextViewer()), IDocument.DEFAULT_CONTENT_TYPE);
	}

	@Override
	public void install(ITextViewer textViewer) {
		if (mustInstall(textViewer)) {
			super.install(textViewer);
			textViewer.getTextWidget().setData(KEY, this);
		}
	}

	@Override
	public void uninstall() {
		ITextViewer viewer= getTextViewer();
		if (viewer != null && viewer.getTextWidget() != null && viewer.getTextWidget().getData(KEY) == this) {
			super.uninstall();
			viewer.getTextWidget().setData(KEY, null);
		}
		viewer= null;
	}


	private static boolean mustInstall(ITextViewer textViewer) {
		return textViewer instanceof ISourceViewerExtension5 && textViewer.getTextWidget().getData(KEY) == null;
	}
}
