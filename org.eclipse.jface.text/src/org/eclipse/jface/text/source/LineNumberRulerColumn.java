/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.jface.text.ITextViewerExtension3;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextEvent;


/**
 * A vertical ruler column displaying line numbers.
 * Clients usually instantiate and configure object of this class.
 * 
 * @since 2.0
 */
public final class LineNumberRulerColumn implements IVerticalRulerColumn {
	
	/**
	 * Internal listener class.
	 */
	class InternalListener implements IViewportListener, ITextListener {
		
		/*
		 * @see IViewportListener#viewportChanged(int)
		 */
		public void viewportChanged(int verticalPosition) {
			if (verticalPosition != fScrollPos)
				redraw();
		}
		
		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent event) {
			
			if (computeNumberOfDigits()) {
				computeIndentations();
				layout(event.getViewerRedrawState());
				return;
			}
			
			if (!event.getViewerRedrawState())
				return;
				
			if (fSensitiveToTextChanges || event.getDocumentEvent() == null) {
				if (fCanvas != null && !fCanvas.isDisposed()) {
					Display d= fCanvas.getDisplay();
					if (d != null) {
						d.asyncExec(new Runnable() {
							public void run() {
								redraw();
							}
						});
					}	
				}
			}
		}
	};
	
	/**
	 * Handles all the mouse interaction in this line number ruler column.
	 */
	class MouseHandler implements MouseListener, MouseMoveListener, MouseTrackListener {
		
		/** The cached view port size */
		private int fCachedViewportSize;
		/** The area of the line at which line selection started */
		private IRegion fStartLine;
		/** The number of the line at which line selection started */
		private int fStartLineNumber;
		/** The auto scroll direction */
		private int fAutoScrollDirection;
	
		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent event) {
			stopSelecting();
			stopAutoScroll();
		}
		
		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			startSelecting();
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
			if (!autoScroll(event)) {
				int newLine= fParentRuler.toDocumentLineNumber(event.y);
				expandSelection(newLine);
			}
		}
		
		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseEnter(MouseEvent event) {
		}

		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseExit(MouseEvent event) {
		}

		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseHover(MouseEvent event) {
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
				fCanvas.addMouseMoveListener(this);
				fCanvas.addMouseTrackListener(this);

			} catch (BadLocationException x) {
			}
		}
		
		/**
		 * Called when line drag selection stopped. Removes all previously
		 * installed listeners from this column's control.
		 */
		private void stopSelecting() {
			// drag selection stopped
			fCanvas.removeMouseMoveListener(this);
			fCanvas.removeMouseTrackListener(this);
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
		 * @return <code>true</code> if scrolling happend, <code>false</code> otherwise
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
				int top= fCachedTextViewer.getTopIndex();
				if ((fCachedTextWidget.getTopPixel() % fCachedTextWidget.getLineHeight()) != 0)
					-- top;
				return top;
			}
			return -1;
		}
	};
	
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
	 */
	protected boolean computeNumberOfDigits() {
		
		IDocument document= fCachedTextViewer.getDocument();
		int lines= document == null ? 0 : document.getNumberOfLines();
		
		int digits= 2;
		while (lines > Math.pow(10, digits) -1) {
			++digits;
		}
		
		if (fCachedNumberOfDigits != digits) {
			fCachedNumberOfDigits= digits;
			return true;
		}
		
		return false;
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
		
		fCanvas.addMouseListener(new MouseHandler());
		
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
			
		computeNumberOfDigits();
		computeIndentations();
		return fCanvas;
	}
	
	/**
	 * Disposes the column's resources.
	 */
	private void handleDispose() {
		
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
	 * @param dest the gc to draw into
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
			
			if (fCachedTextViewer instanceof ITextViewerExtension3)
				doPaint1(gc);
			else
				doPaint(gc);
				
		} finally {
			gc.dispose();
		}
		
		dest.drawImage(fBuffer, 0, 0);
	}
	
	/**
	 * Returns the viewport height in lines.
	 *
	 * @return the viewport height in lines
	 */
	protected int getVisibleLinesInViewport() {
		Rectangle clArea= fCachedTextWidget.getClientArea();
		if (!clArea.isEmpty())
			return clArea.height / fCachedTextWidget.getLineHeight();
		return -1;
	}
	
	/**
	 * Draws the ruler column.
	 * 
	 * @param gc the gc to draw into
	 */
	private void doPaint(GC gc) {
		
		if (fCachedTextViewer == null)
			return;
			
		
		int firstLine= 0;
			
		int topLine= fCachedTextViewer.getTopIndex() -1;
		int bottomLine= fCachedTextViewer.getBottomIndex() + 1;
		
		try {
			
			IRegion region= fCachedTextViewer.getVisibleRegion();
			IDocument doc= fCachedTextViewer.getDocument();
			
			firstLine= doc.getLineOfOffset(region.getOffset());
			if (firstLine > topLine)
				topLine= firstLine;
					
			int lastLine= doc.getLineOfOffset(region.getOffset() + region.getLength());
			if (lastLine < bottomLine)
				bottomLine= lastLine;
				
		} catch (BadLocationException x) {
			return;
		}
		
		fSensitiveToTextChanges= bottomLine - topLine < getVisibleLinesInViewport();
		
		int lineheight= fCachedTextWidget.getLineHeight();
		fScrollPos= fCachedTextWidget.getTopPixel();
		int canvasheight= fCanvas.getSize().y;

		int y= ((topLine - firstLine) * lineheight) - fScrollPos + fCachedTextViewer.getTopInset();
		for (int line= topLine; line <= bottomLine; line++, y+= lineheight) {
			
			if (y >= canvasheight)
				break;
				
			String s= Integer.toString(line + 1);
			int indentation= fIndentation[s.length()];
			gc.drawString(s, indentation, y);
		}
	}
	
	/**
	 * Draws the ruler column. Uses <code>ITextViewerExtension3</code> for the
	 * implementation. Will replace <code>doPinat(GC)</code>.
	 * 
	 * @param gc the gc to draw into
	 */
	private void doPaint1(GC gc) {

		if (fCachedTextViewer == null)
			return;

		ITextViewerExtension3 extension= (ITextViewerExtension3) fCachedTextViewer;

		int firstLine= 0;


		int widgetTopLine= fCachedTextWidget.getTopIndex();
		if (widgetTopLine > 0)
			-- widgetTopLine;

		int topLine= extension.widgetlLine2ModelLine(widgetTopLine);
		int bottomLine= fCachedTextViewer.getBottomIndex();
		if (bottomLine >= 0)
			++ bottomLine;

		try {

			IRegion region= extension.getModelCoverage();
			IDocument doc= fCachedTextViewer.getDocument();

			firstLine= doc.getLineOfOffset(region.getOffset());
			if (firstLine > topLine || topLine == -1)
				topLine= firstLine;

			int lastLine= doc.getLineOfOffset(region.getOffset() + region.getLength());
			if (lastLine < bottomLine || bottomLine == -1)
				bottomLine= lastLine;

		} catch (BadLocationException x) {
			return;
		}

		fSensitiveToTextChanges= bottomLine - topLine < getVisibleLinesInViewport();

		int lineheight= fCachedTextWidget.getLineHeight();
		fScrollPos= fCachedTextWidget.getTopPixel();
		int canvasheight= fCanvas.getSize().y;

		int y= (widgetTopLine * lineheight) - fScrollPos + fCachedTextViewer.getTopInset();
		for (int modelLine= topLine; modelLine <= bottomLine; modelLine++) {

			if (y >= canvasheight)
				break;

			int widgetLine= extension.modelLine2WidgetLine(modelLine);
			if (widgetLine == -1)
				continue;

			String s= Integer.toString(modelLine + 1);
			int indentation= fIndentation[s.length()];
			gc.drawString(s, indentation, y);
			y+= lineheight;
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
		
		if (fCanvas != null && !fCanvas.isDisposed()) {
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
			computeNumberOfDigits();
			computeIndentations();
		}
	}
}
