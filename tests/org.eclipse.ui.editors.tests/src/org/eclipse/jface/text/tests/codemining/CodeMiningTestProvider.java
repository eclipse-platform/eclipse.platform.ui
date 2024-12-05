/*******************************************************************************
 * Copyright (c) 2024 SAP
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests.codemining;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMining;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;

public class CodeMiningTestProvider extends AbstractCodeMiningProvider {
	public static int provideHeaderMiningAtLine = -1;
	public static int provideContentMiningAtOffset = -1;
	public static String lineHeaderMiningText;
	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		try {
			List<ICodeMining> minings = new ArrayList<>();
			// used as indication when the code minings are finished with
			// drawing - widget.getStyleRangeAtOffset(0).metrics
			minings.add(new StaticContentLineCodeMining(new Position(1, 1), "mining", this));
			if (provideHeaderMiningAtLine >= 0) {
				minings.add(new LineHeaderCodeMining(provideHeaderMiningAtLine, viewer.getDocument(), this) {
					@Override
					public String getLabel() {
						if (lineHeaderMiningText != null) {
							return lineHeaderMiningText;
						}
						return "line header mining";
					}
				});
			}
			if (provideContentMiningAtOffset >= 0) {
				minings.add(new AbstractCodeMining(new Position(provideContentMiningAtOffset, 1), this, null) {
					@Override
					public String getLabel() {
						return "Content mining";
					}
				});
			}
			return CompletableFuture.completedFuture(minings);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static final class StaticContentLineCodeMining extends LineContentCodeMining {

		public StaticContentLineCodeMining(Position position, String message, ICodeMiningProvider provider) {
			super(position, provider);
			setLabel(message);
		}

		public StaticContentLineCodeMining(int i, char c, ICodeMiningProvider repeatLettersCodeMiningProvider) {
			super(new Position(i, 1), repeatLettersCodeMiningProvider);
			setLabel(Character.toString(c));
		}

		@Override
		public boolean isResolved() {
			return true;
		}
	}
}
