/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.TextEvent;


/**
 * Abstract implementation of a {@link IVerticalRulerColumn} that
 * uses a {@link Canvas} to draw the ruler contents and which
 * handles scrolling and mouse selection.
 *
 * <h3>Painting</h3>
 * Subclasses can hook into the paint loop at three levels:
 * <ul>
 * <li>Override <strong>{@link #paint(GC, ILineRange)}</strong> to control the entire painting of
 * the ruler.</li>
 * <li>Override <strong>{@link #paintLine(GC, int, int, int, int)}</strong> to control the
 * painting of a line.</li>
 * <li>Leave the painting to the default implementation, but override <strong>{@link #computeBackground(int)}</strong>,
 * <strong>{@link #computeForeground(int)}</strong> and <strong>{@link #computeText(int)}</strong>
 * to specify the ruler appearance for a line.</li>
 * </ul>
 *
 * <h3>Invalidation</h3>
 * Subclasses may call {@link #redraw()} to mark the entire ruler as needing to be redrawn.
 * Alternatively, use {@link #redraw(ILineRange)} to only invalidate a certain line range, for
 * example due to changes to the display model.
 *
 * <h3>Configuration</h3>
 * Subclasses can set the following properties. Setting them may trigger redrawing.
 * <ul>
 * <li>The {@link #setFont(Font) font} used to draw text in {@link #paintLine(GC, int, int, int, int)}.</li>
 * <li>The horizontal {@link #setTextInset(int) text inset} for text drawn.</li>
 * <li>The {@link #setDefaultBackground(Color) default background color} of the ruler.</li>
 * <li>The {@link #setWidth(int) width} of the ruler.</li>
 * </ul>
 *
 * @since 3.3
 */
public abstract class AbstractRulerColumn implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {
	private static final int DEFAULT_WIDTH= 12;
	private static final int DEFAULT_TEXT_INSET= 2;

	/**
	 * Handles all the mouse interaction in this line number ruler column.
	 */
	private final class MouseHandler implements MouseListener, MouseMoveListener {

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent event) {
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
		}

		/*
		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseMove(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
		}
	}

	/**
	 * Internal listener class that updates the ruler upon scrolling and text modifications.
	 */
	private final class InternalListener implements IViewportListener, ITextListener {

		/*
		 * @see IViewportListener#viewportChanged(int)
		 */
		public void viewportChanged(int topPixel) {
			int delta= topPixel - fLastTopPixel;
			if (scrollVertical(delta))
				fCanvas.update(); // force update the invalidated regions
		}

		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent event) {
			/*
			 * Redraw: - when the viewer is drawing, and any of the following - the widget was not
			 * full before the change - the widget is not full after the change - the document event
			 * was a visual modification (no document event attached) - for example when the
			 * projection changes.
			 */
			if (!event.getViewerRedrawState())
				return;

			if (fWasShowingEntireContents || event.getDocumentEvent() == null || JFaceTextUtil.isShowingEntireContents(fStyledText))
				redraw();
		}
	}

	/* Listeners */

	/** The viewport listener. */
	private final InternalListener fInternalListener= new InternalListener();
	/** The mouse handler. */
	private final MouseHandler fMouseHandler= new MouseHandler();

	/*
	 * Implementation and context of this ruler - created and set in createControl(), disposed of in
	 * columnRemoved().
	 */

	/** The parent ruler, possibly <code>null</code>. */
	private CompositeRuler fParentRuler;
	/** The canvas, the only widget used to draw this ruler, possibly <code>null</code>. */
	private Canvas fCanvas;
	/** The text viewer, possibly <code>null</code>. */
	private ITextViewer fTextViewer;
	/** The text viewer's widget, possibly <code>null</code>. */
	private StyledText fStyledText;

	/* State when the canvas was last painted. */

	/** The text widget's top pixel when the ruler was last painted. */
	private int fLastTopPixel= -1;
	/** Whether the text widget was showing the entire contents when the ruler was last painted. */
	private boolean fWasShowingEntireContents= false;

	/* Configuration */

	/** The width of this ruler. */
	private int fWidth= DEFAULT_WIDTH;
	/** The text inset. */
	private int fTextInset= DEFAULT_TEXT_INSET;
	/** The default background color, <code>null</code> to use the text viewer's background color. */
	private Color fBackground;
	/** The font, <code>null</code> to use the default font. */
	private Font fFont;
	/** The annotation model, possibly <code>null</code>. */
	private IAnnotationModel fModel;
	/** The annotation hover, possibly <code>null</code>. */
	private IAnnotationHover fHover;

	/**
	 * Creates a new ruler.
	 */
	protected AbstractRulerColumn() {
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#createControl(org.eclipse.jface.text.source.CompositeRuler,
	 *      org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		Assert.isLegal(parentControl != null);
		Assert.isLegal(parentRuler != null);
		Assert.isLegal(fParentRuler == null); // only call when not yet initialized!

		fParentRuler= parentRuler;

		fTextViewer= getParentRuler().getTextViewer();
		fTextViewer.addViewportListener(fInternalListener);
		fTextViewer.addTextListener(fInternalListener);

		fStyledText= fTextViewer.getTextWidget();

		fCanvas= new Canvas(parentControl, getCanvasStyle());

		fCanvas.setBackground(getDefaultBackground());
		fCanvas.setFont(getFont());

		fCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				AbstractRulerColumn.this.paintControl(event);
			}
		});

		fCanvas.addMouseListener(fMouseHandler);
		fCanvas.addMouseMoveListener(fMouseHandler);

		return fCanvas;
	}

	/**
	 * Returns the SWT style bits used when creating the ruler canvas.
	 * <p>
	 * The default implementation returns <code>SWT.NO_BACKGROUND</code>.</p>
	 * <p>
	 * Clients may reimplement this method to create a canvas with their
	 * desired style bits.</p>
	 *
	 * @return the SWT style bits, or <code>SWT.NONE</code> if none
	 */
	protected int getCanvasStyle() {
		return SWT.NO_BACKGROUND;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#getControl()
	 */
	public final Control getControl() {
		return fCanvas;
	}

	/**
	 * The new width in pixels. The <code>DEFAULT_WIDTH</code> constant
	 * specifies the default width.
	 *
	 * @param width the new width
	 */
	protected final void setWidth(int width) {
		Assert.isLegal(width >= 0);
		if (fWidth != width) {
			fWidth= width;
			CompositeRuler composite= getParentRuler();
			if (composite != null)
				composite.relayout();
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#getWidth()
	 */
	public final int getWidth() {
		return fWidth;
	}

	/**
	 * Returns the parent ruler, <code>null</code> before
	 * {@link #createControl(CompositeRuler, Composite)} has been called.
	 *
	 * @return the parent ruler or <code>null</code>
	 */
	protected final CompositeRuler getParentRuler() {
		return fParentRuler;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param font the font or <code>null</code> to use the default font
	 */
	public final void setFont(Font font) {
		if (fFont != font) {
			fFont= font;
			redraw();
		}
	}

	/**
	 * Returns the current font. If a font has not been explicitly set, the widget's font is
	 * returned.
	 *
	 * @return the font used to render text on the ruler.
	 */
	protected final Font getFont() {
		if (fFont != null)
			return fFont;
		if (fStyledText != null && !fStyledText.isDisposed())
			return fStyledText.getFont();
		return JFaceResources.getTextFont();
	}

	/**
	 * Sets the text inset (padding) used to draw text in {@link #paintLine(GC, int, int, int, int)}.
	 *
	 * @param textInset the new text inset
	 */
	protected final void setTextInset(int textInset) {
		if (textInset != fTextInset) {
			fTextInset= textInset;
			redraw();
		}
	}

	/**
	 * Returns the text inset for text drawn by {@link #paintLine(GC, int, int, int, int)}. The
	 * <code>DEFAULT_TEXT_INSET</code> constant specifies the default inset in pixels.
	 *
	 * @return the text inset for text
	 */
	protected final int getTextInset() {
		return fTextInset;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#setModel(org.eclipse.jface.text.source.IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
		if (fModel != model) {
			fModel= model;
			redraw();
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getModel()
	 */
	public final IAnnotationModel getModel() {
		return fModel;
	}

	/**
	 * Sets the default background color for this column. The default background is used as default
	 * implementation of {@link #computeBackground(int)} and also to paint the area of the ruler
	 * that does not correspond to any lines (when the viewport is not entirely filled with lines).
	 *
	 * @param background the default background color, <code>null</code> to use the text widget's
	 *        background
	 */
	protected final void setDefaultBackground(Color background) {
		if (fBackground != background) {
			fBackground= background;
			if (fCanvas != null && !fCanvas.isDisposed())
				fCanvas.setBackground(getDefaultBackground());
			redraw();
		}
	}

	/**
	 * Returns the background color. May return <code>null</code> if the system is shutting down.
	 *
	 * @return the background color
	 */
	protected final Color getDefaultBackground() {
		if (fBackground != null)
			return fBackground;
		if (fStyledText != null && !fStyledText.isDisposed())
			return fStyledText.getBackground();
		Display display;
		if (fCanvas != null && !fCanvas.isDisposed())
			display= fCanvas.getDisplay();
		else
			display= Display.getCurrent();
		if (display != null)
			return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		return null;
	}

	/**
	 * Sets the annotation hover.
	 *
	 * @param hover the annotation hover, <code>null</code> for no hover
	 */
	protected final void setHover(IAnnotationHover hover) {
		if (fHover != hover)
			fHover= hover;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getHover()
	 */
	public IAnnotationHover getHover() {
		return fHover;
	}

	/**
	 * Disposes this ruler column.
	 * <p>
	 * Subclasses may extend this method.</p>
	 * <p>
	 * Clients who created this column are responsible to call this method
	 * once the column is no longer used.</p>
	 */
	public void dispose() {
		if (fTextViewer != null) {
			fTextViewer.removeViewportListener(fInternalListener);
			fTextViewer.removeTextListener(fInternalListener);
			fTextViewer= null;
		}

		if (fStyledText != null)
			fStyledText= null;

		if (fCanvas != null) {
			fCanvas.dispose();
			fCanvas= null;
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#redraw()
	 */
	public final void redraw() {
		if (fCanvas != null && !fCanvas.isDisposed())
			fCanvas.redraw();
	}

	/**
	 * Marks the region covered by <code>lines</code> as needing to be redrawn.
	 *
	 * @param lines the lines to be redrawn in document coordinates
	 */
	protected final void redraw(ILineRange lines) {
		if (fCanvas == null || fCanvas.isDisposed())
			return;
		int firstModelLine= lines.getStartLine();
		int lastModelLine= firstModelLine + lines.getNumberOfLines();
		int firstWidgetLine= JFaceTextUtil.modelLineToWidgetLine(fTextViewer, firstModelLine);
		int lastWidgetLine= JFaceTextUtil.modelLineToWidgetLine(fTextViewer, lastModelLine);

		int from= Math.max(0, fStyledText.getLinePixel(firstWidgetLine));
		// getLinePixel will return the last pixel of the last line if line == lineCount
		int to= Math.min(fCanvas.getSize().y, fStyledText.getLinePixel(lastWidgetLine + 1));
		fCanvas.redraw(0, from, fWidth, to - from, false);
	}

	/**
	 * Paints the ruler column.
	 *
	 * @param event the paint event
	 */
	private void paintControl(PaintEvent event) {
		if (fTextViewer == null)
			return;
		fWasShowingEntireContents= JFaceTextUtil.isShowingEntireContents(fStyledText);
		fLastTopPixel= fStyledText.getTopPixel();

		ILineRange lines= computeDirtyWidgetLines(event);
		GC gc= event.gc;
		paint(gc, lines);

		if ((fCanvas.getStyle() & SWT.NO_BACKGROUND) != 0) {
			// fill empty area below any lines
			int firstEmpty= Math.max(event.y, fStyledText.getLinePixel(fStyledText.getLineCount()));
			int lastEmpty= event.y + event.height;
			if (lastEmpty > firstEmpty) {
				gc.setBackground(getDefaultBackground());
				gc.fillRectangle(0, firstEmpty, getWidth(), lastEmpty - firstEmpty);
			}
		}
	}

	/**
	 * Computes the widget lines that need repainting given the clipping region of a paint event.
	 *
	 * @param event the paint event
	 * @return the lines in widget coordinates that need repainting
	 */
	private ILineRange computeDirtyWidgetLines(PaintEvent event) {
		int firstLine= fStyledText.getLineIndex(event.y);
		int lastLine= fStyledText.getLineIndex(event.y + event.height - 1);
		return new LineRange(firstLine, lastLine - firstLine + 1);
	}

	/**
	 * Paints the ruler. Note that <code>lines</code> reference widget line indices, and that
	 * <code>lines</code> may not cover the entire viewport, but only the lines that need to be
	 * painted. The lines may not be entirely visible.
	 * <p>
	 * Subclasses may replace or extend. The default implementation calls
	 * {@link #paintLine(GC, int, int, int, int)} for every visible line.
	 * </p>
	 *
	 * @param gc the graphics context to paint on
	 * @param lines the lines to paint in widget coordinates
	 */
	protected void paint(GC gc, ILineRange lines) {
		final int firstLine= lines.getStartLine();
		final int lastLine= firstLine + lines.getNumberOfLines();
		for (int line= firstLine; line < lastLine; line++) {
			int modelLine= JFaceTextUtil.widgetLine2ModelLine(fTextViewer, line);
			if (modelLine == -1)
				continue;
			int linePixel= fStyledText.getLinePixel(line);
			int lineHeight= fStyledText.getLineHeight(fStyledText.getOffsetAtLine(line));
			paintLine(gc, modelLine, line, linePixel, lineHeight);
		}
	}

	/**
	 * Paints the ruler representation of a single line.
	 * <p>
	 * Subclasses may replace or extend. The default implementation draws the text obtained by
	 * {@link #computeText(int)} in the {@link #computeForeground(int) foreground color} and fills
	 * the entire width using the {@link #computeBackground(int) background color}. The text is
	 * drawn {@link #getTextInset()} pixels to the right of the left border.
	 * </p>
	 *
	 * @param gc the graphics context to paint on
	 * @param modelLine the model line (based on document coordinates)
	 * @param widgetLine the line in the text widget corresponding to <code>modelLine</code>
	 * @param linePixel the first y-pixel of the widget line
	 * @param lineHeight the line height in pixels
	 */
	protected void paintLine(GC gc, int modelLine, int widgetLine, int linePixel, int lineHeight) {
		gc.setBackground(computeBackground(modelLine));
		gc.fillRectangle(0, linePixel, getWidth(), lineHeight);
		String text= computeText(modelLine);
		if (text != null) {
			gc.setForeground(computeForeground(modelLine));
			gc.drawString(text, getTextInset(), linePixel, true);
		}
	}

	/**
	 * Returns the text to be drawn for a certain line by {@link #paintLine(GC, int, int, int, int)},
	 * <code>null</code> for no text. The default implementation returns <code>null</code>.
	 * <p>
	 * Subclasses may replace or extend.
	 * </p>
	 *
	 * @param line the document line number
	 * @return the text to be drawn for the given line, <code>null</code> for no text
	 */
	protected String computeText(int line) {
		return null;
	}

	/**
	 * Returns the background color drawn for a certain line by
	 * {@link #paintLine(GC, int, int, int, int)}. The default implementation returns
	 * {@link #getDefaultBackground()}.
	 * <p>
	 * Subclasses may replace or extend.
	 * </p>
	 *
	 * @param line the document line number
	 * @return the background color for drawn for the given line
	 */
	protected Color computeBackground(int line) {
		return getDefaultBackground();
	}

	/**
	 * Returns the foreground color drawn for a certain line by
	 * {@link #paintLine(GC, int, int, int, int)}. The default implementation returns a
	 * {@link SWT#COLOR_DARK_GRAY} color.
	 * <p>
	 * Subclasses may replace or extend.
	 * </p>
	 *
	 * @param line the document line number
	 * @return the foreground color for drawn for the given line
	 */
	protected Color computeForeground(int line) {
		return fStyledText.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	public final int getLineOfLastMouseButtonActivity() {
		return getParentRuler().getLineOfLastMouseButtonActivity();
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	public final int toDocumentLineNumber(int y_coordinate) {
		return getParentRuler().toDocumentLineNumber(y_coordinate);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#addVerticalRulerListener(org.eclipse.jface.text.source.IVerticalRulerListener)
	 */
	public void addVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#removeVerticalRulerListener(org.eclipse.jface.text.source.IVerticalRulerListener)
	 */
	public void removeVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Scrolls the canvas vertically (adapted from
	 * {@linkplain StyledText StyledText.scrollVertical()}).
	 *
	 * @param pixels the number of pixels to scroll (negative to scroll upwards)
	 * @return <code>true</code> if the widget was scrolled, <code>false</code> if the widget
	 *         was not scrolled
	 */
	private boolean scrollVertical(int pixels) {
		if (pixels == 0 || fCanvas == null || fCanvas.isDisposed())
			return false;

		final int width= getWidth();
		final int clientAreaHeight= fStyledText.getClientArea().height;
		final int topMargin= 0;
		final int leftMargin= 0;
		final int bottomMargin= 0;

		if (pixels > 0) {
			// downwards scrolling - content moves upwards
			int sourceY= topMargin + pixels;
			int scrollHeight= clientAreaHeight - sourceY - bottomMargin;
			if (scrollHeight > 0)
				// scroll recycled area
				fCanvas.scroll(leftMargin, topMargin, leftMargin, sourceY, width, scrollHeight, true);
			if (sourceY > scrollHeight) {
				// redraw in-between area
				int redrawY= Math.max(0, topMargin + scrollHeight);
				int redrawHeight= Math.min(clientAreaHeight, pixels - scrollHeight);
				fCanvas.redraw(leftMargin, redrawY, width, redrawHeight, true);
			}
		} else {
			// upwards scrolling - content moves downwards
			int destinationY= topMargin - pixels;
			int scrollHeight= clientAreaHeight - destinationY - bottomMargin;
			if (scrollHeight > 0)
				// scroll recycled area
				fCanvas.scroll(leftMargin, destinationY, leftMargin, topMargin, width, scrollHeight, true);
			if (destinationY > scrollHeight) {
				// redraw in-between area
				int redrawY= Math.max(0, topMargin + scrollHeight);
				int redrawHeight= Math.min(clientAreaHeight, -pixels - scrollHeight);
				fCanvas.redraw(leftMargin, redrawY, width, redrawHeight, true);
			}
		}
		return true;
	}
}
