package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */



import java.util.Iterator;

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
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextEvent;


/**
 * A vertical ruler column displaying the graphics of annotations.
 * Will become final. Do not subclass.
 */
public class AnnotationRulerColumn implements IVerticalRulerColumn {
	
	/**
	 * Internal listener class.
	 */
	class InternalListener implements IViewportListener, IAnnotationModelListener, ITextListener {
		
		/*
		 * @see IViewportListener#viewportChanged
		 */
		public void viewportChanged(int verticalPosition) {
			if (verticalPosition != fScrollPos)
				redraw();
		}
		
		/*
		 * @see IAnnotationModelListener#modelChanged
		 */
		public void modelChanged(IAnnotationModel model) {
			postRedraw();
		}
		
		/*
		 * @see ITextListener#textChanged
		 */
		public void textChanged(TextEvent e) {
			if (e.getViewerRedrawState())
				redraw();
		}
	};
	
	
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
			}
			
			public void mouseDown(MouseEvent event) {
				fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			}
			
			public void mouseDoubleClick(MouseEvent event) {
				fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			}
		});
		
		if (fCachedTextViewer != null) {
			fCachedTextViewer.addViewportListener(fInternalListener);
			fCachedTextViewer.addTextListener(fInternalListener);
		}
		
		return fCanvas;
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
			doPaint(gc);
		} finally {
			gc.dispose();
		}
		
		dest.drawImage(fBuffer, 0, 0);
	}
	
	/**
	 * Draws the vertical ruler w/o drawing the Canvas background.
	 */
	private void doPaint(GC gc) {
	
		if (fModel == null || fCachedTextViewer == null)
			return;

		int topLeft= fCachedTextViewer.getTopIndexStartOffset();
		int bottomRight= fCachedTextViewer.getBottomIndexEndOffset();
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
		int maxLayer= 1;	// loop at least once thru layers.
		
		for (int layer= 0; layer < maxLayer; layer++) {
			Iterator iter= fModel.getAnnotationIterator();
			while (iter.hasNext()) {
				Annotation annotation= (Annotation) iter.next();
				
				int lay= annotation.getLayer();
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
					startLine -= topLine;
					
					int endLine= startLine;
					if (length > 0)
						endLine= doc.getLineOfOffset(offset + length - 1);
					if (endLine > bottomLine)
						endLine= bottomLine;
					endLine -= topLine;
					
					r.x= 0;
					r.y= (startLine * lineheight) - fScrollPos + shift;
					r.width= dimension.x;
					int lines= endLine - startLine;
					if (lines < 0)
						lines= -lines;
					r.height= (lines+1) * lineheight;
					
					if (r.y < dimension.y)  // annotation within visible area
						annotation.paint(gc, fCanvas, r);
					
				} catch (BadLocationException e) {
				}
			}
		}
	}
	
	/**
	 * Post a redraw request for thsi column  into the UI thread.
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
		if (model != fModel) {
			
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
}
