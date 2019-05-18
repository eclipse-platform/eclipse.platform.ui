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
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide extension point for CodeMining - Bug 528419
 */
package org.eclipse.ui.genericeditor.examples.dotproject.codemining;

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
 * Project reference minings provider.
 */
public class ProjectReferencesCodeMiningProvider extends AbstractCodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			IDocument document = viewer.getDocument();
			List<ICodeMining> minings = new ArrayList<>();
			int lineCount = document.getNumberOfLines();
			for (int i = 0; i < lineCount; i++) {
				// check if request was canceled.
				monitor.isCanceled();
				String line = getLineText(document, i).trim();
				int startIndex = line.indexOf("<name>");
				if (startIndex != -1) {
					// It's the first name, we consider we are in <projectDescription></name>
					startIndex += "<name>".length();
					int endIndex = line.indexOf("</name>");
					if (endIndex > startIndex) {
						// Check if parent element is projectDescription
						String projectName = line.substring(startIndex, endIndex);
						if (projectName.length() > 0) {
							try {
								minings.add(new ProjectReferenceCodeMining(projectName, i, document, this));
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
						}
					}
					// stop the compute of minings to avoid process other name like
					// <buildCommand><name>
					break;
				}
			}
			return minings;
		});
	}

	private static String getLineText(IDocument document, int line) {
		try {
			int lo = document.getLineOffset(line);
			int ll = document.getLineLength(line);
			return document.get(lo, ll);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
