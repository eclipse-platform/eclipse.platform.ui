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


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;


/**
 * A vertical ruler column showing graphical representations of  annotations.
 * Will become final. Do not subclass.
 * @since 2.0
 */
public class AnnotationRulerColumn implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {
	
	/**
	 * Internal listener class.
	 */
	class InternalListener implements IViewportListener, IAnnotationModelListener, ITextListener {
		
		/*
		 * @see IViewportListener#viewportChanged(int)
		 */
		public void viewportChanged(int verticalPosition) {
			if (verticalPosition != fScrollPos)
				redraw();
		}
		
		/*
		 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			postRedraw();
		}
		
		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent e) {
			if (e.getViewerRedrawState())
				postRedraw();
		}
	}
	
	
	/** This column's parent ruler */
	private CompositeRuler fParentRuler;
	/** The cached text viewer */
	private ITextViewer fCachedTextViewer;
	/** The cached text widget */
	private StyledText fCachedTextWidget;
	/** The ruler's canvas */
	private Canvas fCanvas;
	/** The vertical ruler's model */
	private IAnnotationModel fModel;
	/** Cache for the actual scroll position in pixels */
	private int fScrollPos;
	/** The drawable for double buffering */
	private Image fBuffer;
	/** The internal listener */
	private InternalListener fInternalListener= new InternalListener();
	/** The width of this vertical ruler */
	private int fWidth;
	/** Switch for enabling/disabling the setModel method. */
	private boolean fAllowSetModel= true;
	/**
	 * The list of annotation types to be shown in this ruler.
	 * @since 3.0
	 */
	private Set fConfiguredAnnotationTypes= new HashSet();
	/**
	 * The list of allowed annotation types to be shown in this ruler.
	 * @since 3.0
	 */
	private Set fAllowedAnnotationTypes= new HashSet();
	/**
	 * The annotation access.
	 * @since 3.0
	 */
	private IAnnotationAccess fAnnotationAccess;
	/** 
	 * The hover for this column.
	 * @since 3.0
	 */
	private IAnnotationHover fHover;
	
	
	/**
	 * Constructs this column with the given arguments.
	 *
	 * @param model the annotation model to get the annotations from
	 * @param width the width of the vertical ruler
	 * @param annotationAccess the annotation access
	 * @since 3.0
	 */
	public AnnotationRulerColumn(IAnnotationModel model, int width, IAnnotationAccess annotationAccess) {
		fWidth= width;
		fAllowSetModel= false;
		fModel= model;
		fModel.addAnnotationModelListener(fInternalListener);
		fAnnotationAccess= annotationAccess;
	}
	
	/**
	 * Constructs this column with the given arguments.
	 *
	 * @param width the width of the vertical ruler
	 * @param annotationAccess the annotation access
	 * @since 3.0
	 */
	public AnnotationRulerColumn(int width, IAnnotationAccess annotationAccess) {
		fWidth= width;
		fAnnotationAccess= annotationAccess;
	}
	
	/**
	 * Constructs this column with the given arguments.
	 *
	 * @param model the annotation model to get the annotations from
	 * @param width the width of the vertical ruler
	 */
	public AnnotationRulerColumn(IAnnotationModel model, int width) {
		fWidth= width;
		fAllowSetModel= false;
		fModel= model;
		fModel.addAnnotationModelListener(fInternalListener);
	}
	
	/**
	 * Constructs this column with the given width.
	 *
	 * @param width the width of the vertical ruler
	 */
	public AnnotationRulerColumn(int width) {
		fWidth= width;
	}
	
	/*
	 * @see IVerticalRulerColumn#getControl()
	 */
	public Control getControl() {
		return fCanvas;
	}
	
	/*
	 * @see IVerticalRulerColumn#getWidth()
	 */
	public int getWidth() {
		return fWidth;
	}
	
	/*
	 * @see IVerticalRulerColumn#createControl(CompositeRuler, Composite)
	 */
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		
		fParentRuler= parentRuler;
		fCachedTextViewer= parentRuler.getTextViewer();
		fCachedTextWidget= fCachedTextViewer.getTextWidget();
		
		fCanvas= new Canvas(parentControl, SWT.NO_BACKGROUND);
		
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
		
		fCanvas.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent event) {
				fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
				mouseClicked(fParentRuler.getLineOfLastMouseButtonActivity());				
			}
			
			public void mouseDown(MouseEvent event) {
				fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			}
			
			public void mouseDoubleClick(MouseEvent event) {
				fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
				mouseDoubleClicked(fParentRuler.getLineOfLastMouseButtonActivity());
			}
		});
		
		if (fCachedTextViewer != null) {
			fCachedTextViewer.addViewportListener(fInternalListener);
			fCachedTextViewer.addTextListener(fInternalListener);
		}
		
		return fCanvas;
	}
	
	/**
	 * Hook method for a mouse double click event on the given ruler line.
	 * 
	 * @param rulerLine the ruler line
	 */
	protected void mouseDoubleClicked(int rulerLine) {
	}

	/**
	 * Hook method for a mouse click event on the given ruler line.
	 * 
	 * @param rulerLine the ruler line
	 */
	protected void mouseClicked(int rulerLine) {
	}
	
	/**
	 * Disposes the ruler's resources.
	 */
	private void handleDispose() {
		
		if (fCachedTextViewer != null) {
			fCachedTextViewer.removeViewportListener(fInternalListener);
			fCachedTextViewer.removeTextListener(fInternalListener);
		}
		
		if (fModel != null)
			fModel.removeAnnotationModelListener(fInternalListener);
		
		if (fBuffer != null) {
			fBuffer.dispose();
			fBuffer= null;
		}
		
		fConfiguredAnnotationTypes.clear();
		fAllowedAnnotationTypes.clear();
		fAnnotationAccess= null;
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
		gc.setFont(fCachedTextWidget.getFont());
		try {
			gc.setBackground(fCanvas.getBackground());
			gc.fillRectangle(0, 0, size.x, size.y);
			
			if (fCachedTextViewer instanceof ITextViewerExtension5)
				doPaint1(gc);
			else
				doPaint(gc);
		} finally {
			gc.dispose();
		}
		
		dest.drawImage(fBuffer, 0, 0);
	}

	/**
	 * Returns the document offset of the upper left corner of the source viewer's
	 * viewport, possibly including partially visible lines.
	 * 
	 * @return document offset of the upper left corner including partially visible lines
	 */
	protected int getInclusiveTopIndexStartOffset() {
		
		if (fCachedTextWidget != null && !fCachedTextWidget.isDisposed()) {	
			int top= fCachedTextViewer.getTopIndex();
			if ((fCachedTextWidget.getTopPixel() % fCachedTextWidget.getLineHeight()) != 0)
				top--;
			try {
				IDocument document= fCachedTextViewer.getDocument();
				return document.getLineOffset(top);
			} catch (BadLocationException ex) {
			}
		}
		
		return -1;
	}
	
	/**
	 * Draws the vertical ruler w/o drawing the Canvas background.
	 * 
	 * @param gc the GC to draw into
	 */
	protected void doPaint(GC gc) {
	
		if (fModel == null || fCachedTextViewer == null)
			return;
		
		IAnnotationAccessExtension annotationAccessExtension= null;
		if (fAnnotationAccess instanceof IAnnotationAccessExtension)
			annotationAccessExtension= (IAnnotationAccessExtension) fAnnotationAccess;

		int topLeft= getInclusiveTopIndexStartOffset();
		int bottomRight;
		
		if (fCachedTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fCachedTextViewer;
			IRegion coverage= extension.getModelCoverage();
			bottomRight= coverage.getOffset() + coverage.getLength();
		} else if (fCachedTextViewer instanceof TextViewer) {
			// TODO remove once TextViewer implements ITextViewerExtension5
			TextViewer extension= (TextViewer) fCachedTextViewer;
			IRegion coverage= extension.getModelCoverage();
			bottomRight= coverage.getOffset() + coverage.getLength();
		} else {
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=14938
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=22487
			// add 1 as getBottomIndexEndOffset returns the inclusive offset, but we want the exclusive offset (right after the last character)
			bottomRight= fCachedTextViewer.getBottomIndexEndOffset() + 1;
		}
		int viewPort= bottomRight - topLeft;
		
		fScrollPos= fCachedTextWidget.getTopPixel();
		int lineheight= fCachedTextWidget.getLineHeight();
		Point dimension= fCanvas.getSize();
		int shift= fCachedTextViewer.getTopInset();

		IDocument doc= fCachedTextViewer.getDocument();		
		
		int topLine= -1, bottomLine= -1;
		try {
			IRegion region= fCachedTextViewer.getVisibleRegion();
			topLine= doc.getLineOfOffset(region.getOffset());
			bottomLine= doc.getLineOfOffset(region.getOffset() + region.getLength());
		} catch (BadLocationException x) {
			return;
		}
				
		// draw Annotations
		Rectangle r= new Rectangle(0, 0, 0, 0);
		int maxLayer= 1;	// loop at least once through layers.
		
		for (int layer= 0; layer < maxLayer; layer++) {
			Iterator iter= fModel.getAnnotationIterator();
			while (iter.hasNext()) {
				Annotation annotation= (Annotation) iter.next();
				
				if (skip(annotation))
					continue;
				
				int lay= IAnnotationAccessExtension.DEFAULT_LAYER;
				if (annotationAccessExtension != null)
					lay= annotationAccessExtension.getLayer(annotation);
				maxLayer= Math.max(maxLayer, lay+1);	// dynamically update layer maximum
				if (lay != layer)	// wrong layer: skip annotation
					continue;
				
				Position position= fModel.getPosition(annotation);
				if (position == null)
					continue;
				
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=20284
				// Position.overlapsWith returns false if the position just starts at the end
				// of the specified range. If the position has zero length, we want to include it anyhow
				int viewPortSize= position.getLength() == 0 ? viewPort + 1 : viewPort;
				if (!position.overlapsWith(topLeft, viewPortSize))
					continue;
					
				try {
					
					int offset= position.getOffset();
					int length= position.getLength();
					
					int startLine= doc.getLineOfOffset(offset);
					if (startLine < topLine)
						startLine= topLine;
					
					int endLine= startLine;
					if (length > 0)
						endLine= doc.getLineOfOffset(offset + length - 1);
					if (endLine > bottomLine)
						endLine= bottomLine;

					startLine -= topLine;
					endLine -= topLine;

					r.x= 0;
					r.y= (startLine * lineheight) - fScrollPos + shift;
					r.width= dimension.x;
					int lines= endLine - startLine;
					if (lines < 0)
						lines= -lines;
					r.height= (lines+1) * lineheight;
					
					if (r.y < dimension.y && annotationAccessExtension != null)  // annotation within visible area
						annotationAccessExtension.paint(annotation, gc, fCanvas, r);
					
				} catch (BadLocationException e) {
				}
			}
		}
	}
	
	/**
	 * Draws the vertical ruler w/o drawing the Canvas background. Implementation based
	 * on <code>ITextViewerExtension5</code>. Will replace <code>doPaint(GC)</code>.
	 * 
	 * @param gc the GC to draw into
	 */
	protected void doPaint1(GC gc) {

		if (fModel == null || fCachedTextViewer == null)
			return;
		
		IAnnotationAccessExtension annotationAccessExtension= null;
		if (fAnnotationAccess instanceof IAnnotationAccessExtension)
			annotationAccessExtension= (IAnnotationAccessExtension) fAnnotationAccess;

		ITextViewerExtension5 extension= (ITextViewerExtension5) fCachedTextViewer;

		fScrollPos= fCachedTextWidget.getTopPixel();
		int lineheight= fCachedTextWidget.getLineHeight();
		Point dimension= fCanvas.getSize();
		int shift= fCachedTextViewer.getTopInset();

		// draw Annotations
		Rectangle r= new Rectangle(0, 0, 0, 0);
		int maxLayer= 1;	// loop at least once through layers.

		for (int layer= 0; layer < maxLayer; layer++) {
			Iterator iter= fModel.getAnnotationIterator();
			while (iter.hasNext()) {
				Annotation annotation= (Annotation) iter.next();
				
				if (skip(annotation))
					continue;
				
				int lay= IAnnotationAccessExtension.DEFAULT_LAYER;
				if (annotationAccessExtension != null)
					lay= annotationAccessExtension.getLayer(annotation);
				maxLayer= Math.max(maxLayer, lay+1);	// dynamically update layer maximum
				if (lay != layer)	// wrong layer: skip annotation
					continue;

				Position position= fModel.getPosition(annotation);
				if (position == null)
					continue;

				IRegion widgetRegion= extension.modelRange2WidgetRange(new Region(position.getOffset(), position.getLength()));
				if (widgetRegion == null)
					continue;

				int startLine= extension.widgetLineOfWidgetOffset(widgetRegion.getOffset());
				if (startLine == -1)
					continue;

				int endLine= extension.widgetLineOfWidgetOffset(widgetRegion.getOffset() + Math.max(widgetRegion.getLength() -1, 0));
				if (endLine == -1)
					continue;

				r.x= 0;
				r.y= (startLine * lineheight) - fScrollPos + shift;
				r.width= dimension.x;
				int lines= endLine - startLine;
				if (lines < 0)
					lines= -lines;
				r.height= (lines+1) * lineheight;

				if (r.y < dimension.y && annotationAccessExtension != null)  // annotation within visible area
					annotationAccessExtension.paint(annotation, gc, fCanvas, r);
			}
		}
	}

	
	/**
	 * Post a redraw request for this column into the UI thread.
	 */
	private void postRedraw() {
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
	 * @see IVerticalRulerColumn#setModel
	 */
	public void setModel(IAnnotationModel model) {
		if (fAllowSetModel && model != fModel) {
			
			if (fModel != null)
				fModel.removeAnnotationModelListener(fInternalListener);
			
			fModel= model;
			
			if (fModel != null)
				fModel.addAnnotationModelListener(fInternalListener);
			
			postRedraw();
		}
	}
	
	/*
	 * @see IVerticalRulerColumn#setFont(Font)
	 */
	public void setFont(Font font) {
	}
	
	/**
	 * Returns the cached text viewer.
	 * 
	 * @return the cached text viewer
	 */
	protected ITextViewer getCachedTextViewer() {
		return fCachedTextViewer;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getModel()
	 */
	public IAnnotationModel getModel() {
		return fModel;
	}
	
	/**
	 * Adds the given annotation type to this annotation ruler column. Starting
	 * with this call, annotations of the given type are shown in this annotation
	 * ruler column.
	 * 
	 * @param annotationType the annotation type
	 * @since 3.0
	 */
	public void addAnnotationType(Object annotationType) {
		fConfiguredAnnotationTypes.add(annotationType);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	public int getLineOfLastMouseButtonActivity() {
		return fParentRuler.getLineOfLastMouseButtonActivity();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	public int toDocumentLineNumber(int y_coordinate) {
		return fParentRuler.toDocumentLineNumber(y_coordinate);
	}

	/**
	 * Removes the given annotation type from this annotation ruler column.
	 * Annotations of the given type are no longer shown in this annotation
	 * ruler column.
	 * 
	 * @param annotationType the annotation type
	 * @since 3.0
	 */
	public void removeAnnotationType(Object annotationType) {
		fConfiguredAnnotationTypes.remove(annotationType);
		fAllowedAnnotationTypes.clear();
	}
	
	/**
	 * Returns whether the given annotation should be skipped by the drawing
	 * routine.
	 * 
	 * @param annotation the annotation
	 * @return <code>true</code> if annotation of the given type should be
	 *         skipped, <code>false</code> otherwise
	 * @since 3.0
	 */
	private boolean skip(Annotation annotation) {
		Object annotationType= annotation.getType();
		if (fAllowedAnnotationTypes.contains(annotationType))
			return false;
		
		boolean skip= skip(annotationType);
		if (!skip)
			fAllowedAnnotationTypes.add(annotationType);
		return skip;
	}
	
	/**
	 * Computes whether the annotation of the given type should be skipped or
	 * not.
	 * 
	 * @param annotationType the annotation type
	 * @return <code>true</code> if annotation should be skipped, <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	private boolean skip(Object annotationType) {
		if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension extension= (IAnnotationAccessExtension) fAnnotationAccess;
			Iterator e= fConfiguredAnnotationTypes.iterator();
			while (e.hasNext()) {
				if (extension.isSubtype(annotationType, e.next()))
					return false;
			}
			return true;
		}
		return !fConfiguredAnnotationTypes.contains(annotationType);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getHover()
	 */
	public IAnnotationHover getHover() {
		return fHover;
	}
	
	/**
	 * @param hover The hover to set.
	 */
	public void setHover(IAnnotationHover hover) {
		fHover= hover;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#addAnnotationListener(org.eclipse.jface.text.source.IAnnotationListener)
	 * @since 3.0
	 */
	public void addAnnotationListener(IAnnotationListener listener) {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#removeAnnotationListener(org.eclipse.jface.text.source.IAnnotationListener)
	 * @since 3.0
	 */
	public void removeAnnotationListener(IAnnotationListener listener) {
		throw new UnsupportedOperationException();
	}
}
