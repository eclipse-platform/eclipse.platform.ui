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

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension3;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;



/**
 * Paints annotations provided by an annotation model as squigglies onto an 
 * associated source viewer. Clients usually instantiate and configure objects
 * of this class.
 * 
 * @since 2.1
 */
public class AnnotationPainter implements IPainter, PaintListener, IAnnotationModelListener {	
	
	/** 
	 * The presentation information (decoration) for an annotation.  Each such
	 * object represents one squiggly.
	 */
	private static class Decoration {
		/** The position of this decoration */
		Position fPosition;
		/** The color of this decoration */
		Color fColor;
		/** Indicates whether this decoration might span multiple lines */
		boolean fMultiLine;
	}
	
	/** Indicates whether this painter is active */
	private boolean fIsActive= false;
	/** Indicates whether this painter is managing decorations */
	private boolean fIsPainting= false;
	/** Indicates whether this painter is setting its annotation model */
	private boolean fIsSettingModel= false;
	/** The associated source viewer */
	private ISourceViewer fSourceViewer;
	/** The cached widget of the source viewer */
	private StyledText fTextWidget;
	/** The annotation model providing the annotations to be drawn */
	private IAnnotationModel fModel;
	/** The annotation access */
	private IAnnotationAccess fAnnotationAccess;
	/** The list of decorations */
	private List fDecorations= new ArrayList();
	/** The internal color table */
	private Map fColorTable= new HashMap();
	/** The list of types of annotations that are painted by this painter */
	private Set fAnnotationTypes= new HashSet();

	
	/**
	 * Creates a new annotation painter for the given source viewer and with the given
	 * annotation access. The painter is uninitialized, i.e.  no annotation types are configured
	 * to be painted.
	 * 
	 * @param sourceViewer the source viewer for this painter
	 * @param access the annotation access for this painter
	 */
	public AnnotationPainter(ISourceViewer sourceViewer, IAnnotationAccess access) {
		fSourceViewer= sourceViewer;
		fAnnotationAccess= access;
		fTextWidget= sourceViewer.getTextWidget();
	}
	
	/** 
	 * Returns whether this painter has to draw any suiggly.
	 * 
	 * @return <code>true</code> if there are squigglies to be drawn, <code>false</code> otherwise
	 */
	private boolean hasDecorations() {
		return !fDecorations.isEmpty();
	}	
	
	/**
	 * Enables painting. This painter registers a paint listener with the
	 * source viewer's widget.
	 */
	private void enablePainting() {
		if (!fIsPainting && hasDecorations()) {
			fIsPainting= true;
			fTextWidget.addPaintListener(this);
			handleDrawRequest(null);
		}
	}
	
	/**
	 * Disables painting, if is has previously been enabled. Removes
	 * any paint listeners registered with the source viewer's widget.
	 * 
	 * @param redraw <code>true</code> if the widget should be redrawn after disabling
	 */
	private void disablePainting(boolean redraw) {
		if (fIsPainting) {
			fIsPainting= false;
			fTextWidget.removePaintListener(this);
			if (redraw && hasDecorations())
				handleDrawRequest(null);
		}
	}
	
	/**
	 * Sets the annotation model for this painter. Registers this painter
	 * as listener of the give model, if the model is not <code>null</code>.
	 * 
	 * @param model the annotation model
	 */
	private void setModel(IAnnotationModel model) {
		if (fModel != model) {
			if (fModel != null)
				fModel.removeAnnotationModelListener(this);
			fModel= model;
			if (fModel != null) {
				try {
					fIsSettingModel= true;
					fModel.addAnnotationModelListener(this);
				} finally {
					fIsSettingModel= false;
				}
			}
		}
	}
	
	/**
	 * Updates the set of decorations based on the current state of
	 * the painter's annotation model.
	 */
	private void catchupWithModel() {	
		if (fDecorations != null) {
			fDecorations.clear();
			if (fModel != null) {
				
				Iterator e= fModel.getAnnotationIterator();
				while (e.hasNext()) {
					
					Annotation annotation= (Annotation) e.next();
					Object annotationType= fAnnotationAccess.getType(annotation);
					if (annotationType == null)
						continue;
						
					Color color= null;
					if (fAnnotationTypes.contains(annotationType))
						color= (Color) fColorTable.get(annotationType);
					
					if (color != null) {
						Decoration pp= new Decoration();
						pp.fPosition= fModel.getPosition(annotation);
						pp.fColor= color;
						pp.fMultiLine= fAnnotationAccess.isMultiLine(annotation);
						fDecorations.add(pp);
					}
				}
			}
		}
	}
	
	/**
	 * Recomputes the squigglies to be drawn and redraws them.
	 */
	private void updatePainting() {
		disablePainting(true);
		catchupWithModel();							
		enablePainting();
	}
	
	/*
	 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
	 */
	public void modelChanged(final IAnnotationModel model) {
		if (fTextWidget != null && !fTextWidget.isDisposed()) {
			if (fIsSettingModel) {
				// inside the ui thread -> no need for posting
				updatePainting();
			} else {
				Display d= fTextWidget.getDisplay();
				if (d != null) {
					d.asyncExec(new Runnable() {
						public void run() {
							if (fTextWidget != null && !fTextWidget.isDisposed())
								updatePainting();
						}
					});
				}
			}
		}
	}
	
	/**
	 * Sets the color in which the squiggly for the given annotation type should be drawn.
	 * 
	 * @param annotationType the annotation type
	 * @param color the color
	 */
	public void setAnnotationTypeColor(Object annotationType, Color color) {
		if (color != null)
			fColorTable.put(annotationType, color);
		else
			fColorTable.remove(annotationType);
	}
	
	/**
	 * Adds the given annotation type to the list of annotation types whose
	 * annotations should be painted by this painter. If the annotation  type
	 * is already in this list, this method is without effect.
	 * 
	 * @param annotationType the annotation type
	 */
	public void addAnnotationType(Object annotationType) {
		fAnnotationTypes.add(annotationType);
	}
	
	/**
	 * Removes the given annotation type from the list of annotation types whose
	 * annotations are painted by this painter. If the annotation type is not
	 * in this list, this method is wihtout effect.
	 * 
	 * @param annotationType the annotation type
	 */
	public void removeAnnotationType(Object annotationType) {
		fAnnotationTypes.remove(annotationType);
	}
	
	/**
	 * Clears the list of annotation types whose annotations are
	 * painted by this painter.
	 */
	public void removeAllAnnotationTypes() {
		fAnnotationTypes.clear();
	}
	
	/**
	 * Returns whether the list of annotation types whose annotations are painted
	 * by this painter contains at least on element.
	 * 
	 * @return <code>true</code> if there is an annotation type whose annotations are painted
	 */
	public boolean isPaintingAnnotations() {
		return !fAnnotationTypes.isEmpty();
	}
	
	/*
	 * @see IPainter#dispose()
	 */
	public void dispose() {
		
		if (fColorTable != null)	
			fColorTable.clear();
		fColorTable= null;
		
		if (fAnnotationTypes != null)
			fAnnotationTypes.clear();
		fAnnotationTypes= null;
		
		fTextWidget= null;
		fSourceViewer= null;
		fAnnotationAccess= null;
		fModel= null;
		fDecorations= null;
	}

	/**
	 * Returns the document offset of the upper left corner of the source viewer's viewport,
	 * possibly including partially visible lines.
	 * 
	 * @return the document offset if the upper left corner of the viewport
	 */
	private int getInclusiveTopIndexStartOffset() {
		
		if (fTextWidget != null && !fTextWidget.isDisposed()) {	
			int top= fSourceViewer.getTopIndex();
			if ((fTextWidget.getTopPixel() % fTextWidget.getLineHeight()) != 0)
				top--;
			try {
				IDocument document= fSourceViewer.getDocument();
				return document.getLineOffset(top);
			} catch (BadLocationException ex) {
			}
		}
		
		return -1;
	}
	
	/*
	 * @see PaintListener#paintControl(PaintEvent)
	 */
	public void paintControl(PaintEvent event) {
		if (fTextWidget != null)
			handleDrawRequest(event.gc);
	}
	
	/**
	 * Handles the request to draw the annotations using the given gaphical context.
	 * 
	 * @param gc the graphical context
	 */
	private void handleDrawRequest(GC gc) {
		
		if (fTextWidget == null) {
			// is already disposed
			return;
		}

		int vOffset= getInclusiveTopIndexStartOffset();
		// http://bugs.eclipse.org/bugs/show_bug.cgi?id=17147
		int vLength= fSourceViewer.getBottomIndexEndOffset() + 1;		
		
		for (Iterator e = fDecorations.iterator(); e.hasNext();) {
			Decoration pp = (Decoration) e.next();
			Position p= pp.fPosition;
			if (p.overlapsWith(vOffset, vLength)) {
								
				if (!pp.fMultiLine) {
					
					IRegion widgetRange= getWidgetRange(p);
					if (widgetRange != null)
						draw(gc, widgetRange.getOffset(), widgetRange.getLength(), pp.fColor);
				
				} else {
					
					IDocument document= fSourceViewer.getDocument();
					try {
												
						int startLine= document.getLineOfOffset(p.getOffset()); 
						int lastInclusive= Math.max(p.getOffset(), p.getOffset() + p.getLength() - 1);
						int endLine= document.getLineOfOffset(lastInclusive);
						
						for (int i= startLine; i <= endLine; i++) {
							IRegion line= document.getLineInformation(i);
							int paintStart= Math.max(line.getOffset(), p.getOffset());
							int paintEnd= Math.min(line.getOffset() + line.getLength(), p.getOffset() + p.getLength());
							if (paintEnd > paintStart) {
								// otherwise inside a line delimiter
								IRegion widgetRange= getWidgetRange(new Position(paintStart, paintEnd - paintStart));
								if (widgetRange != null)
									draw(gc, widgetRange.getOffset(), widgetRange.getLength(), pp.fColor);
							}
						}
					
					} catch (BadLocationException x) {
					}
				}
			}
		}
	}
	
	/**
	 * Returns the widget region that corresponds to the given region in the
	 * viewer's document.
	 * 
	 * @param p the region in the viewer's document
	 * @return the corresponding widget region
	 */
	private IRegion getWidgetRange(Position p) {
		if (fSourceViewer instanceof ITextViewerExtension3) {
			
			ITextViewerExtension3 extension= (ITextViewerExtension3) fSourceViewer;
			return extension.modelRange2WidgetRange(new Region(p.getOffset(), p.getLength()));
		
		} else {
			
			IRegion region= fSourceViewer.getVisibleRegion();
			int offset= region.getOffset();
			int length= region.getLength();
			
			if (p.overlapsWith(offset , length)) {
				int p1= Math.max(offset, p.getOffset());
				int p2= Math.min(offset + length, p.getOffset() + p.getLength());
				return new Region(p1 - offset, p2 - p1);
			}
		}
		
		return null;
	}
	
	/**
	 * Computes an array of alternating x and y values which are the corners of the squiggly line of the
	 * given height between the given end points.
	 *  
	 * @param left the left end point
	 * @param right the right end point
	 * @param height the height of the squiggly line
	 * @return the array of alternating x and y values which are the corners of the squiggly line
	 */
	private int[] computePolyline(Point left, Point right, int height) {
		
		final int WIDTH= 4; // must be even
		final int HEIGHT= 2; // can be any number
//		final int MINPEEKS= 2; // minimal number of peeks
		
		int peeks= (right.x - left.x) / WIDTH;
//		if (peeks < MINPEEKS) {
//			int missing= (MINPEEKS - peeks) * WIDTH;
//			left.x= Math.max(0, left.x - missing/2);
//			peeks= MINPEEKS;
//		}
		
		int leftX= left.x;
				
		// compute (number of point) * 2
		int length= ((2 * peeks) + 1) * 2;
		if (length < 0)
			return new int[0];
			
		int[] coordinates= new int[length];
		
		// cache peeks' y-coordinates
		int bottom= left.y + height - 1;
		int top= bottom - HEIGHT;
		
		// populate array with peek coordinates
		for (int i= 0; i < peeks; i++) {
			int index= 4 * i;
			coordinates[index]= leftX + (WIDTH * i);
			coordinates[index+1]= bottom;
			coordinates[index+2]= coordinates[index] + WIDTH/2;
			coordinates[index+3]= top;
		}
		
		// the last down flank is missing
		coordinates[length-2]= left.x + (WIDTH * peeks);
		coordinates[length-1]= bottom;
		
		return coordinates;
	}
	
	/**
	 * Draws a squiggly line of the given length start at the given offset in the
	 * given color.
	 * 
	 * @param gc the grahical context
	 * @param offset the offset of the line
	 * @param length the length of the line
	 * @param color the color of the line
	 */
	private void draw(GC gc, int offset, int length, Color color) {
		if (gc != null) {
			
			Point left= fTextWidget.getLocationAtOffset(offset);
			Point right= fTextWidget.getLocationAtOffset(offset + length);
			
			gc.setForeground(color);
			int[] polyline= computePolyline(left, right, gc.getFontMetrics().getHeight());
			gc.drawPolyline(polyline);
								
		} else {
			fTextWidget.redrawRange(offset, length, true);
		}
	}
	
	/*
	 * @see IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			disablePainting(redraw);
			setModel(null);
			catchupWithModel();
		}
	}
	
	/*
	 * @see IPainter#paint(int)
	 */
	public void paint(int reason) {
		
		if (fSourceViewer.getDocument() == null) {
			deactivate(false);
			return;
		}
		
		if (!fIsActive) {
			IAnnotationModel model= fSourceViewer.getAnnotationModel();
			if (model != null) {
				fIsActive= true;
				setModel(fSourceViewer.getAnnotationModel());
			}
		} else if (CONFIGURATION == reason || INTERNAL == reason)
			updatePainting();
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.text.IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
	}
}
