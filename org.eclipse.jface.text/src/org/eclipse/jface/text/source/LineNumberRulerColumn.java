/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextEvent;

import org.eclipse.jface.internal.text.JFaceTextUtil;

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
	class InternalListener implements IViewportListener, ITextListener {

		/**
		 * @since 3.1
		 */
		private boolean fCachedRedrawState= true;

		/*
		 * @see IViewportListener#viewportChanged(int)
		 */
		public void viewportChanged(int verticalPosition) {
			if (fCachedRedrawState && verticalPosition != fScrollPos)
				redraw();
		}

		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent event) {

			fCachedRedrawState= event.getViewerRedrawState();
			if (!fCachedRedrawState)
				return;

			if (updateNumberOfDigits()) {
				computeIndentations();
				layout(event.getViewerRedrawState());
				return;
			}

			if (fSensitiveToTextChanges || event.getDocumentEvent() == null)
				postRedraw();
		}
	}

	/**
	 * Handles all the mouse interaction in this line number ruler column.
	 */
	private class MouseHandler implements MouseListener, MouseMoveListener {

		/** The cached view port size */
		private int fCachedViewportSize;
		/** The area of the line at which line selection started */
		private IRegion fStartLine;
		/** The number of the line at which line selection started */
		private int fStartLineNumber;
		/** The auto scroll direction */
		private int fAutoScrollDirection;
		/** Whether we are listening for moves. */
		private boolean fIsListeningForMove= false;

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent event) {
			// see bug 45700
			if (event.button == 1) {
				stopSelecting();
				stopAutoScroll();
			}
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			// see bug 45700
			if (event.button == 1) {
				startSelecting();
			}
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			stopSelecting();
			stopAutoScroll();
		}

		/*
		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
		 */
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
		 */
		private void startSelecting() {
			try {

				// select line
				IDocument document= fCachedTextViewer.getDocument();
				fStartLineNumber= fParentRuler.getLineOfLastMouseButtonActivity();
				fStartLine= document.getLineInformation(fStartLineNumber);
				fCachedTextViewer.setSelectedRange(fStartLine.getOffset(), fStartLine.getLength());
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

				int start= Math.min(fStartLine.getOffset(), lineInfo.getOffset());
				int end= Math.max(fStartLine.getOffset() + fStartLine.getLength(), lineInfo.getOffset() + lineInfo.getLength());

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
			final Display display = fCanvas.getDisplay();
			Runnable timer= null;
			switch (direction) {
				case SWT.UP:
					timer= new Runnable() {
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
					timer = new Runnable() {
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
	}

	/** This column's parent ruler */
	private CompositeRuler fParentRuler;
	/** Cached text viewer */
	private ITextViewer fCachedTextViewer;
	/** Cached text widget */
	private StyledText fCachedTextWidget;
	/** The columns canvas */
	private Canvas fCanvas;
	/** Cache for the actual scroll position in pixels */
	private int fScrollPos;
	/** The drawable for double buffering */
	private Image fBuffer;
	/** The internal listener */
	private InternalListener fInternalListener= new InternalListener();
	/** The font of this column */
	private Font fFont;
	/** The indentation cache */
	private int[] fIndentation;
	/** Indicates whether this column reacts on text change events */
	private boolean fSensitiveToTextChanges= false;
	/** The foreground color */
	private Color fForeground;
	/** The background color */
	private Color fBackground;
	/** Cached number of displayed digits */
	private int fCachedNumberOfDigits= -1;
	/** Flag indicating whether a relayout is required */
	private boolean fRelayoutRequired= false;
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
	private Runnable fRunnable= new Runnable() {
		public void run() {
			synchronized (fRunnableLock) {
				fIsRunnablePosted= false;
			}
			redraw();
		}
	};
	private MouseHandler fMouseHandler;


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

	/*
	 * @see IVerticalRulerColumn#getControl()
	 */
	public Control getControl() {
		return fCanvas;
	}

	/*
	 * @see IVerticalRuleColumnr#getWidth
	 */
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
		if (fCanvas == null)
			return;

		GC gc= new GC(fCanvas);
		try {

			gc.setFont(fCanvas.getFont());

			fIndentation= new int[fCachedNumberOfDigits + 1];

			Float number= new Float(Math.pow(10, fCachedNumberOfDigits) - 1);
			Point p= gc.stringExtent(Integer.toString(number.intValue()));
			fIndentation[0]= p.x;

			for (int i= 1; i <= fCachedNumberOfDigits; i++) {
				number= new Float(Math.pow(10, i) - 1);
				p= gc.stringExtent(Integer.toString(number.intValue()));
				fIndentation[i]= fIndentation[0] - p.x;
			}

		} finally {
			gc.dispose();
		}
	}

	/*
	 * @see IVerticalRulerColumn#createControl(CompositeRuler, Composite)
	 */
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {

		fParentRuler= parentRuler;
		fCachedTextViewer= parentRuler.getTextViewer();
		fCachedTextWidget= fCachedTextViewer.getTextWidget();

		fCanvas= new Canvas(parentControl, SWT.NONE);
		fCanvas.setBackground(getBackground(fCanvas.getDisplay()));
		fCanvas.setForeground(fForeground);

		fCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				if (fCachedTextViewer != null)
					doubleBufferPaint(event.gc);
			}
		});

		fCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				handleDispose();
				fCachedTextViewer= null;
				fCachedTextWidget= null;
			}
		});

		fMouseHandler= new MouseHandler();
		fCanvas.addMouseListener(fMouseHandler);
		fCanvas.addMouseMoveListener(fMouseHandler);

		if (fCachedTextViewer != null) {

			fCachedTextViewer.addViewportListener(fInternalListener);
			fCachedTextViewer.addTextListener(fInternalListener);

			if (fFont == null) {
				if (fCachedTextWidget != null && !fCachedTextWidget.isDisposed())
					fFont= fCachedTextWidget.getFont();
			}
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
			fCachedTextViewer.removeViewportListener(fInternalListener);
			fCachedTextViewer.removeTextListener(fInternalListener);
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

		if (size.x <= 0 || size.y <= 0)
			return;

		if (fBuffer != null) {
			Rectangle r= fBuffer.getBounds();
			if (r.width != size.x || r.height != size.y) {
				fBuffer.dispose();
				fBuffer= null;
			}
		}
		if (fBuffer == null)
			fBuffer= new Image(fCanvas.getDisplay(), size.x, size.y);

		GC gc= new GC(fBuffer);
		gc.setFont(fCanvas.getFont());
		if (fForeground != null)
			gc.setForeground(fForeground);

		try {
			gc.setBackground(getBackground(fCanvas.getDisplay()));
			gc.fillRectangle(0, 0, size.x, size.y);

			ILineRange visibleLines= JFaceTextUtil.getVisibleModelLines(fCachedTextViewer);
			if (visibleLines == null)
				return;
			doPaint(gc, visibleLines);
		} finally {
			gc.dispose();
		}

		dest.drawImage(fBuffer, 0, 0);
	}

	/**
	 * Returns the view port height in lines.
	 * 
	 * @return the view port height in lines
	 * @deprecated as of 3.2 the number of lines in the viewport cannot be computed because
	 *             StyledText supports variable line heights
	 */
	protected int getVisibleLinesInViewport() {
		return JFaceTextUtil.getVisibleLinesInViewport(fCachedTextWidget);
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
	 */
	void doPaint(GC gc, ILineRange visibleLines) {
		fSensitiveToTextChanges= isViewerCompletelyShown();

		fScrollPos= fCachedTextWidget.getTopPixel();
		Display display= fCachedTextWidget.getDisplay();
		
		// draw diff info
		int y= -JFaceTextUtil.getHiddenTopLinePixels(fCachedTextWidget);
		
		int lastLine= end(visibleLines);
		for (int line= visibleLines.getStartLine(); line < lastLine; line++) {
			int widgetLine= JFaceTextUtil.modelLineToWidgetLine(fCachedTextViewer, line);
			if (widgetLine == -1)
				continue;

			int lineHeight= fCachedTextWidget.getLineHeight(fCachedTextWidget.getOffsetAtLine(widgetLine));
			paintLine(line, y, lineHeight, gc, display);
			y += lineHeight;
		}
	}

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
		
		FontMetrics fm = gc.getFontMetrics();
		int fontBaseline = fm.getAscent() + fm.getLeading();
		int baselineBias= widgetBaseline - fontBaseline;
		return Math.max(0, baselineBias);
	}

	/**
	 * Paints the line. After this method is called the line numbers are painted on top
	 * of the result of this method.
	 * <p>This default implementation does nothing.</p>
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
		int indentation= fIndentation[s.length()];
		int baselineBias= getBaselineBias(gc, widgetLine);
		gc.drawString(s, indentation, y + baselineBias, true);
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

	/*
	 * @see IVerticalRulerColumn#redraw()
	 */
	public void redraw() {

		if (fRelayoutRequired) {
			layout(true);
			return;
		}

		if (fCachedTextViewer != null && fCanvas != null && !fCanvas.isDisposed()) {
			GC gc= new GC(fCanvas);
			doubleBufferPaint(gc);
			gc.dispose();
		}
	}

	/*
	 * @see IVerticalRulerColumn#setModel(IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
	}

	/*
	 * @see IVerticalRulerColumn#setFont(Font)
	 */
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
}
