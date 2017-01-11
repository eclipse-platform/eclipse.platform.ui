/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.internal.text.revisions.RevisionPainter;
import org.eclipse.jface.internal.text.source.DiffPainter;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.revisions.IRevisionListener;
import org.eclipse.jface.text.revisions.IRevisionRulerColumn;
import org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension;
import org.eclipse.jface.text.revisions.RevisionInformation;

/**
 * A vertical ruler column displaying line numbers and serving as a UI for quick diff. Clients
 * usually instantiate and configure object of this class.
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LineNumberChangeRulerColumn extends LineNumberRulerColumn implements IChangeRulerColumn, IRevisionRulerColumn, IRevisionRulerColumnExtension {
	/** The ruler's annotation model. */
	private IAnnotationModel fAnnotationModel;
	/** <code>true</code> if changes should be displayed using character indications instead of background colors. */
	private boolean fCharacterDisplay;
	/**
	 * The revision painter strategy.
	 *
	 * @since 3.2
	 */
	private final RevisionPainter fRevisionPainter;
	/**
	 * The diff information painter strategy.
	 *
	 * @since 3.2
	 */
	private final DiffPainter fDiffPainter;
	/**
	 * Whether to show number or to behave like a change ruler column.
	 * @since 3.3
	 */
	private boolean fShowNumbers= true;

	/**
	 * Creates a new instance.
	 *
	 * @param sharedColors the shared colors provider to use
	 */
	public LineNumberChangeRulerColumn(ISharedTextColors sharedColors) {
		Assert.isNotNull(sharedColors);
		fRevisionPainter= new RevisionPainter(this, sharedColors);
		fDiffPainter= new DiffPainter(this, sharedColors);
	}

	@Override
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		Control control= super.createControl(parentRuler, parentControl);
		fRevisionPainter.setParentRuler(parentRuler);
		fDiffPainter.setParentRuler(parentRuler);
		return control;
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
	public void setModel(IAnnotationModel model) {
		setAnnotationModel(model);
		fRevisionPainter.setModel(model);
		fDiffPainter.setModel(model);
		updateNumberOfDigits();
		computeIndentations();
		layout(true);
		postRedraw();
	}

	private void setAnnotationModel(IAnnotationModel model) {
		if (fAnnotationModel != model)
			fAnnotationModel= model;
	}


	/**
	 * Sets the display mode of the ruler. If character mode is set to <code>true</code>, diff
	 * information will be displayed textually on the line number ruler.
	 *
	 * @param characterMode <code>true</code> if diff information is to be displayed textually.
	 */
	public void setDisplayMode(boolean characterMode) {
		if (characterMode != fCharacterDisplay) {
			fCharacterDisplay= characterMode;
			updateNumberOfDigits();
			computeIndentations();
			layout(true);
		}
	}

	@Override
	public IAnnotationModel getModel() {
		return fAnnotationModel;
	}

	@Override
	protected String createDisplayString(int line) {
		StringBuffer buffer= new StringBuffer();
		if (fShowNumbers)
			buffer.append(super.createDisplayString(line));
		if (fCharacterDisplay && getModel() != null)
			buffer.append(fDiffPainter.getDisplayCharacter(line));
		return buffer.toString();
	}

	@Override
	protected int computeNumberOfDigits() {
		int digits;
		if (fCharacterDisplay && getModel() != null) {
			if (fShowNumbers)
				digits= super.computeNumberOfDigits() + 1;
			else
				digits= 1;
		} else {
			if (fShowNumbers)
				digits= super.computeNumberOfDigits();
			else
				digits= 0;
		}
		if (fRevisionPainter.hasInformation())
			digits+= fRevisionPainter.getRequiredWidth();
		return digits;
	}

	@Override
	public void addVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	void doPaint(GC gc, ILineRange visibleLines) {
		Color foreground= gc.getForeground();
		if (visibleLines != null) {
			if (fRevisionPainter.hasInformation())
				fRevisionPainter.paint(gc, visibleLines);
			else if (fDiffPainter.hasInformation()) // don't paint quick diff colors if revisions are painted
				fDiffPainter.paint(gc, visibleLines);
		}
		gc.setForeground(foreground);
		if (fShowNumbers || fCharacterDisplay)
			super.doPaint(gc, visibleLines);
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
	public void setBackground(Color background) {
		super.setBackground(background);
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
	public void setRevisionInformation(RevisionInformation info) {
		fRevisionPainter.setRevisionInformation(info);
		updateNumberOfDigits();
		computeIndentations();
		layout(true);
		postRedraw();
	}

    @Override
	public ISelectionProvider getRevisionSelectionProvider() {
	    return fRevisionPainter.getRevisionSelectionProvider();
    }

    /*
     * @see org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension#setRenderingMode(org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension.RenderingMode)
     * @since 3.3
     */
    @Override
	public void setRevisionRenderingMode(RenderingMode renderingMode) {
		fRevisionPainter.setRenderingMode(renderingMode);
	}

	/**
	 * Sets the line number display mode.
	 *
	 * @param showNumbers <code>true</code> to show numbers, <code>false</code> to only show
	 *        diff / revision info.
	 * @since 3.3
	 */
    public void showLineNumbers(boolean showNumbers) {
    	if (fShowNumbers != showNumbers) {
    		fShowNumbers= showNumbers;
			updateNumberOfDigits();
			computeIndentations();
			layout(true);
    	}
    }

    @Override
	public int getWidth() {
   		int width= super.getWidth();
		return width > 0 ? width : 8; // minimal width to display quick diff / revisions if no textual info is shown
    }

    /**
	 * Returns <code>true</code> if the ruler is showing line numbers, <code>false</code>
	 * otherwise
	 *
	 * @return <code>true</code> if line numbers are shown, <code>false</code> otherwise
	 * @since 3.3
	 */
	public boolean isShowingLineNumbers() {
		return fShowNumbers;
	}

	/**
	 * Returns <code>true</code> if the ruler is showing revision information, <code>false</code>
	 * otherwise
	 *
	 * @return <code>true</code> if revision information is shown, <code>false</code> otherwise
	 * @since 3.3
	 */
	public boolean isShowingRevisionInformation() {
		return fRevisionPainter.hasInformation();
	}

	/**
	 * Returns <code>true</code> if the ruler is showing change information, <code>false</code>
	 * otherwise
	 *
	 * @return <code>true</code> if change information is shown, <code>false</code> otherwise
	 * @since 3.3
	 */
	public boolean isShowingChangeInformation() {
		return fDiffPainter.hasInformation();
	}

	@Override
	public void showRevisionAuthor(boolean show) {
		fRevisionPainter.showRevisionAuthor(show);
		updateNumberOfDigits();
		computeIndentations();
		layout(true);
		postRedraw();
	}

	@Override
	public void showRevisionId(boolean show) {
		fRevisionPainter.showRevisionId(show);
		updateNumberOfDigits();
		computeIndentations();
		layout(true);
		postRedraw();
	}

	@Override
	public void addRevisionListener(IRevisionListener listener) {
		fRevisionPainter.addRevisionListener(listener);
	}

	@Override
	public void removeRevisionListener(IRevisionListener listener) {
		fRevisionPainter.removeRevisionListener(listener);
	}

	@Override
	protected void handleDispose() {
		fRevisionPainter.setParentRuler(null);
		fRevisionPainter.setModel(null);
		fDiffPainter.setParentRuler(null);
		fDiffPainter.setModel(null);
		super.handleDispose();
	}

	@Override
	void handleMouseScrolled(MouseEvent e) {
		if (fRevisionPainter.isWheelHandlerInstalled()) {
			return; // scroll event is already handled; don't interfere
		}
		super.handleMouseScrolled(e);
	}
}
