/**
 *  Copyright (c) 2020, Red Hat Inc.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.jface.text.examples.codemining;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.LineContentCodeMining;

public class ToEchoWithHeaderAndInlineCodeMiningProvider extends AbstractCodeMiningProvider {

	private final String toEcho;

	public ToEchoWithHeaderAndInlineCodeMiningProvider(String keyword) {
		this.toEcho = keyword;
	}

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		int index = 0;
		List<ICodeMining> res = new ArrayList<>();
		while ((index = viewer.getDocument().get().indexOf(toEcho, index)) != -1) {
			index += toEcho.length();
			res.add(new LineContentCodeMining(new Position(index, 1), this) {
					@Override
					public String getLabel() {
						return toEcho;
					}
					
					@Override
					public boolean isResolved() {
						return true;
					}
				});
		};
		return CompletableFuture.completedFuture(res);
	}

}
