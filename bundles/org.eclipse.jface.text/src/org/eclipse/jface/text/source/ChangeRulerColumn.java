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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.internal.text.revisions.RevisionPainter;
import org.eclipse.jface.internal.text.source.DiffPainter;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.revisions.IRevisionRulerColumn;
import org.eclipse.jface.text.revisions.RevisionInformation;

/**
 * A vertical ruler column displaying line numbers and serving as a UI for quick diff.
 * Clients instantiate and configure object of this class.
 *
 * @since 3.0
 */
public final class ChangeRulerColumn implements IChangeRulerColumn, IRevisionRulerColumn {
	/**
	 * Handles all the mouse interaction in this line number ruler column.
	 */
	private class MouseHandler implements MouseListener, MouseMoveListener {

		@Override
		public void mouseUp(MouseEvent event) {
		}

		@Override
		public void mouseDown(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
		}

		@Override
		public void mouseDoubleClick(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
		}

		@Override
		public void mouseMove(MouseEvent event) {
			fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
		}
	}

	/**
	 * Internal listener class.
	 */
	private class InternalListener implements IViewportListener, ITextListener {

		@Override
		public void viewportChanged(int verticalPosition) {
			if (verticalPosition != fScrollPos)
				redraw();
		}

		@Override
		public void textChanged(TextEvent event) {

			if (!event.getViewerRedrawState())
				return;

			if (fSensitiveToTextChanges || event.getDocumentEvent() == null)
				postRedraw();

		}
	}

	/**
	 * The view(port) listener.
	 */
	private final InternalListener fInternalListener= new InternalListener();
	/**
	 * The mouse handler.
	 * @since 3.2
	 */
	private final MouseHandler fMouseHandler= new MouseHandler();
	/**
	 * The revision painter.
	 * @since 3.2
	 */
	private final RevisionPainter fRevisionPainter;
	/**
	 * The diff info painter.
	 * @since 3.2
	 */
	private final DiffPainter fDiffPainter;

	/** This column's parent ruler */
	private CompositeRuler fParentRuler;
	/** Cached text viewer */
	private ITextViewer fCachedTextViewer;
	/** Cached text widget */
	private StyledText fCachedTextWidget;
	/** The columns canvas */
	private Canvas fCanvas;
	/** The background color */
	private Color fBackground;
	/** The ruler's annotation model. */
	private IAnnotationModel fAnnotationModel;
	/** The width of the change ruler column. */
	private static final int fWidth= 5;

	/** Cache for the actual scroll position in pixels */
	private int fScrollPos;
	/** The buffer for double buffering */
	private Image fBuffer;
	/** Indicates whether this column reacts on text change events */
	private boolean fSensitiveToTextChanges= false;

	/**
	 * Creates a new ruler column.
	 *
	 * @deprecated since 3.2 use {@link #ChangeRulerColumn(ISharedTextColors)} instead
	 */
	@Deprecated
	public ChangeRulerColumn() {
		fRevisionPainter= null;
		fDiffPainter= new DiffPainter(this, null);
	}

	/**
	 * Creates a new revision ruler column.
	 *
	 * @param sharedColors the colors to look up RGBs
	 * @since 3.2
	 */
	public ChangeRulerColumn(ISharedTextColors sharedColors) {
		Assert.isNotNull(sharedColors);
		fRevisionPainter= new RevisionPainter(this, sharedColors);
		fDiffPainter= new DiffPainter(this, null); // no shading
	}

	/**
	 * Returns the System background color for list widgets.
	 *
	 * @return the System background color for list widgets
	 */
	private Color getBackground() {
		if (fBackground == null)
			return fCachedTextWidget.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		return fBackground;
	}

	@Override
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {

		fParentRuler= parentRuler;
		fCachedTextViewer= parentRuler.getTextViewer();
		fCachedTextWidget= fCachedTextViewer.getTextWidget();

		fCanvas= new Canvas(parentControl, SWT.NONE);
		fCanvas.setBackground(getBackground());

		fCanvas.addPaintListener(event -> {
			if (fCachedTextViewer != null)
				doubleBufferPaint(event.gc);
		});

		fCanvas.addDisposeListener(e -> {
			handleDispose();
			fCachedTextViewer= null;
			fCachedTextWidget= null;
		});

		fCanvas.addListener(SWT.ZoomChanged, e -> {
			if (fBuffer != null) {
				fBuffer.dispose();
				fBuffer= null;
			}
		});

		fCanvas.addMouseListener(fMouseHandler);
		fCanvas.addMouseMoveListener(fMouseHandler);

		if (fCachedTextViewer != null) {

			fCachedTextViewer.addViewportListener(fInternalListener);
			fCachedTextViewer.addTextListener(fInternalListener);
		}

		fRevisionPainter.setParentRuler(parentRuler);
		fDiffPainter.setParentRuler(parentRuler);

		return fCanvas;
	}

	/**
	 * Disposes the column's resources.
	 */
	protected void handleDispose() {

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

	/**
	 * Returns the view port height in lines.
	 *
	 * @return the view port height in lines
	 * @deprecated as of 3.2 the number of lines in the viewport cannot be computed because
	 *             StyledText supports variable line heights
	 */
	@Deprecated
	protected int getVisibleLinesInViewport() {
		// Hack to reduce amount of copied code.
		return LineNumberRulerColumn.getVisibleLinesInViewport(fCachedTextWidget);
	}

	/**
	 * Returns <code>true</code> if the viewport displays the entire viewer contents, i.e. the
	 * viewer is not vertically scrollable.
	 *
	 * @return <code>true</code> if the viewport displays the entire contents, <code>false</code> otherwise
	 * @since 3.2
	 */
	protected final boolean isViewerCompletelyShown() {
		return JFaceTextUtil.isShowingEntireContents(fCachedTextWidget);
	}

	private void doPaint(GC gc, int width, int height) {
		gc.setFont(fCanvas.getFont());
		gc.setBackground(getBackground());
		gc.fillRectangle(0, 0, width, height);

		doPaint(gc);
	}

	/**
	 * Draws the ruler column.
	 *
	 * @param gc the GC to draw into
	 */
	private void doPaint(GC gc) {
		ILineRange visibleModelLines= computeVisibleModelLines();
		if (visibleModelLines == null)
			return;

		fSensitiveToTextChanges= isViewerCompletelyShown();

		fScrollPos= fCachedTextWidget.getTopPixel();

		fRevisionPainter.paint(gc, visibleModelLines);
		if (!fRevisionPainter.hasInformation()) // don't paint quick diff colors if revisions are painted
			fDiffPainter.paint(gc, visibleModelLines);
	}

	@Override
	public void redraw() {

		if (fCachedTextViewer != null && fCanvas != null && !fCanvas.isDisposed()) {
			if (VerticalRuler.AVOID_NEW_GC) {
				fCanvas.redraw();
			} else {
				GC gc= new GC(fCanvas);
				doubleBufferPaint(gc);
				gc.dispose();
			}
		}
	}

	@Override
	public void setFont(Font font) {
	}

	/**
	 * Returns the parent (composite) ruler of this ruler column.
	 *
	 * @return the parent ruler
	 * @since 3.0
	 */
	private CompositeRuler getParentRuler() {
		return fParentRuler;
	}

	@Override
	public int getLineOfLastMouseButtonActivity() {
		return getParentRuler().getLineOfLastMouseButtonActivity();
	}

	@Override
	public int toDocumentLineNumber(int y_coordinate) {
		return getParentRuler().toDocumentLineNumber(y_coordinate);
	}

	@Override
	public IAnnotationHover getHover() {
		int activeLine= getParentRuler().getLineOfLastMouseButtonActivity();
		if (fRevisionPainter.hasHover(activeLine))
			return fRevisionPainter.getHover();
		if (fDiffPainter.hasHover(activeLine))
			return fDiffPainter.getHover();
		return null;
	}

	@Override
	public void setHover(IAnnotationHover hover) {
		fRevisionPainter.setHover(hover);
		fDiffPainter.setHover(hover);
	}

	@Override
	public void setModel(IAnnotationModel model) {
		setAnnotationModel(model);
		fRevisionPainter.setModel(model);
		fDiffPainter.setModel(model);
	}

	private void setAnnotationModel(IAnnotationModel model) {
		if (fAnnotationModel != model)
			fAnnotationModel= model;
	}

	@Override
	public void setBackground(Color background) {
		fBackground= background;
		if (fCanvas != null && !fCanvas.isDisposed())
			fCanvas.setBackground(getBackground());
		fRevisionPainter.setBackground(background);
		fDiffPainter.setBackground(background);
	}

	@Override
	public void setAddedColor(Color addedColor) {
		fDiffPainter.setAddedColor(addedColor);
	}

	@Override
	public void setChangedColor(Color changedColor) {
		fDiffPainter.setChangedColor(changedColor);
	}

	@Override
	public void setDeletedColor(Color deletedColor) {
		fDiffPainter.setDeletedColor(deletedColor);
	}

	@Override
	public IAnnotationModel getModel() {
		return fAnnotationModel;
	}

	@Override
	public Control getControl() {
		return fCanvas;
	}

	@Override
	public int getWidth() {
		return fWidth;
	}

	/**
	 * Triggers a redraw in the display thread.
	 */
	protected final void postRedraw() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			Display d= fCanvas.getDisplay();
			if (d != null) {
				d.asyncExec(this::redraw);
			}
		}
	}

	@Override
	public void addVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Computes the document based line range visible in the text widget.
	 *
	 * @return the document based line range visible in the text widget
	 * @since 3.2
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
			int widgetTopLine= JFaceTextUtil.getPartialTopIndex(fCachedTextWidget);
			topLine= extension.widgetLine2ModelLine(widgetTopLine);

			coverage= extension.getModelCoverage();

		} else {
			topLine= JFaceTextUtil.getPartialTopIndex(fCachedTextViewer);
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
			return null;
		}

		ILineRange visibleModelLines= new LineRange(topLine, bottomLine - topLine + 1);
		return visibleModelLines;
	}

	@Override
	public void setRevisionInformation(RevisionInformation info) {
		fRevisionPainter.setRevisionInformation(info);
		fRevisionPainter.setBackground(getBackground());
	}

	/**
	 * Returns the revision selection provider.
	 *
	 * @return the revision selection provider
	 * @since 3.2
	 */
	public ISelectionProvider getRevisionSelectionProvider() {
		return fRevisionPainter.getRevisionSelectionProvider();
	}
}
