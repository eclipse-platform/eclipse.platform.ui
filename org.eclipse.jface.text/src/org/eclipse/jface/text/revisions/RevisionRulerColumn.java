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
package org.eclipse.jface.text.revisions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.IAnnotationHoverExtension2;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.text.source.LineRange;

import org.eclipse.jface.internal.text.revisions.ChangeRegion;
import org.eclipse.jface.internal.text.revisions.DiffApplier;

/**
 * A vertical ruler column capable of displaying revision (aka. annotate) information..
 * Clients instantiate and configure object of this class.
 * <p>
 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
 * </p>
 *
 * @since 3.2
 */
public final class RevisionRulerColumn implements IRevisionRulerColumn {
	/** Tells whether this class is in debug mode. */
	private static boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface.text.source/debug/RevisionRulerColumn"));  //$NON-NLS-1$//$NON-NLS-2$
	
	private static final class RevisionAnnotation extends Annotation {
		public RevisionAnnotation(String text) {
			super("org.eclipse.ui.workbench.texteditor.revisionAnnotation", false, text); //$NON-NLS-1$
		}
	}
	
	private final class RevisionHover implements IAnnotationHover, IAnnotationHoverExtension, IAnnotationHoverExtension2 {
		/*
		 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
		 */
		public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
			Object info= getHoverInfo(sourceViewer, getHoverLineRange(sourceViewer, lineNumber), 0);
			return info == null ? null : info.toString();
		}

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverControlCreator()
		 */
		public IInformationControlCreator getHoverControlCreator() {
			return null;
		}

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#canHandleMouseCursor()
		 */
		public boolean canHandleMouseCursor() {
			return false;
		}
		
		/*
		 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension2#canHandleMouseWheel()
		 */
		public boolean canHandleMouseWheel() {
			return true;
		}

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, org.eclipse.jface.text.source.ILineRange, int)
		 */
		public Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleNumberOfLines) {
			ChangeRegion region= getChangeRegion(lineRange.getStartLine());
			return region == null ? null : region.getRevision().getHoverInfo();
		}

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverLineRange(org.eclipse.jface.text.source.ISourceViewer, int)
		 */
		public ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber) {
			ChangeRegion region= getChangeRegion(lineNumber);
			return region == null ? null : new LineRange(lineNumber, 1);
		}
	}

	private final class ColorTool {
		private static final double MAX_SHADING= 0.8;
		private static final double MIN_SHADING= 0.3;
		private static final double FOCUS_COLOR_SHADING= 0.9;
		
		private List fRevisions= new ArrayList();
		
		public void setInfo(RevisionInformation info) {
			if (info == null)
				return;
			List revisions= new ArrayList();
			for (Iterator it= info.getRevisions().iterator(); it.hasNext();) {
				Revision revision= (Revision) it.next();
				revisions.add(new Long(computeAge(revision)));
			}
			Collections.sort(revisions);
			fRevisions= revisions;
		}
		
		private RGB adaptColorToAge(Revision revision, RGB rgb, boolean focus) {
			long age= computeAge(revision);
			// relative age: newest is 0, oldest is 1
			double relativeAge= (double) fRevisions.indexOf(new Long(age)) / (fRevisions.size() - 1);
			
			return getShadedColor(rgb, 1 - relativeAge, focus);
		}
		
		private RGB getShadedColor(RGB color, double scale, boolean focus) {
			Assert.isLegal(scale >= 0.0);
			Assert.isLegal(scale <= 1.0);
			RGB background= fBackground == null ? new RGB(255, 255, 255) : fBackground.getRGB();
			
			if (focus) {
				scale -= FOCUS_COLOR_SHADING;
				if (scale < 0) {
					background= new RGB(255 - background.red, 255 - background.green, 255 - background.blue);
					scale= -scale;
				}
			}
			
			scale= (MAX_SHADING - MIN_SHADING) * scale + MIN_SHADING;

			return interpolate(color, background, scale);
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
		private RGB interpolate(RGB fg, RGB bg, double scale) {
			return new RGB(
				(int) ((1.0-scale) * fg.red + scale * bg.red),
				(int) ((1.0-scale) * fg.green + scale * bg.green),
				(int) ((1.0-scale) * fg.blue + scale * bg.blue)
			);
		}
		
		private long computeAge(Revision revision) {
			return revision.getDate().getTime();
		}
		
		public RGB getColor(Revision revision, boolean focus) {
			RGB rgb= revision.getColor();
			rgb= adaptColorToAge(revision, rgb, focus);
			return rgb;
		}
	}
	
	/**
	 * Handles all the mouse interaction in this line number ruler column.
	 */
	class MouseHandler implements MouseListener, MouseMoveListener, MouseTrackListener, Listener {

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent event) {
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
		}

		/*
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			Assert.isTrue(event.type == SWT.MouseWheel);
			handleMouseWheel(event);
		}

		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseEnter(MouseEvent e) {
			onEnter();
			updateFocusLine(toDocumentLineNumber(e.y));
		}

		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseExit(MouseEvent e) {
			updateFocusLine( -1);
			onExit();
		}

		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseHover(MouseEvent e) {
			onHover();
		}

		/*
		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseMove(MouseEvent e) {
			updateFocusLine(toDocumentLineNumber(e.y));
		}
	}

	/**
	 * Internal listener class.
	 */
	class InternalListener implements IViewportListener, ITextListener {

		/*
		 * @see IViewportListener#viewportChanged(int)
		 */
		public void viewportChanged(int verticalPosition) {
			if (verticalPosition != fScrollPos)
				redraw();
		}

		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent event) {

			if (!event.getViewerRedrawState())
				return;

			if (fSensitiveToTextChanges || event.getDocumentEvent() == null)
				postRedraw();

		}
	}

	/**
	 * Internal listener class that will update the ruler when the underlying model changes.
	 */
	class AnnotationListener implements IAnnotationModelListener {
		/*
		 * @see org.eclipse.jface.text.source.IAnnotationModelListener#modelChanged(org.eclipse.jface.text.source.IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			// TODO incremental
			fChangeRegions= null;
			postRedraw();
		}
	}
	
	/** This column's parent ruler */
	private CompositeRuler fParentRuler;
	/** Cached text viewer */
	private ITextViewer fCachedTextViewer;
	/** Cached text widget */
	private StyledText fCachedTextWidget;
	/** The columns canvas */
	private Canvas fCanvas;
	/** Cache for the actual scroll position in pixels */
	private int fScrollPos;
	/** The buffer for double buffering */
	private Image fBuffer;
	/** The internal listener */
	private InternalListener fInternalListener= new InternalListener();
	/** Indicates whether this column reacts on text change events */
	private boolean fSensitiveToTextChanges= false;
	/** The background color */
	private Color fBackground;
	/** The ruler's annotation model. */
	private IAnnotationModel fAnnotationModel;
	/** The ruler's hover */
	private IAnnotationHover fHover= new RevisionHover();
	/** The internal listener */
	private AnnotationListener fAnnotationListener= new AnnotationListener();
	/** The width of the change ruler column. */
	private int fWidth= 8;
	private RevisionInformation fRevisionInfo;
	private ArrayList fChangeRegions= null;
	private final ISharedTextColors fSharedColors;
	private final ColorTool fColorTool= new ColorTool();
	private List fAnnotations= new ArrayList();
	private MouseHandler fMouseHandler;
	private int fFocusLine= -1;
	private ChangeRegion fFocusRegion= null;
	private Revision fFocusRevision= null;
	private boolean fIsOverviewShowing= false;
	private boolean fWheelHandlerInstalled= false;
	private ILineDiffer fLineDiffer= null;

	/**
	 * Creates a new revision ruler column.
	 * 
	 * @param sharedColors the colors to look up RGBs
	 */
	public RevisionRulerColumn(ISharedTextColors sharedColors) {
		Assert.isNotNull(sharedColors);
		fSharedColors= sharedColors;
	}

	/**
	 * Returns the System background color for list widgets.
	 *
	 * @param display the display the drawing occurs on
	 * @return the System background color for list widgets
	 */
	private final Color getBackground(Display display) {
		if (fBackground == null)
			return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		return fBackground;
	}

	/*
	 * @see IVerticalRulerColumn#createControl(CompositeRuler, Composite)
	 */
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {

		fParentRuler= parentRuler;
		fCachedTextViewer= parentRuler.getTextViewer();
		fCachedTextWidget= fCachedTextViewer.getTextWidget();

		fCanvas= new Canvas(parentControl, SWT.NO_BACKGROUND);
		fCanvas.setBackground(getBackground(fCanvas.getDisplay()));

		fCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
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

		fMouseHandler= new MouseHandler();
		fCanvas.addMouseListener(fMouseHandler);
		fCanvas.addMouseTrackListener(fMouseHandler);
		fCanvas.addMouseMoveListener(fMouseHandler);

		if (fCachedTextViewer != null) {

			fCachedTextViewer.addViewportListener(fInternalListener);
			fCachedTextViewer.addTextListener(fInternalListener);
		}

		return fCanvas;
	}

	/**
	 * Disposes the column's resources.
	 */
	private void handleDispose() {

		if (fLineDiffer != null) {
			((IAnnotationModel) fLineDiffer).removeAnnotationModelListener(fAnnotationListener);
			fLineDiffer= null;
		}

		if (fCachedTextViewer != null) {
			fCachedTextViewer.removeViewportListener(fInternalListener);
			fCachedTextViewer.removeTextListener(fInternalListener);
		}

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
		if (fBuffer == null)
			fBuffer= new Image(fCanvas.getDisplay(), size.x, size.y);

		GC gc= new GC(fBuffer);
		gc.setFont(fCanvas.getFont());

		try {
			gc.setBackground(getBackground(fCanvas.getDisplay()));
			gc.fillRectangle(0, 0, size.x, size.y);

			doPaint(gc);

		} finally {
			gc.dispose();
		}

		dest.drawImage(fBuffer, 0, 0);
	}

	/**
	 * Returns the view port height in lines.
	 *
	 * @return the view port height in lines
	 */
	private final int getVisibleLinesInViewport() {
		Rectangle clArea= fCachedTextWidget.getClientArea();
		if (!clArea.isEmpty())
			return clArea.height / fCachedTextWidget.getLineHeight();
		return -1;
	}

	/**
	 * Draws the ruler column. Uses <code>ITextViewerExtension5</code> for the
	 * implementation. Will replace <code>doPaint(GC)</code>.
	 *
	 * @param gc the GC to draw into
	 */
	private void doPaint(GC gc) {

		if (fCachedTextViewer == null)
			return;

		ILineRange visibleModelLines= computeVisibleModelLines();
		if (visibleModelLines == null)
			return;

		fSensitiveToTextChanges= visibleModelLines.getNumberOfLines() <= getVisibleLinesInViewport();

		fScrollPos= fCachedTextWidget.getTopPixel();

		int y_shift= -fScrollPos;
		List/*<ChangeRegion>*/ changes= getChangeRegions(visibleModelLines);
		for (Iterator it= changes.iterator(); it.hasNext();) {
			ChangeRegion region= (ChangeRegion) it.next();
			paintChangeRegion(region, gc, y_shift);
		}
		
	}

	private void paintChangeRegion(ChangeRegion region, GC gc, int y_shift) {
		Revision revision= region.getRevision();
		gc.setBackground(lookupColor(revision, false));
		if (revision == fFocusRevision)
			gc.setForeground(lookupColor(revision, true));
		
		List ranges= region.getAdjustedRanges();
		for (Iterator it= ranges.iterator(); it.hasNext();) {
			ILineRange range= (ILineRange) it.next();
			Rectangle box= computeBoxBounds(range, y_shift);
			if (box == null)
				return;
			
			if (revision == fFocusRevision)
				paintHighlight(gc, box);
			else
				gc.fillRectangle(box);
			
		}
	}

	private void paintHighlight(GC gc, Rectangle box) {
		boolean fillGradient= false;
		if (fillGradient) {
			fillGradientRectangle(gc, box);
		} else {
			// simple box
			gc.fillRectangle(box); // background
			gc.drawRectangle(box.x, box.y, box.width - 1, box.height - 1); // highlight box
		}
	}

	private void fillGradientRectangle(GC gc, Rectangle box) {
		int half= (box.width + 1) / 2;
		// left
		gc.fillGradientRectangle(box.x, box.y, half, box.height, false);
		// right
		gc.fillGradientRectangle(box.x + box.width, box.y, -half, box.height, false);
		
		org.eclipse.swt.graphics.Region reg= new org.eclipse.swt.graphics.Region(gc.getDevice());
		try {
			int[] triangle= {box.x, box.y, box.x + box.width, box.y, box.x + half, box.y + half};
			reg.add(triangle);
			triangle[1] += box.height;
			triangle[3] += box.height;
			triangle[5] += box.height - box.width;
			reg.add(triangle);
			gc.setClipping(reg);
			
			// top
			gc.fillGradientRectangle(box.x, box.y, box.width, half, true);
			// bottom
			gc.fillGradientRectangle(box.x, box.y + box.height, box.width, -half, true);
			
			gc.setClipping((org.eclipse.swt.graphics.Region) null);
		} finally {
			reg.dispose();
		}
	}
	
	private Color lookupColor(Revision revision, boolean focus) {
		return fSharedColors.getColor(fColorTool.getColor(revision, focus));
	}
	
	/**
	 * Returns the change region that contains the given line in one of its adjusted lien ranges, or
	 * <code>null</code> if there is none.
	 * 
	 * @param line the line of interest
	 * @return the corresponding <code>ChangeRegion</code> or <code>null</code>
	 */
	private ChangeRegion getChangeRegion(int line) {
		List regions= getRegionCache();
		
		if (regions.isEmpty() || line == -1)
			return null;
		
		for (Iterator it= regions.iterator(); it.hasNext();) {
			ChangeRegion region= (ChangeRegion) it.next();
			if (contains(region.getAdjustedRanges(), line))
				return region;
		}
		
		// line may be right after the last region
		ChangeRegion lastRegion= (ChangeRegion) regions.get(regions.size() - 1);
		if (line == end(lastRegion.getAdjustedCoverage()))
			return lastRegion;
		return null;
	}

	/**
	 * Returns the sublist of all <code>ChangeRegion</code>s that intersect with the given lines.
	 * 
	 * @param lines the model based lines of interest
	 * @return elementType: ChangeRegion
	 */
	private List getChangeRegions(ILineRange lines) {
		List regions= getRegionCache();

		// return the interesting subset
		int end= end(lines);
		int first= -1, last= -1;
		for (int i= 0; i < regions.size(); i++) {
			ChangeRegion region= (ChangeRegion) regions.get(i);
			int coverageEnd= end(region.getAdjustedCoverage());
			if (first == -1 && coverageEnd > lines.getStartLine())
				first= i;
			if (first != -1 && coverageEnd > end) {
				last= i;
				break;
			}
		}
		if (first == -1)
			return Collections.EMPTY_LIST;
		if (last == -1)
			last= regions.size() - 1; // bottom index may be one too much
		
		return regions.subList(first, last + 1);
	}

	private List getRegionCache() {
		if (fChangeRegions == null && fRevisionInfo != null) {
			ArrayList regions= new ArrayList();
			// flatten
			for (Iterator revisions= fRevisionInfo.getRevisions().iterator(); revisions.hasNext();) {
				Revision revision= (Revision) revisions.next();
				regions.addAll(revision.getRegions());
			}
			
			// sort
			Collections.sort(regions, new Comparator() {
				public int compare(Object o1, Object o2) {
					ChangeRegion r1= (ChangeRegion) o1;
					ChangeRegion r2= (ChangeRegion) o2;
					
					// sort order is unaffected by diff information
					return r1.getOriginalRange().getStartLine() - r2.getOriginalRange().getStartLine();
				}
			});
			
			if (fLineDiffer != null)
				new DiffApplier().applyDiff(regions, fLineDiffer, fCachedTextViewer.getDocument().getNumberOfLines());
			
			fChangeRegions= regions;
		}

		if (fChangeRegions == null)
			return Collections.EMPTY_LIST;

		return fChangeRegions;
	}

	private static final boolean contains(ILineRange range, int line) {
		return range.getStartLine() <= line && end(range) > line;
	}

	private static final boolean contains(List ranges, int line) {
		for (Iterator it= ranges.iterator(); it.hasNext();) {
			ILineRange range= (ILineRange) it.next();
			if (contains(range, line))
				return true;
		}
		return false;
	}
	
	private static int end(ILineRange one) {
		return one.getStartLine() + one.getNumberOfLines();
	}
	
	/**
	 * Computes the document based line range visible in the text widget.
	 * 
	 * @return the document based line range visible in the text widget
	 */
	private final ILineRange computeVisibleModelLines() {
		IDocument doc= fCachedTextViewer.getDocument();
		if (doc == null)
			return null;
		
		int topLine;
		IRegion coverage;
		
		if (fCachedTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fCachedTextViewer;
			
			// ITextViewer.getTopIndex returns the fully visible line, but we want the partially
			// visible one
			int widgetTopLine= getPartialTopIndex(fCachedTextWidget);
			topLine= extension.widgetLine2ModelLine(widgetTopLine);
			
			coverage= extension.getModelCoverage();
			
		} else {
			topLine= fCachedTextViewer.getTopIndex();
			if (fCachedTextWidget.getTopPixel() % fCachedTextWidget.getLineHeight() != 0)
				topLine--;
			coverage= fCachedTextViewer.getVisibleRegion();
		}
		
		int bottomLine= fCachedTextViewer.getBottomIndex();
		if (bottomLine != -1)
			++ bottomLine;
		
		// clip by coverage window
		try {
			int firstLine= doc.getLineOfOffset(coverage.getOffset());
			if (firstLine > topLine)
				topLine= firstLine;
			
			int lastLine= doc.getLineOfOffset(coverage.getOffset() + coverage.getLength());
			if (lastLine < bottomLine || bottomLine == -1)
				bottomLine= lastLine;
		} catch (BadLocationException x) {
			x.printStackTrace();
			return null;
		}
		
		ILineRange visibleModelLines= new LineRange(topLine, bottomLine - topLine + 1);
		return visibleModelLines;
	}
	
	private final ILineRange modelLinesToWidgetLines(ILineRange range) {
		int widgetStartLine= -1;
		int widgetEndLine= -1;
		if (fCachedTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fCachedTextViewer;
			int modelEndLine= end(range);
			for (int modelLine= range.getStartLine(); modelLine < modelEndLine; modelLine++) {
				int widgetLine= extension.modelLine2WidgetLine(modelLine);
				if (widgetLine != -1) {
					if (widgetStartLine == -1)
						widgetStartLine= widgetLine;
					widgetEndLine= widgetLine;
				}
			}
		} else {
			IRegion region= fCachedTextViewer.getVisibleRegion();
			IDocument document= fCachedTextViewer.getDocument();
			try {
				int visibleStartLine= document.getLineOfOffset(region.getOffset());
				int visibleEndLine= document.getLineOfOffset(region.getOffset() + region.getLength());
				widgetStartLine= Math.max(0, range.getStartLine() - visibleStartLine);
				widgetEndLine= Math.min(visibleEndLine, end(range) - 1);
			} catch (BadLocationException x) {
				x.printStackTrace();
				// ignore and return null
			}
		}
		if (widgetStartLine == -1 || widgetEndLine == -1)
			return null;
		return new LineRange(widgetStartLine, widgetEndLine - widgetStartLine + 1);
	}
	
	private final int getPartialTopIndex(StyledText widget) {
		int topIndex= widget.getTopIndex();
		if (topIndex > 0) {
			int topPixel= widget.getTopPixel();
			int lineHeight= widget.getLineHeight();
			if (topPixel % lineHeight != 0)
				topIndex--;
		}
		return topIndex;
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
	 * @see IVerticalRulerColumn#setFont(Font)
	 */
	public void setFont(Font font) {
		// font not needed
	}

	/**
	 * Returns the parent (composite) ruler of this ruler column.
	 *
	 * @return the parent ruler
	 * @since 3.0
	 */
	private IVerticalRulerInfo getParentRuler() {
		return fParentRuler;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	public int getLineOfLastMouseButtonActivity() {
		return getParentRuler().getLineOfLastMouseButtonActivity();
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	public int toDocumentLineNumber(int y_coordinate) {
		return getParentRuler().toDocumentLineNumber(y_coordinate);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getHover()
	 */
	public IAnnotationHover getHover() {
		return fHover;
	}

	/*
	 * @see IVerticalRulerColumn#setModel(IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
		IAnnotationModel diffModel;
		if (model instanceof IAnnotationModelExtension)
			diffModel= ((IAnnotationModelExtension)model).getAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);
		else
			diffModel= model;
		
		setDiffer(diffModel);
		setAnnotationModel(model);
	}

	private void setAnnotationModel(IAnnotationModel model) {
		if (fAnnotationModel != model)
			fAnnotationModel= model;
	}

	private void setDiffer(IAnnotationModel differ) {
		if (differ instanceof ILineDiffer) {
			if (fLineDiffer != differ) {
				if (fLineDiffer != null)
					((IAnnotationModel) fLineDiffer).removeAnnotationModelListener(fAnnotationListener);
				fLineDiffer= (ILineDiffer) differ;
				if (fLineDiffer != null)
					((IAnnotationModel) fLineDiffer).addAnnotationModelListener(fAnnotationListener);
				redraw();
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getModel()
	 */
	public IAnnotationModel getModel() {
		return fAnnotationModel;
	}

	/*
	 * @see IVerticalRulerColumn#getControl()
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

	/**
	 * Triggers a redraw in the display thread.
	 */
	private final void postRedraw() {
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
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#addVerticalRulerListener(org.eclipse.jface.text.source.IVerticalRulerListener)
	 */
	public void addVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#removeVerticalRulerListener(org.eclipse.jface.text.source.IVerticalRulerListener)
	 */
	public void removeVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.jface.text.revisions.IRevisionRulerColumn#setRevisionInformation(org.eclipse.jface.text.revisions.RevisionInformation)
	 */
	public void setRevisionInformation(RevisionInformation info) {
		fRevisionInfo= info;
		fChangeRegions= null;
		updateFocusRegion(null);
		fColorTool.setInfo(info);
		postRedraw();
	}
	
	private Rectangle computeBoxBounds(ILineRange range, int y_shift) {
		ILineRange widgetRange= modelLinesToWidgetLines(range);
		if (widgetRange == null)
			return null;
		int lineHeight= fCachedTextWidget.getLineHeight();
		int y= widgetRange.getStartLine() * lineHeight + y_shift;
		int height= (widgetRange.getNumberOfLines()) * lineHeight - 1;
		
		return new Rectangle(0, y, getWidth(), height);
	}
	
	private void showOverviewAnnotations(Revision revision) {
		if (fAnnotationModel == null)
			return;
		
		Map added= null;
		if (revision != null && fIsOverviewShowing) {
			added= new HashMap();
			for (Iterator it= revision.getRegions().iterator(); it.hasNext();) {
				ChangeRegion region= (ChangeRegion) it.next();
				for (Iterator regions= region.getAdjustedRanges().iterator(); regions.hasNext();) {
					ILineRange range= (ILineRange) regions.next();
					try {
						IRegion charRegion= toCharRegion(range);
						Position position= new Position(charRegion.getOffset(), charRegion.getLength());
						Annotation annotation= new RevisionAnnotation(revision.getId());
						added.put(annotation, position);
					} catch (BadLocationException x) {
						// ignore - document was changed, show no annotations
					}
				}
			}
		}
		
		if (fAnnotationModel instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension ext= (IAnnotationModelExtension) fAnnotationModel;
			ext.replaceAnnotations((Annotation[]) fAnnotations.toArray(new Annotation[fAnnotations.size()]), added);
		} else {
			for (Iterator it= fAnnotations.iterator(); it.hasNext();) {
				Annotation annotation= (Annotation) it.next();
				fAnnotationModel.removeAnnotation(annotation);
			}
			if (added != null) {
				for (Iterator it= added.entrySet().iterator(); it.hasNext();) {
					Entry entry= (Entry) it.next();
					fAnnotationModel.addAnnotation((Annotation) entry.getKey(), (Position) entry.getValue());
				}
			}
		}
		fAnnotations.clear();
		if (added != null)
			fAnnotations.addAll(added.keySet());
		
	}

	private IRegion toCharRegion(ILineRange lines) throws BadLocationException {
		IDocument document= fCachedTextViewer.getDocument();
		int offset= document.getLineOffset(lines.getStartLine());
		int nextLine= end(lines);
		int endOffset;
		if (nextLine >= document.getNumberOfLines())
			endOffset= document.getLength();
		else
			endOffset= document.getLineOffset(nextLine);
		return new Region(offset, endOffset - offset);
	}

	private void updateFocusLine(int line) {
		if (fFocusLine != line)
			onFocusLineChanged(fFocusLine, line);
	}

	private void onFocusLineChanged(int previousLine, int nextLine) {
		if (DEBUG) System.out.println("line: " + previousLine + " > " + nextLine); //$NON-NLS-1$ //$NON-NLS-2$
		fFocusLine= nextLine;
		ChangeRegion region= getChangeRegion(nextLine);
		updateFocusRegion(region);
	}

	private void updateFocusRegion(ChangeRegion region) {
		if (region != fFocusRegion)
			onFocusRegionChanged(fFocusRegion, region);
	}
	
	private void onFocusRegionChanged(ChangeRegion previousRegion, ChangeRegion nextRegion) {
		if (DEBUG) System.out.println("region: " + previousRegion+ " > " + nextRegion); //$NON-NLS-1$ //$NON-NLS-2$
		fFocusRegion= nextRegion;
		Revision revision= nextRegion == null ? null : nextRegion.getRevision();
		if (fFocusRevision != revision)
			onFocusRevisionChanged(fFocusRevision, revision);
	}
	
	private void onFocusRevisionChanged(Revision previousRevision, Revision nextRevision) {
		if (DEBUG) System.out.println("revision: " + previousRevision+ " > " + nextRevision); //$NON-NLS-1$ //$NON-NLS-2$
		fFocusRevision= nextRevision;
		uninstallWheelHandler();
		showOverviewAnnotations(fFocusRevision);
		redraw(); // pick up new highlights
	}

	private void uninstallWheelHandler() {
		fCanvas.removeListener(SWT.MouseWheel, fMouseHandler);
		fWheelHandlerInstalled= false;
	}

	private void installWheelHandler() {
		if (fFocusRevision != null && !fWheelHandlerInstalled) {
			fCanvas.addListener(SWT.MouseWheel, fMouseHandler);
			fWheelHandlerInstalled= true;
		}
	}
	
	private void onHover() {
		installWheelHandler();
	}
	
	private void onEnter() {
		fIsOverviewShowing= true;
	}

	private void onExit() {
		fIsOverviewShowing= false;
	}
	
	private void handleMouseWheel(Event event) {
		boolean up= event.count > 0;
		int documentHoverLine= fFocusLine;
		
		ILineRange nextWidgetRange= null;
		ILineRange last= null;
		if (up) {
			outer: for (Iterator it= fFocusRevision.getRegions().iterator(); it.hasNext();) {
				ChangeRegion region= (ChangeRegion) it.next();
				for (Iterator regions= region.getAdjustedRanges().iterator(); regions.hasNext();) {
					ILineRange range= (ILineRange) regions.next();

					ILineRange widgetRange= modelLinesToWidgetLines(range);
					if (contains(range, documentHoverLine)) {
						nextWidgetRange= last;
						break outer;
					}
					if (widgetRange != null)
						last= widgetRange;
				}
			}
		} else {
			outer: for (ListIterator it= fFocusRevision.getRegions().listIterator(fFocusRevision.getRegions().size()); it.hasPrevious();) {
				ChangeRegion region= (ChangeRegion) it.previous();
				for (ListIterator regions= region.getAdjustedRanges().listIterator(region.getAdjustedRanges().size()); regions.hasPrevious();) {
					ILineRange range= (ILineRange) regions.previous();
					
					ILineRange widgetRange= modelLinesToWidgetLines(range);
					if (contains(range, documentHoverLine)) {
						nextWidgetRange= last;
						break outer;
					}
					if (widgetRange != null)
						last= widgetRange;
				}
			}
		}
		
		if (nextWidgetRange == null)
			return;
		
		int widgetCurrentFocusLine= modelLinesToWidgetLines(new LineRange(documentHoverLine, 1)).getStartLine();
		int widgetNextFocusLine= nextWidgetRange.getStartLine();
		int newTopPixel= fCachedTextWidget.getTopPixel() + fCachedTextWidget.getLineHeight() * (widgetNextFocusLine - widgetCurrentFocusLine);
		fCachedTextWidget.setTopPixel(newTopPixel);
		if (newTopPixel < 0) {
			Point cursorLocation= fCachedTextWidget.getDisplay().getCursorLocation();
			cursorLocation.y += newTopPixel;
			fCachedTextWidget.getDisplay().setCursorLocation(cursorLocation);
		} else {
			int topPixel= fCachedTextWidget.getTopPixel();
			if (topPixel < newTopPixel) {
				Point cursorLocation= fCachedTextWidget.getDisplay().getCursorLocation();
				cursorLocation.y += newTopPixel - topPixel;
				fCachedTextWidget.getDisplay().setCursorLocation(cursorLocation);
			}
		}
		updateFocusLine(toDocumentLineNumber(fCachedTextWidget.toControl(fCachedTextWidget.getDisplay().getCursorLocation()).y));
		fParentRuler.immediateUpdate();
	}
}
