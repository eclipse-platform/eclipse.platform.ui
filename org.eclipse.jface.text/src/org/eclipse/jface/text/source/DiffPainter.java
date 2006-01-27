/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.internal.text.JFaceTextUtil;

/**
 * 
 * @since 3.2
 */
final class DiffPainter {
	
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

	private final IVerticalRulerColumn fColumn;
	private CompositeRuler fParentRuler;
	private Control fControl;
	private ITextViewer fViewer;
	private StyledText fWidget;
	private ILineDiffer fLineDiffer= null;
	/** Color for changed lines */
	private Color fAddedColor;
	/** Color for added lines */
	private Color fChangedColor;
	/** Color for the deleted line indicator */
	private Color fDeletedColor;
	/** The ruler's annotation model. */
	private IAnnotationModel fAnnotationModel;
	/** The ruler's hover */
	private IAnnotationHover fHover;
	private Color fBackground;
	/** The internal listener */
	private final AnnotationListener fAnnotationListener= new AnnotationListener();
	private final ISharedTextColors fSharedColors;
	
	public DiffPainter(IVerticalRulerColumn column, ISharedTextColors sharedColors) {
		fColumn= column;
		fSharedColors= sharedColors;
	}

	public void setParentRuler(CompositeRuler parentRuler) {
		fParentRuler= parentRuler;
	}
	
	public void setHover(IAnnotationHover hover) {
		fHover= hover;
	}
	
	public IAnnotationHover getHover() {
		return fHover;
	}
	
	public void setBackground(Color background) {
		fBackground= background;
	}
	
	public void paint(GC gc, ILineRange visibleModelLines) {
		getWidgets();
		if (fWidget == null)
			return;
		
		// draw diff info
		final int lastLine= end(visibleModelLines);
		for (int line= visibleModelLines.getStartLine(); line < lastLine; line++) {
			paintLine(line, gc);
		}
	}

	private void getWidgets() {
		if (fWidget == null && fParentRuler != null) {
			fViewer= fParentRuler.getTextViewer();
			fWidget= fViewer.getTextWidget();

			fControl= fColumn.getControl();
			fControl.addDisposeListener(new DisposeListener() {
				/*
				 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
				 */
				public void widgetDisposed(DisposeEvent e) {
					handleDispose();
				}

			});
		}
		
	}
	
	private void handleDispose() {
		if (fLineDiffer != null) {
			((IAnnotationModel) fLineDiffer).removeAnnotationModelListener(fAnnotationListener);
			fLineDiffer= null;
		}
	}
	
	private void paintLine(int line, GC gc) {
		int widgetLine= JFaceTextUtil.modelLineToWidgetLine(fViewer, line);
		if (widgetLine == -1)
			return;
	
		ILineDiffInfo info= getDiffInfo(line);
	
		if (info != null) {
			// width of the column
			int width= getWidth();
			
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
				Color deletionColor= getDeletionColor();
				gc.setForeground(deletionColor);
	
				if (delBefore > 0) {
					gc.drawLine(0, y, width, y);
				}
	
				if (delBelow > 0) {
					gc.drawLine(0, y + lineHeight - 1, width, y + lineHeight - 1);
				}
			}
		}
	}

	/**
	 * Returns whether the line background differs from the default.
	 *
	 * @param info the info being queried
	 * @return <code>true</code> if <code>info</code> describes either a changed or an added line.
	 */
	private boolean hasSpecialColor(ILineDiffInfo info) {
		return info.getChangeType() == ILineDiffInfo.ADDED || info.getChangeType() == ILineDiffInfo.CHANGED;
	}

	/**
	 * Retrieves the <code>ILineDiffInfo</code> for <code>line</code> from the model.
	 * There are optimizations for direct access and sequential access patterns.
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
			case ILineDiffInfo.CHANGED :
				ret= getShadedColor(fChangedColor);
				break;
			case ILineDiffInfo.ADDED :
				ret= getShadedColor(fAddedColor);
				break;
		}
		return ret == null ? getBackground() : ret;
	}
	
	/**
	 * Sets the background color for changed lines. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
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


	/*
	 * @see IVerticalRulerColumn#setModel(IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
		IAnnotationModel newModel;
		if (model instanceof IAnnotationModelExtension)
			newModel= ((IAnnotationModelExtension)model).getAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);
		else
			newModel= model;
		
		setDiffer(newModel);
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

	/**
	 * Triggers a redraw in the display thread.
	 */
	private final void postRedraw() {
		if (fControl != null && !fControl.isDisposed()) {
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
	
	private void redraw() {
		fColumn.redraw();
	}
	
	private int getWidth() {
		return fColumn.getWidth();
	}

	private static int end(ILineRange range) {
		return range.getStartLine() + range.getNumberOfLines();
	}
	
	/**
	 * Returns the System background color for list widgets.
	 *
	 * @return the System background color for list widgets
	 */
	private Color getBackground() {
		if (fBackground == null)
			return fWidget.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		return fBackground;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IChangeRulerColumn#setAddedColor(org.eclipse.swt.graphics.Color)
	 */
	public void setAddedColor(Color addedColor) {
		fAddedColor= addedColor;
	}

	/*
	 * @see org.eclipse.jface.text.source.IChangeRulerColumn#setChangedColor(org.eclipse.swt.graphics.Color)
	 */
	public void setChangedColor(Color changedColor) {
		fChangedColor= changedColor;
	}

	/*
	 * @see org.eclipse.jface.text.source.IChangeRulerColumn#setDeletedColor(org.eclipse.swt.graphics.Color)
	 */
	public void setDeletedColor(Color deletedColor) {
		fDeletedColor= deletedColor;
	}

	public boolean hasHover(int activeLine) {
		return true;
	}

	String getDisplayCharacter(int line) {
		return getDisplayCharacter(getDiffInfo(line));
	}
	
	/**
	 * Returns the character to display in character display mode for the given <code>ILineDiffInfo</code>
	 *
	 * @param info the <code>ILineDiffInfo</code> being queried
	 * @return the character indication for <code>info</code>
	 */
	private String getDisplayCharacter(ILineDiffInfo info) {
		if (info == null)
			return ""; //$NON-NLS-1$
		switch (info.getChangeType()) {
			case ILineDiffInfo.CHANGED :
				return "~"; //$NON-NLS-1$
			case ILineDiffInfo.ADDED :
				return "+"; //$NON-NLS-1$
		}
		return " "; //$NON-NLS-1$
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
	private static RGB interpolate(RGB fg, RGB bg, double scale) {
		return new RGB(
			(int) ((1.0-scale) * fg.red + scale * bg.red),
			(int) ((1.0-scale) * fg.green + scale * bg.green),
			(int) ((1.0-scale) * fg.blue + scale * bg.blue)
		);
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
		return  (0.299 * rgb.red + 0.587 * rgb.green + 0.114 * rgb.blue + 0.5);
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

}
