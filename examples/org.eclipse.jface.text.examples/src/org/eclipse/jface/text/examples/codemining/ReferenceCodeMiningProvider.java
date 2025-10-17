/*******************************************************************************
 *  Copyright (c) 2025, Advantest Europe GmbH
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *  
 *  Contributors:
 *  Dietrich Travkin <dietrich.travkin@solunar.de> - Fix code mining redrawing - Issue 3405
 *  
 *******************************************************************************/
package org.eclipse.jface.text.examples.codemining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;

public class ReferenceCodeMiningProvider extends AbstractCodeMiningProvider {

	private static final String REGEX_REF = "\\[REF-X\\]|\\[REF-Y\\]";
	private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX_REF);

	private AtomicReference<Boolean> useInLineCodeMinings;

	public ReferenceCodeMiningProvider(AtomicReference<Boolean> useInLineCodeMinings) {
		this.useInLineCodeMinings = useInLineCodeMinings;
	}

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			IDocument document = viewer.getDocument();

			if (document == null) {
				return Collections.emptyList();
			}

			return createCodeMiningsFor(document);
		});
	}

	List<ICodeMining> createCodeMiningsFor(IDocument document) {
		String documentContent = document.get();
		List<ICodeMining> minings = new ArrayList<>();

		Matcher regexMatcher = REGEX_PATTERN.matcher(documentContent);
		while (regexMatcher.find()) {
			String matchedText = regexMatcher.group();
			int startIndex = regexMatcher.start();

			String title = matchedText.endsWith("X]") ? "Plugging into Eclipse"
					: "Building commercial quality plug-ins";

			if (useInLineCodeMinings.get()) {
				minings.add(new ReferenceInLineCodeMining(title + ": ", startIndex, document, this));
			} else {
				try {
					int offset = startIndex;
					int line = document.getLineOfOffset(offset);
					int lineOffset = document.getLineOffset(line);

					minings.add(new ReferenceLineHeaderCodeMining(title, line, offset - lineOffset, title.length(),
							document, this));
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}

		return minings;
	}

}
