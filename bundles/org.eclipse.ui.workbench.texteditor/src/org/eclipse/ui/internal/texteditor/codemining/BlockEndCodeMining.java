/*******************************************************************************
 * Copyright (c) 2026 Eclipse Platform contributors.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.codemining;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineEndCodeMining;

/** Echoes a block's opening line at the end of its closing brace line. */
class BlockEndCodeMining extends LineEndCodeMining {

	private final String label;

	BlockEndCodeMining(IDocument document, int line, String label, ICodeMiningProvider provider) throws BadLocationException {
		super(document, line, provider);
		this.label= label;
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public String getLabel() {
		return label;
	}
}
