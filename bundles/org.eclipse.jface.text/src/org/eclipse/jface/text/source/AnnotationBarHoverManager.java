/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.internal.text.InformationControlReplacer;
import org.eclipse.jface.internal.text.InternalAccessor;

import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITextViewerExtension8.EnrichMode;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;


/**
 * This manager controls the layout, content, and visibility of an information
 * control in reaction to mouse hover events issued by the vertical ruler of a
 * source viewer.
 * @since 2.0
 */
public class AnnotationBarHoverManager extends AbstractHoverInformationControlManager {

	/**
	 * The information control closer for the hover information. Closes the information control as soon as the mouse pointer leaves the subject area, a mouse button is pressed, the user presses a key, or the subject control is resized or moved.
	 *
	 * @since 3.0
	 * @deprecated As of 3.4, no longer used as closer from super class is used
	 */
	@Deprecated
	protected class Closer extends MouseTrackAdapter implements IInformationControlCloser, MouseListener, MouseMoveListener, ControlListener, KeyListener, DisposeListener, ShellListener, Listener {

		/** The closer's subject control */
		private Control fSubjectControl;
		/** The subject area */
		private Rectangle fSubjectArea;
		/** Indicates whether this closer is active */
		private boolean fIsActive= false;
		/** The information control. */
		private IInformationControl fInformationControlToClose;
		/**
		 * <code>true</code> if a wheel handler is installed.
		 * @since 3.2
		 */
		private boolean fHasWheelFilter= false;
		/**
		 * The cached display.
		 * @since 3.2
		 */
		private Display fDisplay;


		/**
		 * Creates a new information control closer.
		 */
		public Closer() {
		}

		@Override
		public void setSubjectControl(Control control) {
			fSubjectControl= control;
		}

		/*
		 * @see IInformationControlCloser#setHoverControl(IHoverControl)
		 */
		@Override
		public void setInformationControl(IInformationControl control) {
			fInformationControlToClose= control;
		}

		@Override
		public void start(Rectangle subjectArea) {

			if (fIsActive) {
				return;
			}
			fIsActive= true;

			fSubjectArea= subjectArea;

			fInformationControlToClose.addDisposeListener(this);
			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.addMouseListener(this);
				fSubjectControl.addMouseMoveListener(this);
				fSubjectControl.addMouseTrackListener(this);
				fSubjectControl.getShell().addShellListener(this);
				fSubjectControl.addControlListener(this);
				fSubjectControl.addKeyListener(this);

				fDisplay= fSubjectControl.getDisplay();
				if (!fDisplay.isDisposed() && fHideOnMouseWheel) {
					fHasWheelFilter= true;
					fDisplay.addFilter(SWT.MouseHorizontalWheel, this);
					fDisplay.addFilter(SWT.MouseVerticalWheel, this);
				}
			}
		}

		@Override
		public void stop() {

			if (!fIsActive) {
				return;
			}
			fIsActive= false;

			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.removeMouseListener(this);
				fSubjectControl.removeMouseMoveListener(this);
				fSubjectControl.removeMouseTrackListener(this);
				fSubjectControl.getShell().removeShellListener(this);
				fSubjectControl.removeControlListener(this);
				fSubjectControl.removeKeyListener(this);
			}

			if (fDisplay != null && !fDisplay.isDisposed() && fHasWheelFilter) {
				fDisplay.removeFilter(SWT.MouseHorizontalWheel, this);
				fDisplay.removeFilter(SWT.MouseVerticalWheel, this);
			}
			fHasWheelFilter= false;

			fDisplay= null;

		}

		/**
		 * Stops the information control and if <code>delayRestart</code> is set allows restart only after a certain delay.
		 *
		 * @param delayRestart <code>true</code> if restart should be delayed
		 * @deprecated As of 3.4, replaced by {@link #stop()}. Note that <code>delayRestart</code> was never honored.
		 */
		@Deprecated
		protected void stop(boolean delayRestart) {
			stop();
		}

		@Override
		public void mouseMove(MouseEvent event) {
			if (!fSubjectArea.contains(event.x, event.y)) {
				hideInformationControl();
			}
		}

		@Override
		public void mouseUp(MouseEvent event) {
		}

		@Override
		public void mouseDown(MouseEvent event) {
			hideInformationControl();
		}

		@Override
		public void mouseDoubleClick(MouseEvent event) {
			hideInformationControl();
		}

		@Override
		public void handleEvent(Event event) {
			if (event.type == SWT.MouseHorizontalWheel || event.type == SWT.MouseVerticalWheel) {
				hideInformationControl();
			}
		}

		@Override
		public void mouseExit(MouseEvent event) {
			if (!fAllowMouseExit) {
				hideInformationControl();
			}
		}

		@Override
		public void controlResized(ControlEvent event) {
			hideInformationControl();
		}

		@Override
		public void controlMoved(ControlEvent event) {
			hideInformationControl();
		}

		@Override
		public void keyReleased(KeyEvent event) {
		}

		@Override
		public void keyPressed(KeyEvent event) {
			hideInformationControl();
		}

		@Override
		public void shellActivated(ShellEvent e) {
		}

		@Override
		public void shellClosed(ShellEvent e) {
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			hideInformationControl();
		}

		@Override
		public void shellDeiconified(ShellEvent e) {
		}

		@Override
		public void shellIconified(ShellEvent e) {
		}

		@Override
		public void widgetDisposed(DisposeEvent e) {
			hideInformationControl();
		}
	}

	/** The source viewer the manager is connected to */
	private final ISourceViewer fSourceViewer;
	/** The vertical ruler the manager is registered with */
	private final IVerticalRulerInfo fVerticalRulerInfo;
	/** The annotation hover the manager uses to retrieve the information to display. Can be <code>null</code>. */
	private final IAnnotationHover fAnnotationHover;
	/**
	 * Indicates whether the mouse cursor is allowed to leave the subject area without closing the hover.
	 * @since 3.0
	 */
	protected boolean fAllowMouseExit= false;
	/**
	 * Whether we should hide the over on mouse wheel action.
	 *
	 * @since 3.2
	 */
	private boolean fHideOnMouseWheel= true;

	/**
	 * The current annotation hover.
	 * @since 3.2
	 */
	private IAnnotationHover fCurrentHover;

	/**
	 * Creates an annotation hover manager with the given parameters. In addition,
	 * the hovers anchor is RIGHT and the margin is 5 points to the right.
	 *
	 * @param sourceViewer the source viewer this manager connects to
	 * @param ruler the vertical ruler this manager connects to
	 * @param annotationHover the annotation hover providing the information to be displayed
	 * @param creator the information control creator
	 * @deprecated As of 2.1, replaced by {@link AnnotationBarHoverManager#AnnotationBarHoverManager(IVerticalRulerInfo, ISourceViewer, IAnnotationHover, IInformationControlCreator)}
	 */
	@Deprecated
	public AnnotationBarHoverManager(ISourceViewer sourceViewer, IVerticalRuler ruler, IAnnotationHover annotationHover, IInformationControlCreator creator) {
		this(ruler, sourceViewer, annotationHover, creator);
	}

	/**
	 * Creates an annotation hover manager with the given parameters. In addition,
	 * the hovers anchor is RIGHT and the margin is 5 points to the right.
	 *
	 * @param rulerInfo the vertical ruler this manager connects to
	 * @param sourceViewer the source viewer this manager connects to
	 * @param annotationHover the annotation hover providing the information to be displayed or <code>null</code> if none
	 * @param creator the information control creator
	 * @since 2.1
	 */
	public AnnotationBarHoverManager(IVerticalRulerInfo rulerInfo, ISourceViewer sourceViewer, IAnnotationHover annotationHover, IInformationControlCreator creator) {
		super(creator);

		Assert.isNotNull(sourceViewer);

		fSourceViewer= sourceViewer;
		fVerticalRulerInfo= rulerInfo;
		fAnnotationHover= annotationHover;

		setAnchor(ANCHOR_RIGHT);
		setMargins(5, 0);
		// use closer from super class
	}

	@Override
	protected void computeInformation() {
		fAllowMouseExit= false;
		MouseEvent event= getHoverEvent();
		if ((event.stateMask & SWT.BUTTON_MASK) != 0) {
			setInformation(null, null);
			return;
		}
		IAnnotationHover hover= getHover(event);
		if (hover == null) {
			setInformation(null, null);
			return;
		}

		int line= getHoverLine(event);

		if (hover instanceof IAnnotationHoverExtension extension) {
			ILineRange range= extension.getHoverLineRange(fSourceViewer, line);
			setCustomInformationControlCreator(extension.getHoverControlCreator());
			range= adaptLineRange(range, line);
			if (range != null) {
				setInformation(extension.getHoverInfo(fSourceViewer, range, computeNumberOfVisibleLines()), computeArea(range));
			} else {
				setInformation(null, null);
			}

		} else {
			setCustomInformationControlCreator(null);
			setInformation(hover.getHoverInfo(fSourceViewer, line), computeArea(line));
		}

	}

	@Override
	protected void showInformationControl(Rectangle subjectArea) {
		super.showInformationControl(subjectArea);
		fCurrentHover= getHover(getHoverEvent());
	}

	@Override
	protected void hideInformationControl() {
		fCurrentHover= null;
		super.hideInformationControl();
	}

	/**
	 * Adapts a given line range so that the result is a line range that does
	 * not overlap with any collapsed region and fits into the view port of the
	 * attached viewer.
	 *
	 * @param lineRange the original line range
	 * @param line the anchor line
	 * @return the adapted line range
	 * @since 3.0
	 */
	private ILineRange adaptLineRange(ILineRange lineRange, int line) {
		if (lineRange != null) {
			lineRange= adaptLineRangeToFolding(lineRange, line);
			if (lineRange != null) {
				return adaptLineRangeToViewport(lineRange);
			}
		}
		return null;
	}

	/**
	 * Adapts a given line range so that the result is a line range that does
	 * not overlap with any collapsed region of the attached viewer.
	 *
	 * @param lineRange the original line range
	 * @param line the anchor line
	 * @return the adapted line range
	 * @since 3.0
	 */
	private ILineRange adaptLineRangeToFolding(ILineRange lineRange, int line) {

		if (fSourceViewer instanceof ITextViewerExtension5 extension) {
			try {
				IRegion region= convertToRegion(lineRange);
				IRegion[] coverage= extension.getCoveredModelRanges(region);
				if (coverage != null && coverage.length > 0) {
					IRegion container= findRegionContainingLine(coverage, line);
					if (container != null) {
						return convertToLineRange(container);
					}
				}

			} catch (BadLocationException x) {
			}

			return null;
		}

		return lineRange;
	}

	/**
	 * Adapts a given line range so that the result is a line range that fits
	 * into the view port of the attached viewer.
	 *
	 * @param lineRange the original line range
	 * @return the adapted line range
	 * @since 3.0
	 */
	private ILineRange adaptLineRangeToViewport(ILineRange lineRange) {

		try {
			StyledText text= fSourceViewer.getTextWidget();

			int topLine= text.getTopIndex();
			int rangeTopLine= getWidgetLineNumber(lineRange.getStartLine());
			int topDelta= Math.max(topLine - rangeTopLine, 0);

			Rectangle size= text.getClientArea();
			Rectangle trim= text.computeTrim(0, 0, 0, 0);
			int height= size.height - trim.height;

			int lines= JFaceTextUtil.getLineIndex(text, height) - text.getTopIndex();

			int bottomLine= topLine + lines;

			int rangeBottomLine= getWidgetLineNumber(lineRange.getStartLine() + lineRange.getNumberOfLines() - 1);
			int bottomDelta= Math.max(rangeBottomLine - bottomLine, 0);

			return new LineRange(lineRange.getStartLine() + topDelta, lineRange.getNumberOfLines() - bottomDelta - topDelta);

		} catch (BadLocationException ex) {
		}

		return null;
	}

	/**
	 * Converts a line range into a character range.
	 *
	 * @param lineRange the line range
	 * @return the corresponding character range
	 * @throws BadLocationException in case the given line range is invalid
	 */
	private IRegion convertToRegion(ILineRange lineRange) throws BadLocationException {
		IDocument document= fSourceViewer.getDocument();
		int startOffset= document.getLineOffset(lineRange.getStartLine());
		int endLine= lineRange.getStartLine() + Math.max(0, lineRange.getNumberOfLines() - 1);
		IRegion lineInfo= document.getLineInformation(endLine);
		int endOffset= lineInfo.getOffset() + lineInfo.getLength();
		return new Region(startOffset, endOffset - startOffset);
	}

	/**
	 * Returns the region out of the given set that contains the given line or
	 * <code>null</code>.
	 *
	 * @param regions the set of regions
	 * @param line the line
	 * @return the region of the set that contains the line
	 * @throws BadLocationException in case line is invalid
	 */
	private IRegion findRegionContainingLine(IRegion[] regions, int line) throws BadLocationException {
		IDocument document= fSourceViewer.getDocument();
		IRegion lineInfo= document.getLineInformation(line);
		for (IRegion region : regions) {
			if (TextUtilities.overlaps(region, lineInfo)) {
				return region;
			}
		}
		return null;
	}

	/**
	 * Converts a given character region into a line range.
	 *
	 * @param region the character region
	 * @return the corresponding line range
	 * @throws BadLocationException in case the given region in invalid
	 */
	private ILineRange convertToLineRange(IRegion region) throws BadLocationException {
		IDocument document= fSourceViewer.getDocument();
		int startLine= document.getLineOfOffset(region.getOffset());
		int endLine= document.getLineOfOffset(region.getOffset() + region.getLength());
		return new LineRange(startLine, endLine - startLine + 1);
	}

	/**
	 * Returns the visible area of the vertical ruler covered by the given line
	 * range.
	 *
	 * @param lineRange the line range
	 * @return the visible area
	 */
	private Rectangle computeArea(ILineRange lineRange) {
		try {
			StyledText text= fSourceViewer.getTextWidget();
			final int startLine= getWidgetLineNumber(lineRange.getStartLine());
			int y= JFaceTextUtil.computeLineHeight(text, 0, startLine, startLine) - text.getTopPixel();
			int height= JFaceTextUtil.computeLineHeight(text, startLine, startLine + lineRange.getNumberOfLines(), lineRange.getNumberOfLines());
			Point size= fVerticalRulerInfo.getControl().getSize();
			return new Rectangle(0, y, size.x, height);
		} catch (BadLocationException x) {
		}
		return null;
	}

	/**
	 * Returns the number of the currently visible lines.
	 *
	 * @return the number of the currently visible lines
	 */
	private int computeNumberOfVisibleLines() {
		StyledText textWidget= fSourceViewer.getTextWidget();
		int lineHeight= textWidget.getLineHeight();
		int clientAreaHeight= textWidget.getClientArea().height;
		return clientAreaHeight / lineHeight;
	}

	/**
	 * Determines the hover to be used to display information based on the source of the
	 * mouse hover event. If <code>fVerticalRulerInfo</code> is not a composite ruler, the
	 * standard hover is returned.
	 *
	 * @param event the source of the mouse hover event
	 * @return the hover depending on <code>source</code>, or <code>fAnnotationHover</code> if none can be found.
	 * @since 3.0
	 */
	private IAnnotationHover getHover(MouseEvent event) {
		if (event == null || event.getSource() == null) {
			return fAnnotationHover;
		}

		if (fVerticalRulerInfo instanceof CompositeRuler comp) {
			for (Iterator<IVerticalRulerColumn> it= comp.getDecoratorIterator(); it.hasNext();) {
				Object o= it.next();
				if (o instanceof IVerticalRulerInfoExtension && o instanceof IVerticalRulerInfo) {
					if (((IVerticalRulerInfo) o).getControl() == event.getSource()) {
						IAnnotationHover hover= ((IVerticalRulerInfoExtension) o).getHover();
						if (hover != null) {
							return hover;
						}
					}
				}
			}
		}
		return fAnnotationHover;
	}

	/**
	 * Returns the line of interest deduced from the mouse hover event.
	 *
	 * @param event a mouse hover event that triggered hovering
	 * @return the document model line number on which the hover event occurred or <code>-1</code> if there is no event
	 * @since 3.0
	 */
	private int getHoverLine(MouseEvent event) {
		return event == null ? -1 : fVerticalRulerInfo.toDocumentLineNumber(event.y);
	}

	/**
	 * Returns for the widget line number for the given document line number.
	 *
	 * @param line the absolute line number
	 * @return the line number relative to the viewer's visible region
	 * @throws BadLocationException if <code>line</code> is not valid in the viewer's document
	 */
	private int getWidgetLineNumber(int line) throws BadLocationException {
		if (fSourceViewer instanceof ITextViewerExtension5 extension) {
			return extension.modelLine2WidgetLine(line);
		}

		IRegion region= fSourceViewer.getVisibleRegion();
		int firstLine= fSourceViewer.getDocument().getLineOfOffset(region.getOffset());
		return line - firstLine;
	}

	/**
	 * Determines graphical area covered by the given line.
	 *
	 * @param line the number of the line in the viewer whose graphical extend in the vertical ruler must be computed
	 * @return the graphical extend of the given line
	 */
	private Rectangle computeArea(int line) {
		try {
			StyledText text= fSourceViewer.getTextWidget();
			int widgetLine= getWidgetLineNumber(line);
			int y= JFaceTextUtil.computeLineHeight(text, 0, widgetLine, widgetLine) - text.getTopPixel();
			Point size= fVerticalRulerInfo.getControl().getSize();
			return new Rectangle(0, y, size.x, text.getLineHeight(text.getOffsetAtLine(widgetLine)));
		} catch (IllegalArgumentException ex) {
		} catch (BadLocationException ex) {
		}
		return null;
	}

	/**
	 * Returns the annotation hover for this hover manager.
	 *
	 * @return the annotation hover for this hover manager or <code>null</code> if none
	 * @since 2.1
	 */
	protected IAnnotationHover getAnnotationHover() {
		return fAnnotationHover;
	}

	/**
	 * Returns the source viewer for this hover manager.
	 *
	 * @return the source viewer for this hover manager
	 * @since 2.1
	 */
	protected ISourceViewer getSourceViewer() {
		return fSourceViewer;
	}

	/**
	 * Returns the vertical ruler info for this hover manager
	 *
	 * @return the vertical ruler info for this hover manager
	 * @since 2.1
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fVerticalRulerInfo;
	}

	@Override
	protected Point computeSizeConstraints(Control subjectControl, Rectangle subjectArea, IInformationControl informationControl) {

		Point constraints= super.computeSizeConstraints(subjectControl, subjectArea, informationControl);

		// make as big as text area, if possible
		StyledText styledText= fSourceViewer.getTextWidget();
		if (styledText != null) {
			Rectangle r= styledText.getClientArea();
			if (r != null) {
				constraints.x= r.width;
				constraints.y= r.height;
			}
		}

		return constraints;
	}

	@Override
	protected Point computeInformationControlLocation(Rectangle subjectArea, Point controlSize) {
		MouseEvent event= getHoverEvent();
		IAnnotationHover hover= getHover(event);

		if (hover instanceof IAnnotationHoverExtension extension) {
			boolean allowMouseExit= extension.canHandleMouseCursor();
			if (allowMouseExit) {
				return computeLocation(subjectArea, controlSize, ANCHOR_RIGHT);
			}
		}
		return super.computeInformationControlLocation(subjectArea, controlSize);
	}

	@Override
	protected Point computeLocation(Rectangle subjectArea, Point controlSize, Anchor anchor) {
		MouseEvent event= getHoverEvent();
		IAnnotationHover hover= getHover(event);

		boolean allowMouseExit= false;
		if (hover instanceof IAnnotationHoverExtension extension) {
			allowMouseExit= extension.canHandleMouseCursor();
		}
		boolean hideOnMouseWheel= true;
		if (hover instanceof IAnnotationHoverExtension2 extension) {
			hideOnMouseWheel= !extension.canHandleMouseWheel();
		}
		fHideOnMouseWheel= hideOnMouseWheel;

		if (allowMouseExit) {
			fAllowMouseExit= true;

			Control subjectControl= getSubjectControl();
			// return a location that just overlaps the annotation on the bar
			if (anchor == AbstractInformationControlManager.ANCHOR_RIGHT) {
				return subjectControl.toDisplay(subjectArea.x - 4, subjectArea.y - 2);
			} else if (anchor == AbstractInformationControlManager.ANCHOR_LEFT) {
				return subjectControl.toDisplay(subjectArea.x + subjectArea.width - controlSize.x + 4, subjectArea.y - 2);
			}
		}

		fAllowMouseExit= false;
		return super.computeLocation(subjectArea, controlSize, anchor);
	}

	/**
	 * Returns the currently shown annotation hover or <code>null</code> if none
	 * hover is shown.
	 *
	 * @return the currently shown annotation hover or <code>null</code>
	 * @since 3.2
	 */
	public IAnnotationHover getCurrentAnnotationHover() {
		return fCurrentHover;
	}

	/**
	 * Returns an adapter that gives access to internal methods.
	 * <p>
	 * <strong>Note:</strong> This method is not intended to be referenced or overridden by clients.
	 * </p>
	 *
	 * @return the replaceable information control accessor
	 * @since 3.4
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 */
	@Override
	public InternalAccessor getInternalAccessor() {
		return new InternalAccessor() {
			@Override
			public IInformationControl getCurrentInformationControl() {
				return AnnotationBarHoverManager.super.getInternalAccessor().getCurrentInformationControl();
			}

			@Override
			public void setInformationControlReplacer(InformationControlReplacer replacer) {
				AnnotationBarHoverManager.super.getInternalAccessor().setInformationControlReplacer(replacer);
			}

			@Override
			public InformationControlReplacer getInformationControlReplacer() {
				return AnnotationBarHoverManager.super.getInternalAccessor().getInformationControlReplacer();
			}

			@Override
			public boolean canReplace(IInformationControl control) {
				return AnnotationBarHoverManager.super.getInternalAccessor().canReplace(control);
			}

			@Override
			public boolean isReplaceInProgress() {
				return AnnotationBarHoverManager.super.getInternalAccessor().isReplaceInProgress();
			}

			@Override
			public void replaceInformationControl(boolean takeFocus) {
				AnnotationBarHoverManager.super.getInternalAccessor().replaceInformationControl(takeFocus);
			}

			@Override
			public void cropToClosestMonitor(Rectangle bounds) {
				AnnotationBarHoverManager.super.getInternalAccessor().cropToClosestMonitor(bounds);
			}

			@Override
			public void setHoverEnrichMode(EnrichMode mode) {
				AnnotationBarHoverManager.super.getInternalAccessor().setHoverEnrichMode(mode);
			}

			@Override
			public boolean getAllowMouseExit() {
				return fAllowMouseExit;
			}
		};
	}
}

