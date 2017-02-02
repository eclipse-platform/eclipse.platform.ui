/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nikolay Botev <bono8106@hotmail.com> - [projection] Editor loses keyboard focus when expanding folded region - https://bugs.eclipse.org/bugs/show_bug.cgi?id=184255
 *******************************************************************************/
package org.eclipse.jface.text.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
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
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextEvent;


/**
 * A vertical ruler column showing graphical representations of annotations.
 * Will become final.
 * <p>
 * Do not subclass.
 * </p>
 *
 * @since 2.0
 */
public class AnnotationRulerColumn implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {

	/**
	 * Internal listener class.
	 */
	class InternalListener implements IViewportListener, IAnnotationModelListener, ITextListener {

		@Override
		public void viewportChanged(int verticalPosition) {
			if (verticalPosition != fScrollPos)
				redraw();
		}

		@Override
		public void modelChanged(IAnnotationModel model) {
			postRedraw();
		}

		@Override
		public void textChanged(TextEvent e) {
			if (e.getViewerRedrawState())
				postRedraw();
		}
	}

	/**
	 * Implementation of <code>IRegion</code> that can be reused
	 * by setting the offset and the length.
	 */
	private static class ReusableRegion extends Position implements IRegion {}

	/**
	 * Pair of an annotation and their associated position. Used inside the paint method
	 * for sorting annotations based on the offset of their position.
	 * @since 3.0
	 */
	private static class Tuple {
		Annotation annotation;
		Position position;

		Tuple(Annotation annotation, Position position) {
			this.annotation= annotation;
			this.position= position;
		}
	}

	/**
	 * Comparator for <code>Tuple</code>s.
	 * @since 3.0
	 */
	private static class TupleComparator implements Comparator<Tuple> {
		@Override
		public int compare(Tuple o1, Tuple o2) {
			Position p1= o1.position;
			Position p2= o2.position;
			return p1.getOffset() - p2.getOffset();
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
	/** The buffer for double buffering */
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
	private Set<Object> fConfiguredAnnotationTypes= new HashSet<>();
	/**
	 * The list of allowed annotation types to be shown in this ruler.
	 * An allowed annotation type maps to <code>true</code>, a disallowed
	 * to <code>false</code>.
	 * @since 3.0
	 */
	private Map<Object, Boolean> fAllowedAnnotationTypes= new HashMap<>();
	/**
	 * The annotation access extension.
	 * @since 3.0
	 */
	private IAnnotationAccessExtension fAnnotationAccessExtension;
	/**
	 * The hover for this column.
	 * @since 3.0
	 */
	private IAnnotationHover fHover;
	/**
	 * The cached annotations.
	 * @since 3.0
	 */
	private List<Tuple> fCachedAnnotations= new ArrayList<>();
	/**
	 * The comparator for sorting annotations according to the offset of their position.
	 * @since 3.0
	 */
	private Comparator<Tuple> fTupleComparator= new TupleComparator();
	/**
	 * The hit detection cursor. Do not dispose.
	 * @since 3.0
	 */
	private Cursor fHitDetectionCursor;
	/**
	 * The last cursor. Do not dispose.
	 * @since 3.0
	 */
	private Cursor fLastCursor;
	/**
	 * This ruler's mouse listener.
	 * @since 3.0
	 */
	private MouseListener fMouseListener;

	/**
	 * Constructs this column with the given arguments.
	 *
	 * @param model the annotation model to get the annotations from
	 * @param width the width of the vertical ruler
	 * @param annotationAccess the annotation access
	 * @since 3.0
	 */
	public AnnotationRulerColumn(IAnnotationModel model, int width, IAnnotationAccess annotationAccess) {
		this(width, annotationAccess);
		fAllowSetModel= false;
		fModel= model;
		fModel.addAnnotationModelListener(fInternalListener);
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
		if (annotationAccess instanceof IAnnotationAccessExtension)
			fAnnotationAccessExtension= (IAnnotationAccessExtension) annotationAccess;
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

	@Override
	public Control getControl() {
		return fCanvas;
	}

	@Override
	public int getWidth() {
		return fWidth;
	}

	@Override
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {

		fParentRuler= parentRuler;
		fCachedTextViewer= parentRuler.getTextViewer();
		fCachedTextWidget= fCachedTextViewer.getTextWidget();

		fHitDetectionCursor= parentControl.getDisplay().getSystemCursor(SWT.CURSOR_HAND);

		fCanvas= createCanvas(parentControl);

		fCanvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent event) {
				if (fCachedTextViewer != null)
					doubleBufferPaint(event.gc);
			}
		});

		fCanvas.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				handleDispose();
				fCachedTextViewer= null;
				fCachedTextWidget= null;
			}
		});

		fMouseListener= new MouseListener() {
			@Override
			public void mouseUp(MouseEvent event) {
				int lineNumber;
				if (isPropagatingMouseListener()) {
					fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
					lineNumber= fParentRuler.getLineOfLastMouseButtonActivity();
				} else
					lineNumber= fParentRuler.toDocumentLineNumber(event.y);

				if (1 == event.button)
					mouseClicked(lineNumber);
			}

			@Override
			public void mouseDown(MouseEvent event) {
				int lineNumber;
				if (isPropagatingMouseListener()) {
					fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
					lineNumber= fParentRuler.getLineOfLastMouseButtonActivity();
				} else
					lineNumber= fParentRuler.toDocumentLineNumber(event.y);

				if (1 == event.button)
					AnnotationRulerColumn.this.mouseDown(lineNumber);
			}

			@Override
			public void mouseDoubleClick(MouseEvent event) {
				int lineNumber;
				if (isPropagatingMouseListener()) {
					fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
					lineNumber= fParentRuler.getLineOfLastMouseButtonActivity();
				} else
					lineNumber= fParentRuler.toDocumentLineNumber(event.y);

				if (1 == event.button)
					mouseDoubleClicked(lineNumber);
			}
		};
		fCanvas.addMouseListener(fMouseListener);

		fCanvas.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				handleMouseMove(e);
			}
		});

		fCanvas.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent e) {
				handleMouseScrolled(e);
			}
		});

		if (fCachedTextViewer != null) {
			fCachedTextViewer.addViewportListener(fInternalListener);
			fCachedTextViewer.addTextListener(fInternalListener);
			// on word wrap toggle a "resized" ControlEvent is fired: suggest a redraw of the ruler
			fCachedTextWidget.addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					if (fCachedTextWidget != null && fCachedTextWidget.getWordWrap()) {
						redraw();
					}
				}
			});
		}

		return fCanvas;
	}

	/**
	 * Creates a canvas with the given parent.
	 *
	 * @param parent the parent
	 * @return the created canvas
	 */
	private Canvas createCanvas(Composite parent) {
		return new Canvas(parent, SWT.NO_BACKGROUND | SWT.NO_FOCUS) {
			@Override
			public void addMouseListener(MouseListener listener) {
				if (isPropagatingMouseListener() || listener == fMouseListener)
					super.addMouseListener(listener);
			}
		};
	}

	/**
	 * Tells whether this ruler column propagates mouse listener
	 * events to its parent.
	 *
	 * @return <code>true</code> if propagating to parent
	 * @since 3.0
	 */
	protected boolean isPropagatingMouseListener() {
		return true;
	}

	/**
	 * Hook method for a mouse down event on the given ruler line.
	 *
	 * @param rulerLine the ruler line
	 * @since 3.5
	 */
	protected void mouseDown(int rulerLine) {
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
	 * <p>
	 * <strong>Note:</strong> The event is sent on mouse up.
	 * </p>
	 *
	 * @param rulerLine the ruler line
	 * @since 3.0
	 */
	protected void mouseClicked(int rulerLine) {
	}

	/**
	 * Handles mouse moves.
	 *
	 * @param event the mouse move event
	 */
	private void handleMouseMove(MouseEvent event) {
		fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
		if (fCachedTextViewer != null) {
			int line= toDocumentLineNumber(event.y);
			Cursor cursor= (hasAnnotation(line) ? fHitDetectionCursor : null);
			if (cursor != fLastCursor) {
				fCanvas.setCursor(cursor);
				fLastCursor= cursor;
			}
		}
	}

	/**
	 * Handles mouse scrolls.
	 *
	 * @param event the mouse scrolled event
	 */
	private void handleMouseScrolled(MouseEvent event) {
		if (fCachedTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fCachedTextViewer;
			StyledText textWidget= fCachedTextViewer.getTextWidget();
			int topIndex= textWidget.getTopIndex();
			int newTopIndex= Math.max(0, topIndex - event.count);
			fCachedTextViewer.setTopIndex(extension.widgetLine2ModelLine(newTopIndex));
		} else if (fCachedTextViewer != null) {
			int topIndex= fCachedTextViewer.getTopIndex();
			int newTopIndex= Math.max(0, topIndex - event.count);
			fCachedTextViewer.setTopIndex(newTopIndex);
		}
	}

	/**
	 * Tells whether the given line contains an annotation.
	 *
	 * @param lineNumber the line number
	 * @return <code>true</code> if the given line contains an annotation
	 */
	protected boolean hasAnnotation(int lineNumber) {

		IAnnotationModel model= fModel;
		if (fModel instanceof IAnnotationModelExtension)
			model= ((IAnnotationModelExtension)fModel).getAnnotationModel(SourceViewer.MODEL_ANNOTATION_MODEL);

		if (model == null)
			return false;

		IRegion line;
		try {
			IDocument d= fCachedTextViewer.getDocument();
			if (d == null)
				return false;

			line= d.getLineInformation(lineNumber);
		}  catch (BadLocationException ex) {
			return false;
		}

		int lineStart= line.getOffset();
		int lineLength= line.getLength();

		Iterator<Annotation> e;
		if (fModel instanceof IAnnotationModelExtension2)
			e= ((IAnnotationModelExtension2)fModel).getAnnotationIterator(lineStart, lineLength + 1, true, true);
		else
			e= model.getAnnotationIterator();

		while (e.hasNext()) {
			Annotation a= e.next();

			if (a.isMarkedDeleted())
				continue;

			if (skip(a))
				continue;

			Position p= model.getPosition(a);
			if (p == null || p.isDeleted())
				continue;

			if (p.overlapsWith(lineStart, lineLength) || p.length == 0 && p.offset == lineStart + lineLength)
				return true;
		}

		return false;
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
		fAnnotationAccessExtension= null;
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
	 * view port, possibly including partially visible lines.
	 *
	 * @return document offset of the upper left corner including partially visible lines
	 */
	protected int getInclusiveTopIndexStartOffset() {
		if (fCachedTextWidget == null || fCachedTextWidget.isDisposed())
			return -1;

		IDocument document= fCachedTextViewer.getDocument();
		if (document == null)
			return -1;

		int top= JFaceTextUtil.getPartialTopIndex(fCachedTextViewer);
		try {
			return document.getLineOffset(top);
		} catch (BadLocationException x) {
			return -1;
		}
	}

	/**
	 * Returns the first invisible document offset of the lower right corner of the source viewer's view port,
	 * possibly including partially visible lines.
	 *
	 * @return the first invisible document offset of the lower right corner of the view port
	 */
	private int getExclusiveBottomIndexEndOffset() {
		if (fCachedTextWidget == null || fCachedTextWidget.isDisposed())
			return -1;

		IDocument document= fCachedTextViewer.getDocument();
		if (document == null)
			return -1;

		int bottom= JFaceTextUtil.getPartialBottomIndex(fCachedTextViewer);
		try {
			if (bottom >= document.getNumberOfLines())
				bottom= document.getNumberOfLines() - 1;
			return document.getLineOffset(bottom) + document.getLineLength(bottom);
		} catch (BadLocationException x) {
			return -1;
		}
	}

	/**
	 * Draws the vertical ruler w/o drawing the Canvas background.
	 *
	 * @param gc the GC to draw into
	 */
	protected void doPaint(GC gc) {

		if (fModel == null || fCachedTextViewer == null)
			return;

		int topLeft= getInclusiveTopIndexStartOffset();
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=14938
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=22487
		// we want the exclusive offset (right after the last character)
		int bottomRight= getExclusiveBottomIndexEndOffset();
		int viewPort= bottomRight - topLeft;

		fScrollPos= fCachedTextWidget.getTopPixel();
		Point dimension= fCanvas.getSize();

		IDocument doc= fCachedTextViewer.getDocument();
		if (doc == null)
			return;

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
		boolean isWrapActive= fCachedTextWidget.getWordWrap();
		for (int layer= 0; layer < maxLayer; layer++) {
			Iterator<Annotation> iter;
			if (fModel instanceof IAnnotationModelExtension2)
				iter= ((IAnnotationModelExtension2)fModel).getAnnotationIterator(topLeft, viewPort + 1, true, true);
			else
				iter= fModel.getAnnotationIterator();

			while (iter.hasNext()) {
				Annotation annotation= iter.next();

				int lay= IAnnotationAccessExtension.DEFAULT_LAYER;
				if (fAnnotationAccessExtension != null)
					lay= fAnnotationAccessExtension.getLayer(annotation);
				maxLayer= Math.max(maxLayer, lay+1);	// dynamically update layer maximum
				if (lay != layer)	// wrong layer: skip annotation
					continue;

				if (skip(annotation))
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

					r.width= dimension.x;
					int lines= endLine - startLine;

					if (startLine != endLine || !isWrapActive || length <= 0) {
						// line height for different lines includes wrapped line info already,
						// end we show annotations without offset info at very first line anyway
						r.height= JFaceTextUtil.computeLineHeight(fCachedTextWidget, startLine, endLine + 1, lines + 1);
						r.y= JFaceTextUtil.computeLineHeight(fCachedTextWidget, 0, startLine, startLine) - fScrollPos;
					} else {
						// annotate only the part of the line related to the given offset
						Rectangle textBounds= fCachedTextWidget.getTextBounds(offset, offset + length);
						r.height= textBounds.height;
						r.y= textBounds.y;
					}
					if (r.y < dimension.y && fAnnotationAccessExtension != null)  // annotation within visible area
						fAnnotationAccessExtension.paint(annotation, gc, fCanvas, r);

				} catch (BadLocationException x) {
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

		ITextViewerExtension5 extension= (ITextViewerExtension5) fCachedTextViewer;

		fScrollPos= fCachedTextWidget.getTopPixel();
		Point dimension= fCanvas.getSize();

		int vOffset= getInclusiveTopIndexStartOffset();
		int vLength= getExclusiveBottomIndexEndOffset() - vOffset;

		// draw Annotations
		Rectangle r= new Rectangle(0, 0, 0, 0);
		ReusableRegion range= new ReusableRegion();
		boolean isWrapActive= fCachedTextWidget.getWordWrap();
		int minLayer= Integer.MAX_VALUE, maxLayer= Integer.MIN_VALUE;
		fCachedAnnotations.clear();
		Iterator<Annotation> iter;
		if (fModel instanceof IAnnotationModelExtension2)
			iter= ((IAnnotationModelExtension2)fModel).getAnnotationIterator(vOffset, vLength + 1, true, true);
		else
			iter= fModel.getAnnotationIterator();

		while (iter.hasNext()) {
			Annotation annotation= iter.next();

			if (skip(annotation))
				continue;

			Position position= fModel.getPosition(annotation);
			if (position == null)
				continue;

			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=217710
			int extendedVLength= position.getLength() == 0 ? vLength + 1 : vLength;
			if (!position.overlapsWith(vOffset, extendedVLength))
				continue;

			int lay= IAnnotationAccessExtension.DEFAULT_LAYER;
			if (fAnnotationAccessExtension != null)
				lay= fAnnotationAccessExtension.getLayer(annotation);

			minLayer= Math.min(minLayer, lay);
			maxLayer= Math.max(maxLayer, lay);
			fCachedAnnotations.add(new Tuple(annotation, position));
		}
		Collections.sort(fCachedAnnotations, fTupleComparator);

		for (int layer= minLayer; layer <= maxLayer; layer++) {
			for (int i= 0, n= fCachedAnnotations.size(); i < n; i++) {
				Tuple tuple= fCachedAnnotations.get(i);
				Annotation annotation= tuple.annotation;
				Position position= tuple.position;

				int lay= IAnnotationAccessExtension.DEFAULT_LAYER;
				if (fAnnotationAccessExtension != null)
					lay= fAnnotationAccessExtension.getLayer(annotation);
				if (lay != layer)	// wrong layer: skip annotation
					continue;

				range.setOffset(position.getOffset());
				range.setLength(position.getLength());
				IRegion widgetRegion= extension.modelRange2WidgetRange(range);
				if (widgetRegion == null)
					continue;

				int offset= widgetRegion.getOffset();
				int startLine= extension.widgetLineOfWidgetOffset(offset);
				if (startLine == -1)
					continue;

				int length= Math.max(widgetRegion.getLength() -1, 0);
				int endLine= extension.widgetLineOfWidgetOffset(offset + length);
				if (endLine == -1)
					continue;

				r.x= 0;

				r.width= dimension.x;
				int lines= endLine - startLine;

				if(startLine != endLine || !isWrapActive || length <= 0){
					// line height for different lines includes wrapped line info already,
					// end we show annotations without offset info at very first line anyway
					r.height= JFaceTextUtil.computeLineHeight(fCachedTextWidget, startLine, endLine + 1, lines + 1);
					r.y= JFaceTextUtil.computeLineHeight(fCachedTextWidget, 0, startLine, startLine)  - fScrollPos;
				} else {
					// annotate only the part of the line related to the given offset
					Rectangle textBounds= fCachedTextWidget.getTextBounds(offset, offset + length);
					r.height= textBounds.height;
					r.y = textBounds.y;
				}

				if (r.y < dimension.y && fAnnotationAccessExtension != null)  // annotation within visible area
					fAnnotationAccessExtension.paint(annotation, gc, fCanvas, r);
			}
		}

		fCachedAnnotations.clear();
	}


	/**
	 * Post a redraw request for this column into the UI thread.
	 */
	private void postRedraw() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			Display d= fCanvas.getDisplay();
			if (d != null) {
				d.asyncExec(new Runnable() {
					@Override
					public void run() {
						redraw();
					}
				});
			}
		}
	}

	@Override
	public void redraw() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			if (VerticalRuler.AVOID_NEW_GC) {
				fCanvas.redraw();
			} else {
				GC gc= new GC(fCanvas);
				doubleBufferPaint(gc);
				gc.dispose();
			}
		}
	}

	/*
	 * @see IVerticalRulerColumn#setModel
	 */
	@Override
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

	@Override
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

	@Override
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
		fAllowedAnnotationTypes.clear();
	}

	@Override
	public int getLineOfLastMouseButtonActivity() {
		return fParentRuler.getLineOfLastMouseButtonActivity();
	}

	@Override
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
		Boolean allowed= fAllowedAnnotationTypes.get(annotationType);
		if (allowed != null)
			return !allowed.booleanValue();

		boolean skip= skip(annotationType);
		fAllowedAnnotationTypes.put(annotationType, !skip ? Boolean.TRUE : Boolean.FALSE);
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
		if (fAnnotationAccessExtension != null) {
			Iterator<Object> e= fConfiguredAnnotationTypes.iterator();
			while (e.hasNext()) {
				if (fAnnotationAccessExtension.isSubtype(annotationType, e.next()))
					return false;
			}
			return true;
		}
		return !fConfiguredAnnotationTypes.contains(annotationType);
	}

	@Override
	public IAnnotationHover getHover() {
		return fHover;
	}

	/**
	 * @param hover The hover to set.
	 * @since 3.0
	 */
	public void setHover(IAnnotationHover hover) {
		fHover= hover;
	}

	@Override
	public void addVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}
}
