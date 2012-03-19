/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
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
import org.eclipse.swt.widgets.ScrollBar;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.projection.AnnotationBag;


/**
 * Ruler presented next to a source viewer showing all annotations of the viewer's annotation model
 * in a compact format. The ruler has the same height as the source viewer.
 * <p>
 * This overview ruler uses non-saturated colors unless {@link #setUseSaturatedColors(boolean)} gets
 * called.
 * </p>
 * <p>
 * Clients usually instantiate and configure objects of this class.
 * </p>
 * 
 * @since 2.1
 */
public class OverviewRuler implements IOverviewRulerExtension, IOverviewRuler {

	/**
	 * Internal listener class.
	 */
	class InternalListener implements ITextListener, IAnnotationModelListener, IAnnotationModelListenerExtension {

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

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationModelListenerExtension#modelChanged(org.eclipse.jface.text.source.AnnotationModelEvent)
		 * @since 3.3
		 */
		public void modelChanged(AnnotationModelEvent event) {
			if (!event.isValid())
				return;

			if (event.isWorldChange()) {
				update();
				return;
			}

			Annotation[] annotations= event.getAddedAnnotations();
			int length= annotations.length;
			for (int i= 0; i < length; i++) {
				if (!skip(annotations[i].getType())) {
					update();
					return;
				}
			}

			annotations= event.getRemovedAnnotations();
			length= annotations.length;
			for (int i= 0; i < length; i++) {
				if (!skip(annotations[i].getType())) {
					update();
					return;
				}
			}

			annotations= event.getChangedAnnotations();
			length= annotations.length;
			for (int i= 0; i < length; i++) {
				if (!skip(annotations[i].getType())) {
					update();
					return;
				}
			}

		}
	}

	/**
	 * Enumerates the annotations of a specified type and characteristics
	 * of the associated annotation model.
	 */
	class FilterIterator implements Iterator {

		final static int TEMPORARY= 1 << 1;
		final static int PERSISTENT= 1 << 2;
		final static int IGNORE_BAGS= 1 << 3;

		private Iterator fIterator;
		private Object fType;
		private Annotation fNext;
		private int fStyle;

		/**
		 * Creates a new filter iterator with the given specification.
		 *
		 * @param annotationType the annotation type
		 * @param style the style
		 */
		public FilterIterator(Object annotationType, int style) {
			fType= annotationType;
			fStyle= style;
			if (fModel != null) {
				fIterator= fModel.getAnnotationIterator();
				skip();
			}
		}

		/**
		 * Creates a new filter iterator with the given specification.
		 *
		 * @param annotationType the annotation type
		 * @param style the style
		 * @param iterator the iterator
		 */
		public FilterIterator(Object annotationType, int style, Iterator iterator) {
			fType= annotationType;
			fStyle= style;
			fIterator= iterator;
			skip();
		}

		private void skip() {

			boolean temp= (fStyle & TEMPORARY) != 0;
			boolean pers= (fStyle & PERSISTENT) != 0;
			boolean ignr= (fStyle & IGNORE_BAGS) != 0;

			while (fIterator.hasNext()) {
				Annotation next= (Annotation) fIterator.next();

				if (next.isMarkedDeleted())
					continue;

				if (ignr && (next instanceof AnnotationBag))
					continue;

				fNext= next;
				Object annotationType= next.getType();
				if (fType == null || fType.equals(annotationType) || !fConfiguredAnnotationTypes.contains(annotationType) && isSubtype(annotationType)) {
					if (temp && pers) return;
					if (pers && next.isPersistent()) return;
					if (temp && !next.isPersistent()) return;
				}
			}
			fNext= null;
		}

		private boolean isSubtype(Object annotationType) {
			if (fAnnotationAccess instanceof  IAnnotationAccessExtension) {
				IAnnotationAccessExtension extension= (IAnnotationAccessExtension) fAnnotationAccess;
				return extension.isSubtype(annotationType, fType);
			}
			return fType.equals(annotationType);
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
				if (fIterator != null)
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

		/**
		 * Creates a new header painter.
		 */
		public HeaderPainter() {
			fSeparatorColor= fHeader.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		}

		/**
		 * Sets the header color.
		 *
		 * @param color the header color
		 */
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
			if (fIndicatorColor == null)
				return;

			Point s= fHeader.getSize();

			e.gc.setBackground(fIndicatorColor);
			
			Rectangle headerBounds= fHeader.getBounds();
			boolean isOnTop= headerBounds.y + headerBounds.height <= fCanvas.getLocation().y;
			boolean isTall= s.y > s.x + 2*ANNOTATION_HEIGHT;
			int y;
			if (!isOnTop) {
				// not on top -> attach to bottom
				y= s.y - 3*ANNOTATION_HEIGHT;
			} else if (isTall) {
				// attach to top
				y= ANNOTATION_HEIGHT;
			} else {
				// center
				y= (s.y - (2*ANNOTATION_HEIGHT)) / 2;
			}
			Rectangle r= new Rectangle(INSET, y, s.x - (2*INSET), 2*ANNOTATION_HEIGHT);
			e.gc.fillRectangle(r);

//			Display d= fHeader.getDisplay();
//			drawBevelRect(e.gc, r.x, r.y, r.width -1, r.height -1, d.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW), d.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
			drawBevelRect(e.gc, r.x, r.y, r.width -1, r.height -1, fSeparatorColor, fSeparatorColor);

			e.gc.setForeground(fSeparatorColor);
			e.gc.setLineWidth(0); // NOTE: 0 means width is 1 but with optimized performance

			if (!isOnTop || !isTall) {
				// only draw separator if at bottom or if gap is small
				e.gc.drawLine(0, s.y -1, s.x -1, s.y -1);
			}
		}
	}

	/**
	 * Container for cached widget infos.
	 * 
	 * @since 3.7
	 */
	static class WidgetInfos {
		/**
		 * the text widget line count
		 */
		int maxLines;
		/**
		 * the height of the vertical scrollbar thumb
		 */
		int thumbHeight;
		/**
		 * the visible lines of the text widget
		 */
		double visibleLines;
		/**
		 * the invisible lines of the text widget
		 */
		double invisibleLines;
		/**
		 * the bounds of {@link OverviewRuler#fCanvas}
		 */
		Rectangle bounds;
		/**
		 * the writable area in the text widget (height of all lines in pixels)
		 */
		int writable;
	
		/**
		 * Initializes the widget infos.
		 * 
		 * @param textWidget the text widget
		 * @param canvas the overview ruler canvas
		 */
		public WidgetInfos(StyledText textWidget, Canvas canvas) {
			maxLines= textWidget.getLineCount();
			bounds= canvas.getBounds();
			writable= JFaceTextUtil.computeLineHeight(textWidget, 0, maxLines, maxLines);
			
			ScrollBar verticalBar= textWidget.getVerticalBar();
			thumbHeight= verticalBar != null ? Math.max(Math.min(bounds.height, verticalBar.getThumbBounds().height), 0) : 0;
			
	        int partialTopIndex= JFaceTextUtil.getPartialTopIndex(textWidget);
	        int topLineHeight= textWidget.getLineHeight(textWidget.getOffsetAtLine(partialTopIndex));
	        int topLinePixel= textWidget.getLinePixel(partialTopIndex);
	        double topIndex= partialTopIndex - (double) topLinePixel / topLineHeight;
	        
	        int partialBottomIndex= JFaceTextUtil.getPartialBottomIndex(textWidget);
	        int bottomLineHeight= textWidget.getLineHeight(textWidget.getOffsetAtLine(partialBottomIndex));
	        int bottomLinePixel= textWidget.getLinePixel(partialBottomIndex);
	        double bottomIndex= partialBottomIndex - ((double) bottomLinePixel - textWidget.getClientArea().height) / bottomLineHeight;
			
	        visibleLines= bottomIndex - topIndex;
	        invisibleLines= maxLines - visibleLines;
		}
	}

	private static final boolean DEBUG_DRAW= false;
	private static final boolean DEBUG_COMPUTE_Y= false;
	private static final boolean DEBUG_TO_DOCUMENT_LINE_NUMBER= false;
	
	/**
	 * <code>true</code> if we're on a Mac, where "new GC(canvas)" is expensive.
	 * @see <a href="https://bugs.eclipse.org/298936">bug 298936</a>
	 * @since 3.6
	 */
	private static final boolean IS_MAC= Util.isMac();

	private static final int INSET= 2;
	private static final int ANNOTATION_HEIGHT= 4;
	private static boolean ANNOTATION_HEIGHT_SCALABLE= true;


	/** The model of the overview ruler */
	private IAnnotationModel fModel;
	/** The view to which this ruler is connected */
	private ITextViewer fTextViewer;
	/** The ruler's canvas */
	private Canvas fCanvas;
	/** The ruler's header */
	private Canvas fHeader;
	/** The buffer for double buffering */
	private Image fBuffer;
	/** The internal listener */
	private InternalListener fInternalListener= new InternalListener();
	/** The width of this vertical ruler */
	private int fWidth;
	/** The hit detection cursor. Do not dispose. */
	private Cursor fHitDetectionCursor;
	/** The last cursor. Do not dispose. */
	private Cursor fLastCursor;
	/** The line of the last mouse button activity */
	private int fLastMouseButtonActivityLine= -1;
	/** The actual annotation height */
	private int fAnnotationHeight= -1;
	/** The annotation access */
	private IAnnotationAccess fAnnotationAccess;
	/** The header painter */
	private HeaderPainter fHeaderPainter;
	/**
	 * The list of annotation types to be shown in this ruler.
	 * @since 3.0
	 */
	private Set fConfiguredAnnotationTypes= new HashSet();
	/**
	 * The list of annotation types to be shown in the header of this ruler.
	 * @since 3.0
	 */
	private Set fConfiguredHeaderAnnotationTypes= new HashSet();
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
	 * @since 3.0
	 */
	private List fLayersSortedByLayer= new ArrayList();
	/**
	 * Map of allowed annotation types.
	 * An allowed annotation type maps to <code>true</code>, a disallowed
	 * to <code>false</code>.
	 * @since 3.0
	 */
	private Map fAllowedAnnotationTypes= new HashMap();
	/**
	 * Map of allowed header annotation types.
	 * An allowed annotation type maps to <code>true</code>, a disallowed
	 * to <code>false</code>.
	 * @since 3.0
	 */
	private Map fAllowedHeaderAnnotationTypes= new HashMap();
	/**
	 * The cached annotations.
	 * @since 3.0
	 */
	private List fCachedAnnotations= new ArrayList();

	/**
	 * Redraw runnable lock
	 * @since 3.3
	 */
	private Object fRunnableLock= new Object();
	/**
	 * Redraw runnable state
	 * @since 3.3
	 */
	private boolean fIsRunnablePosted= false;
	/**
	 * Redraw runnable
	 * @since 3.3
	 */
	private Runnable fRunnable= new Runnable() {
		public void run() {
			synchronized (fRunnableLock) {
				fIsRunnablePosted= false;
			}
			redraw();
			updateHeader();
		}
	};
	/**
	 * Tells whether temporary annotations are drawn with
	 * a separate color. This color will be computed by
	 * discoloring the original annotation color.
	 *
	 * @since 3.4
	 */
	private boolean fIsTemporaryAnnotationDiscolored;
	
	/**
	 * Tells whether saturated colors are used in the overview ruler.
	 * 
	 * @since 3.8
	 */
	private boolean fUseSaturatedColors= false;


	/**
	 * Constructs a overview ruler of the given width using the given annotation access and the given
	 * color manager.
	 * <p><strong>Note:</strong> As of 3.4, temporary annotations are no longer discolored.
	 * Use {@link #OverviewRuler(IAnnotationAccess, int, ISharedTextColors, boolean)} if you
	 * want to keep the old behavior.</p>
	 *
	 * @param annotationAccess the annotation access
	 * @param width the width of the vertical ruler
	 * @param sharedColors the color manager
	 */
	public OverviewRuler(IAnnotationAccess annotationAccess, int width, ISharedTextColors sharedColors) {
		this(annotationAccess, width, sharedColors, false);
	}

	/**
	 * Constructs a overview ruler of the given width using the given annotation
	 * access and the given color manager.
	 *
	 * @param annotationAccess the annotation access
	 * @param width the width of the vertical ruler
	 * @param sharedColors the color manager
	 * @param discolorTemporaryAnnotation <code>true</code> if temporary annotations should be discolored
	 * @since 3.4
	 */
	public OverviewRuler(IAnnotationAccess annotationAccess, int width, ISharedTextColors sharedColors, boolean discolorTemporaryAnnotation) {
		fAnnotationAccess= annotationAccess;
		fWidth= width;
		fSharedTextColors= sharedColors;
		fIsTemporaryAnnotationDiscolored= discolorTemporaryAnnotation;
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

		fHitDetectionCursor= parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND);

		fHeader= new Canvas(parent, SWT.NONE);

		if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
			fHeader.addMouseTrackListener(new MouseTrackAdapter() {
				/*
				 * @see org.eclipse.swt.events.MouseTrackAdapter#mouseHover(org.eclipse.swt.events.MouseEvent)
				 * @since 3.3
				 */
				public void mouseEnter(MouseEvent e) {
					updateHeaderToolTipText();
				}
			});
		}

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

		fConfiguredAnnotationTypes.clear();
		fAllowedAnnotationTypes.clear();
		fConfiguredHeaderAnnotationTypes.clear();
		fAllowedHeaderAnnotationTypes.clear();
		fAnnotationTypes2Colors.clear();
		fAnnotationsSortedByLayer.clear();
		fLayersSortedByLayer.clear();
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
		try {
			gc.setBackground(fCanvas.getBackground());
			gc.fillRectangle(0, 0, size.x, size.y);

			cacheAnnotations();

			doPaint(gc);

		} finally {
			gc.dispose();
		}

		dest.drawImage(fBuffer, 0, 0);
	}

	private void cacheAnnotations() {
		fCachedAnnotations.clear();
		if (fModel != null) {
			Iterator iter= fModel.getAnnotationIterator();
			while (iter.hasNext()) {
				Annotation annotation= (Annotation) iter.next();

				if (annotation.isMarkedDeleted())
					continue;

				if (skip(annotation.getType()))
					continue;

				fCachedAnnotations.add(annotation);
			}
		}
	}

	/**
	 * Draws this overview ruler.
	 *
	 * @param gc the GC to draw into
	 */
	private void doPaint(GC gc) {

		Rectangle r= new Rectangle(0, 0, 0, 0);
		int yy, hh= ANNOTATION_HEIGHT;

		IDocument document= fTextViewer.getDocument();
		StyledText textWidget= fTextViewer.getTextWidget();
		ITextViewerExtension5 extension= null;
		IRegion visible= null;
		if (fTextViewer instanceof ITextViewerExtension5)
			extension= (ITextViewerExtension5) fTextViewer;
		else
			visible= fTextViewer.getVisibleRegion(); // legacy support

		WidgetInfos infos= null;
		
		for (Iterator iterator= fAnnotationsSortedByLayer.iterator(); iterator.hasNext();) {
			Object annotationType= iterator.next();

			if (skip(annotationType))
				continue;

			int[] style= new int[] { FilterIterator.PERSISTENT, FilterIterator.TEMPORARY };
			for (int t=0; t < style.length; t++) {
				boolean areColorsComputed= false;
				Color fill= null;
				Color stroke= null;

				Iterator e= new FilterIterator(annotationType, style[t], fCachedAnnotations.iterator());
				while (e.hasNext()) {
					Annotation a= (Annotation) e.next();
					Position p= fModel.getPosition(a);

					if (p == null)
						continue;
					if (visible != null && !p.overlapsWith(visible.getOffset(), visible.getLength()))
						continue;

					int annotationOffset= p.getOffset();
					int annotationLength= p.getLength();
					IRegion widgetRegion= null;
					if (visible != null) {
						annotationOffset= Math.max(p.getOffset(), visible.getOffset());
						int annotationEnd= Math.min(p.getOffset() + p.getLength(), visible.getOffset() + visible.getLength());
						annotationLength= annotationEnd - annotationOffset;
					} else {
						widgetRegion= extension.modelRange2WidgetRange(new Region(annotationOffset, annotationLength));
						if (widgetRegion == null)
							continue;
					}
					
					if (infos == null) {
						infos= new WidgetInfos(textWidget, fCanvas);
						r.x= INSET;
						r.width= infos.bounds.width - (2 * INSET);
					}

					try {
						int startOffset= visible != null ? annotationOffset - visible.getOffset() : widgetRegion.getOffset();
						int startLine= textWidget.getLineAtOffset(startOffset);
						
						yy= computeY(startLine, infos);
						
						if (ANNOTATION_HEIGHT_SCALABLE) {
							int numberOfLines= document.getNumberOfLines(annotationOffset, annotationLength);
							// don't count empty trailing line
							IRegion lastLine= document.getLineInformationOfOffset(annotationOffset + annotationLength);
							if (lastLine.getOffset() == annotationOffset + annotationLength) {
								numberOfLines--;
							}
							if (numberOfLines > 1) {
								int yy2= computeY(startLine + numberOfLines - 1, infos);
								hh= Math.max(yy2 - yy, ANNOTATION_HEIGHT);
							} else {
								hh= ANNOTATION_HEIGHT;
							}
						}
						fAnnotationHeight= hh;
						
						if (!areColorsComputed) {
							stroke= getStrokeColor(annotationType, style[t] == FilterIterator.TEMPORARY);
							fill= fUseSaturatedColors ? stroke : getFillColor(annotationType, style[t] == FilterIterator.TEMPORARY);
							areColorsComputed= true;
						}

						if (fill != null) {
							gc.setBackground(fill);
							gc.fillRectangle(INSET, yy, infos.bounds.width-(2*INSET), hh);
						}

						if (stroke != null) {
							gc.setForeground(stroke);
							r.y= yy;
							if (yy + hh == infos.bounds.height)
								r.y--;
							r.height= hh;
							gc.setLineWidth(0); // NOTE: 0 means width is 1 but with optimized performance
							gc.drawRectangle(r);
						}
					} catch (BadLocationException x) {
					}
				}
			}
		}
		
		if (DEBUG_DRAW) {
			// draw debugging guides (boundaries):
			if (infos == null)
				infos= new WidgetInfos(textWidget, fCanvas);
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_MAGENTA));
			yy= infos.thumbHeight / 2;
			gc.drawLine(0, yy, infos.bounds.x/2, yy);
			yy= infos.bounds.height - infos.thumbHeight / 2;
			gc.drawLine(0, yy, infos.bounds.x/2, yy);
			
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLUE));
			yy= 0;
			gc.drawLine(0, yy, infos.bounds.x/2, yy);
			yy= infos.bounds.height - 1;
			gc.drawLine(0, yy, infos.bounds.x/2, yy);
		}
	}

	/**
	 * Computes and returns the y location of the given startLine.
	 * 
	 * @param startLine the start line
	 * @param infos the cached widget infos
	 * @return the vertical position of the given startLine in the overview ruler
	 * @since 3.7
	 */
	private int computeY(int startLine, WidgetInfos infos) {
		// this is the inverse of #toLineNumbers(int)
		
		int yy;
		if (infos.bounds.height > infos.writable || infos.invisibleLines <= 0) { // too few lines for relative positions: align annotations with textWidget lines
			yy = Math.max(0, (2 * startLine + 1) * infos.writable / (infos.maxLines * 2) - infos.bounds.y);
			if (DEBUG_COMPUTE_Y)
				System.out.println("static: " + yy); //$NON-NLS-1$
			
		} else if (startLine + 1 < infos.visibleLines / 2) { // before middle of first page: map to area from 0 to thumbHeight/2
			yy= (int) (startLine * infos.thumbHeight / infos.visibleLines); // == startLine * (thumbHeight / 2) / (visibleLines / 2);
			if (DEBUG_COMPUTE_Y)
				System.out.println("start:  " + yy); //$NON-NLS-1$
			
		} else if (infos.maxLines - infos.visibleLines / 2 <= startLine) { // after middle of last page: map to area from canvasHeight-1 - thumbHeight/2 to canvasHeight-1
			yy= (int) (infos.bounds.height-1 - (double)infos.thumbHeight / 2 + (startLine - (infos.maxLines - infos.visibleLines / 2) + 1) * infos.thumbHeight / infos.visibleLines);
			if (DEBUG_COMPUTE_Y)
				System.out.println("end:    " + yy); //$NON-NLS-1$
			
		} else { // middle of text: map to area from thumbHeight/2 to (canvasHeight-1 - thumbHeight/2)
			yy= (int) ((double)infos.thumbHeight/2 + (startLine + 1 - infos.visibleLines / 2) * (infos.bounds.height-1 - infos.thumbHeight) / infos.invisibleLines);
			if (DEBUG_COMPUTE_Y)
				System.out.println("middle: " + yy); //$NON-NLS-1$
		}
		// center of rectangle should be at the calculated position:
		yy-= ANNOTATION_HEIGHT / 2;
		// cap at start/end:
		yy= Math.max(0, Math.min(yy, infos.bounds.height-1 - ANNOTATION_HEIGHT));
		return yy;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRuler#update()
	 */
	 public void update() {
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

	/**
	 * Redraws the overview ruler.
	 */
	private void redraw() {
		if (fTextViewer == null || fModel == null)
			return;

		if (fCanvas != null && !fCanvas.isDisposed()) {
			if (IS_MAC) {
				fCanvas.redraw();
				fCanvas.update();
			} else {
				GC gc= new GC(fCanvas);
				doubleBufferPaint(gc);
				gc.dispose();
			}
		}
	}

	/**
	 * Translates a given y-coordinate of this ruler into the corresponding
	 * document lines. The number of lines depends on the concrete scaling
	 * given as the ration between the height of this ruler and the length
	 * of the document.
	 *
	 * @param y_coordinate the y-coordinate
	 * @param annotationRect <code>true</code> to only consider the position of a drawn annotation rectangle,
	 * 	<code>false</code> to consider the whole line
	 * @return the corresponding document lines as {firstLine, lastLine}, or {-1, -1} if no lines correspond to the y-coordinate
	 */
	private int[] toLineNumbers(int y_coordinate, boolean annotationRect) {
		// this is the inverse of #computeY(int, WidgetInfos)
		
		WidgetInfos infos= new WidgetInfos(fTextViewer.getTextWidget(), fCanvas);
		
		if (y_coordinate >= infos.writable || y_coordinate >= infos.bounds.height || y_coordinate < 0)
			return new int[] {-1, -1};
		
		if (annotationRect && ANNOTATION_HEIGHT >= infos.bounds.height / infos.maxLines)
			annotationRect= false;
		
		int[] lines= new int[2];
		int[] pixels;

		int pixelEnd= Math.min(infos.bounds.height, y_coordinate + ANNOTATION_HEIGHT / 2 + 1);
		if (annotationRect) {
			pixels= new int[] { pixelEnd };
		} else {
			int pixelStart= Math.max(y_coordinate - ANNOTATION_HEIGHT / 2 + 1, 0);
			pixels= new int[] { pixelStart, pixelEnd };
		}
		
		if (infos.bounds.height > infos.writable || infos.invisibleLines <= 0) { // too few lines for relative positions: align annotations with textWidget lines
//			yy = Math.max(0, (2 * startLine + 1) * infos.writable / (infos.maxLines * 2) - infos.bounds.y);
			for (int i= 0; i < pixels.length; i++)
				lines[i]= (int) ((pixels[i] + infos.bounds.y) * infos.maxLines / (double)infos.writable);
			if (DEBUG_TO_DOCUMENT_LINE_NUMBER)
				System.out.println("static y: " + y_coordinate + " => [" + lines[0] + ", " + lines[1] + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			
		} else if (y_coordinate < infos.thumbHeight / 2) { // before middle of first page: map to area from 0 to thumbHeight/2
//			yy= (int) (startLine * infos.thumbHeight / infos.visibleLines);
			for (int i= 0; i < pixels.length; i++)
				lines[i] = (int) (pixels[i] * infos.visibleLines / infos.thumbHeight);
			if (DEBUG_TO_DOCUMENT_LINE_NUMBER)
				System.out.println("start  y: " + y_coordinate + " => [" + lines[0] + ", " + lines[1] + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			
		} else if (infos.bounds.height - 1 - infos.thumbHeight / 2 < y_coordinate) { // after middle of last page: map to area from canvasHeight-1 - thumbHeight/2 to canvasHeight-1
//			yy= (int) (infos.bounds.height-1 - (double)infos.thumbHeight / 2 + (startLine - (infos.maxLines - infos.visibleLines / 2) + 1) * infos.thumbHeight / infos.visibleLines);
			for (int i= 0; i < pixels.length; i++)
				lines[i] = (int) ((pixels[i] - (infos.bounds.height-1) + (double)infos.thumbHeight / 2) * infos.visibleLines / infos.thumbHeight - 1 + (infos.maxLines - infos.visibleLines / 2));
			if (DEBUG_TO_DOCUMENT_LINE_NUMBER)
				System.out.println("end    y: " + y_coordinate + " => [" + lines[0] + ", " + lines[1] + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			
		} else { // middle of text: map to area from thumbHeight/2 to (canvasHeight-1 - thumbHeight/2)
//			yy= (int) ((double)infos.thumbHeight/2 + (startLine + 1 - infos.visibleLines / 2) * (infos.bounds.height-1 - infos.thumbHeight) / infos.invisibleLines);
			for (int i= 0; i < pixels.length; i++)
				lines[i]= (int) ((pixels[i] - (double)infos.thumbHeight/2) * infos.invisibleLines / (infos.bounds.height-1 - infos.thumbHeight) - 1 + infos.visibleLines / 2);
			if (DEBUG_TO_DOCUMENT_LINE_NUMBER)
				System.out.println("middle y: " + y_coordinate + " => [" + lines[0] + ", " + lines[1] + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		
		if (y_coordinate < ANNOTATION_HEIGHT && y_coordinate < infos.bounds.y)
			lines[0]= 0;
		else if (lines[0] < 0)
			lines[0]= 0;
		
		if (annotationRect) {
			int y0= computeY(lines[0], infos);
			if (y_coordinate < y0 || y0 + ANNOTATION_HEIGHT < y_coordinate) {
				lines[0]= -1;
				lines[1]= -1;
				return lines;
			} else {
				lines[1]= lines[0];
			}
		}
		
		if (lines[1] > infos.maxLines)
			lines[1]= infos.maxLines;
		
		if (fTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fTextViewer;
			lines[0]= extension.widgetLine2ModelLine(lines[0]);
			lines[1]= extension.widgetLine2ModelLine(lines[1]);
		} else {
			try {
				IRegion visible= fTextViewer.getVisibleRegion();
				int lineNumber= fTextViewer.getDocument().getLineOfOffset(visible.getOffset());
				lines[0] += lineNumber;
				lines[1] += lineNumber;
			} catch (BadLocationException x) {
			}
		}
		
		if (DEBUG_TO_DOCUMENT_LINE_NUMBER)
			System.out.println("result: [" + lines[0] + ", " + lines[1] + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

				Iterator e= new FilterIterator(annotationType, FilterIterator.PERSISTENT | FilterIterator.TEMPORARY);
				while (e.hasNext() && found == null) {
					Annotation a= (Annotation) e.next();
					if (a.isMarkedDeleted())
						continue;

					if (skip(a.getType()))
						continue;

					Position p= fModel.getPosition(a);
					if (p == null)
						continue;

					int posOffset= p.getOffset();
					int posEnd= posOffset + p.getLength();
					IRegion region= d.getLineInformationOfOffset(posEnd);
					// trailing empty lines don't count
					if (posEnd > posOffset && region.getOffset() == posEnd) {
						posEnd--;
						region= d.getLineInformationOfOffset(posEnd);
					}

					if (posOffset <= end && posEnd >= start)
							found= p;
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
	 * @param lineNumbers the line numbers
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
			int[] lines= toLineNumbers(event.y, true);
			if (lines[0] == -1)
				lines= toLineNumbers(event.y, false);
			Position p= getAnnotationPosition(lines);
			if (p == null && event.button == 1) {
				try {
					p= new Position(fTextViewer.getDocument().getLineInformation(lines[0]).getOffset(), 0);
				} catch (BadLocationException e) {
					// do nothing
				}
			}
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
			int[] lines= toLineNumbers(event.y, true);
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
		fConfiguredAnnotationTypes.add(annotationType);
		fAllowedAnnotationTypes.clear();
	}

	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#removeAnnotationType(java.lang.Object)
	 */
	public void removeAnnotationType(Object annotationType) {
		fConfiguredAnnotationTypes.remove(annotationType);
		fAllowedAnnotationTypes.clear();
	}

	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#setAnnotationTypeLayer(java.lang.Object, int)
	 */
	public void setAnnotationTypeLayer(Object annotationType, int layer) {
		int j= fAnnotationsSortedByLayer.indexOf(annotationType);
		if (j != -1) {
			fAnnotationsSortedByLayer.remove(j);
			fLayersSortedByLayer.remove(j);
		}

		if (layer >= 0) {
			int i= 0;
			int size= fLayersSortedByLayer.size();
			while (i < size && layer >= ((Integer)fLayersSortedByLayer.get(i)).intValue())
				i++;
			Integer layerObj= new Integer(layer);
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
	 * Returns whether the given annotation type should be skipped by the drawing routine.
	 *
	 * @param annotationType the annotation type
	 * @return <code>true</code> if annotation of the given type should be skipped
	 */
	private boolean skip(Object annotationType) {
		return !contains(annotationType, fAllowedAnnotationTypes, fConfiguredAnnotationTypes);
	}

	/**
	 * Returns whether the given annotation type should be skipped by the drawing routine of the header.
	 *
	 * @param annotationType the annotation type
	 * @return <code>true</code> if annotation of the given type should be skipped
	 * @since 3.0
	 */
	private boolean skipInHeader(Object annotationType) {
		return !contains(annotationType, fAllowedHeaderAnnotationTypes, fConfiguredHeaderAnnotationTypes);
	}

	/**
	 * Returns whether the given annotation type is mapped to <code>true</code>
	 * in the given <code>allowed</code> map or covered by the <code>configured</code>
	 * set.
	 *
	 * @param annotationType the annotation type
	 * @param allowed the map with allowed annotation types mapped to booleans
	 * @param configured the set with configured annotation types
	 * @return <code>true</code> if annotation is contained, <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	private boolean contains(Object annotationType, Map allowed, Set configured) {
		Boolean cached= (Boolean) allowed.get(annotationType);
		if (cached != null)
			return cached.booleanValue();

		boolean covered= isCovered(annotationType, configured);
		allowed.put(annotationType, covered ? Boolean.TRUE : Boolean.FALSE);
		return covered;
	}

	/**
	 * Computes whether the annotations of the given type are covered by the given <code>configured</code>
	 * set. This is the case if either the type of the annotation or any of its
	 * super types is contained in the <code>configured</code> set.
	 *
	 * @param annotationType the annotation type
	 * @param configured the set with configured annotation types
	 * @return <code>true</code> if annotation is covered, <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	private boolean isCovered(Object annotationType, Set configured) {
		if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension extension= (IAnnotationAccessExtension) fAnnotationAccess;
			Iterator e= configured.iterator();
			while (e.hasNext()) {
				if (extension.isSubtype(annotationType,e.next()))
					return true;
			}
			return false;
		}
		return configured.contains(annotationType);
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
		Color base= findColor(annotationType);
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
	 * Returns the color for the given annotation type
	 *
	 * @param annotationType the annotation type
	 * @return the color
	 * @since 3.0
	 */
	private Color findColor(Object annotationType) {
		Color color= (Color) fAnnotationTypes2Colors.get(annotationType);
		if (color != null)
			return color;

		if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension extension= (IAnnotationAccessExtension) fAnnotationAccess;
			Object[] superTypes= extension.getSupertypes(annotationType);
			if (superTypes != null) {
				for (int i= 0; i < superTypes.length; i++) {
					color= (Color) fAnnotationTypes2Colors.get(superTypes[i]);
					if (color != null)
						return color;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the stroke color for the given annotation type and characteristics.
	 *
	 * @param annotationType the annotation type
	 * @param temporary <code>true</code> if for temporary annotations
	 * @return the stroke color
	 */
	private Color getStrokeColor(Object annotationType, boolean temporary) {
		return getColor(annotationType, temporary && fIsTemporaryAnnotationDiscolored ? 0.5 : 0.2);
	}

	/**
	 * Returns the fill color for the given annotation type and characteristics.
	 *
	 * @param annotationType the annotation type
	 * @param temporary <code>true</code> if for temporary annotations
	 * @return the fill color
	 */
	private Color getFillColor(Object annotationType, boolean temporary) {
		return getColor(annotationType, temporary && fIsTemporaryAnnotationDiscolored ? 0.9 : 0.75);
	}

	/*
	 * @see IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	public int getLineOfLastMouseButtonActivity() {
		if (fLastMouseButtonActivityLine >= fTextViewer.getDocument().getNumberOfLines())
			fLastMouseButtonActivityLine= -1;
		return fLastMouseButtonActivityLine;
	}

	/*
	 * @see IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	public int toDocumentLineNumber(int y_coordinate) {

		if (fTextViewer == null || y_coordinate == -1)
			return -1;

		int[] lineNumbers= toLineNumbers(y_coordinate, true);
		if (lineNumbers[0] == -1)
			lineNumbers= toLineNumbers(y_coordinate, false);
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
		return findBestMatchingLineNumber(toLineNumbers(y, true)) != -1;
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
		fConfiguredHeaderAnnotationTypes.add(annotationType);
		fAllowedHeaderAnnotationTypes.clear();
	}

	/*
	 * @see org.eclipse.jface.text.source.IOverviewRuler#removeHeaderAnnotationType(java.lang.Object)
	 */
	public void removeHeaderAnnotationType(Object annotationType) {
		fConfiguredHeaderAnnotationTypes.remove(annotationType);
		fAllowedHeaderAnnotationTypes.clear();
	}

	/**
	 * Updates the header of this ruler.
	 */
	private void updateHeader() {
		if (fHeader == null || fHeader.isDisposed())
			return;

		fHeader.setToolTipText(null);

		Object colorType= null;
		outer: for (int i= fAnnotationsSortedByLayer.size() -1; i >= 0; i--) {
			Object annotationType= fAnnotationsSortedByLayer.get(i);
			if (skipInHeader(annotationType) || skip(annotationType))
				continue;

			Iterator e= new FilterIterator(annotationType, FilterIterator.PERSISTENT | FilterIterator.TEMPORARY | FilterIterator.IGNORE_BAGS, fCachedAnnotations.iterator());
			while (e.hasNext()) {
				if (e.next() != null) {
					colorType= annotationType;
					break outer;
				}
			}
		}

		Color color= null;
		if (colorType != null)
			color= findColor(colorType);

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

	/**
	 * Updates the header tool tip text of this ruler.
	 */
	private void updateHeaderToolTipText() {
		if (fHeader == null || fHeader.isDisposed())
			return;

		if (fHeader.getToolTipText() != null)
			return;

		String overview= ""; //$NON-NLS-1$

		for (int i= fAnnotationsSortedByLayer.size() -1; i >= 0; i--) {

			Object annotationType= fAnnotationsSortedByLayer.get(i);

			if (skipInHeader(annotationType) || skip(annotationType))
				continue;

			int count= 0;
			String annotationTypeLabel= null;

			Iterator e= new FilterIterator(annotationType, FilterIterator.PERSISTENT | FilterIterator.TEMPORARY | FilterIterator.IGNORE_BAGS, fCachedAnnotations.iterator());
			while (e.hasNext()) {
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

	/**
	 * {@inheritDoc}
	 * 
	 * @since 3.8
	 */
	public void setUseSaturatedColors(boolean useSaturatedColor) {
		fUseSaturatedColors= useSaturatedColor;
	}
}
