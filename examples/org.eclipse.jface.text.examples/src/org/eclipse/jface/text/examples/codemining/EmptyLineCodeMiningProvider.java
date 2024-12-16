/*******************************************************************************
 *  Copyright (c) 2022, Red Hat Inc. and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
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
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;

public class EmptyLineCodeMiningProvider extends AbstractCodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		IDocument document = viewer.getDocument();
		List<LineHeaderCodeMining> emptyLineHeaders = new ArrayList<>();
		for (int line = 0; line < document.getNumberOfLines(); line++) {
			try {
				if (document.getLineLength(line) == 1) {
					emptyLineHeaders.add(new LineHeaderCodeMining(line, document, this) {
						@Override
						public String getLabel() {
							return "Next line is \nempty";
						}
					});
				}
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
		}
		return CompletableFuture.completedFuture(emptyLineHeaders);
	}

}
