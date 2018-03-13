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

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Inlined annotation which is drawn before a line and which takes some place with a given height.
 *
 * @since 3.13
 */
public class LineHeaderAnnotation extends AbstractInlinedAnnotation {

	private int redrawnCharacterHeight;

	private int redrawnCharacterWidth;

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

	int getRedrawnCharacterHeight() {
		return redrawnCharacterHeight;
	}

	void setRedrawnCharacterHeight(int redrawnCharacterHeight) {
		this.redrawnCharacterHeight= redrawnCharacterHeight;
	}

	int getRedrawnCharacterWidth() {
		return redrawnCharacterWidth;
	}

	void setRedrawnCharacterWidth(int redrawnCharacterWidth) {
		this.redrawnCharacterWidth= redrawnCharacterWidth;
	}
}
