/**
 *  Copyright (c) 2017 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Add CodeMining support in SourceViewer - Bug 527515
 */
package org.eclipse.jface.text.examples.codemining;

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;

/**
 * Class implementation mining.
 */
public class ClassImplementationCodeMining extends AbstractClassCodeMining {

	public ClassImplementationCodeMining(String className, int afterLineNumber, IDocument document,
			ICodeMiningProvider provider) throws BadLocationException {
		super(className, afterLineNumber, document, provider);
	}

	@Override
	protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
		return CompletableFuture.runAsync(() -> {
			IDocument document = viewer.getDocument();
			String className = super.getClassName();
			int refCount = 0;
			int lineCount = document.getNumberOfLines();
			for (int i = 0; i < lineCount; i++) {
				// check if request was canceled.
				monitor.isCanceled();
				String line = getLineText(document, i);
				refCount += line.contains("implements " + className) ? 1 : 0;
			}
			super.setLabel(refCount + " implementation");
		});
	}

}
