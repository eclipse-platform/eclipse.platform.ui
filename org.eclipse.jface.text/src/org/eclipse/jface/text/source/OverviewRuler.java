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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
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
import org.eclipse.jface.text.ITextViewerExtension3;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;



/**
 * Ruler presented next to a source viewer showing all annotations of the
 * viewer's annotation model in a compact format. The ruler has the same height
 * as the source viewer.<p>
 * Clients usually instantiate and configure objects of this class.
 * 
 * @since 2.1
 */
public class OverviewRuler implements IOverviewRuler {
	
	/**
	 * Internal listener class.
	 */
	class InternalListener implements ITextListener, IAnnotationModelListener {
		
		/*
		 * @see ITextListener#textChanged
		 */
		public void textChanged(TextEvent e) {		
			if (fTextViewer != null && e.getDocumentEvent() == null && e.getViewerRedrawState()) {
				// handle only changes of visible document
				redraw();
			}
		}
		
		/*
		 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			update();
		}
	}
	
	/**
	 * Enumerates the annotations of a specified type and characteristics
	 * of the associated annotation model.
	 */
	class FilterIterator implements Iterator {
		
		private final static int IGNORE= 0;
		private final static int TEMPORARY= 1;
		private final static int PERSISTENT= 2;
		
		private Iterator fIterator;
		private Object fType;
		private Annotation fNext;
		private int fTemporary;
		
		public FilterIterator() {
			this(null, IGNORE);
		}
		
		public FilterIterator(Object annotationType) {
			this(annotationType, IGNORE);
		}
		
		public FilterIterator(Object annotationType, boolean temporary) {
			this(annotationType, temporary ? TEMPORARY : PERSISTENT);
		}
		
		private FilterIterator(Object annotationType, int temporary) {
			fType= annotationType;
			fTemporary= temporary;
			if (fModel != null) {
				fIterator= fModel.getAnnotationIterator();
				skip();
			}
		}
		
		private void skip() {
			while (fIterator.hasNext()) {
				Annotation next= (Annotation) fIterator.next();
				Object annotationType= fAnnotationAccess.getType(next);
				if (annotationType == null)
					continue;
					
				fNext= next;
				if (fType == null || fType.equals(annotationType)) {
					if (fTemporary == IGNORE) return;
					boolean temporary= fAnnotationAccess.isTemporary(fNext);
					if (fTemporary == TEMPORARY && temporary) return;
					if (fTemporary == PERSISTENT && !temporary) return;
				}
			}
			fNext= null;
		}
		
		/*
		 * @see Iterator#hasNext()
		 */
		public boolean hasNext() {
			return fNext != null;
		}
		/*
		 * @see Iterator#next()
		 */
		public Object next() {
			try {
				return fNext;
			} finally {
				if (fModel != null)
					skip();
			}
		}
		/*
		 * @see Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * The painter of the overview ruler's header.
	 */
	class HeaderPainter implements PaintListener {
		
		private Color fIndicatorColor;
		private Color fSeparatorColor;
		
		public HeaderPainter() {
			fSeparatorColor= fSharedTextColors.getColor(ViewForm.borderInsideRGB);
		}
		
		public void setColor(Color color) {
			fIndicatorColor= color;
		}
		
		private void drawBevelRect(GC gc, int x, int y, int w, int h, Color topLeft, Color bottomRight) {
			gc.setForeground(topLeft);
			gc.drawLine(x, y, x + w -1, y);
			gc.drawLine(x, y, x, y + h -1);
		
			gc.setForeground(bottomRight);
			gc.drawLine(x + w, y, x + w, y + h);
			gc.drawLine(x, y + h, x + w, y + h);
		}
		
		public void paintControl(PaintEvent e) {
			
			Point s= fHeader.getSize();
			
			if (fIndicatorColor != null) {
				e.gc.setBackground(fIndicatorColor);
				Rectangle r= new Rectangle(INSET, (s.y - (2*ANNOTATION_HEIGHT)) / 2, s.x - (2*INSET), 2*ANNOTATION_HEIGHT);
				e.gc.fillRectangle(r);
				Display d= fHeader.getDisplay();
				if (d != null)
					drawBevelRect(e.gc, r.x, r.y, r.width -1, r.height -1, d.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW), d.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
			}
			
			e.gc.setForeground(fSeparatorColor);
			e.gc.setLineWidth(1);
			e.gc.drawLine(0, s.y -1, s.x -1, s.y -1);
		}
	}
		
	private static final int INSET= 2;
	private static final int ANNOTATION_HEIGHT= 4;
	private static boolean ANNOTATION_HEIGHT_SCALABLE= false;
	
	
	/** The model of the overview ruler */
	private IAnnotationModel fModel;
	/** The view to which this ruler is connected */
	private ITextViewer fTextViewer;
	/** The ruler's canvas */
	private Canvas fCanvas;
	/** The ruler's header */
	private Canvas fHeader;
	/** The drawable for double buffering */
	private Image fBuffer;
	/** The internal listener */
	private InternalListener fInternalListener= new InternalListener();
	/** The width of this vertical ruler */
	private int fWidth;
	/** The hit detection cursor */
	private Cursor fHitDetectionCursor;
	/** The last cursor */
	private Cursor fLastCursor;
	/** The line of the last mouse button activity */
	private int fLastMouseButtonActivityLine= -1;
	/** The actual annotation height */
	private int fAnnotationHeight= -1;
	/** The annotation access */
	private IAnnotationAccess fAnnotationAccess;
	/** The header painter */
	private HeaderPainter fHeaderPainter;
	/** The list of annotation types to be shown in this ruler */
	private Set fAnnotationTypes= new HashSet();
	/** The list of annotation types to be shown in the header of this ruler */
	private Set fHeaderAnnotationTypes= new HashSet();
	/** The mapping between annotation types and colors */
	private Map fAnnotationTypes2Colors= new HashMap();
	/** The color manager */
	private ISharedTextColors fSharedTextColors;
	/**
	 * All available annotation types sorted by layer.
	 * 
	 * @since 3.0
	 */
	private List fAnnotationsSortedByLayer= new ArrayList();
	/**
	 * All available layers sorted by layer.
	 * This list may contain duplicates.
	 * 
	 * @since 3.0
	 */
	private List fLayersSortedByLayer= new ArrayList();
	
	
	/**
	 * Constructs a overview ruler of the given width using the given annotation access and the given
	 * color manager.
	 *
	 * @param annotationAccess the annotation access
	 * @param width the width of the vertical ruler
	 * @param sharedColors the color manager
	 */
	public OverviewRuler(IAnnotationAccess annotationAccess, int width, ISharedTextColors sharedColors) {
		fAnnotationAccess= annotationAccess;
		fWidth= width;		
		fSharedTextColors= sharedColors;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getControl()
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
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRuler#setModel(org.eclipse.jface.text.source.IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
		if (model != fModel || model != null) {
			
			if (fModel != null)
				fModel.removeAnnotationModelListener(fInternalListener);
			
			fModel= model;
			
			if (fModel != null)
				fModel.addAnnotationModelListener(fInternalListener);
			
			update();
		}
	}	
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRuler#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.ITextViewer)
	 */
	public Control createControl(Composite parent, ITextViewer textViewer) {
		
		fTextViewer= textViewer;
		
		fHitDetectionCursor= new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);

		fHeader= new Canvas(parent, SWT.NONE);

		fCanvas= new Canvas(parent, SWT.NO_BACKGROUND);
	
		fCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				if (fTextViewer != null)
					doubleBufferPaint(event.gc);
			}
		});
		
		fCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				handleDispose();
				fTextViewer= null;		
			}
		});
		
		fCanvas.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				handleMouseDown(event);
			}
		});
		
		fCanvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent event) {
				handleMouseMove(event);
			}
		});
		
		if (fTextViewer != null)
			fTextViewer.addTextListener(fInternalListener);
		
		return fCanvas;
	}
	
	/**
	 * Disposes the ruler's resources.
	 */
	private void handleDispose() {
		
		if (fTextViewer != null) {
			fTextViewer.removeTextListener(fInternalListener);
			fTextViewer= null;
		}

		if (fModel != null)
			fModel.removeAnnotationModelListener(fInternalListener);

		if (fBuffer != null) {
			fBuffer.dispose();
			fBuffer= null;
		}
		
		if (fHitDetectionCursor != null) {
			fHitDetectionCursor.dispose();
			fHitDetectionCursor= null;
		}
		
		fAnnotationTypes.clear();
		fHeaderAnnotationTypes.clear();
		fAnnotationTypes2Colors.clear();
		fAnnotationsSortedByLayer.clear();
		fLayersSortedByLayer.clear();
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
		try {
			gc.setBackground(fCanvas.getBackground());
			gc.fillRectangle(0, 0, size.x, size.y);
			
			if (fTextViewer instanceof ITextViewerExtension3)
				doPaint1(gc);
			else
				doPaint(gc);
				
		} finally {
			gc.dispose();
		}
		
		dest.drawImage(fBuffer, 0, 0);
	}
	
	/** 
	 * Draws this overview ruler.
	 * 
	 * @param gc the gc to draw into
	 */
	private void doPaint(GC gc) {
		
		if (fTextViewer == null)
			return;
			
		Rectangle r= new Rectangle(0, 0, 0, 0);
		int yy, hh= ANNOTATION_HEIGHT;
		
		
		IDocument document= fTextViewer.getDocument();
		IRegion visible= fTextViewer.getVisibleRegion();
		
		StyledText textWidget= fTextViewer.getTextWidget();
		int maxLines= textWidget.getLineCount();
				
		Point size= fCanvas.getSize();
		int writable= maxLines * textWidget.getLineHeight();
		if (size.y > writable)
			size.y= writable;
		
		for (Iterator iterator= fAnnotationsSortedByLayer.iterator(); iterator.hasNext();) {
			Object annotationType= iterator.next();
			
			if (skip(annotationType))
				continue;
			
			boolean[] temporary= new boolean[] { false, true };
			for (int t=0; t < temporary.length; t++) {
			
				Iterator e= new FilterIterator(annotationType, temporary[t]);
				Color fill= getFillColor(annotationType, temporary[t]);
				Color stroke= getStrokeColor(annotationType, temporary[t]);
				
				for (int i= 0; e.hasNext(); i++) {
					
					Annotation a= (Annotation) e.next();
					Position p= fModel.getPosition(a);
					
					if (p == null || !p.overlapsWith(visible.getOffset(), visible.getLength()))
						continue;
						
					int annotationOffset= Math.max(p.getOffset(), visible.getOffset());
					int annotationEnd= Math.min(p.getOffset() + p.getLength(), visible.getOffset() + visible.getLength());
					int annotationLength= annotationEnd - annotationOffset;				
					
					try {
						if (ANNOTATION_HEIGHT_SCALABLE) {
							int numbersOfLines= document.getNumberOfLines(annotationOffset, annotationLength);
							hh= (numbersOfLines * size.y) / maxLines;
							if (hh < ANNOTATION_HEIGHT)
								hh= ANNOTATION_HEIGHT;
						}
						fAnnotationHeight= hh;

						int startLine= textWidget.getLineAtOffset(annotationOffset - visible.getOffset());
						yy= Math.min((startLine * size.y) / maxLines, size.y - hh);
							
						if (fill != null) {
							gc.setBackground(fill);
							gc.fillRectangle(INSET, yy, size.x-(2*INSET), hh);
						}
						
						if (stroke != null) {
							gc.setForeground(stroke);
							r.x= INSET;
							r.y= yy;
							r.width= size.x - (2 * INSET) - 1;
							r.height= hh;
							gc.setLineWidth(1);
							gc.drawRectangle(r);
						}
					} catch (BadLocationException x) {
					}
				}
			}
		}
	}
	
	/**
	 * Draws this overview ruler. Uses <code>ITextViewerExtension3</code> for
	 * its implementation. Will replace <code>doPaint(GC)</code>.
	 * 
	 * @param gc the gc to draw into
	 */
	private void doPaint1(GC gc) {

		if (fTextViewer == null)
			return;

		Rectangle r= new Rectangle(0, 0, 0, 0);
		int yy, hh= ANNOTATION_HEIGHT;

		ITextViewerExtension3 extension= (ITextViewerExtension3) fTextViewer;
		IDocument document= fTextViewer.getDocument();		
		StyledText textWidget= fTextViewer.getTextWidget();
		
		int maxLines= textWidget.getLineCount();
		Point size= fCanvas.getSize();
		int writable= maxLines * textWidget.getLineHeight();
		if (size.y > writable)
			size.y= writable;
			
		for (Iterator iterator= fAnnotationsSortedByLayer.iterator(); iterator.hasNext();) {
			Object annotationType= iterator.next();

			if (skip(annotationType))
				continue;

			boolean[] temporary= new boolean[] { false, true };
			for (int t=0; t < temporary.length; t++) {

				Iterator e= new FilterIterator(annotationType, temporary[t]);
				Color fill= getFillColor(annotationType, temporary[t]);
				Color stroke= getStrokeColor(annotationType, temporary[t]);

				for (int i= 0; e.hasNext(); i++) {

					Annotation a= (Annotation) e.next();
					Position p= fModel.getPosition(a);

					if (p == null)
						continue;
						
					IRegion widgetRegion= extension.modelRange2WidgetRange(new Region(p.getOffset(), p.getLength()));
					if (widgetRegion == null)
						continue;
						
					try {
						if (ANNOTATION_HEIGHT_SCALABLE) {
							int numbersOfLines= document.getNumberOfLines(p.getOffset(), p.getLength());
							hh= (numbersOfLines * size.y) / maxLines;
							if (hh < ANNOTATION_HEIGHT)
								hh= ANNOTATION_HEIGHT;
						}
						fAnnotationHeight= hh;

						int startLine= textWidget.getLineAtOffset(widgetRegion.getOffset());						
						yy= Math.min((startLine * size.y) / maxLines, size.y - hh);

						if (fill != null) {
							gc.setBackground(fill);
							gc.fillRectangle(INSET, yy, size.x-(2*INSET), hh);
						}

						if (stroke != null) {
							gc.setForeground(stroke);
							r.x= INSET;
							r.y= yy;
							r.width= size.x - (2 * INSET) - 1;
							r.height= hh;
							gc.setLineWidth(1);
							gc.drawRectangle(r);
						}
					} catch (BadLocationException x) {
					}
				}
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRuler#update()
	 */
	 public void update() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			Display d= fCanvas.getDisplay();
			if (d != null) {
				d.asyncExec(new Runnable() {
					public void run() {
						redraw();
						updateHeader();
					}
				});
			}	
		}
	}
	
	/**
	 * Redraws the overview ruler.
	 */
	private void redraw() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			GC gc= new GC(fCanvas);
			doubleBufferPaint(gc);
			gc.dispose();
		}
	}
	
	/**
	 * Translates a given y-coordinate of this ruler into the corresponding
	 * document lines. The number of lines depends on the concrete scaling
	 * given as the ration between the height of this ruler and the length
	 * of the document.
	 * 
	 * @param y_coordinate the y-coordinate
	 * @return the corresponding document lines
	 */
	private int[] toLineNumbers(int y_coordinate) {
					
		StyledText textWidget=  fTextViewer.getTextWidget();
		int maxLines= textWidget.getContent().getLineCount();
		
		int rulerLength= fCanvas.getSize().y;
		int writable= maxLines * textWidget.getLineHeight();

		if (rulerLength > writable)
			rulerLength= writable;

		if (y_coordinate >= writable)
			return new int[] {-1, -1};

		int[] lines= new int[2];
		
		int pixel= Math.max(y_coordinate - 1, 0);
		lines[0]= (pixel * maxLines) / rulerLength;
		
		pixel= Math.min(rulerLength, y_coordinate + 1);
		lines[1]= (pixel * maxLines) / rulerLength;
		
		if (fTextViewer instanceof ITextViewerExtension3) {
			ITextViewerExtension3 extension= (ITextViewerExtension3) fTextViewer;
			lines[0]= extension.widgetlLine2ModelLine(lines[0]);
			lines[1]= extension.widgetlLine2ModelLine(lines[1]);
		} else {
			try {
				IRegion visible= fTextViewer.getVisibleRegion();
				int lineNumber= fTextViewer.getDocument().getLineOfOffset(visible.getOffset());
				lines[0] += lineNumber;
				lines[1] += lineNumber;
			} catch (BadLocationException x) {
			}
		}
		
		return lines;
	}
	
	/**
	 * Returns the position of the first annotation found in the given line range.
	 * 
	 * @param lineNumbers the line range
	 * @return the position of the first found annotation
	 */
	private Position getAnnotationPosition(int[] lineNumbers) {
		if (lineNumbers[0] == -1)
			return null;
		
		Position found= null;
		
		try {
			IDocument d= fTextViewer.getDocument();
			IRegion line= d.getLineInformation(lineNumbers[0]);

			int start= line.getOffset();
			
			line= d.getLineInformation(lineNumbers[lineNumbers.length - 1]);
			int end= line.getOffset() + line.getLength();

			for (int i= fAnnotationsSortedByLayer.size() -1; i >= 0; i--) {
				
				Object annotationType= fAnnotationsSortedByLayer.get(i);
				
				Iterator e= new FilterIterator(annotationType);
				while (e.hasNext() && found == null) {
					Annotation a= (Annotation) e.next();
					
					if (skip(fAnnotationAccess.getType(a)))
						continue;
					
					Position p= fModel.getPosition(a);
					if (start <= p.getOffset() && p.getOffset() < end) {
						if (found == null || p.getOffset() < found.getOffset())
							found= p;
					}
				}
			}			
		} catch (BadLocationException x) {
		}
		
		return found;
	}

	/**
	 * Returns the line which  corresponds best to one of
	 * the underlying annotations at the given y-coordinate.
	 * 
	 * @return the best matching line or <code>-1</code> if no such line can be found
	 */
	private int findBestMatchingLineNumber(int[] lineNumbers) {
		if (lineNumbers == null || lineNumbers.length < 1)
			return -1;

		try {
			Position pos= getAnnotationPosition(lineNumbers);
			if (pos == null)
				return -1;
			return fTextViewer.getDocument().getLineOfOffset(pos.getOffset());
		} catch (BadLocationException ex) {
			return -1;
		}
	}

	/**
	 * Handles mouse clicks.
	 * 
	 * @param event the mouse button down event
	 */
	private void handleMouseDown(MouseEvent event) {
		if (fTextViewer != null) {
			int[] lines= toLineNumbers(event.y);
			Position p= getAnnotationPosition(lines);
			if (p != null) {
				fTextViewer.revealRange(p.getOffset(), p.getLength());
				fTextViewer.setSelectedRange(p.getOffset(), p.getLength());
			}
			fTextViewer.getTextWidget().setFocus();
		}
		fLastMouseButtonActivityLine= toDocumentLineNumber(event.y);
	}
	
	/**
	 * Handles mouse moves.
	 * 
	 * @param event the mouse move event
	 */
	private void handleMouseMove(MouseEvent event) {
		if (fTextViewer != null) {
			int[] lines= toLineNumbers(event.y);
			Position p= getAnnotationPosition(lines);
			Cursor cursor= (p != null ? fHitDetectionCursor : null);
			if (cursor != fLastCursor) {
				fCanvas.setCursor(cursor);
				fLastCursor= cursor;
			}
		}				
	}
		
	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#addAnnotationType(java.lang.Object)
	 */
	public void addAnnotationType(Object annotationType) {
		fAnnotationTypes.add(annotationType);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#removeAnnotationType(java.lang.Object)
	 */
	public void removeAnnotationType(Object annotationType) {
		fAnnotationTypes.remove(annotationType);
	}
		
	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#setAnnotationTypeLayer(java.lang.Object, int)
	 */
	public void setAnnotationTypeLayer(Object annotationType, int layer) {
		Integer layerObj= new Integer(layer);
		if (fAnnotationsSortedByLayer.remove(annotationType))
			fLayersSortedByLayer.remove(layerObj);

		if (layer >= 0) {
			int i= 0;
			int size= fLayersSortedByLayer.size();
			while (i < size && layer >= ((Integer)fLayersSortedByLayer.get(i)).intValue())
				i++;
			fLayersSortedByLayer.add(i, layerObj);
			fAnnotationsSortedByLayer.add(i, annotationType);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#setAnnotationTypeColor(java.lang.Object, org.eclipse.swt.graphics.Color)
	 */
	public void setAnnotationTypeColor(Object annotationType, Color color) {
		if (color != null)
			fAnnotationTypes2Colors.put(annotationType, color);
		else
			fAnnotationTypes2Colors.remove(annotationType);
	}
	
	/**
	 * Returns whether annotation of the given annotation type should be skipped by the drawing routine.
	 * 
	 * @param annotationType the annotation type
	 * @return <code>true</code> if annotation of the given type should be skipped
	 */
	private boolean skip(Object annotationType) {
		return !fAnnotationTypes.contains(annotationType);
	}
	
	/**
	 * Returns a specification of a color that lies between the given
	 * foreground and background color using the given scale factor.
	 * 
	 * @param fg the foreground color
	 * @param bg the background color
	 * @param scale the scale factor
	 * @return the interpolated color
	 */
	private static RGB interpolate(RGB fg, RGB bg, double scale) {
		return new RGB(
			(int) ((1.0-scale) * fg.red + scale * bg.red),
			(int) ((1.0-scale) * fg.green + scale * bg.green),
			(int) ((1.0-scale) * fg.blue + scale * bg.blue)
		);
	}
	
	/**
	 * Returns the grey value in which the given color would be drawn in grey-scale.
	 * 
	 * @param rgb the color
	 * @return the grey-scale value
	 */
	private static double greyLevel(RGB rgb) {
		if (rgb.red == rgb.green && rgb.green == rgb.blue)
			return rgb.red;
		return  (0.299 * rgb.red + 0.587 * rgb.green + 0.114 * rgb.blue + 0.5);
	}
	
	/**
	 * Returns whether the given color is dark or light depending on the colors grey-scale level.
	 * 
	 * @param rgb the color
	 * @return <code>true</code> if the color is dark, <code>false</code> if it is light
	 */
	private static boolean isDark(RGB rgb) {
		return greyLevel(rgb) > 128;
	}
	
	/**
	 * Returns a color based on the color configured for the given annotation type and the given scale factor.
	 * 
	 * @param annotationType the annotation type
	 * @param scale the scale factor
	 * @return the computed color
	 */
	private Color getColor(Object annotationType, double scale) {
		Color base= (Color) fAnnotationTypes2Colors.get(annotationType);
		if (base == null)
			return null;
			
		RGB baseRGB= base.getRGB();
		RGB background= fCanvas.getBackground().getRGB();
		
		boolean darkBase= isDark(baseRGB);
		boolean darkBackground= isDark(background);
		if (darkBase && darkBackground)
			background= new RGB(255, 255, 255);
		else if (!darkBase && !darkBackground)
			background= new RGB(0, 0, 0);
		
		return fSharedTextColors.getColor(interpolate(baseRGB, background, scale));
	}
	
	/**
	 * Returns the stroke color for the given annotation type and characteristics.
	 * 
	 * @param annotationType the annotation type
	 * @param temporary <code>true</code> if for temporary annotations
	 * @return the stroke color
	 */
	private Color getStrokeColor(Object annotationType, boolean temporary) {
		return getColor(annotationType, temporary ? 0.5 : 0.2);
	}
	
	/**
	 * Returns the fill color for the given annotation type and characteristics.
	 * 
	 * @param annotationType the annotation type
	 * @param temporary <code>true</code> if for temporary annotations
	 * @return the fill color
	 */
	private Color getFillColor(Object annotationType, boolean temporary) {
		return getColor(annotationType, temporary ? 0.9 : 0.6);
	}
	
	/*
	 * @see IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	public int getLineOfLastMouseButtonActivity() {
		return fLastMouseButtonActivityLine;
	}

	/*
	 * @see IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	public int toDocumentLineNumber(int y_coordinate) {
		
		if (fTextViewer == null || y_coordinate == -1)
			return -1;

		int[] lineNumbers= toLineNumbers(y_coordinate);
		int bestLine= findBestMatchingLineNumber(lineNumbers);
		if (bestLine == -1 && lineNumbers.length > 0)
			return lineNumbers[0];
		return	bestLine;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRuler#getModel()
	 */
	public IAnnotationModel getModel() {
		return fModel;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#getAnnotationHeight()
	 */
	public int getAnnotationHeight() {
		return fAnnotationHeight;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#hasAnnotation(int)
	 */
	public boolean hasAnnotation(int y) {
		return findBestMatchingLineNumber(toLineNumbers(y)) != -1;
	}

	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#getHeaderControl()
	 */
	public Control getHeaderControl() {
		return fHeader;
	}

	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#addHeaderAnnotationType(java.lang.Object)
	 */
	public void addHeaderAnnotationType(Object annotationType) {
		fHeaderAnnotationTypes.add(annotationType);
	}

	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#removeHeaderAnnotationType(java.lang.Object)
	 */
	public void removeHeaderAnnotationType(Object annotationType) {
		fHeaderAnnotationTypes.remove(annotationType);
	}
	
	/**
	 * Updates the header of this ruler.
	 */
	private void updateHeader() {
		
		if (fHeader == null || fHeader.isDisposed())
			return;
	
		Object colorType= null;
		outer: for (int i= fAnnotationsSortedByLayer.size() -1; i >= 0; i--) {
			
			Object annotationType= fAnnotationsSortedByLayer.get(i);
			
			if (!fHeaderAnnotationTypes.contains(annotationType) || !fAnnotationTypes.contains(annotationType))
				continue;
			
			for (Iterator e= new FilterIterator(annotationType); e.hasNext();) {
				if (e.next() != null) {
					colorType= annotationType;
					break outer;
				}
			}
		}
		
		Color color= null;
		if (colorType != null) 
			color= (Color) fAnnotationTypes2Colors.get(colorType);
			
		if (color == null) {
			if (fHeaderPainter != null)
				fHeaderPainter.setColor(null);
		}	else {
			if (fHeaderPainter == null) {
				fHeaderPainter= new HeaderPainter();
				fHeader.addPaintListener(fHeaderPainter);
			}
			fHeaderPainter.setColor(color);
		}
			
		fHeader.redraw();
		updateHeaderToolTipText();
	}

	/**
	 * Updates the tool tip text of the header of this ruler.
	 * 
	 * @since 3.0
	 */
	private void updateHeaderToolTipText() {

		if (fHeader == null || fHeader.isDisposed())
			return;

		fHeader.setToolTipText(null);

		if (!(fAnnotationAccess instanceof IAnnotationAccessExtension))
			return;
	
		String overview= ""; //$NON-NLS-1$
		
		for (int i= fAnnotationsSortedByLayer.size() -1; i >= 0; i--) {
			
			Object annotationType= fAnnotationsSortedByLayer.get(i);
			
			if (!fHeaderAnnotationTypes.contains(annotationType) || !fAnnotationTypes.contains(annotationType))
				continue;
	
			int count= 0;
			String annotationTypeLabel= null;
	
			for (Iterator e= new FilterIterator(annotationType); e.hasNext();) {
				Annotation annotation= (Annotation)e.next();
				if (annotation != null) {
					if (annotationTypeLabel == null)
						annotationTypeLabel= ((IAnnotationAccessExtension)fAnnotationAccess).getTypeLabel(annotation);
					count++;
				}
			}
			
			if (annotationTypeLabel != null) {
				if (overview.length() > 0)
					overview += "\n"; //$NON-NLS-1$
				overview += JFaceTextMessages.getFormattedString("OverviewRulerHeader.toolTipTextEntry", new Object[] {annotationTypeLabel, new Integer(count)}); //$NON-NLS-1$
			}
		}
		if (overview.length() > 0)
			fHeader.setToolTipText(overview);
	}
}
