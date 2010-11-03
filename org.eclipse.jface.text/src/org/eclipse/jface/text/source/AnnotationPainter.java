/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink <sw@wahlbrink.eu> - Annotations not painted when length is 0 / at document end - http://bugs.eclipse.org/bugs/show_bug.cgi?id=227534
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.JFaceTextUtil;
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
	 * A drawing strategy draws the decoration for an annotation onto the text widget.
	 *
	 * @since 3.0
	 */
	public interface IDrawingStrategy {
		/**
		 * Draws a decoration for an annotation onto the specified GC at the given text range. There
		 * are two different invocation modes of the <code>draw</code> method:
		 * <ul>
		 * <li><strong>drawing mode:</strong> the passed GC is the graphics context of a paint
		 * event occurring on the text widget. The strategy should draw the decoration onto the
		 * graphics context, such that the decoration appears at the given range in the text
		 * widget.</li>
		 * <li><strong>clearing mode:</strong> the passed GC is <code>null</code>. In this case
		 * the strategy must invalidate enough of the text widget's client area to cover any
		 * decoration drawn in drawing mode. This can usually be accomplished by calling
		 * {@linkplain StyledText#redrawRange(int, int, boolean) textWidget.redrawRange(offset, length, true)}.</li>
		 * </ul>
		 *
		 * @param annotation the annotation to be drawn
		 * @param gc the graphics context, <code>null</code> when in clearing mode
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
	 * @deprecated As of 3.4, replaced by {@link AnnotationPainter.UnderlineStrategy}
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
				Rectangle rect= textWidget.getTextBounds(offset, offset + length - 1);
				left.x= rect.x;
				right.x= rect.x + rect.width;

				int[] polyline= computePolyline(left, right, textWidget.getBaseline(offset), textWidget.getLineHeight(offset));

				gc.setLineWidth(0); // NOTE: 0 means width is 1 but with optimized performance
				gc.setLineStyle(SWT.LINE_SOLID);
				gc.setForeground(color);
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
	 * A text style painting strategy draws the decoration for an annotation
	 * onto the text widget by applying a {@link TextStyle} on a given
	 * {@link StyleRange}.
	 *
	 * @since 3.4
	 */
	public interface ITextStyleStrategy {

		/**
		 * Applies a text style on the given <code>StyleRange</code>.
		 *
		 * @param styleRange the style range on which to apply the text style
		 * @param annotationColor the color of the annotation
		 */
		void applyTextStyle(StyleRange styleRange, Color annotationColor);
	}


	/**
	 * @since 3.4
	 */
	public static final class HighlightingStrategy implements ITextStyleStrategy {
		public void applyTextStyle(StyleRange styleRange, Color annotationColor) {
			styleRange.background= annotationColor;
		}
	}


	/**
	 * Underline text style strategy.
	 *
	 * @since 3.4
	 */
	public static final class UnderlineStrategy implements ITextStyleStrategy {

		int fUnderlineStyle;

		public UnderlineStrategy(int style) {
			Assert.isLegal(style == SWT.UNDERLINE_SINGLE || style == SWT.UNDERLINE_DOUBLE || style == SWT.UNDERLINE_ERROR || style == SWT.UNDERLINE_SQUIGGLE);
			fUnderlineStyle= style;
		}

		public void applyTextStyle(StyleRange styleRange, Color annotationColor) {
			styleRange.underline= true;
			styleRange.underlineStyle= fUnderlineStyle;
			styleRange.underlineColor= annotationColor;
		}
	}


	/**
	 * Box text style strategy.
	 *
	 * @since 3.4
	 */
	public static final class BoxStrategy implements ITextStyleStrategy {

		int fBorderStyle;

		public BoxStrategy(int style) {
			Assert.isLegal(style == SWT.BORDER_DASH || style == SWT.BORDER_DOT || style == SWT.BORDER_SOLID);
			fBorderStyle= style;
		}

		public void applyTextStyle(StyleRange styleRange, Color annotationColor) {
			styleRange.borderStyle= fBorderStyle;
			styleRange.borderColor= annotationColor;
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
	private static final IDrawingStrategy SQUIGGLES_STRATEGY= new SquigglesStrategy();

	/**
	 * This strategy is used to mark the <code>null</code> value in the chache
	 * maps.
	 *
	 * @since 3.4
	 */
	private static final IDrawingStrategy NULL_STRATEGY= new NullStrategy();
	/**
	 * The squiggles painter id.
	 * @since 3.0
	 */
	private static final Object SQUIGGLES= new Object();
	/**
	 * The squiggly painter strategy.
	 *
	 * @since 3.4
	 */
	private static final ITextStyleStrategy HIGHLIGHTING_STRATEGY= new HighlightingStrategy();

	/**
	 * The highlighting text style strategy id.
	 *
	 * @since 3.4
	 */
	private static final Object HIGHLIGHTING= new Object();

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
		 * The painting strategy for this decoration.
		 * @since 3.0
		 */
		private Object fPaintingStrategy;
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
	/**
	 * Maps an annotation type to its registered color.
	 *
	 * @see #setAnnotationTypeColor(Object, Color)
	 */
	private Map fAnnotationType2Color= new HashMap();

	/**
	 * Cache that maps the annotation type to its color.
	 * @since 3.4
	 */
	private Map fCachedAnnotationType2Color= new HashMap();
	/**
	 * The range in which the current highlight annotations can be found.
	 * @since 3.0
	 */
	private Position fCurrentHighlightAnnotationRange= null;
	/**
	 * The range in which all added, removed and changed highlight
	 * annotations can be found since the last world change.
	 * @since 3.0
	 */
	private Position fTotalHighlightAnnotationRange= null;
	/**
	 * The range in which the currently drawn annotations can be found.
	 * @since 3.3
	 */
	private Position fCurrentDrawRange= null;
	/**
	 * The range in which all added, removed and changed drawn
	 * annotations can be found since the last world change.
	 * @since 3.3
	 */
	private Position fTotalDrawRange= null;
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
	 * Maps annotation types to painting strategy identifiers.
	 *
	 * @see #addAnnotationType(Object, Object)
	 * @since 3.0
	 */
	private Map fAnnotationType2PaintingStrategyId= new HashMap();
	/**
	 * Maps annotation types to painting strategy identifiers.
	 * @since 3.4
	 */
	private Map fCachedAnnotationType2PaintingStrategy= new HashMap();

	/**
	 * Maps painting strategy identifiers to painting strategies.
	 *
	 * @since 3.0
	 */
	private Map fPaintingStrategyId2PaintingStrategy= new HashMap();

	/**
	 * Reuse this region for performance reasons.
	 * @since 3.3
	 */
	private ReusableRegion fReusableRegion= new ReusableRegion();

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
		fPaintingStrategyId2PaintingStrategy.put(SQUIGGLES, SQUIGGLES_STRATEGY);
		fPaintingStrategyId2PaintingStrategy.put(HIGHLIGHTING, HIGHLIGHTING_STRATEGY);
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

		if (fModel == null) {
			// annotation model is null -> clear all
			synchronized (fDecorationMapLock) {
				fDecorationsMap.clear();
			}
			synchronized (fHighlightedDecorationsMapLock) {
				fHighlightedDecorationsMap.clear();
			}
			return;
		}

		IRegion clippingRegion= computeClippingRegion(null, true);
		IDocument document= fSourceViewer.getDocument();

		int highlightAnnotationRangeStart= Integer.MAX_VALUE;
		int highlightAnnotationRangeEnd= -1;

		int drawRangeStart= Integer.MAX_VALUE;
		int drawRangeEnd= -1;

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

			Iterator iter= decorationsMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry= (Map.Entry)iter.next();
				Annotation annotation= (Annotation)entry.getKey();
				Decoration decoration= (Decoration)entry.getValue();
				drawDecoration(decoration, null, annotation, clippingRegion, document);
			}

			decorationsMap.clear();

			highlightedDecorationsMap.clear();

			e= fModel.getAnnotationIterator();


		} else {

			// Remove annotations
			Annotation[] removedAnnotations= event.getRemovedAnnotations();
			for (int i= 0, length= removedAnnotations.length; i < length; i++) {
				Annotation annotation= removedAnnotations[i];
				Decoration decoration= (Decoration)highlightedDecorationsMap.remove(annotation);
				if (decoration != null) {
					Position position= decoration.fPosition;
					if (position != null) {
						highlightAnnotationRangeStart= Math.min(highlightAnnotationRangeStart, position.offset);
						highlightAnnotationRangeEnd= Math.max(highlightAnnotationRangeEnd, position.offset + position.length);
					}
				}
				decoration= (Decoration)decorationsMap.remove(annotation);
				if (decoration != null) {
					drawDecoration(decoration, null, annotation, clippingRegion, document);
					Position position= decoration.fPosition;
					if (position != null) {
						drawRangeStart= Math.min(drawRangeStart, position.offset);
						drawRangeEnd= Math.max(drawRangeEnd, position.offset + position.length);
					}
				}

			}

			// Update existing annotations
			Annotation[] changedAnnotations= event.getChangedAnnotations();
			for (int i= 0, length= changedAnnotations.length; i < length; i++) {
				Annotation annotation= changedAnnotations[i];

				boolean isHighlighting= false;

				Decoration decoration= (Decoration)highlightedDecorationsMap.get(annotation);

				if (decoration != null) {
					isHighlighting= true;
					// The call below updates the decoration - no need to create new decoration
					decoration= getDecoration(annotation, decoration);
					if (decoration == null)
						highlightedDecorationsMap.remove(annotation);
				} else {
					decoration= getDecoration(annotation, decoration);
					if (decoration != null && decoration.fPaintingStrategy instanceof ITextStyleStrategy) {
						highlightedDecorationsMap.put(annotation, decoration);
						isHighlighting= true;
					}
				}

				boolean usesDrawingStrategy= !isHighlighting && decoration != null;

				Position position= null;
				if (decoration == null)
					position= fModel.getPosition(annotation);
				else
					position= decoration.fPosition;

				if (position != null && !position.isDeleted()) {
					if (isHighlighting) {
						highlightAnnotationRangeStart= Math.min(highlightAnnotationRangeStart, position.offset);
						highlightAnnotationRangeEnd= Math.max(highlightAnnotationRangeEnd, position.offset + position.length);
					}
					if (usesDrawingStrategy) {
						drawRangeStart= Math.min(drawRangeStart, position.offset);
						drawRangeEnd= Math.max(drawRangeEnd, position.offset + position.length);
					}
				} else {
					highlightedDecorationsMap.remove(annotation);
				}

				if (usesDrawingStrategy) {
					Decoration oldDecoration= (Decoration)decorationsMap.get(annotation);
					if (oldDecoration != null) {
						drawDecoration(oldDecoration, null, annotation, clippingRegion, document);
						if (decoration != null)
							decorationsMap.put(annotation, decoration);
						else
							decorationsMap.remove(annotation);
					}
				}
			}

			e= Arrays.asList(event.getAddedAnnotations()).iterator();
		}

		// Add new annotations
		while (e.hasNext()) {
			Annotation annotation= (Annotation)e.next();
			Decoration pp= getDecoration(annotation, null);
			if (pp != null) {
				if (pp.fPaintingStrategy instanceof IDrawingStrategy) {
					decorationsMap.put(annotation, pp);
					drawRangeStart= Math.min(drawRangeStart, pp.fPosition.offset);
					drawRangeEnd= Math.max(drawRangeEnd, pp.fPosition.offset + pp.fPosition.length);
				} else if (pp.fPaintingStrategy instanceof ITextStyleStrategy) {
					highlightedDecorationsMap.put(annotation, pp);
					highlightAnnotationRangeStart= Math.min(highlightAnnotationRangeStart, pp.fPosition.offset);
					highlightAnnotationRangeEnd= Math.max(highlightAnnotationRangeEnd, pp.fPosition.offset + pp.fPosition.length);
				}

			}
		}

		synchronized (fDecorationMapLock) {
			fDecorationsMap= decorationsMap;
			updateDrawRanges(drawRangeStart, drawRangeEnd, isWorldChange);
		}

		synchronized (fHighlightedDecorationsMapLock) {
			fHighlightedDecorationsMap= highlightedDecorationsMap;
			updateHighlightRanges(highlightAnnotationRangeStart, highlightAnnotationRangeEnd, isWorldChange);
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
	 * Updates the remembered decoration ranges.
	 *
	 * @param drawRangeStart	the start of the range
	 * @param drawRangeEnd		the end of the range
	 * @param isWorldChange		tells whether the range belongs to a annotation model event reporting a world change
	 * @since 3.3
	 */
	private void updateDrawRanges(int drawRangeStart, int drawRangeEnd, boolean isWorldChange) {
		if (drawRangeStart != Integer.MAX_VALUE) {

			int maxRangeStart= drawRangeStart;
			int maxRangeEnd= drawRangeEnd;

			if (fTotalDrawRange != null) {
				maxRangeStart= Math.min(maxRangeStart, fTotalDrawRange.offset);
				maxRangeEnd= Math.max(maxRangeEnd, fTotalDrawRange.offset + fTotalDrawRange.length);
			}

			if (fTotalDrawRange == null)
				fTotalDrawRange= new Position(0);
			if (fCurrentDrawRange == null)
				fCurrentDrawRange= new Position(0);

			if (isWorldChange) {
				fTotalDrawRange.offset= drawRangeStart;
				fTotalDrawRange.length= drawRangeEnd - drawRangeStart;
				fCurrentDrawRange.offset= maxRangeStart;
				fCurrentDrawRange.length= maxRangeEnd - maxRangeStart;
			} else {
				fTotalDrawRange.offset= maxRangeStart;
				fTotalDrawRange.length= maxRangeEnd - maxRangeStart;
				fCurrentDrawRange.offset=drawRangeStart;
				fCurrentDrawRange.length= drawRangeEnd - drawRangeStart;
			}
		} else {
			if (isWorldChange) {
				fCurrentDrawRange= fTotalDrawRange;
				fTotalDrawRange= null;
			} else {
				fCurrentDrawRange= null;
			}
		}

		adaptToDocumentLength(fCurrentDrawRange);
		adaptToDocumentLength(fTotalDrawRange);
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
	 * @return the decoration or <code>null</code> if there's no valid one
	 * @since 3.0
	 */
	private Decoration getDecoration(Annotation annotation, Decoration decoration) {

		if (annotation.isMarkedDeleted())
			return null;

		String type= annotation.getType();

		Object paintingStrategy= getPaintingStrategy(type);
		if (paintingStrategy == null || paintingStrategy instanceof NullStrategy)
			return null;

		Color color= getColor(type);
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

		decoration.fPaintingStrategy= paintingStrategy;

		return decoration;
	}

	/**
	 * Returns the painting strategy for the given annotation.
	 *
	 * @param type the annotation type
	 * @return the annotation painter
	 * @since 3.0
	 */
	private Object getPaintingStrategy(final String type) {
		Object strategy= fCachedAnnotationType2PaintingStrategy.get(type);
		if (strategy != null)
			return strategy;

		strategy= fPaintingStrategyId2PaintingStrategy.get(fAnnotationType2PaintingStrategyId.get(type));
		if (strategy != null) {
			fCachedAnnotationType2PaintingStrategy.put(type, strategy);
			return strategy;
		}

		if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension ext = (IAnnotationAccessExtension) fAnnotationAccess;
			Object[] sts = ext.getSupertypes(type);
			for (int i= 0; i < sts.length; i++) {
				strategy= fPaintingStrategyId2PaintingStrategy.get(fAnnotationType2PaintingStrategyId.get(sts[i]));
				if (strategy != null) {
					fCachedAnnotationType2PaintingStrategy.put(type, strategy);
					return strategy;
				}
			}
		}

		fCachedAnnotationType2PaintingStrategy.put(type, NULL_STRATEGY);
		return null;

	}

	/**
	 * Returns the color for the given annotation type
	 *
	 * @param annotationType the annotation type
	 * @return the color
	 * @since 3.0
	 */
	private Color getColor(final Object annotationType) {
		Color color= (Color)fCachedAnnotationType2Color.get(annotationType);
		if (color != null)
			return color;

		color= (Color)fAnnotationType2Color.get(annotationType);
		if (color != null) {
			fCachedAnnotationType2Color.put(annotationType, color);
			return color;
		}

		if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension extension= (IAnnotationAccessExtension) fAnnotationAccess;
			Object[] superTypes= extension.getSupertypes(annotationType);
			if (superTypes != null) {
				for (int i= 0; i < superTypes.length; i++) {
					color= (Color)fAnnotationType2Color.get(superTypes[i]);
					if (color != null) {
						fCachedAnnotationType2Color.put(annotationType, color);
						return color;
					}
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
		disablePainting(event == null);

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
					StyleRange styleRange= new StyleRange(start, length, null, null);
					((ITextStyleStrategy)pp.fPaintingStrategy).applyTextStyle(styleRange, pp.fColor);
					tp.mergeStyleRange(styleRange);
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
		Display textWidgetDisplay;
		try {
			StyledText textWidget= fTextWidget;
			if (textWidget == null || textWidget.isDisposed())
				return;
			textWidgetDisplay= textWidget.getDisplay();
		} catch (SWTException ex) {
			if (ex.code == SWT.ERROR_WIDGET_DISPOSED)
				return;
			throw ex;
		}

		if (fIsSettingModel) {
			// inside the UI thread -> no need for posting
			if (textWidgetDisplay == Display.getCurrent())
				updatePainting(event);
			else {
				/*
				 * we can throw away the changes since
				 * further update painting will happen
				 */
				return;
			}
		} else {
			if (DEBUG && event != null && event.isWorldChange()) {
				System.out.println("AP: WORLD CHANGED, stack trace follows:"); //$NON-NLS-1$
				new Throwable().printStackTrace(System.out);
			}

			// XXX: posting here is a problem for annotations that are being
			// removed and the positions of which are not updated to document
			// changes any more. If the document gets modified between
			// now and running the posted runnable, the position information
			// is not accurate any longer.
			textWidgetDisplay.asyncExec(new Runnable() {
				public void run() {
					if (fTextWidget != null && !fTextWidget.isDisposed())
						updatePainting(event);
				}
			});
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
			fAnnotationType2Color.put(annotationType, color);
		else
			fAnnotationType2Color.remove(annotationType);
		fCachedAnnotationType2Color.clear();
	}

	/**
	 * Adds the given annotation type to the list of annotation types whose annotations should be
	 * painted by this painter using squiggly drawing. If the annotation type is already in this
	 * list, this method is without effect.
	 * 
	 * @param annotationType the annotation type
	 * @deprecated As of 3.4 replaced by
	 *             {@link #addTextStyleStrategy(Object, AnnotationPainter.ITextStyleStrategy)} and
	 *             {@link UnderlineStrategy}
	 */
	public void addAnnotationType(Object annotationType) {
		addAnnotationType(annotationType, SQUIGGLES);
	}

	/**
	 * Adds the given annotation type to the list of annotation types whose annotations should be
	 * painted by this painter using the given strategy. If the annotation type is already in this
	 * list, the old strategy gets replaced.
	 * 
	 * @param annotationType the annotation type
	 * @param strategyID the id of the drawing or text style strategy that should be used for this
	 *            annotation type
	 * @since 3.0
	 */
	public void addAnnotationType(Object annotationType, Object strategyID) {
		fAnnotationType2PaintingStrategyId.put(annotationType, strategyID);
		fCachedAnnotationType2PaintingStrategy.clear();

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
		fPaintingStrategyId2PaintingStrategy.put(id, strategy);
		fCachedAnnotationType2PaintingStrategy.clear();
	}

	/**
	 * Registers a new drawing strategy under the given ID. If there is already
	 * a strategy registered under <code>id</code>, the old strategy gets
	 * replaced.
	 * <p>
	 * The given id can be referenced when adding annotation types, see
	 * {@link #addAnnotationType(Object, Object)}.</p>
	 * <p>
	 * <strong>Note:</strong> The annotations will only be painted if this
	 * painter has been registered as text presentation listener on the viewer.</p>
	 *
	 * @param id the identifier under which the strategy can be referenced, not <code>null</code>
	 * @param strategy the new strategy
	 * @see ITextViewerExtension4#addTextPresentationListener(ITextPresentationListener)
	 * @since 3.4
	 */
	public void addTextStyleStrategy(Object id, ITextStyleStrategy strategy) {
		// don't permit null as null is used to signal that an annotation type is not
		// registered with a specific strategy, and that its annotation hierarchy should be searched
		if (id == null)
			throw new IllegalArgumentException();
		fPaintingStrategyId2PaintingStrategy.put(id, strategy);
		fCachedAnnotationType2PaintingStrategy.clear();
	}

	/**
	 * Adds the given annotation type to the list of annotation types whose
	 * annotations should be highlighted this painter. If the annotation  type
	 * is already in this list, this method is without effect.
	 * <p>
	 * <strong>Note:</strong> The annotations will only be painted if this
	 * painter has been registered as text presentation listener on the viewer.</p>
	 *
	 * @param annotationType the annotation type
	 * @see ITextViewerExtension4#addTextPresentationListener(ITextPresentationListener)
	 * @since 3.0
	 */
	public void addHighlightAnnotationType(Object annotationType) {
		addAnnotationType(annotationType, HIGHLIGHTING);
	}

	/**
	 * Removes the given annotation type from the list of annotation types whose
	 * annotations are painted by this painter. If the annotation type is not
	 * in this list, this method is without effect.
	 *
	 * @param annotationType the annotation type
	 */
	public void removeAnnotationType(Object annotationType) {
		fCachedAnnotationType2PaintingStrategy.clear();
		fAnnotationType2PaintingStrategyId.remove(annotationType);
		if (fAnnotationType2PaintingStrategyId.isEmpty() && fTextInputListener != null) {
			fSourceViewer.removeTextInputListener(fTextInputListener);
			fTextInputListener= null;
			fInputDocumentAboutToBeChanged= false;
		}
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
		removeAnnotationType(annotationType);
	}

	/**
	 * Clears the list of annotation types whose annotations are
	 * painted by this painter.
	 */
	public void removeAllAnnotationTypes() {
		fCachedAnnotationType2PaintingStrategy.clear();
		fAnnotationType2PaintingStrategyId.clear();
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
		return !fAnnotationType2PaintingStrategyId.isEmpty();
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#dispose()
	 */
	public void dispose() {

		if (fAnnotationType2Color != null) {
			fAnnotationType2Color.clear();
			fAnnotationType2Color= null;
		}

		if (fCachedAnnotationType2Color != null) {
			fCachedAnnotationType2Color.clear();
			fCachedAnnotationType2Color= null;
		}

		if (fCachedAnnotationType2PaintingStrategy != null) {
			fCachedAnnotationType2PaintingStrategy.clear();
			fCachedAnnotationType2PaintingStrategy= null;
		}

		if (fAnnotationType2PaintingStrategyId != null) {
			fAnnotationType2PaintingStrategyId.clear();
			fAnnotationType2PaintingStrategyId= null;
		}

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
			int top= JFaceTextUtil.getPartialTopIndex(fSourceViewer);
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
			int bottom= JFaceTextUtil.getPartialBottomIndex(fSourceViewer);
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

		IRegion clippingRegion= computeClippingRegion(event, false);
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
			if (!(a.isMarkedDeleted() || skip(a) || !regionsTouchOrOverlap(pp.fPosition.getOffset(), pp.fPosition.getLength(), vOffset, vLength))) {
				// ensure sized appropriately
				for (int i= toBeDrawn.size(); i <= pp.fLayer; i++)
					toBeDrawn.add(new LinkedList());
				((List) toBeDrawn.get(pp.fLayer)).add(entry);
			}
		}
		IDocument document= fSourceViewer.getDocument();
		for (Iterator it= toBeDrawn.iterator(); it.hasNext();) {
			List layer= (List) it.next();
			for (Iterator e = layer.iterator(); e.hasNext();) {
				Map.Entry entry= (Map.Entry)e.next();
				Annotation a= (Annotation)entry.getKey();
				Decoration pp = (Decoration)entry.getValue();
				drawDecoration(pp, gc, a, clippingRegion, document);
			}
		}
	}

	private void drawDecoration(Decoration pp, GC gc, Annotation annotation, IRegion clippingRegion, IDocument document) {
		if (clippingRegion == null)
			return;

		if (!(pp.fPaintingStrategy instanceof IDrawingStrategy))
			return;

		IDrawingStrategy drawingStrategy= (IDrawingStrategy)pp.fPaintingStrategy;

		int clippingOffset= clippingRegion.getOffset();
		int clippingLength= clippingRegion.getLength();

		Position p= pp.fPosition;
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
				if (paintLength >= 0 && regionsTouchOrOverlap(paintStart, paintLength, clippingOffset, clippingLength)) {
					// otherwise inside a line delimiter
					IRegion widgetRange= getWidgetRange(paintStart, paintLength);
					if (widgetRange != null) {
						drawingStrategy.draw(annotation, gc, fTextWidget, widgetRange.getOffset(), widgetRange.getLength(), pp.fColor);
					}
				}
			}

		} catch (BadLocationException x) {
		}
	}

	/**
	 * Computes the model (document) region that is covered by the paint event's clipping region. If
	 * <code>event</code> is <code>null</code>, the model range covered by the visible editor
	 * area (viewport) is returned.
	 *
	 * @param event the paint event or <code>null</code> to use the entire viewport
	 * @param isClearing tells whether the clipping is need for clearing an annotation
	 * @return the model region comprised by either the paint event's clipping region or the
	 *         viewport
	 * @since 3.2
	 */
	private IRegion computeClippingRegion(PaintEvent event, boolean isClearing) {
		if (event == null) {

			if (!isClearing && fCurrentDrawRange != null)
				return new Region(fCurrentDrawRange.offset, fCurrentDrawRange.length);

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
		} catch (IllegalArgumentException ex1) {
			try {
				int firstVisibleLine= JFaceTextUtil.getPartialTopIndex(fTextWidget);
				widgetOffset= fTextWidget.getOffsetAtLine(firstVisibleLine);
			} catch (IllegalArgumentException ex2) { // above try code might fail too
				widgetOffset= 0;
			}
		}

		int widgetEndOffset;
		try {
			int widgetClippingEndOffset= fTextWidget.getOffsetAtLocation(new Point(0, event.y + event.height));
			int lastWidgetLine= fTextWidget.getLineAtOffset(widgetClippingEndOffset);
			widgetEndOffset= fTextWidget.getOffsetAtLine(lastWidgetLine + 1);
		} catch (IllegalArgumentException ex1) {
			// happens if the editor is not "full", e.g. the last line of the document is visible in the editor
			try {
				int lastVisibleLine= JFaceTextUtil.getPartialBottomIndex(fTextWidget);
				if (lastVisibleLine == fTextWidget.getLineCount() - 1)
					// last line
					widgetEndOffset= fTextWidget.getCharCount();
				else
					widgetEndOffset= fTextWidget.getOffsetAtLine(lastVisibleLine + 1) - 1;
			} catch (IllegalArgumentException ex2) { // above try code might fail too
				widgetEndOffset= fTextWidget.getCharCount();
			}
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
	 * Returns the widget region that corresponds to the
	 * given offset and length in the viewer's document.
	 * 
	 * The returned object can be the fReusableRegion and may used
	 * only to read the return values and must not used to store
	 * the region.
	 *
	 * @param modelOffset the model offset
	 * @param modelLength the model length
	 * @return the corresponding widget region
	 */
	private IRegion getWidgetRange(int modelOffset, int modelLength) {
		if (modelOffset == Integer.MAX_VALUE)
			return null;

		if (fSourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fSourceViewer;
			fReusableRegion.setOffset(modelOffset);
			fReusableRegion.setLength(modelLength);
			return extension.modelRange2WidgetRange(fReusableRegion);
		}

		IRegion region= fSourceViewer.getVisibleRegion();
		int offset= region.getOffset();
		int length= region.getLength();

		if (regionsTouchOrOverlap(modelOffset, modelLength, offset, length)) {
			int p1= Math.max(offset, modelOffset);
			int p2= Math.min(offset + length, modelOffset + modelLength);
			fReusableRegion.setOffset(p1 - offset);
			fReusableRegion.setLength(p2 - p1);
			return fReusableRegion;
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
	 * Checks whether the two given text regions touch or overlap each other.
	 *
	 * @param offset1 offset of the first region
	 * @param length1 length of the first region
	 * @param offset2 offset of the second region
	 * @param length2 length of the second region
	 * @return <code>true</code> if the regions touch or overlap
	 */
	private boolean regionsTouchOrOverlap(int offset1, int length1, int offset2, int length2) {
		return (offset1 <= offset2+length2) && (offset2 <= offset1+length1);
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
