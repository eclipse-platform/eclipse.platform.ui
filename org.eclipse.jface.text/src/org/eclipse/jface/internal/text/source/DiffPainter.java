/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.ILineDifferExtension2;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.IVerticalRulerColumn;


/**
 * A strategy for painting the quick diff colors onto the vertical ruler column. It also manages the
 * quick diff hover.
 *
 * @since 3.2
 */
public final class DiffPainter {
	/**
	 * Internal listener class that will update the ruler when the underlying model changes.
	 */
	private class AnnotationListener implements IAnnotationModelListener {
		/*
		 * @see org.eclipse.jface.text.source.IAnnotationModelListener#modelChanged(org.eclipse.jface.text.source.IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			postRedraw();
		}
	}

	/** The vertical ruler column that delegates painting to this painter. */
	private final IVerticalRulerColumn fColumn;
	/** The parent ruler. */
	private CompositeRuler fParentRuler;
	/** The column's control, typically a {@link Canvas}, possibly <code>null</code>. */
	private Control fControl;
	/** The text viewer that the column is attached to. */
	private ITextViewer fViewer;
	/** The viewer's text widget. */
	private StyledText fWidget;
	/** The line differ extracted from the annotation model. */
	private ILineDiffer fLineDiffer= null;
	/** Color for changed lines */
	private Color fAddedColor;
	/** Color for added lines */
	private Color fChangedColor;
	/** Color for the deleted line indicator */
	private Color fDeletedColor;
	/** The background color. */
	private Color fBackground;
	/** The ruler's hover */
	private IAnnotationHover fHover;
	/** The internal listener */
	private final AnnotationListener fAnnotationListener= new AnnotationListener();
	/** The shared color provider, possibly <code>null</code>. */
	private final ISharedTextColors fSharedColors;

	/**
	 * Creates a new diff painter for a vertical ruler column.
	 *
	 * @param column the column that will delegate{@link #paint(GC, ILineRange) painting} to the
	 *        newly created painter.
	 * @param sharedColors a shared colors object to store shaded colors in, may be
	 *        <code>null</code>
	 */
	public DiffPainter(IVerticalRulerColumn column, ISharedTextColors sharedColors) {
		Assert.isLegal(column != null);
		fColumn= column;
		fSharedColors= sharedColors;
	}

	/**
	 * Sets the parent ruler - the delegating column must call this method as soon as it creates its
	 * control.
	 *
	 * @param parentRuler the parent ruler
	 */
	public void setParentRuler(CompositeRuler parentRuler) {
		fParentRuler= parentRuler;
	}

	/**
	 * Sets the quick diff hover later returned by {@link #getHover()}.
	 *
	 * @param hover the hover
	 */
	public void setHover(IAnnotationHover hover) {
		fHover= hover;
	}

	/**
	 * Returns the quick diff hover set by {@link #setHover(IAnnotationHover)}.
	 *
	 * @return the quick diff hover set by {@link #setHover(IAnnotationHover)}
	 */
	public IAnnotationHover getHover() {
		return fHover;
	}

	/**
	 * Sets the background color.
	 *
	 * @param background the background color, <code>null</code> to use the platform's list background
	 */
	public void setBackground(Color background) {
		fBackground= background;
	}

	/**
	 * Delegates the painting of the quick diff colors to this painter. The painter will draw the
	 * color boxes onto the passed {@link GC} for all model (document) lines in
	 * <code>visibleModelLines</code>.
	 *
	 * @param gc the {@link GC} to draw onto
	 * @param visibleModelLines the lines (in document offsets) that are currently (perhaps only
	 *        partially) visible
	 */
	public void paint(GC gc, ILineRange visibleModelLines) {
		connectIfNeeded();
		if (!isConnected())
			return;

		// draw diff info
		final int lastLine= end(visibleModelLines);
		final int width= getWidth();
		final Color deletionColor= getDeletionColor();
		for (int line= visibleModelLines.getStartLine(); line < lastLine; line++) {
			paintLine(line, gc, width, deletionColor);
		}
	}

	/**
	 * Ensures that the column is fully instantiated, i.e. has a control, and that the viewer is
	 * visible.
	 */
	private void connectIfNeeded() {
		if (isConnected() || fParentRuler == null)
			return;

		fViewer= fParentRuler.getTextViewer();
		if (fViewer == null)
			return;

		fWidget= fViewer.getTextWidget();
		if (fWidget == null)
			return;

		fControl= fColumn.getControl();
		if (fControl == null)
			return;

		fControl.addDisposeListener(new DisposeListener() {
			/*
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				handleDispose();
			}
		});
	}

	/**
	 * Returns <code>true</code> if the column is fully connected.
	 *
	 * @return <code>true</code> if the column is fully connected, false otherwise
	 */
	private boolean isConnected() {
		return fControl != null;
	}

	/**
	 * Disposes of this painter and releases any resources.
	 */
	private void handleDispose() {
		if (fLineDiffer != null) {
			((IAnnotationModel) fLineDiffer).removeAnnotationModelListener(fAnnotationListener);
			fLineDiffer= null;
		}
	}

	/**
	 * Paints a single model line onto <code>gc</code>.
	 *
	 * @param line the model line to paint
	 * @param gc the {@link GC} to paint onto
	 * @param width the width of the column
	 * @param deletionColor the background color used to indicate deletions
	 */
	private void paintLine(int line, GC gc, int width, Color deletionColor) {
		int widgetLine= JFaceTextUtil.modelLineToWidgetLine(fViewer, line);
		if (widgetLine == -1)
			return;

		ILineDiffInfo info= getDiffInfo(line);

		if (info != null) {
			int y= fWidget.getLinePixel(widgetLine);
			int lineHeight= fWidget.getLineHeight(fWidget.getOffsetAtLine(widgetLine));

			// draw background color if special
			if (hasSpecialColor(info)) {
				gc.setBackground(getColor(info));
				gc.fillRectangle(0, y, width, lineHeight);
			}

			/* Deletion Indicator: Simply a horizontal line */
			int delBefore= info.getRemovedLinesAbove();
			int delBelow= info.getRemovedLinesBelow();
			if (delBefore > 0 || delBelow > 0) {
				gc.setForeground(deletionColor);
				if (delBefore > 0)
					gc.drawLine(0, y, width, y);
				if (delBelow > 0)
					gc.drawLine(0, y + lineHeight - 1, width, y + lineHeight - 1);
			}
		}
	}

	/**
	 * Returns whether the line background differs from the default.
	 *
	 * @param info the info being queried
	 * @return <code>true</code> if <code>info</code> describes either a changed or an added
	 *         line.
	 */
	private boolean hasSpecialColor(ILineDiffInfo info) {
		return info.getChangeType() == ILineDiffInfo.ADDED || info.getChangeType() == ILineDiffInfo.CHANGED;
	}

	/**
	 * Retrieves the <code>ILineDiffInfo</code> for <code>line</code> from the model. There are
	 * optimizations for direct access and sequential access patterns.
	 *
	 * @param line the line we want the info for.
	 * @return the <code>ILineDiffInfo</code> for <code>line</code>, or <code>null</code>.
	 */
	private ILineDiffInfo getDiffInfo(int line) {
		if (fLineDiffer != null)
			return fLineDiffer.getLineInfo(line);

		return null;
	}

	/**
	 * Returns the color for deleted lines.
	 *
	 * @return the color to be used for the deletion indicator
	 */
	private Color getDeletionColor() {
		return fDeletedColor == null ? getBackground() : fDeletedColor;
	}

	/**
	 * Returns the color for the given line diff info.
	 *
	 * @param info the <code>ILineDiffInfo</code> being queried
	 * @return the correct background color for the line type being described by <code>info</code>
	 */
	private Color getColor(ILineDiffInfo info) {
		Assert.isTrue(info != null && info.getChangeType() != ILineDiffInfo.UNCHANGED);
		Color ret= null;
		switch (info.getChangeType()) {
			case ILineDiffInfo.CHANGED:
				ret= getShadedColor(fChangedColor);
				break;
			case ILineDiffInfo.ADDED:
				ret= getShadedColor(fAddedColor);
				break;
		}
		return ret == null ? getBackground() : ret;
	}

	/**
	 * Sets the background color for changed lines.
	 *
	 * @param color the new color to be used for the changed lines background
	 * @return the shaded color
	 */
	private Color getShadedColor(Color color) {
		if (color == null)
			return null;

		if (fSharedColors == null)
			return color;

		RGB baseRGB= color.getRGB();
		RGB background= getBackground().getRGB();

		boolean darkBase= isDark(baseRGB);
		boolean darkBackground= isDark(background);
		if (darkBase && darkBackground)
			background= new RGB(255, 255, 255);
		else if (!darkBase && !darkBackground)
			background= new RGB(0, 0, 0);

		return fSharedColors.getColor(interpolate(baseRGB, background, 0.6));
	}

	/**
	 * Sets the annotation model.
	 *
	 * @param model the annotation model, possibly <code>null</code>
	 * @see IVerticalRulerColumn#setModel(IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
		IAnnotationModel newModel;
		if (model instanceof IAnnotationModelExtension)
			newModel= ((IAnnotationModelExtension) model).getAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);
		else
			newModel= model;

		setDiffer(newModel);
	}

	/**
	 * Sets the line differ.
	 *
	 * @param differ the line differ
	 */
	private void setDiffer(IAnnotationModel differ) {
		if (differ instanceof ILineDiffer) {
			if (fLineDiffer != differ) {
				if (fLineDiffer != null)
					((IAnnotationModel) fLineDiffer).removeAnnotationModelListener(fAnnotationListener);
				fLineDiffer= (ILineDiffer) differ;
				if (fLineDiffer != null)
					((IAnnotationModel) fLineDiffer).addAnnotationModelListener(fAnnotationListener);
			}
		}
	}

	/**
	 * Triggers a redraw in the display thread.
	 */
	private final void postRedraw() {
		if (isConnected() && !fControl.isDisposed()) {
			Display d= fControl.getDisplay();
			if (d != null) {
				d.asyncExec(new Runnable() {
					public void run() {
						redraw();
					}
				});
			}
		}
	}

	/**
	 * Triggers redrawing of the column.
	 */
	private void redraw() {
		fColumn.redraw();
	}

	/**
	 * Returns the width of the column.
	 *
	 * @return the width of the column
	 */
	private int getWidth() {
		return fColumn.getWidth();
	}

	/**
	 * Computes the end index of a line range.
	 *
	 * @param range a line range
	 * @return the last line (exclusive) of <code>range</code>
	 */
	private static int end(ILineRange range) {
		return range.getStartLine() + range.getNumberOfLines();
	}

	/**
	 * Returns the System background color for list widgets or the set background.
	 *
	 * @return the System background color for list widgets
	 */
	private Color getBackground() {
		if (fBackground == null)
			return fWidget.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		return fBackground;
	}

	/**
	 * Sets the color for added lines.
	 *
	 * @param addedColor the color for added lines
	 * @see org.eclipse.jface.text.source.IChangeRulerColumn#setAddedColor(org.eclipse.swt.graphics.Color)
	 */
	public void setAddedColor(Color addedColor) {
		fAddedColor= addedColor;
	}

	/**
	 * Sets the color for changed lines.
	 *
	 * @param changedColor the color for changed lines
	 * @see org.eclipse.jface.text.source.IChangeRulerColumn#setChangedColor(org.eclipse.swt.graphics.Color)
	 */
	public void setChangedColor(Color changedColor) {
		fChangedColor= changedColor;
	}

	/**
	 * Sets the color for deleted lines.
	 *
	 * @param deletedColor the color for deleted lines
	 * @see org.eclipse.jface.text.source.IChangeRulerColumn#setDeletedColor(org.eclipse.swt.graphics.Color)
	 */
	public void setDeletedColor(Color deletedColor) {
		fDeletedColor= deletedColor;
	}

	/**
	 * Returns <code>true</code> if the receiver can provide a hover for a certain document line.
	 *
	 * @param activeLine the document line of interest
	 * @return <code>true</code> if the receiver can provide a hover
	 */
	public boolean hasHover(int activeLine) {
		return true;
	}

	/**
	 * Returns the display character for the accessibility mode for a certain model line.
	 *
	 * @param line the document line of interest
	 * @return the display character for <code>line</code>
	 */
	public String getDisplayCharacter(int line) {
		return getDisplayCharacter(getDiffInfo(line));
	}

	/**
	 * Returns the character to display in character display mode for the given
	 * <code>ILineDiffInfo</code>
	 *
	 * @param info the <code>ILineDiffInfo</code> being queried
	 * @return the character indication for <code>info</code>
	 */
	private String getDisplayCharacter(ILineDiffInfo info) {
		if (info == null)
			return " "; //$NON-NLS-1$
		switch (info.getChangeType()) {
			case ILineDiffInfo.CHANGED:
				return "~"; //$NON-NLS-1$
			case ILineDiffInfo.ADDED:
				return "+"; //$NON-NLS-1$
		}
		return " "; //$NON-NLS-1$
	}

	/**
	 * Returns a specification of a color that lies between the given foreground and background
	 * color using the given scale factor.
	 *
	 * @param fg the foreground color
	 * @param bg the background color
	 * @param scale the scale factor
	 * @return the interpolated color
	 */
	private static RGB interpolate(RGB fg, RGB bg, double scale) {
		return new RGB((int) ((1.0 - scale) * fg.red + scale * bg.red), (int) ((1.0 - scale) * fg.green + scale * bg.green), (int) ((1.0 - scale) * fg.blue + scale * bg.blue));
	}

	/**
	 * Returns the grey value in which the given color would be drawn in grey-scale.
	 *
	 * @param rgb the color
	 * @return the grey-scale value
	 */
	private static double greyLevel(RGB rgb) {
		if (rgb.red == rgb.green && rgb.green == rgb.blue)
			return rgb.red;
		return (0.299 * rgb.red + 0.587 * rgb.green + 0.114 * rgb.blue + 0.5);
	}

	/**
	 * Returns whether the given color is dark or light depending on the colors grey-scale level.
	 *
	 * @param rgb the color
	 * @return <code>true</code> if the color is dark, <code>false</code> if it is light
	 */
	private static boolean isDark(RGB rgb) {
		return greyLevel(rgb) > 128;
	}

	/**
	 * Returns <code>true</code> if diff information is being displayed, <code>false</code> otherwise.
	 *
     * @return <code>true</code> if diff information is being displayed, <code>false</code> otherwise
     * @since 3.3
     */
	public boolean hasInformation() {
		if (fLineDiffer instanceof ILineDifferExtension2)
			return !((ILineDifferExtension2) fLineDiffer).isSuspended();
		return fLineDiffer != null;
	}

}
