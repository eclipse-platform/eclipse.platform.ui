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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import org.eclipse.jface.text.Assert;

/**
 * A vertical ruler column displaying line numbers and serving as a UI for quick diff.
 * Clients usually instantiate and configure object of this class.
 *
 * @since 3.0
 */
public final class LineNumberChangeRulerColumn extends LineNumberRulerColumn implements IVerticalRulerInfo, IVerticalRulerInfoExtension, IChangeRulerColumn {
	
	/** Width of the triangle displayed for deleted lines. */
	private final static int fTriangleWidth= 7;
	/** The height of the triangle displayed for deleted lines. */
	private final static int fTriangleHeight= 3;

	/**
	 * Internal listener class that will update the ruler when the underlying model changes.
	 */
	class AnnotationListener implements IAnnotationModelListener {
		/*
		 * @see org.eclipse.jface.text.source.IAnnotationModelListener#modelChanged(org.eclipse.jface.text.source.IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			postRedraw();
		}
	}

	/** Color for changed lines */
	private Color fAddedColor;
	/** Color for added lines */
	private Color fChangedColor;
	/** Color for the deleted line indicator */
	private Color fDeletedColor;
	/** The ruler's annotation model. */
	IAnnotationModel fAnnotationModel;
	/** The ruler's hover */
	private IAnnotationHover fHover;
	/** The internal listener */
	private AnnotationListener fAnnotationListener= new AnnotationListener();

	/*
	 * @see org.eclipse.jface.text.source.LineNumberRulerColumn#handleDispose()
	 */
	protected void handleDispose() {
		if (fAnnotationModel != null) {
			fAnnotationModel.removeAnnotationModelListener(fAnnotationListener);
			fAnnotationModel= null;
		}
		super.handleDispose();
	}

	/*
	 * @see org.eclipse.jface.text.source.LineNumberRulerColumn#paintLineHook(int, int, int, org.eclipse.swt.graphics.GC)
	 */
	protected void paintLine(int line, int y, int lineheight, GC gc) {
		ILineDiffInfo info= getDiffInfo(line);

		if (info != null) {
			// width of the column
			int width= getWidth();

			// draw background color if special
			if (hasSpecialColor(info)) {
				gc.setBackground(getColor(info));
				gc.fillRectangle(0, y, width, lineheight);
			}

			/* Deletion Indicator
			 * It consists of a line across the column and a triangle as shown for a deleted
			 * line below the line 50:
			 * ('x' means its colored)
			 * 
			 * 1, 2, 3 show the points of the triangle painted.
			 * 
			 *      0				  width
			 *		|					| 
			 * 		---------------------        - y
			 * 		|	 ---   --   	|
			 * 		|	|	  |  |		1_
			 * 		|	 --   |  |	   x|   ^
			 * 		|	   |  |  |	 xxx|   |  fTriangleHeight / 2
			 * 		|	---    --  xxxxx|   |
			 * 		xxxxxxxxxxxx0xxxxxxx2_  v    _   y + lineheight
			 * 					|		|
			 * 					<------>
			 * 					 fTriangleWidth	
			 */
			int delBefore= info.getRemovedLinesAbove();
			int delBelow= info.getRemovedLinesBelow();
			if (delBefore > 0 || delBelow > 0) {
				Color deletionColor= getDeletionColor();
				gc.setBackground(deletionColor);
				gc.setForeground(deletionColor);

				int[] triangle= new int[6];
				triangle[0]= width - fTriangleWidth;
				triangle[1]= y;
				triangle[2]= width;
				triangle[3]= y - fTriangleHeight;
				triangle[4]= width;
				triangle[5]= y + fTriangleHeight;

				if (delBefore > 0) {
					gc.drawLine(0, y, width, y);
					gc.fillPolygon(triangle);
				}

				if (delBelow > 0) {
					triangle[1] += lineheight;
					triangle[3] += lineheight;
					triangle[5] += lineheight;

					gc.drawLine(0, y + lineheight, width, y + lineheight);
					gc.fillPolygon(triangle);
				}
				gc.setForeground(getForeground());
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
		return info.getType() == ILineDiffInfo.ADDED || info.getType() == ILineDiffInfo.CHANGED;
	}

	/**
	 * Retrieves the <code>ILineDiffInfo</code> for <code>line</code> from the model.
	 * There are optimizations for direct access and sequential access patterns.
	 * 
	 * @param line the line we want the info for.
	 * @return the <code>ILineDiffInfo</code> for <code>line</code>, or <code>null</code>.
	 */
	private ILineDiffInfo getDiffInfo(int line) {
		if (fAnnotationModel == null)
			return null;

		// assume direct access
		if (fAnnotationModel instanceof ILineDiffer) {
			ILineDiffer differ= (ILineDiffer)fAnnotationModel;
			return differ.getLineInfo(line);
		}
		
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
		Assert.isTrue(info != null && info.getType() != ILineDiffInfo.UNCHANGED);
		Color ret= null;
		switch (info.getType()) {
			case ILineDiffInfo.CHANGED :
				ret= fChangedColor;
				break;
			case ILineDiffInfo.ADDED :
				ret= fAddedColor;
				break;
		}
		return ret == null ? getBackground() : ret;
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

	/**
	 * Sets the hover of this ruler column.
	 * @param hover the hover that will produce hover information text for this ruler column
	 */
	public void setHover(IAnnotationHover hover) {
		fHover= hover;
	}

	/*
	 * @see IVerticalRulerColumn#setModel(IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
		IAnnotationModel newModel;
		if (model instanceof IAnnotationModelExtension) {
			newModel= ((IAnnotationModelExtension)model).getAnnotationModel(QUICK_DIFF_MODEL_ID);
		} else {
			newModel= model;
		}
		if (fAnnotationModel != newModel) {
			if (fAnnotationModel != null) {
				fAnnotationModel.removeAnnotationModelListener(fAnnotationListener);
			}
			fAnnotationModel= newModel;
			if (fAnnotationModel != null) {
				fAnnotationModel.addAnnotationModelListener(fAnnotationListener);
			}
			redraw();
		}
	}

	/**
	 * Sets the background color for added lines. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
	 * 
	 * @param addedColor the new color to be used for the added lines background
	 */
	public void setAddedColor(Color addedColor) {
		fAddedColor= addedColor;
	}

	/**
	 * Sets the background color for changed lines. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
	 * 
	 * @param changedColor the new color to be used for the changed lines background
	 */
	public void setChangedColor(Color changedColor) {
		fChangedColor= changedColor;
	}

	/**
	 * Sets the color for the deleted lines indicator. The color has to be disposed of by the caller when
	 * the receiver is no longer used.
	 * 
	 * @param deletedColor the new color to be used for the deleted lines indicator.
	 */
	public void setDeletedColor(Color deletedColor) {
		fDeletedColor= deletedColor;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getModel()
	 */
	public IAnnotationModel getModel() {
		return fAnnotationModel;
	}
}
