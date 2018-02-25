/**
 *  Copyright (c) 2017, 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.source.inlined;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Inlined annotation which is drawn before a line and which takes some place with a given height.
 *
 * @since 3.13
 */
public class LineHeaderAnnotation extends AbstractInlinedAnnotation {

	/**
	 * Line header annotation constructor.
	 *
	 * @param position the position where the annotation must be drawn.
	 * @param viewer the {@link ISourceViewer} where the annotation must be drawn.
	 */
	public LineHeaderAnnotation(Position position, ISourceViewer viewer) {
		super(position, viewer);
	}

	/**
	 * Returns the annotation height. By default, returns the {@link StyledText#getLineHeight()}.
	 *
	 * @return the annotation height.
	 */
	public int getHeight() {
		StyledText styledText= super.getTextWidget();
		return styledText.getLineHeight();
	}

	@Override
	protected boolean isInVisibleLines() {
		if (!super.isInVisibleLines()) {
			return false;
		}
		// the inlined annotation is in the visible lines
		ISourceViewer viewer= super.getViewer();
		IDocument document= viewer.getDocument();
		if (document == null) {
			return false;
		}
		try {
			// check if previous line where annotation is drawn in the line spacing, is visible
			int startLineOffset= document.getLineInformationOfOffset(getPosition().getOffset()).getOffset();
			int previousEndLineOffset= startLineOffset - 1;
			return super.isInVisibleLines(previousEndLineOffset);
		} catch (BadLocationException e) {
			return false;
		}
	}

}
