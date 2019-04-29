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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

/**
 * Abstract class for inlined annotation.
 *
 * @since 3.13
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
	 * The {@link ISourceViewer} where the annotation must be drawn.
	 */
	private ISourceViewer fViewer;

	/**
	 * The {@link InlinedAnnotationSupport} which manages the annotation.
	 */
	private InlinedAnnotationSupport support;

	int fX;

	int fY;

	/**
	 * Inlined annotation constructor.
	 *
	 * @param position the position where the annotation must be drawn.
	 * @param viewer   the {@link ISourceViewer} where the annotation must be drawn.
	 */
	protected AbstractInlinedAnnotation(Position position, ISourceViewer viewer) {
		super(TYPE, false, ""); //$NON-NLS-1$
		this.position= position;
		fViewer= viewer;
	}

	/**
	 * Returns the position where the annotation must be drawn. For {@link ITextViewerExtension5}
	 * (enabling folding with widget/model projection), this position is the <strong>model</strong>
	 * position.
	 *
	 * @return the model position where the annotation must be drawn.
	 */
	public Position getPosition() {
		return position;
	}

	final Position computeWidgetPosition() {
		if (fViewer instanceof ITextViewerExtension5) {
			IRegion region= ((ITextViewerExtension5) fViewer).modelRange2WidgetRange(new Region(position.getOffset(), position.getLength()));
			return new Position(region.getOffset(), region.getLength());
		}
		return position;
	}

	/**
	 * Returns the {@link StyledText} widget where the annotation must be drawn.
	 *
	 * @return the {@link StyledText} widget where the annotation must be drawn.
	 */
	public StyledText getTextWidget() {
		return fViewer.getTextWidget();
	}

	/**
	 * Returns the {@link ISourceViewer} where the annotation must be drawn.
	 *
	 * @return the {@link ISourceViewer} where the annotation must be drawn.
	 */
	public ISourceViewer getViewer() {
		return fViewer;
	}

	/**
	 * Redraw the inlined annotation.
	 */
	public void redraw() {
		StyledText text= getTextWidget();
		InlinedAnnotationSupport.runInUIThread(text, (t) -> {
			Position pos= getPosition();
			int offset= pos.getOffset();
			ISourceViewer viewer= getViewer();
			if (viewer instanceof ITextViewerExtension5) {
				// adjust offset according folded content
				offset= ((ITextViewerExtension5) viewer).modelOffset2WidgetOffset(offset);
			}
			InlinedAnnotationDrawingStrategy.draw(this, null, t, offset, pos.getLength(), null);
		});
	}

	/**
	 * Draw the inlined annotation. By default it draw the text of the annotation with gray color.
	 * User can override this method to draw anything.
	 *
	 * @param gc the graphics context
	 * @param textWidget the text widget to draw on
	 * @param widgetOffset the offset
	 * @param length the length of the line
	 * @param color the color of the line
	 * @param x the x position of the annotation
	 * @param y the y position of the annotation
	 */
	public void draw(GC gc, StyledText textWidget, int widgetOffset, int length, Color color, int x, int y) {
		gc.setForeground(color);
		gc.setBackground(textWidget.getBackground());
		gc.drawString(getText(), x, y, true);
	}

	/**
	 * Called when mouse over the inlined annotation.
	 *
	 * @param e the mouse event
	 */
	public void onMouseHover(MouseEvent e) {
		StyledText styledText= (StyledText) e.widget;
		styledText.setCursor(styledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
	}

	/**
	 * Called when mouse out the inlined annotation.
	 *
	 * @param e the mouse event
	 */
	public void onMouseOut(MouseEvent e) {
		StyledText styledText= (StyledText) e.widget;
		styledText.setCursor(null);
	}

	public Consumer<MouseEvent> getAction(MouseEvent e) {
		return null;
	}

	/**
	 * Set the inlined annotation support which manages this annotation.
	 *
	 * @param support the inlined annotation support which manages this annotation.
	 */
	void setSupport(InlinedAnnotationSupport support) {
		this.support= support;
	}

	/**
	 * Return whether the annotation is in visible lines.
	 *
	 * @return <code>true</code> if the annotation is in visible lines and <code>false</code>
	 *         otherwise.
	 */
	protected boolean isInVisibleLines() {
		return support.isInVisibleLines(getPosition().getOffset());
	}

	boolean isFirstVisibleOffset(int widgetOffset) {
		if (fViewer instanceof ProjectionViewer) {
			IRegion widgetRange= ((ProjectionViewer) fViewer).modelRange2WidgetRange(new Region(position.getOffset(), position.getLength()));
			return widgetOffset == widgetRange.getOffset();
		} else {
			return position.getOffset() == widgetOffset;
		}
	}

	/**
	 * Return whether the given offset is in visible lines.
	 *
	 * @param offset the offset
	 * @return <code>true</code> if the given offset is in visible lines and <code>false</code>
	 *         otherwise.
	 */
	protected boolean isInVisibleLines(int offset) {
		return support.isInVisibleLines(offset);
	}

	/**
	 * Returns the font according the specified <code>style</code> that the receiver will use to
	 * paint textual information.
	 *
	 * @param style the style of Font widget to get.
	 * @return the receiver's font according the specified <code>style</code>
	 *
	 */
	Font getFont(int style) {
		return support.getFont(style);
	}

	/**
	 * Set the location where the annotation is drawn.
	 *
	 * @param x the x coordinate where draw of annotation starts.
	 * @param y the y coordinate where draw of annotation starts.
	 */
	void setLocation(int x, int y) {
		this.fX= x;
		this.fY= y;
	}

	/**
	 * Returns <code>true</code> if the point specified by the arguments is inside the annotation
	 * specified by the receiver, and <code>false</code> otherwise.
	 *
	 * @param x the x coordinate of the point to test for containment
	 * @param y the y coordinate of the point to test for containment
	 * @return <code>true</code> if the annotation contains the point and <code>false</code>
	 *         otherwise
	 */
	boolean contains(int x, int y) {
		StyledText styledText= getTextWidget();
		GC gc= null;
		try {
			gc= new GC(styledText);
			return x >= fX && y >= fY && y <= fY + styledText.getLineHeight(position.getOffset()) && x <= fX + gc.stringExtent(getText()).x + 2 * gc.getFontMetrics().getAverageCharacterWidth();
		} finally {
			if (gc != null) {
				gc.dispose();
			}
		}
	}
}
