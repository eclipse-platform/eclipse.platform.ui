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
package org.eclipse.jface.text.codemining;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * A code mining that is positioned on end of a line.
 *
 * @since 3.20
 */
public abstract class LineEndCodeMining extends LineContentCodeMining {

	protected LineEndCodeMining(IDocument document, int line, ICodeMiningProvider provider) throws BadLocationException {
		super(getLineEndPosition(document, line), provider);
	}

	private static Position getLineEndPosition(IDocument document, int line) throws BadLocationException {
		int lastCharOffset= document.getLineOffset(line) + document.getLineLength(line);
		String delimiter= document.getLineDelimiter(line);
		return delimiter == null ? // last line of document
				new Position(document.getLength(), 0) : //
				new Position(lastCharOffset - delimiter.length(), delimiter.length());
	}

}
