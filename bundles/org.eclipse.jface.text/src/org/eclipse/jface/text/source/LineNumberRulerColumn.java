/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nikolay Botev <bono8106@hotmail.com> - [rulers] Shift clicking in line number column doesn't select range - https://bugs.eclipse.org/bugs/show_bug.cgi?id=32166
 *     Nikolay Botev <bono8106@hotmail.com> - [rulers] Clicking in line number ruler should not trigger annotation ruler - https://bugs.eclipse.org/bugs/show_bug.cgi?id=40889
 *     Florian Weßling <flo@cdhq.de> - [rulers] Line numbering was wrong when word wrap was active - https://bugs.eclipse.org/bugs/show_bug.cgi?id=35779
 *     Rüdiger Herrmann - Insufficient is-disposed check in LineNumberRulerColumn::redraw - https://bugs.eclipse.org/bugs/show_bug.cgi?id=506427
 *     Angelo ZERR <angelo.zerr@gmail.com> - Compute line height by taking care of line spacing - https://bugs.eclipse.org/bugs/show_bug.cgi?id=481968
 *******************************************************************************/
package org.eclipse.jface.text.source;

import java.util.Arrays;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageGcDrawer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.TextEvent;


/**
 * A vertical ruler column displaying line numbers.
 * Clients usually instantiate and configure object of this class.
 *
 * @since 2.0
 */
public class LineNumberRulerColumn implements IVerticalRulerColumn {

	/**
	 * Internal listener class.
	 */
	class InternalListener implements ITextListener {

		@Override
		public void textChanged(TextEvent event) {

			boolean fCachedRedrawState= event.getViewerRedrawState();
			if (!fCachedRedrawState)
				return;

			if (updateNumberOfDigits()) {
				computeIndentations();
				layout(event.getViewerRedrawState());
				return;
			}
		}
	}

	/**
	 * Handles all the mouse interaction in this line number ruler column.
	 */
	class MouseHandler implements MouseListener, MouseMoveListener, MouseWheelListener {

		/** The cached view port size. */
		private int fCachedViewportSize;
		/** The area of the line at which line selection started. */
		private int fStartLineOffset;
		/** The number of the line at which line selection started. */
		private int fStartLineNumber;
		/** The auto scroll direction. */
		private int fAutoScrollDirection;
		/* @since 3.2 */
		private boolean fIsListeningForMove= false;

		@Override
		public void mouseUp(MouseEvent event) {
			// see bug 45700
			if (event.button == 1) {
				stopSelecting();
				stopAutoScroll();
			}
		}

		@Override
		public void mouseDown(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			// see bug 45700
			if (event.button == 1) {
				startSelecting((event.stateMask & SWT.SHIFT) != 0);
			}
		}

		@Override
		public void mouseDoubleClick(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			stopSelecting();
			stopAutoScroll();
		}

		@Override
		public void mouseMove(MouseEvent event) {
			if (fIsListeningForMove && !autoScroll(event)) {
				int newLine= fParentRuler.toDocumentLineNumber(event.y);
				expandSelection(newLine);
			}
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
		}

		/**
		 * Called when line drag selection started. Adds mouse move and track
		 * listeners to this column's control.
		 *
		 * @param expandExistingSelection if <code>true</code> the existing selection will be expanded,
		 * 			otherwise a new selection is started
		 */
		private void startSelecting(boolean expandExistingSelection) {
			try {

				// select line
				IDocument document= fCachedTextViewer.getDocument();
				int lineNumber= fParentRuler.getLineOfLastMouseButtonActivity();
				final StyledText textWidget= fCachedTextViewer.getTextWidget();
				if (textWidget != null && !textWidget.isFocusControl())
					textWidget.setFocus();
				if (expandExistingSelection && fCachedTextViewer instanceof ITextViewerExtension5 && textWidget != null) {
					ITextViewerExtension5 extension5= ((ITextViewerExtension5)fCachedTextViewer);
					// Find model cursor position
					int widgetCaret= textWidget.getCaretOffset();
					int modelCaret= extension5.widgetOffset2ModelOffset(widgetCaret);
					// Find model selection range
					Point selection= fCachedTextViewer.getSelectedRange();
					// Start from tail of selection range (opposite of cursor position)
					int startOffset= modelCaret == selection.x ? selection.x + selection.y : selection.x;

					fStartLineNumber= document.getLineOfOffset(startOffset);
					fStartLineOffset= startOffset;

					expandSelection(lineNumber);
				} else {
					fStartLineNumber= lineNumber;
					fStartLineOffset= document.getLineInformation(fStartLineNumber).getOffset();
					Point currentSelection= fCachedTextViewer.getSelectedRange();
					// avoid sending unnecessary selection event, see https://bugs.eclipse.org/483747
					if (currentSelection.x != fStartLineOffset || currentSelection.y != 0) {
						fCachedTextViewer.setSelectedRange(fStartLineOffset, 0);
					}
				}
				fCachedViewportSize= getVisibleLinesInViewport();

				// prepare for drag selection
				fIsListeningForMove= true;

			} catch (BadLocationException x) {
			}
		}

		/**
		 * Called when line drag selection stopped. Removes all previously
		 * installed listeners from this column's control.
		 */
		private void stopSelecting() {
			// drag selection stopped
			fIsListeningForMove= false;
		}

		/**
		 * Expands the line selection from the remembered start line to the
		 * given line.
		 *
		 * @param lineNumber the line to which to expand the selection
		 */
		private void expandSelection(int lineNumber) {
			try {

				IDocument document= fCachedTextViewer.getDocument();
				IRegion lineInfo= document.getLineInformation(lineNumber);

				Display display= fCachedTextWidget.getDisplay();
				Point absolutePosition= display.getCursorLocation();
				Point relativePosition= fCachedTextWidget.toControl(absolutePosition);

				int offset;

				if (relativePosition.x < 0)
					offset= lineInfo.getOffset();
				else {
					int widgetOffset= fCachedTextWidget.getOffsetAtPoint(relativePosition);
					if (widgetOffset != -1) {
						Point p= fCachedTextWidget.getLocationAtOffset(widgetOffset);
						if (p.x > relativePosition.x) {
							widgetOffset--;
						}

						// Convert to model offset
						if (fCachedTextViewer instanceof ITextViewerExtension5) {
							ITextViewerExtension5 extension= (ITextViewerExtension5)fCachedTextViewer;
							offset= extension.widgetOffset2ModelOffset(widgetOffset);
						} else {
							offset= widgetOffset + fCachedTextViewer.getVisibleRegion().getOffset();
						}
					} else {
						int lineEndOffset= lineInfo.getOffset() + lineInfo.getLength();

						// Convert to widget offset
						int lineEndWidgetOffset;
						if (fCachedTextViewer instanceof ITextViewerExtension5) {
							ITextViewerExtension5 extension= (ITextViewerExtension5)fCachedTextViewer;
							lineEndWidgetOffset= extension.modelOffset2WidgetOffset(lineEndOffset);
						} else
							lineEndWidgetOffset= lineEndOffset - fCachedTextViewer.getVisibleRegion().getOffset();

						Point p= fCachedTextWidget.getLocationAtOffset(lineEndWidgetOffset);
						if (p.x < relativePosition.x)
							offset= lineEndOffset;
						else
							offset= lineInfo.getOffset();
					}
				}

				int start= Math.min(fStartLineOffset, offset);
				int end= Math.max(fStartLineOffset, offset);

				if (lineNumber < fStartLineNumber)
					fCachedTextViewer.setSelectedRange(end, start - end);
				else
					fCachedTextViewer.setSelectedRange(start, end - start);

			} catch (BadLocationException x) {
			}
		}

		/**
		 * Called when auto scrolling stopped. Clears the auto scroll direction.
		 */
		private void stopAutoScroll() {
			fAutoScrollDirection= SWT.NULL;
		}

		/**
		 * Called on drag selection.
		 *
		 * @param event the mouse event caught by the mouse move listener
		 * @return <code>true</code> if scrolling happened, <code>false</code> otherwise
		 */
		private boolean autoScroll(MouseEvent event) {
			Rectangle area= fCanvas.getClientArea();

			if (event.y > area.height) {
				autoScroll(SWT.DOWN);
				return true;
			}

			if (event.y < 0) {
				autoScroll(SWT.UP);
				return true;
			}

			stopAutoScroll();
			return false;
		}

		/**
		 * Scrolls the viewer into the given direction.
		 *
		 * @param direction the scroll direction
		 */
		private void autoScroll(int direction) {

			if (fAutoScrollDirection == direction)
				return;

			final int TIMER_INTERVAL= 5;
			final Display display= fCanvas.getDisplay();
			Runnable timer= null;
			switch (direction) {
				case SWT.UP:
					timer= new Runnable() {
						@Override
						public void run() {
							if (fAutoScrollDirection == SWT.UP) {
								int top= getInclusiveTopIndex();
								if (top > 0) {
									fCachedTextViewer.setTopIndex(top -1);
									expandSelection(top -1);
									display.timerExec(TIMER_INTERVAL, this);
								}
							}
						}
					};
					break;
				case  SWT.DOWN:
					timer= new Runnable() {
						@Override
						public void run() {
							if (fAutoScrollDirection == SWT.DOWN) {
								int top= getInclusiveTopIndex();
								fCachedTextViewer.setTopIndex(top +1);
								expandSelection(top +1 + fCachedViewportSize);
								display.timerExec(TIMER_INTERVAL, this);
							}
						}
					};
					break;
			}

			if (timer != null) {
				fAutoScrollDirection= direction;
				display.timerExec(TIMER_INTERVAL, timer);
			}
		}

		/**
		 * Returns the viewer's first visible line, even if only partially visible.
		 *
		 * @return the viewer's first visible line
		 */
		private int getInclusiveTopIndex() {
			if (fCachedTextWidget != null && !fCachedTextWidget.isDisposed()) {
				return JFaceTextUtil.getPartialTopIndex(fCachedTextViewer);
			}
			return -1;
		}

		@Override
		public void mouseScrolled(MouseEvent e) {
			handleMouseScrolled(e);
		}
	}

	/** This column's parent ruler */
	private CompositeRuler fParentRuler;
	/** Cached text viewer */
	private ITextViewer fCachedTextViewer;
	/** Cached text widget */
	private StyledText fCachedTextWidget;
	/** The columns canvas */
	private Canvas fCanvas;
	/** The drawable for double buffering */
	private Image fBuffer;
	/** The internal listener */
	private ITextListener fInternalListener= new InternalListener();
	/** The font of this column */
	private Font fFont;
	/** The indentation cache */
	private int[] fIndentation;
	/** The foreground color */
	private Color fForeground;
	/** The background color */
	private Color fBackground;
	/** Cached number of displayed digits */
	private int fCachedNumberOfDigits= -1;
	/** Flag indicating whether a relayout is required */
	private boolean fRelayoutRequired= false;
	/** Last top pixel. */
	private int fLastTopPixel = -1;
	/** Last top model line. */
	private int fLastTopModelLine;
	/** Last number of lines visible. */
	private int fLastNumberOfLines;
	/** Last bottom model line. */
	private int fLastBottomModelLine;
	/** Last canvas height used. */
	private int fLastHeight= -1;
	/**
	 * Redraw runnable lock
	 * @since 3.0
	 */
	private Object fRunnableLock= new Object();
	/**
	 * Redraw runnable state
	 * @since 3.0
	 */
	private boolean fIsRunnablePosted= false;
	/**
	 * Redraw runnable
	 * @since 3.0
	 */
	private Runnable fRunnable= () -> {
		synchronized (fRunnableLock) {
			fIsRunnablePosted= false;
		}
		redraw();
	};
	/* @since 3.2 */
	private MouseHandler fMouseHandler;

	/**
	 * Redraw the ruler handler called when a line height change.
	 *
	 * @since 3.13
	 */
	private Consumer<StyledText> lineHeightChangeHandler= t -> postRedraw();

	/**
	 * Constructs a new vertical ruler column.
	 */
	public LineNumberRulerColumn() {
	}

	/**
	 * Sets the foreground color of this column.
	 *
	 * @param foreground the foreground color
	 */
	public void setForeground(Color foreground) {
		fForeground= foreground;
	}

	/**
	 * Returns the foreground color being used to print the line numbers.
	 *
	 * @return the configured foreground color
	 * @since 3.0
	 */
	protected Color getForeground() {
		return fForeground;
	}

	/**
	 * Sets the background color of this column.
	 *
	 * @param background the background color
	 */
	public void setBackground(Color background) {
		fBackground= background;
		if (fCanvas != null && !fCanvas.isDisposed())
			fCanvas.setBackground(getBackground(fCanvas.getDisplay()));
	}

	/**
	 * Returns the System background color for list widgets.
	 *
	 * @param display the display
	 * @return the System background color for list widgets
	 */
	protected Color getBackground(Display display) {
		if (fBackground == null)
			return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		return fBackground;
	}

	@Override
	public Control getControl() {
		return fCanvas;
	}

	/*
	 * @see IVerticalRuleColumnr#getWidth
	 */
	@Override
	public int getWidth() {
		return fIndentation[0];
	}

	/**
	 * Computes the number of digits to be displayed. Returns
	 * <code>true</code> if the number of digits changed compared
	 * to the previous call of this method. If the method is called
	 * for the first time, the return value is also <code>true</code>.
	 *
	 * @return whether the number of digits has been changed
	 * @since 3.0
	 */
	protected boolean updateNumberOfDigits() {
		if (fCachedTextViewer == null)
			return false;

		int digits= computeNumberOfDigits();

		if (fCachedNumberOfDigits != digits) {
			fCachedNumberOfDigits= digits;
			return true;
		}

		return false;
	}

	/**
	 * Does the real computation of the number of digits. Subclasses may override this method if
	 * they need extra space on the line number ruler.
	 *
	 * @return the number of digits to be displayed on the line number ruler.
	 */
	protected int computeNumberOfDigits() {
		IDocument document= fCachedTextViewer.getDocument();
		int lines= document == null ? 0 : document.getNumberOfLines();

		int digits= 2;
		while (lines > Math.pow(10, digits) -1) {
			++digits;
		}
		return digits;
	}

	/**
	 * Layouts the enclosing viewer to adapt the layout to changes of the
	 * size of the individual components.
	 *
	 * @param redraw <code>true</code> if this column can be redrawn
	 */
	protected void layout(boolean redraw) {
		if (!redraw) {
			fRelayoutRequired= true;
			return;
		}

		fRelayoutRequired= false;
		if (fCachedTextViewer instanceof ITextViewerExtension) {
			ITextViewerExtension extension= (ITextViewerExtension) fCachedTextViewer;
			Control control= extension.getControl();
			if (control instanceof Composite && !control.isDisposed()) {
				Composite composite= (Composite) control;
				composite.layout(true);
			}
		}
	}

	/**
	 * Computes the indentations for the given font and stores them in
	 * <code>fIndentation</code>.
	 */
	protected void computeIndentations() {
		if (fCanvas == null || fCanvas.isDisposed())
			return;

		GC gc= new GC(fCanvas);
		try {

			gc.setFont(fCanvas.getFont());

			fIndentation= new int[fCachedNumberOfDigits + 1];

			char[] nines= new char[fCachedNumberOfDigits];
			Arrays.fill(nines, '9');
			String nineString= new String(nines);
			Point p= gc.stringExtent(nineString);
			fIndentation[0]= p.x;

			for (int i= 1; i <= fCachedNumberOfDigits; i++) {
				p= gc.stringExtent(nineString.substring(0, i));
				fIndentation[i]= fIndentation[0] - p.x;
			}

		} finally {
			gc.dispose();
		}
	}

	@Override
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {

		fParentRuler= parentRuler;
		fCachedTextViewer= parentRuler.getTextViewer();
		fCachedTextWidget= fCachedTextViewer.getTextWidget();

		// on word wrap toggle a "resized" ControlEvent is fired: suggest a redraw of the line ruler
		fCachedTextWidget.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (fCachedTextWidget != null && fCachedTextWidget.getWordWrap()) {
					postRedraw();
				}
			}
		});

		// track when StyledText is redrawn to check if some line height changed. In this case, the ruler must be redrawn
		// to draw line number with well height.
		VisibleLinesTracker.track(fCachedTextViewer, lineHeightChangeHandler);

		fCanvas= new Canvas(parentControl, SWT.NO_FOCUS ) {
			@Override
			public void addMouseListener(MouseListener listener) {
				// see bug 40889, bug 230073 and AnnotationRulerColumn#isPropagatingMouseListener()
				if (listener == fMouseHandler)
					super.addMouseListener(listener);
				else {
					addTypedListener(listener, SWT.MouseDoubleClick);
				}
			}
		};
		fCanvas.setBackground(getBackground(fCanvas.getDisplay()));
		fCanvas.setForeground(fForeground);

		fCanvas.addPaintListener(event -> {
			if (fCachedTextViewer != null)
				doubleBufferPaint(event.gc);
		});

		fCanvas.addDisposeListener(e -> {
			handleDispose();
			fCachedTextViewer= null;
			fCachedTextWidget= null;
		});

		fCanvas.addListener(SWT.ZoomChanged, e -> {
			Display.getDefault().asyncExec(() -> {
				computeIndentations();
				if (fBuffer != null) {
					fBuffer.dispose();
					fBuffer= null;
				}
				layout(false);
			});
		});

		fMouseHandler= new MouseHandler();
		fCanvas.addMouseListener(fMouseHandler);
		fCanvas.addMouseMoveListener(fMouseHandler);
		fCanvas.addMouseWheelListener(fMouseHandler);

		fCachedTextViewer.addTextListener(fInternalListener);

		if (fFont == null) {
			if (!fCachedTextWidget.isDisposed())
				fFont= fCachedTextWidget.getFont();
		}

		if (fFont != null)
			fCanvas.setFont(fFont);

		updateNumberOfDigits();
		computeIndentations();
		return fCanvas;
	}

	/**
	 * Disposes the column's resources.
	 */
	protected void handleDispose() {

		if (fCachedTextViewer != null) {
			VisibleLinesTracker.untrack(fCachedTextViewer, lineHeightChangeHandler);
		}

		if (fBuffer != null) {
			fBuffer.dispose();
			fBuffer= null;
		}
	}

	/**
	 * Double buffer drawing.
	 *
	 * @param dest the GC to draw into
	 */
	private void doubleBufferPaint(GC dest) {

		Point size= fCanvas.getSize();

		if (size.x <= 0 || size.y <= 0) {
			return;
		}

		if (fBuffer != null) {
			Rectangle r= fBuffer.getBounds();
			if (r.width != size.x || r.height != size.y) {
				fBuffer.dispose();
				fBuffer= null;
			}
		}

		ILineRange visibleLines= JFaceTextUtil.getVisibleModelLines(fCachedTextViewer);
		if (visibleLines == null) {
			return;
		}

		if (fBuffer == null) {
			newFullBufferImage(visibleLines, size);
		} else {
			doPaint(visibleLines, size);
		}
		dest.drawImage(fBuffer, 0, 0);
	}

	private void newFullBufferImage(ILineRange visibleLines, Point size) {
		ImageGcDrawer imageGcDrawer= (gc, imageWidth, imageHeight) -> {
			// We redraw everything; paint directly into the buffer
			initializeGC(gc, 0, 0, imageWidth, imageHeight);
			doPaint(gc, visibleLines);
		};
		fBuffer= new Image(fCanvas.getDisplay(), imageGcDrawer, size.x, size.y);
	}

	private void doPaint(ILineRange visibleLines, Point size) {
		GC bufferGC= new GC(fBuffer);
		Image newBuffer= null;
		try {
			int topPixel= fCachedTextWidget.getTopPixel();
			int bufferY= 0;
			int bufferH= size.y;
			int numberOfLines= visibleLines.getNumberOfLines();
			int dy= topPixel - fLastTopPixel;
			int topModelLine= visibleLines.getStartLine();
			int bottomModelLine= topModelLine + numberOfLines - 1;
			int bottomWidgetLine= JFaceTextUtil.modelLineToWidgetLine(fCachedTextViewer, bottomModelLine);
			boolean atEnd= bottomWidgetLine + 1 >= fCachedTextWidget.getLineCount();
			int height= size.y;
			if (dy != 0 && !atEnd && fLastTopPixel >= 0 && numberOfLines > 1 && numberOfLines == fLastNumberOfLines) {
				int bottomPixel= fCachedTextWidget.getLinePixel(bottomWidgetLine + 1);
				if (dy > 0 && bottomPixel < size.y) {
					// Can occur on GTK with static scrollbars; see bug 551320.
					height= bottomPixel;
				}
				if (dy > 0 && dy < height) {
					int goodPixels= height;
					if (goodPixels > fLastHeight) {
						// Dont copy empty pixels from last time.
						goodPixels= fLastHeight;
					}
					if (dy < goodPixels) {
						bufferGC.copyArea(0, dy, size.x, goodPixels - dy, 0, 0);
						bufferY= goodPixels - dy;
						bufferH= height - bufferY;
					} else {
						// Redraw everything.
						height= size.y;
						dy= 0;
					}
				} else if (dy < 0 && -dy < height) {
					bufferGC.copyArea(0, 0, size.x, height + dy, 0, -dy);
					bufferY= 0;
					bufferH= -dy;
				} else {
					height= size.y;
					dy= 0;
				}
			} else {
				dy= 0;
			}
			// dy == 0 means now "draw everything", either because there was no overlap, or we indeed didn't move
			// (refresh or other cases), or there was a resize, or it's the first time, or we are showing the very
			// last line.
			if (dy != 0) {
				// Reduce the line range.
				if (dy > 0) {
					visibleLines= new LineRange(fLastBottomModelLine, bottomModelLine - fLastBottomModelLine + 1);
				} else {
					visibleLines= new LineRange(topModelLine, fLastTopModelLine - topModelLine + 1);
				}
			}
			fLastTopPixel= topPixel;
			fLastTopModelLine= topModelLine;
			fLastNumberOfLines= numberOfLines;
			fLastBottomModelLine= bottomModelLine;
			fLastHeight= height;
			if (dy != 0) {
				newBuffer= newBufferImage(size, bufferY, bufferH, visibleLines);
				bufferGC.drawImage(newBuffer, 0, bufferY, size.x, bufferH, 0, bufferY, size.x, bufferH);
				if (dy > 0 && bufferY + bufferH < size.y) {
					// Scrolled down in the text, but didn't use the full height of the Canvas: clear
					// the rest. Occurs on GTK with static scrollbars; the area cleared here is the
					// bit next to the horizontal scrollbar. See bug 551320.
					bufferGC.setBackground(getBackground(fCanvas.getDisplay()));
					bufferGC.fillRectangle(0, bufferY + bufferH, size.x, size.y - bufferY - bufferH + 1);
				}
			} else {
				// We redraw everything; paint directly into the buffer
				initializeGC(bufferGC, 0, 0, size.x, size.y);
				doPaint(bufferGC, visibleLines);
			}
		} finally {
			bufferGC.dispose();
			if (newBuffer != null) {
				newBuffer.dispose();
			}
		}
	}

	private Image newBufferImage(Point size, int bufferY, int bufferH, final ILineRange visibleLines) {
		ImageGcDrawer imageGcDrawer= (localGC, imageWidth, imageHeight) -> {
			// Some rulers may paint outside the line region. Let them paint in a new image,
			// the copy the wanted bits.
			initializeGC(localGC, 0, bufferY, imageWidth, bufferH);
			doPaint(localGC, visibleLines);
		};
		return new Image(fCanvas.getDisplay(), imageGcDrawer, size.x, size.y);
	}

	private void initializeGC(GC gc, int x, int y, int width, int height) {
		gc.setFont(fCanvas.getFont());
		if (fForeground != null) {
			gc.setForeground(fForeground);
		}
		gc.setBackground(getBackground(fCanvas.getDisplay()));
		gc.fillRectangle(x, y, width, height);
	}

	/**
	 * Returns the view port height in lines.
	 *
	 * @return the view port height in lines
	 * @deprecated as of 3.2 the number of lines in the viewport cannot be computed because
	 *             StyledText supports variable line heights
	 */
	@Deprecated
	protected int getVisibleLinesInViewport() {
		return getVisibleLinesInViewport(fCachedTextWidget);
	}


	/**
	 * Returns <code>true</code> if the viewport displays the entire viewer contents, i.e. the
	 * viewer is not vertically scrollable.
	 *
	 * @return <code>true</code> if the viewport displays the entire contents, <code>false</code> otherwise
	 * @since 3.2
	 */
	protected final boolean isViewerCompletelyShown() {
		return JFaceTextUtil.isShowingEntireContents(fCachedTextWidget);
	}

	/**
	 * Draws the ruler column.
	 *
	 * @param gc the GC to draw into
	 * @param visibleLines the visible model lines
	 * @since 3.2
	 */
	void doPaint(GC gc, ILineRange visibleLines) {
		Display display= fCachedTextWidget.getDisplay();

		int firstWidgetLineToDraw= JFaceTextUtil.modelLineToWidgetLine(fCachedTextViewer, visibleLines.getStartLine());
		int y= fCachedTextWidget.getLinePixel(firstWidgetLineToDraw);

		// add empty lines if line is wrapped
		boolean isWrapActive= fCachedTextWidget.getWordWrap();

		int lastLine= end(visibleLines);
		for (int line= visibleLines.getStartLine(); line < lastLine; line++) {
			int widgetLine= JFaceTextUtil.modelLineToWidgetLine(fCachedTextViewer, line);
			if (widgetLine == -1)
				continue;

			final int offsetAtLine= fCachedTextWidget.getOffsetAtLine(widgetLine);
			int lineHeight = JFaceTextUtil.computeLineHeight(fCachedTextWidget, widgetLine, widgetLine + 1, 1);
			paintLine(line, y, lineHeight, gc, display);

			// increment y position
			if (!isWrapActive) {
				y+= lineHeight;
			} else {
				int charCount= fCachedTextWidget.getCharCount();
				if (offsetAtLine == charCount)
					continue;

				// end of wrapped line
				final int offsetEnd= offsetAtLine + fCachedTextWidget.getLine(widgetLine).length();

				if (offsetEnd == charCount)
					continue;

				// use height of text bounding because bounds.width changes on word wrap
				y+= fCachedTextWidget.getTextBounds(offsetAtLine, offsetEnd).height;
				y+= fCachedTextWidget.getLineSpacing();
			}
		}
	}

	/* @since 3.2 */
	private static int end(ILineRange range) {
		return range.getStartLine() + range.getNumberOfLines();
	}

	/**
	 * Computes the string to be printed for <code>line</code>. The default implementation returns
	 * <code>Integer.toString(line + 1)</code>.
	 *
	 * @param line the line number for which the line number string is generated
	 * @return the string to be printed on the line number bar for <code>line</code>
	 * @since 3.0
	 */
	protected String createDisplayString(int line) {
		return Integer.toString(line + 1);
	}

	/**
	 * Returns the difference between the baseline of the widget and the
	 * baseline as specified by the font for <code>gc</code>. When drawing
	 * line numbers, the returned bias should be added to obtain text lined up
	 * on the correct base line of the text widget.
	 *
	 * @param gc the <code>GC</code> to get the font metrics from
	 * @param widgetLine the widget line
	 * @return the baseline bias to use when drawing text that is lined up with
	 *         <code>fCachedTextWidget</code>
	 * @since 3.2
	 */
	private int getBaselineBias(GC gc, int widgetLine) {
		/*
		 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=62951
		 * widget line height may be more than the font height used for the
		 * line numbers, since font styles (bold, italics...) can have larger
		 * font metrics than the simple font used for the numbers.
		 */
		int offset= fCachedTextWidget.getOffsetAtLine(widgetLine);
		int widgetBaseline= fCachedTextWidget.getBaseline(offset);

		FontMetrics fm= gc.getFontMetrics();
		int fontBaseline= fm.getAscent() + fm.getLeading();
		int baselineBias= widgetBaseline - fontBaseline;
		return Math.max(0, baselineBias);
	}

	/**
	 * Paints the line. After this method is called the line numbers are painted on top
	 * of the result of this method.
	 *
	 * @param line the line of the document which the ruler is painted for
	 * @param y the y-coordinate of the box being painted for <code>line</code>, relative to <code>gc</code>
	 * @param lineheight the height of one line (and therefore of the box being painted)
	 * @param gc the drawing context the client may choose to draw on.
	 * @param display the display the drawing occurs on
	 * @since 3.0
	 */
	protected void paintLine(int line, int y, int lineheight, GC gc, Display display) {
		int widgetLine= JFaceTextUtil.modelLineToWidgetLine(fCachedTextViewer, line);

		String s= createDisplayString(line);
		int index= s.length();
		if (index >= fIndentation.length) {
			// Bug 325434: our data is not in-sync with the document, don't try to paint
			return;
		}
		int indentation= fIndentation[index];
		int baselineBias= getBaselineBias(gc, widgetLine);
		int verticalIndent= fCachedTextViewer.getTextWidget().getLineVerticalIndent(widgetLine);
		gc.drawString(s, indentation, y + baselineBias + verticalIndent, true);
	}

	/**
	 * Triggers a redraw in the display thread.
	 *
	 * @since 3.0
	 */
	protected final void postRedraw() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			Display d= fCanvas.getDisplay();
			if (d != null) {
				synchronized (fRunnableLock) {
					if (fIsRunnablePosted)
						return;
					fIsRunnablePosted= true;
				}
				d.asyncExec(fRunnable);
			}
		}
	}

	@Override
	public void redraw() {

		if (fRelayoutRequired) {
			layout(true);
			return;
		}

		if (!isDisposed()) {
			if (VerticalRuler.AVOID_NEW_GC) {
				fCanvas.redraw();
			} else {
				GC gc= new GC(fCanvas);
				doubleBufferPaint(gc);
				gc.dispose();
			}
		}
	}

	private boolean isDisposed() {
		return fCachedTextViewer == null || fCanvas == null || fCanvas.isDisposed()
				|| fCachedTextViewer.getTextWidget() == null;
	}

	@Override
	public void setModel(IAnnotationModel model) {
	}

	@Override
	public void setFont(Font font) {
		fFont= font;
		if (fCanvas != null && !fCanvas.isDisposed()) {
			fCanvas.setFont(fFont);
			updateNumberOfDigits();
			computeIndentations();
		}
	}

	/**
	 * Returns the parent (composite) ruler of this ruler column.
	 *
	 * @return the parent ruler
	 * @since 3.0
	 */
	protected CompositeRuler getParentRuler() {
		return fParentRuler;
	}


	/**
	 * Handles mouse scrolled events on the ruler by forwarding them to the text widget.
	 *
	 * @param e the mouse event
	 * @since 3.10
	 */
	void handleMouseScrolled(MouseEvent e) {
		if (fCachedTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fCachedTextViewer;
			StyledText textWidget= fCachedTextViewer.getTextWidget();
			int topIndex= textWidget.getTopIndex();
			int newTopIndex= Math.max(0, topIndex - e.count);
			fCachedTextViewer.setTopIndex(extension.widgetLine2ModelLine(newTopIndex));
		} else {
			int topIndex= fCachedTextViewer.getTopIndex();
			int newTopIndex= Math.max(0, topIndex - e.count);
			fCachedTextViewer.setTopIndex(newTopIndex);
		}
	}

	/**
	 * Returns the number of lines in the view port.
	 *
	 * @param textWidget the styled text widget
	 * @return the number of lines visible in the view port <code>-1</code> if there's no client
	 *         area
	 * @deprecated this method should not be used - it relies on the widget using a uniform line
	 *             height
	 */
	@Deprecated
	static int getVisibleLinesInViewport(StyledText textWidget) {
		if (textWidget != null) {
			Rectangle clArea= textWidget.getClientArea();
			if (!clArea.isEmpty()) {
				int firstPixel= 0;
				int lastPixel= clArea.height - 1; // XXX: what about margins? don't take trims as they include scrollbars
				int first= JFaceTextUtil.getLineIndex(textWidget, firstPixel);
				int last= JFaceTextUtil.getLineIndex(textWidget, lastPixel);
				return last - first;
			}
		}
		return -1;
	}

}
