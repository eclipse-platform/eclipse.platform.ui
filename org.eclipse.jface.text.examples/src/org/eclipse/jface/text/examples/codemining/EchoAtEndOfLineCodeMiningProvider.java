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
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.LineEndCodeMining;
import org.eclipse.swt.events.MouseEvent;

public class EchoAtEndOfLineCodeMiningProvider extends AbstractCodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		IDocument document = viewer.getDocument();
		List<ICodeMining> res = new ArrayList<>();
		for (int i = 0; i < document.getNumberOfLines(); i++) {
			try {
				if (document.get(document.getLineOffset(i), document.getLineLength(i)).contains("end")) {
					res.add(new LineEndCodeMining(document, i, this) {
						@Override
						public String getLabel() {
							return "End of line";
						}
						@Override
						public boolean isResolved() {
							return true;
						}
						@Override
						public Consumer<MouseEvent> getAction() {
							return e -> System.err.println(getLabel() + getPosition());
						}
					});
				}
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
		}
		return CompletableFuture.completedFuture(res);
	}

	@Override
	public void dispose() {

	}

}
