/*******************************************************************************
* Copyright (c) 2025 SAP SE
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/
package org.eclipse.jface.text.source.inlined;

import java.util.function.Consumer;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Inlined annotation which is drawn after a line and which takes some place with a given height.
 *
 * @since 3.28
 */
public class LineFooterAnnotation extends AbstractInlinedAnnotation {


	protected LineFooterAnnotation(Position position, ISourceViewer viewer, Consumer<MouseEvent> onMouseHover, Consumer<MouseEvent> onMouseOut, Consumer<MouseEvent> onMouseMove) {
		super(position, viewer, onMouseHover, onMouseOut, onMouseMove);
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
