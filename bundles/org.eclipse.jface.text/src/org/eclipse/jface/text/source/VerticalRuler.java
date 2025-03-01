/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.text.source;


import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;


/**
 * A vertical ruler which is connected to a text viewer. Single column standard
 * implementation of {@link org.eclipse.jface.text.source.IVerticalRuler}.
 * <p>
 * The same can be achieved by using <code>CompositeRuler</code> configured
 * with an <code>AnnotationRulerColumn</code>. Clients may use this class as
 * is.
 *
 * @see org.eclipse.jface.text.ITextViewer
 */
public final class VerticalRuler implements IVerticalRuler, IVerticalRulerExtension {

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
			update();
		}

		@Override
		public void textChanged(TextEvent e) {
			if (fTextViewer != null && e.getViewerRedrawState())
				redraw();
		}
	}

	/**
	 * <code>true</code> if we're on a Mac/GTK, where "new GC(canvas)" is expensive.
	 * <b>Warning:</b> On Windows, we can't switch to direct-paint mode, because
	 * calls to update() have been removed, and now repaints would only happen with huge
	 * delays when scrolling, see <a href="https://bugs.eclipse.org/511596#c26">bug 511596 comment 26</a>.
	 *
	 * @see <a href="https://bugs.eclipse.org/298936">bug 298936</a>
	 * @see <a href="https://bugs.eclipse.org/467499">bug 467499</a>
	 * @since 3.6
	 */
	static final boolean AVOID_NEW_GC= Util.isMac() || Util.isGtk();


	/** The vertical ruler's text viewer */
	private ITextViewer fTextViewer;
	/** The ruler's canvas */
	private Canvas fCanvas;
	/** The vertical ruler's model */
	private IAnnotationModel fModel;
	/** Cache for the actual scroll position in pixels */
	private int fScrollPos;
	/** The buffer for double buffering */
	private Image fBuffer;
	/** The line of the last mouse button activity */
	private int fLastMouseButtonActivityLine= -1;
	/** The internal listener */
	private InternalListener fInternalListener= new InternalListener();
	/** The width of this vertical ruler */
	private int fWidth;
	/**
	 * The annotation access of this vertical ruler
	 * @since 3.0
	 */
	private IAnnotationAccess fAnnotationAccess;

	/**
	 * Constructs a vertical ruler with the given width.
	 *
	 * @param width the width of the vertical ruler
	 */
	public VerticalRuler(int width) {
		this(width, null);
	}

	/**
	 * Constructs a vertical ruler with the given width and the given annotation
	 * access.
	 *
	 * @param width the width of the vertical ruler
	 * @param annotationAcccess the annotation access
	 * @since 3.0
	 */
	public VerticalRuler(int width, IAnnotationAccess annotationAcccess) {
		fWidth= width;
		fAnnotationAccess= annotationAcccess;
	}

	@Override
	public Control getControl() {
		return fCanvas;
	}

	@Override
	public Control createControl(Composite parent, ITextViewer textViewer) {

		fTextViewer= textViewer;

		fCanvas= new Canvas(parent, SWT.NO_BACKGROUND);

		fCanvas.addPaintListener(event -> {
			if (fTextViewer != null)
				doubleBufferPaint(event.gc);
		});

		fCanvas.addDisposeListener(e -> {
			handleDispose();
			fTextViewer= null;
		});

		fCanvas.addListener(SWT.ZoomChanged, e -> {
			if (fBuffer != null) {
				fBuffer.dispose();
				fBuffer= null;
			}
		});

		fCanvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent event) {
			}

			@Override
			public void mouseDown(MouseEvent event) {
				fLastMouseButtonActivityLine= toDocumentLineNumber(event.y);
			}

			@Override
			public void mouseDoubleClick(MouseEvent event) {
				fLastMouseButtonActivityLine= toDocumentLineNumber(event.y);
			}
		});

		if (fTextViewer != null) {
			fTextViewer.addViewportListener(fInternalListener);
			fTextViewer.addTextListener(fInternalListener);
		}

		return fCanvas;
	}

	/**
	 * Disposes the ruler's resources.
	 */
	private void handleDispose() {

		if (fTextViewer != null) {
			fTextViewer.removeViewportListener(fInternalListener);
			fTextViewer.removeTextListener(fInternalListener);
			fTextViewer= null;
		}

		if (fModel != null)
			fModel.removeAnnotationModelListener(fInternalListener);

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

		if (fBuffer == null) {
			fBuffer= new Image(fCanvas.getDisplay(), this::doPaint, size.x, size.y);
		} else {
			GC gc= new GC(fBuffer);
			try {
				doPaint(gc, size.x, size.y);
			} finally {
				gc.dispose();
			}
		}
		dest.drawImage(fBuffer, 0, 0);
	}

	private void doPaint(GC gc, int width, int height) {
		gc.setFont(fTextViewer.getTextWidget().getFont());
		gc.setBackground(fCanvas.getBackground());
		gc.fillRectangle(0, 0, width, height);

		if (fTextViewer instanceof ITextViewerExtension5)
			doPaint1(gc);
		else
			doPaint(gc);
	}

	/**
	 * Returns the document offset of the upper left corner of the
	 * widgets view port, possibly including partially visible lines.
	 *
	 * @return the document offset of the upper left corner including partially visible lines
	 * @since 2.0
	 */
	private int getInclusiveTopIndexStartOffset() {

		StyledText textWidget= fTextViewer.getTextWidget();
		if (textWidget != null && !textWidget.isDisposed()) {
			int top= JFaceTextUtil.getPartialTopIndex(fTextViewer);
			try {
				IDocument document= fTextViewer.getDocument();
				return document.getLineOffset(top);
			} catch (BadLocationException x) {
			}
		}

		return -1;
	}



	/**
	 * Draws the vertical ruler w/o drawing the Canvas background.
	 *
	 * @param gc  the GC to draw into
	 */
	protected void doPaint(GC gc) {

		if (fModel == null || fTextViewer == null)
			return;

		IAnnotationAccessExtension annotationAccessExtension= null;
		if (fAnnotationAccess instanceof IAnnotationAccessExtension)
			annotationAccessExtension= (IAnnotationAccessExtension) fAnnotationAccess;

		StyledText styledText= fTextViewer.getTextWidget();
		IDocument doc= fTextViewer.getDocument();

		int topLeft= getInclusiveTopIndexStartOffset();
		int bottomRight= fTextViewer.getBottomIndexEndOffset();
		int viewPort= bottomRight - topLeft;

		Point d= fCanvas.getSize();
		fScrollPos= styledText.getTopPixel();

		int topLine= -1, bottomLine= -1;
		try {
			IRegion region= fTextViewer.getVisibleRegion();
			topLine= doc.getLineOfOffset(region.getOffset());
			bottomLine= doc.getLineOfOffset(region.getOffset() + region.getLength());
		} catch (BadLocationException x) {
			return;
		}

		// draw Annotations
		Rectangle r= new Rectangle(0, 0, 0, 0);
		int maxLayer= 1;	// loop at least once though layers.

		for (int layer= 0; layer < maxLayer; layer++) {
			Iterator<Annotation> iter= fModel.getAnnotationIterator();
			while (iter.hasNext()) {
				IAnnotationPresentation annotationPresentation= null;
				Annotation annotation= iter.next();

				int lay= IAnnotationAccessExtension.DEFAULT_LAYER;
				if (annotationAccessExtension != null)
					lay= annotationAccessExtension.getLayer(annotation);
				else if (annotation instanceof IAnnotationPresentation) {
					annotationPresentation= (IAnnotationPresentation)annotation;
					lay= annotationPresentation.getLayer();
				}
				maxLayer= Math.max(maxLayer, lay+1);	// dynamically update layer maximum
				if (lay != layer)	// wrong layer: skip annotation
					continue;

				Position position= fModel.getPosition(annotation);
				if (position == null)
					continue;

				if (!position.overlapsWith(topLeft, viewPort))
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
					r.y= JFaceTextUtil.computeLineHeight(styledText, 0, startLine, startLine)  - fScrollPos;

					r.width= d.x;
					int lines= endLine - startLine;

					r.height= JFaceTextUtil.computeLineHeight(styledText, startLine, endLine + 1, (lines+1));

					if (r.y < d.y && annotationAccessExtension != null)  // annotation within visible area
						annotationAccessExtension.paint(annotation, gc, fCanvas, r);
					else if (annotationPresentation != null)
						annotationPresentation.paint(gc, fCanvas, r);

				} catch (BadLocationException e) {
				}
			}
		}
	}

	/**
	 * Draws the vertical ruler w/o drawing the Canvas background. Uses
	 * <code>ITextViewerExtension5</code> for its implementation. Will replace
	 * <code>doPaint(GC)</code>.
	 *
	 * @param gc  the GC to draw into
	 */
	protected void doPaint1(GC gc) {

		if (fModel == null || fTextViewer == null)
			return;

		IAnnotationAccessExtension annotationAccessExtension= null;
		if (fAnnotationAccess instanceof IAnnotationAccessExtension)
			annotationAccessExtension= (IAnnotationAccessExtension) fAnnotationAccess;

		ITextViewerExtension5 extension= (ITextViewerExtension5) fTextViewer;
		StyledText textWidget= fTextViewer.getTextWidget();

		fScrollPos= textWidget.getTopPixel();
		Point dimension= fCanvas.getSize();

		// draw Annotations
		Rectangle r= new Rectangle(0, 0, 0, 0);
		int maxLayer= 1;	// loop at least once through layers.

		for (int layer= 0; layer < maxLayer; layer++) {
			Iterator<Annotation> iter= fModel.getAnnotationIterator();
			while (iter.hasNext()) {
				IAnnotationPresentation annotationPresentation= null;
				Annotation annotation= iter.next();

				int lay= IAnnotationAccessExtension.DEFAULT_LAYER;
				if (annotationAccessExtension != null)
					lay= annotationAccessExtension.getLayer(annotation);
				else if (annotation instanceof IAnnotationPresentation) {
					annotationPresentation= (IAnnotationPresentation)annotation;
					lay= annotationPresentation.getLayer();
				}
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
				r.y= JFaceTextUtil.computeLineHeight(textWidget, 0, startLine, startLine)  - fScrollPos;

				r.width= dimension.x;
				int lines= endLine - startLine;

				r.height= JFaceTextUtil.computeLineHeight(textWidget, startLine, endLine + 1, lines+1);

				if (r.y < dimension.y && annotationAccessExtension != null)  // annotation within visible area
					annotationAccessExtension.paint(annotation, gc, fCanvas, r);
				else if (annotationPresentation != null)
					annotationPresentation.paint(gc, fCanvas, r);
			}
		}
	}

	/**
	 * Thread-safe implementation.
	 * Can be called from any thread.
	 */
	@Override
	public void update() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			Display d= fCanvas.getDisplay();
			if (d != null) {
				d.asyncExec(this::redraw);
			}
		}
	}

	/**
	 * Redraws the vertical ruler.
	 */
	private void redraw() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			if (AVOID_NEW_GC) {
				fCanvas.redraw();
			} else {
				GC gc= new GC(fCanvas);
				doubleBufferPaint(gc);
				gc.dispose();
			}
		}
	}

	@Override
	public void setModel(IAnnotationModel model) {
		if (model != fModel) {

			if (fModel != null)
				fModel.removeAnnotationModelListener(fInternalListener);

			fModel= model;

			if (fModel != null)
				fModel.addAnnotationModelListener(fInternalListener);

			update();
		}
	}

	@Override
	public IAnnotationModel getModel() {
		return fModel;
	}

	@Override
	public int getWidth() {
		return fWidth;
	}

	@Override
	public int getLineOfLastMouseButtonActivity() {
		IDocument doc= fTextViewer.getDocument();
		if (doc == null || fLastMouseButtonActivityLine >= fTextViewer.getDocument().getNumberOfLines())
			fLastMouseButtonActivityLine= -1;
		return fLastMouseButtonActivityLine;
	}

	@Override
	public int toDocumentLineNumber(int y_coordinate) {
		if (fTextViewer == null  || y_coordinate == -1)
			return -1;

		StyledText text= fTextViewer.getTextWidget();
		int line= text.getLineIndex(y_coordinate);

		if (line == text.getLineCount() - 1) {
			// check whether y_coordinate exceeds last line
			if (y_coordinate > text.getLinePixel(line + 1))
				return -1;
		}

		return widgetLine2ModelLine(fTextViewer, line);
	}

	/**
	 * Returns the line of the viewer's document that corresponds to the given widget line.
	 *
	 * @param viewer the viewer
	 * @param widgetLine the widget line
	 * @return the corresponding line of the viewer's document
	 * @since 2.1
	 */
	protected final static int widgetLine2ModelLine(ITextViewer viewer, int widgetLine) {

		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			return extension.widgetLine2ModelLine(widgetLine);
		}

		try {
			IRegion r= viewer.getVisibleRegion();
			IDocument d= viewer.getDocument();
			return widgetLine + d.getLineOfOffset(r.getOffset());
		} catch (BadLocationException x) {
		}
		return widgetLine;
	}

	@Override
	public void setFont(Font font) {
	}

	@Override
	public void setLocationOfLastMouseButtonActivity(int x, int y) {
		fLastMouseButtonActivityLine= toDocumentLineNumber(y);
	}

	/**
	 * Adds the given mouse listener.
	 *
	 * @param listener the listener to be added
	 * @deprecated will be removed
	 * @since 2.0
	 */
	@Deprecated
	public void addMouseListener(MouseListener listener) {
		if (fCanvas != null && !fCanvas.isDisposed())
			fCanvas.addMouseListener(listener);
	}

	/**
	 * Removes the given mouse listener.
	 *
	 * @param listener the listener to be removed
	 * @deprecated will be removed
	 * @since 2.0
	 */
	@Deprecated
	public void removeMouseListener(MouseListener listener) {
		if (fCanvas != null && !fCanvas.isDisposed())
			fCanvas.removeMouseListener(listener);
	}
}
