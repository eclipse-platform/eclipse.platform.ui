/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


class LineSelectionTextTripleClickStrategy  {

	/*
	 * @see org.eclipse.jface.text.ITextTipleClickStrategy#tripleClicked(org.eclipse.jface.text.ITextViewer)
	 */
	static void tripleClicked(ITextViewer text) {
		int position= text.getSelectedRange().x;

		if (position < 0)
			return;

		try {
			IDocument document= text.getDocument();
			int lineNumber= document.getLineOfOffset(position);
			String lineDelimiter= document.getLineDelimiter(lineNumber);
			int lineDelimiterLength= lineDelimiter != null ? lineDelimiter.length() : 0;
			IRegion line= document.getLineInformationOfOffset(position);
			text.setSelectedRange(line.getOffset(), line.getLength() + lineDelimiterLength);
		} catch (BadLocationException x) {
		}
	}
}
