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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.jface.text.source.inlined.Positions;

public class ReferenceLineHeaderCodeMining extends LineHeaderCodeMining {

	public ReferenceLineHeaderCodeMining(String label, int beforeLineNumber, int columnInLine, int length,
			IDocument document, ICodeMiningProvider provider) throws BadLocationException {
		super(calculatePosition(beforeLineNumber, columnInLine, document), provider, null);
		this.setLabel(label);
	}

	private static Position calculatePosition(int beforeLineNumber, int columnInLine, IDocument document)
			throws BadLocationException {
		Position pos = Positions.of(beforeLineNumber, document, true);
		pos.setOffset(pos.offset + columnInLine);
		return pos;
	}

}
