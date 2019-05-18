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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;

/**
 * Class reference mining provider.
 *
 */
public class ClassReferenceCodeMiningProvider extends AbstractCodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			IDocument document = viewer.getDocument();
			List<ICodeMining> lenses = new ArrayList<>();
			int lineCount = document.getNumberOfLines();
			for (int i = 0; i < lineCount; i++) {
				// check if request was canceled.
				monitor.isCanceled();
				String line = AbstractClassCodeMining.getLineText(document, i).trim();
				int index = line.indexOf("class ");
				if (index == 0) {
					String className = line.substring(index + "class ".length(), line.length()).trim();
					if (className.length() > 0) {
						try {
							lenses.add(new ClassReferenceCodeMining(className, i, document, this));
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return lenses;
		});
	}

}
