/**
 *  Copyright (c) 2017, 2018 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.source.inlined;

import java.util.function.Consumer;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Inlined annotation which is drawn before a line and which takes some place with a given height.
 *
 * @since 3.13
 */
public class LineHeaderAnnotation extends AbstractInlinedAnnotation {

	int oldLine;

	/**
	 * Line header annotation constructor.
	 *
	 * @param position the position where the annotation must be drawn.
	 * @param viewer the {@link ISourceViewer} where the annotation must be drawn.
	 */
	public LineHeaderAnnotation(Position position, ISourceViewer viewer) {
		this(position, viewer, null, null, null);
	}

	/**
	 * Line header annotation constructor.
	 *
	 * @param position the position where the annotation must be drawn.
	 * @param viewer the {@link ISourceViewer} where the annotation must be drawn.
	 * @param onMouseHover the consumer to be called on mouse hover. If set, the implementor needs
	 *            to take care of setting the cursor if wanted.
	 * @param onMouseOut the consumer to be called on mouse out. If set, the implementor needs to
	 *            take care of resetting the cursor.
	 * @param onMouseMove the consumer to be called on mouse move
	 * @since 3.28
	 */
	public LineHeaderAnnotation(Position position, ISourceViewer viewer, Consumer<MouseEvent> onMouseHover, Consumer<MouseEvent> onMouseOut, Consumer<MouseEvent> onMouseMove) {
		super(position, viewer, onMouseHover, onMouseOut, onMouseMove);
		oldLine= -1;
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
	boolean contains(int x, int y) {
		return (x >= this.fX && y >= this.fY && y <= this.fY + getHeight());
	}
}
