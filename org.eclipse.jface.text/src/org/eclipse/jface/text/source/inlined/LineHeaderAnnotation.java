/**
 *  Copyright (c) 2017 Angelo ZERR.
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

/**
 * Inlined annotation which is drawn before a line and which takes some place with a given height.
 *
 * @since 3.13.0
 */
public class LineHeaderAnnotation extends AbstractInlinedAnnotation {

	/**
	 * Line header annotation constructor.
	 *
	 * @param position the position where the annotation must be drawn.
	 * @param textWidget the {@link StyledText} widget where the annotation must be drawn.
	 */
	public LineHeaderAnnotation(Position position, StyledText textWidget) {
		super(position, textWidget);
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

}
