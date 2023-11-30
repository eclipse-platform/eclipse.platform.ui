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
 * Class implementation mining provider.
 */
public class ClassImplementationsCodeMiningProvider extends AbstractCodeMiningProvider {

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
				updateContentMining(i, document, "class ", lenses);
				updateContentMining(i, document, "interface ", lenses);
			}
			return lenses;
		});
	}

	private void updateContentMining(int lineIndex, IDocument document, String token, List<ICodeMining> lenses) {
		String line = AbstractClassCodeMining.getLineText(document, lineIndex).trim();
		int index = line.indexOf(token);
		if (index == 0) {
			String className = line.substring(index + token.length());
			index = className.indexOf(" ");
			if (index != -1) {
				className = className.substring(0, index);
			}
			if (!className.isEmpty()) {
				try {
					lenses.add(new ClassImplementationCodeMining(className, lineIndex, document, this));
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
