/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ScrollBar;

import org.eclipse.jface.text.IInformationControlCreator;

/**
 * This manager controls the layout, content, and visibility of an information
 * control in reaction to mouse hover events issued by the overview ruler of a
 * source viewer.
 *
 * @since 2.1
 */
class OverviewRulerHoverManager extends AnnotationBarHoverManager {

	/**
	 * Creates an overview hover manager with the given parameters. In addition,
	 * the hovers anchor is RIGHT and the margin is 5 points to the right.
	 *
	 * @param ruler the overview ruler this manager connects to
	 * @param sourceViewer the source viewer this manager connects to
	 * @param annotationHover the annotation hover providing the information to be displayed
	 * @param creator the information control creator
	 */
	public OverviewRulerHoverManager(IOverviewRuler ruler, ISourceViewer sourceViewer, IAnnotationHover annotationHover, IInformationControlCreator creator) {
		super(ruler, sourceViewer, annotationHover, creator);
		setAnchor(ANCHOR_LEFT);
		StyledText textWidget= sourceViewer.getTextWidget();
		if (textWidget != null) {
			ScrollBar verticalBar= textWidget.getVerticalBar();
			if (verticalBar != null)
				setMargins(verticalBar.getSize().x, 5);
		}
	}

	/*
	 * @see AbstractHoverInformationControlManager#computeInformation()
	 */
	protected void computeInformation() {
		Point location= getHoverEventLocation();
		int line= getVerticalRulerInfo().toDocumentLineNumber(location.y);
		IAnnotationHover hover= getAnnotationHover();

		IInformationControlCreator controlCreator= null;
		if (hover instanceof IAnnotationHoverExtension)
			controlCreator= ((IAnnotationHoverExtension)hover).getHoverControlCreator();
		setCustomInformationControlCreator(controlCreator);

		setInformation(hover.getHoverInfo(getSourceViewer(), line), computeArea(location.y));
	}

	/**
	 * Determines graphical area covered for which the hover is valid.
	 *
	 * @param y y-coordinate in the vertical ruler
	 * @return the graphical extend where the hover is valid
	 */
	private Rectangle computeArea(int y) {
		// This is OK (see constructor)
		IOverviewRuler overviewRuler= (IOverviewRuler) getVerticalRulerInfo();

		int hover_height= overviewRuler.getAnnotationHeight();
		int hover_width= getVerticalRulerInfo().getControl().getSize().x;

		// Calculate y-coordinate for hover
		int hover_y= y;
		boolean hasAnnotation= true;
		while (hasAnnotation && hover_y > y - hover_height) {
			hover_y--;
			hasAnnotation= overviewRuler.hasAnnotation(hover_y);
		}
		hover_y++;

		return new Rectangle(0, hover_y, hover_width, hover_height);
	}
}
