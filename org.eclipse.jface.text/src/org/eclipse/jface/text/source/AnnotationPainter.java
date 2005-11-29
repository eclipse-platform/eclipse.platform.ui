/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;


/**
 * Paints decorations for annotations provided by an annotation model and/or
 * highlights them in the associated source viewer.
 * <p>
 * The annotation painter can be configured with drawing strategies. A drawing
 * strategy defines the visual presentation of a particular type of annotation
 * decoration.</p>
 * <p>
 * Clients usually instantiate and configure objects of this class.</p>
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
		 * given color onto the specified GC.
		 *
		 * @param annotation the annotation to be drawn
		 * @param gc the graphical context
		 * @param textWidget the text widget to draw on
		 * @param offset the offset of the line
		 * @param length the length of the line
		 * @param color the color of the line
		 */
		void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color);
	}

	/**
	 * Squiggles drawing strategy.
	 *
	 * @since 3.0
	 */
	public static class SquigglesStrategy implements IDrawingStrategy {

		/*
		 * @see org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy#draw(org.eclipse.jface.text.source.Annotation, org.eclipse.swt.graphics.GC, org.eclipse.swt.custom.StyledText, int, int, org.eclipse.swt.graphics.Color)
		 * @since 3.0
		 */
		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
			if (gc != null) {

				if (length < 1)
					return;

				Point left= textWidget.getLocationAtOffset(offset);
				Point right= textWidget.getLocationAtOffset(offset + length);

				gc.setForeground(color);
				int[] polyline= computePolyline(left, right, textWidget.getBaseline(offset), textWidget.getLineHeight(offset));
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
		 * @param baseline the font's baseline
		 * @param lineHeight the height of the line
		 * @return the array of alternating x and y values which are the corners of the squiggly line
		 */
		private int[] computePolyline(Point left, Point right, int baseline, int lineHeight) {

			final int WIDTH= 4; // must be even
			final int HEIGHT= 2; // can be any number

			int peaks= (right.x - left.x) / WIDTH;
			if (peaks == 0 && right.x - left.x > 2)
				peaks= 1;

			int leftX= left.x;

			// compute (number of point) * 2
			int length= ((2 * peaks) + 1) * 2;
			if (length < 0)
				return new int[0];

			int[] coordinates= new int[length];

			// cache peeks' y-coordinates
			int top= left.y + Math.min(baseline + 1, lineHeight - HEIGHT - 1);
			int bottom= top + HEIGHT;

			// populate array with peek coordinates
			for (int i= 0; i < peaks; i++) {
				int index= 4 * i;
				coordinates[index]= leftX + (WIDTH * i);
				coordinates[index+1]= bottom;
				coordinates[index+2]= coordinates[index] + WIDTH/2;
				coordinates[index+3]= top;
			}

			// the last down flank is missing
			coordinates[length-2]= Math.min(Math.max(0, right.x - 1), left.x + (WIDTH * peaks));
			coordinates[length-1]= bottom;

			return coordinates;
		}
	}

	/**
	 * Drawing strategy that does nothing.
	 *
	 * @since 3.0
	 */
	public static final class NullStrategy implements IDrawingStrategy {

		/*
		 * @see org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy#draw(org.eclipse.jface.text.source.Annotation, org.eclipse.swt.graphics.GC, org.eclipse.swt.custom.StyledText, int, int, org.eclipse.swt.graphics.Color)
		 * @since 3.0
		 */
		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
			// do nothing
		}
	}

	/**
	 * Implementation of <code>IRegion</code> that can be reused
	 * by setting the offset and the length.
	 */
	private static class ReusableRegion extends Position implements IRegion {}

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
	private volatile boolean  fIsSettingModel= false;
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
	/**
	 * Mutex for highlighted decorations map.
	 * @since 3.0
	 */
	private Object fDecorationMapLock= new Object();
	/**
	 * Mutex for for decorations map.
	 * @since 3.0
	 */
	private Object fHighlightedDecorationsMapLock= new Object();
	/** The internal color table */
	private Map fColorTable= new HashMap();
	/**
	 * The list of configured annotation types for being painted by this painter.
	 * @since 3.0
	 */
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
	 * Maps annotation types to drawing strategy identifiers.
	 * @since 3.0
	 */
	private Map fAnnotationType2DrawingStrategyId= new HashMap();
	/**
	 * Maps drawing strategy identifiers to drawing strategies.
	 * @since 3.0
	 */
	private Map fRegisteredDrawingStrategies= new HashMap();


	/**
	 * Creates a new annotation painter for the given source viewer and with the
	 * given annotation access. The painter is not initialized, i.e. no
	 * annotation types are configured to be painted.
	 *
	 * @param sourceViewer the source viewer for this painter
	 * @param access the annotation access for this painter
	 */
	public AnnotationPainter(ISourceViewer sourceViewer, IAnnotationAccess access) {
		fSourceViewer= sourceViewer;
		fAnnotationAccess= access;
		fTextWidget= sourceViewer.getTextWidget();

		// default drawing strategies: squiggles were the only decoration style before version 3.0
		fRegisteredDrawingStrategies.put(SQUIGGLES, fgSquigglyDrawer);
	}

	/**
	 * Returns whether this painter has to draw any squiggles.
	 *
	 * @return <code>true</code> if there are squiggles to be drawn, <code>false</code> otherwise
	 */
	private boolean hasDecorations() {
		synchronized (fDecorationMapLock) {
			return !fDecorationsMap.isEmpty();
		}
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
	 *
	 * @param event the annotation model event
	 */
	private void catchupWithModel(AnnotationModelEvent event) {

		synchronized (fDecorationMapLock) {
			if (fDecorationsMap == null)
				return;
		}

		int highlightAnnotationRangeStart= Integer.MAX_VALUE;
		int highlightAnnotationRangeEnd= -1;

		if (fModel != null) {

			Map decorationsMap;
			Map highlightedDecorationsMap;

			// Clone decoration maps
			synchronized (fDecorationMapLock) {
				decorationsMap= new HashMap(fDecorationsMap);
			}
			synchronized (fHighlightedDecorationsMapLock) {
				highlightedDecorationsMap= new HashMap(fHighlightedDecorationsMap);
			}

			boolean isWorldChange= false;

			Iterator e;
			if (event == null || event.isWorldChange()) {
				isWorldChange= true;

				if (DEBUG && event == null)
					System.out.println("AP: INTERNAL CHANGE"); //$NON-NLS-1$

				decorationsMap.clear();
				highlightedDecorationsMap.clear();

				e= fModel.getAnnotationIterator();


			} else {

				// Remove annotations
				Annotation[] removedAnnotations= event.getRemovedAnnotations();
				for (int i=0, length= removedAnnotations.length; i < length; i++) {
					Annotation annotation= removedAnnotations[i];
					Decoration decoration= (Decoration)highlightedDecorationsMap.remove(annotation);
					if (decoration != null) {
						Position position= decoration.fPosition;
						if (position != null) {
							highlightAnnotationRangeStart= Math.min(highlightAnnotationRangeStart, position.offset);
							highlightAnnotationRangeEnd= Math.max(highlightAnnotationRangeEnd, position.offset + position.length);
						}
					}
					decorationsMap.remove(annotation);
				}

				// Update existing annotations
				Annotation[] changedAnnotations= event.getChangedAnnotations();
				for (int i=0, length= changedAnnotations.length; i < length; i++) {
					Annotation annotation= changedAnnotations[i];

					Object annotationType= annotation.getType();
					boolean isHighlighting=  shouldBeHighlighted(annotationType);
					boolean isDrawingSquiggles= shouldBeDrawn(annotationType);

					Decoration decoration= (Decoration)highlightedDecorationsMap.get(annotation);

					if (decoration != null) {
						// The call below updates the decoration - no need to create new decoration
						decoration= getDecoration(annotation, decoration, isDrawingSquiggles, isHighlighting);
						if (decoration == null)
							highlightedDecorationsMap.remove(annotation);
					} else {
						decoration= getDecoration(annotation, decoration, isDrawingSquiggles, isHighlighting);
						if (decoration != null && isHighlighting)
							highlightedDecorationsMap.put(annotation, decoration);
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
						highlightedDecorationsMap.remove(annotation);
					}

					Decoration oldDecoration= (Decoration)decorationsMap.get(annotation);
					if (decoration != null && isDrawingSquiggles)
						decorationsMap.put(annotation, decoration);
					else if (oldDecoration != null)
						decorationsMap.remove(annotation);
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
						decorationsMap.put(annotation, pp);

					if (isHighlighting) {
						highlightedDecorationsMap.put(annotation, pp);
						highlightAnnotationRangeStart= Math.min(highlightAnnotationRangeStart, pp.fPosition.offset);
						highlightAnnotationRangeEnd= Math.max(highlightAnnotationRangeEnd, pp.fPosition.offset + pp.fPosition.length);
					}
				}
			}

			synchronized (fDecorationMapLock) {
				fDecorationsMap= decorationsMap;
			}

			synchronized (fHighlightedDecorationsMapLock) {
				fHighlightedDecorationsMap= highlightedDecorationsMap;
				updateHighlightRanges(highlightAnnotationRangeStart, highlightAnnotationRangeEnd, isWorldChange);
			}
		} else {
			// annotation model is null -> clear all
			synchronized (fDecorationMapLock) {
				fDecorationsMap.clear();
			}
			synchronized (fHighlightedDecorationsMapLock) {
				fHighlightedDecorationsMap.clear();
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
	 * Returns the drawing type for the given annotation.
	 *
	 * @param annotation the annotation
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
			for (int i= 0; i < sts.length; i++) {
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
	 * @param allowed set with allowed annotation types
	 * @param configured set with configured annotation types
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
	 * @param annotationType the annotation type
	 * @param configured set with configured annotation types
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
	 * @since 3.0
	 */
	private Color findColor(Object annotationType) {
		Color color= (Color) fColorTable.get(annotationType);
		if (color != null)
			return color;

		if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension extension= (IAnnotationAccessExtension) fAnnotationAccess;
			Object[] superTypes= extension.getSupertypes(annotationType);
			if (superTypes != null) {
				for (int i= 0; i < superTypes.length; i++) {
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
	 *
	 * @param event the annotation model event
	 * @since 3.0
	 */
	private void updatePainting(AnnotationModelEvent event) {
		disablePainting(true);

		catchupWithModel(event);

		if (!fInputDocumentAboutToBeChanged)
			invalidateTextPresentation();

		enablePainting();
	}

	private void invalidateTextPresentation() {
		IRegion r= null;
		synchronized (fHighlightedDecorationsMapLock) {
		    if (fCurrentHighlightAnnotationRange != null)
		    	r= new Region(fCurrentHighlightAnnotationRange.getOffset(), fCurrentHighlightAnnotationRange.getLength());
		}
		if (r == null)
			return;

		if (fSourceViewer instanceof ITextViewerExtension2) {
			if (DEBUG)
				System.out.println("AP: invalidating offset: " + r.getOffset() + ", length= " + r.getLength()); //$NON-NLS-1$ //$NON-NLS-2$

			((ITextViewerExtension2)fSourceViewer).invalidateTextPresentation(r.getOffset(), r.getLength());

		} else {
			fSourceViewer.invalidateTextPresentation();
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextPresentationListener#applyTextPresentation(org.eclipse.jface.text.TextPresentation)
	 * @since 3.0
	 */
	public void applyTextPresentation(TextPresentation tp) {
		Set decorations;

		synchronized (fHighlightedDecorationsMapLock) {
			if (fHighlightedDecorationsMap == null || fHighlightedDecorationsMap.isEmpty())
				return;

			decorations= new HashSet(fHighlightedDecorationsMap.entrySet());
		}

		IRegion region= tp.getExtent();

		if (DEBUG)
			System.out.println("AP: applying text presentation offset: " + region.getOffset() + ", length= " + region.getLength()); //$NON-NLS-1$ //$NON-NLS-2$

		for (int layer= 0, maxLayer= 1;	layer < maxLayer; layer++) {

			for (Iterator iter= decorations.iterator(); iter.hasNext();) {
				Map.Entry entry= (Map.Entry)iter.next();

				Annotation a= (Annotation)entry.getKey();
				if (a.isMarkedDeleted())
					continue;

				Decoration pp = (Decoration)entry.getValue();

				maxLayer= Math.max(maxLayer, pp.fLayer + 1); // dynamically update layer maximum
				if (pp.fLayer != layer)	// wrong layer: skip annotation
					continue;

				Position p= pp.fPosition;
				if (fSourceViewer instanceof ITextViewerExtension5) {
					ITextViewerExtension5 extension3= (ITextViewerExtension5) fSourceViewer;
					if (null == extension3.modelRange2WidgetRange(new Region(p.getOffset(), p.getLength())))
						continue;
				} else if (!fSourceViewer.overlapsWithVisibleRegion(p.offset, p.length)) {
					continue;
				}

				int regionEnd= region.getOffset() + region.getLength();
				int pEnd= p.getOffset() + p.getLength();
				if (pEnd >= region.getOffset() && regionEnd > p.getOffset()) {
					int start= Math.max(p.getOffset(), region.getOffset());
					int end= Math.min(regionEnd, pEnd);
					int length= Math.max(end - start, 0);
					tp.mergeStyleRange(new StyleRange(start, length, null, pp.fColor));
				}
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModelListener#modelChanged(org.eclipse.jface.text.source.IAnnotationModel)
	 */
	public synchronized void modelChanged(final IAnnotationModel model) {
		if (DEBUG)
			System.err.println("AP: OLD API of AnnotationModelListener called"); //$NON-NLS-1$

		modelChanged(new AnnotationModelEvent(model));
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModelListenerExtension#modelChanged(org.eclipse.jface.text.source.AnnotationModelEvent)
	 */
	public void modelChanged(final AnnotationModelEvent event) {
		if (fTextWidget != null && !fTextWidget.isDisposed()) {
			if (fIsSettingModel) {
				// inside the UI thread -> no need for posting
				 if (fTextWidget.getDisplay() == Display.getCurrent())
				 	updatePainting(event);
				 else {
				 	/*
				 	 * we can throw away the changes since
				 	 * further update painting will happen
				 	 */
				 	return;
				 }
			} else {
				Display d= fTextWidget.getDisplay();
				if (DEBUG && event != null && event.isWorldChange()) {
					System.out.println("AP: WORLD CHANGED, stack trace follows:"); //$NON-NLS-1$
					new Throwable().printStackTrace(System.out);
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
	 * in this list, this method is without effect.
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
	 * in this list, this method is without effect.
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
	 * @see org.eclipse.jface.text.IPainter#dispose()
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
		synchronized (fDecorationMapLock) {
			fDecorationsMap= null;
		}
		synchronized (fHighlightedDecorationsMapLock) {
			fHighlightedDecorationsMap= null;
		}
	}

	/**
	 * Returns the document offset of the upper left corner of the source viewer's view port,
	 * possibly including partially visible lines.
	 *
	 * @return the document offset if the upper left corner of the view port
	 */
	private int getInclusiveTopIndexStartOffset() {

		if (fTextWidget != null && !fTextWidget.isDisposed()) {
			int top= -1;
			if (fSourceViewer instanceof ITextViewerExtension5) {
				top= fTextWidget.getTopIndex();
				if ((fTextWidget.getTopPixel() % fTextWidget.getLineHeight()) != 0)
					top--;
				ITextViewerExtension5 extension= (ITextViewerExtension5) fSourceViewer;
				top= extension.widgetLine2ModelLine(top);
			} else {
				top= fSourceViewer.getTopIndex();
				if ((fTextWidget.getTopPixel() % fTextWidget.getLineHeight()) != 0)
					top--;
			}

			try {
				IDocument document= fSourceViewer.getDocument();
				return document.getLineOffset(top);
			} catch (BadLocationException x) {
			}
		}

		return -1;
	}

	/**
	 * Returns the first invisible document offset of the lower right corner of the source viewer's view port,
	 * possibly including partially visible lines.
	 *
	 * @return the first invisible document offset of the lower right corner of the view port
	 */
	private int getExclusiveBottomIndexEndOffset() {

		if (fTextWidget != null && !fTextWidget.isDisposed()) {
			int bottom= fSourceViewer.getBottomIndex();
			if (((fTextWidget.getTopPixel() + fTextWidget.getClientArea().height) % fTextWidget.getLineHeight()) != 0)
				bottom++;
			try {
				IDocument document= fSourceViewer.getDocument();

				if (bottom >= document.getNumberOfLines())
					bottom= document.getNumberOfLines() - 1;

				return document.getLineOffset(bottom) + document.getLineLength(bottom);
			} catch (BadLocationException x) {
			}
		}

		return -1;
	}

	/*
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent event) {
		if (fTextWidget != null)
			handleDrawRequest(event);
	}

	/**
	 * Handles the request to draw the annotations using the given graphical context.
	 *
	 * @param event the paint event or <code>null</code>
	 */
	private void handleDrawRequest(PaintEvent event) {

		if (fTextWidget == null) {
			// is already disposed
			return;
		}

		IRegion clippingRegion= computeClippingRegion(event);
		if (clippingRegion == null)
			return;
		
		int vOffset= clippingRegion.getOffset();
		int vLength= clippingRegion.getLength();
		
		final GC gc= event != null ? event.gc : null;

		// Clone decorations
		Collection decorations;
		synchronized (fDecorationMapLock) {
			decorations= new ArrayList(fDecorationsMap.size());
			decorations.addAll(fDecorationsMap.entrySet());
		}

		/*
		 * Create a new list of annotations to be drawn, since removing from decorations is more
		 * expensive. One bucket per drawing layer. Use linked lists as addition is cheap here.
		 */
		ArrayList toBeDrawn= new ArrayList(10);
		for (Iterator e = decorations.iterator(); e.hasNext();) {
			Map.Entry entry= (Map.Entry)e.next();
			
			Annotation a= (Annotation)entry.getKey();
			Decoration pp = (Decoration)entry.getValue();
			// prune any annotation that is not drawable or does not need drawing
			if (!(a.isMarkedDeleted() || pp.fPainter == fgNullDrawer || pp.fPainter instanceof NullStrategy || skip(a) || !pp.fPosition.overlapsWith(vOffset, vLength))) {
				// ensure sized appropriately
				for (int i= toBeDrawn.size(); i <= pp.fLayer; i++)
					toBeDrawn.add(new LinkedList());
				((List) toBeDrawn.get(pp.fLayer)).add(entry);
			}
		}
		
		ReusableRegion range= new ReusableRegion();
		for (Iterator it= toBeDrawn.iterator(); it.hasNext();) {
			List layer= (List) it.next();

			for (Iterator e = layer.iterator(); e.hasNext();) {
				Map.Entry entry= (Map.Entry)e.next();

				Decoration pp = (Decoration)entry.getValue();
				Position p= pp.fPosition;
				IDocument document= fSourceViewer.getDocument();
				try {

					int startLine= document.getLineOfOffset(p.getOffset());
					int lastInclusive= Math.max(p.getOffset(), p.getOffset() + p.getLength() - 1);
					int endLine= document.getLineOfOffset(lastInclusive);

					for (int i= startLine; i <= endLine; i++) {
						int lineOffset= document.getLineOffset(i);
						int paintStart= Math.max(lineOffset, p.getOffset());
						String lineDelimiter= document.getLineDelimiter(i);
						int delimiterLength= lineDelimiter != null ? lineDelimiter.length() : 0;
						int paintLength= Math.min(lineOffset + document.getLineLength(i) - delimiterLength, p.getOffset() + p.getLength()) - paintStart;
						if (paintLength >= 0 && overlapsWith(paintStart, paintLength, vOffset, vLength)) {
							// otherwise inside a line delimiter
							range.setOffset(paintStart);
							range.setLength(paintLength);
							IRegion widgetRange= getWidgetRange(range);
							if (widgetRange != null) {
								Annotation a= (Annotation)entry.getKey();
								pp.fPainter.draw(a, gc, fTextWidget, widgetRange.getOffset(), widgetRange.getLength(), pp.fColor);
							}
						}
					}

				} catch (BadLocationException x) {
				}
			}
		}
	}
	
	/**
	 * Computes the model (document) region that is covered by the paint event's clipping region. If
	 * <code>event</code> is <code>null</code>, the model range covered by the visible editor
	 * area (viewport) is returned.
	 * 
	 * @param event the paint event or <code>null</code> to use the entire viewport
	 * @return the model region comprised by either the paint event's clipping region or the
	 *         viewport
	 * @since 3.2
	 */
	private IRegion computeClippingRegion(PaintEvent event) {
		if (event == null) {
			// trigger a repaint of the entire viewport
			int vOffset= getInclusiveTopIndexStartOffset();
			if (vOffset == -1)
				return null;
			
			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=17147
			int vLength= getExclusiveBottomIndexEndOffset() - vOffset;
			
			return new Region(vOffset, vLength);
		}
		
		int widgetOffset;
		try {
			int widgetClippingStartOffset= fTextWidget.getOffsetAtLocation(new Point(0, event.y));
			int firstWidgetLine= fTextWidget.getLineAtOffset(widgetClippingStartOffset);
			widgetOffset= fTextWidget.getOffsetAtLine(firstWidgetLine);
		} catch (IllegalArgumentException x) {
			// should not happen
			widgetOffset= 0;
		}
		
		int widgetEndOffset;
		try {
			int widgetClippingEndOffset= fTextWidget.getOffsetAtLocation(new Point(0, event.y + event.height));
			int lastWidgetLine= fTextWidget.getLineAtOffset(widgetClippingEndOffset);
			widgetEndOffset= fTextWidget.getOffsetAtLine(lastWidgetLine + 1);
		} catch (IllegalArgumentException x) {
			// happens if the editor is not "full", eg. the last line of the document is visible in the editor
			// in that case, simply use the last character
			widgetEndOffset= fTextWidget.getCharCount();
		}
		
		IRegion clippingRegion= getModelRange(widgetOffset, widgetEndOffset - widgetOffset);
		
		return clippingRegion;
	}

	/**
	 * Should the given annotation be skipped when handling draw requests?
	 *
	 * @param annotation the annotation
	 * @return <code>true</code> iff the given annotation should be
	 *         skipped when handling draw requests
	 * @since 3.0
	 */
	protected boolean skip(Annotation annotation) {
		return false;
	}

	/**
	 * Returns the widget region that corresponds to the given region in the
	 * viewer's document.
	 *
	 * @param p the region in the viewer's document
	 * @return the corresponding widget region
	 */
	private IRegion getWidgetRange(IRegion p) {
		if (p == null || p.getOffset() == Integer.MAX_VALUE)
			return null;

		if (fSourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fSourceViewer;
			return extension.modelRange2WidgetRange(p);
		}

		IRegion region= fSourceViewer.getVisibleRegion();
		int offset= region.getOffset();
		int length= region.getLength();

		if (overlapsWith(p, region)) {
			int p1= Math.max(offset, p.getOffset());
			int p2= Math.min(offset + length, p.getOffset() + p.getLength());
			return new Region(p1 - offset, p2 - p1);
		}
		return null;
	}

	/**
	 * Returns the model region that corresponds to the given region in the
	 * viewer's text widget.
	 *
	 * @param offset the offset in the viewer's widget 
	 * @param length the length in the viewer's widget
	 * @return the corresponding document region
	 * @since 3.2
	 */
	private IRegion getModelRange(int offset, int length) {
		if (offset == Integer.MAX_VALUE)
			return null;

		if (fSourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fSourceViewer;
			return extension.widgetRange2ModelRange(new Region(offset, length));
		}
		
		IRegion region= fSourceViewer.getVisibleRegion();
		return new Region(region.getOffset() + offset, length);
	}
	
	/**
	 * Checks whether the intersection of the given text ranges
	 * is empty or not.
	 *
	 * @param range1 the first range to check
	 * @param range2 the second range to check
	 * @return <code>true</code> if intersection is not empty
	 */
	private boolean overlapsWith(IRegion range1, IRegion range2) {
		return overlapsWith(range1.getOffset(), range1.getLength(), range2.getOffset(), range2.getLength());
	}

	/**
	 * Checks whether the intersection of the given text ranges
	 * is empty or not.
	 *
	 * @param offset1 offset of the first range
	 * @param length1 length of the first range
	 * @param offset2 offset of the second range
	 * @param length2 length of the second range
	 * @return <code>true</code> if intersection is not empty
	 */
	private boolean overlapsWith(int offset1, int length1, int offset2, int length2) {
		int end= offset2 + length2;
		int thisEnd= offset1 + length1;

		if (length2 > 0) {
			if (length1 > 0)
				return offset1 < end && offset2 < thisEnd;
			return  offset2 <= offset1 && offset1 < end;
		}

		if (length1 > 0)
			return offset1 <= offset2 && offset2 < thisEnd;
		return offset1 == offset2;
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#deactivate(boolean)
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
     *
     * @param sourceViewer the source viewer
     * @return the source viewer's annotation model or <code>null</code> if none can be found
	 * @since 3.0
	 */
	protected IAnnotationModel findAnnotationModel(ISourceViewer sourceViewer) {
		if(sourceViewer != null)
			return sourceViewer.getAnnotationModel();
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#paint(int)
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
