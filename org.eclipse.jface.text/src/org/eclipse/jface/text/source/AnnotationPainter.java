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


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.custom.StyleRange;
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
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension3;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;


/**
 * Paints annotations provided by an annotation model as squiggly lines and/or
 * highlighted onto an associated source viewer.
 * Clients usually instantiate and configure objects of this class.
 * 
 * @since 2.1
 */
public class AnnotationPainter implements IPainter, PaintListener, IAnnotationModelListener, IAnnotationModelListenerExtension, ITextPresentationListener {	
	
	
	/**
	 * A drawing strategy responsible for drawing a certain decoration.
	 * 
	 * @since 3.0
	 */
	public interface IDrawingStrategy {
		/**
		 * Draws a decoration of the given length start at the given offset in the
		 * given color onto the specified gc.
		 * 
		 * @param annotation the annotation to be drawn
		 * @param gc the grahical context
		 * @param textWidget the text widget to draw on
		 * @param offset the offset of the line
		 * @param length the length of the line
		 * @param color the color of the line
		 */
		void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color);
	}
	
	/**
	 * Squiggly drawing strategy.
	 * 
	 * @since 3.0
	 */
	public static class SquigglesStrategy implements IDrawingStrategy {
		/**
		 * {@inheritdoc}
		 */
		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
			if (gc != null) {
				
				Point left= textWidget.getLocationAtOffset(offset);
				Point right= textWidget.getLocationAtOffset(offset + length);
				
				gc.setForeground(color);
				int[] polyline= computePolyline(left, right, gc.getFontMetrics().getHeight());
				gc.drawPolyline(polyline);
									
			} else {
				textWidget.redrawRange(offset, length, true);
			}
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
//			final int MINPEEKS= 2; // minimal number of peeks
			
			int peeks= (right.x - left.x) / WIDTH;
//			if (peeks < MINPEEKS) {
//				int missing= (MINPEEKS - peeks) * WIDTH;
//				left.x= Math.max(0, left.x - missing/2);
//				peeks= MINPEEKS;
//			}
			
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
	}
	
	/**
	 * Drawing strategy that does nothing.
	 * @since 3.0
	 */
	public static final class NullStrategy implements IDrawingStrategy {
		/**
		 * {@inheritdoc}
		 */
		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
			// do nothing
		}
	}
	
	/**
	 * Tells whether this class is in debug mode.
	 * @since 3.0
	 */
	private static boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface.text/debug/AnnotationPainter"));  //$NON-NLS-1$//$NON-NLS-2$
	/**
	 * The squiggly painter strategy.
	 * @since 3.0
	 */
	private static final IDrawingStrategy fgSquigglyDrawer= new SquigglesStrategy();
	/** 
	 * The squiggles painter id. 
	 * @since 3.0
	 */
	private static final Object SQUIGGLES= new Object();
	/**
	 * The default strategy that does nothing.
	 * @since 3.0
	 */
	private static final IDrawingStrategy fgNullDrawer= new NullStrategy();
	
	/** 
	 * The presentation information (decoration) for an annotation.  Each such
	 * object represents one decoration drawn on the text area, such as squiggly lines
	 * and underlines.
	 */
	private static class Decoration {
		/** The position of this decoration */
		private Position fPosition;
		/** The color of this decoration */
		private Color fColor;
		/**
		 * The annotation's layer
		 * @since 3.0
		 */
		private int fLayer;
		/**
		 * The painter strategy for this decoration.
		 * @since 3.0
		 */
		private IDrawingStrategy fPainter;
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
	/**
	 * The map with decorations
	 * @since 3.0
	 */
	private Map fDecorationsMap= new HashMap(); // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=50767
	/**
	 * The map with of highlighted decorations.
	 * @since 3.0
	 */
	private Map fHighlightedDecorationsMap= new HashMap(); // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=50767
	/** The internal color table */
	private Map fColorTable= new HashMap();
	/** The list of configured annotation types for being painted by this painter */
	private Set fConfiguredAnnotationTypes= new HashSet();
	/**
	 * The list of allowed annotation types for being painted by this painter.
	 * @since 3.0
	 */
	private Set fAllowedAnnotationTypes= new HashSet();
	/**
	 * The list of configured annotation typed to be highlighted by this painter.
	 * @since 3.0
	 */
	private Set fConfiguredHighlightAnnotationTypes= new HashSet();
	/**
	 * The list of allowed annotation types to be highlighted by this painter.
	 * @since 3.0
	 */
	private Set fAllowedHighlightAnnotationTypes= new HashSet();
	/**
	 * The range in which the current highlight annotations can be found.
	 * @since 3.0
	 */
	private Position fCurrentHighlightAnnotationRange= null;
	/**
	 * The range in which all add, removed and changed highlight
	 * annotations can be found since the last world change.
	 * @since 3.0
	 */
	private Position fTotalHighlightAnnotationRange= null;
	/**
	 * The text input listener.
	 * @since 3.0
	 */
	private ITextInputListener fTextInputListener;
	/**
	 * Flag which tells that a new document input is currently being set.
	 * @since 3.0
	 */
	private boolean fInputDocumentAboutToBeChanged;
	/**
	 * Maps annotation types to drawing strategy ids.
	 * @since 3.0
	 */
	private Map fAnnotationType2DrawingStrategyId= new HashMap();
	/**
	 * Maps drawing strategy ids to drawing strategies.
	 * @since 3.0
	 */
	private Map fRegisteredDrawingStrategies= new HashMap();

	
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
		
		// default drawing strategies: squiggles are the only pre-3.0 drawing style,
		fRegisteredDrawingStrategies.put(SQUIGGLES, fgSquigglyDrawer);
	}
	
	/** 
	 * Returns whether this painter has to draw any squiggle.
	 * 
	 * @return <code>true</code> if there are squiggles to be drawn, <code>false</code> otherwise
	 */
	private boolean hasDecorations() {
		return !fDecorationsMap.isEmpty();
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
				synchronized(this) {
					try {
						fIsSettingModel= true;
						fModel.addAnnotationModelListener(this);
					} finally {
						fIsSettingModel= false;
					}
				}
			}
		}
	}
	
	/**
	 * Updates the set of decorations based on the current state of
	 * the painter's annotation model.
	 */
	private synchronized void catchupWithModel(AnnotationModelEvent event) {
	    
		if (fDecorationsMap != null) {
			
			int highlightAnnotationRangeStart= Integer.MAX_VALUE; 			
			int highlightAnnotationRangeEnd= -1;
			
			if (fModel != null) {
				
				boolean isWorldChange= false;
				
				Iterator e;
				if (event == null || event.isWorldChange()) {
					isWorldChange= true;
					
					if (DEBUG && event == null)
						System.out.println("AP: INTERNAL CHANGE"); //$NON-NLS-1$
					
					fDecorationsMap.clear();
					fHighlightedDecorationsMap.clear();

					e= fModel.getAnnotationIterator();


				} else {
					
					// Remove annotations
					Annotation[] removedAnnotations= event.getRemovedAnnotations();
					for (int i=0, length= removedAnnotations.length; i < length; i++) {
						Annotation annotation= removedAnnotations[i];
						Decoration decoration= (Decoration)fHighlightedDecorationsMap.remove(annotation);
						if (decoration != null) {
							Position position= decoration.fPosition;
							if (position != null && !position.isDeleted()) {
								highlightAnnotationRangeStart= Math.min(highlightAnnotationRangeStart, position.offset);
								highlightAnnotationRangeEnd= Math.max(highlightAnnotationRangeEnd, position.offset + position.length);
							}
						}
						fDecorationsMap.remove(annotation);
					}
					
					// Update existing annotations
					Annotation[] changedAnnotations= event.getChangedAnnotations();
					for (int i=0, length= changedAnnotations.length; i < length; i++) {
						Annotation annotation= changedAnnotations[i];

						Object annotationType= annotation.getType();
						boolean isHighlighting=  shouldBeHighlighted(annotationType);
						boolean isDrawingSquiggles= shouldBeDrawn(annotationType); 
						
						Decoration decoration= (Decoration)fHighlightedDecorationsMap.get(annotation);
						
						if (decoration != null) {
							// The call below updates the decoration - no need to create new decoration
							decoration= getDecoration(annotation, decoration, isDrawingSquiggles, isHighlighting);
							if (decoration == null)
								fHighlightedDecorationsMap.remove(annotation);
						} else {
							decoration= getDecoration(annotation, decoration, isDrawingSquiggles, isHighlighting);
							if (decoration != null && isHighlighting)
								fHighlightedDecorationsMap.put(annotation, decoration);
						}
							
						Position position= null;
						if (decoration == null)
							position= fModel.getPosition(annotation);
						else
							position= decoration.fPosition;
						
						if (position != null && !position.isDeleted()) {
							highlightAnnotationRangeStart= Math.min(highlightAnnotationRangeStart, position.offset);
							highlightAnnotationRangeEnd= Math.max(highlightAnnotationRangeEnd, position.offset + position.length);
						} else {
							fHighlightedDecorationsMap.remove(annotation);
						}
					
						Decoration oldDecoration= (Decoration)fDecorationsMap.get(annotation);
						if (decoration != null && isDrawingSquiggles)
							fDecorationsMap.put(annotation, decoration);
						else if (oldDecoration != null)
							fDecorationsMap.remove(annotation);
					}
					
					e= Arrays.asList(event.getAddedAnnotations()).iterator();
				}
				
				// Add new annotations
				while (e.hasNext()) {
					Annotation annotation= (Annotation) e.next();

					Object annotationType= annotation.getType();
					boolean isHighlighting=  shouldBeHighlighted(annotationType);
					boolean isDrawingSquiggles= shouldBeDrawn(annotationType);
					
					Decoration pp= getDecoration(annotation, null, isDrawingSquiggles, isHighlighting);
					
					if (pp != null) {
						
						if (isDrawingSquiggles)
							fDecorationsMap.put(annotation, pp);
						
						if (isHighlighting) {
							fHighlightedDecorationsMap.put(annotation, pp);
							highlightAnnotationRangeStart= Math.min(highlightAnnotationRangeStart, pp.fPosition.offset);
							highlightAnnotationRangeEnd= Math.max(highlightAnnotationRangeEnd, pp.fPosition.offset + pp.fPosition.length);
						}
					}
				}
				
				updateHighlightRanges(highlightAnnotationRangeStart, highlightAnnotationRangeEnd, isWorldChange);
			}
		}
	}
	
	/**
	 * Updates the remembered highlight ranges.
	 * 
	 * @param highlightAnnotationRangeStart the start of the range
	 * @param highlightAnnotationRangeEnd	the end of the range
	 * @param isWorldChange					tells whether the range belongs to a annotation model event reporting a world change
	 * @since 3.0
	 */
	private void updateHighlightRanges(int highlightAnnotationRangeStart, int highlightAnnotationRangeEnd, boolean isWorldChange) {
		if (highlightAnnotationRangeStart != Integer.MAX_VALUE) {
			
			int maxRangeStart= highlightAnnotationRangeStart;
			int maxRangeEnd= highlightAnnotationRangeEnd;
			
			if (fTotalHighlightAnnotationRange != null) {
				maxRangeStart= Math.min(maxRangeStart, fTotalHighlightAnnotationRange.offset);
				maxRangeEnd= Math.max(maxRangeEnd, fTotalHighlightAnnotationRange.offset + fTotalHighlightAnnotationRange.length);
			}
			
			if (fTotalHighlightAnnotationRange == null)
				fTotalHighlightAnnotationRange= new Position(0);
			if (fCurrentHighlightAnnotationRange == null)
				fCurrentHighlightAnnotationRange= new Position(0);
				
			if (isWorldChange) {
				fTotalHighlightAnnotationRange.offset= highlightAnnotationRangeStart;
				fTotalHighlightAnnotationRange.length= highlightAnnotationRangeEnd - highlightAnnotationRangeStart;
				fCurrentHighlightAnnotationRange.offset= maxRangeStart;
				fCurrentHighlightAnnotationRange.length= maxRangeEnd - maxRangeStart;
			} else {
				fTotalHighlightAnnotationRange.offset= maxRangeStart;
				fTotalHighlightAnnotationRange.length= maxRangeEnd - maxRangeStart;
				fCurrentHighlightAnnotationRange.offset=highlightAnnotationRangeStart;
				fCurrentHighlightAnnotationRange.length= highlightAnnotationRangeEnd - highlightAnnotationRangeStart;
			}
		} else {
			if (isWorldChange) {
				fCurrentHighlightAnnotationRange= fTotalHighlightAnnotationRange;
				fTotalHighlightAnnotationRange= null;
			} else {
				fCurrentHighlightAnnotationRange= null;
			}
		}
		
		adaptToDocumentLength(fCurrentHighlightAnnotationRange);
		adaptToDocumentLength(fTotalHighlightAnnotationRange);
	}

	/**
	 * Adapts the given position to the document length.
	 * 
	 * @param position the position to adapt
	 * @since 3.0
	 */
	private void adaptToDocumentLength(Position position) {
		if (position == null)
			return;
		
		int length= fSourceViewer.getDocument().getLength();
		position.offset= Math.min(position.offset, length);
		position.length= Math.min(position.length, length - position.offset);
	}

	/**
	 * Returns a decoration for the given annotation if this
	 * annotation is valid and shown by this painter.
	 * 
	 * @param annotation 			the annotation
	 * @param decoration 			the decoration to be adapted and returned or <code>null</code> if a new one must be created
	 * @param isDrawingSquiggles	tells if squiggles should be drawn for this annotation
	 * @param isHighlighting		tells if this annotation should be highlighted
	 * @return the decoration or <code>null</code> if there's no valid one
	 * @since 3.0
	 */
	private Decoration getDecoration(Annotation annotation, Decoration decoration, boolean isDrawingSquiggles, boolean isHighlighting) {

		if (annotation.isMarkedDeleted())
			return null;

		Color color= null;

		if (isDrawingSquiggles || isHighlighting)
			color= findColor(annotation.getType());
		
		if (color == null)
			return null;
			
		Position position= fModel.getPosition(annotation);
		if (position == null || position.isDeleted())
			return null;
		
		if (decoration == null)
			decoration= new Decoration();
		
		decoration.fPosition= position;
		decoration.fColor= color;
		if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension extension= (IAnnotationAccessExtension) fAnnotationAccess;
			decoration.fLayer= extension.getLayer(annotation);
		} else {
			decoration.fLayer= IAnnotationAccessExtension.DEFAULT_LAYER;
		}
		decoration.fPainter= getDrawingStrategy(annotation);
		
		return decoration;
	}
	
	/**
	 * Returns the drawing type for the given annotation type.
	 * 
	 * @param annotationType the annotation type
	 * @return the annotation painter
	 * @since 3.0
	 */
	private IDrawingStrategy getDrawingStrategy(Annotation annotation) {
		String type= annotation.getType();
		IDrawingStrategy strategy = (IDrawingStrategy) fRegisteredDrawingStrategies.get(fAnnotationType2DrawingStrategyId.get(type));
		if (strategy != null)
			return strategy;
		
		if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension ext = (IAnnotationAccessExtension) fAnnotationAccess;
			Object[] sts = ext.getSupertypes(type);
			for (int i = 0; i < sts.length; i++) {
				strategy= (IDrawingStrategy) fRegisteredDrawingStrategies.get(fAnnotationType2DrawingStrategyId.get(sts[i]));
				if (strategy != null)
					return strategy;
			}
		}
		
		return fgNullDrawer;
		
	}
	
	/**
	 * Returns whether the given annotation type should be drawn.
	 * 
	 * @param annotationType the annotation type
	 * @return <code>true</code> if annotation type should be drawn, <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	private boolean shouldBeDrawn(Object annotationType) {
		return contains(annotationType, fAllowedAnnotationTypes, fConfiguredAnnotationTypes);
	}

	/**
	 * Returns whether the given annotation type should be highlighted.
	 * 
	 * @param annotationType the annotation type
	 * @return <code>true</code> if annotation type should be highlighted, <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	private boolean shouldBeHighlighted(Object annotationType) {
		return contains(annotationType, fAllowedHighlightAnnotationTypes, fConfiguredHighlightAnnotationTypes);
	}
	
	/**
	 * Returns whether the given annotation type is contained in the given <code>allowed</code>
	 * set. This is the case if the type is either in the set
	 * or covered by the <code>configured</code> set.
	 * 
	 * @param annotationType the annotation type
	 * @return <code>true</code> if annotation is contained, <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	private boolean contains(Object annotationType, Set allowed, Set configured) {
		if (allowed.contains(annotationType))
			return true;
		
		boolean covered= isCovered(annotationType, configured);
		if (covered)
			allowed.add(annotationType);
		
		return covered;
	}

	/**
	 * Computes whether the annotations of the given type are covered by the given <code>configured</code>
	 * set. This is the case if either the type of the annotation or any of its
	 * super types is contained in the <code>configured</code> set.
	 * 
	 * @param annotation the annotation
	 * @param annotationType the annotation type
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
	 * Returns the color for the given annotation type
	 * 
	 * @param annotationType the annotation type
	 * @return the color
	 */
	private Color findColor(Object annotationType) {
		Color color= (Color) fColorTable.get(annotationType);
		if (color != null)
			return color;
		
		if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension extension= (IAnnotationAccessExtension) fAnnotationAccess;
			Object[] superTypes= extension.getSupertypes(annotationType);
			if (superTypes != null) {
				for (int i= superTypes.length -1; i > -1; i--) {
					color= (Color) fColorTable.get(superTypes[i]);
					if (color != null)
						return color;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Recomputes the squiggles to be drawn and redraws them.
	 */
	private void updatePainting(AnnotationModelEvent event) {
		disablePainting(true);
		
		catchupWithModel(event);
		
		if (!fInputDocumentAboutToBeChanged)
			invalidateTextPresentation();
		
		enablePainting();
	}

	private void invalidateTextPresentation() {
	    if (fCurrentHighlightAnnotationRange== null)
	        return;
	    
		if (fSourceViewer instanceof ITextViewerExtension2) {
			IRegion r= new Region(fCurrentHighlightAnnotationRange.getOffset(), fCurrentHighlightAnnotationRange.getLength());

			if (DEBUG)
				System.out.println("AP: invalidating offset: " + r.getOffset() + ", length= " + r.getLength()); //$NON-NLS-1$ //$NON-NLS-2$
			
			((ITextViewerExtension2)fSourceViewer).invalidateTextPresentation(r.getOffset(), r.getLength());
		} else {
			fSourceViewer.invalidateTextPresentation();
		}
	}

	/*
	 * @see ITextPresentationListener#applyTextPresentation(TextPresentation)
	 * @since 3.0
	 */
	public synchronized void applyTextPresentation(TextPresentation tp) {

		if (fHighlightedDecorationsMap == null || fHighlightedDecorationsMap.isEmpty())
			return;

		IRegion region= tp.getExtent();
		
		if (DEBUG)
			System.out.println("AP: applying text presentation offset: " + region.getOffset() + ", length= " + region.getLength()); //$NON-NLS-1$ //$NON-NLS-2$

		for (int layer= 0, maxLayer= 1;	layer < maxLayer; layer++) {
			
			for (Iterator iter= fHighlightedDecorationsMap.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry= (Map.Entry)iter.next();
				
				Annotation a= (Annotation)entry.getKey();
				if (a.isMarkedDeleted())
					continue;
				
				Decoration pp = (Decoration)entry.getValue();
				
				maxLayer= Math.max(maxLayer, pp.fLayer + 1); // dynamically update layer maximum
				if (pp.fLayer != layer)	// wrong layer: skip annotation
					continue;
				
				Position p= pp.fPosition;
				if (!fSourceViewer.overlapsWithVisibleRegion(p.offset, p.length))
					continue;
	
				if (p.getOffset() + p.getLength() >= region.getOffset() && region.getOffset() + region.getLength() > p.getOffset())
					tp.mergeStyleRange(new StyleRange(p.getOffset(), p.getLength(), null, pp.fColor));
			}
		}
	}

	/*
	 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
	 */
	public synchronized void modelChanged(final IAnnotationModel model) {
		if (DEBUG)
			System.err.println("AP: OLD API of AnnotationModelListener called"); //$NON-NLS-1$

		modelChanged(new AnnotationModelEvent(model));
	}
	
	/*
	 * @see IAnnotationModelListenerExtension#modelChanged(AnnotationModelEvent)
	 */
	public synchronized void modelChanged(final AnnotationModelEvent event) {
		if (fTextWidget != null && !fTextWidget.isDisposed()) {
			if (fIsSettingModel) {
				// inside the ui thread -> no need for posting
				updatePainting(event);
			} else {
				Display d= fTextWidget.getDisplay();
				if (DEBUG && event != null && event.isWorldChange()) {
					System.out.println("AP: WORLD CHANGED, stack trace follows:"); //$NON-NLS-1$
					try {
						throw new Throwable();
					} catch (Throwable t) {
						t.printStackTrace(System.out);
					}
				}
				
				// TODO posting here is a problem for annotations that are being
				// removed and the positions of which are not updated to document
				// changes any more. If the document gets modified between
				// now and running the posted runnable, the position information
				// is not accurate any longer.
				if (d != null) {
					d.asyncExec(new Runnable() {
						public void run() {
							if (fTextWidget != null && !fTextWidget.isDisposed())
								updatePainting(event);
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
	 * annotations should be painted by this painter using squiggly drawing. If the annotation  type
	 * is already in this list, this method is without effect.
	 * 
	 * @param annotationType the annotation type
	 */
	public void addAnnotationType(Object annotationType) {
		addAnnotationType(annotationType, SQUIGGLES);
	}
	
	/**
	 * Adds the given annotation type to the list of annotation types whose
	 * annotations should be painted by this painter using the given drawing strategy. 
	 * If the annotation type is already in this list, the old drawing strategy gets replaced.
	 * 
	 * <p>TODO This is new API and subject to change. </p>
	 * 
	 * @param annotationType the annotation type
	 * @param drawingStrategyID the id of the drawing strategy that should be used for this annotation type
	 * @since 3.0
	 */
	public void addAnnotationType(Object annotationType, Object drawingStrategyID) {
		fConfiguredAnnotationTypes.add(annotationType);
		fAnnotationType2DrawingStrategyId.put(annotationType, drawingStrategyID);
	}
	
	/**
	 * Registers a new drawing strategy under the given ID. If there is already a
	 * strategy registered under <code>id</code>, the old strategy gets replaced.
	 * <p>The given id can be referenced when adding annotation types, see
	 * {@link #addAnnotationType(Object, Object)}.</p>
	 * 
	 * <p>TODO This is new API and subject to change. </p>
	 * 
	 * @param id the identifier under which the strategy can be referenced, not <code>null</code>
	 * @param strategy the new strategy
	 * @since 3.0
	 */
	public void addDrawingStrategy(Object id, IDrawingStrategy strategy) {
		// don't permit null as null is used to signal that an annotation type is not 
		// registered with a specific strategy, and that its annotation hierarchy should be searched
		if (id == null)
			throw new IllegalArgumentException();
		fRegisteredDrawingStrategies.put(id, strategy);
	}
	
	/**
	 * Adds the given annotation type to the list of annotation types whose
	 * annotations should be highlighted this painter. If the annotation  type
	 * is already in this list, this method is without effect.
	 * 
	 * @param annotationType the annotation type
	 * @since 3.0
	 */
	public void addHighlightAnnotationType(Object annotationType) {
		fConfiguredHighlightAnnotationTypes.add(annotationType);
		if (fTextInputListener == null) {
			fTextInputListener= new ITextInputListener() {
				/*
				 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
				 */
				public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
					fInputDocumentAboutToBeChanged= true;
				}
				/*
				 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
				 */
				public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
					fInputDocumentAboutToBeChanged= false;
				}
			};
			fSourceViewer.addTextInputListener(fTextInputListener);
		}
	}
	
	/**
	 * Removes the given annotation type from the list of annotation types whose
	 * annotations are painted by this painter. If the annotation type is not
	 * in this list, this method is wihtout effect.
	 * 
	 * @param annotationType the annotation type
	 */
	public void removeAnnotationType(Object annotationType) {
		fConfiguredAnnotationTypes.remove(annotationType);
		fAllowedAnnotationTypes.clear();
	}
	
	/**
	 * Removes the given annotation type from the list of annotation types whose
	 * annotations are highlighted by this painter. If the annotation type is not
	 * in this list, this method is wihtout effect.
	 * 
	 * @param annotationType the annotation type
	 * @since 3.0
	 */
	public void removeHighlightAnnotationType(Object annotationType) {
		fConfiguredHighlightAnnotationTypes.remove(annotationType);
		fAllowedHighlightAnnotationTypes.clear();
		if (fConfiguredHighlightAnnotationTypes.isEmpty() && fTextInputListener != null) {
			fSourceViewer.removeTextInputListener(fTextInputListener);
			fTextInputListener= null;
			fInputDocumentAboutToBeChanged= false;
		}
	}
	
	/**
	 * Clears the list of annotation types whose annotations are
	 * painted by this painter.
	 */
	public void removeAllAnnotationTypes() {
		fConfiguredAnnotationTypes.clear();
		fAllowedAnnotationTypes.clear();
		fConfiguredHighlightAnnotationTypes.clear();
		fAllowedHighlightAnnotationTypes.clear();
		if (fTextInputListener != null) {
			fSourceViewer.removeTextInputListener(fTextInputListener);
			fTextInputListener= null;
		}
	}
	
	/**
	 * Returns whether the list of annotation types whose annotations are painted
	 * by this painter contains at least on element.
	 * 
	 * @return <code>true</code> if there is an annotation type whose annotations are painted
	 */
	public boolean isPaintingAnnotations() {
		return !fConfiguredAnnotationTypes.isEmpty() || !fConfiguredHighlightAnnotationTypes.isEmpty();
	}
	
	/*
	 * @see IPainter#dispose()
	 */
	public void dispose() {
		
		if (fColorTable != null)	
			fColorTable.clear();
		fColorTable= null;
		
		if (fConfiguredAnnotationTypes != null)
			fConfiguredAnnotationTypes.clear();
		fConfiguredAnnotationTypes= null;
		
		if (fAllowedAnnotationTypes != null)
			fAllowedAnnotationTypes.clear();
		fAllowedAnnotationTypes= null;

		if (fConfiguredHighlightAnnotationTypes != null)
			fConfiguredHighlightAnnotationTypes.clear();
		fConfiguredHighlightAnnotationTypes= null;

		if (fAllowedHighlightAnnotationTypes != null)
			fAllowedHighlightAnnotationTypes.clear();
		fAllowedHighlightAnnotationTypes= null;
		
		fTextWidget= null;
		fSourceViewer= null;
		fAnnotationAccess= null;
		fModel= null;
		fDecorationsMap= null;
		fHighlightedDecorationsMap= null;
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

		for (int layer= 0, maxLayer= 1;	layer < maxLayer; layer++) {
			
			for (Iterator e = fDecorationsMap.entrySet().iterator(); e.hasNext();) {
				Map.Entry entry= (Map.Entry)e.next();
				
				Annotation a= (Annotation)entry.getKey();
				if (a.isMarkedDeleted())
					continue;
				
				Decoration pp = (Decoration)entry.getValue();
	
				maxLayer= Math.max(maxLayer, pp.fLayer + 1);	// dynamically update layer maximum
				if (pp.fLayer != layer)	// wrong layer: skip annotation
					continue;
				
				Position p= pp.fPosition;
				if (p.overlapsWith(vOffset, vLength)) {
					
					IDocument document= fSourceViewer.getDocument();
					try {
						
						int startLine= document.getLineOfOffset(p.getOffset()); 
						int lastInclusive= Math.max(p.getOffset(), p.getOffset() + p.getLength() - 1);
						int endLine= document.getLineOfOffset(lastInclusive);
						
						for (int i= startLine; i <= endLine; i++) {
							IRegion line= document.getLineInformation(i);
							int paintStart= Math.max(line.getOffset(), p.getOffset());
							int paintEnd= Math.min(line.getOffset() + line.getLength(), p.getOffset() + p.getLength());
							if (paintEnd >= paintStart) {
								// otherwise inside a line delimiter
								IRegion widgetRange= getWidgetRange(new Position(paintStart, paintEnd - paintStart));
								if (widgetRange != null)
									pp.fPainter.draw(a, gc, fTextWidget, widgetRange.getOffset(), widgetRange.getLength(), pp.fColor);
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
		if (p == null || p.offset == Integer.MAX_VALUE)
			return null;
		
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
	
	/*
	 * @see IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			disablePainting(redraw);
			setModel(null);
			catchupWithModel(null);
		}
	}
	
	/**
	 * Returns whether the given reason causes a repaint.
	 * 
	 * @param reason the reason
	 * @return <code>true</code> if repaint reason, <code>false</code> otherwise
	 * @since 3.0
	 */
	protected boolean isRepaintReason(int reason) {
		return CONFIGURATION == reason || INTERNAL == reason;
	}
	
	/**
     * Retrieves the annotation model from the given source viewer.
	 * @since 3.0
	 */
	protected IAnnotationModel findAnnotationModel(ISourceViewer sourceViewer) {
		if(sourceViewer != null)
			return sourceViewer.getAnnotationModel();
		return null;
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
			IAnnotationModel model= findAnnotationModel(fSourceViewer);
			if (model != null) {
				fIsActive= true;
				setModel(model);
			}
		} else if (isRepaintReason(reason))
			updatePainting(null);
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.text.IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
	}
}
