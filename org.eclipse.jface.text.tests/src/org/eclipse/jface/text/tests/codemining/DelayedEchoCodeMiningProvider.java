/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.codemining;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;

public class DelayedEchoCodeMiningProvider extends AbstractCodeMiningProvider {

	public static int DELAY = 0;

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return null;
			}
			IDocument document = viewer.getDocument();
			List<ICodeMining> res = new ArrayList<>();
			for (int lineNumber = 0; lineNumber < document.getNumberOfLines(); lineNumber++) {
				try {
					String lineContent = document.get(document.getLineOffset(lineNumber), document.getLineLength(lineNumber));
					if (!lineContent.trim().isEmpty()) {
						LineHeaderCodeMining mining = new LineHeaderCodeMining(lineNumber, document, DelayedEchoCodeMiningProvider.this) {
							// Nothing in particular
						};
						mining.setLabel(lineContent);
						res.add(mining);
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}

			}
			return res;
		});
	}

}
