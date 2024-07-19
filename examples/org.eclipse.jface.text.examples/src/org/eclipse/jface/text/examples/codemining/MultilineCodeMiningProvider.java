/*******************************************************************************
 *  Copyright (c) 2024, SAP.
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
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMining;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;

public class MultilineCodeMiningProvider extends AbstractCodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		String multiLineText = "multiline";
		IDocument document = viewer.getDocument();
		List<ICodeMining> res = new ArrayList<>();
		int index = 0;
		while ((index = document.get().indexOf(multiLineText, index)) != -1) {
			index += multiLineText.length();
			res.add(new AbstractCodeMining(new Position(index, 1), this, null) {
				@Override
				public String getLabel() {
					return "multiline first part in same line";
				}
			});
			try {
				int line = document.getLineOfOffset(index);
				String lineDelimiter = document.getLineDelimiter(line);
				res.add(new LineHeaderCodeMining(line + 1, document, this) {
					@Override
					public String getLabel() {
						return "multiline second line" + lineDelimiter + //
								"multiline third line" + lineDelimiter + //
								"multiline fourth line"+ lineDelimiter + //
								"multiline fifth line";
					}
				});
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return CompletableFuture.completedFuture(res);
	}

}
