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
import java.util.Collections;
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
 * viewer's annotation model in a compact format, i.e. using the same height as
 * the source viewer.
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
	};
	
	/**
	 * Filters annotations based on their types.
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
				if (fType == null || fType == annotationType) {
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
	};
	
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
	};
		
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
	/** Cache for the actual scroll position in pixels */
	private int fScrollPos;
	/** The line of the last mouse button activity */
	private int fLastMouseButtonActivityLine= -1;
	/** The actual annotation height */
	private int fAnnotationHeight= -1;
	/** The annotation access */
	private IAnnotationAccess fAnnotationAccess;
	/** The header painter */
	private HeaderPainter fHeaderPainter;
	
	private Set fAnnotationTypes= new HashSet();
	private Set fHeaderAnnotationTypes= new HashSet();
	private Map fAnnotationTypes2Layers= new HashMap();
	private Map fAnnotationTypes2Colors= new HashMap();
	private ISharedTextColors fSharedTextColors;
	
	
	
	/**
	 * Constructs a vertical ruler with the given width.
	 *
	 * @param width the width of the vertical ruler
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
		fCanvas= new Canvas(parent, SWT.NO_BACKGROUND);
		fHeader= new Canvas(parent, SWT.NONE);
		
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
		fAnnotationTypes2Layers.clear();
		fAnnotationTypes2Colors.clear();
	}

	/**
	 * Double buffer drawing.
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
	
	private void doPaint(GC gc) {
		
		if (fTextViewer == null)
			return;
			
		Rectangle r= new Rectangle(0, 0, 0, 0);
		int yy, hh= ANNOTATION_HEIGHT;
		
		
		IDocument document= fTextViewer.getDocument();
		IRegion visible= fTextViewer.getVisibleRegion();
		
		StyledText textWidget= fTextViewer.getTextWidget();
		int maxLines= textWidget.getLineCount();
		fScrollPos= textWidget.getTopPixel();		
				
		Point size= fCanvas.getSize();
		int writable= maxLines * textWidget.getLineHeight();
		if (size.y > writable)
			size.y= writable;
		
		
		List indices= new ArrayList(fAnnotationTypes2Layers.keySet());
		Collections.sort(indices);
		
		for (Iterator iterator= indices.iterator(); iterator.hasNext();) {
			Object layer= iterator.next();
			Object annotationType= fAnnotationTypes2Layers.get(layer);
			
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
	
	private void doPaint1(GC gc) {

		if (fTextViewer == null)
			return;

		Rectangle r= new Rectangle(0, 0, 0, 0);
		int yy, hh= ANNOTATION_HEIGHT;

		ITextViewerExtension3 extension= (ITextViewerExtension3) fTextViewer;
		IDocument document= fTextViewer.getDocument();		
		StyledText textWidget= fTextViewer.getTextWidget();
		fScrollPos= textWidget.getTopPixel();
		
		int maxLines= textWidget.getLineCount();
		Point size= fCanvas.getSize();
		int writable= maxLines * textWidget.getLineHeight();
		if (size.y > writable)
			size.y= writable;
			
		List indices= new ArrayList(fAnnotationTypes2Layers.keySet());
		Collections.sort(indices);

		for (Iterator iterator= indices.iterator(); iterator.hasNext();) {
			Object layer= iterator.next();
			Object annotationType= fAnnotationTypes2Layers.get(layer);

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
			
			Iterator e= new FilterIterator();
			while (e.hasNext()) {
				Annotation a= (Annotation) e.next();
				Position p= fModel.getPosition(a);
				if (start <= p.getOffset() && p.getOffset() < end) {
					if (found == null || p.getOffset() < found.getOffset())
						found= p;
				}
			}
			
		} catch (BadLocationException x) {
		}
		
		return found;
	}

	/**
	 * Returns the line which best corresponds to one of
	 * the underlying annotations at the given y ruler coordinate.
	 * 
	 * @return the best matching line or <code>-1</code> if no such line can be
	 * found
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
		
	/**
	 * Sets the layer at which annotations of the given annotation type are
	 * drawn. Layers are drawn in ascending order.
	 * 
	 * @param annotationType
	 * @param layer
	 */
	public void setAnnotationTypeLayer(Object annotationType, int layer) {
		if (layer >= 0)
			fAnnotationTypes2Layers.put(new Integer(layer), annotationType);
		else {
			Iterator e= fAnnotationTypes2Layers.keySet().iterator();
			while (e.hasNext()) {
				Object key= e.next();
				if (annotationType.equals(fAnnotationTypes2Layers.get(key))) {
					fAnnotationTypes2Layers.remove(key);
					return;
				}
			}
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
	
	private boolean skip(Object annotationType) {
		return !fAnnotationTypes.contains(annotationType);
	}
	
	private static RGB interpolate(RGB fg, RGB bg, double scale) {
		return new RGB(
			(int) ((1.0-scale) * fg.red + scale * bg.red),
			(int) ((1.0-scale) * fg.green + scale * bg.green),
			(int) ((1.0-scale) * fg.blue + scale * bg.blue)
		);
	}
	
	private static double greyLevel(RGB rgb) {
		if (rgb.red == rgb.green && rgb.green == rgb.blue)
			return rgb.red;
		return  (0.299 * rgb.red + 0.587 * rgb.green + 0.114 * rgb.blue + 0.5);
	}
	
	private static boolean isDark(RGB rgb) {
		return greyLevel(rgb) > 128;
	}
	
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
	
	private Color getStrokeColor(Object annotationType, boolean temporary) {
		return getColor(annotationType, temporary ? 0.5 : 0.2);
	}
	
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
	
	private void updateHeader() {
		
		if (fHeader == null || fHeader.isDisposed())
			return;
	
		List indices= new ArrayList(fAnnotationTypes2Layers.keySet());
		Collections.sort(indices);
		
		Object colorType= null;
		outer: for (int i= indices.size() -1; i >= 0; i--) {
			
			Object layer=indices.get(i);
			Object annotationType= fAnnotationTypes2Layers.get(layer);
			
			if (!fHeaderAnnotationTypes.contains(annotationType))
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
	}
}
