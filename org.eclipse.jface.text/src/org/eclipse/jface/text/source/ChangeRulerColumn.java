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

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension3;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextEvent;

/**
 * A vertical ruler column displaying line numbers and serving as a UI for quick diff.
 * Clients usually instantiate and configure object of this class.
 *
 * @since 3.0
 */
public final class ChangeRulerColumn implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension, IChangeRulerColumn {
	
	/**
	 * Handles all the mouse interaction in this line number ruler column.
	 */
	class MouseHandler implements MouseListener {
	
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
	
	}

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
			
			if (!event.getViewerRedrawState())
				return;
				
			if (fSensitiveToTextChanges || event.getDocumentEvent() == null)
				postRedraw();

		}
	}
	
	/**
	 * Internal listener class that will update the ruler when the underlying model changes.
	 */
	class AnnotationListener implements IAnnotationModelListener {
		/*
		 * @see org.eclipse.jface.text.source.IAnnotationModelListener#modelChanged(org.eclipse.jface.text.source.IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			postRedraw();
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
	/** Indicates whether this column reacts on text change events */
	private boolean fSensitiveToTextChanges= false;
	/** The foreground color */
	private Color fForeground;
	/** The background color */
	private Color fBackground;
	/** Color for changed lines */
	private Color fAddedColor;
	/** Color for added lines */
	private Color fChangedColor;
	/** Color for the deleted line indicator */
	private Color fDeletedColor;
	/** The ruler's annotation model. */
	private IAnnotationModel fAnnotationModel;
	/** The ruler's hover */
	private IAnnotationHover fHover;
	/** The internal listener */
	private AnnotationListener fAnnotationListener= new AnnotationListener();
	/** The width of the change ruler column. */
	private int fWidth= 5;

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
		}
		
		return fCanvas;
	}
	
	/**
	 * Disposes the column's resources.
	 */
	protected void handleDispose() {
		
		if (fAnnotationModel != null) {
			fAnnotationModel.removeAnnotationModelListener(fAnnotationListener);
			fAnnotationModel= null;
		}
		
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
			
		if (fCachedTextWidget == null)
			return;
			
		
		int firstLine= 0;
			
		int topLine= fCachedTextViewer.getTopIndex() -1;
		int bottomLine= fCachedTextViewer.getBottomIndex() + 1;
		
		try {
			
			IRegion region= fCachedTextViewer.getVisibleRegion();
			IDocument doc= fCachedTextViewer.getDocument();
			
			if (doc == null)
				return;
			
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
			
			paintLine(line, y, lineheight, gc);
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
			
			if (doc == null)
				 return;

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

			paintLine(modelLine, y, lineheight, gc);
			
			y+= lineheight;
		}
	}
	
	/*
	 * @see IVerticalRulerColumn#redraw()
	 */
	public void redraw() {
		
		if (fCanvas != null && !fCanvas.isDisposed()) {
			GC gc= new GC(fCanvas);
			doubleBufferPaint(gc);
			gc.dispose();
		}
	}
	
	/*
	 * @see IVerticalRulerColumn#setFont(Font)
	 */
	public void setFont(Font font) {
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

	/*
	 * @see org.eclipse.jface.text.source.LineNumberRulerColumn#paintLineHook(int, int, int, org.eclipse.swt.graphics.GC)
	 */
	protected void paintLine(int line, int y, int lineheight, GC gc) {
		ILineDiffInfo info= getDiffInfo(line);

		if (info != null) {
			// width of the column
			int width= getWidth();

			// draw background color if special
			if (hasSpecialColor(info)) {
				gc.setBackground(getColor(info));
				gc.fillRectangle(0, y, width, lineheight);
			}

			/* Deletion Indicator: Simply a horizontal line */
			int delBefore= info.getRemovedLinesAbove();
			int delBelow= info.getRemovedLinesBelow();
			if (delBefore > 0 || delBelow > 0) {
				Color deletionColor= getDeletionColor();
				gc.setForeground(deletionColor);

				if (delBefore > 0) {
					gc.drawLine(0, y, width, y);
				}

				if (delBelow > 0) {
					gc.drawLine(0, y + lineheight - 1, width, y + lineheight - 1);
				}
			}
		}
	}

	/**
	 * Returns whether the line background differs from the default.
	 * 
	 * @param info the info being queried
	 * @return <code>true</code> if <code>info</code> describes either a changed or an added line.
	 */
	private boolean hasSpecialColor(ILineDiffInfo info) {
		return info.getType() == ILineDiffInfo.ADDED || info.getType() == ILineDiffInfo.CHANGED;
	}

	/**
	 * Retrieves the <code>ILineDiffInfo</code> for <code>line</code> from the model.
	 * There are optimizations for direct access and sequential access patterns.
	 * 
	 * @param line the line we want the info for.
	 * @return the <code>ILineDiffInfo</code> for <code>line</code>, or <code>null</code>.
	 */
	private ILineDiffInfo getDiffInfo(int line) {
		if (fAnnotationModel == null)
			return null;

		// assume direct access
		if (fAnnotationModel instanceof ILineDiffer) {
			ILineDiffer differ= (ILineDiffer)fAnnotationModel;
			return differ.getLineInfo(line);
		}
		
		return null;
	}

	/**
	 * Returns the current background color.
	 * 
	 * @return the currently set background color or <code>null</code>
	 * @since 3.0
	 */
	protected Color getBackground() {
		return fBackground;
	}
	
	/**
	 * Returns the color for deleted lines.
	 * 
	 * @return the color to be used for the deletion indicator
	 */
	private Color getDeletionColor() {
		return fDeletedColor == null ? getBackground() : fDeletedColor;
	}

	/**
	 * Returns the color for the given line diff info.
	 * 
	 * @param info the <code>ILineDiffInfo</code> being queried
	 * @return the correct background color for the line type being described by <code>info</code>
	 */
	private Color getColor(ILineDiffInfo info) {
		Assert.isTrue(info != null && info.getType() != ILineDiffInfo.UNCHANGED);
		Color ret= null;
		switch (info.getType()) {
			case ILineDiffInfo.CHANGED :
				ret= fChangedColor;
				break;
			case ILineDiffInfo.ADDED :
				ret= fAddedColor;
				break;
		}
		return ret == null ? getBackground() : ret;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	public int getLineOfLastMouseButtonActivity() {
		return getParentRuler().getLineOfLastMouseButtonActivity();
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	public int toDocumentLineNumber(int y_coordinate) {
		return getParentRuler().toDocumentLineNumber(y_coordinate);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getHover()
	 */
	public IAnnotationHover getHover() {
		return fHover;
	}

	/**
	 * Sets the hover of this ruler column.
	 * 
	 * @param hover the hover that will produce hover information text for this ruler column
	 */
	public void setHover(IAnnotationHover hover) {
		fHover= hover;
	}

	/*
	 * @see IVerticalRulerColumn#setModel(IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
		IAnnotationModel newModel;
		if (model instanceof IAnnotationModelExtension) {
			newModel= ((IAnnotationModelExtension)model).getAnnotationModel(QUICK_DIFF_MODEL_ID);
		} else {
			newModel= model;
		}
		if (fAnnotationModel != newModel) {
			if (fAnnotationModel != null) {
				fAnnotationModel.removeAnnotationModelListener(fAnnotationListener);
			}
			fAnnotationModel= newModel;
			if (fAnnotationModel != null) {
				fAnnotationModel.addAnnotationModelListener(fAnnotationListener);
			}
			redraw();
		}
	}

	/**
	 * Sets the background color for added lines. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
	 * 
	 * @param addedColor the new color to be used for the added lines background
	 */
	public void setAddedColor(Color addedColor) {
		fAddedColor= addedColor;
	}

	/**
	 * Sets the background color for changed lines. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
	 * 
	 * @param changedColor the new color to be used for the changed lines background
	 */
	public void setChangedColor(Color changedColor) {
		fChangedColor= changedColor;
	}

	/**
	 * Sets the color for the deleted lines indicator. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
	 * 
	 * @param deletedColor the new color to be used for the deleted lines indicator.
	 */
	public void setDeletedColor(Color deletedColor) {
		fDeletedColor= deletedColor;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getModel()
	 */
	public IAnnotationModel getModel() {
		return fAnnotationModel;
	}

	/*
	 * @see IVerticalRulerColumn#getControl()
	 */
	public Control getControl() {
		return fCanvas;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getWidth()
	 */
	public int getWidth() {
		return fWidth;
	}
	
	/**
	 * Triggers a redraw in the display thread.
	 */
	protected final void postRedraw() {
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
