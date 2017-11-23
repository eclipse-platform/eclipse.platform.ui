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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

/**
 * Abstract class for inlined annotation.
 *
 * @since 3.13.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public abstract class AbstractInlinedAnnotation extends Annotation {

	/**
	 * The type of inlined annotations.
	 */
	public static final String TYPE= "org.eclipse.jface.text.source.inlined"; //$NON-NLS-1$

	/**
	 * The position where the annotation must be drawn.
	 */
	private final Position position;

	/**
	 * The {@link StyledText} widget where the annotation must be drawn.
	 */
	private final StyledText textWidget;

	/**
	 * Inlined annotation constructor.
	 *
	 * @param position the position where the annotation must be drawn.
	 * @param textWidget the {@link StyledText} widget where the annotation must be drawn.
	 */
	protected AbstractInlinedAnnotation(Position position, StyledText textWidget) {
		super(TYPE, false, ""); //$NON-NLS-1$
		this.position= position;
		this.textWidget= textWidget;
	}

	/**
	 * Returns the position where the annotation must be drawn.
	 *
	 * @return the position where the annotation must be drawn.
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Returns the {@link StyledText} widget where the annotation must be drawn.
	 *
	 * @return the {@link StyledText} widget where the annotation must be drawn.
	 */
	public StyledText getTextWidget() {
		return textWidget;
	}

	/**
	 * Redraw the inlined annotation.
	 */
	public void redraw() {
		StyledText text= getTextWidget();
		InlinedAnnotationSupport.runInUIThread(text, (t) -> {
			Position pos= getPosition();
			InlinedAnnotationDrawingStrategy.draw(this, null, t, pos.getOffset(), pos.getLength(), null);
		});
	}

	/**
	 * Draw the inlined annotation. By default it draw the text of the annotation with gray color.
	 * User can override this method to draw anything.
	 *
	 * @param gc the graphics context
	 * @param textWidget the text widget to draw on
	 * @param offset the offset of the line
	 * @param length the length of the line
	 * @param color the color of the line
	 * @param x the x position of the annotation
	 * @param y the y position of the annotation
	 */
	public void draw(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		gc.setForeground(color);
		gc.setBackground(textWidget.getBackground());
		gc.drawText(getText(), x, y);
	}

}
