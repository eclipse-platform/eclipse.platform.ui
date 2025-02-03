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
import org.eclipse.swt.graphics.GC;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
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

	private final Consumer<MouseEvent> onMouseHover;

	private final Consumer<MouseEvent> onMouseOut;

	private final Consumer<MouseEvent> onMouseMove;

	/**
	 * Inlined annotation constructor.
	 *
	 * @param position the position where the annotation must be drawn.
	 * @param viewer the {@link ISourceViewer} where the annotation must be drawn.
	 */
	protected AbstractInlinedAnnotation(Position position, ISourceViewer viewer) {
		this(position, viewer, null, null, null);
	}

	/**
	 * Inlined annotation constructor.
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
	protected AbstractInlinedAnnotation(Position position, ISourceViewer viewer, Consumer<MouseEvent> onMouseHover, Consumer<MouseEvent> onMouseOut, Consumer<MouseEvent> onMouseMove) {
		super(TYPE, false, ""); //$NON-NLS-1$
		this.position= position;
		this.onMouseHover= onMouseHover;
		this.onMouseOut= onMouseOut;
		this.onMouseMove= onMouseMove;
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

	final Position computeWidgetPosition(ITextViewer viewer) {
		if (viewer instanceof ITextViewerExtension5 projectionViewer) {
			IRegion region= projectionViewer.modelRange2WidgetRange(new Region(position.getOffset(), position.getLength()));
			if (region != null) {
				return new Position(region.getOffset(), region.getLength());
			} else {
				return null;
			}
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
		if (text == null || text.isDisposed()) {
			return;
		}
		text.getDisplay().execute(() -> {
			if (text.isDisposed()) {
				return;
			}
			try {
				Position pos= getPosition();
				int offset= pos.getOffset();
				ISourceViewer viewer= getViewer();
				if (viewer instanceof ITextViewerExtension5) {
					// adjust offset according folded content
					offset= ((ITextViewerExtension5) viewer).modelOffset2WidgetOffset(offset);
				}
				InlinedAnnotationDrawingStrategy.draw(this, null, text, offset, pos.getLength(), null);
			} catch (RuntimeException e) {
				// Ignore UI error
			}
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
		if (onMouseHover != null) {
			onMouseHover.accept(e);
		} else {
			StyledText styledText= (StyledText) e.widget;
			styledText.setCursor(styledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		}
	}

	/**
	 * Called when mouse moved in the inlined annotation.
	 *
	 * @param e the mouse event
	 * @since 3.28
	 */
	public void onMouseMove(MouseEvent e) {
		if (onMouseMove != null) {
			onMouseMove.accept(e);
		}
	}

	/**
	 * Called when mouse out the inlined annotation.
	 *
	 * @param e the mouse event
	 */
	public void onMouseOut(MouseEvent e) {
		if (onMouseOut != null) {
			onMouseOut.accept(e);
		} else {
			StyledText styledText= (StyledText) e.widget;
			styledText.setCursor(null);
		}
	}

	/**
	 * @param e MouseEvent to be used by overrides
	 */
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
		this.fViewer = support.getViewer();
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

	boolean isFirstVisibleOffset(int widgetOffset, ITextViewer viewer) {
		if (viewer == null) {
			// fail back to initial viewer
			viewer = fViewer;
		}
		if (viewer instanceof ProjectionViewer projectionViewer) {
			IRegion widgetRange= projectionViewer.modelRange2WidgetRange(new Region(position.getOffset(), position.getLength()));
			return widgetRange != null && widgetOffset == widgetRange.getOffset();
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
